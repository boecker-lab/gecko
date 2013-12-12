package gecko2;

import gecko2.util.LibraryUtils;
import gecko2.util.LibraryUtils.PlatformNotSupportedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.zip.DataFormatException;

import static gecko2.GeneClusterTestUtils.automaticGeneClusterTestFromFile;

public class ReferenceClusterLargeTest {
	/**
	 * The method adds libgecko2 to the library path.
	 * 
	 */
	@BeforeClass
	public static void loadLibGecko2()
	{
        GeneClusterTestUtils.loadLibGecko2();
	}
	
	@Test
	public void statisticDataReferenceClusterTest() throws IOException, DataFormatException, ParseException {
		File inputFile = new File(getClass().getResource("/statistics.cog").getFile());
		File resultFile = new File(getClass().getResource("/statisticsDataD5S8Q10FixedRef.txt").getFile());
		
		automaticGeneClusterTestFromFile(inputFile, resultFile);
	}
}
