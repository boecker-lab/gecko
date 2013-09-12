package gecko2.algorithm;

import gecko2.GeckoInstance;
import gecko2.io.CogFileReader;
import gecko2.io.CogFileReaderTest;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class BreakPointDistanceTest {

	@Test
	public void shouldGiveDistanceZero() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2, 3, 4, 5, 6, 7}});
		testChromosomes.add(new int[][]{{1, 2, 3, 4, 5, 6, 7}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceZeroInverse() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2, 3, 4, 5, 6, 7}});
		testChromosomes.add(new int[][]{{-7, -6, -5, -4, -3, -2, -1}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceTwo() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2}});
		testChromosomes.add(new int[][]{{1, 3}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, true);
	}
	
	@Test
	public void shouldGiveDifferentDistanceWhenSigned() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2}});
		testChromosomes.add(new int[][]{{2, 1}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, true);
	}
	
	@Test
	public void shouldGiveSameDistanceWhenSigned() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2}});
		testChromosomes.add(new int[][]{{-2, -1}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceTwoInverse() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2}});
		testChromosomes.add(new int[][]{{-3, -1}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceOne() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 1}});
		testChromosomes.add(new int[][]{{1, 1, 1}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 1},{1, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 1},{1, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceThree() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{2, 2, 2}});
		testChromosomes.add(new int[][]{{1, 1}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 3},{3, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 3},{3, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistancesTwo() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2, 3}});
		testChromosomes.add(new int[][]{{2, 3, 5}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceOneWithTwoChromosomes() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2, 3, 4, 5, 6, 7}});
		testChromosomes.add(new int[][]{{1, 2, 3, 4}, {5, 6, 7}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 1},{1, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 1},{1, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceZeroWithTwoChromosomes() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2, 3, 4}, {5, 6, 7}});
		testChromosomes.add(new int[][]{{1, 2, 3, 4}, {5, 6, 7}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceZeroWithTwoChromosomesDifferentOrder() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2, 3, 4}, {5, 6, 7}});
		testChromosomes.add(new int[][]{{5, 6, 7}, {1, 2, 3, 4}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, true);
	}
		
		
	@Test
	public void shouldGiveDistanceEleven() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2, 3, 3, 5, 2, 5, 7, 9}});
		testChromosomes.add(new int[][]{{2, 3, 5, 5, 6, 2, 10, 7, 9, 20}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 11},{11, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 11},{11, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceNine() {
		List<int[][]> testChromosomes = new ArrayList<int[][]>(2);
		testChromosomes.add(new int[][]{{1, 2, 3, 5, 5, 2, 5, 7, 9}});
		testChromosomes.add(new int[][]{{2, 3, -5, -5, 6, 2, 10, -9, -7, 20}});
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 9},{9, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 11},{11, 0}}, true);
	}

	private void breakpointDistanceTest(List<int[][]> chromosomes, int[][] expectedDistances, boolean signed) {
		int[][] breakPointDistances = BreakPointDistance.computeBreakPointDistance(chromosomes, signed);
		
		compareResults(expectedDistances, breakPointDistances);
	}
	
	private void breakpointDistanceTest(Genome[] genomes, int[][] expectedDistances, boolean signed) {
		int[][] breakPointDistances = BreakPointDistance.computeBreakPointDistance(genomes, true, signed);
		
		compareResults(expectedDistances, breakPointDistances);
	}
	
	private void compareResults(int[][] expected, int[][] actual) {
		assertEquals(expected.length, actual.length);
		for (int i=0; i<expected.length; i++) {
			assertEquals(expected[i].length, actual[i].length);
			for (int j=0; j<actual[i].length; j++) 
				assertEquals(String.format("at (%d/%d)", i, j), expected[i][j], actual[i][j]);
		}
	}
	
	@Test
	public void testFromGenomes() throws Exception{
		File inputFile = new File(getClass().getResource("/c.cog").getFile());
        CogFileReader reader = new CogFileReader(inputFile);
        GeckoInstance.getInstance().setCurrentInputFile(inputFile);

        reader.readData();

		Genome[] genomes = reader.getGenomes();
		
		breakpointDistanceTest(genomes, new int[][] {{0, 14, 20, 20}, {14, 0, 22, 24},{20, 22, 0, 24},{20, 24, 24, 0}}, false);
	}
	
	@Test
	public void testGrouping() throws Exception{
		File inputFile = new File(getClass().getResource("/c.cog").getFile());
        CogFileReader reader = new CogFileReader(inputFile);
		GeckoInstance.getInstance().setCurrentInputFile(inputFile);

        reader.readData();
				
		Genome[] genomes = reader.getGenomes();
		
		List<Set<Integer>> cluster = BreakPointDistance.groupGenomes(genomes, 0.7, false);
		assertEquals(2, cluster.size());
		
		assertEquals(3, cluster.get(0).size());
		assertTrue(cluster.get(0).contains(0));
		assertTrue(cluster.get(0).contains(1));
		assertTrue(cluster.get(0).contains(3));
		
		assertEquals(1, cluster.get(1).size());
		assertTrue(cluster.get(1).contains(2));
		
		cluster = BreakPointDistance.groupGenomes(genomes, 0.9, false);
		assertEquals(1, cluster.size());
		
		assertEquals(4, cluster.get(0).size());
		assertTrue(cluster.get(0).contains(0));
		assertTrue(cluster.get(0).contains(1));
		assertTrue(cluster.get(0).contains(3));
		assertTrue(cluster.get(0).contains(2));
		
		cluster = BreakPointDistance.groupGenomes(genomes, 0.1, false);
		assertEquals(4, cluster.size());
		
		assertEquals(1, cluster.get(0).size());
		assertTrue(cluster.get(0).contains(0));
		
		assertEquals(1, cluster.get(1).size());
		assertTrue(cluster.get(1).contains(1));
		
		assertEquals(1, cluster.get(2).size());
		assertTrue(cluster.get(2).contains(2));
		
		assertEquals(1, cluster.get(3).size());
		assertTrue(cluster.get(3).contains(3));
	}

}
