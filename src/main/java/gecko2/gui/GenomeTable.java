package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Gene;
import gecko2.algorithm.Genome;
import gecko2.util.PrintUtils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * This class is the merged result of the classes GeneElement.java and parts of the GenomeBrowser.java 
 * of the old gecko2 version for reducing the memory use.
 * 
 * The class generates a JTable (just a single row) which cells contain
 * symbols for the genes also for beginning and ending of a chromosome. The result is 
 * a table with a full genome.
 * 
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.01
 *
 */
public class GenomeTable extends JTable {
	
	/**
	 * Random generated serial version UID
	 */
	private static final long serialVersionUID = -3581998622623005184L;
	
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
	
	/**
	 * The variable stores the status of the genome whether it is flipped or not.
	 */
	private boolean flipped;
	
	/**
	 * Background color for flipped genomes
	 */
	private final static Color BG_COLOR_FLIPPED = Color.ORANGE;
	
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
	 * The index of the genome in the genome array in GeckoInstance
	 */
	private int genomeNumber;
	
	private GenomeTableModel dataModel;
	
	private HashSet<Integer> highlighted;
	private HashSet<Integer> greyed;
	
	/**
	 * The constructor sets the global variables gecko. It also computes 
	 * and sets the variables elemHeight, elemWidth. Also it fills the table model based in the genome
	 *  and set up the table properties.
	 * 
	 * 	
	 * @param g the genome which we want to display.
	 * @param flipped information whether the genome is flipped or not
	 */
	public GenomeTable(Genome g, boolean flipped) {
		
		this.gecko = GeckoInstance.getInstance();
		this.setBackground(Color.WHITE);
		this.elemHeight = this.computeElemHeight();
		this.elemWidth = this.computeElemWidth();
		this.flipped = flipped;
		this.genomeNumber = findGenomeNumber(g);
		System.out.println(genomeNumber);
		this.getTableHeader().setDefaultRenderer(new GeneElementRenderer());
		this.setDefaultRenderer(Object.class, new GeneElementRenderer());
		this.setEnabled(false);
		this.setDragEnabled(false);
		this.setRowHeight(30);
		
		this.setShowHorizontalLines(false);
		this.setShowVerticalLines(false);
		this.setShowGrid(false);
		
		
		
		dataModel = new GenomeTableModel(calculateArraySize(g));
		
		int count = 0;
		int[] ids = new int[2];
		
		for (int i = 0; i < g.getChromosomes().size(); i++) {
			
			ids[0] = -1;
			dataModel.setValueAt(count, createElemPic(ids));
			count++;
			
			for (int j = 0; j < g.getChromosomes().get(i).getGenes().size(); j++) {
				
				ids[0] = i;
				ids[1] = j;
				dataModel.setValueAt(count, createElemPic(ids));
				count++;
				
			}
			
			ids[0] = -2;
			dataModel.setValueAt(count, createElemPic(ids));
			count++;
			System.gc();
		}
		
		
		this.setModel(dataModel);
		
		// set prefered size of the columns
		TableColumnModel columnModel = this.getColumnModel();
	    int numberOfCols = columnModel.getColumnCount();
	    
	    for (int i = 0; i < numberOfCols; i++) {
	    	
	    	columnModel.getColumn(i).setPreferredWidth(this.elemWidth + 20);
	    }
	    
	}
	
	private int findGenomeNumber(Genome g) {
		
		int gN = -1;
		
		for (int i = 0; i < this.gecko.getGenomes().length; i++) {
			
			if (this.gecko.getGenomes()[i].equals(g)) {
			
				gN = i;
			}
		}
		
		return gN;
	}

