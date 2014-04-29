package gecko2;

import gecko2.algo.ReferenceCluster;
import gecko2.algo.ReferenceClusterAlgorithm;
import gecko2.algorithm.Parameter;
import gecko2.testUtils.ExpectedDeltaLocationValues;
import gecko2.testUtils.ExpectedReferenceClusterValues;
import gecko2.testUtils.GeneClusterTestUtils;
import gecko2.testUtils.GeneClusterTestUtils.PValueComparison;
import gecko2.testUtils.ReferenceClusterTestSettings;
import gecko2.util.LibraryUtils;
import gecko2.util.LibraryUtils.PlatformNotSupportedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;
import java.util.zip.DataFormatException;

import static gecko2.testUtils.GeneClusterTestUtils.automaticGeneClusterTestFromFile;
import static gecko2.testUtils.GeneClusterTestUtils.compareReferenceClusters;

/**
 * The class tests the computeClusters algorithm from the Gecko2 program
 * 
 * @author Hans-Martin Haase <hans-martin.haase@uni-jena.de>
 * @version 0.54
 *
 */
public class ReferenceClusterTest 
{
    private static boolean libGeckoLoaded = false;
	/**
	 * The method adds libgecko2 to the library path.
	 * 
	 */
	@BeforeClass
	public static void loadLibGecko2()
	{
        try {
            LibraryUtils.loadLibrary("libgecko2");
            libGeckoLoaded = true;
        } catch (PlatformNotSupportedException | IOException e) {
            libGeckoLoaded = false;
        }
	}

