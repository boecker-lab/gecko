package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.Subsequence;
import gecko2.event.BrowserContentEvent;
import gecko2.event.BrowserContentListener;
import gecko2.event.ClusterSelectionEvent;
import gecko2.event.ClusterSelectionListener;
import gecko2.event.DataEvent;
import gecko2.event.DataListener;
import gecko2.event.LocationSelectionEvent;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

public class GenomeNavigator extends JPanel implements DataListener,BrowserContentListener,ClusterSelectionListener {
	
	private static final long serialVersionUID = -3454613481139655426L;
	private MultipleGenomesBrowser mgb;
	private int virtualWidth=0;
	private int barWidth = 0;
	private float windowCoverage;
	private int maxBarMax;
	public static final int PADDING = 5;
	public static final int GENOME_DIST = 10;
	public static final int GENOME_LINE_WIDTH = 6;
	private GeneClusterOccurrence gOcc;
	
	public GenomeNavigator() {
		this.addComponentListener(this.componentAdapter);
		this.mgb = GeckoInstance.getInstance().getGui().getMgb();
	}
	
	@Override
	public void paint(Graphics g1) {
		super.paint(g1);
		if (mgb==null) return;
		
		Graphics2D g = (Graphics2D) g1;
		
		float scale = this.getWidth()/(virtualWidth*1.0F);
		
		int y = PADDING;
		
		for (int i=0; i<mgb.getGenomeBrowsers().size();i++) {
			GenomeBrowser gb = mgb.getGenomeBrowsers().get(i);
			JScrollBar bar = gb.getHorizontalScrollBar();
			
			// Compute the length of the bar representation for each chromosome
			ArrayList<Integer> lineLengths = new ArrayList<Integer>(gb.getGenome().getChromosomes().size());
			ArrayList<Chromosome> chromosomes = gb.getGenome().getChromosomes();
			for (int j=0; j<chromosomes.size(); j++)  {
				// Compute the line length in GenomeBrowser pixels
				int length = chromosomes.get(j).getGenes().size()*gb.getGenWidth();
				// Scale it to GenomeNavigator pixels
				lineLengths.add((int) Math.floor(length*scale));
				
			}
								
			// Compute the start pixel for the first chromosome
			int lineStart = maxBarMax-barWidth-2+(int) ChromosomeEnd.computeDimension().getWidth(); // rightmost position
			float substract = bar.getValue();// * (bar.getMaximum()/(1.0F*maxBarMax)); // substract value (scaled to longest bar)
			lineStart-= (int)(Math.floor(substract));	
			lineStart = (int) Math.floor(lineStart*scale);
			
			// Compute the distance of the chromosomes
			int chromDist = ((int) ChromosomeEnd.computeDimension().getWidth())*2;
			// Scale it to Nagivator pixels
			chromDist *= scale;
			
			if (gb.isFlipped()) Collections.reverse(lineLengths);
			
			ArrayList<Integer> lineStarts = new ArrayList<Integer>(lineLengths.size());
			lineStarts.add(lineStart);
			for (int x=1;x<lineLengths.size();x++)
				lineStarts.add(lineStarts.get(x-1)+lineLengths.get(x-1)+chromDist);
			for (int lineLength : lineLengths) {
				Shape t = new RoundRectangle2D.Float(lineStart,
						y,
						lineLength,
						GENOME_LINE_WIDTH,
						4,
						4);
				
				g.setStroke(new BasicStroke(1));
				if (gb.isFlipped())
					g.setColor(new Color(1F,1F,0.75F));
				else
					g.setColor(new Color(0.7F,0.7F,1.0F));
				g.fill(t);
				g.setColor(Color.BLACK);
				g.draw(t);
				lineStart += lineLength+chromDist;
			}
			if (gOcc!=null) {
				g.setColor(new Color(145,0,20));
				for (Subsequence s : gOcc.getSubsequences()[i]) {
					int chrom = s.getChromosome();
					System.err.println("chrom = "+chrom);
					int gcount;
					if (gb.isFlipped()) {
						System.err.println("chrom = "+gb.getGenome().getChromosomes().size()+"-"+chrom+"- 1");
						chrom = gb.getGenome().getChromosomes().size() - chrom -1;
						System.err.println("chomosome "+chrom);
						gcount = gb.getGenome().getChromosomes().get(s.getChromosome()).getGenes().size() - 1 - ((int) Math.ceil(((s.getStart()+s.getStop()-2)/2.0)));
					} else 
						gcount = ((int) ((s.getStart()+s.getStop()-2)/2.0));	
					
					int xpos = gcount*gb.getGenWidth();
					if (gb.isFlipped())
						xpos = (int) Math.ceil(xpos*scale);
					else
						xpos = (int) Math.floor(xpos*scale);
					xpos += lineStarts.get(chrom);

					g.drawLine(xpos, y, xpos, y+GENOME_LINE_WIDTH);
					g.drawLine(xpos+1, y, xpos+1, y+GENOME_LINE_WIDTH);
				}

			}
			y+= GENOME_DIST;
		}
			
		
		// Draw the window frame (minimum width is two pixels, otherwise it disappears
		// for large genomes and narrow windows
		int drawWidth = Math.max((int) Math.floor(windowCoverage*this.getWidth()), 2);
		Shape s = new RoundRectangle2D.Float((int) Math.floor(this.getWidth()/2.0F-(0.5*windowCoverage*this.getWidth())), 
				1, 
				drawWidth, 
				this.getHeight()-2, 
				2, 
				2);
		g.setStroke(new BasicStroke(1));
		g.setColor(new Color(0.7F,0.7F,0.7F,0.5F));
		g.fill(s);
	}

