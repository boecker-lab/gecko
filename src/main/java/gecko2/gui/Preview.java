package gecko2.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The class implements a simple preview for the pdf file created with the
 * GeneClusterToPDFWriter.
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 *
 */
public class Preview extends JPanel {

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
	public Preview (BufferedImage toShow) {
		this.setImage(toShow);
		this.setBackground(Color.WHITE);		
	}
	
	private void setImage(BufferedImage image) {
		this.clusPic = image;
		this.setPreferredSize(new Dimension(clusPic.getWidth(), clusPic.getHeight()));
	}

	/**
	 * Overwrites the paint method so the the cluster picture is on the Frame.
	 */
	@Override
	public void paint(Graphics g) {
		g.drawImage(this.clusPic, 15, 15, null);
	}

	public void updatePreview(BufferedImage newImage) {
		setImage(newImage);
		this.getGraphics().dispose();
		this.getGraphics().drawImage(this.clusPic, 15, 15, null);
		this.validate();
		this.repaint();
	}
	
}
