package gecko2;

import gecko2.util.LibraryUtils;
import gecko2.util.LibraryUtils.PlatformNotSupportedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.zip.DataFormatException;

public class ReferenceClusterLargeTest {
    private static boolean libGeckoLoaded = false;
    /**
     * The method adds libgecko2 to the library path.
     *
     */
    @BeforeClass
    public static void loadLibGecko2()
    {
        try {
            LibraryUtils.loadLibrary("libgecko2");
            libGeckoLoaded = true;
        } catch (PlatformNotSupportedException | IOException e) {
            libGeckoLoaded = false;
        }
    }
	
	@Test
	public void statisticDataReferenceClusterTest() throws IOException, DataFormatException, ParseException {
		File inputFile = new File(getClass().getResource("/statistics.cog").getFile());
		File resultFile = new File(getClass().getResource("/statisticsDataD5S8Q10FixedRef.txt").getFile());
		
		//automaticGeneClusterTestFromFile(inputFile, resultFile, libGeckoLoaded);
	}
}
