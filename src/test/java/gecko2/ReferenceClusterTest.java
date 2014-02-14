package gecko2;

import gecko2.GeneClusterTestUtils.PValueComparison;
import gecko2.algo.ReferenceClusterAlgorithm;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.Parameter;
import gecko2.algorithm.Subsequence;
import gecko2.util.LibraryUtils;
import gecko2.util.LibraryUtils.PlatformNotSupportedException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import static gecko2.GeneClusterTestUtils.automaticGeneClusterTestFromFile;
import static gecko2.GeneClusterTestUtils.performTest;

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
	
	
	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 * 
	 * Parameter set:
	 * 		genomes: 2 (one chromosome)
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: d
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 * 
	 */
	@Test
	public void testComputeClusters1() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}}, {{0, 1, 2, 5, 4, 0}}};
			
		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5};
			
		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence[][] subsequences = {{sub1},{sub2}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};

		int[] genes = {1, 2, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
                javaRes[0].getBestPValue(),
                javaRes[0].getBestPValueCorrected(),
                0,
                0,
                Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}


	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 2 (one chromosome)
	 * 		cluster size: 3
	 * 		delta: 1
	 * 		operation mode: r
	 * 		refType: d
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters2()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}}, {{0, 1, 2, 6, 5, 4, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6};

		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 4, 0, 1, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence[][] subsequences = {{sub1},{sub2}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 1, 2)};

		int[] genes1 = {1, 2, 5};

		Subsequence sub3 = new Subsequence(1, 3, 0, 1, javaRes[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub4 = new Subsequence(1, 4, 0, 0, javaRes[1].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence[][] subsequences2 = {{sub3},{sub4}};

		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getAllOccurrences()[0].getBestpValue(), 1, 2)};

		int[] genes2 = {1, 2, 6, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes1,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									1,
									0,
									Parameter.OperationMode.reference),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes2,
									javaRes[1].getBestPValue(),
									javaRes[1].getBestPValueCorrected(),
									1,
									1,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }

	}



	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 2 (one with two chromosomes)
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: d
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters3()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}, {0, 3, 3, 1, 2, 5, 6, 0}}, {{0, 1, 2, 5, 4, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 1, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());

		Subsequence[][] subsequences = {{sub1},{sub2}};
		Subsequence[][] subsequences2 = {{sub3}, {sub2}};


		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};

		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getAllOccurrences()[0].getBestpValue(), 0, 2)};

		int[] genes = {1, 2, 5};
		int[] genes2 = {1, 2, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									0,
									0,
									Parameter.OperationMode.reference),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes2,
									javaRes[1].getBestPValue(),
									javaRes[1].getBestPValueCorrected(),
									0,
									0,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}

	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 2 (one with two chromosomes)
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: d
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters3InvertedGenomes()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 4, 0}}, {{0, 1, 2, 5, 3, 0}, {0, 3, 3, 1, 2, 5, 6, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 1, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][1].getpValue());

		Subsequence[][] subsequences = {{sub1},{sub2, sub3}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};

		int[] genes = {1, 2, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									0,
									1,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}

	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 2 (one with two chromosomes)
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: g
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters3WithInvertedGenomesSingleRef()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 4, 0}}, {{0, 1, 2, 5, 3, 0}, {0, 3, 3, 1, 2, 5, 6, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.genome);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 1, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][1].getpValue());

		Subsequence[][] subsequences = {{sub1},{sub2, sub3}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};

		int[] genes = {1, 2, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									0,
									0,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}

	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 2 (one with two chromosomes)
	 * 		cluster size: 3
	 * 		delta: 1
	 * 		operation mode: r
	 * 		refType: d
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters4()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}, {0, 3, 8, 1, 2, 5, 6, 0}}, {{0, 9, 1, 2, 7, 5, 4, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9};

		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 5, 0, 1, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 1, 0, javaRes[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub4 = new Subsequence(2, 5, 0, 0, javaRes[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub5 = new Subsequence(1, 3, 0, 1, javaRes[2].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub6 = new Subsequence(3, 5, 1, 1, javaRes[2].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());

		Subsequence[][] subsequences = {{sub1},{sub2}};
		Subsequence[][] subsequences2 = {{sub3}, {sub2}};
		Subsequence[][] subsequences3 = {{sub5, sub6}, {sub4}};
		//Subsequence[][] subsequences4 = {{sub4}, {sub6}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 1, 2)};

		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getAllOccurrences()[0].getBestpValue(), 1, 2)};

		GeneClusterOccurrence[] bestOccurrences3 = {new GeneClusterOccurrence(0, subsequences3, javaRes[2].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences3 = {new GeneClusterOccurrence(0, subsequences3, javaRes[2].getAllOccurrences()[0].getBestpValue(), 1, 2)};

		int[] genes = {1, 2, 5};
		int[] genes2 = {1, 2, 7, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									1,
									0,
									Parameter.OperationMode.reference),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes,
									javaRes[1].getBestPValue(),
									javaRes[1].getBestPValueCorrected(),
									1,
									0,
									Parameter.OperationMode.reference),
									new GeneCluster(2, bestOccurrences3, allOccurrences3, genes2,
									javaRes[2].getBestPValue(),
									javaRes[2].getBestPValueCorrected(),
									1,
									1,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}


	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 2 (with two chromosomes)
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: d
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters5()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}, {0, 3, 8, 1, 2, 5, 6, 0}}, {{0, 9, 1, 2, 5, 4, 0}, {0,11, 10, 7, 2, 1, 5, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 1, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub4 = new Subsequence(4, 6, 1, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][1].getpValue());


		Subsequence[][] subsequences = {{sub1},{sub2, sub4}};
		Subsequence[][] subsequences2 = {{sub3}, {sub2, sub4}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};

		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getAllOccurrences()[0].getBestpValue(), 0, 2)};

		int[] genes = {1, 2, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									0,
									0,
									Parameter.OperationMode.reference),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes,
									javaRes[1].getBestPValue(),
									javaRes[1].getBestPValueCorrected(),
									0,
									0,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}


	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 2 (with two chromosomes)
	 * 		cluster size: 3
	 * 		delta: 1
	 * 		operation mode: r
	 * 		refType: dsub2, sub4
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters6()
	{
		// def array for computationsub2, sub4
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}, {0, 3, 10, 1, 2, 5, 6, 0}}, {{0, 9, 1, 2, 8, 5, 4, 0}, {0, 7, 11, 11, 2, 1, 12, 5, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,12};

		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 5, 0, 1, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 1, 0, javaRes[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub4 = new Subsequence(4, 7, 1, 1, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][1].getpValue());

		Subsequence sub5 = new Subsequence(1, 3, 0, 1, javaRes[2].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub6 = new Subsequence(2, 5, 0, 0, javaRes[2].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub7 = new Subsequence(3, 5, 1, 1, javaRes[3].getAllOccurrences()[0].getSubsequences()[0][1].getpValue());
		Subsequence sub8 = new Subsequence(4, 7, 1, 0, javaRes[3].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());

		Subsequence[][] subsequences = {{sub1} ,{sub2, sub4}};
		Subsequence[][] subsequences2 = {{sub3}, {sub2, sub4}};
		Subsequence[][] subsequences3 = {{sub5, sub7}, {sub6}};
		Subsequence[][] subsequences4 = {{sub5, sub7}, {sub8}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 1, 2)};

		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getAllOccurrences()[0].getBestpValue(), 1, 2)};

		GeneClusterOccurrence[] bestOccurrences3 = {new GeneClusterOccurrence(0, subsequences3, javaRes[2].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences3 = {new GeneClusterOccurrence(0, subsequences3, javaRes[2].getAllOccurrences()[0].getBestpValue(), 1, 2)};

		GeneClusterOccurrence[] bestOccurrences4 = {new GeneClusterOccurrence(0, subsequences4, javaRes[3].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences4 = {new GeneClusterOccurrence(0, subsequences4, javaRes[3].getAllOccurrences()[0].getBestpValue(), 1, 2)};

		int[] genes = {1, 2, 5};
		int[] genes2 = {1, 2, 8, 5};
		int[] genes3 = {2, 1, 12, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									1,
									0,
									Parameter.OperationMode.reference),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes,
									javaRes[1].getBestPValue(),
									javaRes[1].getBestPValueCorrected(),
									1,
									0,
									Parameter.OperationMode.reference),
									new GeneCluster(2, bestOccurrences3, allOccurrences3, genes2,
									javaRes[2].getBestPValue(),
									javaRes[2].getBestPValueCorrected(),
									1,
									1,
									Parameter.OperationMode.reference),
									new GeneCluster(3, bestOccurrences4, allOccurrences4, genes3,
									javaRes[3].getBestPValue(),
									javaRes[3].getBestPValueCorrected(),
									1,
									1,
									Parameter.OperationMode.reference)};


		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}



	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 3
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: 0
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 3
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters7()
	{
		// def array for computationsub2, sub4
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}}, {{0, 9, 1, 2, 5, 4, 0}}, {{0, 8, 10, 1, 2, 5, 11, 6, 7, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

		Parameter p = new Parameter(0, 3, 3, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());

		Subsequence[][] subsequences = {{sub1} ,{sub2}, {sub3}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 0, 3)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 0, 3)};



		int[] genes = {1, 2, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									0,
									0,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}



	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 3
	 * 		cluster size: 3
	 * 		delta: 1
	 * 		operation mode: r
	 * 		refType: 0
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 3
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters8()
	{
		// def array for computation
		int genomes[][][] = {{{0,13, 4, 12, 1, 2, 5, 3, 0}}, {{0, 9, 1, 2, 5, 6, 4, 0}}, {{0, 8, 10, 1, 2, 7, 5, 11,0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

		Parameter p = new Parameter(1, 3, 3, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(4, 6, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 6, 0, 1, javaRes[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());

		Subsequence sub7 = new Subsequence(4, 6, 0, 1, javaRes[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub8 = new Subsequence(2, 4, 0, 1, javaRes[1].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub9 = new Subsequence(3, 6, 0, 0, javaRes[1].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());


		Subsequence[][] subsequences = {{sub1} ,{sub2}, {sub3}};
		Subsequence[][] subsequences3 = {{sub7} ,{sub8}, {sub9}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 1, 3)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 1, 3)};

		GeneClusterOccurrence[] bestOccurrences1 = {new GeneClusterOccurrence(0, subsequences3, javaRes[1].getOccurrences()[0].getBestpValue(), 2, 3)};
		GeneClusterOccurrence[] allOccurrences1 = {new GeneClusterOccurrence(0, subsequences3, javaRes[1].getAllOccurrences()[0].getBestpValue(), 2, 3)};

		int[] genes = {1, 2, 5};
		int[] genes3 = {1, 2, 7, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									1,
									1,
									Parameter.OperationMode.reference),
									new GeneCluster(1, bestOccurrences1, allOccurrences1, genes3,
									javaRes[1].getBestPValue(),
									javaRes[1].getBestPValueCorrected(),
									2,
									2,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}


	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 3
	 * 		cluster size: 3
	 * 		delta: 1
	 * 		operation mode: r
	 * 		refType: 0
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2 (cluster isn't contained in genome 3)
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters9()
	{
		// def array for computation
		int genomes[][][] = {{{0,13, 4, 12, 1, 2, 5, 3, 0}}, {{0, 9, 1, 2, 5, 6, 4, 0}}, {{0, 8, 10, 1, 2, 7, 5, 11,0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(4, 6, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 6, 0, 1, javaRes[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());

		Subsequence sub7 = new Subsequence(4, 6, 0, 1, javaRes[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub8 = new Subsequence(2, 4, 0, 1, javaRes[1].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub9 = new Subsequence(3, 6, 0, 0, javaRes[1].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());


		Subsequence[][] subsequences = {{sub1} ,{sub2}, {sub3}};
		Subsequence[][] subsequences3 = {{sub7} ,{sub8}, {sub9}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 1, 3)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 1, 3)};

		GeneClusterOccurrence[] bestOccurrences1 = {new GeneClusterOccurrence(0, subsequences3, javaRes[1].getOccurrences()[0].getBestpValue(), 2, 3)};
		GeneClusterOccurrence[] allOccurrences1 = {new GeneClusterOccurrence(0, subsequences3, javaRes[1].getAllOccurrences()[0].getBestpValue(), 2, 3)};

		int[] genes = {1, 2, 5};
		int[] genes3 = {1, 2, 7, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									1,
									1,
									Parameter.OperationMode.reference),
									new GeneCluster(1, bestOccurrences1, allOccurrences1, genes3,
									javaRes[1].getBestPValue(),
									javaRes[1].getBestPValueCorrected(),
									2,
									2,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}


	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 3
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: 0
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2 (cluster isn't contained in genome 3)
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters10()
	{
		// def array for computation
		int genomes[][][] = {{{0,13, 4, 12, 1, 2, 5, 3, 0}}, {{0, 9, 1, 2, 5, 6, 4, 0}}, {{0, 8, 10, 1, 2, 7, 5, 11,0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(4, 6, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());

		Subsequence[][] subsequences = {{sub1} ,{sub2}, {}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};


		int[] genes = {1, 2, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									0,
									1,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}


	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 3
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: 0
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2 (cluster isn't contained in genome 1)
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters11()
	{
		// def array for computation
		int genomes[][][] = {{{0,13, 4, 12, 1, 2, 7, 5, 3, 0}}, {{0, 9, 1, 2, 5, 6, 4, 0}}, {{0, 8, 10, 1, 2, 5, 11,0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());

		Subsequence[][] subsequences = {{}, {sub2} ,{sub3}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};


		int[] genes = {1, 2, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									0,
									1,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}


	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 3
	 * 		cluster size: 3
	 * 		delta: 1
	 * 		operation mode: r
	 * 		refType: 0
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2 (cluster isn't contained in genome 3)
	 * 		contigSpanning: false
	 */
	@Test
	public void testComputeClusters12()
	{
		// def array for computation
		int genomes[][][] = {{{0,13, 4, 12, 1, 2, 7, 5, 3, 0}}, {{0, 9, 1, 2, 5, 6, 4, 0}}, {{0, 8, 10, 1, 2, 5, 11,0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(4, 7, 0, 1, javaRes[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, javaRes[1].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 0, 0, javaRes[1].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());


		Subsequence sub4 = new Subsequence(4, 7, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub5 = new Subsequence(2, 4, 0, 1, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub6 = new Subsequence(3, 5, 0, 1, javaRes[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());

		Subsequence[][] subsequences = {{sub1} ,{sub2}, {sub3}};
		Subsequence[][] subsequences2 = {{sub4}, {sub5}, {sub6}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[1].getOccurrences()[0].getBestpValue(), 1, 3)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[1].getAllOccurrences()[0].getBestpValue(), 1, 3)};

		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[0].getOccurrences()[0].getBestpValue(), 2, 3)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[0].getAllOccurrences()[0].getBestpValue(), 2, 3)};

		int[] genes = {1, 2, 5};
		int[] genes2 = {1, 2, 7, 5};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences2, allOccurrences2, genes2,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									2,
									0,
									Parameter.OperationMode.reference),
									new GeneCluster(1, bestOccurrences, allOccurrences, genes,
									javaRes[1].getBestPValue(),
									javaRes[1].getBestPValueCorrected(),
									1,
									1,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}

	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 2 (one chromosome)
	 * 		cluster size: 2
	 * 		delta: 1
	 * 		operation mode: r
	 * 		refType: d
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 */
	@Test
	public void testComputeClusters13()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 0}}, {{0, 1, 2, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2};

		Parameter p = new Parameter(1, 2, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 2, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 2, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence[][] subsequences = {{sub1},{sub2}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};

		int[] genes = {1, 2};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									0,
									0,
									Parameter.OperationMode.reference)};

		performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}

	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 2 (one with two chromosomes)
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: d
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClusters14()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 3, 0}}, {{0, 1, 2, 0}}, {{0, 2, 3, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3};

		Parameter p = new Parameter(1, 3, 3, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		/// Test the java implementation
        GeneCluster[] javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 2, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(1, 2, 0, 0, javaRes[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());

		Subsequence[][] subsequences = {{sub1},{sub2}};
		Subsequence[][] subsequences2 = {{sub3}, {sub2}};


		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, javaRes[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};

		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, javaRes[1].getAllOccurrences()[0].getBestpValue(), 0, 2)};

		int[] genes = {1, 2, 3};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
									javaRes[0].getBestPValue(),
									javaRes[0].getBestPValueCorrected(),
									0,
									0,
									Parameter.OperationMode.reference),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes,
									javaRes[1].getBestPValue(),
									javaRes[1].getBestPValueCorrected(),
									0,
									0,
									Parameter.OperationMode.reference)};

        performTest(refCluster, javaRes, PValueComparison.COMPARE_NONE);

        // test the c library
        if (libGeckoLoaded) {
            GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);
            performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
        }
	}

	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 2 (one chromosome)
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: d
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClustersRefInRef()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 5, 1, 2, 0}}, {{0, 3, 4, 5, 4, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5};
		Parameter p = new Parameter(0, 2, 1, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll, true);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] res = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 2, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(4, 5, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][1].getpValue());
		Subsequence[][] subsequences = {{sub1, sub2},{}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 0, 1)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 0, 1)};

		int[] genes = {1, 2};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
				res[0].getBestPValue(),
				res[0].getBestPValueCorrected(),
				0,
				0,
				Parameter.OperationMode.reference)};

		performTest(refCluster, res, PValueComparison.COMPARE_NONE);
	}

	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 2 (one chromosome)
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: d
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 2
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClustersRefInRefWithErrors()
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 3, 2, 5, 1, 2, 0}}, {{0, 3, 4, 5, 4, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5};

		Parameter p = new Parameter(1, 3, 1, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll, true);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] res = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(5, 6, 0, 1, res[0].getAllOccurrences()[0].getSubsequences()[0][1].getpValue());
		Subsequence[][] subsequences = {{sub1, sub2},{}};

		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 1, 1)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 1, 1)};

		int[] genes = {1, 3, 2};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes,
				res[0].getBestPValue(),
				res[0].getBestPValueCorrected(),
				1,
				0,
				Parameter.OperationMode.reference)};

		performTest(refCluster, res, PValueComparison.COMPARE_NONE);
	}

	@Test
	public void fiveProteobacterReferenceClusterTest() throws URISyntaxException, IOException, DataFormatException, ParseException {
		File inputFile = new File(ReferenceClusterTest.class.getResource("/fiveProteobacter.cog").toURI());
		File resultFile = new File(ReferenceClusterTest.class.getResource("/fiveProteobacterD3S6Q4.txt").toURI());

		automaticGeneClusterTestFromFile(inputFile, resultFile, libGeckoLoaded);
	}

	@Test
	public void fiveProteobacterReferenceClusterTestWithGrouping() throws URISyntaxException, IOException, DataFormatException, ParseException {
		File inputFile = new File(ReferenceClusterTest.class.getResource("/fiveProteobacter.cog").toURI());
		File resultFile = new File(ReferenceClusterTest.class.getResource("/fiveProteobacterD3S6Q2Grouping.txt").toURI());

		// def genome groups, grouping genomes 2 and 3 and 4 and 5
		List<Set<Integer>> genomeGroups = new ArrayList<Set<Integer>>(2);
		Set<Integer> set1 = new HashSet<Integer>();
		set1.add(0);
		genomeGroups.add(set1);
		Set<Integer> set2 = new HashSet<Integer>();
		set2.add(1);
		set2.add(2);
		genomeGroups.add(set2);
		Set<Integer> set3 = new HashSet<Integer>();
		set3.add(3);
		set3.add(4);
		genomeGroups.add(set3);

		automaticGeneClusterTestFromFile(inputFile, resultFile, genomeGroups, libGeckoLoaded);
	}

	@Test
	public void testQuorumParamterJava()
	{
		// def array for computationsub2, sub4
		int genomes[][][] = {{{0, 1, 2, 3, 0}}, {{0, 1, 2, 3, 0}}, {{0, 1, 3, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3};

		Parameter maxQuorumParamters = new Parameter(0, 3, 3, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		maxQuorumParamters.setAlphabetSize(geneLabelMap.length);

		// result of computation
		GeneCluster[] maxQuorumResult = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, maxQuorumParamters);

		Parameter noQuorumParameters = new Parameter(0, 3, 0, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		noQuorumParameters.setAlphabetSize(geneLabelMap.length);

		// result of computation
		GeneCluster[] noQuorumResult = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, noQuorumParameters);

		performTest(maxQuorumResult, noQuorumResult, PValueComparison.COMPARE_ALL);
	}

	@Test
	public void testQuorumParamter()
	{
		// def array for computationsub2, sub4
		int genomes[][][] = {{{0, 1, 2, 3, 0}}, {{0, 1, 2, 3, 0}}, {{0, 1, 3, 0}}};

		// def parameters
		int[] geneLabelMap = {1, 2, 3};

		Parameter maxQuorumParamters = new Parameter(0, 3, 3, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		maxQuorumParamters.setAlphabetSize(geneLabelMap.length);

		// result of computation
		GeneCluster[] maxQuorumResult = GeckoInstance.getInstance().computeClustersLibgecko(genomes, maxQuorumParamters);

		Parameter noQuorumParameters = new Parameter(0, 3, 0, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		noQuorumParameters.setAlphabetSize(geneLabelMap.length);

		// result of computation
		GeneCluster[] noQuorumResult = GeckoInstance.getInstance().computeClustersLibgecko(genomes, noQuorumParameters);

		performTest(maxQuorumResult, noQuorumResult, PValueComparison.COMPARE_ALL);
	}

	/**
	 * Method for testing the computeClusters method which is provided by the external library libgecko2
	 *
	 * Parameter set:
	 * 		genomes: 3
	 * 		cluster size: 3
	 * 		delta: 0
	 * 		operation mode: r
	 * 		refType: 0
	 * 		qtype: QUORUM_NO_COST
	 * 		q (number of genomes where cluster appears): 3
	 * 		contigSpanning: false
	 *
	 */
	@Test
	public void testComputeClustersWithGroupedGenomes()
	{
		// def array for computationsub2, sub4
		int genomes[][][] = {{{0, 1, 2, 3, 8 ,9, 0}}, {{0, 1, 2, 4, 6, 7, 8, 9, 0}}, {{0, 1, 2, 5, 6, 7, 0}}};

		// def genome groups, grouping genomes 2 and 3
		List<Set<Integer>> genomeGroups = new ArrayList<Set<Integer>>(2);
		Set<Integer> set1 = new HashSet<Integer>();
		set1.add(0);
		genomeGroups.add(set1);
		Set<Integer> set2 = new HashSet<Integer>();
		set2.add(1);
		set2.add(2);
		genomeGroups.add(set2);

		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9};

		Parameter p = new Parameter(0, 2, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(geneLabelMap.length);

		// Test the java implementation
		GeneCluster[] res = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p, genomeGroups);

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 2, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 2, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(1, 2, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());

		Subsequence[][] subsequences1 = {{sub1} ,{sub2}, {sub3}};

		GeneClusterOccurrence[] bestOccurrences1 = {new GeneClusterOccurrence(0, subsequences1, res[0].getOccurrences()[0].getBestpValue(), 0, 3)};
		GeneClusterOccurrence[] allOccurrences1 = {new GeneClusterOccurrence(0, subsequences1, res[0].getAllOccurrences()[0].getBestpValue(), 0, 3)};

		int[] genes1 = {1, 2};

		Subsequence sub4 = new Subsequence(4, 5, 0, 0, res[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub5 = new Subsequence(6, 7, 0, 0, res[1].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());

		Subsequence[][] subsequences2 = {{sub4} ,{sub5}, {}};

		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getAllOccurrences()[0].getBestpValue(), 0, 2)};

		int[] genes2 = {8, 9};

		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences1, allOccurrences1, genes1,
				res[0].getBestPValue(),
				res[0].getBestPValueCorrected(),
				0,
				0,
				Parameter.OperationMode.reference),
				new GeneCluster(1, bestOccurrences2, allOccurrences2, genes2,
						res[1].getBestPValue(),
						res[1].getBestPValueCorrected(),
						0,
						0,
						Parameter.OperationMode.reference)};
				
		performTest(refCluster, res, PValueComparison.COMPARE_NONE);
	}
}

