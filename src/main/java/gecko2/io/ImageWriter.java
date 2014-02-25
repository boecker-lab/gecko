package gecko2.io;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This class provides static methods to export a BufferdImage into 
 * different image formats.
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.02
 */
public class ImageWriter {

	/**
	 * This static method creates a .png image from a BufferedImage and saves it in the 
	 * given location. 
	 * 
	 * @param exportPic the image to export
	 * @param storeLocation the location where to store the image
	 */
	public static void createPNGPic(BufferedImage exportPic, String storeLocation) {
		try {
		    ImageIO.write(exportPic, "png", new File(storeLocation));
		} 
		catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Errors occured while creating the image please try again.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * This static method creates a .jpg image from a BufferedImage and saves it in the 
	 * given location. 
	 * 
	 * @param exportPic the images to export
	 * @param storeLocation the location where to store the image
	 */
	public static void createJPGPic(BufferedImage exportPic, String storeLocation) {
		try {
			ImageIO.write(exportPic, "jpg", new File(storeLocation));
		} 
		catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Errors occured while creating the image please try again.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