	/**
	 * Repaint the {@link GenomeNavigator}. Ensures to be run in the EventDispatchThread
	 * and might therefore not be effective instantly.
	 */
	public void refresh() {
		Runnable toRun = new Runnable() {
			
			@Override
			public void run() {
				GenomeNavigator.this.repaint();
				
			}
		};
		if (SwingUtilities.isEventDispatchThread())
			toRun.run();
		else
			SwingUtilities.invokeLater(toRun);
	}
	
	/**
	 * Update some the internal sizes "virtualWidth" and "windowCoverage" that
	 * are used to scale the scroll bar ranges from the {@link GenomeBrowser}s to the
	 * width of the {@link GenomeNavigator}. Re-computation is necessary whenever
	 * the width of a {@link GenomeBrowser} changes (displayed data changes, user
	 * uses zoom function).
	 */
	private void updateVirtualWidth() {
		maxBarMax=0;
		for (GenomeBrowser gb : mgb.getGenomeBrowsers()) {
			JScrollBar bar = gb.getHorizontalScrollBar();
			barWidth = gb.getWidth();
			
			if (bar.getMaximum()>maxBarMax)
				maxBarMax = bar.getMaximum();
		}
		int maxGenomeWidth = maxBarMax - (2*(barWidth+2)); 
		virtualWidth = 2*maxGenomeWidth + barWidth;
		windowCoverage = barWidth/(1.0F*virtualWidth);
	}
	
	private ComponentAdapter componentAdapter = new ComponentAdapter() {
		public void componentResized(java.awt.event.ComponentEvent e) {
			updateVirtualWidth();
			refresh();
		};
	};

	@Override
	public void dataChanged(DataEvent e) {
		if (mgb.getGenomeBrowsers().size()==0) {
			this.setPreferredSize(new Dimension(0,0));
		} else {
			this.setPreferredSize(new Dimension(0,mgb.getGenomeBrowsers().size()*(GENOME_DIST)+2*PADDING));
			updateVirtualWidth();
		}
		this.getParent().doLayout();
		this.refresh();
	}

	@Override
	public void browserContentChanged(BrowserContentEvent e) {
		if (e.getEventType()==BrowserContentEvent.ZOOM_FACTOR_CHANGED) {
			updateVirtualWidth();
		}
		this.refresh();		
	}
	
	@Override
	public void selectionChanged(ClusterSelectionEvent e) {
		if ((!(e instanceof LocationSelectionEvent)) || e.getSelection()==null)
			this.gOcc=null;
		else {
			this.gOcc = ((LocationSelectionEvent)e).getgOcc();
		}
		repaint();				
	}
	
}
