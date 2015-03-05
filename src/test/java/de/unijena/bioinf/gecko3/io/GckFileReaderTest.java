/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.io;

import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.*;
import de.unijena.bioinf.gecko3.algo.GeneClusterTestUtils;
import de.unijena.bioinf.gecko3.testUtils.PerformanceTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

import static de.unijena.bioinf.gecko3.algo.GeneClusterTestUtils.compareGeneClusters;
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
        Parameter p = new Parameter(1, 4, 4, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
        testReadingClusters(cogFile, gckFile, p);
    }

    @Test
    public void readFileWithUnknownGeneInClusters()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/gckReaderTestUnknownGeneInCluster.cog").getFile());
        File gckFile = new File(getClass().getResource("/gckReaderTestUnknownGeneInCluster.gck").getFile());
        Parameter p = new Parameter(1, 4, 2, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
        testReadingClusters(cogFile, gckFile, p);
    }

    @Test
    @Category(PerformanceTest.class)
    public void readFileStatisticsClusters()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/statistics.cog").getFile());
        File gckFile = new File(getClass().getResource("/statisticsClusters.gck").getFile());

        Parameter p = new Parameter(3, 5, 10, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);
        testReadingClusters(cogFile, gckFile, p);
    }

    @Test
    @Category(PerformanceTest.class)
    public void readFileStringDBPartialClusters()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/stringDBPartial.cog").getFile());
        File gckFile = new File(getClass().getResource("/stringDBPartialClusters.gck").getFile());

        Parameter p = new Parameter(3, 7, 50, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);
        testReadingClusters(cogFile, gckFile, p);
    }

    private static void testReadingDataWithoutClusters(File cogInfile, File gckInfile) throws IOException, ParseException {
        CogFileReader cogFileReader = new CogFileReader(cogInfile);

        DataSet expectedData = cogFileReader.readData();

        GckFileReader gckFileReader = new GckFileReader(gckInfile);

        DataSet actualData = gckFileReader.readData();

        assertEquals(expectedData.getMaxIdLength(), actualData.getMaxIdLength());
        assertEquals(expectedData.getMaxLocusTagLength(), actualData.getMaxLocusTagLength());
        assertEquals(expectedData.getMaxNameLength(), actualData.getMaxNameLength());

        assertEquals(expectedData.getGenomes().length, actualData.getGenomes().length);
        for (int i=0; i<expectedData.getGenomes().length; i++) {
            assertEquals(expectedData.getGenomes()[i].getName(), actualData.getGenomes()[i].getName());

            assertEquals(expectedData.getGenomes()[i].getChromosomes().size(), actualData.getGenomes()[i].getChromosomes().size());
            for (int j=0; j<expectedData.getGenomes()[i].getChromosomes().size(); j++) {
                Chromosome expectedChromosome = expectedData.getGenomes()[i].getChromosomes().get(j);
                Chromosome actualChromosome = actualData.getGenomes()[i].getChromosomes().get(j);

                assertEquals(expectedChromosome.getName(), actualChromosome.getName());

                assertEquals(expectedChromosome.getGenes().size(), actualChromosome.getGenes().size());
                for (int k=0; k<expectedChromosome.getGenes().size(); k++)
                    assertEquals(expectedChromosome.getGenes().get(k), actualChromosome.getGenes().get(k));
            }
        }
        assertArrayEquals(expectedData.getGenomes(), actualData.getGenomes());
        assertEquals(expectedData.getGeneFamilySet(), actualData.getGeneFamilySet());
        assertEquals(expectedData.getUnknownGeneFamily(), actualData.getUnknownGeneFamily());
    }

    private static void testReadingClusters(File cogInfile, File gckInfile, Parameter p) throws IOException, ParseException {
        CogFileReader cogFileReader = new CogFileReader(cogInfile);

        DataSet expectedData = cogFileReader.readData();

        GckFileReader gckFileReader = new GckFileReader(gckInfile);

        DataSet actualData = gckFileReader.readData();

        // Assert successful genome reading
        assertEquals(expectedData.getMaxIdLength(), actualData.getMaxIdLength());
        assertEquals(expectedData.getMaxLocusTagLength(), actualData.getMaxLocusTagLength());
        assertEquals(expectedData.getMaxNameLength(), actualData.getMaxNameLength());

        assertArrayEquals(expectedData.getGenomes(), actualData.getGenomes());
        assertEquals(expectedData.getGeneFamilySet(), actualData.getGeneFamilySet());
        assertEquals(expectedData.getUnknownGeneFamily(), actualData.getUnknownGeneFamily());

        List<GeneCluster> computedResult = GeckoInstance.getInstance().computeClustersJava(expectedData, p, null);

        compareGeneClusters(computedResult, actualData.getClusters(), GeneClusterTestUtils.PValueComparison.COMPARE_NONE);
    }
}
