package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.*;
import gecko2.event.*;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class implements the multiple genome browser (mgb) for the gecko2 GUI.
 * The mbg is implemented a container for GenomeBrowser objects and provides
 * functions for setting up the mgb.
 * 
 * This class contains modifications (genomeBrowserFilter, related variables and getter/setter)
 * made by Hans-Martin Haase <hans-martin.haase@uni-jena.de>
 * 
 * @author original author unknown
 */
public class MultipleGenomesBrowser extends AbstractMultipleGenomeBrowser {

	private static final long serialVersionUID = -6769789368841494821L;
	private final JPanel centerpanel;

	private final JPanel rightPanel;
	private final List<AbstractGenomeBrowser> genomeBrowsers;
	private final ScrollListener wheelListener;
	private LocationSelectionEvent lastLocationEvent;
	private final List<GBNavigator> gbNavigators;
	
	private final GeckoInstance gecko;
	
	/**
	 * The variable stores the currently selected gene cluster
	 */
	private GeneCluster selectedCluster = null;

    /**
     * What name is shown for each gene
     */
    private GenomePainting.NameType nameType = GenomePainting.NameType.ID;
	
	/**
	 * The variable stores the filter activity: <br>
	 * false filter is inactive
	 * true filter is active
	 */
	private boolean filterNonContainedGenomes = false; 
	
