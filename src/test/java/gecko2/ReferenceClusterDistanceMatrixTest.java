package gecko2;

import gecko2.algo.ReferenceCluster;
import gecko2.algo.ReferenceClusterAlgorithm;
import gecko2.algorithm.Parameter;
import gecko2.testUtils.ExpectedDeltaLocationValues;
import gecko2.testUtils.ExpectedReferenceClusterValues;
import gecko2.testUtils.GeneClusterTestUtils;
import gecko2.testUtils.GeneClusterTestUtils.PValueComparison;
import gecko2.testUtils.ReferenceClusterTestSettings;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;

import static gecko2.testUtils.GeneClusterTestUtils.automaticGeneClusterTestFromFile;

public class ReferenceClusterDistanceMatrixTest {
	
	@Test
	public void testComputeClustersConstMatrix() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}}, {{0, 1, 2, 6, 5, 4, 0}}};
			
		// def parameters
		int[][] distanceMatrix = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
		Parameter p = new Parameter(distanceMatrix, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 4, 1);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances1 = new int[]{0, 1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(0, 1, 3, 1);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 1, 4, 0);
        List<Integer> genes2 = Arrays.asList(1, 2, 5, 6);
        int[] minimumDistances2 = new int[]{1, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues2 = {{dLoc2_1},{dLoc2_2}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances1,
                        0,
                        0,
                        2,
                        expectedDeltaLocationValues1),
                new ExpectedReferenceClusterValues(
                        genes2,
                        minimumDistances2,
                        1,
                        0,
                        2,
                        expectedDeltaLocationValues2
                )
        };

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}
	
	@Test
	public void testComputeClusters3() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 4 ,3 , 0}}, {{0, 1, 2, 6, 5, 4, 0}}, {{0, 1, 2, 7, 5, 8, 3, 4, 0}}};
		
		int[][] distanceMatrix = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {2, 2, 2}};
		Parameter p = new Parameter(distanceMatrix, 4, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 4, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 5, 1);
        List<Integer> genes1 = Arrays.asList(1, 2, 4, 5);
        int[] minimumDistances1 = new int[]{0, 1, -1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2},{}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(0, 1, 5, 0);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 1, 5, 2);
        ExpectedDeltaLocationValues dLoc2_3 = new ExpectedDeltaLocationValues(0, 1, 7, 2);
        List<Integer> genes2 = Arrays.asList(1, 2, 3, 4, 5);
        int[] minimumDistances2 = new int[]{0, 2, 2};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues2 = {{dLoc2_1},{dLoc2_2},{dLoc2_3}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances1,
                        0,
                        0,
                        2,
                        expectedDeltaLocationValues1),
                new ExpectedReferenceClusterValues(
                        genes2,
                        minimumDistances2,
                        0,
                        0,
                        3,
                        expectedDeltaLocationValues2
                )
        };

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}
	
	@Test
	public void testComputeClusters4() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5 , 4, 11, 3 , 10, 0}}, {{0, 1, 2, 6, 7, 8, 0}}, {{0, 3, 2, 1, 9, 4, 7, 5, 10, 0}}};
			
		// def parameters
		int[][] distanceMatrix = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 1, 1}, {1, 1, 1}, {1, 1, 1}, {3, 3, 3}};
		Parameter p = new Parameter(distanceMatrix, 2, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 2, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 2, 0);
        ExpectedDeltaLocationValues dLoc1_3 = new ExpectedDeltaLocationValues(0, 2, 3, 0);
        List<Integer> genes1 = Arrays.asList(1, 2);
        int[] minimumDistances1 = new int[]{0, 0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2},{dLoc1_3}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(0, 1, 7, 0);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 1, 8, 3);
        List<Integer> genes2 = Arrays.asList(1, 2, 3, 4, 5, 10, 11);
        int[] minimumDistances2 = new int[]{0, -1, 3};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues2 = {{dLoc2_1},{},{dLoc2_2}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances1,
                        0,
                        0,
                        3,
                        expectedDeltaLocationValues1),
                new ExpectedReferenceClusterValues(
                        genes2,
                        minimumDistances2,
                        0,
                        0,
                        2,
                        expectedDeltaLocationValues2
                )
        };

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}
	
	@Test
	public void testComputeClustersNoDeletions() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 3, 0}}, {{0, 1, 2, 4, 3, 0}}, {{0, 1, 3, 0}}};
			
		// def parameters
		int[][] distanceMatrix = {{1, 0, 1}, {1, 0, 1}, {1, 0, 1}, {1, 0, 1}};
		Parameter p = new Parameter(distanceMatrix, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 4, 1);
        List<Integer> genes1 = Arrays.asList(1, 2, 3);
        int[] minimumDistances1 = new int[]{0, 1, -1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2},{}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances1,
                        0,
                        0,
                        2,
                        expectedDeltaLocationValues1
                )
        };

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}
	
	@Test
	public void testComputeClustersNoInsertions() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 3, 4, 0}}, {{0, 1, 2, 5, 3, 0}}, {{0, 1, 3, 4, 0}}};
			
		// def parameters
		int[][] distanceMatrix = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 1, 1}};
		Parameter p = new Parameter(distanceMatrix, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 4, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 3, 1);
        List<Integer> genes1 = Arrays.asList(1, 2, 3, 4);
        int[] minimumDistances1 = new int[]{0, -1, 1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{},{dLoc1_2}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances1,
                        0,
                        0,
                        2,
                        expectedDeltaLocationValues1
                )
        };

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}
	
	@Test
	public void testComputeClustersHigherTotalDist() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 3, 0}}, {{0, 1, 4, 3, 0}}, {{0, 1, 4, 5, 3, 0}}};
			
		// def parameters
		int[][] distanceMatrix = {{1, 1, 2}, {1, 1, 2}, {1, 1, 2}, {1, 1, 2}};
		Parameter p = new Parameter(distanceMatrix, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 3, 2);
        List<Integer> genes1 = Arrays.asList(1, 2, 3);
        int[] minimumDistances1 = new int[]{0, 2, -1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2},{}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances1,
                        0,
                        0,
                        2,
                        expectedDeltaLocationValues1
                )
        };

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
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
		Parameter p = new Parameter(5, 8, genomes.length-1, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);
		
		int[][] deltaTable = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 1, 1}, {2, 2, 2}, {3, 3, 3}, {5, 5, 5}};
		Parameter p_deltaTable = new Parameter(deltaTable, 4, genomes.length-1, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);
			
		// Test the java implementation
		List<ReferenceCluster> deltaTableRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p_deltaTable);
        List<ReferenceCluster> res = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);
		
		// def result (using p values from calculated result)
		
		GeneClusterTestUtils.compareReferenceClusters(deltaTableRes, res, PValueComparison.COMPARE_NONE);
	}
	
	
	
	@Test
	public void fiveProteobacterReferenceClusterWithDistanceMatrixTest() throws IOException, DataFormatException, ParseException {
        ReferenceClusterTestSettings settings = ReferenceClusterTestSettings.fiveProteobacterDeltaTable();

        automaticGeneClusterTestFromFile(settings, false);
	}
}
