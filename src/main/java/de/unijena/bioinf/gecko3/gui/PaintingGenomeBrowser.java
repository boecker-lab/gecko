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

import com.google.common.collect.Lists;
import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.Chromosome;
import de.unijena.bioinf.gecko3.datastructures.Gene;
import de.unijena.bioinf.gecko3.datastructures.Genome;
import de.unijena.bioinf.gecko3.event.BrowserContentEvent;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

public class PaintingGenomeBrowser extends AbstractGenomeBrowser {
	private static final long serialVersionUID = -87373710905284714L;
	
	private final JPanel canvas;
	private final Genome genome;
	private final GeckoInstance gecko;
	private final MultipleGenomesBrowser parent;
	private final int borderSpace = 10000;
	private static final int hgap = 2;
	private static final int vgap = 2;

	private String maxLengthString;
	private GenomePainting.NameType nameType;

    private boolean flipped;

    /**
     * The current scroll position
     */
    private int geneIndex;
    private int chromosomeIndex;
    private int minPosition;
    private int maxPosition;


	/**
	 * The genes that will be highlighted.
	 * An array of int arrays, that contain the chromosome index, the start and the stop gene index
	 */
	private int[] highlights;
	private Color highlightColor;
	
	
	public PaintingGenomeBrowser(Genome g, MultipleGenomesBrowser parent, GenomePainting.NameType nameType) {
		this.gecko = GeckoInstance.getInstance();
		this.setBorder(null);
		this.genome = g;
		this.parent = parent;
        this.nameType = nameType;
        this.flipped = false;
        this.geneIndex = -1;
        this.chromosomeIndex = -1;
		
		FlowLayout flowlayout = new FlowLayout(FlowLayout.LEFT);
		flowlayout.setHgap(0);
		flowlayout.setVgap(0);
		this.canvas = new GenomeCanvas();
		this.setBackground(Color.WHITE);
		this.canvas.setBackground(Color.WHITE);
		this.canvas.setPreferredSize(new Dimension (this.getCanvasWidth(), this.getGBHeight()));
		this.setViewportBorder(null);
		this.setViewportView(this.canvas);
		this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		this.addComponentListener(new PaintingGenomeBrowserListener());
		PaintingGenomeBrowserMouseListener listener = new PaintingGenomeBrowserMouseListener();
		canvas.addMouseMotionListener(listener);
		canvas.addMouseListener(listener);
        scrollToInitialPosition();
        this.adjustSize();
	}

    @Override
    public Dimension getMaximumSize(){
        return new Dimension(getCanvasWidth(), getGBHeight());
    }

    @Override
    public Dimension getMinimumSize(){
        return new Dimension(50, getGBHeight());
    }

    @Override
    public Dimension getPreferredSize(){
        return new Dimension(getCanvasWidth(), getGBHeight());
    }

