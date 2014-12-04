package de.unijena.bioinf.gecko3.gui;

import com.google.common.collect.Lists;
import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.Chromosome;
import de.unijena.bioinf.gecko3.datastructures.Gene;
import de.unijena.bioinf.gecko3.datastructures.GeneFamily;
import de.unijena.bioinf.gecko3.datastructures.Genome;

import javax.swing.*;
import java.awt.*;
import java.security.InvalidParameterException;

import static de.unijena.bioinf.gecko3.gui.util.ColorUtils.getTranslucentColor;


public class GenomePainting {
	
	private final static int ARROWSIZE = 8;
	private final static int CHROMOSOME_END_WIDTH = 18;

    /**
     * Tells us whether to write gene name, gene ID or locus tag
     */
    public enum NameType {
        ID("ID"), NAME("Name"), LOCUS_TAG("locus_tag");
        private final String display;
        private NameType(String s) {
            display = s;
        }
        @Override
        public String toString() {
            return display;
        }
        public static NameType getNameTypeFromString(String name) {
            switch (name) {
                case "ID":
                    return ID;
                case "Name":
                    return NAME;
                case "locus_tag":
                    return LOCUS_TAG;
                default:
                    throw new IllegalArgumentException("Only 'ID', 'Name' or 'locus_tag' are supported!");
            }
        }
    }

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
     * Paints one gene, the gene text is automatically generated from the gene id, the gecko gene label map and the nameType,
     * the orientation is generated from the gene id (id < 0 = NEGATIVE, id > 0 = POSITIVE)
     * @param g the Graphics
     * @param gene the gene
     * @param flipped if the gene shall be painted flipped
     * @param nameType the type of name information that shall be used
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
    public static int paintGene(Graphics g, Gene gene, boolean flipped, NameType nameType, Color backgroundColor, Color color, int x, int y, int width, int height, int hgap, int vgap) {
        return paintGene(g, gene, flipped, nameType, gene.getOrientation(), backgroundColor, color, x, y, width, height, hgap, vgap);
    }

    /**
     * Paints one gene, the gene text is automatically generated from the gene id, the gecko gene label map and the nameType
     * @param g the Graphics
     * @param gene the gene
     * @param flipped if the gene shall be painted flipped
     * @param nameType the type of name information that shall be used
     * @param orientation the orientation of the gene, NEGATIVE, POSITIVE, or UNSIGNED
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
    public static int paintGene(Graphics g, Gene gene, boolean flipped, NameType nameType, Gene.GeneOrientation orientation, Color backgroundColor, Color color, int x, int y, int width, int height, int hgap, int vgap) {
        String name = "";
        switch (nameType) {
            case ID: name = gene.getExternalId();
                break;
            case NAME: name = gene.getName();
                break;
            case LOCUS_TAG: name = gene.getTag();
                break;
        }
        return paintGene(g, flipped, orientation, backgroundColor, color, name, x, y, width, height, hgap, vgap);
    }

    /**
     * Paints one gene
     * @param g the Graphics
     * @param flipped if the gene shall be painted flipped
     * @param geneOrientation the orientation of the gene,
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
	public static int paintGene(Graphics g, boolean flipped, Gene.GeneOrientation geneOrientation, Color backgroundColor, Color color, String text, int x, int y, int width, int height, int hgap, int vgap) {
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
		
		// check the id the first time to know whether we have to paint the triangle to the left side
		if ((flipped && geneOrientation == Gene.GeneOrientation.POSITIVE) || (!flipped && geneOrientation == Gene.GeneOrientation.NEGATIVE)) {
			int xPoints[] = {x , x + ARROWSIZE, x + width, x + width, x + ARROWSIZE};
			int yPoints[] = {y + (height / 2), y + height, y + height, y, y};
			g.fillPolygon(xPoints, yPoints, 5);

			x = x + ARROWSIZE; // don't paint text in the arrow
		}
		
		// check the id to know whether we have to paint the triangle to the right side
		if ((flipped && geneOrientation == Gene.GeneOrientation.NEGATIVE) || (!flipped && geneOrientation == Gene.GeneOrientation.POSITIVE)) {
			int xPoints[] = {x , x + width - ARROWSIZE, x + width, x + width - ARROWSIZE, x};
			int yPoints[] = {y + height, y + height,y + (height / 2), y, y};
			g.fillPolygon(xPoints, yPoints, 5);
		}

        // check the id to know whether we have to paint no triangle
        if (geneOrientation == Gene.GeneOrientation.UNSIGNED) {
            int xPoints[] = {x , x + width, x + width, x};
            int yPoints[] = {y + height, y + height, y, y};
            g.fillPolygon(xPoints, yPoints, 4);
        }
			
		// draw the gene number or the name into the rectangle
		// change color of the letters to white if element color is to dark
		if (color.getRed() + color.getGreen() + color.getBlue() > 450)
			g.setColor(Color.BLACK);
		else 
			g.setColor(Color.WHITE);

		setTextSize(g, height);
        int baseline = getBaseline(g.getFontMetrics(), y, height);

		g.drawString(text, x + 5, baseline);
		
		g.setColor(color);
		
		return returnX;
	}

    private static int getBaseline(FontMetrics metrics, int y, int height){
        int top = y + height;
        return (top+((y+1-top)/2) - ((metrics.getAscent() + metrics.getDescent())/2)) + metrics.getAscent();
    }
	
	private static void setTextSize(Graphics g, int geneHeight){
		float newTextSize = Math.round((float)geneHeight - ((float)geneHeight / 2.4F));
		g.setFont(g.getFont().deriveFont(newTextSize));
	}

    public static String buildMaxLengthString(int textWidth) {
        StringBuilder builder = new StringBuilder(textWidth);
        for (int i=0; i<textWidth; i++)
            builder.append("W");
        return builder.toString();
    }

    public static int getGeneWidth(Graphics g, String text, int geneHeight) {
        setTextSize(g, geneHeight);
        return g.getFontMetrics().stringWidth(text) + ARROWSIZE + 4;
    }
	
	private static Color getColor(Gene gene) {
        return getColor(gene.getGeneFamily());
	}

    private static Color getColor(GeneFamily geneFamily) {
        Color color = GeckoInstance.getInstance().getGeneColor(geneFamily);
        if (color == null)
            return Color.GRAY;
        else
            return color;
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
     * @param nameType what information shall be painted in each gene
	 * @param x the x coordinate to start the painting at
	 * @param y the y coordinate to start the painting at
	 * @param width the width of one gene
	 * @param height the height of one gene
	 * @param hgap the size of the gap between genes
	 * @param vgap the vertical gap size
     * @param flipped if the genome shall be painted inverse
	 */
	public static void paintGenome(Graphics g, Genome genome, boolean flipped, NameType nameType, int x, int y, int width, int height, int hgap, int vgap) {
		for (Chromosome chr : genome.getChromosomes()) {
			x = paintChromosome(g, chr, flipped, nameType, x, y, width, height, hgap, vgap);
		}
	}
	