	public MultipleGenomesBrowser() {
		gecko = GeckoInstance.getInstance();
		this.setBackground(Color.WHITE);
		this.addSelectionListener(this);
		this.setPreferredSize(new Dimension(0,0));
		this.wheelListener = new ScrollListener();
		this.genomeBrowsers = new ArrayList<AbstractGenomeBrowser>();
		this.gbNavigators = new ArrayList<GBNavigator>();
		this.setLayout(new BorderLayout());
        JPanel leftpanel = new JPanel();
		this.centerpanel = new JPanel();
		centerpanel.setBackground(Color.WHITE);
		this.rightPanel = new JPanel();
		rightPanel.setBackground(Color.WHITE);
		rightPanel.setVisible(false);
		leftpanel.setLayout(new BoxLayout(leftpanel,BoxLayout.Y_AXIS));
		this.centerpanel.setLayout(new BoxLayout(centerpanel,BoxLayout.Y_AXIS));
		this.centerpanel.setPreferredSize(new Dimension(200,200));
		this.rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.Y_AXIS));
		this.add(centerpanel,BorderLayout.CENTER);
		this.add(leftpanel,BorderLayout.WEST);
		this.add(rightPanel,BorderLayout.EAST);
		this.addKeyListener(this.wheelListener);
		this.addMouseWheelListener(this.wheelListener);
	}
	
	/**
	 * Getter for the variable selectedCluster.
	 * 
	 * @return a GenCluster objectgenomeIndex
	 */
	@Override
	public GeneCluster getSelectedCluster() 
	{
		return selectedCluster;
	}

	/**
	 * This method implements a filter for the mgb. It removes the GenomeBrowers for the genomes
	 * which don't support the current cluster.
	 * For this the method sets the filterActiv variable and the clusterBackup to undo the view change.
	 * So the method activates and deactivates the filter.
	 * 
	 * @param filter true if the filter should be activated else false to turn it off
	 */
	@Override
	public void hideNonClusteredGenomes(boolean filter)
	{
		// filter is only active if a cluster was selected
		if (this.getSelectedCluster() != null)
		{
			if (!filterNonContainedGenomes && filter)
			{
				// activation branch
				for (int i = 0; i < this.getSelectedCluster().getOccurrences()[0].getSubsequences().length; i++)
				{
					if (this.getSelectedCluster().getOccurrences()[0].getSubsequences()[i].length == 0)
					{	
						this.centerpanel.getComponent(i).setVisible(false);
						this.setPreferredSize(new Dimension((int)this.getPreferredSize().getWidth(),(int) this.getPreferredSize().getHeight() - getGenomeBrowserHeight()));
					}
				}
				
				this.validate();
			}
			else
			{
				if (filterNonContainedGenomes && !filter)
				{
					// deactivate filter branch
					for (int i = 0; i < this.getSelectedCluster().getOccurrences()[0].getSubsequences().length; i++)
					{
						this.centerpanel.getComponent(i).setVisible(true);
						if (this.getSelectedCluster().getOccurrences()[0].getSubsequences()[i].length == 0)
						{	
							this.setPreferredSize(new Dimension((int)this.getPreferredSize().getWidth(),(int) this.getPreferredSize().getHeight() + getGenomeBrowserHeight()));
						}
					}
				
					this.validate();
				}
			}
		}
		filterNonContainedGenomes = filter;
	}
	
	@Override
	public int getScrollValue(int genomeIndex) {
		return genomeBrowsers.get(genomeIndex).getScrollValue();
	}
	
	@Override
	public ScrollListener getWheelListener() {
		return wheelListener;
	}
	
	@Override
	public int getNrGenomes() {
		return genomeBrowsers.size();
	}
	
	class ScrollListener extends KeyAdapter implements MouseWheelListener {
		
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (!e.isShiftDown()) {
				for (AbstractGenomeBrowser gb : MultipleGenomesBrowser.this.genomeBrowsers) {
					gb.adjustScrollPosition(e.getUnitsToScroll());
				}
				fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
			}
		}

	}

	private final MouseWheelListener mouseWheelListener = new MouseWheelListener() {
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isShiftDown()) {
				handleGlobalMouseWheelEvent(e);
				return;
			}			
			if (e.getSource() instanceof AbstractGenomeBrowser) {
				((AbstractGenomeBrowser) e.getSource()).adjustScrollPosition(e.getUnitsToScroll());
				fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
			}
		}
	};
	
	@Override
	public void changeGeneElementHight(int adjustment) {
		gecko.setGeneElementHight(gecko.getGeneElementHight() + adjustment);
		adjustAllSizes();
		repaint();	
	}
	
	/*
	 * When a GenomeBrowser 
	 */
	private final ComponentListener genomeBrowserComponentListener = new ComponentAdapter() {
		public void componentResized(java.awt.event.ComponentEvent e) {
			fireBrowserContentChanged(BrowserContentEvent.ZOOM_FACTOR_CHANGED);
		}
	};
	
	/**
	 * This method creates the combo box on each genome browser.
	 * It sets the name, selection and implements an ActionListener
	 * which handles the different selection events.
	 * 
	 * @param genomeBrowsersSize size of the ArrayList genomeBrowsers.
	 * this is the id of the genome.
	 * @return a JComboBox for the use in a GenomeBrowser
	 */
	private JComboBox createComboBox(int genomeBrowsersSize, int height) {
		String[] selection = {"None", "Include", "Exclude"};
		JComboBox<String> box = new JComboBox<>(selection);
		box.setPreferredSize(new Dimension(100, height));
		box.setMaximumSize(box.getPreferredSize());
		box.setName(Integer.toString(genomeBrowsersSize - 1));
		
		box.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				JComboBox cb = (JComboBox) arg0.getSource();
		        String selectedItem = (String) cb.getSelectedItem();
		        
		        if (selectedItem.equals("None")) {
		        	gecko.getGui().getGcSelector().resetGenome(Integer.parseInt(cb.getName()));
		        }
		        
		        if (selectedItem.equals("Include")) {
		        	gecko.getGui().getGcSelector().showOnlyClusWthSelecGenome(Integer.parseInt(cb.getName()));
		        }
		        
		        if (selectedItem.equals("Exclude")) {
		        	gecko.getGui().getGcSelector().dontShowClusWthSelecGenome(Integer.parseInt(cb.getName()));
		        }
			}
		});
			
		return box;
	}
	
	@Override
	public void addGenomes(Genome[] genomes) {
		for (Genome g : genomes)
			addGenome(g);
	}

    @Override
    public void changeNameType(GenomePainting.NameType nameType) {
        this.nameType = nameType;
        for (AbstractGenomeBrowser genomeBrowser : genomeBrowsers)
            genomeBrowser.setNameType(nameType);
    }

    private void addGenome(Genome g)	{
		AbstractGenomeBrowser gb = new PaintingGenomeBrowser(g, this, nameType);
		gb.addMouseWheelListener(mouseWheelListener);
		gb.addComponentListener(genomeBrowserComponentListener);
		this.genomeBrowsers.add(gb);
		JPanel boxPanel = new JPanel();
		
		boxPanel.setBackground(gb.getBackground());
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.LINE_AXIS));
		NumberInRectangle n = new NumberInRectangle(genomeBrowsers.size(), gb.getBackground());
