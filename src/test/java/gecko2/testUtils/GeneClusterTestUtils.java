package gecko2.testUtils;

import gecko2.GeckoInstance;
import gecko2.algo.DeltaLocation;
import gecko2.algo.ReferenceCluster;
import gecko2.algo.ReferenceClusterAlgorithm;
import gecko2.algorithm.*;
import gecko2.io.CogFileReader;
import gecko2.io.DataSetWriter;
import gecko2.io.GckFileReader;
import gecko2.io.GeckoDataReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import static org.junit.Assert.*;

public class GeneClusterTestUtils {

    public static void performTest(Parameter p, int[][][] genomes, ExpectedReferenceClusterValues[] expectedReferenceClusters) {
        performTest(p, genomes, null, expectedReferenceClusters);
    }

    public static void performTest(Parameter p, int[][][] genomes, List<Set<Integer>> genomeGroups, ExpectedReferenceClusterValues[] expectedReferenceClusters) {
        // Test the java implementation
    	int k = 20;
        long[][] start = new long [2][k];
        long[][] stop = new long [2][k];
        for(int i=0;i<k;i++){
        	start[0][i] = System.currentTimeMillis();
        	List<ReferenceCluster> javaRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p, genomeGroups);
        	compareReferenceClusters(expectedReferenceClusters, javaRes, PValueComparison.COMPARE_NONE);
        	stop[0][i] = System.currentTimeMillis();
        }
        int sum = 0;
        long[] erg = new long [k];
        for(int i=0;i<k;i++){
        	erg[i] = stop[0][i]-start[0][i];
        	sum += erg[i];
        }
        sum/=k;
        Arrays.sort(erg);
        System.out.println("ohne Speicher red");
        System.out.println("	arith. Mittelwert; " + sum);
        System.out.println("	median Mittelwert; " + erg[k/2-1]);
        
        //DataSet.printIntArray(genomes);
        //for (ExpectedReferenceClusterValues cluster : expectedReferenceClusters)
        //    System.out.println(cluster.getGeneContent());

        MemoryReduction.memReducer(genomes, expectedReferenceClusters);
        
