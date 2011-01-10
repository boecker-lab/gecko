package gecko2;

import gecko2.LibraryUtils.PlatformNotSupportedException;
import gecko2.gui.Gui;


import java.io.IOException;

import javax.swing.UIManager;

public class Gecko2 {
	
	public static void main (String[] args) {
		String lcOSName = System.getProperty("os.name").toLowerCase();
		System.err.println(lcOSName);
		boolean IS_MAC = lcOSName.startsWith("mac os x");
		if (IS_MAC) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Gecko\u00B2");
		}
		try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch (Throwable t) {
	    	;
	    }
	    
		try {
			LibraryUtils.loadLibrary("libgecko2");
		} catch (PlatformNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println(":o)");
	    		
		GeckoInstance.getInstance(); 
		Gui.startUp();
	}
	
	
}
