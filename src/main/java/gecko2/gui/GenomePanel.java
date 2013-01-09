package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.Gene;
import gecko2.algorithm.Genome;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

public class GenomePanel extends JPanel implements Adjustable, MouseInputListener{

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
	
	private int spaceWidth = 5;
	
	/**
	 * In this ArrayList we map the tool tip (annotation, name, id) with the position of each gene 
	 * on the chromosome (index of the array list). The size of the integer array object in the 
	 * HashMap is always four.
	 */
	private ArrayList<HashMap<int[], String>> toolTipList = new ArrayList<HashMap<int[], String>>();
	
	/**
	 * In this HashMap we map the area of each chromosome onto the chromosome id. 
	 */
	private HashMap<int[], Integer> chromAreas = new HashMap<int[], Integer>();
	
	/**
	 * The current gecko instance.
	 */
	private GeckoInstance gecko;
	
	private HashMap<Integer, HashSet<Integer>> greyedOut = new HashMap<Integer, HashSet<Integer>>();
	
	private HashMap<Integer, int[]> highlighted;
	
	/**
	 * In this color we highlight a range of genes.
	 * Default is white (not highlighted)
	 */
	private Color highlightColor = Color.WHITE;
	
	/**
	 * 3 dimensional boolean array for getting the correct gene on every mouse position.
	 * first index is the x coordinate, second the chromosome and third the gene
	 */
	private int[][] genePositions;
	
	/**
	 * The constructor sets the genome and the background color for the panel also the gecko instance and 
	 * the width and height of the genes. Launches the adjustSize and updatePanel methods.
	 * 
	 * @param g the genome we want to draw on the panel
	 */
	public GenomePanel(Genome g) {
		
		this.gecko = GeckoInstance.getInstance();
		this.genome = g;
		this.adjustSize();
		this.setBackground(Color.WHITE);
		this.elemHeight = this.computeElemHeight();
		this.elemWidth = this.computeElemWidth();
		this.genGenePositionsSize();
		updatePanel();
	}
	
	private void genGenePositionsSize() {
		
		
		int picWidth = 0;
		
		for (Chromosome chrom : this.genome.getChromosomes()) {
			
			picWidth = picWidth + (chrom.getGenes().size() * (elemWidth + 8)) + ((chrom.getGenes().size() - 1) * spaceWidth);
			picWidth = picWidth + (2 * elemWidth) + spaceWidth;
		}
		
		// behind the last genome isn't space
		picWidth = picWidth - spaceWidth;
		
		this.genePositions = new int[picWidth][2];
	}
	
