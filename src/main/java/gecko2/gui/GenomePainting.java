package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.Gene;
import gecko2.algorithm.Genome;

import java.awt.*;

public class GenomePainting {
	
	private final static int ARROWSIZE = 8;
	private final static int CHROMOSOME_END_WIDTH = 18;
	
	/**
	 * Paints the header of the genome.
	 * @param g the Graphics
	 * @param text the genome name or number
	 * @param refSeq if the genome shall be highlighted as the reference sequence
	 * @param x the x coordinate the painting starts at
	 * @param y the y coordinate of the painting
	 * @param width the width
	 * @param height the height
	 * @return the x coordinate after the painting
	 */
	public static int paintGenomeHeader(Graphics g, String text, boolean refSeq, int x, int y, int width, int height){
		// set color for the rectangle with genome name or number
		if (refSeq)
			g.setColor(Color.RED);// reference sequence
		else		
			g.setColor(Color.BLUE);
		
		// first draw number or name				
		g.drawRect(x, y, width, height);
			
		g.setColor(Color.black);
		g.drawString(text, x + 4, y + 12);
			
		// move pen
		x = x + width;
		
		// move the pen 10 pixels right
		x = x + 10;
		return x;
	}

	/**
	 * Paints the beginning symbol |--
	 * @param g the Graphics
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param width the width
	 * @param height the height
	 * @param hgap the size of the gap next to the gene
	 * @return the x coordinate after the painting
	 */
	public static int paintChromosomeStart(Graphics g, int x, int y, int width, int height, int hgap)  {
		g.setColor(Color.BLACK);
		int paintPosition = x + width + hgap - CHROMOSOME_END_WIDTH;
		g.fillRect(paintPosition, y, 2, height);
		g.fillRect(paintPosition + 2, y + height/2, CHROMOSOME_END_WIDTH - 2, 2);
		
		return x + width + 2 * hgap;
	}
	
	/**
	 * Paints the end symbol --|
	 * @param g the Graphics
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param width the width
	 * @param height the height
	 * @param hgap the size of the gap next to the gene
	 * @return the x coordinate after the painting
	 */
	public static int paintChromosomeEnd(Graphics g, int x, int y, int width, int height, int hgap) {
		g.setColor(Color.BLACK);	
		g.fillRect(x + hgap, y + height/2, CHROMOSOME_END_WIDTH - 2, 2);
		g.fillRect(x + hgap + CHROMOSOME_END_WIDTH - 2, y, 2, height);
		
		return x + width + 2 * hgap;
	}
	
	/**
	  * Paints one gene, the gene text is automatically generated from the gene id and the gecko gene label map
	  * @param g the Graphics
	  * @param gene the gene
	  * @param backgroundColor the color of the background
	  * @param color the color of the gene arrow
	  * @param x the x coordinate
	  * @param y the y coordinate
	  * @param width the width of the gene box
	  * @param height the height of the gene box
	  * @param hgap the size of the gap next to the gene
	  * @param vgap the vertical gap size
	  * @return the x coordinate after the painting
	  */
	public static int paintGene(Graphics g, Gene gene, Color backgroundColor, Color color, int x, int y, int width, int height, int hgap, int vgap) {
		String geneName = GeckoInstance.getInstance().getGenLabelMap().get(Math.abs(gene.getId()))[0];
		return paintGene(g, gene, backgroundColor, color, geneName, x, y, width, height, hgap, vgap);
	}
	
