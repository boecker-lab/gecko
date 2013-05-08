package gecko2.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import gecko2.GeckoInstance;


/**
 * The class generates a picture with the gene cluster selected in the GeneClusterSelector.
 * 
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.17
 */
public class GeneClusterToPDFWriter {

	/**
	 * This variable tells us whether the data from the GeneClusterDisplay should
	 * be added to the picture.
	 * <br>
	 * Default is false.
	 */
	private boolean eData = false;
	
	/**
	 * File pointer to the pdf file.
	 */
	private File targetFile = null;
	
	/**
	 * Stores the current data from GeckoInstance.
	 */
	GeckoInstance gecko;
	
	/**
	 * The variable contains the name of the user.
	 */
	private String author;
	
	/**
	 * The images we want to have as pdf
	 */
	private GeneClusterPicture clusterPic;
	
	/**
	 * The constructor sets the global variables gecko, selectedCluster, genomes, eData, 
	 * gnames and targetFile.
	 * 
	 * @param targetFile this becomes the pdf output file
	 * @param author name of the user
	 * @param edata true for adding cluster informations
	 * @param pics the image content we want to export to pdf
	 */
	public GeneClusterToPDFWriter(File targetFile, String author, boolean edata, GeneClusterPicture picture) {
		
		this.eData = edata;
		this.targetFile = targetFile;
		this.author = author;
		this.clusterPic = picture;
	}
	
	public void setOutputFile(String outputFile) {
		
		this.targetFile = new File(outputFile);
	}
	
	/**
	 * The function creates a PDF file from the panel content.
	 */
	public void createPDF() {
		
		Document clusterPDF = new Document(new Rectangle(clusterPic.getPageWidth(), clusterPic.getPageHeight()));
		
		try {
			FileOutputStream out = new FileOutputStream(this.targetFile);
			PdfWriter writer = PdfWriter.getInstance(clusterPDF , out);
		
			clusterPDF.addCreationDate();
			clusterPDF.addAuthor(this.author);
			clusterPDF.addCreator("Gecko2");
			clusterPDF.addProducer();
			clusterPDF.addSubject("Gene cluster pdf export");
			clusterPDF.addTitle("Gene cluster pdf export");
			
			// open pdf for writing
			clusterPDF.open();
			
			PdfContentByte cb = writer.getDirectContent();
			PdfGraphics2D g = new PdfGraphics2D(cb, clusterPDF.getPageSize().getWidth(), clusterPDF.getPageSize().getHeight());
			clusterPic.paint(g);
			g.dispose();
			clusterPDF.close();
			out.close();
		
		} catch (FileNotFoundException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
