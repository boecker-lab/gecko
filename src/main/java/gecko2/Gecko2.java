package gecko2;

import gecko2.LibraryUtils.PlatformNotSupportedException;
import gecko2.gui.Gui;
import gecko2.io.CLI;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	    } catch (Throwable t) {}
	    
		boolean libgeckoLoaded = false;
		
		// check whether first argument is  valid file
		// checked in the load routine
		boolean externalLib = false;
		
		// check whether showing the help is requested
		boolean help = false;
		boolean gui = false;
		Pattern sHelp = Pattern.compile("-h|--help");
		Pattern uGui = Pattern.compile("-g|--gui");
		Matcher optChecker;
		
		for (int i = 0; i < args.length && !help; i++) {
			
			optChecker = sHelp.matcher(args[i]);
			
			if (optChecker.find()) {
				
				help = true;
			}
			
			optChecker = uGui.matcher(args[i]);
			
			if (optChecker.find()) {
				
				gui = true;
			}
		}
		
		// load the libraries if showing the help isn't requested
		if (!help) {
		
			// there is no possibility to check whether a file is a text or compiled binary file
			// so we use primarily the file extension for checking the library existence.
			
			boolean inFileGckCog = true;
			
			if (args.length > 0) {
				
				Pattern inFile = Pattern.compile("gck|cog");
				Matcher inFileMatch = inFile.matcher(args[0]);
				
				if (!inFileMatch.find()) {
					
					inFileGckCog = false;
				}
			}
			boolean libloaderror = false;
			
			try {
				
				if ((args.length > 2 || args.length == 1 || (args.length == 2 && gui)) && !inFileGckCog) {
				
					LibraryUtils.loadLibrary(args[0],true);
					externalLib = true;
				}
				else {
				
					LibraryUtils.loadLibrary("libgecko2");
				}
			
				libgeckoLoaded = true;
			} catch (PlatformNotSupportedException e) {
			
				e.printStackTrace();
				System.err.println("Running in visualization only mode!");
				libloaderror = true;
			} catch (IOException e) {
			
				e.printStackTrace();
				System.err.println("Running in visualization only mode!");
				libloaderror = true;
			}
		
			System.err.println(":o)");
	    		
			GeckoInstance instance = GeckoInstance.getInstance();
			instance.setLibgeckoLoaded(true);
		
			if (gui || args.length <= 1) {
				
				// start gui session
				Gui.startUp();
			}
			else {
				
				// start cli session
				if (!libloaderror) {
				
					new CLI(args, externalLib);
				}
				else {
					
					// cli is useless without the lib so we terminate the program
					// NOTE: Return codes 1 - 7 are used in CLI.java
					System.exit(8);
				}
			}
		}
		else {
			
			CLI.showHelp();
		}
	}
}