//		n.addMouseListener(gb.getGenomebrowsermouseover());
		JComboBox box = createComboBox(genomeBrowsers.size(), getGenomeBrowserHeight());
		boxPanel.add(box);
		boxPanel.add(new JToolBar.Separator());
		boxPanel.add(n);
		boxPanel.add(Box.createRigidArea(new Dimension(4, 0)));
		boxPanel.add(gb);
		GBNavigator nav = new GBNavigator(boxPanel, genomeBrowsers.size() - 1);
		this.gbNavigators.add(nav);
		this.centerpanel.add(boxPanel);
		rightPanel.add(nav);
		this.setPreferredSize(new Dimension((int)this.getPreferredSize().getWidth(), (int) this.getPreferredSize().getHeight() + getGenomeBrowserHeight()));
		this.repaint();

		this.revalidate();
	}
	
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
				GBNavigator.this.getParent().doLayout();
				GBNavigator.this.doLayout();
			}
		};
		
		public JButton getPrev() {
			return prev;
		}
		
		public JButton getNext() {
			return next;
		}
		
		public GBNavigator(JComponent sizeReference, int genome) {
			this.genome = genome;
			sizeReference.addComponentListener(componentListener);
			this.setBackground(Color.WHITE);
			prev.putClientProperty("JButton.buttonType", "bevel");
			next.putClientProperty("JButton.buttonType", "bevel");
			next.addActionListener(this);
			prev.addActionListener(this);
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
			if (sizeReference!=null) {
				Dimension d = super.getMinimumSize();
				return new Dimension((int) d.getWidth(),sizeReference.getHeight());
			} else
				return new Dimension(0,0);
			
		}
		
		@Override
		public Dimension getPreferredSize() {
			return getMinimumSize();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==next) {
				int[] subselections = lastLocationEvent.getsubselection();
				subselections[genome] = subselections[genome] + 1;
				fireSelectionEvent(new LocationSelectionEvent(MultipleGenomesBrowser.this, 
						lastLocationEvent.getSelection(), 
						lastLocationEvent.getgOcc(), 
						subselections));
			} else if (e.getSource()==prev) {
				int[] subselections = lastLocationEvent.getsubselection();
				subselections[genome] = subselections[genome] - 1;
				fireSelectionEvent(new LocationSelectionEvent(MultipleGenomesBrowser.this, 
						lastLocationEvent.getSelection(), 
						lastLocationEvent.getgOcc(), 
						subselections));
			}
		}
		
	}

	@Override
	public void clear() {
		this.setPreferredSize(new Dimension(0,0));
		this.centerpanel.removeAll();
		this.genomeBrowsers.clear();
		this.rightPanel.removeAll();
		this.rightPanel.setVisible(false);
		this.gbNavigators.clear();
		this.repaint();
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
	 * Calls the {@link GenomeBrowser#scrollToPosition(int, int)}
	 * function for the {@link GenomeBrowser} with the specified id
	 * @param gbid The id of the genome browser
	 */
	private void scrollToPosition(int gbid, int chromosome, int position) {
		this.genomeBrowsers.get(gbid).scrollToPosition(chromosome, position);
	}
	
	@Override
	public int getScrollWidth() {
		if (genomeBrowsers.size() == 0)
			return 0;
		return genomeBrowsers.get(0).getScrollWidth();
	}
	
	@Override
	public int getScrollMaximum() {
		int maxBarMax = 0;
		for (AbstractGenomeBrowser gb : genomeBrowsers) {
			if (gb.getMaximumValue() > maxBarMax)
				maxBarMax = gb.getMaximumValue();
		}
		return maxBarMax;
	}

	@Override
	public void centerCurrentClusterAt(int geneID) {
		int[] subselections 		= lastLocationEvent.getsubselection();
		GeneClusterOccurrence gOcc 	= lastLocationEvent.getgOcc();
		
		int[] positions = new int[gOcc.getSubsequences().length];
		Arrays.fill(positions, -1);
		ArrayList<Integer> minus = new ArrayList<Integer>();
		ArrayList<Integer> plus = new ArrayList<Integer>();
		for (int i=0; i<positions.length;i++) {
			// If genome i is not in the cluser, skip
			if (subselections[i]==GeneClusterOccurrence.GENOME_NOT_INCLUDED)
				continue;
			List<Chromosome> genes = gecko.getGenomes()[i].getChromosomes();
			Subsequence s = gOcc.getSubsequences()[i][subselections[i]];
			for (int j=s.getStart()-1;j<s.getStop();j++) {
				if (Math.abs(genes.get(s.getChromosome()).getGenes().get(j).getId())==geneID) {
					positions[i] = j;
					if (genes.get(s.getChromosome()).getGenes().get(j).getId()<0) 
						minus.add(i);
					else
						plus.add(i);
					break;
				}
			}
		}
		if (minus.size()>plus.size()) {
			for (Integer i : plus) 
				if (!genomeBrowsers.get(i).isFlipped()) genomeBrowsers.get(i).flip();
			for (Integer i : minus)
				if (genomeBrowsers.get(i).isFlipped()) genomeBrowsers.get(i).flip();
		} else {
			for (Integer i : minus)
				if (!genomeBrowsers.get(i).isFlipped()) genomeBrowsers.get(i).flip();
			for (Integer i : plus) 
				if (genomeBrowsers.get(i).isFlipped()) genomeBrowsers.get(i).flip();
		}
		for (int i=0;i<positions.length;i++) //TODO REFIX THIS
			if (positions[i]!=-1) scrollToPosition(i, 
					gOcc.getSubsequences()[i][subselections[i]].getChromosome(), 
					positions[i]);
	}
	
	/**
	 * Call the {@link GenomeBrowser#adjustAllSizes()} method for all {@link GenomeBrowser}s handled by
	 * this {@link MultipleGenomesBrowser}.
	 */
	private void adjustAllSizes() {
		this.setPreferredSize(new Dimension((int)this.getPreferredSize().getWidth(), genomeBrowsers.size() * getGenomeBrowserHeight()));
		for (AbstractGenomeBrowser g : genomeBrowsers) {
			g.adjustSize();
		}
		this.revalidate();
	}
	
	private int getGenomeBrowserHeight() {
		if (genomeBrowsers.size() == 0)
			return 0;
		else 
			return genomeBrowsers.get(0).getGBHeight();
	}
	
	@Override
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
			if (subselection[i]<subseqs[i].length-1)
				gbn.getNext().setEnabled(true);
			else
				gbn.getNext().setEnabled(false);
			if (subselection[i]>0)
				gbn.getPrev().setEnabled(true);
			else
				gbn.getPrev().setEnabled(false);
		}
	}
	
	private void visualizeCluster(GeneCluster gc, GeneClusterOccurrence gOcc, int[] subselection) {
		clearHighlight();
		if (gc.getType()== Parameter.OperationMode.reference)
			rightPanel.setVisible(true);
		else
			rightPanel.setVisible(false);
		updateButtons(gOcc,subselection);
		for (int i=0;i<subselection.length;i++) {
			if (subselection[i]==GeneClusterOccurrence.GENOME_NOT_INCLUDED) continue;
			Subsequence s = gOcc.getSubsequences()[i][subselection[i]];
//			if (s==null) continue; // must actually not be the case


			scrollToPosition(i, s.getChromosome(), (s.getStart() - 1 + s.getStop() - 1) / 2);
			
			if (i == gc.getRefSeqIndex()) {
				genomeBrowsers.get(i).highlightCluster(s.getChromosome(), s.getStart() - 1, s.getStop() - 1, GeneElement.COLOR_HIGHLIGHT_REFCLUST);
			}
			else {
				genomeBrowsers.get(i).highlightCluster(s.getChromosome(), s.getStart() - 1, s.getStop() - 1, GeneElement.COLOR_HIGHLIGHT_DEFAULT);
			}
		}		
	}

	/*
	 * BrowserContentEvents
	 */
	
	private final EventListenerList eventListener = new EventListenerList();

	@Override
	public void addBrowserContentListener(BrowserContentListener l) {
		eventListener.add(BrowserContentListener.class, l);
	}
	
	private void removeBrowserContentListener(BrowserContentListener l) {
		eventListener.remove(BrowserContentListener.class, l);
	}
	
	@Override
	protected synchronized void fireBrowserContentChanged(short type) {
		for (BrowserContentListener d : eventListener.getListeners(BrowserContentListener.class) ) {
			d.browserContentChanged(new BrowserContentEvent(this, type));
		}
	}
	
	@Override
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
			lastLocationEvent = (LocationSelectionEvent) e;
			
			// save current filter status
			boolean stat = filterNonContainedGenomes;
			
			// deactivate the filter if active
			this.hideNonClusteredGenomes(false);
		
			// save the currently selected cluster for the filter function
			selectedCluster = e.getSelection();
			
			// if the check box is selected we show only the genomes which contain the cluster
			if (stat)
				this.hideNonClusteredGenomes(stat);
			
			if (e.getSelection()!=null)
				visualizeCluster(lastLocationEvent.getSelection(), lastLocationEvent.getgOcc(), lastLocationEvent.getsubselection());
		}
	}

	/**
	 * Returns the int array of the last subselection
	 * @return
	 */
	public int[] getSubselection() {
		return lastLocationEvent.getsubselection();
	}

	@Override
	public int getGeneWidth() {
		if (this.genomeBrowsers.size() == 0)
			return 0;
		else 
			return this.genomeBrowsers.get(0).getGeneWidth();
	}

    @Override
	public int[] getGeneNumbers(int genomeIndex) {
		int[] lineLengths = new int[genomeBrowsers.get(genomeIndex).getGenome().getChromosomes().size()];
        for (int i=0; i<genomeBrowsers.get(genomeIndex).getGenome().getChromosomes().size(); i++) {
			// Compute the line length in GenomeBrowser pixels
			lineLengths[i] = genomeBrowsers.get(genomeIndex).getGenome().getChromosomes().get(i).getGenes().size();
		}
		return lineLengths;
	}

	@Override
	public boolean isFlipped(int genomeIndex) {
		return genomeBrowsers.get(genomeIndex).isFlipped();
	}
}
