package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.Gene;
import gecko2.algorithm.Genome;
import gecko2.event.BrowserContentEvent;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class GenomeBrowser extends AbstractGenomeBrowser {
	
	private final static Color BG_COLOR_FLIPPED = Color.ORANGE;
	
	private final FlowLayout flowlayout;
	
	private static final long serialVersionUID = 7086043901343368118L;
	private final Genome genome;
	private ArrayList<GeneElement> genElements[];
	private final JPanel contentPanel;
	private final GenomeBrowserMouseDrag genomebrowsermousedrag;
	private final GeckoInstance gecko;
	private GenomeScollThread last;
	private final AbstractMultipleGenomeBrowser parent;
	
	private boolean flipped = false;
	
	private final JPanel leftspace;
    private final JPanel rightspace;
	
	private boolean init = true;
	
	public GenomeBrowser(Genome g, MultipleGenomesBrowser parent) {
		this.parent = parent;
		gecko = GeckoInstance.getInstance();
		genomebrowsermousedrag = new GenomeBrowserMouseDrag();
		this.setBorder(null);
		this.contentPanel = new JPanel();
		this.leftspace = new JPanel();
		this.rightspace = new JPanel();
		this.flowlayout = new FlowLayout(FlowLayout.LEFT);
		this.flowlayout.setHgap(0);
		this.flowlayout.setVgap(0);
		this.contentPanel.setLayout(this.flowlayout);
		this.genome = g;
		this.setBackground(Color.WHITE);
		this.contentPanel.setBackground(Color.WHITE);
		this.adjustSize();
		this.setViewportBorder(null);
		this.setViewportView(this.contentPanel);
		this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		this.addComponentListener(new GenomeBrowserListener());
		createGeneElements();
	}
	
	@Override
	public boolean isFlipped() {
		return flipped;
	}	

	/**
	 * Call the adjustSize() method for all GeneElements handled by this
	 * GenomeBrowser
	 */
	private void adjustAllSizes() {
		for (Component c : contentPanel.getComponents())
			if (c instanceof Adjustable)
				((Adjustable) c).adjustSize();
		this.revalidate();
		this.repaint();
	}
	
	private void setRangeToGrey(int chromosome, int start, int stop, boolean grey) {
		if (stop==-1) stop = genElements[chromosome].size()-1;
		if (genElements[chromosome].size()!=0)
			for (int i=start;i<=stop;i++)
				this.genElements[chromosome].get(i).setGrey(grey);
		this.repaint();
	}
	
	/**
	 * Highlights a range within a chromosome
	 * @param chromosome The id of the chromosome
	 * @param left The first gene to highlight
	 * @param right The last gene to highlight
	 * @param highlightColor the color to use for the highlight
	 */
	private void markClusterBorder(int chromosome, int left, int right, Color highlightColor) {
		for (int i=0;i<genElements.length;i++)
			this.setHighlightRange(i, 0, genElements[i].size()-1, null);
		this.setHighlightRange(chromosome, left, right, highlightColor);
	}
	
	@Override
	public void clearHighlight() {
		for (int chr=0;chr<genome.getChromosomes().size();chr++){
			setHighlightRange(chr,0,-1, null);
			setRangeToGrey(chr, 0, -1, false);
		}
	}
	
	/**
	 * Highlights or unhighlights a range within a chromosome
	 * @param chromosome The id of the chromosome
	 * @param start The first gene to be affected
	 * @param stop The last gene to be affected
	 * @param highlight {@code true} for highlight {@code false} for unhighlight
	 */
	private void setHighlightRange(int chromosome, int start, int stop, Color highlight) {
		if (stop==-1) 
			stop = genElements[chromosome].size()-1;
		if (genElements[chromosome].size()!=0)
			for (int i=start; i<=stop; i++)
				genElements[chromosome].get(i).setHighlighted(highlight);
	}
	
	/**
	 * Marks a region within a genome by setting everything except that region to 
	 * greyscale instead of the gene colors and by changing the background color
	 * of the specified region.
	 * @param chr The id of the chromosome
	 * @param start The first gene in the region. If the value is greater than stop
	 * or below zero the whole genome is set to greyscale.
	 * @param stop The last gene in the region. If the value is lower than start or 
	 * greater than the chromosome length -1 the whole genome is set to greyscale.
     * @param highlightColor The color that is used for highlighting
	 */
	public void highlightCluster(int chr, int start, int stop, Color highlightColor) {
		List<Chromosome> chromosomes = genome.getChromosomes();
		int genomeLength = chromosomes.get(chr).getGenes().size();
		// Set everything to grey if the parameter don't make sense
		// (algorithm produces start>stop if a genome is not part of the cluster)
		
		if (start<0 || stop>=genomeLength || start>stop) {
			for (int i=0;i<chromosomes.size();i++)
				this.setRangeToGrey(i, 0, chromosomes.get(i).getGenes().size()-1, true);
			return;
		}
		// Set all the chromosomes before to grey
		for (int i=0;i<chr;i++)
			this.setRangeToGrey(i, 0, chromosomes.get(i).getGenes().size()-1, true);
		// Set the target chromosome to grey except for the highlighted region
		this.setRangeToGrey(chr, 0, start-1, true);
		this.setRangeToGrey(chr, start, stop, false);
		this.setRangeToGrey(chr, stop+1, genomeLength-1, true);
		// Set all the chromosomes after to grey
		for (int i=chr+1;i<chromosomes.size();i++)
			this.setRangeToGrey(i, 0, chromosomes.get(i).getGenes().size()-1,true);
		// Highlight (color the background)
		this.markClusterBorder(chr, start, stop, highlightColor);
	}
	
	private void arrangeGeneElements(boolean flipped) {
		boolean changeOrientation = false;
		if (this.flipped!=flipped) {
			this.getHorizontalScrollBar().setValue(this.getHorizontalScrollBar().getMaximum()-this.getHorizontalScrollBar().getValue()-leftspace.getWidth()+2);
			// TODO Understand dirty +2 hack - probably the border width
			changeOrientation = true;
		}
		this.flipped = flipped;
		contentPanel.removeAll();
		contentPanel.add(leftspace);
		if (flipped) {
			contentPanel.setBackground(BG_COLOR_FLIPPED);
			for (int j=genElements.length-1;j>=0;j--) {
				contentPanel.add(new ChromosomeEnd(contentPanel.getBackground(), ChromosomeEnd.LEFT));
				for (int i=genElements[j].size()-1; i>=0; i--) {
					genElements[j].get(i).setBackground(contentPanel.getBackground());
					contentPanel.add(genElements[j].get(i));
					if (changeOrientation) genElements[j].get(i).flipOrientation();
				}
				contentPanel.add(new ChromosomeEnd(contentPanel.getBackground(), ChromosomeEnd.RIGHT));
			}
		} else  {
			contentPanel.setBackground(Color.WHITE);
			for (ArrayList<GeneElement> chromo : genElements) {
				contentPanel.add(new ChromosomeEnd(contentPanel.getBackground(), ChromosomeEnd.LEFT));
				for (GeneElement e : chromo) {
					contentPanel.add(e);
					e.setBackground(contentPanel.getBackground());
					if (changeOrientation) e.flipOrientation();
				}
				contentPanel.add(new ChromosomeEnd(contentPanel.getBackground(), ChromosomeEnd.RIGHT));
			}
		}
		contentPanel.add(rightspace);
		contentPanel.revalidate();
		this.gecko.getGui().getMgb().fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
	}
	
	private void arrangeGeneElements() {
		arrangeGeneElements(false);
	}
	
	@SuppressWarnings("unchecked")
	private void createGeneElements() {
		genElements = new ArrayList[genome.getChromosomes().size()];
		for (int i=0;i<genome.getChromosomes().size();i++) {
			MouseListener ml = genome.getChromosomes().get(i).getChromosomeMouseListener();
			genElements[i] = new ArrayList<GeneElement>();
			for (Gene g : this.genome.getChromosomes().get(i).getGenes()) {
				GeneElement element = new GeneElement(g);
				if (g.getId()<0) element.setOrientation(GeneElement.ORIENTATION_BACKWARDS);
				element.addMouseMotionListener(genomebrowsermousedrag);
				element.addMouseListener(genomebrowsermousedrag);
				element.addMouseListener(ml);
				if (g.isUnknown()) element.setUnknown(true);
				this.genElements[i].add(element);
			}
		}
		arrangeGeneElements();
 	}
	
	@Override
	public void adjustSize() {
		// Save old scroll position
		float pos = (getScrollValue()-leftspace.getWidth())/(float)(getMaximumValue()-2*leftspace.getWidth());
		
		this.adjustAllSizes();
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, getGBHeight()));
		this.setMinimumSize(new Dimension(20,getGBHeight()));
		this.setPreferredSize(new Dimension(20,getGBHeight()));
		
		// set scroll position to fit new spacer width
		this.getHorizontalScrollBar().setValue((int)(leftspace.getWidth() + (pos*(getMaximumValue()-2*leftspace.getWidth()))));
		parent.fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
		
		this.revalidate();
		this.repaint();
	}
	
	private class GenomeBrowserListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			super.componentResized(e);
			final int brwidth = (int)GenomeBrowser.this.getSize().getWidth();
			leftspace.setPreferredSize(new Dimension(brwidth,0));
			rightspace.setPreferredSize(new Dimension(brwidth,0));
			leftspace.revalidate();
			rightspace.revalidate();
			if (init) {
				init = false;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						GenomeBrowser.this.adjustScrollPosition(brwidth);						
					}
				});
			}
		}
	}
	
	@Override
	public int getGeneWidth() {
		if (this.genElements.length==0 || this.genElements[0].size()==0)
			return 0;
		return this.genElements[0].get(0).getWidth()+this.flowlayout.getHgap();
	}
	
	@Override
	public Genome getGenome() {
		return genome;
	}
	
	@Override
	public int getGBHeight() {
		return 4+gecko.getGeneElementHight()+this.flowlayout.getVgap();
	}
	
	@Override
	public void flip() {
		arrangeGeneElements(!flipped);
	}
	
	@Override
	public int getScrollValue() {
		return this.getHorizontalScrollBar().getValue();
	}
	
	@Override
	public int getMaximumValue() {
		return this.getHorizontalScrollBar().getMaximum();
	}
	
	@Override
	public void adjustScrollPosition(int value) {
		this.getHorizontalScrollBar().setValue(this.getHorizontalScrollBar().getValue() + value);
	}
	
	
	/**
	 * Scrolls to a specified position within a genome. A thread is created to perform the
	 * scrolling. The funktion returns after the Tread is created and started.
	 * @param chromosome The chromosome id
	 * @param position The position of the gene within the chromosome
	 */
	public synchronized void scrollToPosition(int chromosome, int position) {
		if (position<0
				|| genome.getChromosomes().size()<=chromosome
				|| genome.getChromosomes().get(chromosome).getGenes().size()<=position) 
			return;
		last = new GenomeScollThread(chromosome, position, last);
	}
	
	@Override
	public int getScrollWidth() {
		return this.getHorizontalScrollBar().getWidth();
	}
	
	private class GenomeBrowserMouseDrag extends MouseInputAdapter {
		
		private int clickXPos;
		
		@Override
		public void mousePressed(MouseEvent e) {
			this.clickXPos = e.getX();
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount()%2==0 && e.getSource() instanceof GeneElement) {
				if (!e.isShiftDown()) {
					GeneElement el = (GeneElement) e.getSource();
					if (el.isHighlighted()) {
						parent.centerCurrentClusterAt(Math.abs(el.getGene().getId()));
					}
				} else
					arrangeGeneElements(!flipped);
				}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			int diff = e.getX()-this.clickXPos;
			adjustScrollPosition(-diff);
		}
	}
	
    private class GenomeScollThread implements Runnable {
        private int units;
        private final int chromosome;
        private final int gene;
		private final Thread myThread;
		final GenomeScollThread waitForMe;
		private boolean stop;
						
		public GenomeScollThread(int chromosome, int gene, GenomeScollThread waitForMe) {
			stop = false;
			this.waitForMe = waitForMe;
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
							position += getGenome().getChromosomes().get(i).getGenes().size() * getGeneWidth();
						position += (gene-1) * getGeneWidth(); 		// We skip the genes before the gene that has to be displayed
						position += leftspace.getWidth();			// We skip the left spacer
						position -= getWidth()/2;					 // We want to have the gene in the 
																		// middle of the screen, not at the left border
						position += getGeneWidth()+getGeneWidth()/2;
						if (isFlipped()) { 							// If the genome browser is in flipped mode the position is invers
							position = getMaximumValue()-position-getWidth();
							position -= 1; // No - i don't know why... probably some border - or black magic
						}
						units = position - getScrollValue();

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
			int steps = 1;
            int sign = 1;
            int absunits = Math.abs(this.units);
			if (this.units<0) sign = -1;
			
			// If the animation is disabled, don't bother
			if (!gecko.isAnimationEnabled()) {
				
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							adjustScrollPosition(GenomeScollThread.this.units);
							parent.fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
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
					
					if (stop)
                        break;
										
					final int finalsign = sign;
					final int finalsteps = steps;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							adjustScrollPosition(finalsteps*finalsign);
							parent.fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
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
					parent.repaint();
				}
			});
			
		}
		
	}
}

