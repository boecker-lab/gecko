package gecko2.gui;

import gecko2.datastructures.Genome;

import javax.swing.*;
import java.awt.*;

abstract class AbstractGenomeBrowser extends JScrollPane implements Adjustable{
	public abstract void adjustScrollPosition(int value);
	public abstract void scrollToPosition(int chromosomeIndex, int geneIndex);
	public abstract boolean isFlipped();
	public abstract void flip();


    /**
     * The whole genome browser is understated, no cluster contained
     */
    public abstract void highlightCluster();

    /**
     * Highlights the given interval in the visualisation, the rest is understated
     * @param chr
     * @param start
     * @param stop
     * @param highlightColor
     */
	public abstract void highlightCluster(int chr, int start, int stop, Color highlightColor);
	public abstract void clearHighlight();
	public abstract Genome getGenome();
	public abstract int getGBHeight();
    public abstract void setNameType(GenomePainting.NameType nameType);
}
