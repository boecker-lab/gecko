package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.Genome;
import gecko2.algorithm.Subsequence;
import gecko2.event.BrowserContentEvent;
import gecko2.event.BrowserContentListener;
import gecko2.event.ClusterSelectionEvent;
import gecko2.event.ClusterSelectionListener;
import gecko2.event.LocationSelectionEvent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

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
public class MultipleGenomesBrowser extends JPanel implements ClusterSelectionListener {

	private static final long serialVersionUID = -6769789368841494821L;
	private JPanel leftpanel ;
	private JPanel centerpanel;

	private JPanel rightPanel;
	private ArrayList<GenomeBrowser> genomeBrowsers;
	private ScrollListener wheelListener;
	private LocationSelectionEvent lastLocationEvent;
	private ArrayList<GBNavigator> gbNavigators;
	
	GeckoInstance gecko;

	/**
	 * The variable stores the currently selected gene cluster
	 */
	private GeneCluster selectedCluster = null;
	
	/**
	 * The variable stores the filter activity: <br>
	 * false filter is inactive
	 * true filter is active
	 */
	private boolean filterActiv = false; 
	
	/**
	 * Getter for the variable selectedCluster.
	 * 
	 * @return a GenCluster object
	 */
	private GeneCluster getSelectedCluster() 
	{
		return selectedCluster;
	}

	/**
	 * Setter for the variable selectedCluster.
	 * 
	 * @param selectedCluster a GeneCluster object
	 */
	private void setSelectedCluster(GeneCluster selectedCluster) 
	{
		this.selectedCluster = selectedCluster;
	}

	/**
	 * Getter for the variable filterActiv.
	 * 
	 * @return true if the filter is active else false
	 */
	private boolean isFilterActiv() 
	{
		return filterActiv;
	}

	/**
	 * Setter for the variable filterActiv
	 * 
	 * @param filterActiv boolean true for filter is active false for filter inactive
	 */
	private void setFilterActiv(boolean filterActiv) 
	{
		this.filterActiv = filterActiv;
	}

	/**
	 * This method implements a filter for the mgb. It removes the GenomeBrowers for the genomes
	 * which don't support the current cluster.
	 * For this the method sets the filterActiv variable and the clusterBackup to undo the view change.
	 * So the method activates and deactivates the filter.
	 * 
	 * @param action true if the filter should be activated else false to turn it off
	 */
	public void genomeBrowserFilter(boolean action)
	{
		// filter is only active if a cluster was selected
		FlowLayout flowlayout = new FlowLayout();
		flowlayout.setVgap(0);
		
		if (this.getSelectedCluster() != null)
		{
			if (this.isFilterActiv() == false && action == true)
			{
				// activation branch
				for (int i = 0; i < this.getSelectedCluster().getOccurrences()[0].getSubsequences().length; i++)
				{
					if (this.getSelectedCluster().getOccurrences()[0].getSubsequences()[i].length == 0)
					{	
						this.centerpanel.getComponent(i).setVisible(false);
						this.setPreferredSize(new Dimension((int)this.getPreferredSize().getWidth(),(int) this.getPreferredSize().getHeight() - (4 + gecko.getGeneElementHight() + flowlayout.getVgap())));
					}
				}
				
				this.validate();
				this.setFilterActiv(true);
			}
			else
			{
				if (this.isFilterActiv() == true && action == false)
				{
					// deactivate filter branch
					for (int i = 0; i < this.getSelectedCluster().getOccurrences()[0].getSubsequences().length; i++)
					{
						this.centerpanel.getComponent(i).setVisible(true);
						if (this.getSelectedCluster().getOccurrences()[0].getSubsequences()[i].length == 0)
						{	
							this.setPreferredSize(new Dimension((int)this.getPreferredSize().getWidth(),(int) this.getPreferredSize().getHeight() + (4 + gecko.getGeneElementHight() + flowlayout.getVgap())));
						}
					}
				
					this.validate();
					this.setFilterActiv(false);
				}
			}
		}
	}
	
	public float[] getScrollPositions() {
		float[] pos = new float[genomeBrowsers.size()];
		for (int i=0; i<genomeBrowsers.size(); i++) {
			GenomeBrowser gb = genomeBrowsers.get(i);
			JScrollBar s = gb.getHorizontalScrollBar();
			pos[i] = (s.getValue()-gb.getLeftSpacerWidth())/(float)(s.getMaximum()-2*gb.getLeftSpacerWidth());
		}
		return pos;
	}
	
