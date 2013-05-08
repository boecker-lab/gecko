package gecko2.gui;

import gecko2.algorithm.Genome;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public abstract class AbstractGenomeBrowser extends JScrollPane implements Adjustable{
	public abstract int getScrollValue();
	public abstract int getMaximumValue();
	public abstract void adjustScrollPosition(int value);
	public abstract void scrollToPosition(int chromosomeIndex, int geneIndex);
	public abstract boolean isFlipped();
	public abstract void flip();
	public abstract void highlightCluster(int chr, int start, int stop, Color highlightColor);
	public abstract void clearHighlight();
	public abstract int getScrollWidth();
	public abstract Genome getGenome();
	public abstract int getGeneWidth();
	public abstract int getGBHeight();
}
