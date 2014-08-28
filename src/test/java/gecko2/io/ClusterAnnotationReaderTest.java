package gecko2.io;

import gecko2.GeckoInstance;
import gecko2.algorithm.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gecko2.testUtils.GeneClusterTestUtils.assertEqualsBigDecimal;
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
				
				for (int i=0; i<actualCluster.getAllOccurrences().length; i++){
					assertEquals(expectedCluster.getAllOccurrences()[i].getTotalDist(), actualCluster.getAllOccurrences()[i].getTotalDist());
					Subsequence[][] expSub = expectedCluster.getAllOccurrences()[i].getSubsequences();
					Subsequence[][] actSub = actualCluster.getAllOccurrences()[i].getSubsequences();
					
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
				}
				for (int i=0; i<actualCluster.getOccurrences().length; i++){
					assertEquals(expectedCluster.getOccurrences()[i].getTotalDist(), actualCluster.getOccurrences()[i].getTotalDist());
					Subsequence[][] expSub = expectedCluster.getOccurrences()[i].getSubsequences();
					Subsequence[][] actSub = actualCluster.getOccurrences()[i].getSubsequences();
					
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
		
		//GeneCluster[] newClusters = GeckoInstance.getInstance().computeReferenceStatistics(computeGenomes, p, readClusters, GeckoInstance.getInstance());
		//compareClusters(res, newClusters, true);
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
		GeckoInstance geckoInstance = GeckoInstance.getInstance();
		geckoInstance.setGeckoInstanceData(data);
        List<GeneCluster> clusterWithPValue = geckoInstance.computeReferenceStatistics(clusters);
		
		compareClusters(clusters, clusterWithPValue, false);
	}
}