	/**
	 * Paints the Genome
	 * @param g the used Graphics
	 * @param genome the Genome
	 * @param highlights the chromosome index, start and end gene of the area that will be highlighted
     * @param nameType what information shall be painted in each gene
	 * @param x the x coordinate to start the painting at
	 * @param y the y coordinate to start the painting at
	 * @param width the width of one gene
	 * @param height the height of one gene
	 * @param hgap the size of the gap between genes
	 * @param vgap the vertical gap size
	 */
	public static void paintGenomeWithCluster(Graphics g, Genome genome, boolean flipped, int[] highlights, NameType nameType, Color highlightColor, int x, int y, int width, int height, int hgap, int vgap) {
		if (highlights == null || highlights.length != 3)
			paintGenome(g, genome, flipped, nameType, x, y, width, height, hgap, vgap);
		else {
			for (int i=0; i<genome.getChromosomes().size(); i++) {
				Chromosome chr = genome.getChromosomes().get(i);
				if (i != highlights[0])
					x = paintChromosomeGrey(g, chr, flipped, nameType, x, y, width, height, hgap, vgap);
				else
					x = paintChromosomeWithCluster(g, chr, flipped, highlights[1], highlights[2], nameType, highlightColor, x, y, width, height, hgap, vgap);
			}
		}
	}

	/**
	 * Paints the Chromosome
	 * @param g the used Graphics
	 * @param chromosome the Chromosome
     * @param nameType what information shall be painted in each gene
	 * @param x the x coordinate to start the painting at
	 * @param y the y coordinate to start the painting at
	 * @param width the width of one gene
	 * @param height the height of one gene
	 * @param hgap the size of the gap between genes
	 * @param vgap the vertical gap size
     * @param flipped if the chromosome shall be painted inverse
	 */
	private static int paintChromosome(Graphics g, Chromosome chromosome, boolean flipped, NameType nameType, int x, int y, int width, int height, int hgap, int vgap){
        return paintChromosome(g, chromosome, flipped, -1, -1, false, nameType, null, x, y, width, height, hgap, vgap);
	}
	
