package gecko2.gui;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import gecko2.GeckoInstance;
import gecko2.datastructures.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements methods to paint parts of a genome.
 * The class creates the images itself based on the cluster information from a GeckoInstance instance 
 * and other available options.
 * There are also methods available which return and convert the image data.
 * 
 * @author Hans-Martin Haase <hmhaase at pclinuxosusers dot de>
 * @version 0.12
 */
public class GeneClusterPicture {

	/**
	 * Color array for the gene elements to prevent not nice looking colors.
	 */
	private final Color[] geneColors = {
		new Color(0xEEEEEE), // Shiny silver
		new Color(0xFFFF88), // Interactive action yellow
		new Color(0xCDEB8B), // Qoop Mint
		new Color(0xC3D9FF), // Gmail blue
		new Color(0xFF1A00), // Mozilla Red
		new Color(0xCC0000), // Rollyo Red
		new Color(0xFF7400), // RSS Orange
		new Color(0x008C00), // Techcrunch green
		new Color(0x006E2E), // Newsvine Green
		new Color(0x4096EE), // Flock Blue
		new Color(0xFF0084), // Flickr Pink
		new Color(0xB02B2C), // Ruby on Rails Red
		new Color(0xD15600), // Etsy Vermillion (kind of orange)
		new Color(0xC79810), // 43 Things Gold
		new Color(0x73880A), // Writely Olive
		new Color(0x6BBA70), // Basecamp Green
		new Color(0x3F4C6B), // Mozilla Blue
		new Color(0x356AA0), // Digg Blue
		new Color(0xD01F3C)}; // Last.fm Crimson (kind of pink)

    private Map<GeneFamily, Color> colorMap;

	/**
	 * The selected gene cluster
	 */
	private final GeneClusterLocationSelection clusterSelection;
	
	/**
	 * Tells us whether to write genome names instead if just the number.
	 * <br>
	 * Default is false.
	 */
	private boolean gNames = false;

    private GenomePainting.NameType nameType = null;

    /**
     * The maximum number of genes in any subSequence
     */
    private int maxSubseqLength;

	/**
	 * Default height of the gene elements
	 */
	private final int elemHeight = 16;
	
	/**
	 * Default width of the gene element. (Calculated by using the longest available id)
	 */
	private int elemWidth;
	
	/**
	 * Vertical space between the genomes.
	 */
	private final int vgap = 2;
	
	/**
	 * Horizontal space between the gene elements
	 */
	private final int hgap = 2;
	
	/**
	 * Page width
	 */
	private int pageWidth;
	
	/**
	 * Page height
	 */
	private int pageHeight;
	
	/**
	 * The length of the longest gene code.
	 */
	private int maxGeneCodeLength;	/**
	
	/**
	 * The length of the longest genome name from the clustered genomes.
	 */
	private int maxGenomeNameLength;
	
	private int nameWidth;
	
	/**
	 * New color mapping with the gene id.
	 */
	private final Map<GeneFamily, Color> newColorMap = new HashMap<>();
	
	/**
	 * Current position in the geneColor array.
	 */
	private int colorPos = 0;
	
	/**
	 * The number of additional genes that is shown on each side of the longest cluster occurrence.
	 */
	private static final int NR_ADDITIONAL_GENES = 0;

    public GeneClusterPicture(GeneCluster selectedCluster, GenomePainting.NameType nameType, boolean gnames) {
        this(selectedCluster.getDefaultLocationSelection(GeckoInstance.getInstance().getGenomes(),false), nameType, gnames);
    }

	/**
	 * The constructor sets all important variables while the variables
	 * gNames and geneCode depend on the users choice.
	 *
     * @param nameType either id, name or locus_tag
     * @param gnames true if the genome name shall replace the number
     */
	public GeneClusterPicture(GeneClusterLocationSelection clusterLocation, GenomePainting.NameType nameType, boolean gnames) {
		this.clusterSelection = clusterLocation;
		this.gNames = gnames;
		this.setNameType(nameType);
	}
	
    private int[] getSubselection() {
        return clusterSelection.getSubselection();
    }
	
	/**
	 * Setter for the variable gNames. Recreates the images.
	 * 
	 * @param gNames new boolean value
	 */
	public void setGnames (boolean gNames) {		
		this.gNames = gNames;
		
		if (this.maxGenomeNameLength == 0) {
			resetPageSize();
		}
	}
	
	/**
	 * Set the name type, either id, name or locus_tag
     * Recreates the image
	 *
	 * @param nameType the name type
	 */
	public void setNameType(GenomePainting.NameType nameType) {
		if (this.nameType != nameType) {
			this.nameType = nameType;
			resetPageSize();
		}
	}
	
