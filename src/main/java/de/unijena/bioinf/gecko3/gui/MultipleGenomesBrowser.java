/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.gui;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.*;
import de.unijena.bioinf.gecko3.event.*;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the multiple genome browser (mgb) for the de.unijena.bioinf.gecko3 GUI.
 * The mbg is implemented a container for GenomeBrowser objects and provides
 * functions for setting up the mgb.
 * 
 * This class contains modifications (genomeBrowserFilter, related variables and getter/setter)
 * made by Hans-Martin Haase <hans-martin.haase@uni-jena.de>
 * 
 * @author original author unknown
 */
public class MultipleGenomesBrowser extends JPanel implements Scrollable, ClusterSelectionListener {
    private final JSplitPane split;
    private final JPanel left;
    private final JPanel right;

	private final List<AbstractGenomeBrowser> genomeBrowsers;
    private final List<GenomeLabel> genomeLabels;
    private final List<GenomeFilterBox> genomeFilterBoxes;
    private final List<GBNavigator> gbNavigators;
	private final ScrollListener wheelListener;
	private final GeckoInstance gecko;

    /*
     * Some Size values for the gui
     */
    public static final int DEFAULT_GENE_HEIGHT = 20;
    private static final int MAX_GENEELEMENT_HIGHT = 40;
    private static final int MIN_GENEELEMENT_HIGHT = 9;
    private int geneElementHeight = DEFAULT_GENE_HEIGHT;

    private static final Color COLOR_HIGHLIGHT_DEFAULT = new Color(120,120,254);
    private static final Color COLOR_HIGHLIGHT_REFCLUST = Color.RED;

    /**
     * All information about the selected cluster, which sub locations are selected, etc.
     */
    private GeneClusterLocationSelection clusterLocationSelection;

    /**
     * What name is shown for each gene
     */
    private GenomePainting.NameType nameType = GenomePainting.NameType.ID;
	
	/**
	 * The variable stores the filter activity: <br>
	 * false filter is inactive
	 * true filter is active
	 */
	private boolean filterNonContainedGenomes;

    public enum GenomeFilterMode {
        None("No Filter"), Include("Include"), Exclude("Exclude");

        private final String toString;

        GenomeFilterMode(String string){
            toString = string;
        }

        @Override
        public String toString() {
            return toString;
        }
    }
	
	public MultipleGenomesBrowser() {
        super(new BorderLayout());
		gecko = GeckoInstance.getInstance();

        left = new JPanel(new BorderLayout());
        left.setBackground(Color.WHITE);

        right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);

        wheelListener = new ScrollListener();
        right.addMouseWheelListener(this.wheelListener);

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setBackground(Color.WHITE);

        setBackground(Color.WHITE);
        this.add(split);

        addSelectionListener(this);

		genomeBrowsers = new ArrayList<>();
		gbNavigators = new ArrayList<>();
        genomeLabels = new ArrayList<>();
        genomeFilterBoxes = new ArrayList<>();