	/**
	 * Paints the Chromosome with translucent gene colors
	 * @param g the used Graphics
	 * @param chromosome the Chromosome
     * @param nameType what information shall be painted in each gene
	 * @param x the x coordinate to start the painting at
	 * @param y the y coordinate to start the painting at
	 * @param width the width of one gene
	 * @param height the height of one gene
	 * @param hgap the size of the gap between genes
 	 * @param vgap the vertical gap size
     * @param flipped if the chromosome shall be painted inverse
	 */
	private static int paintChromosomeGrey(Graphics g, Chromosome chromosome, boolean flipped, NameType nameType, int x, int y, int width, int height, int hgap, int vgap){
        return paintChromosome(g, chromosome, flipped, -1, -1, true, nameType, null, x, y, width, height, hgap, vgap);

	}
	
	/**
	 * Paints the Chromosome with highlighted cluster, the rest has translucent gene colors
	 * @param g the used Graphics
     * @param chromosome the Chromosome
	 * @param start the start gene of the cluster
	 * @param stop the end gene of the cluster
     * @param nameType what information shall be painted in each gene
	 * @param highlightColor the color the cluster background is color with
	 * @param x the x coordinate to start the painting at
	 * @param y the y coordinate to start the painting at
	 * @param width the width of one gene
	 * @param height the height of one gene
	 * @param hgap the size of the gap between genes
 	 * @param vgap the vertical gap size
     * @param flipped if the chromosome shall be painted inverse
	 */
	private static int paintChromosomeWithCluster(Graphics g, Chromosome chromosome, boolean flipped,
			int start, int stop, NameType nameType, Color highlightColor, int x, int y, int width,
			int height, int hgap, int vgap) {
        return paintChromosome(g, chromosome, flipped, start, stop, true, nameType, highlightColor, x, y, width, height, hgap, vgap);
	}

    /**
     * Paints the Chromosome with all possible options.
     * Used by all chromosome painting methods.
     * @param g the used Graphics
     * @param chromosome the Chromosome
     * @param start the start gene of the cluster, -1 if no cluster
     * @param stop the end gene of the cluster, -1 if no cluster
     * @param  useTranslucentGenes if translucent colors shall be used for the painting, outside the cluster, if a cluster is contained
     * @param nameType what information shall be painted in each gene
     * @param highlightColor the color the cluster background is color with, null if no cluster
     * @param x the x coordinate to start the painting at
     * @param y the y coordinate to start the painting at
     * @param width the width of one gene
     * @param height the height of one gene
     * @param hgap the size of the gap between genes
     * @param vgap the vertical gap size
     * @param flipped if the chromosome shall be painted inverse
     */
    private static int paintChromosome(Graphics g, Chromosome chromosome, boolean flipped, int start, int stop, boolean useTranslucentGenes, NameType nameType, Color highlightColor, int x, int y, int width,
                                       int height, int hgap, int vgap) {
        if (highlightColor == null && !(start == -1 && stop==-1))
            throw new InvalidParameterException("Invalid Parameters, highlightColor == null and not start and stop == -1");

        x = paintChromosomeStart(g, x, y, width, height, hgap);

        java.util.List<Gene> listView = flipped ? Lists.reverse(chromosome.getGenes()) : chromosome.getGenes();
        if (flipped && start != -1 && stop != -1){
            int tmpStart = start;
            start = chromosome.getGenes().size() - 1 - stop;
            stop = chromosome.getGenes().size() - 1 - tmpStart;
        }

        for (int i=0; i<listView.size(); i++) {
            Gene gene = listView.get(i);
            if (i>=start && i<=stop)
                x = paintGene(g, gene, flipped, nameType, highlightColor, getColor(gene), x, y, width, height, hgap, vgap);
            else
                x = paintGene(g, gene, flipped, nameType, Color.WHITE, useTranslucentGenes ? getTranslucentColor(getColor(gene)) : getColor(gene), x, y, width, height, hgap, vgap);
        }

        x = paintChromosomeEnd(g, x, y, width, height, hgap);
        return x;
    }

    public static class GeneIcon implements Icon {
        private final GeneFamily geneFamily;
        private final int width;
        private final int height;

        public GeneIcon(GeneFamily geneFamily, int width, int height) {
            this.geneFamily = geneFamily;
            this.width = width;
            this.height = height;
        }

        /**
         * Draw the icon at the specified location.  Icon implementations
         * may use the Component argument to get properties useful for
         * painting, e.g. the foreground or background color.
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            paintGene(g, false, Gene.GeneOrientation.UNSIGNED, Color.WHITE, getColor(geneFamily), geneFamily.getExternalId(), x, y, width, height, 0, 0);
        }

        /**
         * Returns the icon's width.
         *
         * @return an int specifying the fixed width of the icon.
         */
        @Override
        public int getIconWidth() {
            return width;  //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * Returns the icon's height.
         *
         * @return an int specifying the fixed height of the icon.
         */
        @Override
        public int getIconHeight() {
            return height;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
