package gecko2.algorithm;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Contains all the information about a gene cluster that is need for printing
 * @author swinter
 */
public class GeneClusterOutput {
	private final BigDecimal pValue;
	private final int refSeq;
	private final int[] distances; 
	private final Map<Integer, Gene[][]> geneAnnotations;
	/**
	 * The gene ids for all the intervals. The first list contains one entry per genome. 
	 * The second contains one entry per occurrence on the genome. The third contains
	 * the gene ids for the occurrence.
	 */
	private final List<List<List<Integer>>> intervals;
	/**
	 * The borders for all the intervals. The first list contains one entry per genome. 
	 * The second contains one entry per occurrence on the genome. The array is always
	 * of size 2 and contains the left and right borders of the interval.
	 */
	private final List<List<int[]>> intervalBorders;

	private final List<List<String>> chromosomes;
	private final int[] nrOfOccurrences;

	public static class Builder {
		private final int nrOfSequences;
		
		private BigDecimal pValue;
		private int refSeq;
		private final int[] distances;
		private Map<Integer, Gene[][]> geneAnnotations;
		private List<List<List<Integer>>> intervals;
		private List<List<int[]>> intervalBorders;
		private List<List<String>> chromosomes;

		private int[] nrOfOccurrences;
		
		public Builder (int nrOfSequences) {
			this.nrOfSequences = nrOfSequences;
			
			distances = new int[this.nrOfSequences];
			Arrays.fill(distances, 0);
		}
		
		public Builder pValue(BigDecimal pV) {
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
		
		public Builder annotations(Map<Integer, Gene[][]> geneAnnotations) {
			this.geneAnnotations = geneAnnotations;
			return this;
		}
		
		public void intervals(List<List<List<Integer>>> intervals) {
			this.intervals = intervals;
		}
		
		public void intervalBorders(List<List<int[]>> intervalBorders){
			this.intervalBorders = intervalBorders;
		}

		public Builder chromosomes(List<List<String>> chrom) {
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
		intervalBorders = builder.intervalBorders;
		chromosomes = builder.chromosomes;
		nrOfOccurrences = Arrays.copyOf(builder.nrOfOccurrences, builder.nrOfOccurrences.length);
	}

	public List<List<String>> getChromosomes() {
		return chromosomes;
	}

	public BigDecimal getPValue() {
		return pValue;
	}

	public int getRefSeq() {
		return refSeq;
	}

	public int[] getDistances() {
		return distances;
	}

	public Map<Integer, Gene[][]> getGeneAnnotations() {
		return geneAnnotations;
	}
	
	public List<List<List<Integer>>> getIntervals() {
		return intervals;
	}
	
	public List<List<int[]>> getIntervalBorders() {
		return intervalBorders;
	}
	
	public int[] getNrOfOccurrences() {
		return nrOfOccurrences;
	}
}