	private int calculateArraySize(Genome g) {
		
		int size = 0;
		
		for (int i = 0; i < g.getChromosomes().size(); i++) {
			
			size = size + g.getChromosomes().get(i).getGenes().size();
		}
		
		// for begin and end symbol
		size = size + (2 * g.getChromosomes().size());
		
		return size;
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
	 * This method checks whether we have a genome without a chromosome and without genes on the 
	 * first chromosome. (used by GenomeBrowser)
	 * 
	 * @return true if the genome is empty else false
	 */
	public boolean isEmptyGenome() {
		
		if (this.gecko.getGenomes()[this.genomeNumber].getChromosomes().size() == 0 && 
			this.gecko.getGenomes()[this.genomeNumber].getChromosomes().get(0).getGenes().size() == 0) {
			
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
	private int geneToColIndex(int chromosome, int firstGene) {
		
		int indexCounter = 0;
	
		// add sizes of all previous chromosomes
		// +2 for the begin and end symbol
		for (int i = 0; i < chromosome - 1; i++) {
			
			indexCounter = indexCounter + this.gecko.getGenomes()[genomeNumber].getChromosomes().get(i).getGenes().size() + 2;
		}
		
		// for the begin symbol of the current chromosome
		indexCounter++;
		
		// adds the distance of the current gene 
		// +1 for index correction
		indexCounter = indexCounter + firstGene + 1;
		
		return indexCounter;
	}
	
	private int[] colIndexToGene(int colIndex) {
		
		int[] chromGene = new int[2];
		int chrom = -1;
		int gene = -1;
		int currentSize = 0;
		boolean stop = false;
		
		for (int i = 0; i < this.gecko.getGenomes()[genomeNumber].getChromosomes().size() && stop == false; i++) {
			
			if (currentSize + this.gecko.getGenomes()[genomeNumber].getChromosomes().get(i).getGenes().size() + 2 < colIndex) {
				
				// +2 for begin and end symbol
				currentSize = currentSize + this.gecko.getGenomes()[genomeNumber].getChromosomes().get(i).getGenes().size() + 2;
			}
			else {
				
				// -1 because current chromsome is to long
				chrom = i;
				stop = true;
				boolean stop2 = false;
				
				// begin symbol
				currentSize++;
				
				for (int j = 0; j < this.gecko.getGenomes()[genomeNumber].getChromosomes().get(chrom).getGenes().size() && stop2 == false; j++) {
					
					if (currentSize + 1 == colIndex) {
						
						gene = j;
					}
					else {
						
						currentSize = currentSize + 1;
					}
				}
			}
		}
		
		chromGene[0] = chrom;
		chromGene[1] = gene;
		
		return chromGene;
	}
	
	private Icon createElemPic(int[] id) {
		
		return this.createElemPic(id, false, false);
	}
	
	private Icon createElemPic(int[] id, boolean highlighted, boolean greyedOut) {
		
		BufferedImage elemPic = new BufferedImage(elemWidth + 20, elemHeight, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g = elemPic.createGraphics();
		//System.out.println(g);
		
		g.setColor(Color.WHITE);
		
		if (this.flipped == true) {
			
			g.setColor(BG_COLOR_FLIPPED);
		}
		
		if (highlighted == true) {
			
			g.setColor(this.highlightColor);
		}
		
		g.fillRect(0, 0, elemWidth + 20, elemHeight);
		
		g.setColor(Color.BLACK);
		// draw the left ending of the chromosome
		if (id[0] == -1) {
	//		System.out.println("begin");
			g.fillRect(2 + elemHeight / 2, 2, 2, elemHeight); 
			g.fillRect(this.gecko.getGeneElementHight() / 2 + 4, this.gecko.getGeneElementHight() / 2 + 1, this.gecko.getGeneElementHight() / 2 - 2, 2);
		}
						
		// draw the right ending of the chromosome
		if (id[0] == -2) {
	//		System.out.println("end");
			g.fillRect(2 + this.gecko.getGeneElementHight() / 2, 2, 2, this.gecko.getGeneElementHight()); 
			g.fillRect(4, this.gecko.getGeneElementHight() / 2 + 1, this.gecko.getGeneElementHight() / 2 - 2, 2);
		}
		
		// Draw the element
		if (id[0] >= 0) {
			Gene gene = gecko.getGenomes()[genomeNumber].getChromosomes().get(id[0]).getGenes().get(id[1]);
			//System.out.println("GeneID: " + gene.getId());
			
			int gc = gene.getId();
			
			if (gc < 0) {
				
				gc = gc * -1;
			}
			
			Color geneColor = this.gecko.getColormap().get(gc);
			
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
		 		
		 		if (greyedOut == true) {
		 			//System.out.println("GeneColor: " + geneColor);
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
				
				g.fillRect(3, Y_OFFSET + 0, this.elemWidth, this.elemHeight);
				int xPoints[] = {3 + this.elemWidth,
						3 + this.elemWidth + ((int) Math.ceil(this.elemHeight / 2.0)),
						3 + this.elemWidth};
				int yPoints[] = {Y_OFFSET + 0, Y_OFFSET + this.elemHeight / 2, Y_OFFSET + this.elemHeight};
				g.fillPolygon(xPoints, yPoints, 3);
			} 
			else {
				
				if (orientation == ORIENTATION_BACKWARDS) {
					
					int triangleHeight = (int) Math.ceil(this.elemHeight / 2.0);
					g.fillRect(3 + triangleHeight, Y_OFFSET + 0, this.elemWidth, this.elemHeight);
					int xPoints[] = {3 + triangleHeight, 3, 3 + triangleHeight};
					int yPoints[] = {Y_OFFSET + 0, (int) Math.ceil(Y_OFFSET + this.elemHeight / 2.0), Y_OFFSET + this.elemHeight};
					g.fillPolygon(xPoints, yPoints, 3);
					fontoffset = triangleHeight;
				} 
				else {
					
					g.fillRect(8, Y_OFFSET + 0, this.elemWidth, this.elemHeight);
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
				
				g.setFont(new Font("Monospaced", Font.PLAIN, Math.round((float) this.elemHeight - ((float) this.elemHeight / 2.4F))));
				
				/* Draw only the first index of the mapped String array */
				if (this.gecko.getGenLabelMap().get(Math.abs(gene.getId()))[0].length() < gecko.getMaxIdLength()) {
					
					g.drawString(this.gecko.getGenLabelMap().get(Math.abs(gene.getId()))[0] , ((8 * this.gecko.getMaxIdLength() + 3) / 2) - ((8 * this.gecko.getGenLabelMap().get(Math.abs(gene.getId()))[0].length()) / 2) + 5, this.elemHeight / 2 + Y_OFFSET + (int) Math.round(g.getFont().getSize() / 2));
				}
				else {
					
					g.drawString(this.gecko.getGenLabelMap().get(Math.abs(gene.getId()))[0] , 5 + fontoffset, this.elemHeight / 2 + Y_OFFSET + (int) Math.round(g.getFont().getSize() / 2));
				}
		
		
				
		
			}
		
		
		
		}
		/*File test2 = new File("/home/po84quv/test152");
		try {
			ImageIO.write(elemPic, "jpg", test2);
		} catch (IOException e) {
			System.out.println("ladida");
			e.printStackTrace();
		}*/
		//Image test2 = this.createImage(elemWidth, elemHeight);
		Image test2 = Toolkit.getDefaultToolkit().createImage(elemPic.getSource());
		ImageIcon test3 = new ImageIcon(test2);
		Icon test4 = (Icon) test3;
		
		return test4;
	}
	
	/**
	 * The method creates a data structure which tell the paint method which gene shall be painted
	 * grey.
	 * 
	 * @param chromosome the chromosome where to paint grey genes
	 * @param start the first gene which shall be painted grey
	 * @param stop the last gene which shall be painted grey
	 */
	protected void setRangeToGrey(int chromosome, int start, int stop, boolean grey) {
		
		if (this.greyed == null) {
			
			this.greyed = new HashSet<Integer>();
		}
		
		int colIndex = geneToColIndex(chromosome, start);
		
		if (grey == false) {
			
			for (int i = colIndex; i < colIndex + (stop - start) - 1; i++) {
				
				int[] cg = {chromosome, i};
				
				this.dataModel.setValueAt(colIndex, createElemPic(cg, false, false));
				this.greyed.remove(colIndex);
			}
		}
		else {
		
			for (int i = colIndex; i < colIndex + (stop - start) - 1; i++) {
			
				int[] cg = {chromosome, i};
				System.out.println("colindex: " + colIndex + " cg[0]: " + cg[0] + " cg[1]: " + cg[1]);
				this.dataModel.setValueAt(colIndex, createElemPic(cg, false, true));
				this.greyed.add(colIndex);
			}
		}
		
		this.dataModel.fireTableDataChanged();
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
		
		if (this.highlighted == null) {
			
			this.highlighted = new HashSet<Integer>();
		}
		
		int colIndex = geneToColIndex(chromosome, start);
		
		if (highlight == null) {
			
			for (int i = colIndex; i < colIndex + (stop - start) - 1; i++) {
				
				int[] cg = {chromosome, i};
				
				if (this.greyed.contains(colIndex)) {
					
					this.dataModel.setValueAt(colIndex, createElemPic(cg, false, true));
				}
				else {
					
					this.dataModel.setValueAt(colIndex, createElemPic(cg, false, false));
				}
				
				this.highlighted.remove(colIndex);
			}
		}
		else {
		
			this.highlightColor = highlight;
	
			for (int i = colIndex; i < colIndex + (stop - start) - 1; i++) {
			//System.out.println(i + "____" + chromosome);
				int[] cg = {chromosome, i};
			
				if (this.greyed != null && this.greyed.contains(colIndex)) {
				
					this.dataModel.setValueAt(colIndex, createElemPic(cg, true, true));
				}
				else {
				
					this.dataModel.setValueAt(colIndex, createElemPic(cg, true, false));
				}
			
				this.highlighted.add(colIndex);
			}
		}
		
		this.dataModel.fireTableDataChanged();
	}
	
	public Gene getGeneOnPos(int xPos) {
		
		int tableCol = this.columnAtPoint(new Point(xPos, 5));
		int counter = -1;
		Gene g = null;
		
		if (tableCol != -1) {
			
			for (int i = 0; i < this.gecko.getGenomes()[genomeNumber].getChromosomes().size(); i++) {
				
				counter++;
				
				for (int j = 0; j < this.gecko.getGenomes()[genomeNumber].getChromosomes().get(i).getGenes().size(); j++) {
					
					counter++;
					
					if (counter == tableCol) {
						
						g = this.gecko.getGenomes()[genomeNumber].getChromosomes().get(i).getGenes().get(j);
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
	
	public Genome getGenome() {
		
		return this.gecko.getGenomes()[genomeNumber];
	}
	
	public int getElemWidth() {
		
		return this.elemWidth;
	}
	
	public boolean isHighlighted(int pos) {
		
		int col = this.columnAtPoint(new Point(pos, 5));
		
		if (this.highlighted.contains(col)) {
			
			return true;
		}
		else {
			
			return false;
		}	
	}
	
	protected class GeneElementRenderer extends JLabel implements TableCellRenderer {

		/**
		 * Random generated serial version UID
		 */
		private static final long serialVersionUID = 5354665619670618252L;
		
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
			
			Icon current = (Icon) value;
			int[] chromGene = colIndexToGene(col);
			// set background and tooltip for the cell
			this.setBackground(Color.WHITE);
			
			if (chromGene[1] > -1) {
				
				// setup tooltip text
				Gene gene = GenomeTable.this.gecko.getGenomes()[genomeNumber].getChromosomes().get(chromGene[0]).getGenes().get(chromGene[1]);
				//Color geneColor = GenomePanel2.this.gecko.getColormap().get(gene.getId());
				
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
			}
			
			this.setIcon(current);
			
			return this;
		}
	}
	
	class GenomeTableModel extends AbstractTableModel {

		/**
		 * Random generated serial version UID
		 */
		private static final long serialVersionUID = -3646735967098490559L;
		
		private Icon[] data1;
		
		
		GenomeTableModel(Icon[] content) {
			
			super();
			this.data1 = content;
			this.fireTableDataChanged();
		}
		
		
		GenomeTableModel(int size) {
			
			super();
			this.data1 = new Icon[size];
		}
		
		
		GenomeTableModel() {
			
			super();
		}
		
		@Override
		public int getColumnCount() {
			
			return this.data1.length;
		}

		@Override
		public int getRowCount() {
			
			return 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			
			return this.data1[columnIndex];
		}		
		
		public void setValueAt(int index, Icon newContentCell) {
			
			if (this.data1.length >= index - 1) {
			
				this.data1[index] = newContentCell;
			}
		}
	
	}
}