	public void setScrollPositions(float[] positions) 
	{
		if (positions.length != genomeBrowsers.size())
		{
			throw new IndexOutOfBoundsException("The positions array size does not fit the number of GenomeBrowsers");
		}
		
		for (int i = 0; i < positions.length; i++) 
		{
			GenomeBrowser gb = genomeBrowsers.get(i);
			JScrollBar s = gb.getHorizontalScrollBar();
			s.setValue((int) (gb.getLeftSpacerWidth() + (positions[i]*(s.getMaximum()-2*gb.getLeftSpacerWidth()))));
			fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
		}
		
	}
	
	public ScrollListener getWheelListener() {
		return wheelListener;
	}
	
	public GenomeBrowser getBrowser(int index) {
		return genomeBrowsers.get(index);
	}

	public ArrayList<GenomeBrowser> getGenomeBrowsers() {
		return genomeBrowsers;
	}
	
	class ScrollListener extends KeyAdapter implements MouseWheelListener {

		
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (!e.isShiftDown()) {
				for (GenomeBrowser gb : MultipleGenomesBrowser.this.genomeBrowsers) {
					JScrollBar b = gb.getHorizontalScrollBar();
					b.setValue(b.getValue()+e.getUnitsToScroll());
				}
				fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
			}
		}

	}
