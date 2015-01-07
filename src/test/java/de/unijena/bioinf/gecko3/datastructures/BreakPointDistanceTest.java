/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.datastructures;

import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.io.CogFileReader;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class BreakPointDistanceTest {

	@Test
	public void shouldGiveDistanceZero() {
        int[][][] testChromosomes = new int[][][] {{{1, 2, 3, 4, 5, 6, 7}}, {{1, 2, 3, 4, 5, 6, 7}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceZeroInverse() {
        int[][][] testChromosomes = new int[][][] {{{1, 2, 3, 4, 5, 6, 7}}, {{-7, -6, -5, -4, -3, -2, -1}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceTwo() {
        int[][][] testChromosomes = new int[][][] {{{1, 2}}, {{1, 3}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, true);
	}
	
	@Test
	public void shouldGiveDifferentDistanceWhenSigned() {
        int[][][] testChromosomes = new int[][][] {{{1, 2}}, {{2, 1}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, true);
	}
	
	@Test
	public void shouldGiveSameDistanceWhenSigned() {
        int[][][] testChromosomes = new int[][][] {{{1, 2}}, {{-2, -1}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceTwoInverse() {
        int[][][] testChromosomes = new int[][][] {{{1, 2}}, {{-3, -1}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceOne() {
        int[][][] testChromosomes = new int[][][] {{{1, 1}}, {{1, 1, 1}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 1},{1, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 1},{1, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceThree() {
        int[][][] testChromosomes = new int[][][] {{{2, 2, 2}}, {{1, 1}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 3},{3, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 3},{3, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistancesTwo() {
        int[][][] testChromosomes = new int[][][] {{{1, 2, 3}}, {{2, 3, 5}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 2},{2, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceOneWithTwoChromosomes() {
        int[][][] testChromosomes = new int[][][] {{{1, 2, 3, 4, 5, 6, 7}}, {{1, 2, 3, 4}, {5, 6, 7}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 1},{1, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 1},{1, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceZeroWithTwoChromosomes() {
        int[][][] testChromosomes = new int[][][] {{{1, 2, 3, 4}, {5, 6, 7}}, {{1, 2, 3, 4}, {5, 6, 7}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceZeroWithTwoChromosomesDifferentOrder() {
        int[][][] testChromosomes = new int[][][] {{{1, 2, 3, 4}, {5, 6, 7}}, {{5, 6, 7}, {1, 2, 3, 4}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 0},{0, 0}}, true);
	}
		
		
	@Test
	public void shouldGiveDistanceEleven() {
        int[][][] testChromosomes = new int[][][] {{{1, 2, 3, 3, 5, 2, 5, 7, 9}}, {{2, 3, 5, 5, 6, 2, 10, 7, 9, 20}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 11},{11, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 11},{11, 0}}, true);
	}
	
	@Test
	public void shouldGiveDistanceNine() {
		int[][][] testChromosomes = new int[][][] {{{1, 2, 3, 5, 5, 2, 5, 7, 9}}, {{2, 3, -5, -5, 6, 2, 10, -9, -7, 20}}};
		
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 9},{9, 0}}, false);
		breakpointDistanceTest(testChromosomes, new int[][]{{0, 11},{11, 0}}, true);
	}

	private void breakpointDistanceTest(int[][][] chromosomes, int[][] expectedDistances, boolean signed) {
		int[][] breakPointDistances = BreakPointDistance.computeBreakPointDistance(chromosomes, signed);
		
		compareResults(expectedDistances, breakPointDistances);
	}
	
	private void breakpointDistanceTest(DataSet data, int[][] expectedDistances, boolean signed) {
		int[][] breakPointDistances = BreakPointDistance.computeBreakPointDistance(data, true, signed);
		
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
        GeckoInstance.getInstance().setCurrentWorkingDirectoryOrFile(inputFile);

		DataSet data = reader.readData();
		
		breakpointDistanceTest(data, new int[][] {{0, 14, 20, 20}, {14, 0, 22, 24},{20, 22, 0, 24},{20, 24, 24, 0}}, false);
	}
	
	@Test
	public void testGrouping() throws Exception{
		File inputFile = new File(getClass().getResource("/c.cog").getFile());
        CogFileReader reader = new CogFileReader(inputFile);
		GeckoInstance.getInstance().setCurrentWorkingDirectoryOrFile(inputFile);

		DataSet data = reader.readData();
		
		List<Set<Integer>> cluster = BreakPointDistance.groupGenomes(data, 0.7, false);
		assertEquals(2, cluster.size());
		
		assertEquals(3, cluster.get(0).size());
		assertTrue(cluster.get(0).contains(0));
		assertTrue(cluster.get(0).contains(1));
		assertTrue(cluster.get(0).contains(3));
		
		assertEquals(1, cluster.get(1).size());
		assertTrue(cluster.get(1).contains(2));
		
		cluster = BreakPointDistance.groupGenomes(data, 0.9, false);
		assertEquals(1, cluster.size());
		
		assertEquals(4, cluster.get(0).size());
		assertTrue(cluster.get(0).contains(0));
		assertTrue(cluster.get(0).contains(1));
		assertTrue(cluster.get(0).contains(3));
		assertTrue(cluster.get(0).contains(2));
		
		cluster = BreakPointDistance.groupGenomes(data, 0.1, false);
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
