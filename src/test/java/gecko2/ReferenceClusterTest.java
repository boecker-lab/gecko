package gecko2;

import static gecko2.GeneClusterTestUtils.*;
import static org.junit.Assert.*;

import gecko2.LibraryUtils.PlatformNotSupportedException;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.Parameter;
import gecko2.algorithm.Subsequence;
import gecko2.io.CogFileReader;
import gecko2.io.GeneClusterResult;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The class tests the computeClusters algorithm from the Gecko2 program
 * 
 * @author Hans-Martin Haase <hans-martin.haase@uni-jena.de>
 * @version 0.54
 *
 */
public class ReferenceClusterTest 
{	
	/**
	 * The method adds libgecko2 to the library path.
	 * 
	 */
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
	
	/**
	 * The method tests whether the two input array are equal.
	 * 
	 * @param preDefClusters pre defined GeneCluster array
	 * @param calcClusters the GeneCluster array which is the result of the compute clusters algorithm
	 */
	public void performTest(GeneCluster[] preDefClusters, GeneCluster[] calcClusters)
	{
		assertEquals(preDefClusters.length, calcClusters.length);
		
		for(int i = 0; i < preDefClusters.length; i++)
		{
			assertEquals(preDefClusters[i].getId(), calcClusters[i].getId());
			assertArrayEquals(preDefClusters[i].getGenes(), calcClusters[i].getGenes());
			assertEquals(preDefClusters[i].getSize(), calcClusters[i].getSize());
			assertEquals(preDefClusters[i].isMatch(), calcClusters[i].isMatch());
			assertEqualsBigDecimal(preDefClusters[i].getBestPValue(), calcClusters[i].getBestPValue(), 10);
			assertEquals(preDefClusters[i].getMinTotalDist(), calcClusters[i].getMinTotalDist());
			assertEquals(preDefClusters[i].getType(), calcClusters[i].getType());
			assertEquals(preDefClusters[i].getRefSeqIndex(), calcClusters[i].getRefSeqIndex());
			
			assertEquals(preDefClusters[i].getOccurrences().length, calcClusters[i].getOccurrences().length);
			
			for(int j = 0; j < calcClusters[i].getOccurrences().length; j++)
			{
				
				assertEquals(preDefClusters[i].getOccurrences()[j].getId(), calcClusters[i].getOccurrences()[j].getId());
				assertEqualsBigDecimal(preDefClusters[i].getOccurrences()[j].getBestpValue(), calcClusters[i].getOccurrences()[j].getBestpValue(), 10);
				assertEquals(preDefClusters[i].getOccurrences()[j].getTotalDist(), calcClusters[i].getOccurrences()[j].getTotalDist());
				assertEquals(preDefClusters[i].getOccurrences()[j].getSupport(), calcClusters[i].getOccurrences()[j].getSupport());
				
				assertEquals(preDefClusters[i].getOccurrences()[j].getSubsequences().length, calcClusters[i].getOccurrences()[j].getSubsequences().length);
				
				// Vars for help
				int p = 0;
				int k = 0;
				int l = 0;
				
				for(Subsequence[] sub : preDefClusters[i].getOccurrences()[j].getSubsequences())
				{
					assertEquals(sub.length, calcClusters[i].getOccurrences()[j].getSubsequences()[p].length);
					p++;
					
					for(Subsequence seq : sub)
					{
						assertEquals(seq.getStart(), calcClusters[i].getOccurrences()[j].getSubsequences()[k][l].getStart());
						assertEquals(seq.getStop(), calcClusters[i].getOccurrences()[j].getSubsequences()[k][l].getStop());
						assertEquals(seq.getChromosome(), calcClusters[i].getOccurrences()[j].getSubsequences()[k][l].getChromosome());
						assertEquals(seq.getDist(), calcClusters[i].getOccurrences()[j].getSubsequences()[k][l].getDist());
						assertEqualsBigDecimal(seq.getpValue(), calcClusters[i].getOccurrences()[j].getSubsequences()[k][l].getpValue(), 10);
						l++;
					}
					k++;
					l = 0;
				}	
				
				k = 0;
			}	
			
			
			assertEquals(preDefClusters[i].getAllOccurrences().length, calcClusters[i].getAllOccurrences().length);
			for(int j = 0; j < calcClusters[i].getAllOccurrences().length; j++)
			{
				
				assertEquals(preDefClusters[i].getAllOccurrences()[j].getId(), calcClusters[i].getAllOccurrences()[j].getId());
				assertEqualsBigDecimal(preDefClusters[i].getAllOccurrences()[j].getBestpValue(), calcClusters[i].getAllOccurrences()[j].getBestpValue(), 10);
				assertEquals(preDefClusters[i].getAllOccurrences()[j].getTotalDist(), calcClusters[i].getAllOccurrences()[j].getTotalDist());
				assertEquals(preDefClusters[i].getAllOccurrences()[j].getSupport(), calcClusters[i].getAllOccurrences()[j].getSupport());
				
				// Vars for help
				int p = 0;
				int k = 0;
				int l = 0;
				
				for(Subsequence[] sub : preDefClusters[i].getAllOccurrences()[j].getSubsequences())
				{
					assertEquals(sub.length, calcClusters[i].getAllOccurrences()[j].getSubsequences()[p].length);
					p++;
					
					for(Subsequence seq : sub)
					{
						assertEquals(seq.getStart(), calcClusters[i].getAllOccurrences()[j].getSubsequences()[k][l].getStart());
						assertEquals(seq.getStop(), calcClusters[i].getAllOccurrences()[j].getSubsequences()[k][l].getStop());
						assertEquals(seq.getChromosome(), calcClusters[i].getAllOccurrences()[j].getSubsequences()[k][l].getChromosome());
						assertEquals(seq.getDist(), calcClusters[i].getAllOccurrences()[j].getSubsequences()[k][l].getDist());
						assertEqualsBigDecimal(seq.getpValue(), calcClusters[i].getAllOccurrences()[j].getSubsequences()[k][l].getpValue(), 10);
						l++;
					}
					k++;
					l = 0;
				}
				
				k = 0;
			}
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
			
		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);

		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());
		
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence[][] subsequences = {{sub1},{sub2}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};
		