//	
	public MultipleGenomesBrowser() {
		gecko = GeckoInstance.getInstance();
		this.setBackground(Color.WHITE);
		this.addSelectionListener(this);
		this.setPreferredSize(new Dimension(0,0));
		this.wheelListener = new ScrollListener();
		this.genomeBrowsers = new ArrayList<GenomeBrowser>();
		this.gbNavigators = new ArrayList<GBNavigator>();
		this.setLayout(new BorderLayout());
		this.leftpanel = new JPanel();
		this.centerpanel = new JPanel();
		centerpanel.setBackground(Color.WHITE);
		this.rightPanel = new JPanel();
		rightPanel.setBackground(Color.WHITE);
		rightPanel.setVisible(false);
		this.leftpanel.setLayout(new BoxLayout(leftpanel,BoxLayout.Y_AXIS));
		this.centerpanel.setLayout(new BoxLayout(centerpanel,BoxLayout.Y_AXIS));
		this.centerpanel.setPreferredSize(new Dimension(200,200));
		this.rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.Y_AXIS));
		this.add(centerpanel,BorderLayout.CENTER);
		this.add(leftpanel,BorderLayout.WEST);
		this.add(rightPanel,BorderLayout.EAST);
		this.addKeyListener(this.wheelListener);
		this.addMouseWheelListener(this.wheelListener);
	}
	
	private MouseWheelListener mouseWheelListener = new MouseWheelListener() {
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isShiftDown()) {
				handleGlobalMouseWheelEvent(e);
				return;
			}			
			if (e.getSource() instanceof GenomeBrowser) {
				scrollGenomeBrowser((GenomeBrowser) e.getSource(), e);
				fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
			}
		}
	};
	
	private void scrollGenomeBrowser(GenomeBrowser gb, MouseWheelEvent e) {
		int value = gb.getHorizontalScrollBar().getValue();
		value += e.getUnitsToScroll();
		gb.getHorizontalScrollBar().setValue(value);
	}
	
	public void changeGeneElementHight(int adjustment) {
		final float[] positions = getScrollPositions();
		gecko.setGeneElementHight(gecko.getGeneElementHight()+adjustment);
		adjustAllSizes();
		repaint();
		setScrollPositions(positions);		
	}
	
	/*
	 * When a GenomeBrowser 
	 */
	private ComponentListener genomeBrowserComponentListener = new ComponentAdapter() {
		public void componentResized(java.awt.event.ComponentEvent e) {
			fireBrowserContentChanged(BrowserContentEvent.ZOOM_FACTOR_CHANGED);
		};
	};
	
	public void addGenome(Genome g) {
		GenomeBrowser gb = new GenomeBrowser(g);
		gb.addMouseWheelListener(mouseWheelListener);
		gb.addComponentListener(genomeBrowserComponentListener);
		this.genomeBrowsers.add(gb);
		JPanel boxPanel = new JPanel();
		boxPanel.setBackground(gb.getBackground());
		boxPanel.setLayout(new BoxLayout(boxPanel,BoxLayout.LINE_AXIS));
		NumberInRectangle n = new NumberInRectangle(genomeBrowsers.size(),gb.getBackground(), Integer.toString(this.genomeBrowsers.size()).length());
//		n.addMouseListener(gb.getGenomebrowsermouseover());
		boxPanel.add(n);
		boxPanel.add(Box.createRigidArea(new Dimension(4, 0)));
		boxPanel.add(gb);
		GBNavigator nav = new GBNavigator(boxPanel,genomeBrowsers.size()-1);
		this.gbNavigators.add(nav);
		this.centerpanel.add(boxPanel);
		rightPanel.add(nav);
		this.setPreferredSize(new Dimension((int)this.getPreferredSize().getWidth(),(int) this.getPreferredSize().getHeight()+gb.getGBHeight()));
		this.repaint();
		this.revalidate();
	}
	
	public class GBNavigator  extends JPanel implements ActionListener {
		
		/**
		 * Random generated serialization UID
		 */
		private static final long serialVersionUID = -1597716317000549154L;
		private JButton prev=new JButton("<"),next=new JButton(">");
		private JComponent sizeReference;
		private int genome;
		
		private final ComponentListener componentListener = new ComponentAdapter() {
			
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				GBNavigator.this.invalidate();
				GBNavigator.this.getParent().doLayout();
				GBNavigator.this.doLayout();
			};
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

	
	public void clear() {
		this.setPreferredSize(new Dimension(0,0));
		this.centerpanel.removeAll();
		this.genomeBrowsers.clear();
		this.rightPanel.removeAll();
		this.rightPanel.setVisible(false);
		this.gbNavigators.clear();
		this.repaint();
	}
	
	public void unflipAll() {
		for (GenomeBrowser gb : genomeBrowsers)
			if (gb.isFlipped()) gb.flip();
	}
	
	public void handleGlobalMouseWheelEvent(MouseWheelEvent e) {
		for (GenomeBrowser browser : genomeBrowsers)
			scrollGenomeBrowser(browser, e);
		fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
	}
	
	/**
	 * Marks a region within a genome by setting everything except that region to 
	 * greyscale instead of the gene colors and by changing the background color
	 * of the specified region.
	 * @param id The id of the genome
	 * @param chr The id of the chromosome
	 * @param start The first gene in the region. If the value is greater than stop
	 * or below zero the whole genome is set to greyscale.
	 * @param stop The last gene in the region. If the value is lower than start or 
	 * greater than the chromosome length -1 the whole genome is set to greyscale.
	 */
	public void highlightCluster(int id, int chr, int start, int stop, Color highlightColor) {
		if (id>=genomeBrowsers.size()) return;
		List<Chromosome> chromosomes = this.genomeBrowsers.get(id).getGenome().getChromosomes();
		int genomeLength = chromosomes.get(chr).getGenes().size();
		// Set everything to grey if the parameter don't make sense
		// (algorithm produces start>stop if a genome is not part of the cluster)
		if (start<0 || stop>=genomeLength || start>stop) {
			for (int i=0;i<chromosomes.size();i++)
				this.setGenomeRangeToGrey(id, i, 0, chromosomes.get(i).getGenes().size()-1, true);
			return;
		}
		// Set all the chromosomes before to grey
		for (int i=0;i<chr;i++)
			this.setGenomeRangeToGrey(id, i, 0, chromosomes.get(i).getGenes().size()-1, true);
		// Set the target chromosome to grey except for the highlighted region
		this.setGenomeRangeToGrey(id, chr, 0, start-1, true);
		this.setGenomeRangeToGrey(id, chr, start, stop, false);
		this.setGenomeRangeToGrey(id, chr, stop+1, genomeLength-1, true);
		// Set all the chromosomes after to grey
		for (int i=chr+1;i<chromosomes.size();i++)
			this.setGenomeRangeToGrey(id, i, 0, chromosomes.get(i).getGenes().size()-1,true);
		// Highlight (color the background)
		this.genomeBrowsers.get(id).markClusterBorder(chr, start, stop, highlightColor);
	}
	
	/**
	 * Calls the {@link GenomeBrowser#setRangeToGrey(int, int, int, boolean)} method
	 * for a specified genome.
	 * @param id The id of the genome
	 * @param chr The id of the chromosome
	 * @param start The first gene to be set to greyscale / color
	 * @param stop The last gene to be set to greyscale / color
	 * @param grey If <code>true</code> the region is set to greyscale, otherwise to color mode
	 */
	public void setGenomeRangeToGrey(int id, int chr, int start, int stop, boolean grey) {
		this.genomeBrowsers.get(id).setRangeToGrey(chr,start,stop,grey);
	}
	

	/**
	 * Calls the {@link MultipleGenomesBrowser#scrollToPosition(GenomeBrowser, int, int)} 
	 * function for the {@link GenomeBrowser} with the specified id
	 * @param gbid The id of the genome browser
	 */
	public void scrollToPosition(int gbid, int chromosome, int position) {
		this.scrollToPosition(this.genomeBrowsers.get(gbid), chromosome, position);
	}
	
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
	
	private HashMap<GenomeBrowser, GenomeScollThread> lastScrollThreads = new HashMap<GenomeBrowser, GenomeScollThread>();
	
	/**
	 * Scrolls to a specified position within a genome. A thread is created to perform the
	 * scrolling. The funktion returns after the Tread is created and started.
	 * @param gb The genomebrowser
	 * @param chromosome The chromosome id
	 * @param position The position of the gene within the chromosome
	 */
	public synchronized void scrollToPosition(GenomeBrowser gb, int chromosome, int position) {
		if (position<0
				|| gb.getGenome().getChromosomes().size()<=chromosome
				|| gb.getGenome().getChromosomes().get(chromosome).getGenes().size()<=position) return;
		GenomeScollThread last = lastScrollThreads.get(gb);
		lastScrollThreads.put(gb,new GenomeScollThread(gb, chromosome, position, last));
	}
	
	/**
	 * Call the {@link GenomeBrowser#adjustAllSizes()} method for all {@link GenomeBrowser}s handled by
	 * this {@link MultipleGenomesBrowser}.
	 */
	private void adjustAllSizes() {
		for (GenomeBrowser g : genomeBrowsers) {
			g.adjustSize();
		}
		this.revalidate();
	}
	
	public void clearHighlight() {
		for (GenomeBrowser g: genomeBrowsers)
			for (int chr=0;chr<g.getGenome().getChromosomes().size();chr++)
				g.setHighlightRange(chr,0,-1, null);
	}
	
	public void clearGrey() {
		for (GenomeBrowser g: genomeBrowsers)
			for (int chr=0;chr<g.getGenome().getChromosomes().size();chr++)
				g.setRangeToGrey(chr, 0, -1, false);
	}
	
	
	private class GenomeScollThread implements Runnable {
		
		private int units,chromosome,gene;
		private GenomeBrowser gb;
		private Thread myThread;
		GenomeScollThread waitForMe;
		private boolean stop;
						
		public GenomeScollThread(GenomeBrowser gb, int chromosome, int gene, GenomeScollThread waitForMe) {
			stop = false;
			this.waitForMe = waitForMe;
			this.gb = gb;
			this.gene = gene;
			this.chromosome = chromosome;

			myThread = new Thread(this);
			myThread.start();
		}
		
		public Thread getThread() {
			return myThread;
		}
		
		public synchronized void stopScrolling() {
			stop = true;
		}
		
		public void run() {
			// Wait for previous scrolling activity to finish
			if (waitForMe!=null) {
				try {
					waitForMe.stopScrolling();
					waitForMe.getThread().join();
				} catch (InterruptedException e) {
					return;
				}
			}
			
			// Compute to what position we need to move the scrollbar
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					
					public void run() {
						int position=0;
						position += (chromosome+1) * (gecko.getGeneElementHight()+4); 				// The width of the starter marks
						position += chromosome * (gecko.getGeneElementHight()+4);					// The width of the end marks
						for (int i=0; i<chromosome; i++) 				// We skip all the previous chromosomes
							position += gb.getGenome().getChromosomes().get(i).getGenes().size() * gb.getGenWidth();
						position += (gene-1) * gb.getGenWidth(); 		// We skip the genes before the gene that has to be displayed
						position += gb.getLeftSpacerWidth();			// We skip the left spacer
						position -= gb.getWidth()/2;					 // We want to have the gene in the 
																		// middle of the screen, not at the left border
						position += gb.getGenWidth()+gb.getGenWidth()/2;
						if (gb.isFlipped()) { 							// If the genome browser is in flipped mode the position is invers
							position = gb.getHorizontalScrollBar().getMaximum()-position-gb.getWidth();
							position -= 1; // No - i don't know why... probably some border - or black magic
						}
						units = position - gb.getHorizontalScrollBar().getValue();

					}
				});
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Do the scrolling
			final JScrollBar sb = this.gb.getHorizontalScrollBar();
			int steps = 1; int sign = 1; int absunits = Math.abs(this.units);
			if (this.units<0) sign = -1;
			
			// If the animation is disabled, don't bother
			if (!gecko.isAnimationEnabled()) {
				
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							sb.setValue(sb.getValue()+GenomeScollThread.this.units);
							fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
						}
					});
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				int sleepTime = 10;
				for (int i=0; i<absunits; i=i+steps) {
					
					if (stop) break;
										
					final int finalsign = sign;
					final int finalsteps = steps;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							sb.setValue(sb.getValue()+finalsteps*finalsign);
							fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
						}
					});
					 

					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
