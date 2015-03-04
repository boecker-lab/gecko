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
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.unijena.bioinf.gecko3.algo.GeneClusterTestUtils.assertEqualsBigDecimal;
import static org.junit.Assert.*;

public class ClusterAnnotationReaderTest {
	private DataSet data = null;
	
	@Before
	public void setUp() {
		try {
			File inputFile = new File(ClusterAnnotationReader.class.getResource("/smallTest.cog").toURI());
            CogFileReader reader = new CogFileReader(inputFile);
			GeckoInstance.getInstance().setCurrentWorkingDirectoryOrFile(inputFile);

			data = reader.readData();
		} catch (IOException | ParseException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private void compareClusters(List<GeneCluster> expected, List<GeneCluster> actual, boolean comparePValues){
		assertEquals(expected.size(), actual.size());
		for (GeneCluster actualCluster : actual) {
			boolean match = false;
			
			for (GeneCluster expectedCluster: expected) {
				if (expectedCluster.getMinTotalDist() != actualCluster.getMinTotalDist())
					continue;
				if (expectedCluster.getRefSeqIndex() != actualCluster.getRefSeqIndex())
					continue;
				
				Set<GeneFamily> expGenes = new HashSet<>();
				for (GeneFamily i : expectedCluster.getGeneFamilies())
					expGenes.add(i);
				Set<GeneFamily> actGenes = new HashSet<>();
				for (GeneFamily i : actualCluster.getGeneFamilies())
					actGenes.add(i);
				if (!expGenes.equals(actGenes))
					continue;
				
				match = true;
				
				if (comparePValues)
					assertEqualsBigDecimal(expectedCluster.getBestPValue(), actualCluster.getBestPValue(), 14);

                assertEquals(expectedCluster.getOccurrences(true).getTotalDist(), actualCluster.getOccurrences(true).getTotalDist());
                Subsequence[][] expSub = expectedCluster.getOccurrences(true).getSubsequences();
                Subsequence[][] actSub = actualCluster.getOccurrences(true).getSubsequences();

                assertEquals(expSub.length, actSub.length);
                for (int j=0; j<actSub.length; j++) {
                    if (expSub[j].length != actSub[j].length)
                        System.out.println("TEST");
                    assertEquals(expSub[j].length, actSub[j].length);
                    for (int k=0; k<actSub[j].length; k++){
                        assertEquals(expSub[j][k].getChromosome(), actSub[j][k].getChromosome());
                        assertEquals(expSub[j][k].getDist(), actSub[j][k].getDist());
                        assertEquals(expSub[j][k].getStart(), actSub[j][k].getStart());
                        assertEquals(expSub[j][k].getStop(), actSub[j][k].getStop());
                        if (comparePValues)
                            assertEqualsBigDecimal(expSub[j][k].getpValue(), actSub[j][k].getpValue(), 14);
                    }
                }

                assertEquals(expectedCluster.getOccurrences(false).getTotalDist(), actualCluster.getOccurrences(false).getTotalDist());
                expSub = expectedCluster.getOccurrences(false).getSubsequences();
                actSub = actualCluster.getOccurrences(false).getSubsequences();

                assertEquals(expSub.length, actSub.length);
                for (int j=0; j<actSub.length; j++) {
                    assertEquals(expSub[j].length, actSub[j].length);
                    for (int k=0; k<actSub[j].length; k++){
                        assertEquals(expSub[j][k].getChromosome(), actSub[j][k].getChromosome());
                        assertEquals(expSub[j][k].getDist(), actSub[j][k].getDist());
                        assertEquals(expSub[j][k].getStart(), actSub[j][k].getStart());
                        assertEquals(expSub[j][k].getStop(), actSub[j][k].getStop());
                    }
                }
			}
			assertTrue(String.format("No matching cluster for %s found!", actualCluster.toString()), match);
		}
	}
	
	@Test
	public void readAnnotationsCorrectOrderingTest() {
		File annotationFile = null;
		try {
			annotationFile = new File(ClusterAnnotationReader.class.getResource("/smallTestD1S4Q3.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		readAnnotationsTest(annotationFile);
	}
	
	@Test
	public void readAnnotationsRandomOrderingTest() {
		File annotationFile = null;
		try {
			annotationFile = new File(ClusterAnnotationReader.class.getResource("/smallTestD1S4Q3RandomOrder.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		readAnnotationsTest(annotationFile);
	}
	
	private void readAnnotationsTest(File annotationFile){
		List<GeneCluster> clusters = ClusterAnnotationReader.readClusterAnnotations(annotationFile, data);
		assertNotNull(clusters);
		assertEquals(12, clusters.size());

		Parameter p = new Parameter(1, 4, 3, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        List<GeneCluster> res = GeckoInstance.computeClustersJava(data, p, null);
		
		compareClusters(res, clusters, false);
	}
	
	@Test
	public void geckoInstanceIntegrationTest(){
		File annotationFile = null;
		try {
			annotationFile = new File(ClusterAnnotationReader.class.getResource("/smallTestD1S4Q3.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		List<GeneCluster> clusters = ClusterAnnotationReader.readClusterAnnotations(annotationFile, data);
		assertNotNull(clusters);
		assertEquals(12, clusters.size());
	}
}
