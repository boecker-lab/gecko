package gecko2.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides functions to load the gecko jni library on various platforms
 * 
 * @author Leon Kuchenbecker
 *
 */
public class LibraryUtils {
		
	/**
	 * Exception indicating that the operating system or architecture hosting this
	 * instance is not supported
	 * 
	 * @author Leon Kuchenbecker
	 *
	 */
	public static class PlatformNotSupportedException extends Exception {

		private static final long serialVersionUID = 1L;
		private final String os;
        private final String arch;
		
		public String getArch() {
			return arch;
		}
		
		public String getOs() {
			return os;
		}
		
		
		/**
		 * Creates a new {@link PlatformNotSupportedException} that will supply an error
		 * message naming the operating system and architecture
		 * @param os operating system to report
		 * @param arch architecture to report
		 */
		public PlatformNotSupportedException(String os, String arch) {
			super("Your platform ("+os+") and/or architecture ("+arch+") is not supported by this binary release.");
			this.os=os;
			this.arch=arch;
		}
		
	}
	
	 /**
	  * Try to load the specified library from the jar file.
	  * @param source the source directory
	  * @throws PlatformNotSupportedException
	  */
	public static void loadLibrary(String source, boolean fixedPath) throws PlatformNotSupportedException,IOException {
	
		// find out what library version we need to extract from the
		// jar file by checking the hosts operating system and architecture
		
		File f = null;
		String os 	= System.getProperty("os.name").toLowerCase().split(" ")[0];
		String arch = System.getProperty("os.arch").toLowerCase();
		if (!fixedPath) {
		
	
			String loadPath = "/lib/"+source+"/"+os+"__"+arch+".jni";
			System.err.println("Trying to load library from "+loadPath);
			
			InputStream is = LibraryUtils.class.getResourceAsStream(loadPath);
			if (is==null) {
				throw new PlatformNotSupportedException(os, arch);
			}
			
			
			f = File.createTempFile("libgecko", ".jni");
			f.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(f);
			
			while (is.available()!=0) {
				fos.write(is.read());
			}
			
			fos.flush();
			fos.close();
			is.close();
		} else {
			source = new File(source).getAbsolutePath();
			System.err.println("Trying to load library from "+source);
		}
		
		try {
			if (fixedPath)
				System.load(source);
			else
				System.load(f.getAbsolutePath());
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			if (fixedPath) {
				e.printStackTrace();
				System.err.println("The library file you provided could not be loaded!");
			} else
				throw new PlatformNotSupportedException(os, arch);
		}
	
		
	}
	
	public static void loadLibrary(String libname) throws PlatformNotSupportedException,IOException {
		loadLibrary(libname,false);
	}


}
