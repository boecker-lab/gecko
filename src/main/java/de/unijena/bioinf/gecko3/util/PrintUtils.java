package de.unijena.bioinf.gecko3.util;

import de.unijena.bioinf.gecko3.GeckoInstance;

public class PrintUtils {
	
	public static void printDebug(String s) {
		if (GeckoInstance.getInstance().isDebugEnabled())
			System.err.println(s);
	}
	
}
