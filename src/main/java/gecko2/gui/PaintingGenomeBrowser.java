package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.Gene;
import gecko2.algorithm.Genome;
import gecko2.event.BrowserContentEvent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

public class PaintingGenomeBrowser extends AbstractGenomeBrowser {
	private static final long serialVersionUID = -87373710905284714L;
	
	private JPanel canvas;
	private Genome genome;
	private GeckoInstance gecko;
	private MultipleGenomesBrowser parent;
	private int borderSpace;
	private final int hgap = 2;
	private final int vgap = 2;
	
	private String maxLengthString;
	
	/**
	 * The genes that will be highlighted.
	 * An array of int arrays, that contain the chromosome index, the start and the stop gene index
	 */
	private int[] highlights;
	private Color highlightColor;
	
	
	public PaintingGenomeBrowser(Genome g, MultipleGenomesBrowser parent) {
		this.gecko = GeckoInstance.getInstance();
		this.setBorder(null);
		this.genome = g;
		this.parent = parent;
		
		FlowLayout flowlayout = new FlowLayout(FlowLayout.LEFT);
		flowlayout.setHgap(0);
		flowlayout.setVgap(0);
		canvas = new GenomeCanvas();
		this.setBackground(Color.WHITE);
		canvas.setBackground(Color.WHITE);
		this.canvas.setPreferredSize(new Dimension (getCanvasWidth(), getGBHeight()));
		this.setViewportBorder(null);
		this.setViewportView(this.canvas);
		this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		this.adjustSize();
		this.addComponentListener(new PaintingGenomeBrowserListener());
		PaintingGenomeBrowserMouseListener listener = new PaintingGenomeBrowserMouseListener();
		canvas.addMouseMotionListener(listener);
		canvas.addMouseListener(listener);
	}

	@Override
	public void adjustSize() {
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, getGBHeight()));
		this.setMinimumSize(new Dimension(20, getGBHeight()));
		this.setPreferredSize(new Dimension(20, getGBHeight()));
		borderSpace = (int)this.getSize().getWidth();
		
		this.canvas.setPreferredSize(new Dimension (getCanvasWidth(), getGBHeight()));

		this.revalidate();
		this.repaint();
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
		parent.fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
	}
	
	@Override
	public void scrollToPosition(int chromosomeIndex, int geneIndex) {
		this.getHorizontalScrollBar().setValue(getScrollPosition(chromosomeIndex, geneIndex));
		parent.fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
	}
	
	private int getScrollPosition(int chromosomeIndex, int geneIndex) {
		int scrollPosition = borderSpace/2;  // skip left border (+ borderSpace), but we want the gene in the middle (- borderSpace/2) 
		for (int i=0; i<chromosomeIndex; i++) {
			scrollPosition += 2 * getChromosomeEndingWidth();
			scrollPosition += genome.getChromosomes().get(i).getGenes().size() * getGeneWidth();
		}
		scrollPosition += getChromosomeEndingWidth();
		scrollPosition += geneIndex * getGeneWidth();
		scrollPosition += getGeneWidth()/2; // center on the middle of the gene
		return scrollPosition;
	}
	
	private Gene getGeneAtPosition(int position) {
		position -= borderSpace;  // Ignore left border
		
		for (Chromosome chr : genome.getChromosomes()) {
			position -= getChromosomeEndingWidth();
			if (position <= 0)
				return null;
			for (Gene gene : chr.getGenes()){
				position -= getGeneWidth();
				if (position <= 0)
					return gene;
			}
			position -= getChromosomeEndingWidth();
			if (position <= 0)
				return null;
		}
		return null;
	}
	
	private Chromosome getChromosomeAtPosition(int position) {
		position -= borderSpace;  // Ignore left border
		
		for (Chromosome chr : genome.getChromosomes()) {
			position -= getChromosomeEndingWidth();
			if (position <= 0)
				return null;
			position -= chr.getGenes().size()*getGeneWidth();
			if (position <= 0)
				return chr;
			position -= getChromosomeEndingWidth();
			if (position <= 0)
				return null;
		}
		return null;
	}
	
	@Override
	public boolean isFlipped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void flip() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getScrollWidth() {
		return this.getHorizontalScrollBar().getWidth();
	}

	@Override
	public Genome getGenome() {
		return genome;
	}

	@Override
	public int getGeneWidth() {
		return getGeneElementWidth() + 2 * hgap;
	}
	
	private int getChromosomeEndingWidth() {
		return GenomePainting.getChromosomeEndingWidth(getGeneElementWidth()) + 2 * hgap;
	}
	
	private int getGeneElementWidth() {
		if (canvas.getGraphics() == null)	// if we have no graphics
			return 8 * gecko.getMaxIdLength(); // improvise!
		
		if (maxLengthString == null || maxLengthString.length() != gecko.getMaxIdLength()){
			StringBuilder builder = new StringBuilder(gecko.getMaxIdLength());
			for (int i=0; i<gecko.getMaxIdLength(); i++)
				builder.append("w");
			maxLengthString = new String(builder);
		}
		return GenomePainting.getGeneWidth(canvas.getGraphics(), maxLengthString, gecko.getGeneElementHight());
	}

	@Override
	public int getGBHeight() {
		return 2 * vgap + gecko.getGeneElementHight();
	}
	
	private int getCanvasWidth() {
		return 2 * borderSpace + 
				genome.getTotalGeneNumber() * getGeneWidth() + 
				2 * genome.getChromosomes().size() * getChromosomeEndingWidth();
	}
	
	@Override
	public void highlightCluster(int chr, int start, int stop,
			Color highlightColor) {
		highlights = new int[]{chr, start, stop};
		this.highlightColor = highlightColor;
		canvas.repaint();
	}

	@Override
	public void clearHighlight() {
		highlights = null;
		highlightColor = null;
		canvas.repaint();
	}
	
	private class GenomeCanvas extends JPanel {
		private static final long serialVersionUID = 6009238153875103381L;

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			//TODO evlt Bild speicher und nicht immer neu zeichnen
			GenomePainting.paintGenomeWithCluster(g, genome, highlights, highlightColor, borderSpace, vgap, getGeneElementWidth(), gecko.getGeneElementHight(), hgap, vgap);
		}
	}
	
	private class PaintingGenomeBrowserListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			super.componentResized(e);
			final int offset = -borderSpace + (int)PaintingGenomeBrowser.this.getSize().getWidth();
			borderSpace = (int)PaintingGenomeBrowser.this.getSize().getWidth();
			canvas.setPreferredSize(new Dimension (getCanvasWidth(), getGBHeight()));
			PaintingGenomeBrowser.this.revalidate();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					PaintingGenomeBrowser.this.adjustScrollPosition(offset);					
				}
			});
		}
	}
	
	private class PaintingGenomeBrowserMouseListener extends MouseInputAdapter {
		
		private int clickXPos;
		
		@Override
		public void mousePressed(MouseEvent e) {
			this.clickXPos = e.getX();
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			int diff = e.getX()-this.clickXPos;
			adjustScrollPosition(-diff);
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			Gene gene = getGeneAtPosition(e.getX());
			if (gene != null) {
				canvas.setToolTipText(gene.getSummary());
			} else {
				canvas.setToolTipText(null);
			}
			Chromosome chr = getChromosomeAtPosition(e.getX());
			if (chr != null)
				GeckoInstance.getInstance().getGui().setInfobarText(chr.getFullName());
			else
				GeckoInstance.getInstance().getGui().setInfobarText("");
		}
	}
}