	@Override
	public void adjustSize() {
		canvas.setPreferredSize(new Dimension (getCanvasWidth(), getGBHeight()));
        canvas.invalidate();
		revalidate();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PaintingGenomeBrowser.this.scrollToPosition(chromosomeIndex, geneIndex);
            }
        });
	}

	private int getScrollValue() {
		return this.getHorizontalScrollBar().getValue();
	}

	private int getMaximumValue() {
		return this.getHorizontalScrollBar().getMaximum();
	}

	@Override
	public void adjustScrollPosition(int value) {
        int newScrollValue = getHorizontalScrollBar().getValue() + value;
        checkAndSetCenterGene(newScrollValue);
		this.getHorizontalScrollBar().setValue(newScrollValue);
		parent.fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
	}
	
	@Override
	public void scrollToPosition(int chromosomeIndex, int geneIndex) {
        int newScrollPosition = getCenterScrollPosition(chromosomeIndex, geneIndex);
        setMinMaxPositions(chromosomeIndex, geneIndex, newScrollPosition);
		this.getHorizontalScrollBar().setValue(newScrollPosition);
		parent.fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
	}

    /**
     * Finds the genes that will make the genome start at the left border and scrolls there
     */
    private void scrollToInitialPosition(){
        int chromosomeIndex = 0;
        int geneIndex = 0;

        int scrollPosition = 0;
        while(scrollPosition < 8){ // skip 8 genes
            if (geneIndex == genome.getChromosomes().get(chromosomeIndex).getGenes().size() - 1){
                if (chromosomeIndex == genome.getChromosomes().size() - 1)
                    break;
                geneIndex = 0;
                chromosomeIndex++;
                scrollPosition += 2;
            } else {
                geneIndex++;
                scrollPosition ++;
            }
        }
        scrollToPosition(chromosomeIndex, geneIndex);
    }

	
	private int getCenterScrollPosition(int chromosomeIndex, int geneIndex) {
		int scrollPosition = borderSpace;  // skip left border (+ borderSpace)
        scrollPosition -= this.getWidth()/2; // un-skip half the width, so we are centered
		for (int i=0; i<chromosomeIndex; i++) {
			scrollPosition += 2 * getChromosomeEndingWidth();
			scrollPosition += genome.getChromosomes().get(i).getGenes().size() * getGeneWidth();
		}
		scrollPosition += getChromosomeEndingWidth();

        if (!flipped)
		    scrollPosition += geneIndex * getGeneWidth();
        else
            scrollPosition += (genome.getChromosomes().get(chromosomeIndex).getGenes().size() - 1 - geneIndex) * getGeneWidth();

		scrollPosition += getGeneWidth()/2; // center on the middle of the gene

		return scrollPosition;
	}

    private void setMinMaxPositions() {
        if (chromosomeIndex < 0)
            return;
        int centerPosition = getCenterScrollPosition(chromosomeIndex, geneIndex);
        setMinMaxPositions(chromosomeIndex, geneIndex, centerPosition);
    }

    /**
     * Sets the minimum and maximum scroll positions that still center on this gene.
     * @param chromosomeIndex
     * @param geneIndex
     * @param scrollPosition the center position for this gene
     */
    private void setMinMaxPositions(int chromosomeIndex, int geneIndex, int scrollPosition) {
        this.chromosomeIndex = chromosomeIndex;
        this.geneIndex = geneIndex;
        minPosition = scrollPosition;
        maxPosition = scrollPosition + getGeneWidth();
        if (!flipped) {
            if (geneIndex == 0) {
                if (chromosomeIndex == 0)
                    minPosition = 0;
                else
                    minPosition -= 2 * getChromosomeEndingWidth();
            }
            if (geneIndex == genome.getChromosomes().get(chromosomeIndex).getGenes().size() - 1) {
                if (chromosomeIndex < genome.getChromosomes().size() - 1)
                    maxPosition += 2 * getChromosomeEndingWidth();
                else
                    maxPosition = getHorizontalScrollBar().getMaximum();
            }
        } else {
            if (geneIndex == 0) {
                if (chromosomeIndex < genome.getChromosomes().size() - 1)
                    maxPosition += 2 * getChromosomeEndingWidth();
                else
                    maxPosition = getHorizontalScrollBar().getMaximum();
            }
            if (geneIndex == genome.getChromosomes().get(chromosomeIndex).getGenes().size() - 1) {
                if (chromosomeIndex == 0)
                    minPosition = 0;
                else
                    minPosition -= 2 * getChromosomeEndingWidth();
            }
        }
    }


    private void checkAndSetCenterGene(int position){
        boolean changedCenter = false;
        if (!flipped) {
            if (position < minPosition) {
                if (geneIndex == 0) {
                    chromosomeIndex--;
                    geneIndex = genome.getChromosomes().get(chromosomeIndex).getGenes().size() - 1;
                    changedCenter = true;
                } else {
                    geneIndex--;
                    changedCenter = true;
                }
            } else if (position > maxPosition) {
                if (geneIndex == genome.getChromosomes().get(chromosomeIndex).getGenes().size() - 1){
                    chromosomeIndex++;
                    geneIndex = 0;
                    changedCenter = true;
                } else {
                    geneIndex++;
                    changedCenter = true;
                }
            }
        } else {
            if (position < minPosition) {
                if (geneIndex == genome.getChromosomes().get(chromosomeIndex).getGenes().size() - 1){
                    chromosomeIndex--;
                    geneIndex = 0;
                    changedCenter = true;
                } else {
                    geneIndex++;
                    changedCenter = true;
                }
            } else if (position > maxPosition) {
                if (geneIndex == 0) {
                    chromosomeIndex++;
                    geneIndex = genome.getChromosomes().get(chromosomeIndex).getGenes().size() - 1;
                    changedCenter = true;
                } else {
                    geneIndex--;
                    changedCenter = true;
                }
            }
        }
        if (changedCenter)
            setMinMaxPositions();
    }

    /**
     * Returns the gene at the given x positions
     * @param position
     * @return
     */
	private Gene getGeneAtPosition(int position) {
		position -= borderSpace;  // Ignore left border
		
		for (Chromosome chr : genome.getChromosomes()) {
			position -= getChromosomeEndingWidth();
			if (position <= 0)
				return null;
            java.util.List<Gene> listView = flipped ? Lists.reverse(chr.getGenes()) : chr.getGenes();
            for (Gene gene : listView){
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

    /**
     * Returns the chromosome at the given x position
     * @param position
     * @return
     */
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
		return flipped;
	}

	@Override
	public void flip() {
        this.flipped = !this.flipped;
        canvas.invalidate();
        revalidate();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PaintingGenomeBrowser.this.scrollToPosition(chromosomeIndex, geneIndex);
            }
        });
	}

	@Override
	public Genome getGenome() {
		return genome;
	}

	private int getGeneWidth() {
		return getGeneElementWidth() + 2 * hgap;
	}
	
	private int getChromosomeEndingWidth() {
		return GenomePainting.getChromosomeEndingWidth(getGeneElementWidth()) + 2 * hgap;
	}
	
	private int getGeneElementWidth() {
		if (canvas.getGraphics() == null)	// if we have no graphics
			return 8 * gecko.getMaxLength(nameType); // improvise!

		if (maxLengthString == null || maxLengthString.length() != gecko.getMaxLength(nameType)){
			maxLengthString = GenomePainting.buildMaxLengthString(gecko.getMaxLength(nameType));
		}
		return GenomePainting.getGeneWidth(canvas.getGraphics(), maxLengthString, parent.getGeneElementHeight());
	}

	@Override
	public int getGBHeight() {
		return 2 * vgap + parent.getGeneElementHeight();
	}

    @Override
    public void setNameType(GenomePainting.NameType nameType) {
        this.nameType = nameType;
        this.adjustSize();
        canvas.repaint();
    }

    private int getCanvasWidth() {
		return 2 * borderSpace +
				genome.getTotalGeneNumber() * getGeneWidth() + 
				2 * genome.getChromosomes().size() * getChromosomeEndingWidth();
	}

    @Override
    public void highlightCluster() {
        highlightCluster(-1, -1, -1, null);
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
			GenomePainting.paintGenomeWithCluster(g, genome, flipped, highlights, nameType, highlightColor, borderSpace, vgap, getGeneElementWidth(), parent.getGeneElementHeight(), hgap, vgap);
		}
	}
	
	private class PaintingGenomeBrowserListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			super.componentResized(e);
			canvas.setPreferredSize(new Dimension (getCanvasWidth(), getGBHeight()));
			PaintingGenomeBrowser.this.revalidate();
			PaintingGenomeBrowser.this.repaint();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    PaintingGenomeBrowser.this.scrollToPosition(chromosomeIndex, geneIndex);
                }
            });
		}
	}

    /**
     * Mouse listener for the GenomeBrowser
     */
	private class PaintingGenomeBrowserMouseListener extends MouseInputAdapter {
		
		private int clickXPos;
		
		@Override
		public void mousePressed(MouseEvent e) {
			this.clickXPos = e.getX();
		}


        @Override
        public void mouseClicked(MouseEvent event)
        {
            if (event.getClickCount() == 2) {
                Gene gene = getGeneAtPosition(event.getX());
                if (event.isShiftDown()){
                    PaintingGenomeBrowser.this.flip();
                    PaintingGenomeBrowser.this.getHorizontalScrollBar().setValue(getMaximumValue()- borderSpace - getScrollValue());
                    PaintingGenomeBrowser.this.repaint();
                    parent.fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
                }
                if (!event.isShiftDown() && gene != null){
                    parent.centerCurrentClusterAt(gene.getGeneFamily());
                }
            }
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
