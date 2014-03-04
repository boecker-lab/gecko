package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Gene;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;
import gecko2.algorithm.Subsequence;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

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
	private final GeneCluster selectedCluster;
	private final int[] subselection;
	
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
	private final HashMap<Integer, Color> newColorMap = new HashMap<>();
	
	/**
	 * Current position in the geneColor array.
	 */
	private int colorPos = 0;
	
	/**
	 * The number of additional genes that is shown on each side of the longest cluster occurrence.
	 */
	private static final int NR_ADDITIONAL_GENES = 1;

    public GeneClusterPicture(GeneCluster selectedCluster) {
        this(selectedCluster, selectedCluster.getDefaultSubSelection(false));
    }

    /**
     * Default constructor which sets all important global variable.
     */
    public GeneClusterPicture(GeneCluster selectedCluster, int[] subselection) {
        this(selectedCluster, subselection, GenomePainting.NameType.ID, false);
    }

	/**
	 * The constructor sets all important variables while the variables
	 * gNames and geneCode depend on the users choice.
	 *
     * @param nameType either id, name or locus_tag
     * @param gnames true if the genome name shall be replace the number
     */
	public GeneClusterPicture(GeneCluster selectedCluster, int[] subselection, GenomePainting.NameType nameType, boolean gnames) {
		this.gecko = GeckoInstance.getInstance();
		this.selectedCluster = selectedCluster;
		this.subselection = subselection;
		this.genomes = gecko.getGenomes();
		this.gNames = gnames;
		this.setNameType(nameType);
	}
	
	/**
	 * This method find the genome with the longest sequence in the cluster.
	 */
	private void setRefPaintGenom() {		
		refPaintLength = 0;
		
		for (int i = 0; i < this.selectedCluster.getOccurrences()[0].getSubsequences().length; i++) {			
			if (this.selectedCluster.getOccurrences()[0].getSubsequences()[i].length != 0) {
				Subsequence subsequence = this.selectedCluster.getOccurrences()[0].getSubsequences()[i][subselection[i]];
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
		
		for (int i = 0; i < gecko.getGenomes().length; i++) {	
			if (this.selectedCluster.getOccurrences()[0].getSubsequences()[i].length > 0) {			
				if (gecko.getGenomes()[i].getName().length() > maxGenomeNameLength)				
					maxGenomeNameLength = (byte) gecko.getGenomes()[i].getName().length();
								
				if (this.selectedCluster.getOccurrences()[0].getSubsequences()[i][subselection[i]].isValid()) {					
					int start = this.selectedCluster.getOccurrences()[0].getSubsequences()[i][subselection[i]].getStart();
					int stop = this.selectedCluster.getOccurrences()[0].getSubsequences()[i][subselection[i]].getStop();
					int chrom = this.selectedCluster.getOccurrences()[0].getSubsequences()[i][subselection[i]].getChromosome();
					
					for (int k = start -1; k < stop; k++) {
						if (stop-start+1 > maxSubseqLength)
							maxSubseqLength = stop - start + 1;
						
						Gene gene = gecko.getGenomes()[i].getChromosomes().get(chrom).getGenes().get(k);
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
	}
	
	private void resetPageSize() {
		calcLengths(nameType);
		this.pageHeight = selectedCluster.getSize() * (this.elemHeight + 2 * this.vgap);
				
		// we have to set the width here in the constructor it does not work
		if (this.nameType != GenomePainting.NameType.ID)
			this.elemWidth = this.maxGeneCodeLength * 8 + 8;
		else 	
			this.elemWidth = gecko.getMaxLength(GenomePainting.NameType.ID) * 7 + 12;
		
		if (gNames)
			this.nameWidth = 7 * maxGenomeNameLength + 4;
		else
			this.nameWidth = 7 * Integer.toString(this.gecko.getGenomes().length).length() + 4;
		
		this.pageWidth = 2 + this.nameWidth + 2 + 16 + 10 + (maxSubseqLength + 2 * NR_ADDITIONAL_GENES) *(elemWidth + hgap + 8) + 2;
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
			
		boolean refSeq = (this.selectedCluster.getRefSeqIndex() == genomeIndex);
		
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
		Color currentColor = getColor(Math.abs(gene.getId()));
		
		return GenomePainting.paintGene(g, gene, nameType, partOfCluster ? Color.ORANGE : Color.WHITE, currentColor, x, y, elemWidth, elemHeight, hgap, vgap);
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
		
		for (int i = 0; i < this.genomes.length; i++) {
			// if the length is 0 the genome isn't contained in the cluster
			if (this.selectedCluster.getOccurrences()[0].getSubsequences()[i].length != 0) {
				Subsequence subsequence = selectedCluster.getOccurrences()[0].getSubsequences()[i][subselection[i]];
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
					else if (geneIndex >= this.gecko.getGenomes()[i].getChromosomes().get(subsequence.getChromosome()).getGenes().size()) {
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
	 * given gene ID (colorID) and returns the color. If the id already exists just the 
	 * color will be returned without any new association.
	 * 
	 * @param colorID gene ID
	 * @return returns the color for the gene element of the given gene ID
	 */
	private Color getColor(int colorID) {
		Color out;
		
		if (Gene.isSingleGeneFamily(colorID)) {
			out = Color.GRAY;
		}
		else {
			if (newColorMap.containsKey(colorID)) {
				out = newColorMap.get(colorID);
			}
			else {		
				if (colorPos < geneColors.length) {
					out = geneColors[colorPos];
					newColorMap.put(colorID, out);
					colorPos++;
				}
				else {
					// fallback if we do not have enough colors defined
					out = Gene.getGeneColor(colorID);
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
}
