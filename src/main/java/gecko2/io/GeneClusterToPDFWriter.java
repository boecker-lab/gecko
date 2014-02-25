package gecko2.io;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import gecko2.gui.GeneClusterPicture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


/**
 * The class generates a picture with the gene cluster selected in the GeneClusterSelector.
 * 
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.17
 */
public class GeneClusterToPDFWriter {

	/**
	 * File pointer to the pdf file.
	 */
	private File targetFile;
	
	/**
	 * The variable contains the name of the user.
	 */
	private final String author;
	
	/**
	 * The images we want to have as pdf
	 */
	private final List<GeneClusterPicture> clusterPictures;
	
	/**
	 * The constructor sets the global variables gecko, selectedCluster, genomes, eData, 
	 * gnames and targetFile.
	 * 
	 * @param targetFile this becomes the pdf output file
	 * @param author name of the user
	 * @param pictures the cluster images we want to export to pdf
	 */
	public GeneClusterToPDFWriter(File targetFile, String author, List<GeneClusterPicture> pictures) {
		this.targetFile = targetFile;
		this.author = author;
		this.clusterPictures = pictures;
	}
	
	/**
	 * The function creates a PDF file from the panel content.
	 */
	public boolean createPDF() {
        boolean writtenSuccessfully = false;
		Document clusterPDF = new Document(new Rectangle(clusterPic.getPageWidth(), clusterPic.getPageHeight()));
		try (FileOutputStream out = new FileOutputStream(this.targetFile)) {
            for (GeneClusterPicture picture : clusterPictures) {
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
                picture.paint(g);
                g.dispose();
            }

            writtenSuccessfully = true;
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
            writtenSuccessfully = false;
		}
        clusterPDF.close();
        return writtenSuccessfully;
	}
}
