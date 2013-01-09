package gecko2.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;

/**
 * The class implements a simple preview for the pdf file created with the
 * GeneClusterToPDFWriter.
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 *
 */
public class PDFPreview extends JScrollPane {

	/**
	 * Random generated serial version UID
	 */
	private static final long serialVersionUID = -1392501449288707281L;
	
	/**
	 * Stores the original given picture which was set in the constructor.
	 */
	private BufferedImage clusPic;
	
	
	/**
	 * The constructor sets the global variable clusPic and sets a basic layout.
	 * 
	 * @param toShow BufferdImage from GeneClusterToPDFWriter
	 */
	public PDFPreview (BufferedImage toShow) {
	
		this.clusPic = toShow;
		
		// set up the panel
		this.setPreferredSize(new Dimension(297, 421));
		this.setSize(getPreferredSize());
		this.setLayout(new ScrollPaneLayout());
		this.setBackground(Color.WHITE);		
	}
	
	/**
	 * Overwrites the paint method so the the cluster picture is on the Frame.
	 */
	public void paint(Graphics g) {
		
		// resize image; original page size does not fit on many laptops 
		BufferedImage resized= new BufferedImage(297, 421, BufferedImage.TYPE_INT_ARGB);
		resized.getGraphics().drawImage(this.clusPic, 0,0, 297, 421, null);
		
		g.drawImage(resized, 100, 20, null);
		
	}

}
