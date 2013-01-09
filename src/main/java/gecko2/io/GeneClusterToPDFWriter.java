package gecko2.io;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import gecko2.GeckoInstance;
import gecko2.algorithm.Gene;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;
import gecko2.algorithm.Subsequence;

/**
 * The class generates a picture with the gene cluster selected in the GeneClusterSelector.
 * 
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.07
 */
public class GeneClusterToPDFWriter {
	
	/**
	 * The selected gene cluster
	 */
	private GeneCluster selectedCluster;
	
	/**
	 * All input genomes from gecko instance
	 */
	private Genome[] genomes;
	
	/**
	 * This variable tells us whether the data from the GeneClusterDisplay should
	 * be added to the picture.
	 * <br>
	 * Default is false.
	 */
	private boolean eData = false;
	
	/**
	 * Tell as whether to write genome names instead if just the number.
	 * <br>
	 * Default is false.
	 */
	private boolean gNames = false;
	
	/**
	 * File pointer to the pdf file.
	 */
	private File targetFile = null;
	
	/**
	 * Stores the current data from GeckoInstance.
	 */
	GeckoInstance gecko;
	
	/**
	 * Stores the number of the genome with the longest cluster. This is needed to calculate
	 * the centering of the other genomes.
	 */
	private int refPaintGenome = -1;
	
	/**
	 * Per convention we paint one or two genes in front of the cluster sequence. If one of this 
	 * genes is the first or the first in the cluster sequence is the first gene of the genome
	 * we paint a start symbol.
	 * <br><br>
	 * possible values: <br>
	 * -2: no beginning
	 * -1: first gene of the cluster sequence is beginning
	 *  0: the gene in front of the cluster sequence is the beginning
	 *  1: the gene in front of the 0 gene is beginning 
	 */
	private int paintBeginning = -3;
	
	/**
	 * This is analog to paintBeginning for the end of the genome.
	 */
	private int paintEnding = -3;
	
	/**
	 * The variable contains the name of the user.
	 */
	private String author;
	
	/**
	 * The constructor sets the global variables gecko, selectedCluster, genomes, eData, 
	 * gnames and targetFile.
	 * 
	 * @param targetFile this becomes the pdf output file
	 * @param author name of the user
	 * @param edata true for adding cluster informations
	 * @param gnames true for using genome names instead of IDs 
	 */
	public GeneClusterToPDFWriter(File targetFile, String author, boolean edata, boolean gnames) {
		
		this.gecko = GeckoInstance.getInstance();
		this.selectedCluster = gecko.getGui().getMgb().getSelectedCluster();
		this.genomes = gecko.getGenomes();
		this.eData = edata;
		this.gNames = gnames;
		this.targetFile = targetFile;
	}
	
	/**
	 * This method find the genome with the longest sequence in the cluster.
	 * 
	 * @return a HashMap which maps the genome ID on a integer array with two indices.
	 * Index zero is the length of the sequence in the cluster and index one is the number
	 * of the chromosome which contains the sequence.
	 */
	private HashMap<Integer, int[]> getGenomWhChromWhSeqLength() {
		
		HashMap<Integer, int[]> genChromSeq = new HashMap<Integer, int[]>();
		int[] chromAndSeqSize = new int[2];
		
		// don't know why but we have to do this in reverse order to get the right results
		for (int i = this.selectedCluster.getOccurrences()[0].getSubsequences().length - 1; i > -1; i--) {
			
			for (int j = 0; j < this.selectedCluster.getOccurrences()[0].getSubsequences()[i].length; j++) {
				
				Subsequence chrom = this.selectedCluster.getOccurrences()[0].getSubsequences()[i][j];
					
				chromAndSeqSize[0] = chrom.getStop() - chrom.getStart() + 1;
				chromAndSeqSize[1] = chrom.getChromosome();
				genChromSeq.put(i, chromAndSeqSize);		
				chromAndSeqSize = new int[2];
				
				// bit dirty
				if (this.refPaintGenome < chromAndSeqSize[0]) {
					
					// transform the gene number in the right one form (reverse order)
					this.refPaintGenome = this.selectedCluster.getOccurrences()[0].getSubsequences().length - i;
					
					if (chrom.getStart() > 2) {
						
						this.paintBeginning = -2;
					}
					else if (chrom.getStart() == 0) {
						
						this.paintBeginning = -1;
					}
					else if (chrom.getStart() == 1) {
						
						this.paintBeginning = 0;
					}
					else if (chrom.getStart() == 2) {
						
						this.paintBeginning = 1;
					}
					
					// now the same for the end
					if (this.genomes[this.selectedCluster.getOccurrences()[0].getSubsequences().length - i - 1].getChromosomes().get(j).getGenes().size() - 1 - 2 == chrom.getStop()) {
						
						this.paintBeginning = -1;
					}
					else if (this.genomes[this.selectedCluster.getOccurrences()[0].getSubsequences().length - i - 1].getChromosomes().get(j).getGenes().size() - 1 - 2 == chrom.getStop() + 1) {
						
						this.paintBeginning = 0;
					}
					else if (this.genomes[this.selectedCluster.getOccurrences()[0].getSubsequences().length - i - 1].getChromosomes().get(j).getGenes().size() - 1 - 2 == chrom.getStop() + 2) {
						
						this.paintBeginning = 1;
					}
					else {
						
						this.paintBeginning = -2;
					}
				}
			}
		}
		
		return genChromSeq;
	}
	
