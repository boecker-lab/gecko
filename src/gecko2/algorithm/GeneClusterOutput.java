package gecko2.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GeneClusterOutput {
	private final double pValue;
	private final int refSeq;
	private final int[] distances; 
	private final Map<Integer, Gene[]> geneAnnotations;
	private final List<List<Integer>> intervals;
	private final List<String> chromosomes;
	private final int[] nrOfOccurrences;

	public static class Builder {
		private final int nrOfSequences;
		
		private double pValue;
		private int refSeq;
		private int[] distances;
		private Map<Integer, Gene[]> geneAnnotations;
		private List<List<Integer>> intervals;
		private List<String> chromosomes;

		private int[] nrOfOccurrences;
		
		public Builder (int nrOfSequences) {
			this.nrOfSequences = nrOfSequences;
			
			distances = new int[this.nrOfSequences];
			Arrays.fill(distances, 0);
		}
		
		public Builder pValue(double pV) {
			pValue = pV;
			return this;
		}
		
		public Builder refSeq(int rS) {
			refSeq = rS;
			return this;
		}
		
		public Builder setDistance(int d, int i) {
			distances[i] = d;
			return this;
		}
		
		public Builder annotations(Map<Integer, Gene[]> geneAnnotations) {
			this.geneAnnotations = geneAnnotations;
			return this;
		}
		
		public void intervals(List<List<Integer>> intervals) {
			this.intervals = intervals;
		}

		public Builder chromosomes(List<String> chrom) {
			this.chromosomes = chrom;
			return this;
		}

		public Builder nrOfOccurrences(int[] nrOfOccurrences) {
			this.nrOfOccurrences = nrOfOccurrences;
			return this;
		}
		
		public GeneClusterOutput build() {
			return new GeneClusterOutput(this);
		}
	}
	
	private GeneClusterOutput(Builder builder) {
		pValue = builder.pValue;
		refSeq = builder.refSeq;
		distances = Arrays.copyOf(builder.distances, builder.distances.length);
		geneAnnotations = builder.geneAnnotations;
		intervals = builder.intervals;
		chromosomes = builder.chromosomes;
		nrOfOccurrences = Arrays.copyOf(builder.nrOfOccurrences, builder.nrOfOccurrences.length);
	}

	public List<String> getChromosomes() {
		return chromosomes;
	}

	public double getPValue() {
		return pValue;
	}

	public int getRefSeq() {
		return refSeq;
	}

	public int[] getDistances() {
		return distances;
	}

	public Map<Integer, Gene[]> getGeneAnnotations() {
		return geneAnnotations;
	}
	
	public List<List<Integer>> getIntervals() {
		return intervals;
	}

	public List<Gene> getGeneAnnotationsForGenom(int genomeIndex){
		ArrayList<Gene> result = new ArrayList<Gene>();

		for (Integer geneid : geneAnnotations.keySet()) {
			Gene gene = geneAnnotations.get(geneid)[genomeIndex];
			if (gene != null)
				result.add(gene);
		}
		return result;
	}
	
	public int[] getNrOfOccurrences() {
		return nrOfOccurrences;
	}
}
