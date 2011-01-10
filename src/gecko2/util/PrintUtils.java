package gecko2.util;

import gecko2.GeckoInstance;

public class PrintUtils {
	
	public static void printDebug(String s) {
		if (GeckoInstance.getInstance().isDebugEnabled())
			System.err.println(s);
	}

}