	/**
	 * This method calculates the length of the longest gene code.
	 * It also calculates the length of the longest genome name.
	 * @param nameType the type of naming that is used.
	 */
	private void calcLengths(GenomePainting.NameType nameType) {
		maxGeneCodeLength = 0;
		maxGenomeNameLength = 0;
		maxSubseqLength = clusterSelection.getMaxClusterLocationWidth();
		
		for (int i = 0; i < clusterSelection.getTotalGenomeNumber(); i++) {
            if (getSubselection()[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                continue;

            if (clusterSelection.getGenome(i).getName().length() > maxGenomeNameLength)
                maxGenomeNameLength = (byte) clusterSelection.getGenome(i).getName().length();

            Subsequence subSequence = clusterSelection.getSubsequence(i);
            if (subSequence.isValid()){
                for (int k = subSequence.getStart() - 1; k < subSequence.getStop(); k++){
                    Gene gene = clusterSelection.getGenome(i).getChromosomes().get(subSequence.getChromosome()).getGenes().get(k);
                    if (nameType == GenomePainting.NameType.NAME)
                        if (gene.getName().length() > maxGeneCodeLength)
                            maxGeneCodeLength = gene.getName().length();
                    if (nameType == GenomePainting.NameType.LOCUS_TAG)
                        if (gene.getTag().length() > maxGeneCodeLength)
                            maxGeneCodeLength = gene.getTag().length();
                }
			}
		}
	}
	
	private void resetPageSize() {
		calcLengths(nameType);
		this.pageHeight = clusterSelection.getCluster().getSize() * (this.elemHeight + 2 * this.vgap);
				
		// we have to set the width here in the constructor it does not work
		if (this.nameType != GenomePainting.NameType.ID)
			this.elemWidth = this.maxGeneCodeLength * 8 + 8;
		else 	
			this.elemWidth = GeckoInstance.getInstance().getMaxLength(GenomePainting.NameType.ID) * 7 + 12;
		
		if (gNames)
			this.nameWidth = 7 * maxGenomeNameLength + 4;
		else
			this.nameWidth = 7 * Integer.toString(clusterSelection.getTotalGenomeNumber()).length() + 4;
		
		this.pageWidth = 2 + this.nameWidth + 2 + 16 + 10 + (maxSubseqLength + 2 * NR_ADDITIONAL_GENES) *(elemWidth + hgap + 8) + 2;
	}
	
	public void paint(Graphics g){
		int y = 2;
		
		// create a white background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.pageWidth, this.pageHeight);
		g.setFont(new Font("Monospaced", Font.PLAIN, 10));
		
		for (int i = 0; i < clusterSelection.getTotalGenomeNumber(); i++) {
			// if the length is 0 the genome isn't contained in the cluster
			if (getSubselection()[i] != GeneClusterOccurrence.GENOME_NOT_INCLUDED) {
                paintGenome(g, i, y);
				// move to the next line
				y = y + elemHeight + 2 * vgap;
			}
		}
	}
	
	public BufferedImage createImage() {
		resetPageSize();
		BufferedImage image = new BufferedImage(this.pageWidth,  this.pageHeight, BufferedImage.TYPE_INT_RGB);
		paint(image.getGraphics());
		return image;
	}

    private Color getColor(GeneFamily geneFamily) {
        /*if (colorMap == null)
            colorMap = GeneFamily.prepareColorMap(clusterSelection.getGeneFamilies(), null, clusterSelection.getCluster().getId());
        Color geneColor = colorMap.get(geneFamily);
        //Color geneColor = getStandardColor(geneFamily);
        if (geneColor == null)*/
            Color geneColor = GeckoInstance.getInstance().getGeneColor(geneFamily);
        return geneColor;
    }
	
	/**
	 * The method takes a color from the array geneColors and associate the color with a 
	 * given gene family and returns the color. If the id already exists just the
	 * color will be returned without any new association.
	 * 
	 * @param geneFamily the gene family
	 * @return returns the color for the gene element of the given gene ID
	 */
	private Color getStandardColor(GeneFamily geneFamily) {
		Color out = null;
		
		if (geneFamily.isSingleGeneFamily()) {
			out = Color.GRAY;
		}
		else {
			if (newColorMap.containsKey(geneFamily)) {
				out = newColorMap.get(geneFamily);
			}
			else {		
				if (colorPos < geneColors.length) {
					out = geneColors[colorPos];
					newColorMap.put(geneFamily, out);
					colorPos++;
				}
			}
		}
		
		return out;
	}

	public int getPageWidth() {
		return pageWidth;
	}

	public int getPageHeight() {
		return pageHeight;
	}

    private void paintGenome(Graphics g, int genomeIndex, int y){
        Subsequence subsequence = clusterSelection.getSubsequence(genomeIndex);
        int x = 2;

        x = paintGenomeHeader(g, genomeIndex, x, y);

        // determine start setOff if not in refPaintGenome
        int setOff = NR_ADDITIONAL_GENES + clusterSelection.getGeneOffset(genomeIndex);

        int geneIndex;
        if (clusterSelection.isFlipped(genomeIndex)){
            geneIndex = subsequence.getStop() + setOff;
        } else {
            geneIndex = subsequence.getStart() - 1 - setOff; // Paint setOff many additional genes (or gaps) that are not part of the cluster
        }

        for (int paintIndex = 0; paintIndex < maxSubseqLength + 2 * NR_ADDITIONAL_GENES; paintIndex++) {
            if (!clusterSelection.isFlipped(genomeIndex)) {
                if (geneIndex < -1)  // skip not in chromosome
                    x += elemWidth + 2 * hgap;
                else if (geneIndex == -1)
                    x = paintChromosomeStart(g, x, y);
                else if (geneIndex >= clusterSelection.getGenome(genomeIndex).getChromosomes().get(subsequence.getChromosome()).getGenes().size()) {
                    paintChromosomeEnd(g, x, y);
                    break;
                } else {
                    boolean partOfCluster = subsequence.getStart() - 1 <= geneIndex && geneIndex < subsequence.getStop();
                    x = paintGene(g, clusterSelection.getGenome(genomeIndex).getChromosomes().get(subsequence.getChromosome()).getGenes().get(geneIndex), clusterSelection.isFlipped(genomeIndex), partOfCluster, x, y);
                }
                geneIndex++;
            } else {
                if (geneIndex > clusterSelection.getGenome(genomeIndex).getChromosomes().get(subsequence.getChromosome()).getGenes().size()) // skip not in chromosome
                    x += elemWidth + 2 * hgap;
                else if (geneIndex == clusterSelection.getGenome(genomeIndex).getChromosomes().get(subsequence.getChromosome()).getGenes().size())
                    x = paintChromosomeStart(g, x, y);
                else if (geneIndex == -1) {
                    paintChromosomeEnd(g, x, y);
                    break;
                } else {
                    boolean partOfCluster = subsequence.getStart() - 1 <= geneIndex && geneIndex < subsequence.getStop();
                    x = paintGene(g, clusterSelection.getGenome(genomeIndex).getChromosomes().get(subsequence.getChromosome()).getGenes().get(geneIndex), clusterSelection.isFlipped(genomeIndex), partOfCluster, x, y);
                }
                geneIndex--;
            }

        }
    }

    /**
     * Paints the header of the genome. If gNames is set, paints the name, otherwise paints the number.
     * @param g the Graphics
     * @param genomeIndex the index of the genomes
     * @param x the x coordinate the painting starts at
     * @param y the y coordinate of the painting
     * @return the x coordinate after the painting
     */
    private int paintGenomeHeader(Graphics g, int genomeIndex, int x, int y){
        String name;
        if (this.gNames)
            name = clusterSelection.getGenome(genomeIndex).getName();
        else
            name = Integer.toString(genomeIndex + 1);

        boolean refSeq = (this.clusterSelection.getCluster().getRefSeqIndex() == genomeIndex);

        return GenomePainting.paintGenomeHeader(g, name, refSeq, x, y, this.nameWidth, this.elemHeight);
    }

    /**
     * Paints the beginning symbol |--
     * @param g the Graphics
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the x coordinate after the painting
     */
    private int paintChromosomeStart(Graphics g, int x, int y)  {
        return GenomePainting.paintChromosomeStart(g, x, y, elemWidth, elemHeight, hgap);
    }

    /**
     * Paints the end symbol --|
     * @param g the Graphics
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the x coordinate after the painting
     */
    private int paintChromosomeEnd(Graphics g, int x, int y) {
        return GenomePainting.paintChromosomeEnd(g, x, y, elemWidth, elemHeight, hgap);
    }

    /**
     * Paints one gene
     * @param g the Graphics
     * @param gene the gene
     * @param partOfCluster if the gene is part of the cluster and gets an orange background
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the new x coordinate behind the gene
     */
    private int paintGene(Graphics g, Gene gene, boolean flipped, boolean partOfCluster, int x, int y) {
        Color currentColor = getColor(gene.getGeneFamily());
        if (!partOfCluster) {
            currentColor = GenomePainting.getOpaqueColor(currentColor);
        }
        return GenomePainting.paintGene(g, gene, flipped, nameType, partOfCluster ? Color.ORANGE : Color.WHITE, currentColor, x, y, elemWidth, elemHeight, hgap, vgap);
    }
}