//					if (i<absunits-1500)
//						steps = absunits-1000;
//					else 
					if (i<absunits-11000) {
						steps = 6000;
						sleepTime= 30;
					} else if (i<absunits-8000) {
						steps = 4000;
						sleepTime=30;
					} else if (i<absunits-5000) {
						steps = 2000;
						sleepTime=30;
					} else if (i<absunits-2000) {
						steps = 500;
						sleepTime=30;
					} else if (i<absunits-500) {
						steps = 250;
						sleepTime=30;
					}
					else if (i<absunits-100) {
						steps = 30;
						sleepTime = 30;
					}
					else if (i<absunits-80) {
						sleepTime = 30;
						steps = 20;
					} else if (i<absunits-60) {
						sleepTime = 10;
						steps = 4;
					} else if (i<absunits-5) {
						sleepTime=10;
						steps = 1;
					}
				}
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					MultipleGenomesBrowser.this.repaint();
				}
			});
			
		}
		
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
		clearGrey();
		if (gc.getType()==GeneCluster.TYPE_REFERENCE)
			rightPanel.setVisible(true);
		else
			rightPanel.setVisible(false);
		updateButtons(gOcc,subselection);
		for (int i=0;i<subselection.length;i++) {
			if (subselection[i]==GeneClusterOccurrence.GENOME_NOT_INCLUDED) continue;
			Subsequence s = gOcc.getSubsequences()[i][subselection[i]];
//			if (s==null) continue; // must actually not be the case
			if (getBrowser(i).isFlipped())
				scrollToPosition(i, s.getChromosome(), (int) Math.floor((s.getStart()-1+s.getStop()-1)/2));
			else
				scrollToPosition(i, s.getChromosome(), (int) Math.ceil((s.getStart()-1+s.getStop()-1)/2));
			if (i==gc.getRefSeqIndex())				
				highlightCluster(i, s.getChromosome(), s.getStart()-1, s.getStop()-1, GeneElement.COLOR_HIGHLIGHT_REFCLUST);
			else
				highlightCluster(i, s.getChromosome(), s.getStart()-1, s.getStop()-1, GeneElement.COLOR_HIGHLIGHT_DEFAULT);			
		}		
	}
	

	/*
	 * BrowserContentEvents
	 */
	
	private EventListenerList eventListener = new EventListenerList();

	
	public void addBrowserContentListener(BrowserContentListener l) {
		eventListener.add(BrowserContentListener.class, l);
	}
	
	public void removeBrowserContentListener(BrowserContentListener l) {
		eventListener.remove(BrowserContentListener.class, l);
	}
	
	protected synchronized void fireBrowserContentChanged(short type) {
		for (BrowserContentListener d : eventListener.getListeners(BrowserContentListener.class) ) {
			d.browserContentChanged(new BrowserContentEvent(this,type));
		}
	}
	

	public void addSelectionListener(ClusterSelectionListener s) {
		eventListener.add(ClusterSelectionListener.class, s);
	}
	public void removeSelectionListener(ClusterSelectionListener s) {
		eventListener.remove(ClusterSelectionListener.class, s);
	}
	
	protected synchronized void fireSelectionEvent(ClusterSelectionEvent e) {
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
			boolean stat = this.isFilterActiv();
			
			// deactivate the filter
			this.genomeBrowserFilter(false);
			
			if (e.getSelection()!=null)
				visualizeCluster(lastLocationEvent.getSelection(), lastLocationEvent.getgOcc(), lastLocationEvent.getsubselection());
		
			// save the currently selected cluster for the filter function
			this.setSelectedCluster(e.getSelection());
			
			// if the check box is selected we show only the genomes which contain the cluster
			if (stat)
			{
				this.genomeBrowserFilter(stat);
				visualizeCluster(lastLocationEvent.getSelection(), lastLocationEvent.getgOcc(), lastLocationEvent.getsubselection());
			}	
		}
	}
}
