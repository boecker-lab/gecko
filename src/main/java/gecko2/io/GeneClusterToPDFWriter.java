package gecko2.io;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGraphics2D;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import gecko2.gui.GeneClusterPicture;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;


/**
 * The class generates a picture with the gene cluster selected in the GeneClusterSelector.
 * 
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.17
 */
public class GeneClusterToPDFWriter implements AutoCloseable {

	/**
	 * the output stream.
	 */
	private OutputStream out;

    /**
     * Creates a gene cluster to pdf writer using the given output stream
     *
     * @param outputStream the output stream
     */
    public GeneClusterToPDFWriter(OutputStream outputStream) {
        this.out = outputStream;
    }


    /**
     * Writes the given gene clusters picture to the output stream as a PDF.
     * @param clusterPicture the cluster picture
     * @throws IOException
     * @throws DocumentException
     */
    public void write(GeneClusterPicture clusterPicture) throws IOException, DocumentException {
        write(Arrays.asList(clusterPicture));
    }

    /**
     * Writes the given gene clusters pictures to the output stream as a PDF.
     * @param clusterPictures the list of GeneClusterPictures
     * @throws IOException
     * @throws DocumentException
     */
	public void write(List<GeneClusterPicture> clusterPictures) throws IOException, DocumentException {
        int maxWidth = 0;
        int maxHeight = 0;
        for (GeneClusterPicture picture : clusterPictures) {
            maxWidth = Math.max(maxWidth, picture.getPageWidth());
            maxHeight = Math.max(maxHeight, picture.getPageHeight());
        }

		Document clusterPDF = new Document(PageSize.A4);
        clusterPDF.addCreationDate();
        clusterPDF.addAuthor("Gecko2");
        clusterPDF.addCreator("Gecko2");
        clusterPDF.addProducer();
        clusterPDF.addSubject("Gene cluster pdf export");
        clusterPDF.addTitle("Gene cluster pdf export");

        PdfWriter writer = PdfWriter.getInstance(clusterPDF , out);
        clusterPDF.open();
        PdfContentByte cb = writer.getDirectContent();
        for (GeneClusterPicture clusterPicture : clusterPictures) {
            clusterPDF.newPage();
            // open pdf for writing
            PdfTemplate template = cb.createTemplate(clusterPicture.getPageWidth(), clusterPicture.getPageHeight());
            Graphics2D g = template.createGraphics(clusterPicture.getPageWidth(), clusterPicture.getPageHeight());
            clusterPicture.paint(g);
            g.dispose();
            Image image = Image.getInstance(template);
            float width = clusterPDF.getPageSize().getWidth() - clusterPDF.leftMargin() - clusterPDF.rightMargin();
            float height = clusterPDF.getPageSize().getHeight() - clusterPDF.topMargin() - clusterPDF.bottomMargin();
            image.scaleToFit(width, height);
            clusterPDF.add(image);
        }
        /**
         * Don't close the underlying stream. Would kill zipOutputStream when writing multiple pdfs.
         */
        writer.setCloseStream(false);
        clusterPDF.close();
	}

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * <p/>
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     * <p/>
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p/>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p/>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     * <p/>
     * <p>Note that unlike the {@link java.io.Closeable#close close}
     * method of {@link java.io.Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p/>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws java.io.IOException if this resource cannot be closed
     */
    @Override
    public void close() throws IOException {
        if (out != null)
            out.close();
    }
}