    @Test
    public void testMemoryReduction()
    {
        // def array for computation
        int genomes[][][] = {{{0, 1, 2, -1, -1, -1, -1, 3, 4, 0}}, {{0, 3, 2, -1, 1, 4, 0}}};

        Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result (using p values from calculated result)
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 2, 1);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 2, 4, 0);
        List<Integer> genes1 = Arrays.asList(-1, 1, 2);
        int[] minimumDistances = new int[]{1, 0};

        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);
        
        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues = {{dLoc1_1},{dLoc1_2}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances,
                        1,
                        0,
                        2,
                        expectedDeltaLocationValues
                )
        };
        
        

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
    }

	@Test
	public void testComputeClusters1() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}}, {{0, 1, 2, 5, 4, 0}}};
			
		// def parameters
		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result (using p values from calculated result)
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances = new int[]{0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues = {{dLoc1_1},{dLoc1_2}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances,
                        0,
                        0,
                        2,
                        expectedDeltaLocationValues
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

    @Test
    public void testComputeClusters1_memoryReduction()
    {
        // def array for computation
        int genomes[][][] = {{{0, 1, 2, 3, -1, 0}}, {{0, 1, 2, 3, -1, 0}}};

        // def parameters
        Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result (using p values from calculated result)
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        List<Integer> genes1 = Arrays.asList(1, 2, 3);
        int[] minimumDistances = new int[]{0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues = {{dLoc1_1},{dLoc1_2}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances,
                        0,
                        0,
                        2,
                        expectedDeltaLocationValues
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
    }

	@Test
	public void testComputeClusters2()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}}, {{0, 1, 2, 6, 5, 4, 0}}};

		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

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
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes2);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters2_memoryReduction()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 3, -1, 0}}, {{0, 1, 2, -1, 3, -1, 0}}};

        Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 4, 1);
        List<Integer> genes1 = Arrays.asList(1, 2, 3);
        int[] minimumDistances1 = new int[]{0, 1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(0, 1, 3, 1);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 1, 4, 0);
        List<Integer> genes2 = Arrays.asList(-1, 1, 2, 3);
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
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters3()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}, {0, 3, 3, 1, 2, 5, 6, 0}}, {{0, 1, 2, 5, 4, 0}}};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances1 = new int[]{0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(1, 3, 5, 0);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        List<Integer> genes2 = Arrays.asList(1, 2, 5);
        int[] minimumDistances2 = new int[]{0, 0};

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
                        0,
                        1,
                        2,
                        expectedDeltaLocationValues2
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes2);
        
        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters3InvertedGenomes()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 4, 0}}, {{0, 1, 2, 5, 3, 0}, {0, 3, 3, 1, 2, 5, 6, 0}}};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result (using p values from calculated result)
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_3 = new ExpectedDeltaLocationValues(1, 3, 5, 0);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances = new int[]{0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues = {{dLoc1_1},{dLoc1_2, dLoc1_3}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances,
                        1,
                        0,
                        2,
                        expectedDeltaLocationValues
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters3WithInvertedGenomesSingleRef()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 4, 0}}, {{0, 1, 2, 5, 3, 0}, {0, 3, 3, 1, 2, 5, 6, 0}}};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);

        // def result (using p values from calculated result)
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_3 = new ExpectedDeltaLocationValues(1, 3, 5, 0);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances = new int[]{0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues = {{dLoc1_1},{dLoc1_2, dLoc1_3}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances,
                        0,
                        0,
                        2,
                        expectedDeltaLocationValues
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters4()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}, {0, 3, 8, 1, 2, 5, 6, 0}}, {{0, 9, 1, 2, 7, 5, 4, 0}}};

		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 2, 5, 1);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances1 = new int[]{0, 1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(1, 3, 5, 0);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 2, 5, 1);
        List<Integer> genes2 = Arrays.asList(1, 2, 5);
        int[] minimumDistances2 = new int[]{0, 1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues2 = {{dLoc2_1},{dLoc2_2}};

        // def result 3
        ExpectedDeltaLocationValues dLoc3_1 = new ExpectedDeltaLocationValues(0, 1, 3, 1);
        ExpectedDeltaLocationValues dLoc3_2 = new ExpectedDeltaLocationValues(1, 3, 5, 1);
        ExpectedDeltaLocationValues dLoc3_3 = new ExpectedDeltaLocationValues(0, 2, 5, 0);
        List<Integer> genes3 = Arrays.asList(1, 2, 5, 7);
        int[] minimumDistances3 = new int[]{1, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues3 = {{dLoc3_1, dLoc3_2},{dLoc3_3}};

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
                        1,
                        2,
                        expectedDeltaLocationValues2),
                new ExpectedReferenceClusterValues(
                        genes3,
                        minimumDistances3,
                        1,
                        0,
                        2,
                        expectedDeltaLocationValues3
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters5()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}, {0, 3, 8, 1, 2, 5, 6, 0}}, {{0, 9, 1, 2, 5, 4, 0}, {0,11, 10, 7, 2, 1, 5, 0}}};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 2, 4, 0);
        ExpectedDeltaLocationValues dLoc1_3 = new ExpectedDeltaLocationValues(1, 4, 6, 0);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances1 = new int[]{0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2, dLoc1_3}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(1, 3, 5, 0);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 2, 4, 0);
        ExpectedDeltaLocationValues dLoc2_3 = new ExpectedDeltaLocationValues(1, 4, 6, 0);
        List<Integer> genes2 = Arrays.asList(1, 2, 5);
        int[] minimumDistances2 = new int[]{0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues2 = {{dLoc2_1},{dLoc2_2, dLoc2_3}};

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
                        1,
                        2,
                        expectedDeltaLocationValues2
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters6()
	{
		// def array for computationsub2, sub4
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}, {0, 3, 10, 1, 2, 5, 6, 0}}, {{0, 9, 1, 2, 8, 5, 4, 0}, {0, 7, 11, 11, 2, 1, 12, 5, 0}}};

		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 2, 5, 1);
        ExpectedDeltaLocationValues dLoc1_3 = new ExpectedDeltaLocationValues(1, 4, 7, 1);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances1 = new int[]{0, 1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2, dLoc1_3}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(1, 3, 5, 0);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 2, 5, 1);
        ExpectedDeltaLocationValues dLoc2_3 = new ExpectedDeltaLocationValues(1, 4, 7, 1);
        List<Integer> genes2 = Arrays.asList(1, 2, 5);
        int[] minimumDistances2 = new int[]{0, 1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues2 = {{dLoc2_1},{dLoc2_2, dLoc2_3}};

        // def result 3
        ExpectedDeltaLocationValues dLoc3_1 = new ExpectedDeltaLocationValues(0, 1, 3, 1);
        ExpectedDeltaLocationValues dLoc3_2 = new ExpectedDeltaLocationValues(1, 3, 5, 1);
        ExpectedDeltaLocationValues dLoc3_3 = new ExpectedDeltaLocationValues(0, 2, 5, 0);
        List<Integer> genes3 = Arrays.asList(1, 2, 5, 8);
        int[] minimumDistances3 = new int[]{1, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues3 = {{dLoc3_1, dLoc3_2},{dLoc3_3}};

        // def result 3
        ExpectedDeltaLocationValues dLoc4_1 = new ExpectedDeltaLocationValues(0, 1, 3, 1);
        ExpectedDeltaLocationValues dLoc4_2 = new ExpectedDeltaLocationValues(1, 3, 5, 1);
        ExpectedDeltaLocationValues dLoc4_3 = new ExpectedDeltaLocationValues(1, 4, 7, 0);
        List<Integer> genes4 = Arrays.asList(1, 2, 5, 12);
        int[] minimumDistances4 = new int[]{1, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues4 = {{dLoc4_1, dLoc4_2},{dLoc4_3}};

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
                        1,
                        2,
                        expectedDeltaLocationValues2),
                new ExpectedReferenceClusterValues(
                        genes3,
                        minimumDistances3,
                        1,
                        0,
                        2,
                        expectedDeltaLocationValues3),
                new ExpectedReferenceClusterValues(
                        genes4,
                        minimumDistances4,
                        1,
                        1,
                        2,
                        expectedDeltaLocationValues4
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters7()
	{
		// def array for computationsub2, sub4
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}}, {{0, 9, 1, 2, 5, 4, 0}}, {{0, 8, 10, 1, 2, 5, 11, 6, 7, 0}}};

		Parameter p = new Parameter(0, 3, 3, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result (using p values from calculated result)
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 2, 4, 0);
        ExpectedDeltaLocationValues dLoc1_3 = new ExpectedDeltaLocationValues(0, 3, 5, 0);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances = new int[]{0, 0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues = {{dLoc1_1},{dLoc1_2}, {dLoc1_3}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances,
                        0,
                        0,
                        3,
                        expectedDeltaLocationValues
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters8()
	{
		// def array for computation
		int genomes[][][] = {{{0,13, 4, 12, 1, 2, 5, 3, 0}}, {{0, 9, 1, 2, 5, 6, 4, 0}}, {{0, 8, 10, 1, 2, 7, 5, 11,0}}};

		Parameter p = new Parameter(1, 3, 3, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 4, 6, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 2, 4, 0);
        ExpectedDeltaLocationValues dLoc1_3 = new ExpectedDeltaLocationValues(0, 3, 6, 1);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances1 = new int[]{0, 0, 1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2}, {dLoc1_3}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(0, 4, 6, 1);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 2, 4, 1);
        ExpectedDeltaLocationValues dLoc2_3 = new ExpectedDeltaLocationValues(0, 3, 6, 0);
        List<Integer> genes2 = Arrays.asList(1, 2, 5, 7);
        int[] minimumDistances2 = new int[]{1, 1, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues2 = {{dLoc2_1},{dLoc2_2}, {dLoc2_3}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances1,
                        1,
                        0,
                        3,
                        expectedDeltaLocationValues1),
                new ExpectedReferenceClusterValues(
                        genes2,
                        minimumDistances2,
                        2,
                        0,
                        3,
                        expectedDeltaLocationValues2
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters9()
	{
		// def array for computation
		int genomes[][][] = {{{0,13, 4, 12, 1, 2, 5, 3, 0}}, {{0, 9, 1, 2, 5, 6, 4, 0}}, {{0, 8, 10, 1, 2, 7, 5, 11,0}}};

		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 4, 6, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 2, 4, 0);
        ExpectedDeltaLocationValues dLoc1_3 = new ExpectedDeltaLocationValues(0, 3, 6, 1);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances1 = new int[]{0, 0, 1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2}, {dLoc1_3}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(0, 4, 6, 1);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 2, 4, 1);
        ExpectedDeltaLocationValues dLoc2_3 = new ExpectedDeltaLocationValues(0, 3, 6, 0);
        List<Integer> genes2 = Arrays.asList(1, 2, 5, 7);
        int[] minimumDistances2 = new int[]{1, 1, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues2 = {{dLoc2_1},{dLoc2_2}, {dLoc2_3}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances1,
                        1,
                        0,
                        3,
                        expectedDeltaLocationValues1),
                new ExpectedReferenceClusterValues(
                        genes2,
                        minimumDistances2,
                        2,
                        0,
                        3,
                        expectedDeltaLocationValues2
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters10()
	{
		// def array for computation
		int genomes[][][] = {{{0,13, 4, 12, 1, 2, 5, 3, 0}}, {{0, 9, 1, 2, 5, 6, 4, 0}}, {{0, 8, 10, 1, 2, 7, 5, 11,0}}};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result (using p values from calculated result)
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 4, 6, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 2, 4, 0);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances = new int[]{0, 0, -1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues = {{dLoc1_1},{dLoc1_2}, {}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances,
                        1,
                        0,
                        2,
                        expectedDeltaLocationValues
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters11()
	{
		// def array for computation
		int genomes[][][] = {{{0,13, 4, 12, 1, 2, 7, 5, 3, 0}}, {{0, 9, 1, 2, 5, 6, 4, 0}}, {{0, 8, 10, 1, 2, 5, 11,0}}};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result (using p values from calculated result)
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 2, 4, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 3, 5, 0);
        List<Integer> genes1 = Arrays.asList(1, 2, 5);
        int[] minimumDistances = new int[]{-1, 0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues = {{},{dLoc1_1}, {dLoc1_2}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances,
                        1,
                        0,
                        2,
                        expectedDeltaLocationValues
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters12()
	{
		// def array for computation
		int genomes[][][] = {{{0,13, 4, 12, 1, 2, 7, 5, 3, 0}}, {{0, 9, 1, 2, 5, 6, 4, 0}}, {{0, 8, 10, 1, 2, 5, 11,0}}};

		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 4, 7, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 2, 4, 1);
        ExpectedDeltaLocationValues dLoc1_3 = new ExpectedDeltaLocationValues(0, 3, 5, 1);
        List<Integer> genes1 = Arrays.asList(1, 2, 5, 7);
        int[] minimumDistances1 = new int[]{0, 1, 1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2}, {dLoc1_3}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(0, 4, 7, 1);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 2, 4, 0);
        ExpectedDeltaLocationValues dLoc2_3 = new ExpectedDeltaLocationValues(0, 3, 5, 0);
        List<Integer> genes2 = Arrays.asList(1, 2, 5);
        int[] minimumDistances2 = new int[]{1, 0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues2 = {{dLoc2_1},{dLoc2_2}, {dLoc2_3}};

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
                        1,
                        0,
                        3,
                        expectedDeltaLocationValues2
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters13()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 0}}, {{0, 1, 2, 0}}};

		// def parameters
		Parameter p = new Parameter(1, 2, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result (using p values from calculated result)
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 2, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 2, 0);
        List<Integer> genes1 = Arrays.asList(1, 2);
        int[] minimumDistances = new int[]{0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues = {{dLoc1_1}, {dLoc1_2}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances,
                        0,
                        0,
                        2,
                        expectedDeltaLocationValues
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClusters14()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 3, 0}}, {{0, 1, 2, 0}}, {{0, 2, 3, 0}}};

		// def parameters
		Parameter p = new Parameter(1, 3, 3, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 2, 0);
        List<Integer> genes1 = Arrays.asList(1, 2, 3);
        int[] minimumDistances1 = new int[]{0, 1, 1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(0, 1, 2, 0);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 1, 2, 0);
        List<Integer> genes2 = Arrays.asList(1, 2, 3);
        int[] minimumDistances2 = new int[]{1, 0, 0};

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
                        0,
                        0,
                        2,
                        expectedDeltaLocationValues2
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClustersRefInRef()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 1, 2, 0}}, {{0, 3, 4, 5, 4, 0}}};

		// def parameters
		Parameter p = new Parameter(0, 2, 1, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll, true);

        // def result (using p values from calculated result)
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 2, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 4, 5, 0);
        List<Integer> genes1 = Arrays.asList(1, 2);
        int[] minimumDistances = new int[]{0, -1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues = {{dLoc1_1, dLoc1_2}, {}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances,
                        0,
                        0,
                        1,
                        expectedDeltaLocationValues
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void testComputeClustersRefInRefWithErrors()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 3, 2, 5, 1, 2, 0}}, {{0, 3, 4, 5, 4, 0}}};

		// def parameters
		Parameter p = new Parameter(1, 3, 1, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll, true);

        // def result (using p values from calculated result)
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 3, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 5, 6, 1);
        List<Integer> genes1 = Arrays.asList(1, 2, 3);
        int[] minimumDistances = new int[]{1, -1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues = {{dLoc1_1, dLoc1_2}, {}};

        ExpectedReferenceClusterValues[] referenceClusterValues = {
                new ExpectedReferenceClusterValues(
                        genes1,
                        minimumDistances,
                        0,
                        0,
                        1,
                        expectedDeltaLocationValues
                )
        };
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}

	@Test
	public void fiveProteobacterReferenceClusterTest() throws URISyntaxException, IOException, DataFormatException, ParseException {
        ReferenceClusterTestSettings settings = ReferenceClusterTestSettings.fiveProteobacterD3S6Q4();
		automaticGeneClusterTestFromFile(settings, libGeckoLoaded);
	}

	@Test
	public void fiveProteobacterReferenceClusterTestWithGrouping() throws URISyntaxException, IOException, DataFormatException, ParseException {
        ReferenceClusterTestSettings settings = ReferenceClusterTestSettings.fiveProteobacterD3S6Q2Grouping();

		automaticGeneClusterTestFromFile(settings, libGeckoLoaded);
	}

	@Test
	public void testQuorumParamterJava()
	{
		// def array for computationsub2, sub4
		int genomes[][][] = {{{0, 1, 2, 3, 0}}, {{0, 1, 2, 3, 0}}, {{0, 1, 3, 0}}};

		// def parameters
		Parameter maxQuorumParamters = new Parameter(0, 3, 3, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

		// result of computation
		List<ReferenceCluster> maxQuorumResult = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, maxQuorumParamters);

		Parameter noQuorumParameters = new Parameter(0, 3, 0, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

		// result of computation
		List<ReferenceCluster> noQuorumResult = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, noQuorumParameters);

		compareReferenceClusters(maxQuorumResult, noQuorumResult, PValueComparison.COMPARE_ALL);
	}

	@Test
	public void testComputeClustersWithGroupedGenomes()
	{
		// def array for computationsub2, sub4
		int genomes[][][] = {{{0, 1, 2, 3, 8 ,9, 0}}, {{0, 1, 2, 4, 6, 7, 8, 9, 0}}, {{0, 1, 2, 5, 6, 7, 0}}};

		// def genome groups, grouping genomes 2 and 3
		List<Set<Integer>> genomeGroups = new ArrayList<>(2);
		Set<Integer> set1 = new HashSet<>();
		set1.add(0);
		genomeGroups.add(set1);
		Set<Integer> set2 = new HashSet<>();
		set2.add(1);
		set2.add(2);
		genomeGroups.add(set2);

		// def parameters
		Parameter p = new Parameter(0, 2, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);

        // def result 1
        ExpectedDeltaLocationValues dLoc1_1 = new ExpectedDeltaLocationValues(0, 1, 2, 0);
        ExpectedDeltaLocationValues dLoc1_2 = new ExpectedDeltaLocationValues(0, 1, 2, 0);
        ExpectedDeltaLocationValues dLoc1_3 = new ExpectedDeltaLocationValues(0, 1, 2, 0);
        List<Integer> genes1 = Arrays.asList(1, 2);
        int[] minimumDistances1 = new int[]{0, 0, 0};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues1 = {{dLoc1_1},{dLoc1_2},{dLoc1_3}};

        // def result 2
        ExpectedDeltaLocationValues dLoc2_1 = new ExpectedDeltaLocationValues(0, 4, 5, 0);
        ExpectedDeltaLocationValues dLoc2_2 = new ExpectedDeltaLocationValues(0, 6, 7, 0);
        List<Integer> genes2 = Arrays.asList(8, 9);
        int[] minimumDistances2 = new int[]{0, 0, -1};

        ExpectedDeltaLocationValues[][] expectedDeltaLocationValues2 = {{dLoc2_1},{dLoc2_2}, {}};

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
        
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes1);
        ReferenceClusterAlgorithm.memReducer(genomes, p, genes2);

        GeneClusterTestUtils.performTest(p, genomes, referenceClusterValues);
	}
}

