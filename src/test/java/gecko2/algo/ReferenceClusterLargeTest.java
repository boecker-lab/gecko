package gecko2.algo;

import gecko2.testUtils.ReferenceClusterTestSettings;
import gecko2.util.LibraryUtils;
import gecko2.util.LibraryUtils.PlatformNotSupportedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.zip.DataFormatException;

import static gecko2.algo.GeneClusterTestUtils.automaticGeneClusterTestFromFile;

public class ReferenceClusterLargeTest {

	@Test
	public void statisticDataReferenceClusterTest() throws IOException, DataFormatException, ParseException {
        ReferenceClusterTestSettings settings = ReferenceClusterTestSettings.statisticsDataD5S8Q10FixedRef();

        automaticGeneClusterTestFromFile(settings);
	}
}
