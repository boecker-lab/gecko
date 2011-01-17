package gecko2;

import gecko2.LibraryUtils.PlatformNotSupportedException;
import gecko2.gui.Gui;


import java.io.IOException;

import javax.swing.UIManager;

public class Gecko2 {
	
	public static void main (String[] args) {
		
		String lcOSName = System.getProperty("os.name").toLowerCase();
		System.err.println("You are running "+
				System.getProperty("os.arch")+
				"-Java on "+System.getProperty("os.name"));
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
			if (args.length==1)
				LibraryUtils.loadLibrary(args[0],true);
			else
				LibraryUtils.loadLibrary("libgecko2");
		} catch (PlatformNotSupportedException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.err.println(":o)");
	    		
		GeckoInstance.getInstance(); 
		Gui.startUp();

	}
	
	
}
