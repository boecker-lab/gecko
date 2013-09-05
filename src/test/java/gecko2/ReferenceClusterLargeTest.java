package gecko2;

import static gecko2.GeneClusterTestUtils.automaticGeneClusterTestFromFile;

import gecko2.util.LibraryUtils;
import gecko2.util.LibraryUtils.PlatformNotSupportedException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.DataFormatException;

import gecko2.exceptions.LinePassedException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReferenceClusterLargeTest {
	/**
	 * The method adds libgecko2 to the library path.
	 * 
	 */
	@BeforeClass
	public static void loadLibGecko2()
	{
		System.err.println("You are running " + System.getProperty("os.arch") + "-Java on " + System.getProperty("os.name"));
		
		try 
		{
			LibraryUtils.loadLibrary("libgecko2");
		} 
		catch (PlatformNotSupportedException e) 
		{
			e.printStackTrace();
			System.exit(1);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Test
	public void statisticDataReferenceClusterTest() throws URISyntaxException, IOException, DataFormatException, LinePassedException {
		File inputFile = new File(ReferenceClusterTest.class.getResource("/statisticsData.cog").toURI());
		File resultFile = new File(ReferenceClusterTest.class.getResource("/statisticsDataD5S8Q10FixedRef.txt").toURI());
		
		automaticGeneClusterTestFromFile(inputFile, resultFile);
	}
}
