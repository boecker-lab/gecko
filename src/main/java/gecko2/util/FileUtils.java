package gecko2.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileUtils {
	
    public static String getExtension(File f) {
        String ext = "";
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
	public static class GenericFilter extends FileFilter {
		
		private final String extensions;
		
		/**
		 * Create a new Generic Filter
		 * @param extensions A list of allowed extensions separated by semicolon
		 */
		public GenericFilter(String extensions) {
			this.extensions = extensions;
		}

		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			String ext = FileUtils.getExtension(f).toLowerCase();
			if (ext!=null) {
				String[] exts = extensions.split(";");
				for (String e : exts)
					if (ext.equals(e.toLowerCase())) return true;
			}	
			return false;
		}

		@Override
		public String getDescription() {
			if (extensions!=null && !extensions.equals(""))
				return "."+extensions.replace(";"," .");
			else
				return "";
		}
		
	}
	



}
