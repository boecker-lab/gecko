package gecko2.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreakPointDistance {
	
	public static void breakPointDistance(Genome[] genomes, boolean signed) {
		breakPointDistance(genomes, signed, false);
	}
	
	public static void breakPointDistanceRandom(Genome[] genomes, boolean signed) {
		breakPointDistance(genomes, signed, true);
	}
	
	private static void breakPointDistance(Genome[] genomes, boolean signed, boolean random) {
		int[][] result = computeBreakPointDistance(genomes, signed, random);
		printBreakPointDistanceArray(result, genomes);
	}
	
	public static int[][] computeBreakPointDistance(Genome[] genomes, boolean signed, boolean random) {
		List<int[][]> chromosomes = new ArrayList<int[][]>(genomes.length);
		for (Genome g: genomes){
			int[][] chromArray = new int[g.getChromosomes().size()][];
			for (int i=0; i<g.getChromosomes().size(); i++){
				if (random)
					chromArray[i] = g.getChromosomes().get(i).toRandomIntArray(false, false);
				else
					chromArray[i] = g.getChromosomes().get(i).toIntArray(false, false);
			}
			chromosomes.add(chromArray);
		}
		return computeBreakPointDistance(chromosomes, signed);
	}
	
	public static int[][] computeBreakPointDistance(List<int[][]> genomes, boolean signed) {
		int[][] result = new int[genomes.size()][genomes.size()];
		
		List<BreakPointVector> breakPointVectors = new ArrayList<BreakPointVector> (genomes.size());
		for (int i=0; i<genomes.size(); i++) {
			BreakPointVector currentVector = new BreakPointVector(genomes.get(i), signed);

			breakPointVectors.add(currentVector);
			
			for (int j=0; j<i; j++) {
				int distance = currentVector.getDistance(breakPointVectors.get(j));
				result[i][j] = distance;
				result[j][i] = distance;
			}
			result[i][i] = 0;
		}
		
		return result;
	}
	
	static void printBreakPointDistanceColumn(int [][] distances, Genome[] genomes) {
		for (int i=0; i<genomes.length; i++) {
			for (int j=0; j<i; j++)
				System.out.println(String.format("%s \t %s:\t %d", genomes[i].getName(), genomes[j].getName(), distances[i][j]));
		}
	}	
	
	static void printBreakPointDistanceArray(int [][] distances, Genome[] genomes) {
		System.out.print("Name\t");
		for (int i=0; i<genomes.length; i++)
			System.out.print(genomes[i].getName() + "\t");
		System.out.println();
		for (int i=0; i<genomes.length; i++) {
			System.out.print(genomes[i].getName() + "\t");
			for (int j=0; j<distances[i].length; j++){
				System.out.print(distances[i][j]/(double)(genomes[i].getTotalGeneNumber()-1 + genomes[j].getTotalGeneNumber()-1));
				if (j!=distances[i].length-1)
					System.out.print("\t");
			}
			System.out.println();
		}
	}
	
	private static class BreakPointVector {
		Map<IntPair, Integer> values;
		
		public BreakPointVector(int[][] genome, boolean signed) {
			values = new HashMap<IntPair, Integer>();
			if (genome.length == 0) 
				return;

			for (int[] chrom : genome){
				int firstPos = 0;
				for (int secondPos=1; secondPos<chrom.length; secondPos++){
					IntPair pair;
					if (signed)
						pair = new SignedIntPair(chrom[firstPos], chrom[secondPos]);
					else
						pair = new UnsignedIntPair(chrom[firstPos], chrom[secondPos]);
					Integer occs = values.get(pair);
					if(occs == null)
						values.put(pair, 1);
					else
						values.put(pair, occs+1);
					firstPos++;
				}
			}
		}
		
		public int getDistance(BreakPointVector otherVector) {
			Map<IntPair, Integer> otherDuplicate = new HashMap<IntPair, Integer>(otherVector.values);
			
			int breakpoints = 0;
			
			for (Map.Entry<IntPair, Integer> entry : values.entrySet()) {
				Integer otherValue = otherDuplicate.get(entry.getKey());
				
				if (otherValue == null)
					breakpoints += entry.getValue();
				else {
					breakpoints += Math.abs(entry.getValue() - otherValue);
					otherDuplicate.remove(entry.getKey());
				}
			}
			for (Map.Entry<IntPair, Integer> entry : otherDuplicate.entrySet()) {
				breakpoints += entry.getValue();
			}
			return breakpoints;
		}
	}
	
	private static interface IntPair {
		public int getFirst();
		public int getSecond();
	}
	
	/**
	 * Stores a pair of signed integers (the two gene ids) used as key in the breakpoint vector
	 * Equality is given when either a_1 == b_1 && a_2 == b_2 or a_1 == -b_2 && a_2 == -b_1  
	 * @author swinter
	 */
	private static class SignedIntPair implements IntPair{
		private final int first;
		private final int second;
		
		SignedIntPair(int first, int second) {
			if (first > second) {
				this.first = first;
				this.second = second;
			} else {
				this.first = -second;
				this.second = -first;
			}
		}

		public int getFirst() {
			return first;
		}

		public int getSecond() {
			return second;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof SignedIntPair))
				return false;
			SignedIntPair otherPair = (SignedIntPair)other;
			return ((this.first==otherPair.first && this.second == otherPair.second)
					|| (this.first==-otherPair.second && this.second == -otherPair.first));
		}
		
		@Override
		public int hashCode() {
			int hash = 23;
			if (Math.abs(first) > Math.abs(second)) {
				hash = hash * 31 - second;
				hash = hash * 31 - first;
			} else if (Math.abs(first) == Math.abs(second) && first < 0){
				hash = hash * 31 - second;
				hash = hash * 31 - first;
			} else {
				hash = hash * 31 + first;
				hash = hash * 31 + second;
			}
			return hash;
		}
		
		@Override
		public String toString(){
			return String.format("(%d, %d)", first, second);
		}
	}
	
	/**
	 * Stores a pair of integers (the two gene ids) used as key in the breakpoint vector.
	 * Any signs of the integers are ignored.
	 * Equality is given when either a_1 == b_1 && a_2 == b_2 or a_1 == b_2 && a_2 == b_1  
	 * @author swinter
	 */
	private static class UnsignedIntPair implements IntPair{
		private final int first;
		private final int second;
		
		UnsignedIntPair(int first, int second) {
			this.first = Math.max(Math.abs(first), Math.abs(second));
			this.second = Math.min(Math.abs(first), Math.abs(second));
		}

		public int getFirst() {
			return first;
		}

		public int getSecond() {
			return second;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof UnsignedIntPair))
				return false;
			UnsignedIntPair otherPair = (UnsignedIntPair)other;
			return (this.first==otherPair.first && this.second == otherPair.second);
		}
		
		@Override
		public int hashCode() {
			int hash = 23;
			hash = hash * 31 + first;
			hash = hash * 31 + second;
			
			return hash;
		}
		
		@Override
		public String toString(){
			return String.format("(%d, %d)", first, second);
		}
	}
}

