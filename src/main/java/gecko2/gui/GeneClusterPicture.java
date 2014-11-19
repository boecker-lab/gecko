package gecko2.gui;

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
		
	/**
	 * The selected gene cluster
	 */
	private final GeneClusterLocationSelection clusterSelection;
	
	/**
	 * All input genomes from gecko instance
	 */
	private final Genome[] genomes;
	
	/**
	 * Tells us whether to write genome names instead if just the number.
	 * <br>
	 * Default is false.
	 */
	private boolean gNames = false;

    private GenomePainting.NameType nameType = null;
	
	/**
	 * Stores the current data from GeckoInstance.
	 */
	private final GeckoInstance gecko;
	
	/**
	 * Stores the number of the genome with the longest cluster. This is needed to calculate
	 * the centering of the other genomes.
	 */
	private int refPaintGenome;
	
	/**
	 * Stores the length of the longest cluster.
	 */
	private int refPaintLength;

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
	
	/**
	 * The maximum number of genes in any subsequence
	 */
	private int maxSubseqLength;
	
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
	private static final int NR_ADDITIONAL_GENES = 1;

    public GeneClusterPicture(GeneCluster selectedCluster, GenomePainting.NameType nameType, boolean gnames) {
        this(selectedCluster.getDefaultLocationSelection(false), nameType, gnames);
    }

	/**
	 * The constructor sets all important variables while the variables
	 * gNames and geneCode depend on the users choice.
	 *
     * @param nameType either id, name or locus_tag
     * @param gnames true if the genome name shall replace the number
     */
	public GeneClusterPicture(GeneClusterLocationSelection clusterLocation, GenomePainting.NameType nameType, boolean gnames) {
		this.gecko = GeckoInstance.getInstance();
		this.clusterSelection = clusterLocation;
		this.genomes = gecko.getGenomes();
		this.gNames = gnames;
		this.setNameType(nameType);
	}
	
    private int[] getSubselection() {
        return clusterSelection.getSubselection();
    }

	/**
	 * This method find the genome with the longest sequence in the cluster.
	 */
	private void setRefPaintGenom() {		
		refPaintLength = 0;
		
		for (int i = 0; i < this.clusterSelection.getSubsequenceLength(); i++) {
			if (getSubselection()[i] != GeneClusterOccurrence.GENOME_NOT_INCLUDED) {
				Subsequence subsequence = this.clusterSelection.getSubsequence(i);
				int size = subsequence.getStop() - subsequence.getStart() + 1;
				
				if (size > refPaintLength) {		
					refPaintLength = size;
					refPaintGenome = i;
				}				
			}
		}
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
		maxSubseqLength = 0;
		
		for (int i = 0; i < clusterSelection.getSubsequenceLength(); i++) {
            if (getSubselection()[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                continue;

            if (genomes[i].getName().length() > maxGenomeNameLength)
                maxGenomeNameLength = (byte) genomes[i].getName().length();

            Subsequence subSequence = clusterSelection.getSubsequence(i);
            if (subSequence.isValid()){
                for (int k = subSequence.getStart() - 1; k < subSequence.getStop(); k++){
                    if (subSequence.getStop()-subSequence.getStart()+1 > maxSubseqLength)
                        maxSubseqLength = subSequence.getStop()-subSequence.getStart()+1;

                    Gene gene = genomes[i].getChromosomes().get(subSequence.getChromosome()).getGenes().get(k);
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
			this.elemWidth = gecko.getMaxLength(GenomePainting.NameType.ID) * 7 + 12;
		
		if (gNames)
			this.nameWidth = 7 * maxGenomeNameLength + 4;
		else
			this.nameWidth = 7 * Integer.toString(genomes.length).length() + 4;
		
		this.pageWidth = 2 + this.nameWidth + 2 + 16 + 10 + (maxSubseqLength + 2 * NR_ADDITIONAL_GENES) *(elemWidth + hgap + 8) + 2;
	}
	
	public void paint(Graphics g){
		this.setRefPaintGenom();
		// half cluster sequence + 1 if number of genes in the sequence is no straight
		boolean refEven = refPaintLength % 2 == 0;

		int y = 2;
		
		// create a white background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.pageWidth, this.pageHeight);
		g.setFont(new Font("Monospaced", Font.PLAIN, 10));
		
		for (int i = 0; i < clusterSelection.getSubsequenceLength(); i++) {
			// if the length is 0 the genome isn't contained in the cluster
			if (getSubselection()[i] != GeneClusterOccurrence.GENOME_NOT_INCLUDED) {
				Subsequence subsequence = clusterSelection.getSubsequence(i);
				int x = 2;
				
				x = paintGenomeHeader(g, i, x, y);
				
				// determine start setOff if not in refPaintGenome
				int setOff = NR_ADDITIONAL_GENES;
				if (this.refPaintGenome != i) {
					int seqLength = subsequence.getStop() - subsequence.getStart() + 1;
					if (refEven)
						setOff = NR_ADDITIONAL_GENES + refPaintLength/2 - (seqLength/2 + seqLength%2);
					else
						setOff = NR_ADDITIONAL_GENES + refPaintLength/2 - seqLength/2;
				}
				
				int geneIndex = subsequence.getStart() - 1 - setOff; // Paint setOff many additional genes (or gaps) that are not part of the cluster
				
				for (int paintIndex = 0; paintIndex < refPaintLength + 2 * NR_ADDITIONAL_GENES; paintIndex++) {
					if (geneIndex < -1)  // first gene is at index 0
						x += elemWidth + 2 * hgap;
					else if (geneIndex == -1)
						x = paintChromosomeStart(g, x, y);
					else if (geneIndex >= genomes[i].getChromosomes().get(subsequence.getChromosome()).getGenes().size()) {
						x = paintChromosomeEnd(g, x, y);
						break;
					} else {
						boolean partOfCluster = subsequence.getStart() - 1 <= geneIndex && geneIndex < subsequence.getStop(); 
						x = paintGene(g, this.genomes[i].getChromosomes().get(subsequence.getChromosome()).getGenes().get(geneIndex), partOfCluster, x, y);
					}
					geneIndex++;
				}
				
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
	
	/**
	 * The method takes a color from the array geneColors and associate the color with a 
	 * given gene family and returns the color. If the id already exists just the
	 * color will be returned without any new association.
	 * 
	 * @param geneFamily the gene family
	 * @return returns the color for the gene element of the given gene ID
	 */
	private Color getColor(GeneFamily geneFamily) {
		Color out;
		
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
				else {
					// fallback if we do not have enough colors defined
					out = GeckoInstance.getInstance().getGeneColor(geneFamily);
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
            name = this.genomes[genomeIndex].getName();
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
    private int paintGene(Graphics g, Gene gene, boolean partOfCluster, int x, int y) {
        Color currentColor = getColor(gene.getGeneFamily());

        return GenomePainting.paintGene(g, gene, false, nameType, partOfCluster ? Color.ORANGE : Color.WHITE, currentColor, x, y, elemWidth, elemHeight, hgap, vgap);
    }
}
