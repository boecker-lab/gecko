package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;
import gecko2.algorithm.Subsequence;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;


public class MultipleGenomesBrowser extends JPanel {

	private static final long serialVersionUID = -6769789368841494821L;
	private JPanel leftpanel ;
	private JPanel centerpanel;
	private JPanel rightPanel;
	private ArrayList<GenomeBrowser> genomeBrowsers;
	private ScrollListener wheelListener;
	
	GeckoInstance gecko;

	public float[] getScrollPositions() {
		float[] pos = new float[genomeBrowsers.size()];
		for (int i=0; i<genomeBrowsers.size(); i++) {
			GenomeBrowser gb = genomeBrowsers.get(i);
			JScrollBar s = gb.getHorizontalScrollBar();
			pos[i] = (s.getValue()-gb.getLeftSpacerWidth())/(float)(s.getMaximum()-2*gb.getLeftSpacerWidth());
		}
		return pos;
	}
	
	public void setScrollPositions(float[] positions) {
		if (positions.length!=genomeBrowsers.size()) 
			throw new IndexOutOfBoundsException("The positions array size does not fit the number of GenomeBrowsers");
		for (int i=0;i<positions.length;i++) {
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
		this.setPreferredSize(new Dimension(0,0));
		this.wheelListener = new ScrollListener();
		this.genomeBrowsers = new ArrayList<GenomeBrowser>();
		this.setLayout(new BorderLayout());
		this.leftpanel = new JPanel();
		this.centerpanel = new JPanel();
		centerpanel.setBackground(Color.WHITE);
		this.rightPanel = new JPanel();
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
		NumberInRectangle n = new NumberInRectangle(genomeBrowsers.size(),gb.getBackground());
//		n.addMouseListener(gb.getGenomebrowsermouseover());
		boxPanel.add(n);
		boxPanel.add(Box.createRigidArea(new Dimension(4, 0)));
		boxPanel.add(gb);
		this.centerpanel.add(boxPanel);
		this.setPreferredSize(new Dimension((int)this.getPreferredSize().getWidth(),(int) this.getPreferredSize().getHeight()+gb.getGBHeight()));
		this.repaint();
		this.revalidate();
	}

	
	public void clear() {
		this.setPreferredSize(new Dimension(0,0));
		this.centerpanel.removeAll();
		this.genomeBrowsers.clear();
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
		ArrayList<Chromosome> chromosomes = this.genomeBrowsers.get(id).getGenome().getChromosomes();
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
		GeneCluster cluster = gecko.getClusters()[gecko.getHighlightedCluster()];
		Subsequence[] subseqs = cluster.getSubsequences();
		int[] positions = new int[subseqs.length];
		Arrays.fill(positions, -1);
		ArrayList<Integer> minus = new ArrayList<Integer>();
		ArrayList<Integer> plus = new ArrayList<Integer>();
		for (int i=0; i<subseqs.length;i++) {
			ArrayList<Chromosome> genes = gecko.getGenomes()[i].getChromosomes();
			for (int j=subseqs[i].getStart()-1;j<subseqs[i].getStop();j++) {
				if (Math.abs(genes.get(subseqs[i].getChromosome()).getGenes().get(j).getId())==geneID) {
					positions[i] = j;
					if (genes.get(subseqs[i].getChromosome()).getGenes().get(j).getId()<0) 
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
			if (positions[i]!=-1) scrollToPosition(i, subseqs[i].getChromosome(), positions[i]);
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
						// TODO Auto-generated method stub
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
					if (i<absunits-1000) {
						steps = 500;
						sleepTime= 3;
					} else if (i<absunits-300) {
						sleepTime=10;
						steps = 40;
					} else if (i<absunits-200) {
						sleepTime=10;
						steps = 14;
					}
						
					else if (i<absunits-100) {
						sleepTime = 10;
						steps = 10;
					}
					else if (i<absunits-80) {
						sleepTime = 10;
						steps = 8;
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
	

	/*
	 * BrowserContentEvents
	 */
	
	public interface BrowserContentListener extends EventListener {
		void browserContentChanged(BrowserContentEvent e);
	}
	
	public class BrowserContentEvent extends EventObject {
		
		public static final short SCROLL_VALUE_CHANGED = 1;
		public static final short ZOOM_FACTOR_CHANGED = 2;
		
		private short eventType;
		private static final long serialVersionUID = 1382632021469547584L;

		public BrowserContentEvent(MultipleGenomesBrowser source, short eventType) {
			super(source);
			this.eventType = eventType;
		}
		
		public short getEventType() {
			return eventType;
		}
		
		@Override
		public MultipleGenomesBrowser getSource() {
			return (MultipleGenomesBrowser) super.getSource();
		}
	}

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
	/*
	 * END BrowserContentEvents
	 */


}
