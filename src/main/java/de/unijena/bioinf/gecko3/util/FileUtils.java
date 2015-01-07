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

package de.unijena.bioinf.gecko3.util;

import javax.swing.filechooser.FileFilter;
import java.io.File;

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
