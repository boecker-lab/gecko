package gecko2.io;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.*;
import com.itextpdf.text.log.SysoCounter;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import gecko2.gui.GeneClusterPicture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
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
     * @param picture the cluster image we want to export to pdf
     */
    public GeneClusterToPDFWriter(File targetFile, String author, GeneClusterPicture picture) {
        this(targetFile, author, Arrays.asList(picture));
    }
	
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
        boolean writtenSuccessfully;
        int maxWidth = 0;
        int maxHeight = 0;
        for (GeneClusterPicture picture : clusterPictures) {
            maxWidth = Math.max(maxWidth, picture.getPageWidth());
            maxHeight = Math.max(maxHeight, picture.getPageHeight());
        }

		Document clusterPDF = new Document(new Rectangle(maxWidth, maxHeight));
        clusterPDF.addCreationDate();
        clusterPDF.addAuthor(this.author);
        clusterPDF.addCreator("Gecko2");
        clusterPDF.addProducer();
        clusterPDF.addSubject("Gene cluster pdf export");
        clusterPDF.addTitle("Gene cluster pdf export");

		try (FileOutputStream out = new FileOutputStream(this.targetFile)) {
            PdfWriter writer = PdfWriter.getInstance(clusterPDF , out);
            clusterPDF.open();
            PdfContentByte cb = writer.getDirectContent();
            for (int i=0; i<clusterPictures.size(); i++) {
                clusterPDF.newPage();
                // open pdf for writing
                PdfGraphics2D g = new PdfGraphics2D(cb, clusterPDF.getPageSize().getWidth(), clusterPDF.getPageSize().getHeight());
                //g.translate(0, (clusterPictures.size() - i) * clusterPDF.getPageSize().getHeight());
                clusterPictures.get(i).paint(g);
                g.dispose();
                //Image image = Image.getInstance(template);
                //float width = clusterPDF.getPageSize().getWidth() - clusterPDF.leftMargin() - clusterPDF.rightMargin();
                //float height = clusterPDF.getPageSize().getHeight() - clusterPDF.topMargin() - clusterPDF.bottomMargin();
                //image.scaleToFit(width, height);
                //clusterPDF.add(image);
            }
            clusterPDF.close();
            writtenSuccessfully = true;
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
            writtenSuccessfully = false;
		}
        return writtenSuccessfully;
	}
}
