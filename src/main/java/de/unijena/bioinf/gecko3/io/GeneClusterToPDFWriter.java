/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.io;

import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import de.unijena.bioinf.gecko3.gui.GeneClusterPicture;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;


/**
 * The class generates a picture with the gene cluster selected in the GeneClusterSelector.
 * 
 * 
 * @author Hans-Martin Haase
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
        clusterPDF.addAuthor("Gecko3");
        clusterPDF.addCreator("Gecko3");
        clusterPDF.addProducer();
        clusterPDF.addSubject("Gene cluster pdf export");
        clusterPDF.addTitle("Gene cluster pdf export");

        PdfWriter writer = PdfWriter.getInstance(clusterPDF , out);
        clusterPDF.open();
        PdfContentByte cb = writer.getDirectContent();
        for (GeneClusterPicture clusterPicture : clusterPictures) {
            clusterPDF.setPageSize(new RectangleReadOnly(clusterPicture.getPageWidth(), clusterPicture.getPageHeight()));
            clusterPDF.setMargins(0, 0, 0, 0);
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
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        if (out != null)
            out.close();
    }
}
