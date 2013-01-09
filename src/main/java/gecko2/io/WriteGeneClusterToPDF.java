package gecko2.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import gecko2.GeckoInstance;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class WriteGeneClusterToPDF {
	
	private Document document;
	protected GeckoInstance gecko;
	
	public WriteGeneClusterToPDF(String fileName, String containedGenomes) {
		
		document = new Document(PageSize.A4, 50, 50, 50, 50); // TODO check this border properties
		gecko = GeckoInstance.getInstance();
		
		try {
			// TODO generate file name unique e.g. by using time stamp
			PdfWriter.getInstance(document , new FileOutputStream(fileName + ".pdf"));
			document.addCreationDate();
			document.addAuthor("Gecko2");	// TODO get the user name from the system and enter it here
			document.addCreator("Gecko2");
			document.addProducer();
			document.addSubject("Gene cluster pdf export");
			document.addTitle("Gene cluster pdf export" );
			
			document.open();
			
			document.add(new Paragraph("Hello world, this is a test for usage of iText"));
			document.close();
			
		} catch (FileNotFoundException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO add method for writing the gene cluster
		
		
		
	}
	
	// TODO remove main, currently just for testing
	public static void main (String[] args) {
		
		WriteGeneClusterToPDF test = new WriteGeneClusterToPDF("test", "hund x katze");
	}

}