	/**
	  * Paints one gene
	  * @param g the Graphics
	  * @param gene the gene
	  * @param backgroundColor the color of the background
	  * @param color the color of the gene arrow
	  * @param text the text in the gene box
	  * @param x the x coordinate
	  * @param y the y coordinate
	  * @param width the width of the gene box
	  * @param height the height of the gene box
	  * @param hgap the size of the gap next to the gene
	  * @param vgap the vertical gap size
	  * @return the x coordinate after the painting
	  */
	public static int paintGene(Graphics g, Gene gene, Color backgroundColor, Color color, String text, int x, int y, int width, int height, int hgap, int vgap) {
		g.setColor(color);
		
		int returnX = x + width + 2 * hgap;
		
		// if we are in the cluster, paint orange background
		if (!backgroundColor.equals(Color.WHITE)) {
			g.setColor(backgroundColor);
			g.fillRect(x, y - vgap, width + 2 * hgap, height + 2 * vgap);
			g.setColor(color);
		}
		
		// Start gap
		x += hgap;
		
		// check the id the first time to know whether we have to paint the triangle 
		// to the left side
		if (gene.getId() < 0) {
			int xPoints[] = {x , x + ARROWSIZE, x + width, x + width, x + ARROWSIZE};
			int yPoints[] = {y + (height / 2), y + height, y + height, y, y};
			g.fillPolygon(xPoints, yPoints, 5);

			x = x + ARROWSIZE; // don't paint text in the arrow
		}
		
		// check the id to know whether we have to paint the triangle 
		// to the right side
		if (gene.getId() > 0) {
			int xPoints[] = {x , x + width - ARROWSIZE, x + width, x + width - ARROWSIZE, x};
			int yPoints[] = {y + height, y + height,y + (height / 2), y, y};
			g.fillPolygon(xPoints, yPoints, 5);
		}
			
		// draw the gene number or the name into the rectangle
		// change color of the letters to white if element color is to dark
		if (color.getRed() + color.getGreen() + color.getBlue() > 450)
			g.setColor(Color.BLACK);
		else 
			g.setColor(Color.WHITE);
		
		//float newSize = Math.round((float) height - ((float) height / 2.4F));
		//g.setFont(g.getFont().deriveFont(newSize));
		setTextSize(g, height);
		FontMetrics metrics = g.getFontMetrics();
		int fontY_Position = y + Math.round(height / 2.0f) + Math.round(metrics.getHeight() / 2.0f);

		g.drawString(text, x + 5, fontY_Position);
		
		g.setColor(color);
		
		return returnX;
	}
	
	private static void setTextSize(Graphics g, int geneHeight){
		float newTextSize = Math.round((float)geneHeight - ((float)geneHeight / 2.4F));
		g.setFont(g.getFont().deriveFont(newTextSize));
	}
	
	public static int getGeneWidth(Graphics g, String text, int geneHeight) {
		setTextSize(g, geneHeight);
		return g.getFontMetrics().stringWidth(text) + ARROWSIZE + 4;
	}
	
	private static Color getColor(Gene gene) {
		if (gene.isUnknown())
			return Color.GRAY;
		return GeckoInstance.getInstance().getColormap().get(Math.abs(gene.getId()));
	}
	
	private static Color getGreyValueColor(Gene gene) {
		Color original = getColor(gene);
		if (original.equals(Color.GRAY))
			return original;
		int greyValue = (original.getBlue() + original.getRed() + original.getGreen()) / 3;
		return new Color(greyValue, greyValue, greyValue);

	}

	/**
	 * Returns the width of the chromosome start and end symbol for a given gene width
	 * @param geneWidth the gene width
	 * @return the width of the chromosome ending symbol
	 */
	public static int getChromosomeEndingWidth(int geneWidth) {
		return Math.max(geneWidth, CHROMOSOME_END_WIDTH);
	}
	
	/**
	 * Paints the Genome
	 * @param g the used Graphics
	 * @param genome the Genome
	 * @param x the x coordinate to start the painting at
	 * @param y the y coordinate to start the painting at
	 * @param width the width of one gene
	 * @param height the height of one gene
	 * @param hgap the size of the gap between genes
	 * @param vgap the vertical gap size
	 */
	public static void paintGenome(Graphics g, Genome genome, int x, int y, int width, int height, int hgap, int vgap) {
		for (Chromosome chr : genome.getChromosomes()) {
			x = paintChromosome(g, chr, x, y, width, height, hgap, vgap);
		}
	}
	
