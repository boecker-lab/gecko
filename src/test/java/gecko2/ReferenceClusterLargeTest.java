package gecko2;

import gecko2.testUtils.ReferenceClusterTestSettings;
import gecko2.util.LibraryUtils;
import gecko2.util.LibraryUtils.PlatformNotSupportedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.zip.DataFormatException;

import static gecko2.testUtils.GeneClusterTestUtils.automaticGeneClusterTestFromFile;

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
        ReferenceClusterTestSettings settings = ReferenceClusterTestSettings.statisticsDataD5S8Q10FixedRef();

        automaticGeneClusterTestFromFile(settings, libGeckoLoaded);
	}
}