	public BufferedImage createPic() {
		BufferedImage clusPic = new BufferedImage(565, 812, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g = clusPic.createGraphics();
		
		createPic(g, clusPic.getWidth(), clusPic.getHeight());
		
		return clusPic;
	}
	
	
	public void createPic(Graphics2D g, int width, int height) {
		//super.paint(g);
		
		//System.out.println(this.getGenomWhLongestSeq()[0] + "   " + this.getGenomWhLongestSeq()[1]);
		
		int elemHeight = 16;
		int elemWidth = 8 + (9 * (gecko.getMaxIdLength() - 1));
		int vgap = 2;
		int hgap = 5;
		
		int x = 2;
		int y = 2;
		
		// create a white background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		
		HashMap<Integer, int[]> chromWhClus = this.getGenomWhChromWhSeqLength();
		int refSeq = ((int) Math.floor(chromWhClus.get(this.refPaintGenome)[0] / 2)) + (chromWhClus.get(this.refPaintGenome)[0] % 2);
		int back1 = refSeq + this.paintEnding;
		
		// loop over the genomes
		for (int i = 0; i < this.genomes.length; i++) {
			
			// if the length is 0 the genome isn't contained in the cluster
			if (this.selectedCluster.getOccurrences()[0].getSubsequences()[i].length != 0) {
			
				// set color for the rectangle 
				if (this.selectedCluster.getRefSeqIndex() == i) {
					
					g.setColor(Color.RED);
				}
				else {
					
					g.setColor(Color.BLUE);
				}
				
				// first draw number or name
				if (this.gNames == true) {
						
					g.drawRect(x, y, 8 + (9 * (this.genomes[i].getName().length() - 1)) - 1, elemHeight);
					
					g.setColor(Color.black);
					g.setFont(g.getFont().deriveFont(10.5F));
					g.drawString(this.genomes[i].getName(), x + 1, y + 12);
					
					// move pen
					x = x + 8 + (7 * (this.genomes[i].getName().length() - 1)) - 1;
				}
				else {
					
					g.drawRect(x, y, 8 + (2 * (Integer.toString(this.genomes.length).length() - 1)) + 4, elemHeight);
					
					g.setColor(Color.black);
					g.setFont(g.getFont().deriveFont(10.5F));
					g.drawString(Integer.toString(i + 1) , x + 3, y + 12);
					
					// move pen
					x = x + 8 + (2 * (Integer.toString(this.genomes.length).length() - 1)) + 4;
				}
				
				// init start
				int start = 0;
				
				// draw the beginning of the chromosome
				if (this.paintBeginning >= -1 && this.refPaintGenome == i) {
				
					// move the pen 30 pixels right
					x = x + 30;
					g.fillRect(x, y, 2, elemHeight);
					g.fillRect(x + 2, y + 8, 16, 2);
					
					// move the pen again
					// 2 + 16 length of the beginning symbol
					// 5 space between the beginning symbol and the first gene
					x = x + 2 + 16 + 5;
				}
				else {
					
					if (this.refPaintGenome == i) {
						
						// move the pen 30 pixels right
						x = x + 30;
						start = this.selectedCluster.getOccurrences()[0].getSubsequences()[i][0].getStart() - 2;
					}
					
					int cmpSeq = (int) (chromWhClus.get(i)[0] / 2) - (( chromWhClus.get(this.refPaintGenome)[0] / 2) % 2);
					int difffront2 = refSeq - cmpSeq;
					
					start = this.selectedCluster.getOccurrences()[0].getSubsequences()[i][0].getStart();
		
					if (start == 0) {

						// move the pen 30 pixels right
						x = x + 30;
						x = x + ((difffront2 + 1) * (5 + elemWidth + 8));
						g.fillRect(x, y, 2, elemHeight);
						g.fillRect(x + 2, y + 8, 16, 2);
						x = x + 2 + 16 + 5;
					}
					else {
						
						while(start > 0 && difffront2 >= 0) {
							
							start--;
							difffront2--;
						}
						
						if (start == 0) {
							
							// move the pen 30 pixels right
							x = x + 30;
							x = x + ((difffront2 + 1 -(chromWhClus.get(i)[0] % 2)) * (5 + elemWidth + 8));
							g.fillRect(x, y, 2, elemHeight);
							g.fillRect(x + 2, y + 8, 16, 2);
							x = x + 2 + 16 + 5;
						}
						else {
							
							// move the pen 30 pixels right
							x = x + 30;
							start = this.selectedCluster.getOccurrences()[0].getSubsequences()[i][0].getStart() - (refSeq - cmpSeq);
							x = x + 2 + 16 + 5;
						}
					}	
				}
				
				int end = this.selectedCluster.getOccurrences()[0].getSubsequences()[i][0].getStop();
				int bhMid = back1 - (int) (chromWhClus.get(i)[0] / 2);
				
				while (end < this.genomes[i].getChromosomes().get(0).getGenes().size() && bhMid < back1) {
					
					end++;
					bhMid++;
				} 
				
				int count = start + 1;
				
				// loop over the genes on the chromosome
				for (int z = start; z <  end; z++) {
					
					Gene gene = this.genomes[i].getChromosomes().get(chromWhClus.get(i)[1]).getGenes().get(z);	
					Color currentColor;
					int idToDraw;
					
					
					// get the gene element color from gecko.colormap
					if (gene.getId() < 0) {
							
						idToDraw = gene.getId() / -1;
						currentColor = gecko.getColormap().get(idToDraw);
					}
					else {
							
						idToDraw = gene.getId();
						currentColor = gecko.getColormap().get(gene.getId());
					}
					
					// mark the cluster sequence
					if (count == this.selectedCluster.getOccurrences()[0].getSubsequences()[i][0].getStart()) {
						
						g.setColor(Color.ORANGE);
						g.fillRect(x - 1, y - 1, (((elemWidth + 8) * chromWhClus.get(i)[0]) + ((chromWhClus.get(i)[0] - 1) * hgap)) + 2, elemHeight + 2);
					}
					
					g.setColor(currentColor);
						
					// check the id the first time to know whether we have to paint the triangle 
					// to the left side
					if (gene.getId() < 0) {
							
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
					if (gene.getId() > 0) {
							
						int xPoints[] = {x, x + 8, x};
						int yPoints[] = {y, y + (elemHeight / 2), y + elemHeight};
						g.fillPolygon(xPoints, yPoints, 3);
							
						x = x + 8;
					}
						
					x = x + 5;
					count++;
				}
					
				g.setColor(Color.BLACK);
					
				// draw the ending of the chromosome
				if (end == this.genomes[i].getChromosomes().get(chromWhClus.get(i)[1]).getGenes().size()) {
				
					g.fillRect(x, y + 8, 16, 2);
					g.fillRect(x + 16, y, 2, elemHeight);			
				}
				
				// move to the next line / reset x
				y = y + elemHeight + vgap;
				x = 2;
			}
		}
			
		//this.setVisible(true);
	}
	
	/**
	 * The function creates a PDF file from the panel content.
	 */
	public void createPDF() {
		Document clusterPDF = new Document(PageSize.A4, 50, 50, 50, 50);
		
		try {
			PdfWriter writer = PdfWriter.getInstance(clusterPDF , new FileOutputStream(this.targetFile));
		
			clusterPDF.addCreationDate();
			clusterPDF.addAuthor(System.getProperty("user.name"));
			clusterPDF.addCreator("Gecko2");
			clusterPDF.addProducer();
			clusterPDF.addSubject("Gene cluster pdf export");
			clusterPDF.addTitle("Gene cluster pdf export" );
			
			// open pdf for writing
			clusterPDF.open();
			
			// create graphics2D
			PdfContentByte cb = writer.getDirectContent();
			PdfGraphics2D g = new PdfGraphics2D(cb, clusterPDF.getPageSize().getWidth(), clusterPDF.getPageSize().getHeight());
			
			// create image
			this.createPic(g, 565, 812);
			
			// close pdf
			g.dispose();
			clusterPDF.close();
		
		} catch (FileNotFoundException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