        //System.out.println();
        //DataSet.printIntArray(genomes);
        //for (ExpectedReferenceClusterValues cluster : expectedReferenceClusters)
        //    System.out.println(cluster.getGeneContent());
        for(int i=0;i<k;i++){
        	start[1][i] = System.currentTimeMillis();
        	List<ReferenceCluster> reducedRes = ReferenceClusterAlgorithm.computeReferenceClusters(genomes, p, genomeGroups);
        	compareReferenceClusters(expectedReferenceClusters, reducedRes, PValueComparison.COMPARE_NONE);
        	stop[1][i] = System.currentTimeMillis();
        }
        for(int i=0;i<k;i++){
        	erg[i] = stop[1][i]-start[1][i];
        	sum += erg[i];
        }
        sum/=k;
        Arrays.sort(erg);
        System.out.println("mit Speicher red");
        System.out.println("	arith. Mittelwert; " + sum);
        System.out.println("	median Mittelwert; " + erg[k/2-1]);
        System.out.println("");
    }

    public enum PValueComparison {
		COMPARE_ALL,
		COMPARE_UNCORRECTED,
		COMPARE_NONE
	}
	
	//===============================================================================//
	//============================= Methods =========================================//
	//===============================================================================//
	
	/**
	 * Compares two BigDecimals for equal values, up to a precision of three after the first significant value 
	 * @param expected expected value
	 * @param actual actual value
	 */
	public static void  assertEqualsBigDecimal(BigDecimal expected, BigDecimal actual) {
		assertEqualsBigDecimal(expected, actual, 3);
	}
	
	/**
	 * Compares two BigDecimals for equal values, up to a precision after the first significant value 
	 * @param expected expected value
	 * @param actual actual value
	 * @param precision how many digits after the first significant value will be compared
	 */
	public static void  assertEqualsBigDecimal(BigDecimal expected, BigDecimal actual, int precision) {
		String[] split = expected.toString().split("E");
		int expExp = 0;
		if (split.length == 2)
			expExp = Integer.parseInt(split[1]);
		
		//
		BigInteger exp = expected.scaleByPowerOfTen(-expExp+precision).toBigInteger();
		BigInteger act = actual.scaleByPowerOfTen(-expExp+precision).toBigInteger();

	    assertTrue("expected:<"+expected.toString()+"> but was:<"+actual.toString()+">", exp.equals(act));
	    
	    split = actual.toString().split("E");
		int expAct = 0;
		if (split.length == 2)
			expAct = Integer.parseInt(split[1]);
		
		exp = expected.scaleByPowerOfTen(-expAct+precision).toBigInteger();
		act = actual.scaleByPowerOfTen(-expAct+precision).toBigInteger();

	    assertTrue("expected:<"+expected.toString()+"> but was:<"+actual.toString()+">", exp.equals(act));
	}
	
	/**
	 * Compare two Subsequences
	 * @param expected the expected result
	 * @param actual the actual result
	 */
	private static void compareSubsequence(Subsequence expected, Subsequence actual, PValueComparison pValueComp) {
		assertEquals(expected.getStart(), actual.getStart());
		assertEquals(expected.getStop(), actual.getStop());
		assertEquals(expected.getChromosome(), actual.getChromosome());
		assertEquals(expected.getDist(), actual.getDist());
		if (pValueComp != PValueComparison.COMPARE_NONE)
			assertEqualsBigDecimal(expected.getpValue(), actual.getpValue());
	}

    private static void compareDeltaLocations(ExpectedDeltaLocationValues[] expected, List<DeltaLocation> actual, PValueComparison pValueComp) {
        assertEquals(expected.length, actual.size());
        for (int i=0; i<actual.size(); i++){
            assertEquals(expected[i].getChrNr(), actual.get(i).getChrNr());
            assertEquals(expected[i].getL(), actual.get(i).getL());
            assertEquals(expected[i].getR(), actual.get(i).getR());
            assertEquals(expected[i].getDistance(), actual.get(i).getDistance());
            if (pValueComp != PValueComparison.COMPARE_NONE)
                assertEquals(expected[i].getpValue(), actual.get(i).getpValue(), 1e-15 * expected[i].getpValue());
        }
    }

    private static void compareDeltaLocations(List<DeltaLocation> expected, List<DeltaLocation> actual, PValueComparison pValueComp) {
        assertEquals(expected.size(), actual.size());
        for (int i=0; i<actual.size(); i++){
            assertEquals(expected.get(i).getChrNr(), actual.get(i).getChrNr());
            assertEquals(expected.get(i).getL(), actual.get(i).getL());
            assertEquals(expected.get(i).getR(), actual.get(i).getR());
            assertEquals(expected.get(i).getDistance(), actual.get(i).getDistance());
            if (pValueComp != PValueComparison.COMPARE_NONE)
                assertEquals(expected.get(i).getpValue(), actual.get(i).getpValue(), 1e-15 * expected.get(i).getpValue());
        }
    }
	
	/**
	 * Compares two GeneClusterOccurrences
	 * @param expected the expected result
	 * @param actual the actual result
	 */
	private static void compareOccurrence(GeneClusterOccurrence expected, GeneClusterOccurrence actual, PValueComparison pValueComp) {
		assertEquals(expected.getId(), actual.getId());
		if (pValueComp != PValueComparison.COMPARE_NONE)
			assertEqualsBigDecimal(expected.getBestpValue(), actual.getBestpValue());
		assertEquals(expected.getTotalDist(), actual.getTotalDist());
		assertEquals(expected.getSupport(), actual.getSupport());
		
		assertEquals(expected.getSubsequences().length, actual.getSubsequences().length);
		
		for (int i=0; i<expected.getSubsequences().length; i++)
		{
			assertEquals(expected.getSubsequences()[i].length, actual.getSubsequences()[i].length);
			
			for (int j=0; j<expected.getSubsequences()[i].length; j++)
				compareSubsequence(expected.getSubsequences()[i][j], actual.getSubsequences()[i][j], pValueComp);

		}	
	}

    /**
     * Compares the two GeneClusters
     * @param expected the expected result
     * @param actual the actual result
     */
    private static void compareReferenceCluster(ExpectedReferenceClusterValues expected, ReferenceCluster actual, PValueComparison pValueComp) {
        assertEquals(expected.getGeneContent().size(), actual.getGeneContent().size());
        // TODO Test removed till statistics is fixed
        //assertEquals(new HashSet<>(expected.getGeneContent()), new HashSet<>(actual.getGeneContent()));
        assertEquals(expected.getSize(), actual.getSize());
        if (pValueComp != PValueComparison.COMPARE_NONE)
            assertEqualsBigDecimal(expected.getBestCombined_pValue(), actual.getBestCombined_pValue());
        if (pValueComp == PValueComparison.COMPARE_ALL)
            assertEqualsBigDecimal(expected.getBestCombined_pValueCorrected(), actual.getBestCombined_pValueCorrected());
        assertArrayEquals(expected.getMinimumDistances(), actual.getMinimumDistances());
        // TODO Test removed till statistics is fixed
        //assertEquals(expected.getGenomeNr(), actual.getGenomeNr());
        //assertEquals(expected.getChrNr(), actual.getChrNr());
        assertEquals(expected.getCoveredGenomes(), actual.getCoveredGenomes());

        assertEquals(expected.getAllDeltaLocations().length, actual.getAllDeltaLocations().size());

        for (int i = 0; i < actual.getAllDeltaLocations().size(); i++)
            compareDeltaLocations(expected.getAllDeltaLocations()[i], actual.getDeltaLocations(i), pValueComp);
    }

    /**
     * Compares the two GeneClusters
     * @param expected the expected result
     * @param actual the actual result
     */
    private static void compareReferenceCluster(ReferenceCluster expected, ReferenceCluster actual, PValueComparison pValueComp) {
        assertEquals(expected.getGeneContent().size(), actual.getGeneContent().size());
        assertEquals(new HashSet<>(expected.getGeneContent()), new HashSet<>(actual.getGeneContent()));
        assertEquals(expected.getSize(), actual.getSize());
        if (pValueComp != PValueComparison.COMPARE_NONE)
            assertEqualsBigDecimal(expected.getBestCombined_pValue(), actual.getBestCombined_pValue());
        if (pValueComp == PValueComparison.COMPARE_ALL)
            assertEqualsBigDecimal(expected.getBestCombined_pValueCorrected(), actual.getBestCombined_pValueCorrected());
        assertArrayEquals(expected.getMinimumDistances(), actual.getMinimumDistances());
        assertEquals(expected.getGenomeNr(), actual.getGenomeNr());
        assertEquals(expected.getChrNr(), actual.getChrNr());
        assertEquals(expected.getCoveredGenomes(), actual.getCoveredGenomes());

        assertEquals(expected.getAllDeltaLocations().size(), actual.getAllDeltaLocations().size());

        for (int i = 0; i < actual.getAllDeltaLocations().size(); i++)
            compareDeltaLocations(expected.getDeltaLocations(i), actual.getDeltaLocations(i), pValueComp);
    }

    /**
	 * Compares the two GeneClusters
	 * @param expected the expected result
	 * @param actual the actual result
	 */
	private static void compareGeneClusters(GeneCluster expected, GeneCluster actual, PValueComparison pValueComp) {
		assertEquals(expected.getId(), actual.getId());
        // TODO Test removed till statistics is fixed
		//assertEquals(expected.getGeneFamilies(), actual.getGeneFamilies());
		assertEquals(expected.getSize(), actual.getSize());
		assertEquals(expected.isMatch(), actual.isMatch());
		if (pValueComp != PValueComparison.COMPARE_NONE)
			assertEqualsBigDecimal(expected.getBestPValue(), actual.getBestPValue());
		if (pValueComp == PValueComparison.COMPARE_ALL)
			assertEqualsBigDecimal(expected.getBestPValueCorrected(), actual.getBestPValueCorrected());
		assertEquals(expected.getMinTotalDist(), actual.getMinTotalDist());
		assertEquals(expected.getType(), actual.getType());
        // TODO Test removed till statistics is fixed
		//assertEquals(expected.getRefSeqIndex(), actual.getRefSeqIndex());
		
		assertEquals(expected.getOccurrences().length, actual.getOccurrences().length);
		
		for(int i = 0; i < actual.getOccurrences().length; i++)
		{
			compareOccurrence(expected.getOccurrences()[i], actual.getOccurrences()[i], pValueComp);
		}		
		
		assertEquals(expected.getAllOccurrences().length, actual.getAllOccurrences().length);
		for(int i = 0; i < actual.getAllOccurrences().length; i++)
		{
			compareOccurrence(expected.getAllOccurrences()[i], actual.getAllOccurrences()[i], pValueComp);
		}
	}

    /**
     * The method tests whether the two input array are equal.
     *
     * @param expected expected GeneCluster array
     * @param actual actual GeneCluster array
     * @param pValueComp how pValues shall be compared (all, only uncorrected, or none)
     */
    public static void compareReferenceClusters(ExpectedReferenceClusterValues[] expected, List<ReferenceCluster> actual, PValueComparison pValueComp)
    {
        assertEquals(expected.length, actual.size());

        for(int i = 0; i < expected.length; i++)
        {
            compareReferenceCluster(expected[i], actual.get(i), pValueComp);
        }
    }

    /**
     * The method tests whether the two input array are equal.
     *
     * @param expected expected GeneCluster array
     * @param actual actual GeneCluster array
     * @param pValueComp how pValues shall be compared (all, only uncorrected, or none)
     */
    public static void compareReferenceClusters(List<ReferenceCluster> expected, List<ReferenceCluster> actual, PValueComparison pValueComp)
    {
        assertEquals(expected.size(), actual.size());

        for(int i = 0; i < expected.size(); i++)
        {
            compareReferenceCluster(expected.get(i), actual.get(i), pValueComp);
        }
    }
	
	/**
	 * The method tests whether the two input array are equal.
	 * 
	 * @param expected expected GeneCluster array
	 * @param actual actual GeneCluster array
     * @param pValueComp how pValues shall be compared (all, only uncorrected, or none)
	 */
	public static void compareGeneClusters(GeneCluster[] expected, GeneCluster[] actual, PValueComparison pValueComp)
	{
		assertEquals(expected.length, actual.length);
		
		for(int i = 0; i < expected.length; i++)
		{
			compareGeneClusters(expected[i], actual[i], pValueComp);
		}
	}
	
	public static void automaticGeneClusterTestFromFile(ReferenceClusterTestSettings settings, boolean libGeckoLoaded) throws IOException, DataFormatException, ParseException {
        // Expected Results
        assertNotNull(settings.expectedResultFile);
        GeckoDataReader resultReader = new GckFileReader(settings.expectedResultFile);
        DataSet expectedData = resultReader.readData();

        // Test unreduced
        CogFileReader reader = new CogFileReader(settings.dataFile);
        DataSet actualData = reader.readData();

		GeneCluster[] javaRes = GeckoInstance.computeClustersJava(actualData, settings.p, settings.genomeGroups);
        actualData.setClusters(javaRes);
		
		compareGeneClusters(expectedData.getClusters(), javaRes, PValueComparison.COMPARE_NONE);

        // Test with memory reduction
        CogFileReader reducedReader = new CogFileReader(settings.dataFile);
        DataSet reducedData = reducedReader.readData();

        GeneCluster[] reducedRes = GeckoInstance.computeClustersJava(reducedData, settings.p, settings.genomeGroups, true);        reducedData.setClusters(reducedRes);

        compareGeneClusters(expectedData.getClusters(), reducedRes, PValueComparison.COMPARE_NONE);
		
		if (libGeckoLoaded && settings.p.getDelta() >= 0 && settings.genomeGroups == null){
			//GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(actualData, settings.p);
		
			// Test the java implementation
			//compareReferenceClusters(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
		}
	}
			
	/**
	 * The method launches computeCluster for a given parameter set and 
	 * genomes from a .cog file and writes the result to a the given outputFile 
	 *
	 */
	public static void generateRefClusterFile(ReferenceClusterTestSettings settings) throws IOException, ParseException {
		if (settings.resultOutputFile.exists()) {
			System.err.println("Error: File " + settings.resultOutputFile.getAbsolutePath() + " exists already. Delete it manually if you want to continue!");
			System.exit(1);
		}

		// generate the Genome array from the input file
        CogFileReader reader = new CogFileReader(settings.dataFile);
        DataSet data = reader.readData();

        GeneCluster[] javaRes = GeckoInstance.computeClustersJava(data, settings.p, settings.genomeGroups);
        data.setClusters(javaRes);

        assertTrue(settings.resultOutputFile.createNewFile());
        DataSetWriter.saveDataSetToFile(data, settings.resultOutputFile);
	}

    public static void performanceTest(ReferenceClusterTestSettings settings, boolean useMemoryReduction) throws IOException, DataFormatException, ParseException {
        // Test unreduced
        CogFileReader reader = new CogFileReader(settings.dataFile);
        DataSet actualData = reader.readData();

        GeneCluster[] javaRes = GeckoInstance.computeClustersJava(actualData, settings.p, settings.genomeGroups, useMemoryReduction);
        actualData.setClusters(javaRes);
    }
	
	public static void main(String[] args)
	{
        ReferenceClusterTestSettings testType = ReferenceClusterTestSettings.memoryReductionDataD2S4Q2();
        //ReferenceClusterTestSettings testType = ReferenceClusterTestSettings.memoryReductionBugD2S5Q2();
        //ReferenceClusterTestSettings testType = ReferenceClusterTestSettings.fiveProteobacterD3S6Q2Grouping();
        //ReferenceClusterTestSettings testType = ReferenceClusterTestSettings.fiveProteobacterD3S6Q4();
        //ReferenceClusterTestSettings testType = ReferenceClusterTestSettings.fiveProteobacterDeltaTable();
        //ReferenceClusterTestSettings testType = ReferenceClusterTestSettings.statisticsDataD5S8Q10FixedRef();
        try{
			generateRefClusterFile(testType);
            //performanceTest(testType, false);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
}
