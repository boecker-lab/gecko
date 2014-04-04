package gecko2.testUtils;

import gecko2.GeckoInstance;
import gecko2.algo.DeltaLocation;
import gecko2.algo.ReferenceCluster;
import gecko2.algorithm.*;
import gecko2.io.CogFileReader;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import static org.junit.Assert.*;


/**
 * This class implements static methods for storing a GeneCluster array in a file and reading from a file.
 * 
 * @author Hans-Martin Haase <hans-martin.haase@uni-jena.de>
 * @version 0.10
 *
 */
public class GeneClusterTestUtils {
	
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
            assertEquals(expected[i], actual.get(i));
            if (pValueComp != PValueComparison.COMPARE_NONE)
                assertEquals(expected[i].getpValue(), actual.get(i).getpValue(), 1e-15 * expected[i].getpValue());
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
        assertEquals(expected.getGeneContent(), actual.getGeneContent());
        assertEquals(expected.getSize(), actual.getSize());
        if (pValueComp != PValueComparison.COMPARE_NONE)
            assertEqualsBigDecimal(expected.getBestCombined_pValue(), actual.getBestCombined_pValue());
        if (pValueComp == PValueComparison.COMPARE_ALL)
            assertEqualsBigDecimal(expected.getBestCombined_pValueCorrected(), actual.getBestCombined_pValueCorrected());
        assertArrayEquals(expected.getMinimumDistances(), actual.getMinimumDistances());
        assertEquals(expected.getGenomeNr(), actual.getGenomeNr());
        assertEquals(expected.getChrNr(), actual.getChrNr());
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
	private static void compareGeneClusters(GeneCluster expected, GeneCluster actual, PValueComparison pValueComp) {
		assertEquals(expected.getId(), actual.getId());
		assertEquals(expected.getGeneFamilies(), actual.getGeneFamilies());
		assertEquals(expected.getSize(), actual.getSize());
		assertEquals(expected.isMatch(), actual.isMatch());
		if (pValueComp != PValueComparison.COMPARE_NONE)
			assertEqualsBigDecimal(expected.getBestPValue(), actual.getBestPValue());
		if (pValueComp == PValueComparison.COMPARE_ALL)
			assertEqualsBigDecimal(expected.getBestPValueCorrected(), actual.getBestPValueCorrected());
		assertEquals(expected.getMinTotalDist(), actual.getMinTotalDist());
		assertEquals(expected.getType(), actual.getType());
		assertEquals(expected.getRefSeqIndex(), actual.getRefSeqIndex());
		
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
    public static void performTest(ExpectedReferenceClusterValues[] expected, List<ReferenceCluster> actual, PValueComparison pValueComp)
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
	public static void performTest(GeneCluster[] expected, GeneCluster[] actual, PValueComparison pValueComp)
	{
		assertEquals(expected.length, actual.length);
		
		for(int i = 0; i < expected.length; i++)
		{
			compareGeneClusters(expected[i], actual[i], pValueComp);
		}
	}
	
	static void automaticGeneClusterTestFromFile(File input, File expected, boolean libGeckoLoaded) throws IOException, DataFormatException, ParseException {
		automaticGeneClusterTestFromFile(input, expected, null, libGeckoLoaded);
	}
	
	static void automaticGeneClusterTestFromFile(File input, File expected, List<Set<Integer>> genomeGroups, boolean libGeckoLoaded) throws IOException, DataFormatException, ParseException {
		/*GeneClusterResult gcr = GeneClusterResult.readResultFile(expected);


        CogFileReader reader = new CogFileReader(input);
        int genomes[][][] = readGenomes(reader, input);
		
		Parameter p = gcr.getParameterSet();

		GeneCluster[] javaRes = GeckoInstance.getInstance().computeClustersJava(genomes, p, genomeGroups);
		
		performTest(gcr.getCompResult(), javaRes, PValueComparison.COMPARE_ALL);
		
		if (libGeckoLoaded && p.getDelta() >= 0 && genomeGroups == null){
			GeneCluster[] res = GeckoInstance.getInstance().computeClustersLibgecko(genomes, p);	
		
			// Test the java implementation
			performTest(res, javaRes, PValueComparison.COMPARE_UNCORRECTED);
		}*/
	}
			
	/**
	 * The method launches computeCluster for a given parameter set and 
	 * genomes from a .cog file and writes the result to a the given outputFile 
	 * 
	 * @param inputCogFile File object to the input file
	 * @param p Parameter set
	 * @param outputFile the output file
	 */
	public static void generateRefClusterFile(File inputCogFile, File outputFile, Parameter p, List<Set<Integer>> genomeGrouping) throws IOException, ParseException {
		/*if (outputFile.exists()) {
			System.err.println("Error: File " + outputFile.getAbsolutePath() + " exists already. Delete it manually if you want to continue!");
			System.exit(1);
		}
		
		// Load the native library for the computeClusters run
		ReferenceClusterTest.loadLibGecko2();
		
			// generate the Genome array from the input file
		CogFileReader reader = new CogFileReader(inputCogFile);
		int genomes[][][] = readGenomes(reader, inputCogFile);

		GeneCluster[] result = GeckoInstance.getInstance().computeClustersJava(genomes, p, genomeGrouping);

		GeneClusterResult gcResult = new GeneClusterResult(result, p, inputCogFile.getName());

		System.out.println(outputFile.getAbsolutePath());
		assertTrue(outputFile.createNewFile());
		
		gcResult.writeToFile(outputFile);*/
	}
	
	/**
	 * Reads the genomes from the inputFile, using the reader
	 * @param reader the file reader
	 * @param inputFile the input file
	 * @return the int[][][] of genomes
	 * @throws IOException
	 */
	private static int[][][] readGenomes(CogFileReader reader, File inputFile) throws IOException, ParseException {
		GeckoInstance.getInstance();
		GeckoInstance.getInstance().setCurrentWorkingDirectoryOrFile(inputFile);
        reader.readData();

        return reader.getData().toIntArray();
	}
	
	private enum GenerateTestDataType {
		fiveProteobacterDeltaTable,
		fiveProteobacterD3S6Q4,
		fiveProteobacterD3S6Q2Grouping,
		statisticsDataD5S8Q10FixedRef
	}
	
	public static void main(String[] args)
	{
		GenerateTestDataType testType = GenerateTestDataType.fiveProteobacterD3S6Q2Grouping;
		try {
			List<Set<Integer>> genomeGroups = null;
			Parameter p = null;
			File inCogFile = null;
			File outFile = null;
			
			switch (testType) {
				case fiveProteobacterDeltaTable:
					int[][] deltaArray = {{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {1,1,1}, {2,2,2}, {3,3,3}, {5,5,5}};
					p = new Parameter(deltaArray, 4, 4, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
					inCogFile = new File(GeneClusterTestUtils.class.getResource("/fiveProteobacter.cog").getFile());
					outFile = new File("src/test/resources/fiveProteobacterDeltaTable.txt");
					break;
					
				case fiveProteobacterD3S6Q4:
					p = new Parameter(3, 6, 4, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
					inCogFile = new File(GeneClusterTestUtils.class.getResource("/fiveProteobacter.cog").getFile());
					outFile = new File("src/test/resources/fiveProteobacterD3S6Q4.txt");
					break;
					
				case fiveProteobacterD3S6Q2Grouping:
					p = new Parameter(3, 6, 2, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
					inCogFile = new File(GeneClusterTestUtils.class.getResource("/fiveProteobacter.cog").getFile());
					outFile = new File("src/test/resources/fiveProteobacterD3S6Q2Grouping.txt");
					genomeGroups = new ArrayList<>(2);
					Set<Integer> set1 = new HashSet<>();
					set1.add(0);
					genomeGroups.add(set1);
					Set<Integer> set2 = new HashSet<>();
					set2.add(1);
					set2.add(2);
					genomeGroups.add(set2);
					Set<Integer> set3 = new HashSet<>();
					set3.add(3);
					set3.add(4);
					genomeGroups.add(set3);
					break;
					
				case statisticsDataD5S8Q10FixedRef:
					p = new Parameter(5, 8, 10, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
					inCogFile = new File(GeneClusterTestUtils.class.getResource("/statisticsData.cog").getFile());
					outFile = new File("src/test/resources/statisticsDataD5S8Q10FixedRef.txt");
					break;
			}
			
			generateRefClusterFile(inCogFile, outFile, p, genomeGroups);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
}
