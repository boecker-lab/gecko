package gecko2.algorithm;

import gecko2.GeckoInstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * @author Leon Kuchenbecker <lkuchenb@inf.fu-berlin.de>
 *
 */
public class GeneCluster implements Serializable {
	
	public final static char TYPE_MEDIAN = 'm';
	public final static char TYPE_CENTER = 'c';
	public final static char TYPE_REFERENCE = 'r';

	private static final long serialVersionUID = -5371037483783752995L;

	int id;
	int[] genes;
	int size;
	private boolean match;
	private double bestPValue;
	private int minTotalDist;
	private GeneClusterOccurrence[] bestOccurrences;
	private GeneClusterOccurrence[] allOccurrences;
	private char type;
	// The index of the subsequence containing the reference genecluster
	private int refSeqIndex;
	
	public char getType() {
		return type;
	}
	
	public GeneClusterOccurrence[] getAllOccurrences() {
		return allOccurrences;
	}
	
	public double getBestPValue() {
		return bestPValue;
	}
	
	public double getBestScore() {
		return -Math.log(bestPValue);
	}
	
	public int getMinTotalDist() {
		return minTotalDist;
	}
	
	public boolean isMatch() {
		return match;
	}
	
	public void setMatch(boolean match) {
		this.match = match;
	}
	
	public int getRefSeqIndex() {
		return refSeqIndex;
	}
	
	public GeneClusterOccurrence[] getOccurrences() {
		return bestOccurrences;
	}
		