	/**
	 * Paints the Genome
	 * @param g the used Graphics
	 * @param genome the Genome
	 * @param highlights the chromsome index, start and end gene of the area that will be highlighted
	 * @param x the x coordinate to start the painting at
	 * @param y the y coordinate to start the painting at
	 * @param width the width of one gene
	 * @param height the height of one gene
	 * @param hgap the size of the gap between genes
	 * @param vgap the vertical gap size
	 */
	public static void paintGenomeWithCluster(Graphics g, Genome genome, int[] highlights, Color highlightColor, int x, int y, int width, int height, int hgap, int vgap) {
		if (highlights == null || highlights.length != 3)
			paintGenome(g, genome, x, y, width, height, hgap, vgap);
		else {
			for (int i=0; i<genome.getChromosomes().size(); i++) {
				Chromosome chr = genome.getChromosomes().get(i);
				if (i != highlights[0])
					x = paintChromosomeGrey(g, chr, x, y, width, height, hgap, vgap);
				else
					x = paintChromosomeWithCluster(g, chr, highlights[1], highlights[2], highlightColor, x, y, width, height, hgap, vgap);
			}
		}
	}

	/**
	 * Paints the Chromosome
	 * @param g the used Graphics
	 * @param chromosome the Chromosome
	 * @param x the x coordinate to start the painting at
	 * @param y the y coordinate to start the painting at
	 * @param width the width of one gene
	 * @param height the height of one gene
	 * @param hgap the size of the gap between genes
	 * @param vgap the vertical gap size
	 */
	private static int paintChromosome(Graphics g, Chromosome chromosome, int x, int y, int width, int height, int hgap, int vgap){
		x = paintChromosomeStart(g, x, y, width, height, hgap);
		for (Gene gene : chromosome.getGenes()) {
			x = paintGene(g, gene, Color.WHITE, getColor(gene), x, y, width, height, hgap, vgap);
		}
		x = paintChromosomeEnd(g, x, y, width, height, hgap);
		
		return x;
	}
	
	/**
	 * Paints the Chromosome with grey value gene colors
	 * @param g the used Graphics
	 * @param chromosome the Chromosome
	 * @param x the x coordinate to start the painting at
	 * @param y the y coordinate to start the painting at
	 * @param width the width of one gene
	 * @param height the height of one gene
	 * @param hgap the size of the gap between genes
 	 * @param vgap the vertical gap size
	 */
	private static int paintChromosomeGrey(Graphics g, Chromosome chromosome, int x, int y, int width, int height, int hgap, int vgap){
		x = paintChromosomeStart(g, x, y, width, height, hgap);
		for (Gene gene : chromosome.getGenes()) {
			x = paintGene(g, gene, Color.WHITE, getGreyValueColor(gene), x, y, width, height, hgap, vgap);
		}
		x = paintChromosomeEnd(g, x, y, width, height, hgap);
		
		return x;
	}
	
	/**
	 * Paints the Chromosome with grey value gene colors
	 * @param g the used Graphics
	 * @param chr the Chromosome
	 * @param start the start gene of the cluster
	 * @param stop the end gene of the cluster
	 * @param highlightColor the color the cluster background is color with
	 * @param x the x coordinate to start the painting at
	 * @param y the y coordinate to start the painting at
	 * @param width the width of one gene
	 * @param height the height of one gene
	 * @param hgap the size of the gap between genes
 	 * @param vgap the vertical gap size
	 */
	private static int paintChromosomeWithCluster(Graphics g, Chromosome chr,
			int start, int stop, Color highlightColor, int x, int y, int width,
			int height, int hgap, int vgap) {
		x = paintChromosomeStart(g, x, y, width, height, hgap);
		for (int i=0; i<chr.getGenes().size(); i++) {
			Gene gene = chr.getGenes().get(i);
			if (i>=start && i<=stop)
				x = paintGene(g, gene, highlightColor, getColor(gene), x, y, width, height, hgap, vgap);
			else
				x = paintGene(g, gene, Color.WHITE, getGreyValueColor(gene), x, y, width, height, hgap, vgap);
		}
		x = paintChromosomeEnd(g, x, y, width, height, hgap);
		
		return x;
	}
}
