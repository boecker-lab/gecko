package gecko2.io;

import static gecko2.GeneClusterTestUtils.*;
import static org.junit.Assert.*;
import gecko2.GeckoInstance;
import gecko2.GenomeOccurence;
import gecko2.LibraryUtils;
import gecko2.LibraryUtils.PlatformNotSupportedException;
import gecko2.LinePassedException;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;
import gecko2.algorithm.Parameter;
import gecko2.algorithm.Subsequence;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClusterAnnotationReaderTest {
	public static File annotationFile = null;
	public static Genome[] genomes = null;
	
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
	
	@Before
	public void setUp() {
		try {
			CogFileReader reader = new CogFileReader((byte) 1);
			File inputFile = new File(ClusterAnnotationReader.class.getResource("/smallTest.cog").toURI());
			GeckoInstance.getInstance().setCurrentInputFile(inputFile);

			ArrayList<GenomeOccurence> genOcc = reader.importGenomes(inputFile);

			reader.readFileContent(genOcc);
			genomes = reader.getGenomes();
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LinePassedException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private void compareClusters(GeneCluster[] expected, GeneCluster[] actual, boolean comparePValues){
		assertEquals(expected.length, actual.length);
		for (GeneCluster actualCluster : actual){
			boolean match = false;
			
			for (GeneCluster expectedCluster: expected) {
				if (expectedCluster.getMinTotalDist() != actualCluster.getMinTotalDist())
					continue;
				if (expectedCluster.getRefSeqIndex() != actualCluster.getRefSeqIndex())
					continue;
				
				Set<Integer> expGenes = new HashSet<Integer>();
				for (Integer i : expectedCluster.getGenes())
					expGenes.add(i);
				Set<Integer> actGenes = new HashSet<Integer>();
				for (Integer i : actualCluster.getGenes())
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
			assertTrue(String.format("No matching cluster for %s found!", actual.toString()), match);
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
	
	public void readAnnotationsTest(File annotationFile){
		List<GeneCluster> clusters = ClusterAnnotationReader.readClusterAnnotations(annotationFile, genomes);
		assertNotNull(clusters);
		assertEquals(12, clusters.size());
		
		int computeGenomes[][][] = new int[genomes.length][][];
		for (int i=0;i<computeGenomes.length;i++) {
			computeGenomes[i] = new int[genomes[i].getChromosomes().size()][];
			for (int j=0;j<computeGenomes[i].length;j++)
				computeGenomes[i][j] = genomes[i].getChromosomes().get(j).toIntArray(true, true);
		}
		Parameter p = new Parameter(1, 4, 3, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(13);
		GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(computeGenomes, p);
		
		GeneCluster[] readClusters = clusters.toArray(new GeneCluster[clusters.size()]);
		
		compareClusters(res, readClusters, false);
		
		GeneCluster[] newClusters = GeckoInstance.getInstance().computeReferenceStatistics(computeGenomes, p, readClusters, GeckoInstance.getInstance());
		compareClusters(res, newClusters, true);
	}
	
	@Test
	public void geckoInstanceIntegrationTest(){
		File annotationFile = null;
		try {
			annotationFile = new File(ClusterAnnotationReader.class.getResource("/smallTestD1S4Q3.txt").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		List<GeneCluster> clusters = ClusterAnnotationReader.readClusterAnnotations(annotationFile, genomes);
		assertNotNull(clusters);
		assertEquals(12, clusters.size());
		GeckoInstance geckoInstance = GeckoInstance.getInstance();
		geckoInstance.setGenomes(genomes);
		GeneCluster[] clusterWithPValue = geckoInstance.computeReferenceStatistics(clusters.toArray(new GeneCluster[clusters.size()]));
		
		compareClusters(clusters.toArray(new GeneCluster[clusters.size()]), clusterWithPValue, false);
	}
}
