package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.Gene;
import gecko2.algorithm.Genome;
import gecko2.util.PrintUtils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.BitSet;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class GenomePanel2 extends JPanel {

	// some constants for the cell renderer
	
	/**
	 * Default color for the highlighted cluster
	 */
	final Color COLOR_HIGHLIGHT_DEFAULT = new Color(120,120,254);
	
	/**
	 * Default color for the reference cluster/sequence
	 */
	final Color COLOR_HIGHLIGHT_REFCLUST = Color.RED;
	
	/**
	 * Constant for forward directed genes.
	 */
	public static final short ORIENTATION_FORWARD = -1;
	
	/**
	 * Constant for backward directed genes.
	 */
	public static final short ORIENTATION_BACKWARDS = 1;
	
	/**
	 * Constant for non directed genes.
	 */
	public static final short ORIENTATION_NONE = 0;
	
	/**
	 * Constant for a left border
	 */
	public static final short BORDER_LEFT = 1;
	
	/**
	 * Constant for no border
	 */
	public static final short BORDER_NO = 2;
	
	/**
	 * Constant for a right border
	 */
	public static final short BORDER_RIGHT = 3;

	/**
	 * Constant for a offset in y direction
	 */
	private static final int Y_OFFSET = 2;

	
	// gloabal constants and variables for the panel
	
	/**
	 * Background color for flipped genomes
	 */
	private final static Color BG_COLOR_FLIPPED = Color.ORANGE;
	
	/**
	 * Random generated serial Version UID
	 */
	private static final long serialVersionUID = 962672499227894828L;

	/**
	 * The genome we want to paint on the panel
	 */
	private Genome genome;
	
	/**
	 * The height of the genes which will be painted on the panel
	 */
	private int elemHeight;
	
	/**
	 * The width of the genes we paint on the panel
	 */
	private int elemWidth;

	/**
	 * The current gecko instance.
	 */
	private GeckoInstance gecko;
	
	/**
	 * In this color we highlight a range of genes.
	 * Default is white (not highlighted)
	 */
	private Color highlightColor = Color.WHITE;

	/**
	 * BitSet with the length of the number of columns in the table which contains a set bit for every
	 * cell which is greyed out.
	 */
	private BitSet greyedOut = new BitSet();
	
	/**
	 * BitSet with the length of the number of columns in the table which contains a set bit for every
	 * cell which is highlighted.
	 */
	private BitSet highlighted = new BitSet();
	
	/**
	 * In this table we create the gene elements.
	 */
	private JTable table;
	
	/**
	 * The constructor sets the global variables genome, gecko and background color. It also computes 
	 * and sets the variables elemHeight, elemWidth ns table.
	 * 	
	 * @param g the genome which we want to display.
	 */
	public GenomePanel2(Genome g) {
		
		this.gecko = GeckoInstance.getInstance();
		this.genome = g;
		//this.adjustSize();
		this.setBackground(Color.WHITE);
		this.elemHeight = this.computeElemHeight();
		this.elemWidth = this.computeElemWidth();
		table = new JTable(this.fillTable(), null);
		table.setDefaultRenderer(Object.class, new GeneElementRenderer());
		table.setBackground(Color.WHITE);
		
		repaint();
	}
	
	/**
	 * The method creates a data vector for the table and init the highlighted and greyedOut 
	 * BitSets with just false.
	 * 
	 * @return returns a Vector which contains the table data
	 */
	private Vector<Object> fillTable() {
		
		short lEnd = 1;
		short rEnd = 2;
		Vector<Object> result = new Vector<Object>();
		int counter = 0;
		
		for (Chromosome chrom : this.genome.getChromosomes()) {
			
			result.add(lEnd);
			this.greyedOut.set(counter, false);
			this.highlighted.set(counter, false);
			counter++;
			
			for (Gene g : chrom.getGenes()) {
				
				result.add(g);
				this.greyedOut.set(counter, false);
				this.highlighted.set(counter, false);
				counter++;
			}
			
			result.add(rEnd);
			this.greyedOut.set(counter, false);
			this.highlighted.set(counter, false);
			counter++;
		}
		
		return result;
	}
	
	/**
	 * The method checks whether a cell on a given position is highlighted or not.
	 * 
	 * @param clickXPos the x coordinate of the point to look whether the cell there is 
	 * highlighted
	 * @return true if the cell is highlighted else false
	 */
	public boolean isHighlighted(int clickXPos) {
		
		if (this.table.columnAtPoint(new Point(clickXPos, 5)) != -1) {
			
			return true;
		}
			
		return false;
	}
	
	/**
	 * The method returns the current highlight color. If the nothing is highlighted it returns white.
	 * 
	 * @return Current highlight color
	 */
	public Color getHighlightColor() {
		
		return this.highlightColor;
	}
	
	/**
	 * The method returns the genome which is painted on the panel.
	 * 
	 * @return the currently used panel
	 */
	public Genome getGenome() {
		
		return this.genome;
	}
		
	/**
	 * The method returns the element height from the gecko instance
	 * 
	 * @return element height
	 */
	private int computeElemHeight() {
		
		return gecko.getGeneElementHight();
	}
	
	/**
	 * The method calculates the universal width for all genes based on the maximal id length of all
	 * genes in every genome of the gecko instance.
	 * 
	 * @return the width of the painted genes (just the rectangle width)
	 */
	private int computeElemWidth() {
		
		return 8 * gecko.getMaxIdLength() + 3; 
	}
	
	/**
	 * The method returns the current width of the gene elements.
	 * 
	 * @return the width of the gene elements
	 */
	public int getElemWidth() {
		
		return this.elemWidth;
	}
	
	/**
	 * This method checks whether we have a genome without a chromosome and without genes on the 
	 * first chromosome. (used by GenomeBrowser)
	 * 
	 * @return true if the genome is empty else false
	 */
	public boolean isEmptyGenome() {
		
		if (this.genome.getChromosomes().size() == 0 && this.genome.getChromosomes().get(0).getGenes().size() == 0) {
			
			return true;
		}
		else{
			
			return false;
		}
			
	}
	
	/**
	 * This method computes the column where the gene given via the parameters is.
	 * 
	 * @param chromosome 
	 * @param firstGene
	 * @return
	 */
	private int computeColIndex(int chromosome, int firstGene) {
		
		int indexCounter = 0;
		
		// add the beginning and ending symbols
		// chromosomes start also with 0 so the current nummer reflects all before
		// + 1 for the current one.
		indexCounter = (2 * chromosome) + 1;
		
		for (int i = 0; i < chromosome - 1; i++) {
			
			indexCounter = indexCounter + this.genome.getChromosomes().get(i).getGenes().size();
		}
		
		return indexCounter;
	}
	
	/**
	 * The method creates a data structure which tell the paint method which gene shall be painted
	 * grey.
	 * 
	 * @param chromosome the chromosome where to paint grey genes
	 * @param start the first gene which shall be painted grey
	 * @param stop the last gene which shall be painted grey
	 */
	protected void setRangeToGrey(int chromosome, int start, int stop) {
		
		int colIndex = computeColIndex(chromosome, start);
		
		for (int i = colIndex; i < colIndex + (stop - start); i++) {
			
			greyedOut.set(i);
		}
		
		repaint();
	}
	
	/**
	 * The method creates a data structure which tells the paint method which genes shall be
	 * highlighted.
	 * 
	 * @param chromosome the chromosome with the genes we want to highlight
	 * @param start the first gene of the range we want to highlight
	 * @param stop the last gene of the range we want to highlight
	 * @param highlight the color for highlighting
	 */
	protected void setHighlightRange(int chromosome, int start, int stop, Color highlight) {
		
		this.highlightColor = highlight;
		int colIndex = computeColIndex(chromosome, start);
		
		for (int i = colIndex; i < colIndex + (stop - start); i++) {
			
			highlighted.set(i);
		}
		
		this.repaint();
		
	}
	
	public Gene getGeneOnPos(int xPos) {
		
		int tableCol = this.table.columnAtPoint(new Point(xPos, 5));
		int counter = -1;
		Gene g = null;
		
		if (tableCol != -1) {
			
			for (int i = 0; i < this.genome.getChromosomes().size(); i++) {
				
				counter++;
				
				for (int j = 0; j < this.genome.getChromosomes().get(i).getGenes().size(); j++) {
					
					counter++;
					
					if (counter == tableCol) {
						
						g = this.genome.getChromosomes().get(i).getGenes().get(j);
					}
				}
				
				counter++;
			}
		}
		else {
			
			return null;
		}
		
		return g;
	}
	
	
	class GeneElementRenderer extends JLabel implements TableCellRenderer {

		/**
		 * Random generated serial version UID
		 */
		private static final long serialVersionUID = 5354665619670618252L;

		public GeneElementRenderer() {
			//super();
            this.setOpaque(true);
            //this.setHorizontalAlignment(JPanel.CENTER_ALIGNMENT);
        }
		
		
		@Override
		/**
		 * Method for creation of the cell content. Row is always 0 because the table has only one.
		 * 
		 * @param table the JTable to modify
		 * @param value the value we want to insert here the Gene object or 1 for chromosome
		 * beginning and 2 ending
		 * @param isSelected true if the cell is selected else false
		 * @param hasFocus true if the cell has the focus
		 * @param row the row where to insert the cell
		 * @param col the column where to insert the cell
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			
			Graphics g = this.getGraphics();
			
			this.setBackground(Color.WHITE);
			
			// draw the left ending of the chromosome
			if (value instanceof Short && ((Short) value).shortValue() == 1) {
				
				g.fillRect(2 + gecko.getGeneElementHight() / 2, 2, 2, gecko.getGeneElementHight()); 
				g.fillRect(gecko.getGeneElementHight() / 2 + 4, gecko.getGeneElementHight() / 2 + 1, gecko.getGeneElementHight() / 2 - 2, 2);
			}
				
			// draw the right ending of the chromosome
			if (value instanceof Short && ((Short) value).shortValue() == 2) {
				
				g.fillRect(2 + gecko.getGeneElementHight() / 2, 2, 2, gecko.getGeneElementHight()); 
				g.fillRect(4, gecko.getGeneElementHight() / 2 + 1, gecko.getGeneElementHight() / 2 - 2, 2);
			}				
			
			// draw the gene element
			if (value instanceof Gene) {
				
				// set the background of the cell dependent on the highlighted BitSet
				if (highlighted.get(col)) {
					
					this.setBackground(highlightColor);
				}
				
				// setup tooltip test
				Gene gene = (Gene) value;
				Color geneColor = GenomePanel2.this.gecko.getColormap().get(gene.getId());
				
				if (gene.getAnnotation() == null) {
				
					if (gene.getName() != null && ! gene.getName().equals("----")) {
					
						this.setToolTipText(gene.getName());
					} 
					else {
				
						this.setToolTipText("[no annotation available]");
					}
				}
				else {
				
					if (gene.getName() == null) {
					
						this.setToolTipText("---- -" + gene.getAnnotation());
					}
					else {
						
						this.setToolTipText(gene.getName() + " - " + gene.getAnnotation());
					}
				}
				
				// set orientation
				int orientation = 0;
				if (gene.getId() < 0) {
					
					orientation = 1;
				}
				else {
					
					orientation = -1;
				}
				
				
				
				Color c;
				
				if (gene.isUnknown()) {
					
					c = Color.GRAY;
				}
			 	else {
			 		
			 		
			 		
			 		if (GenomePanel2.this.greyedOut.get(col)) {
			 				
			 			int gv = (int) Math.floor((geneColor.getBlue() + geneColor.getRed() + geneColor.getGreen()) / 3);
			 			c = new Color(gv, gv, gv);
			 		} 
			 		else {
			 			
			 			c = geneColor;
			 		}
			 	}
				
				g.setColor(c);
				int fontoffset = 0;
				if (orientation == ORIENTATION_FORWARD) {
					
					g.fillRect(3, Y_OFFSET+0, elemWidth, elemHeight);
					int xPoints[] = {3 + elemWidth,
							3 + elemWidth + ((int) Math.ceil(elemHeight / 2.0)),
							3 + elemWidth};
					int yPoints[] = {Y_OFFSET + 0, Y_OFFSET + elemHeight / 2, Y_OFFSET + elemHeight};
					g.fillPolygon(xPoints, yPoints, 3);
				} 
				else {
					
					if (orientation == ORIENTATION_BACKWARDS) {
						
						int triangleHeight = (int) Math.ceil(elemHeight / 2.0);
						g.fillRect(3 + triangleHeight, Y_OFFSET + 0, elemWidth, elemHeight);
						int xPoints[] = {3 + triangleHeight, 3, 3 + triangleHeight};
						int yPoints[] = {Y_OFFSET + 0, (int) Math.ceil(Y_OFFSET + elemHeight / 2.0), Y_OFFSET + elemHeight};
						g.fillPolygon(xPoints, yPoints, 3);
						fontoffset = triangleHeight;
					} 
					else {
						
						g.fillRect(8, Y_OFFSET+0, elemWidth, elemHeight);
						fontoffset = 5;
					}
				}
				
				if (!gene.isUnknown()) {
					
					if (geneColor == null) {
						
						PrintUtils.printDebug("COLOR ERROR");
						geneColor = Color.blue;
					} 
					
					if (geneColor.getRed() + geneColor.getGreen() + geneColor.getBlue() > 450) {
						
						g.setColor(Color.BLACK);
					} 
					else {
						
						g.setColor(Color.WHITE);
					}
					
					g.setFont(new Font("Monospaced", Font.PLAIN, Math.round((float) elemHeight - ((float) elemHeight / 2.4F))));
					
					/* Draw only the first index of the mapped String array */
					if (gecko.getGenLabelMap().get(Math.abs(gene.getId()))[0].length() < gecko.getMaxIdLength()) {
						
						g.drawString(gecko.getGenLabelMap().get(Math.abs(gene.getId()))[0] , ((8 * gecko.getMaxIdLength() + 3) / 2) - ((8 * gecko.getGenLabelMap().get(Math.abs(gene.getId()))[0].length()) / 2) + 5, elemHeight / 2 + Y_OFFSET + (int) Math.round(g.getFont().getSize() / 2));
					}
					else {
						
						g.drawString(gecko.getGenLabelMap().get(Math.abs(gene.getId()))[0] , 5 + fontoffset, elemHeight / 2 + Y_OFFSET + (int) Math.round(g.getFont().getSize() / 2));
					}
				}	
			}
			
			//this.revalidate();
			//this.repaint();
			
			return this;
		}
		
	}

}