        filterNonContainedGenomes = false;
	}

	/**
	 * This method implements a filter for the mgb. It removes the GenomeBrowers for the genomes
	 * which don't support the current cluster.
	 * For this the method sets the filterActive variable and the clusterBackup to undo the view change.
	 * So the method activates and deactivates the filter.
	 *
	 * @param filter true if the filter should be activated else false to turn it off
	 */
	public void hideNonClusteredGenomes(boolean filter)	{
        if (!filterNonContainedGenomes && !filter) // nothing to do
            return;
        filterNonContainedGenomes = filter;
        applyHideNonClusteredGenomesFilter();
	}

    private void applyHideNonClusteredGenomesFilter() {
        if (clusterLocationSelection != null && clusterLocationSelection.getCluster() != null) {
            if (filterNonContainedGenomes) {
                // activation branch
                for (int i = 0; i < clusterLocationSelection.getTotalGenomeNumber(); i++) {
                    if (clusterLocationSelection.getSubsequence(i) == null) {
                        setVisibleForGenomeBrowser(i, false);
                    } else {
                        setVisibleForGenomeBrowser(i, true);
                    }
                }
            } else if (!filterNonContainedGenomes) {
                for (int i = 0; i < clusterLocationSelection.getTotalGenomeNumber(); i++) {
                    setVisibleForGenomeBrowser(i, true);
                }
            }
        }
        revalidate();
    }

    /**
     * @param index
     * @param visible
     * @return
     */
    private void setVisibleForGenomeBrowser(int index, boolean visible){
        genomeBrowsers.get(index).setVisible(visible);
        genomeLabels.get(index).setVisible(visible);
        gbNavigators.get(index).setVisible(visible);
        genomeFilterBoxes.get(index).setVisible(visible);
    }

    public GeneClusterLocationSelection getClusterSelection() {
        return clusterLocationSelection;
    }

	public void changeGeneElementHeight(int adjustment) {
		geneElementHeight = geneElementHeight + adjustment;
		adjustAllSizes();
        repaint();
	}

    public int getGeneElementHeight() {
        return geneElementHeight;
    }

    /*
         * When a GenomeBrowser
         */
	private final ComponentListener genomeBrowserComponentListener = new ComponentAdapter() {
		public void componentResized(java.awt.event.ComponentEvent e) {
			fireBrowserContentChanged(BrowserContentEvent.ZOOM_FACTOR_CHANGED);
		}
	};

	public void setGenomes(Genome[] genomes) {
        clear();

        if (genomes != null) {
            FormLayout layoutLeft = new FormLayout("min, 2dlu, default:grow", "");
            FormLayout layoutRight = new FormLayout("default:grow, 2dlu, min", "");

            DefaultFormBuilder builderLeft = new DefaultFormBuilder(layoutLeft, left);
            DefaultFormBuilder builderRight = new DefaultFormBuilder(layoutRight, right);
            builderLeft.lineGapSize(Sizes.ZERO);
            builderRight.lineGapSize(Sizes.ZERO);

            for (Genome g : genomes) {
                generateGenomeElements(g);
                layoutGenome(genomeBrowsers.size()-1, builderLeft, builderRight);
            }
            left.setMinimumSize(new Dimension(0, (int)left.getMinimumSize().getHeight()));
            split.setDividerLocation(250);
        }
	}

    /**
     * Appends the needed Gui elements for the given genome to the lists
     * @param g
     */
    private void generateGenomeElements(Genome g)	{
        AbstractGenomeBrowser gb = new PaintingGenomeBrowser(g, this, nameType);
        gb.addMouseWheelListener(wheelListener);
        gb.addComponentListener(genomeBrowserComponentListener);
        genomeBrowsers.add(gb);

        genomeLabels.add(new GenomeLabel(gb, g));

        genomeFilterBoxes.add(new GenomeFilterBox(gb, genomeBrowsers.size() - 1));

        GBNavigator nav = new GBNavigator(gb, genomeBrowsers.size() - 1);
        gbNavigators.add(nav);
    }

    private void layoutGenome(int index, DefaultFormBuilder builderLeft, DefaultFormBuilder builderRight)	{
        builderLeft.append(genomeFilterBoxes.get(index));
        builderLeft.append(genomeLabels.get(index));
        builderRight.append(genomeBrowsers.get(index));
        builderRight.append(gbNavigators.get(index));
    }

    public void updateGeneWidth() {
        adjustAllSizes();
    }

    public boolean canZoomIn() {
        return (geneElementHeight<=MAX_GENEELEMENT_HIGHT);
    }

    public boolean canZoomOut() {
        return (geneElementHeight>=MIN_GENEELEMENT_HIGHT);
    }

    public void changeNameType(GenomePainting.NameType nameType) {
        this.nameType = nameType;
        for (AbstractGenomeBrowser genomeBrowser : genomeBrowsers)
            genomeBrowser.setNameType(nameType);
    }


	public void clear() {
        right.removeAll();
		left.removeAll();
		genomeBrowsers.clear();
		gbNavigators.clear();
        genomeFilterBoxes.clear();
        genomeLabels.clear();
        revalidate();
	}
	
	private void unflipAll() {
		for (AbstractGenomeBrowser gb : genomeBrowsers)
			if (gb.isFlipped()) gb.flip();
	}
	
	private void handleGlobalMouseWheelEvent(MouseWheelEvent e) {
		for (AbstractGenomeBrowser browser : genomeBrowsers)
			browser.adjustScrollPosition(e.getUnitsToScroll());
		fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
	}
	
	/**
	 * Calls the {@link de.unijena.bioinf.gecko3.gui.PaintingGenomeBrowser#scrollToPosition(int, int)}
	 * function for the {@link de.unijena.bioinf.gecko3.gui.PaintingGenomeBrowser} with the specified id
	 * @param gbid The id of the genome browser
	 */
	private void scrollToPosition(int gbid, int chromosome, int position) {
		this.genomeBrowsers.get(gbid).scrollToPosition(chromosome, position);
	}

	public void centerCurrentClusterAt(GeneFamily geneFamily) {
        clusterLocationSelection = clusterLocationSelection.getGeneClusterLocationSelection(geneFamily);

        for (int i=0; i<clusterLocationSelection.getTotalGenomeNumber(); i++){
            if (clusterLocationSelection.isFlipped(i) != genomeBrowsers.get(i).isFlipped())
                genomeBrowsers.get(i).flip();
            if (clusterLocationSelection.getAlignmentGenePosition(i)!=-1)
                scrollToPosition(i,
                        clusterLocationSelection.getSubsequence(i).getChromosome(),
                        clusterLocationSelection.getAlignmentGenePosition(i));
            else if (clusterLocationSelection.getSubsequence(i) != null)
                scrollToPosition(i,
                        clusterLocationSelection.getSubsequence(i).getChromosome(),
                        (clusterLocationSelection.getSubsequence(i).getStart() - 1 + clusterLocationSelection.getSubsequence(i).getStop() - 1) / 2);
        }
	}
	
	/**
	 * Call the {@link de.unijena.bioinf.gecko3.gui.PaintingGenomeBrowser#adjustSize()} method for all {@link de.unijena.bioinf.gecko3.gui.PaintingGenomeBrowser}s handled by
	 * this {@link MultipleGenomesBrowser}.
	 */
	private void adjustAllSizes() {
        int height = genomeBrowsers.size() * getGenomeBrowserHeight();
        left.setPreferredSize(new Dimension((int)left.getPreferredSize().getWidth(), height));
        right.setPreferredSize(new Dimension((int)right.getPreferredSize().getWidth(), height));
        setPreferredSize(new Dimension((int) getPreferredSize().getWidth(), height));
		for (AbstractGenomeBrowser g : genomeBrowsers) {
			g.adjustSize();
		}
        revalidate();
    }
	
	private int getGenomeBrowserHeight() {
		if (genomeBrowsers.size() == 0)
			return 0;
		else 
			return genomeBrowsers.get(0).getGBHeight();
	}
	
	public void clearSelection() {
		clearHighlight();
		unflipAll();
	}
	
	private void clearHighlight() {
		for (AbstractGenomeBrowser g: genomeBrowsers)
			g.clearHighlight();
	}
	
	private void updateButtons(GeneClusterOccurrence gOcc, int[] subselection) {
		Subsequence[][] subseqs = gOcc.getSubsequences();
		for (int i=0; i<subseqs.length;i++) {
			GBNavigator gbn = gbNavigators.get(i);
            gbn.updateButtons(subselection[i], subseqs[i]);
		}
	}
	
	private void visualizeCluster(GeneCluster gc, boolean includeSuboptimalOccs, int[] subselection) {
		clearHighlight();
		/*if (gc.getType()== Parameter.OperationMode.reference)
			rightPanel.setVisible(true);
		else
			rightPanel.setVisible(false);*/
		updateButtons(gc.getOccurrences(includeSuboptimalOccs),subselection);
		for (int i=0;i<subselection.length;i++) {
			if (subselection[i]==GeneClusterOccurrence.GENOME_NOT_INCLUDED) {
                genomeBrowsers.get(i).highlightCluster();
                continue;
            }
			Subsequence s = gc.getOccurrences(includeSuboptimalOccs).getSubsequences()[i][subselection[i]];
			scrollToPosition(i, s.getChromosome(), (s.getStart() - 1 + s.getStop() - 1) / 2);
			if (i == gc.getRefSeqIndex()) {
				genomeBrowsers.get(i).highlightCluster(s.getChromosome(), s.getStart() - 1, s.getStop() - 1, COLOR_HIGHLIGHT_REFCLUST);
			} else {
				genomeBrowsers.get(i).highlightCluster(s.getChromosome(), s.getStart() - 1, s.getStop() - 1, COLOR_HIGHLIGHT_DEFAULT);
			}
		}		
	}

	/*
	 * BrowserContentEvents
	 */
	
	private final EventListenerList eventListener = new EventListenerList();
	
	public synchronized void fireBrowserContentChanged(short type) {
		for (BrowserContentListener d : eventListener.getListeners(BrowserContentListener.class) ) {
			d.browserContentChanged(new BrowserContentEvent(this, type));
		}
	}

	public void addSelectionListener(ClusterSelectionListener s) {
		eventListener.add(ClusterSelectionListener.class, s);
	}
	
	private void removeSelectionListener(ClusterSelectionListener s) {
		eventListener.remove(ClusterSelectionListener.class, s);
	}
	
	private synchronized void fireSelectionEvent(ClusterSelectionEvent e) {
		for (ClusterSelectionListener l : eventListener.getListeners(ClusterSelectionListener.class))
			l.selectionChanged(e);
	}
	/*
	 * END BrowserContentEvents
	 */

	@Override
	public void selectionChanged(ClusterSelectionEvent e) {
		// We don't care if only a gene cluster was selected, we only need
		// to do something if a particular location of a gene cluster was
		// selected
		if (e instanceof LocationSelectionEvent) {
            LocationSelectionEvent lastLocationEvent = (LocationSelectionEvent) e;

            // save the currently selected cluster for the filter function
            clusterLocationSelection = new GeneClusterLocationSelection(gecko.getGenomes(), lastLocationEvent.getSelection(), lastLocationEvent.getsubselection(), lastLocationEvent.includeSubOptimalOccurrences());

			// deactivate the filter if active
			//this.hideNonClusteredGenomes(false);

            // if the check box is selected we show only the genomes which contain the cluster
			if (filterNonContainedGenomes)
				applyHideNonClusteredGenomesFilter();
			
			if (e.getSelection()!=null)
				visualizeCluster(lastLocationEvent.getSelection(), lastLocationEvent.includeSubOptimalOccurrences(), lastLocationEvent.getsubselection());
		}
	}

    /*
     * Scrollable implementation
     */

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 0;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 0;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return genomeBrowsers.isEmpty();
    }

    private class ScrollListener implements MouseWheelListener {
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.isShiftDown() || !(e.getSource() instanceof AbstractGenomeBrowser)) {
                handleGlobalMouseWheelEvent(e);
                return;
            } else {
                if (e.getSource() instanceof AbstractGenomeBrowser) {
                    ((AbstractGenomeBrowser) e.getSource()).adjustScrollPosition(e.getUnitsToScroll());
                    fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
                }
            }
        }

    }

    private static class GenomeLabel extends JScrollPane {
        private final JComponent sizeReference;

        private final ComponentListener componentListener = new ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                GenomeLabel.this.invalidate();
                GenomeLabel.this.getParent().validate();
            }
        };

        public GenomeLabel(JComponent sizeReference, Genome g){
            super(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.sizeReference = sizeReference;
            sizeReference.addComponentListener(componentListener);
            JLabel genomeName = new JLabel(g.getName());
            genomeName.setFont(genomeName.getFont().deriveFont(10.0f));
            genomeName.setBackground(sizeReference.getBackground());
            genomeName.setOpaque(true);
            this.setViewportView(genomeName);
            this.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 5));
            this.setPreferredSize(new Dimension((int)this.getPreferredSize().getWidth(), sizeReference.getHeight()));
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            return new Dimension((int) d.getWidth(),sizeReference.getHeight());
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension d = super.getMaximumSize();
            return new Dimension((int) d.getWidth(),sizeReference.getHeight());
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension((int) d.getWidth(),sizeReference.getHeight());
        }
    }

    /**
     * The filter mode combo box on each genome browser.
     * It sets the name, selection and implements an ActionListener
     * which handles the different selection events.
     */
    private static class GenomeFilterBox extends JComboBox<GenomeFilterMode>{
        private final JComponent sizeReference;

        private final ComponentListener componentListener = new ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                GenomeFilterBox.this.invalidate();
                GenomeFilterBox.this.getParent().validate();
            }
        };

        /**
         * @param sizeReference this component will always have the same height. Must not be null!
         * @param id
         */
        public GenomeFilterBox(JComponent sizeReference, int id) {
            super(GenomeFilterMode.values());
            this.setName(Integer.toString(id));
            this.sizeReference = sizeReference;
            this.sizeReference.addComponentListener(componentListener);
            GeckoInstance.getInstance().getGui().getGcSelector().addIncludeExcludeFilterComboBox(this);
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            return new Dimension((int) d.getWidth(),sizeReference.getHeight());
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension d = super.getMaximumSize();
            return new Dimension((int) d.getWidth(),sizeReference.getHeight());
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension((int) d.getWidth(),sizeReference.getHeight());
        }
    }

    /**
     * Allows navigation to the different occurrences of one cluster on one genome
     */
    private class GBNavigator  extends JPanel implements ActionListener {
        /**
         * Random generated serialization UID
         */
        private static final long serialVersionUID = -1597716317000549154L;
        private final JButton prev=new JButton("<");
        private final JButton next=new JButton(">");
        private final JComponent sizeReference;
        private final int genome;

        private final ComponentListener componentListener = new ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                GBNavigator.this.invalidate();
                GBNavigator.this.getParent().validate();
            }
        };

        public GBNavigator(JComponent sizeReference, int genome) {
            this.genome = genome;
            sizeReference.addComponentListener(componentListener);
            this.setBackground(Color.WHITE);
            prev.putClientProperty("JButton.buttonType", "bevel");
            prev.addActionListener(this);
            prev.setMargin(new Insets(0, 0, 0, 0));
            prev.setEnabled(false);
            next.putClientProperty("JButton.buttonType", "bevel");
            next.addActionListener(this);
            next.setMargin(new Insets(0, 0, 0, 0));
            next.setEnabled(false);
            this.sizeReference = sizeReference;
            this.setLayout(new GridLayout(1, 2));
            this.add(prev);
            this.add(next);
        }

        @Override
        public Dimension getMaximumSize() {
            return getMinimumSize();
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            return new Dimension((int) d.getWidth(),sizeReference.getHeight());
        }

        @Override
        public Dimension getPreferredSize() {
            return getMinimumSize();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource()==next || e.getSource()==prev) {
                int[] subselections = clusterLocationSelection.getSubselection();
                if (e.getSource() == next)
                    subselections[genome] = subselections[genome] + 1;
                else
                    subselections[genome] = subselections[genome] - 1;
                fireSelectionEvent(new LocationSelectionEvent(MultipleGenomesBrowser.this,
                        clusterLocationSelection.getCluster(),
                        clusterLocationSelection.includeSubOptimalOccurrences(),
                        subselections));
            }
        }

        /**
         * Updates the buttons for the given sub selection in the given sub sequence
         * @param subselection
         * @param subseq
         */
        public void updateButtons(int subselection, Subsequence[] subseq) {
            if (subselection<subseq.length-1)
                next.setEnabled(true);
            else
                next.setEnabled(false);
            if (subselection>0)
                prev.setEnabled(true);
            else
                prev.setEnabled(false);
        }
    }
}
