package gecko2.io;

import gecko2.GeckoInstance;
import gecko2.algorithm.Chromosome;
import gecko2.testUtils.GeneClusterTestUtils;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;
import gecko2.algorithm.Parameter;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static gecko2.testUtils.GeneClusterTestUtils.performTest;
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

        assertEquals(cogReader.getGenomes().length, gckReader.getGenomes().length);
        for (int i=0; i<cogReader.getGenomes().length; i++) {
            assertEquals(cogReader.getGenomes()[i].getName(), gckReader.getGenomes()[i].getName());

            assertEquals(cogReader.getGenomes()[i].getChromosomes().size(), gckReader.getGenomes()[i].getChromosomes().size());
            for (int j=0; j<cogReader.getGenomes()[i].getChromosomes().size(); j++) {
                Chromosome expectedChromosome = cogReader.getGenomes()[i].getChromosomes().get(j);
                Chromosome actualChromosome = gckReader.getGenomes()[i].getChromosomes().get(j);

                assertEquals(expectedChromosome.getName(), actualChromosome.getName());

                assertEquals(expectedChromosome.getGenes().size(), actualChromosome.getGenes().size());
                for (int k=0; k<expectedChromosome.getGenes().size(); k++)
                    assertEquals(expectedChromosome.getGenes().get(k), actualChromosome.getGenes().get(k));
            }
        }
        assertArrayEquals(cogReader.getGenomes(), gckReader.getGenomes());
        assertEquals(cogReader.getGeneFamilySet(), gckReader.getGeneFamilySet());
        assertEquals(cogReader.getUnknownGeneFamily(), gckReader.getUnknownGeneFamily());
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
        assertEquals(cogReader.getGeneFamilySet(), gckReader.getGeneFamilySet());
        assertEquals(cogReader.getUnknownGeneFamily(), gckReader.getUnknownGeneFamily());

        GeneCluster[] computedResult = GeckoInstance.getInstance().computeClustersJava(cogReader.getData(), p);

        performTest(computedResult, gckReader.getGeneClusters(), GeneClusterTestUtils.PValueComparison.COMPARE_NONE);
    }
}
