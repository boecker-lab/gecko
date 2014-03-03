package gecko2.io;

import gecko2.GeneClusterTestUtils;
import gecko2.algo.ReferenceClusterAlgorithm;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;
import gecko2.algorithm.Parameter;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static gecko2.GeneClusterTestUtils.performTest;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class GckFileReaderTest {
    @Test
    public void readFileSmallData()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/smallReaderTest.cog").getFile());
        File gckFile = new File(getClass().getResource("/smallReaderTestData.gck").getFile());

        testReadingDataWithoutClusters(cogFile, gckFile);
    }

    @Test
    public void readFileStatisticsData()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/statistics.cog").getFile());
        File gckFile = new File(getClass().getResource("/statisticsData.gck").getFile());

        testReadingDataWithoutClusters(cogFile, gckFile);
    }

    @Test
    public void readFileStringDBPartialData()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/stringDBPartial.cog").getFile());
        File gckFile = new File(getClass().getResource("/stringDBPartialData.gck").getFile());

        testReadingDataWithoutClusters(cogFile, gckFile);
    }

    @Test
    public void readFileSmallClusters()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/smallReaderTest.cog").getFile());
        File gckFile = new File(getClass().getResource("/smallReaderTestClusters.gck").getFile());
        Parameter p = new Parameter(1, 4, 4, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
        testReadingClusters(cogFile, gckFile, p);
    }

    @Test
    public void readFileStatisticsClusters()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/statistics.cog").getFile());
        File gckFile = new File(getClass().getResource("/statisticsClusters.gck").getFile());

        Parameter p = new Parameter(3, 7, 10, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);
        testReadingClusters(cogFile, gckFile, p);
    }

    @Test
    public void readFileStringDBPartialClusters()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/stringDBPartial.cog").getFile());
        File gckFile = new File(getClass().getResource("/stringDBPartialClusters.gck").getFile());

        Parameter p = new Parameter(3, 7, 10, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);
        testReadingClusters(cogFile, gckFile, p);
    }

    private static void testReadingDataWithoutClusters(File cogInfile, File gckInfile) throws IOException, ParseException {
        CogFileReader cogReader = new CogFileReader(cogInfile);

        cogReader.readData();

        GckFileReader gckReader = new GckFileReader(gckInfile);

        gckReader.readData();

        assertEquals(cogReader.getMaxIdLength(), gckReader.getMaxIdLength());
        assertEquals(cogReader.getMaxLocusTagLength(), gckReader.getMaxLocusTagLength());
        assertEquals(cogReader.getMaxNameLength(), gckReader.getMaxNameLength());

        assertArrayEquals(cogReader.getGenomes(), gckReader.getGenomes());
        assertEquals(cogReader.getGeneLabelMap(), gckReader.getGeneLabelMap());
    }

    private static void testReadingClusters(File cogInfile, File gckInfile, Parameter p) throws IOException, ParseException {
        CogFileReader cogReader = new CogFileReader(cogInfile);

        cogReader.readData();

        GckFileReader gckReader = new GckFileReader(gckInfile);

        gckReader.readData();

        // Assert successful genome reading
        assertEquals(cogReader.getMaxIdLength(), gckReader.getMaxIdLength());
        assertEquals(cogReader.getMaxLocusTagLength(), gckReader.getMaxLocusTagLength());
        assertEquals(cogReader.getMaxNameLength(), gckReader.getMaxNameLength());

        assertArrayEquals(cogReader.getGenomes(), gckReader.getGenomes());
        assertEquals(cogReader.getGeneLabelMap(), gckReader.getGeneLabelMap());

        GeneCluster[] computedResult = ReferenceClusterAlgorithm.computeReferenceClusters(Genome.toIntArray(cogReader.getGenomes()), p);

        performTest(computedResult, gckReader.getGeneClusters(), GeneClusterTestUtils.PValueComparison.COMPARE_NONE);
    }
}