	public boolean isHighlighted() {
		
		if (this.highlighted.keySet() != null) {
			
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
	
	public int getNumberOfGenes() {
		
		int numbOfGenes = 0;
		
		for (Chromosome chrom : this.genome.getChromosomes()) {
			
			numbOfGenes = numbOfGenes + chrom.getGenes().size();
		}
		
		return numbOfGenes;
	}
	
	private void updatePanel() {
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
	 * The method creates a data structure which tell the paint method which gene shall be painted
	 * grey.
	 * 
	 * @param chromosome the chromosome where to paint grey genes
	 * @param start the first gene which shall be painted grey
	 * @param stop the last gene which shall be painted grey
	 */
	protected void setRangeToGrey(int chromosome, int start, int stop) {
		
		HashSet<Integer> greyed;
		
		if (this.greyedOut.get(chromosome) == null) {
			
			greyed = new HashSet<Integer>();
		}
		else {
			
			greyed = this.greyedOut.get(chromosome);
		}
		
		for (int i = start; i <= stop; i++) {
			
			greyed.add(i);
		}
		
		this.greyedOut.put(chromosome, greyed);
		
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
		
		this.highlighted = new HashMap<Integer, int[]>();
		this.highlightColor = highlight;
		
		int[] range = new int[2];
		range[0] = start;
		range[1] = stop;
		
		this.highlighted.put(chromosome, range);
		
		this.repaint();
		
	}
	
	/**
	 * The overwritten paint method paints the complete genome on the panel.
	 * It also generates 
	 */
	public void paint(Graphics g) {
		
		int[] nullChrom = new int[2];
		nullChrom[0] = -1;
		nullChrom[1] = -1;
		
		int count = 0;
		
		int x = 0;
		int y = 0;
		
		// create a white background
		g.setColor(Color.BLACK);
		
		for (int i = 0; i < this.genome.getChromosomes().size(); i++) {
			
			int[] usedChrom = new int[2];
			usedChrom[0] = i;
			boolean colored = false;
			
			if (this.highlighted != null && this.highlighted.containsKey(i)) {
				
				colored = true;
			}
			
			// move the pen 30 pixels right
			//x = x + 30;
			g.fillRect(x, y, 2, elemHeight);
			g.fillRect(x + 2, y + 8, elemWidth - 2, 2);
			
			for (int k = count; k < count + elemWidth + spaceWidth; k++) {
				
				this.genePositions[count] = nullChrom;
			}
			count = count + elemWidth + spaceWidth;
			
			// move the pen again
			// 2 + 16 length of the beginning symbol
			// 5 space between the beginning symbol and the first gene
			x = x + elemWidth + spaceWidth;
					
			// loop over the genes on the chromosome
			for (int z = 0; z <  this.genome.getChromosomes().get(i).getGenes().size(); z++) {
				
				usedChrom[1] = z;
				for (int k = count; k < count + elemWidth + 8 + spaceWidth; k++) {
					
					this.genePositions[count] = usedChrom;
				}
				
				count = count + elemWidth + 8;
				
				if (colored == true && this.highlighted.get(i)[0] == z) {
					
					g.setColor(highlightColor);
					int lenght = this.highlighted.get(i)[1] - this.highlighted.get(i)[0];
					int cArea = ((lenght + 1) * (this.elemWidth + 8)) + (lenght * spaceWidth) + 4;
					
					g.fillRect(x - 2, y, cArea, elemHeight);
				}
					
				
				int geneID = this.genome.getChromosomes().get(i).getGenes().get(z).getId();	
				Color currentColor;
				int idToDraw;
					
					
				// get the gene element color from gecko.colormap
				if (geneID < 0) {
							
					idToDraw = geneID / -1;
					currentColor = gecko.getColormap().get(idToDraw);
				}
				else {
							
					idToDraw = geneID;
					currentColor = gecko.getColormap().get(geneID);
				}
					
				g.setColor(currentColor);
						
				// check the id the first time to know whether we have to paint the triangle 
				// to the left side
				if (geneID < 0) {
							
					int xPoints[] = {x + 8, x, x + 8};
					int yPoints[] = {y, y + (elemHeight / 2), y + elemHeight};
					g.fillPolygon(xPoints, yPoints, 3);
						
					x = x + 8;
				}	
						
				// draw the rectangle
				g.fillRect(x, y, elemWidth, elemHeight);
						
				// draw the gene number into the rectangle
				g.setColor(Color.BLACK);
				g.drawString(Integer.toString(idToDraw) , x + 3, y + 12);
				g.setColor(currentColor);
						
				x = x + elemWidth;
						
				// check the id the second time to know whether we have to paint the triangle 
				// to the right side
				if (geneID > 0) {
							
					int xPoints[] = {x, x + 8, x};
					int yPoints[] = {y, y + (elemHeight / 2), y + elemHeight};
					g.fillPolygon(xPoints, yPoints, 3);
							
					x = x + 8;
				}
						
				x = x + spaceWidth;
				for (int k = count; k < count + spaceWidth; k++) {
					
					this.genePositions[count] = usedChrom;
				}
				
				count = count + spaceWidth;
			}
					
			g.setColor(Color.BLACK);
					
			// draw the ending of the chromosome
			g.fillRect(x, y + 8, elemWidth - 2, 2);
			g.fillRect(x + elemWidth - 2, y, 2, elemHeight);
			
			this.setPreferredSize(new Dimension(x + 20, 20));
			this.setSize(getPreferredSize());
		}
	}
	
	
	private void updateToolTip(int xPos) {
		
		int chrom = this.genePositions[xPos][0];
		
		if (chrom != -1) {
			
			int gene = this.genePositions[xPos][1];
			this.setToolTipText(this.genome.getChromosomes().get(chrom).getGenes().get(gene).getName());
		}
		
		
	}
	
	public Gene getGeneOnPos(int xPos) {
		
		int chrom = this.genePositions[xPos][0];
		
		if (chrom == -1) {
			
			return null;
		}
		else {
			
			int gene = this.genePositions[xPos][1];
			return this.genome.getChromosomes().get(chrom).getGenes().get(gene);
		}
		
	}
	
	
	
	
	//___________________________________________________________________________//
	//_____________________ Implementation Adjustable Interface _________________//
	//___________________________________________________________________________//
	
	@Override
	public void adjustSize() {
		// TODO Auto-generated method stub
		
	}


	//___________________________________________________________________________//
	//___________________________ MouseListener stuff ___________________________//
	//___________________________________________________________________________//

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// Do nothing
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
		this.updateToolTip(arg0.getX());
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
		this.setToolTipText("");
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// Do nothing
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// Do nothing
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TDo nothing
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
		this.updateToolTip(arg0.getX());
		
	}

}