		int[] genes = {1, 2, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									0, 
									0,
									'r')};
		
		performTest(refCluster, res);
		
		
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
			
		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());

		
		
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
		
		performTest(refCluster, res);
				
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
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}, {0, 3, 3, 1, 2, 5, 6}}, {{0, 1, 2, 5, 4, 0}}};
			
		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6};
			
		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());
		
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 1, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		
		Subsequence[][] subsequences = {{sub1},{sub2}};
		Subsequence[][] subsequences2 = {{sub3}, {sub2}};
		
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};
		
		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getAllOccurrences()[0].getBestpValue(), 0, 2)};
		
		int[] genes = {1, 2, 5};
		int[] genes2 = {1, 2, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									0, 
									0,
									'r'),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes2,
									res[1].getBestPValue(),
									res[1].getBestPValueCorrected(),
									0,
									0,
									'r')};
		
		performTest(refCluster, res);
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
			
		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());		
		
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 5, 0, 1, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 1, 0, res[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub4 = new Subsequence(2, 5, 0, 0, res[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub5 = new Subsequence(1, 3, 0, 1, res[2].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub6 = new Subsequence(3, 5, 1, 1, res[2].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		
		Subsequence[][] subsequences = {{sub1},{sub2}};
		Subsequence[][] subsequences2 = {{sub3}, {sub2}};
		Subsequence[][] subsequences3 = {{sub5, sub6}, {sub4}};
		//Subsequence[][] subsequences4 = {{sub4}, {sub6}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		GeneClusterOccurrence[] bestOccurrences3 = {new GeneClusterOccurrence(0, subsequences3, res[2].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences3 = {new GeneClusterOccurrence(0, subsequences3, res[2].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		int[] genes = {1, 2, 5};
		int[] genes2 = {1, 2, 7, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									1, 
									0,
									'r'),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes,
									res[1].getBestPValue(),
									res[1].getBestPValueCorrected(),
									1,
									0,
									'r'),
									new GeneCluster(2, bestOccurrences3, allOccurrences3, genes2,
									res[2].getBestPValue(),
									res[2].getBestPValueCorrected(),
									1,
									1,
									'r')};
				
		performTest(refCluster, res);
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
			
		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());

		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 1, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub4 = new Subsequence(4, 6, 1, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][1].getpValue());
		
		
		Subsequence[][] subsequences = {{sub1},{sub2, sub4}};
		Subsequence[][] subsequences2 = {{sub3}, {sub2, sub4}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};
		
		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getAllOccurrences()[0].getBestpValue(), 0, 2)};
		
		int[] genes = {1, 2, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									0, 
									0,
									'r'),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes,
									res[1].getBestPValue(),
									res[1].getBestPValueCorrected(),
									0,
									0,
									'r')};
				
		performTest(refCluster, res);
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
			
		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());

				
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 5, 0, 1, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 1, 0, res[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub4 = new Subsequence(4, 7, 1, 1, res[0].getAllOccurrences()[0].getSubsequences()[1][1].getpValue());
		
		Subsequence sub5 = new Subsequence(1, 3, 0, 1, res[2].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub6 = new Subsequence(2, 5, 0, 0, res[2].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub7 = new Subsequence(3, 5, 1, 1, res[3].getAllOccurrences()[0].getSubsequences()[0][1].getpValue());
		Subsequence sub8 = new Subsequence(4, 7, 1, 0, res[3].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		
		Subsequence[][] subsequences = {{sub1} ,{sub2, sub4}};
		Subsequence[][] subsequences2 = {{sub3}, {sub2, sub4}};
		Subsequence[][] subsequences3 = {{sub5, sub7}, {sub6}};
		Subsequence[][] subsequences4 = {{sub5, sub7}, {sub8}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		GeneClusterOccurrence[] bestOccurrences3 = {new GeneClusterOccurrence(0, subsequences3, res[2].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences3 = {new GeneClusterOccurrence(0, subsequences3, res[2].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		GeneClusterOccurrence[] bestOccurrences4 = {new GeneClusterOccurrence(0, subsequences4, res[3].getOccurrences()[0].getBestpValue(), 1, 2)};
		GeneClusterOccurrence[] allOccurrences4 = {new GeneClusterOccurrence(0, subsequences4, res[3].getAllOccurrences()[0].getBestpValue(), 1, 2)};
		
		int[] genes = {1, 2, 5};
		int[] genes2 = {1, 2, 8, 5};
		int[] genes3 = {2, 1, 12, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(),
									res[0].getBestPValueCorrected(),
									1, 
									0,
									'r'),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes,
									res[1].getBestPValue(),
									res[1].getBestPValueCorrected(),
									1,
									0,
									'r'),
									new GeneCluster(2, bestOccurrences3, allOccurrences3, genes2,
									res[2].getBestPValue(),
									res[2].getBestPValueCorrected(),
									1,
									1,
									'r'),
									new GeneCluster(3, bestOccurrences4, allOccurrences4, genes3,
									res[3].getBestPValue(),
									res[3].getBestPValueCorrected(),
									1,
									1,
									'r')};
		
		
		performTest(refCluster, res);
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
		int genomes[][][] = {{{0, 1, 2, 5, 3, 0}}, {{0, 9, 1, 2, 5, 4, 0}}, {{0, 8, 10, 1, 2, 5, 11,0}}};
			
		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
			
		Parameter p = new Parameter(0, 3, 3, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());

				
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());
		
		Subsequence[][] subsequences = {{sub1} ,{sub2}, {sub3}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 0, 3)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 0, 3)};
		
		
		
		int[] genes = {1, 2, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									0, 
									0,
									'r')};
				
		performTest(refCluster, res);
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
			
		Parameter p = new Parameter(1, 3, 3, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());

				
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(4, 6, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 6, 0, 1, res[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());
				
		Subsequence sub7 = new Subsequence(4, 6, 0, 1, res[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub8 = new Subsequence(2, 4, 0, 1, res[1].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub9 = new Subsequence(3, 6, 0, 0, res[1].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());
		
		
		Subsequence[][] subsequences = {{sub1} ,{sub2}, {sub3}};
		Subsequence[][] subsequences3 = {{sub7} ,{sub8}, {sub9}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 1, 3)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 1, 3)};
		
		GeneClusterOccurrence[] bestOccurrences1 = {new GeneClusterOccurrence(0, subsequences3, res[1].getOccurrences()[0].getBestpValue(), 2, 3)};
		GeneClusterOccurrence[] allOccurrences1 = {new GeneClusterOccurrence(0, subsequences3, res[1].getAllOccurrences()[0].getBestpValue(), 2, 3)};
		
		int[] genes = {1, 2, 5};
		int[] genes3 = {1, 2, 7, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									1, 
									0,
									'r'),
									new GeneCluster(1, bestOccurrences1, allOccurrences1, genes3, 
									res[1].getBestPValue(), 
									res[1].getBestPValueCorrected(),
									2, 
									2,
									'r')};
				
		performTest(refCluster, res);
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
			
		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());

				
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(4, 6, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 6, 0, 1, res[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());
				
		Subsequence sub7 = new Subsequence(4, 6, 0, 1, res[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub8 = new Subsequence(2, 4, 0, 1, res[1].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub9 = new Subsequence(3, 6, 0, 0, res[1].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());
		
		
		Subsequence[][] subsequences = {{sub1} ,{sub2}, {sub3}};
		Subsequence[][] subsequences3 = {{sub7} ,{sub8}, {sub9}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 1, 3)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 1, 3)};
		
		GeneClusterOccurrence[] bestOccurrences1 = {new GeneClusterOccurrence(0, subsequences3, res[1].getOccurrences()[0].getBestpValue(), 2, 3)};
		GeneClusterOccurrence[] allOccurrences1 = {new GeneClusterOccurrence(0, subsequences3, res[1].getAllOccurrences()[0].getBestpValue(), 2, 3)};
		
		int[] genes = {1, 2, 5};
		int[] genes3 = {1, 2, 7, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									1, 
									0,
									'r'),
									new GeneCluster(1, bestOccurrences1, allOccurrences1, genes3, 
									res[1].getBestPValue(), 
									res[1].getBestPValueCorrected(),
									2, 
									2,
									'r')};
				
		performTest(refCluster, res);
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
			
		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());

				
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(4, 6, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());	
		
		Subsequence[][] subsequences = {{sub1} ,{sub2}, {}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};
		
		
		int[] genes = {1, 2, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(), 
									0, 
									0,
									'r')};
				
		performTest(refCluster, res);
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
			
		Parameter p = new Parameter(0, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());

				
		// def result (using p values from calculated result)
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());	
		
		Subsequence[][] subsequences = {{}, {sub2} ,{sub3}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};
		
		
		int[] genes = {1, 2, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									0, 
									1,
									'r')};
				
		performTest(refCluster, res);
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
	public void testComputeClusters12() 
	{
		// def array for computation
		int genomes[][][] = {{{0,13, 4, 12, 1, 2, 7, 5, 3, 0}}, {{0, 9, 1, 2, 5, 6, 4, 0}}, {{0, 8, 10, 1, 2, 5, 11,0}}};
			
		// def parameters
		int[] geneLabelMap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
			
		Parameter p = new Parameter(1, 3, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());

				
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(4, 7, 0, 1, res[1].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(2, 4, 0, 0, res[1].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());	
		Subsequence sub3 = new Subsequence(3, 5, 0, 0, res[1].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());
		
		
		Subsequence sub4 = new Subsequence(4, 7, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub5 = new Subsequence(2, 4, 0, 1, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());	
		Subsequence sub6 = new Subsequence(3, 5, 0, 1, res[0].getAllOccurrences()[0].getSubsequences()[2][0].getpValue());
		
		Subsequence[][] subsequences = {{sub1} ,{sub2}, {sub3}};
		Subsequence[][] subsequences2 = {{sub4}, {sub5}, {sub6}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[1].getOccurrences()[0].getBestpValue(), 1, 3)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[1].getAllOccurrences()[0].getBestpValue(), 1, 3)};
		
		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[0].getOccurrences()[0].getBestpValue(), 2, 3)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[0].getAllOccurrences()[0].getBestpValue(), 2, 3)};
		
		int[] genes = {1, 2, 5};
		int[] genes2 = {1, 2, 7, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences2, allOccurrences2, genes2, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									2, 
									0,
									'r'), 
									new GeneCluster(1, bestOccurrences, allOccurrences, genes, 
									res[1].getBestPValue(), 
									res[1].getBestPValueCorrected(),
									1, 
									1,
									'r')};

		performTest(refCluster, res);
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
	 * 
	 */
	@Test
	public void testComputeClusters13() 
	{
		// def array for computation
		int genomes[][][] = {{{0, 1, 2, 0}}, {{0, 1, 2, 0}}};
			
		// def parameters
		int[] geneLabelMap = {1, 2};
			
		Parameter p = new Parameter(1, 2, 2, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);

		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());
		
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 2, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 2, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence[][] subsequences = {{sub1},{sub2}};
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};
		
		int[] genes = {1, 2};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									0, 
									0,
									'r')};
		
		performTest(refCluster, res);	
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
		int genomes[][][] = {{{0, 1, 2, 3, 0}, {0, 1, 2, 0}}, {{0, 2, 3, 0}}};
			
		// def parameters
		int[] geneLabelMap = {1, 2, 3};
			
		Parameter p = new Parameter(1, 3, 3, Parameter.QUORUM_NO_COST, 'r', 'd');
		p.setAlphabetSize(geneLabelMap.length);
				
		// result of computation 
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());
		
		// def result (using p values from calculated result)
		Subsequence sub1 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		Subsequence sub2 = new Subsequence(1, 3, 0, 0, res[0].getAllOccurrences()[0].getSubsequences()[1][0].getpValue());
		Subsequence sub3 = new Subsequence(3, 5, 1, 0, res[0].getAllOccurrences()[0].getSubsequences()[0][0].getpValue());
		
		Subsequence[][] subsequences = {{sub1},{sub2}};
		Subsequence[][] subsequences2 = {{sub3}, {sub2}};
		
		
		GeneClusterOccurrence[] bestOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences = {new GeneClusterOccurrence(0, subsequences, res[0].getAllOccurrences()[0].getBestpValue(), 0, 2)};
		
		GeneClusterOccurrence[] bestOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getOccurrences()[0].getBestpValue(), 0, 2)};
		GeneClusterOccurrence[] allOccurrences2 = {new GeneClusterOccurrence(0, subsequences2, res[1].getAllOccurrences()[0].getBestpValue(), 0, 2)};
		
		int[] genes = {1, 2, 5};
		int[] genes2 = {1, 2, 5};
		
		GeneCluster[] refCluster = {new GeneCluster(0, bestOccurrences, allOccurrences, genes, 
									res[0].getBestPValue(), 
									res[0].getBestPValueCorrected(),
									0, 
									0,
									'r'),
									new GeneCluster(1, bestOccurrences2, allOccurrences2, genes2,
									res[1].getBestPValue(),
									res[1].getBestPValueCorrected(),
									0,
									0,
									'r')};
		
		performTest(refCluster, res);
	}
	
	@Test
	public void fiveProteobacterReferenceClusterTest() throws URISyntaxException, IOException, DataFormatException, LinePassedException {
		File inputFile = new File(ReferenceClusterTest.class.getResource("/fiveProteobacter.cog").toURI());
		File resultFile = new File(ReferenceClusterTest.class.getResource("/fiveProteobacterD3S6Q4.txt").toURI());
		
		automaticGeneClusterTestFromFile(inputFile, resultFile);
	}
	
	private void automaticGeneClusterTestFromFile(File input, File expected) throws IOException, DataFormatException, LinePassedException {
		GeneClusterResult gcr = GeneClusterTestUtils.readResultFile(expected);
		
		GeckoInstance.getInstance().setCurrentInputFile(input);
		
		CogFileReader reader = new CogFileReader();
		ArrayList<GenomeOccurence> genOcc = reader.importGenomes(input);
		
		reader.readFileContent(genOcc);
		
		int genomes[][][] = new int[reader.getGenomes().length][][];
		
		for (int k = 0; k < genomes.length; k++) 
		{
			genomes[k] = new int[reader.getGenomes()[k].getChromosomes().size()][];
		
			for (int j = 0; j < genomes[k].length; j++)
			{
				genomes[k][j] = reader.getGenomes()[k].getChromosomes().get(j).toIntArray(true, true);
			}
		}
		
		Parameter p = gcr.getParameterSet();
		p.setAlphabetSize(reader.getGeneLabelMap().size());
		
		GeneCluster[] res = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());
		
		performTest(gcr.getCompResult(), res);
	}
}

