package gecko2;

import gecko2.GeneClusterTestUtils.PValueComparison;
import gecko2.algo.ReferenceClusterAlgorithm;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.Parameter;
import gecko2.algorithm.Subsequence;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.zip.DataFormatException;

import static gecko2.GeneClusterTestUtils.automaticGeneClusterTestFromFile;
import static gecko2.GeneClusterTestUtils.performTest;

public class ReferenceClusterDistanceMatrixTest {
	
	@Test
	public void testComputeClustersConstMatrix() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}}, {{0, 1, 2, 6, 5, 4, 0}}};
			
		// def parameters
		int[][] distanceMatrix = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
		Parameter p = new Parameter(distanceMatrix, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
			
		// Test the java implementation
		GeneCluster[] res = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);
		
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 4, 0, 1, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence[][] subsequences = {{sub1},{sub2}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		int[] genes1 = {1, 2, 5};
		
		Subsequence sub3 = new Subsequence(1, 3, 0, 1, res[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub4 = new Subsequence(1, 4, 0, 0, res[1].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence[][] subsequences2 = {{sub3},{sub4}};
		
		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		int[] genes2 = {1, 2, 6, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes1, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									1, 
									0,
									'r'),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes2,
									res[1].getBestPValue(),
									res[1].getBestPValueCorrected(),
									1,
									1,
									'r')};
		
		performTest(refCluster, res, PValueComparison.COMPARE_NONE);	
	}
	
	@Test
	public void testComputeClusters3() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 4 ,3 , 0}}, {{0, 1, 2, 6, 5, 4, 0}}, {{0, 1, 2, 7, 5, 8, 3, 4, 0}}};
			
		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8};
		
		int[][] distanceMatrix = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {2, 2, 2}};
		Parameter p = new Parameter(distanceMatrix, 4, 2, Parameter.QUORUM_NO_COST, 'r', 'g');
		p.setAlphabetSize(geneLabelMap.length);
		
		// Test the java implementation
		GeneCluster[] res = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);
		
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 4, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 5, 0, 1, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence[][] subsequences = {{sub1},{sub2}, {}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		int[] genes1 = {1, 2, 5, 4};
		
		Subsequence sub3 = new Subsequence(1, 5, 0, 0, res[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub4 = new Subsequence(1, 5, 0, 2, res[1].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub5 = new Subsequence(1, 7, 0, 2, res[1].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());
		Subsequence[][] subsequences2 = {{sub3},{sub4},{sub5}};
		
		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getOccurrences()[0].getBestpValue(), 4, 3)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getAllOccurrences()[0].getBestpValue(), 4, 3)};
		
		int[] genes2 = {1, 2, 5, 4, 3};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes1, 
									res[0].getBestPValue(),
									res[0].getBestPValueCorrected(),
									1, 
									0,
									'r'),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes2,
									res[1].getBestPValue(),
									res[1].getBestPValueCorrected(),
									4,
									0,
									'r')};
		
		performTest(refCluster, res, PValueComparison.COMPARE_NONE);		
	}
	
	@Test
	public void testComputeClusters4() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5 , 4, 11, 3 , 10, 0}}, {{0, 1, 2, 6, 7, 8, 0}}, {{0, 3, 2, 1, 9, 4, 7, 5, 10, 0}}};
			
		// def parameters
		int[][] distanceMatrix = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {3, 3, 3}};
		Parameter p = new Parameter(distanceMatrix, 2, 2, Parameter.QUORUM_NO_COST, 'r', 'g');
			
		// Test the java implementation
		GeneCluster[] res = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);
		
		// def result (using p values from calculated result)
		Subsequence sub1_1 = new Subsequence(1, 2, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub1_2 = new Subsequence(1, 2, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub1_3 = new Subsequence(2, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());
		Subsequence[][] subsequences1 = {{sub1_1},{sub1_2}, {sub1_3}};
		
		GeneClusterOccurrence[] bestOccurrences1 = {new GeneClusterOccurrence(0, subsequences1, res[0].getOccurrences()[0].getBestpValue(), 0, 3)};
		GeneClusterOccurrence[] allOccurrences1 = {new GeneClusterOccurrence(0, subsequences1, res[0].getAllOccurrences()[0].getBestpValue(), 0, 3)};
		
		int[] genes1 = {1, 2};
		
		// def result (using p values from calculated result)
		Subsequence sub2_1 = new Subsequence(1, 7, 0, 0, res[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2_2 = new Subsequence(1, 8, 0, 3, res[1].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());
		Subsequence[][] subsequences2 = {{sub2_1}, {}, {sub2_2}};
		
		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getOccurrences()[0].getBestpValue(), 3, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getAllOccurrences()[0].getBestpValue(), 3, 2)};
		
		int[] genes2 = {1, 2, 5 , 4, 11, 3 , 10};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences1, allOccurrences1, genes1, 
										res[0].getBestPValue(), 
										res[0].getBestPValueCorrected(),
										0, 
										0,
										'r'),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes2,
											res[1].getBestPValue(),
											res[1].getBestPValueCorrected(),
											3,
											0,
											'r')};
		
		performTest(refCluster, res, PValueComparison.COMPARE_NONE);		
	}
	
	@Test
	public void testComputeClustersNoDeletions() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 3, 0}}, {{0, 1, 2, 4, 3, 0}}, {{0, 1, 3, 0}}};
			
		// def parameters
		int[][] distanceMatrix = {{1, 0, 1}, {1, 0, 1}, {1, 0, 1}, {1, 0, 1}};
		Parameter p = new Parameter(distanceMatrix, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'g');
			
		// Test the java implementation
		GeneCluster[] res = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);
		
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 4, 0, 1, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence[][] subsequences = {{sub1},{sub2}, {}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		int[] genes1 = {1, 2, 3};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes1, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									1, 
									0,
									'r')};
		
		performTest(refCluster, res, PValueComparison.COMPARE_NONE);		
	}
	
	@Test
	public void testComputeClustersNoInsertions() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 3, 4, 0}}, {{0, 1, 2, 5, 3, 0}}, {{0, 1, 3, 4, 0}}};
			
		// def parameters
		int[][] distanceMatrix = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 1, 1}};
		Parameter p = new Parameter(distanceMatrix, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'g');
			
		// Test the java implementation
		GeneCluster[] res = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);
		
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 4, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub3 = new Subsequence(1, 3, 0, 1, res[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());
		Subsequence[][] subsequences = {{sub1}, {}, {sub3}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		int[] genes1 = {1, 2, 3, 4};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes1, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									1, 
									0,
									'r')};
		
		performTest(refCluster, res, PValueComparison.COMPARE_NONE);		
	}
	
	@Test
	public void testComputeClustersHigherTotalDist() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 3, 0}}, {{0, 1, 4, 3, 0}}, {{0, 1, 4, 5, 3, 0}}};
			
		// def parameters
		int[][] distanceMatrix = {{1, 1, 2}, {1, 1, 2}, {1, 1, 2}, {1, 1, 2}};
		Parameter p = new Parameter(distanceMatrix, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'g');
			
		// Test the java implementation
		GeneCluster[] res = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);
		
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 3, 0, 2, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence[][] subsequences = {{sub1},{sub2}, {}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 2, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 2, 2)};
		
		int[] genes1 = {1, 2, 3};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes1, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									2, 
									0,
									'r')};
		
		performTest(refCluster, res, PValueComparison.COMPARE_NONE);		
	}
	
	/**
	 * For the given genomes and parameters, delta table and d=5, s=8 should give equal results
	 */
	@Test
	public void testComputeClustersD5S8_DeltaTable() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 3, 4, 5, 6, 7 , 8, 0}},
				{{0, 4, 5, 6, 7, 8, 0}},
				{{0, 7, 6, 5, 4, 0}},
				{{0, 7, 6, 5, 4, 9, 1, 0}},
				{{0, 4, 5, 8, 0}},
				{{0, 4, 5, 1, 0}},
				{{0, 7, 6, 5, 4, 10, 1, 0}}};
		
		// def parameters
		Parameter p = new Parameter(5, 8, genomes.length-1, Parameter.QUORUM_NO_COST, 'r', 'g');
		
		int[][] deltaTable = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 1, 1}, {2, 2, 2}, {3, 3, 3}, {5, 5, 5}};
		Parameter p_deltaTable = new Parameter(deltaTable, 4, genomes.length-1, Parameter.QUORUM_NO_COST, 'r', 'g');
			
		// Test the java implementation
		GeneCluster[] deltaTableRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p_deltaTable);
		GeneCluster[] res = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);
		
		// def result (using p values from calculated result)
		
		performTest(deltaTableRes, res, PValueComparison.COMPARE_NONE);		
	}
	
	
	
	@Test
	public void fiveProteobacterReferenceClusterWithDistanceMatrixTest() throws URISyntaxException, IOException, DataFormatException, ParseException {
		File inputFile = new File(ReferenceClusterTest.class.getResource("/fiveProteobacter.cog").toURI());
		File resultFile = new File(ReferenceClusterTest.class.getResource("/fiveProteobacterDeltaTable.txt").toURI());
		
		automaticGeneClusterTestFromFile(inputFile, resultFile);
	}
}