	public GeneCluster(int id, 
			GeneClusterOccurrence[] bestOccurrences, 
			GeneClusterOccurrence[] allOccurrences, 
			int[] genes, 
			double bestPValue, 
			int minTotalDist, 
			int refSeqIndex,
			char type) 
	{
		match=true;
		//TODO check if this right
		size=0;
		if (allOccurrences!=null && allOccurrences.length!=0)
			for (Subsequence[] subSeqs : allOccurrences[0].getSubsequences())
				if (subSeqs.length>0) size++;
		
		this.bestPValue = bestPValue;
		this.bestOccurrences = bestOccurrences;
		this.allOccurrences = allOccurrences;
		this.refSeqIndex = refSeqIndex;
		this.minTotalDist = minTotalDist;
		this.type = type;

		this.genes = genes;
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
		
	public int[] getGenes() {
		return genes;
	}
	
	public void setGenes(int[] genes) {
		this.genes = genes;
	}
	
	public int getSize() {
		return size;
	}
	
	/**
	 * Generates a @link GeneClusterOutput object, that contains all information about the gene cluster.
	 * @return
	 */
	public GeneClusterOutput generateGeneClusterOutput() {
		GeneClusterOccurrence occ = this.getOccurrences()[0];
		
		GeneClusterOutput.Builder builder = new GeneClusterOutput.Builder(occ.getSubsequences().length);
		builder.pValue(this.getBestPValue());
		builder.refSeq(this.getRefSeqIndex());
		
		int[] subseqs = new int[occ.getSubsequences().length];
		List<String> chromosomes = new ArrayList<String>(occ.getSubsequences().length);
		List<Gene[]> tempIntervals = new ArrayList<Gene[]>(occ.getSubsequences().length);
		for (int  i=0; i<occ.getSubsequences().length; i++) {
			if (occ.getSubsequences()[i].length > 0) {
				int chrNr = occ.getSubsequences()[i][0].getChromosome();
				subseqs[i] = 0;
				builder.setDistance(occ.getSubsequences()[i][0].getDist(), i);
				chromosomes.add(GeckoInstance.getInstance().getGenomes()[i].getChromosomes().get(chrNr).getName());
				Gene[] interval = new Gene[occ.getSubsequences()[i][0].getStop() - occ.getSubsequences()[i][0].getStart() + 1];
				Chromosome chrom = GeckoInstance.getInstance().getGenomes()[i].getChromosomes().get(chrNr);
				int index = 0;
				for (int j=occ.getSubsequences()[i][0].getStart(); j<=occ.getSubsequences()[i][0].getStop(); j++) {
					interval[index] = chrom.getGenes().get(j-1);
					index++;
				}
				tempIntervals.add(interval);
					
			} else {
				subseqs[i] = -1;
				builder.setDistance(-1, i);
				chromosomes.add(null);
				tempIntervals.add(null);
			}
		}
		builder.chromosomes(chromosomes);
		Map<Integer, Gene[]> annotations = GeckoInstance.getInstance().generateAnnotations(this, 
				occ,
				subseqs);
		
		Map<Integer, Gene[]> newAnnotations = new HashMap<Integer, Gene[]>();
		for (Integer geneId : annotations.keySet())
			newAnnotations.put(GeckoInstance.getInstance().getGenLabelMap()[Math.abs(geneId)], annotations.get(geneId));
		
		builder.annotations(newAnnotations);
		
		List<List<Integer>> intervals = new ArrayList<List<Integer>>(tempIntervals.size());
		for (Gene[] genes : tempIntervals) {
			if (genes != null) {
				List<Integer> interval = new ArrayList<Integer>(genes.length);
				for (Gene gene : genes) {
					int geneLabel = GeckoInstance.getInstance().getGenLabelMap()[Math.abs(gene.getId())];
					interval.add(gene.getId() >= 0 ? geneLabel : -geneLabel);
				}
				intervals.add(interval);
			} else
				intervals.add(null);
		}
		
		builder.intervals(intervals);
		
		List<List<Gene>> geneAnnotations = new ArrayList<List<Gene>>(annotations.keySet().size());
		for (int genid : annotations.keySet()) {
			Gene[] genes = annotations.get(genid);
			List<Gene> annot = new ArrayList<Gene>(genes.length);
			for (Gene gene : genes) {
				annot.add(gene);
			}
			geneAnnotations.add(annot);
		}
		
		return builder.build();
	}
	
	/**
	 * Generates a reduced list of the gene clusters, keeping of all similar clusters only the one with the lowest p-Value.
	 * Returns a @Link SortedSet of the indices of the kept clusters.  
	 * @param allClusters The array of gene clusters
	 * @return The SortedSet, containing the indices of the clusters that are to keep
	 */
	public static SortedSet<Integer> generateReducedClusterList(GeneCluster[] allClusters) {
		SortedSet<Integer> reducedList = new TreeSet<Integer>();
		
		for (GeneCluster geneCluster : allClusters) {
			boolean contained = false;
			for (Iterator<Integer> it = reducedList.iterator(); it.hasNext() && !contained; ) {
				int index = it.next();
				GeneCluster cluster = allClusters[index];
				assert(cluster.getId() == index);
				int similarity = geneCluster.similarity(cluster);
				if (similarity != 0) { // if similar
					if (geneCluster.bestPValue > cluster.bestPValue)// if similar, but worse then the previously inserted cluster
						contained = true;
					else
						it.remove(); // if similar, but better then the previously inserted cluster
				}
			}
			if (!contained)
				reducedList.add(geneCluster.getId());		
		}
		return reducedList;
	}
	
	/**
	 * Tests, if this gene cluster is similar to the the other gene cluster
	 * @param other the other gene cluster
	 * @return 0 if they are not similar, 1 if it is similar
	 */
	private int similarity(GeneCluster other) {
		for (int i=0; i < this.bestOccurrences.length; i++) {
			int similarity = 0;
			boolean possibleSimilarOcc = true;
			if (this.bestOccurrences[i] == null || other.bestOccurrences[i] == null)
				continue;
			for (int genome=0; genome < this.bestOccurrences[i].getSubsequences().length; genome++) {
				Subsequence[] subsequences = this.bestOccurrences[i].getSubsequences()[genome];
				Subsequence[] otherSubsequences = other.bestOccurrences[i].getSubsequences()[genome];
				if ((subsequences.length == 0) ^ (otherSubsequences.length == 0)) {
					//possibleSimilarOcc = false;
					//break; //TODO not sure what is better!
					continue;
				}
				for (Subsequence seq : subsequences) {
					for (Subsequence oSeq : otherSubsequences) {
						if (seq.chromosome == oSeq.chromosome) {
							if (seq.start >= oSeq.start && seq.start <= oSeq.stop)
								similarity = 1;
							if (seq.stop > oSeq.start && seq.stop < oSeq.stop)
								similarity = 1;
							if (oSeq.start >= seq.start && oSeq.start <= seq.stop)
								similarity = 1;
							if (oSeq.stop > seq.start && oSeq.stop < seq.stop)
								similarity = 1;
						}
					}
				}
			}
			if (similarity != 0 && possibleSimilarOcc) 
				return similarity;
		}
		return 0;
	}
}
