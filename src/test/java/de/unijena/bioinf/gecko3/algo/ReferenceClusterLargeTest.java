package de.unijena.bioinf.gecko3.algo;

import de.unijena.bioinf.gecko3.testUtils.ReferenceClusterTestSettings;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.zip.DataFormatException;

import static de.unijena.bioinf.gecko3.algo.GeneClusterTestUtils.automaticGeneClusterTestFromFile;

public class ReferenceClusterLargeTest {

	@Test
	public void statisticDataReferenceClusterTest() throws IOException, DataFormatException, ParseException {
        ReferenceClusterTestSettings settings = ReferenceClusterTestSettings.statisticsDataD5S8Q10FixedRef();

        automaticGeneClusterTestFromFile(settings);
	}
}
