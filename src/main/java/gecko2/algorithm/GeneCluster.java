package gecko2.algorithm;

import gecko2.GeckoInstance;
import gecko2.algo.DeltaLocation;
import gecko2.algo.ReferenceCluster;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
public class GeneCluster implements Serializable, Comparable<GeneCluster> {
	
	public final static char TYPE_MEDIAN = 'm';
	public final static char TYPE_CENTER = 'c';
	public final static char TYPE_REFERENCE = 'r';

	private static final long serialVersionUID = -5371037483783752995L;

	int id;
	int[] genes;
	int size;
	private boolean match;
	private BigDecimal bestPValue;
	private BigDecimal bestPValueCorrected;
	private int minTotalDist;
	private GeneClusterOccurrence[] bestOccurrences;
	private GeneClusterOccurrence[] allOccurrences;
	private char type;
	// The index of the subsequence containing the reference genecluster
	private int refSeqIndex;
	
	public GeneCluster(int id, 
			GeneClusterOccurrence[] bestOccurrences, 
			GeneClusterOccurrence[] allOccurrences, 
			int[] genes, 
			double bestPValueBase,
			int bestPValueExp,
			double bestPValueCorrectedBase,
			int bestPValueCorrectedExp,
			int minTotalDist, 
			int refSeqIndex,
			char type) 
	{
		this(id, bestOccurrences, allOccurrences, genes, (new BigDecimal(bestPValueBase)).scaleByPowerOfTen(bestPValueExp), (new BigDecimal(bestPValueCorrectedBase)).scaleByPowerOfTen(bestPValueCorrectedExp), minTotalDist, refSeqIndex, type);		
	}
		
	public GeneCluster(int id, 
			GeneClusterOccurrence[] bestOccurrences, 
			GeneClusterOccurrence[] allOccurrences, 
			int[] genes, 
			BigDecimal bestPValue, 
			BigDecimal bestPValueCorrected,
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
		this.bestPValueCorrected = bestPValueCorrected;
		this.bestOccurrences = bestOccurrences;
		this.allOccurrences = allOccurrences;
		this.refSeqIndex = refSeqIndex;
		this.minTotalDist = minTotalDist;
		this.type = type;

		this.genes = genes;
		this.id = id;
	}
	
	public GeneCluster(int id, ReferenceCluster refCluster){
		this.id = id;
		this.match = true;
		this.bestPValue = refCluster.getBestCombined_pValue();
		this.bestPValueCorrected = refCluster.getBestCombined_pValueCorrected();
		this.refSeqIndex = refCluster.getGenomeNr();
		this.type = TYPE_REFERENCE;
		this.genes = new int[refCluster.getGeneContent().size()];
		for (int i=0; i<refCluster.getGeneContent().size(); i++)
			genes[i] = refCluster.getGeneContent().get(i);
		
		this.size = 0;
		this.minTotalDist = 0;
		int[] minDistances = refCluster.getMinimumDistances();
		for (Integer dist : minDistances){
			if (dist >= 0){
				this.size++;
				this.minTotalDist += dist;
			}
		}

		Subsequence[][] bestSubseqs = new Subsequence[refCluster.getAllDeltaLocations().size()][];
		Subsequence[][] allSubseqs = new Subsequence[refCluster.getAllDeltaLocations().size()][];
		for (int i=0; i<refCluster.getAllDeltaLocations().size(); i++){
			List<Subsequence> allSub = new ArrayList<Subsequence>(refCluster.getDeltaLocations(i).size());
			List<Subsequence> bestSub = new ArrayList<Subsequence>(refCluster.getDeltaLocations(i).size());
			for (DeltaLocation dLoc : refCluster.getDeltaLocations(i)){
				Subsequence subseq = new Subsequence(dLoc.getL(), dLoc.getR(), dLoc.getChrNr(), dLoc.getDistance(), new BigDecimal(dLoc.getpValue()));
				if (dLoc.getDistance() <= minDistances[i]){
					bestSub.add(subseq);
				}
				allSub.add(subseq);
			}
			bestSubseqs[i]=bestSub.toArray(new Subsequence[bestSub.size()]);
			allSubseqs[i]=allSub.toArray(new Subsequence[allSub.size()]);
		}
		this.bestOccurrences = new GeneClusterOccurrence[1];
		this.bestOccurrences[0] = new GeneClusterOccurrence(0, bestSubseqs, refCluster.getBestCombined_pValue(), minTotalDist, refCluster.getCoveredGenomes());
		this.allOccurrences = new GeneClusterOccurrence[1];
		this.allOccurrences[0] = new GeneClusterOccurrence(0, allSubseqs, refCluster.getBestCombined_pValue(), minTotalDist, refCluster.getCoveredGenomes());
	}
	
	public char getType() {
		return type;
	}
	
	public GeneClusterOccurrence[] getAllOccurrences() {
		return allOccurrences;
	}
	
	public BigDecimal getBestPValue() {
		return bestPValue;
	}
	
	public double getBestScore() {
		return -getBigDecimalLog(bestPValue);
	}
	
	public double getBestCorrectedScore() {
		return -getBigDecimalLog(bestPValueCorrected);
	}
	
	/**
	 * Computes the Log value of the given BigDecimal
	 * @param value the value
	 * @return Log(value)
	 */
	private static double getBigDecimalLog(BigDecimal value){
		String s = value.toString();
		String[] split = s.split("E");
		double base = Double.parseDouble(split[0]);
		double log = 0.0;
		if (split.length == 2){
			log = Double.parseDouble(split[1]);
			
		}
		log += Math.log10(base);
		return log;
	}
	
	public BigDecimal getBestPValueCorrected() {
		return bestPValueCorrected;
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
	
	public int getMinRefSeqLength() {
		if (refSeqIndex < 0)
			return -1;
		int minLength = Integer.MAX_VALUE;
		for (GeneClusterOccurrence occ : bestOccurrences){
			if (refSeqIndex >= occ.getSubsequences().length)
				return -1;
			Subsequence[] refSeqs = occ.getSubsequences()[refSeqIndex];
			for (Subsequence seq : refSeqs){
				if (seq.dist == 0)
					minLength = Math.min(minLength, (seq.stop - seq.start) + 1);
			}
		}
		if (minLength == Integer.MAX_VALUE)
			return -1;
		return minLength;
	}
	
	/**
	 * Returns the maximum pairwise distance in the gene cluster
	 * @return the maximum pairwise distance
	 */
	public int getMaxPWDist(){
		int maxPWDist = -1;
		for (GeneClusterOccurrence occ : allOccurrences){
			for (Subsequence[] refSeqs : occ.getSubsequences())
				for (Subsequence seq : refSeqs){
					maxPWDist = Math.max(maxPWDist, seq.dist);
				}
		}
		return maxPWDist;
	}
	
	/**
	 * Returns the minimum pairwise distance for any non reference occurrence
	 * @return the minimum pairwise distance
	 */
	public int getMinPWDist(){
		int minPWDist = minTotalDist;
		for (GeneClusterOccurrence occ : allOccurrences){
			for (int i=0; i<occ.getSubsequences().length; i++){
				if (i == refSeqIndex)
					continue;
				Subsequence[] refSeqs = occ.getSubsequences()[i];
				for (Subsequence seq : refSeqs){
					minPWDist = Math.min(minPWDist, seq.dist);
				}
			}
		}
		return minPWDist;
	}
	
	/**
	 * Returns the average pairwise distance of the best occurrences to the reference
	 * @return the average pairwise distance
	 */
	public double getAvgPWDist(){
		double avgDist = minTotalDist;
		for (GeneClusterOccurrence occ : allOccurrences){
			int avgPVDist = 0;
			int seqs = 0;
			
			for (int i=0; i<occ.getSubsequences().length; i++){
				if (i == refSeqIndex)
					continue;
				Subsequence[] refSeqs = occ.getSubsequences()[i];
				int minDist = 0;
				if (refSeqs.length != 0){
					minDist = minTotalDist;
					for (Subsequence seq : refSeqs){
						minDist = Math.min(minDist, seq.dist);
					}
					seqs++;
				}
				avgPVDist += minDist;
				
			}
			avgDist = Math.min(avgDist, (double)avgPVDist/seqs);
		}
		
		return avgDist;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
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
	 * @return a GeneClusterOutput object
	 */
	public GeneClusterOutput generateGeneClusterOutput() {
		return generateGeneClusterOutput(true);
	}
	
	/**
	 * Generates a @link GeneClusterOutput object, that contains all information about the gene cluster.
	 * @return a GeneClusterOutput object
	 */
	public GeneClusterOutput generateGeneClusterOutput(boolean onlyBestOccs) {
		GeneClusterOccurrence occ;
		if (onlyBestOccs)
			occ = this.getOccurrences()[0];
		else
			occ = this.getAllOccurrences()[0];
		
		GeneClusterOutput.Builder builder = new GeneClusterOutput.Builder(occ.getSubsequences().length);
		builder.pValue(this.getBestPValue());
		builder.refSeq(this.getRefSeqIndex());
		
		int[] subseqs = new int[occ.getSubsequences().length];
		int[] nrOccurrences = new int[occ.getSubsequences().length];
		List<List<String>> chromosomes = new ArrayList<List<String>>(occ.getSubsequences().length);
		List<List<Gene[]>> tempIntervals = new ArrayList<List<Gene[]>>(occ.getSubsequences().length);
		List<List<int[]>> intervalBorders = new ArrayList<List<int[]>>(occ.getSubsequences().length);
		for (int  i=0; i<occ.getSubsequences().length; i++) {
			if (occ.getSubsequences()[i].length > 0) {
				builder.setDistance(occ.getSubsequences()[i][0].getDist(), i);
				nrOccurrences[i] = occ.getSubsequences()[i].length;
				
				List<Gene[]> tempInt = new ArrayList<Gene[]>(occ.getSubsequences()[i].length);
				List<String> tmpChrom = new ArrayList<String>(occ.getSubsequences()[i].length);
				List<int[]> borders = new ArrayList<int[]>(occ.getSubsequences()[i].length);
				
				for (Subsequence seq : occ.getSubsequences()[i]) {
					int chrNr = seq.getChromosome();
					subseqs[i] = 0;
					Gene[] interval = new Gene[seq.getStop() - seq.getStart() + 1];
					Chromosome chrom = GeckoInstance.getInstance().getGenomes()[i].getChromosomes().get(chrNr);
					int[] border = new int[]{seq.start, seq.stop};
					int index = 0;
					for (int j=seq.getStart(); j<=seq.getStop(); j++) {
						interval[index] = chrom.getGenes().get(j-1);
						index++;
					}
					tempInt.add(interval);
					tmpChrom.add(GeckoInstance.getInstance().getGenomes()[i].getFullChromosomeName(chrNr));
					borders.add(border);
				}
				chromosomes.add(tmpChrom);
				tempIntervals.add(tempInt);
				intervalBorders.add(borders);
			} else {
				subseqs[i] = -1;
				builder.setDistance(-1, i);
				chromosomes.add(null);
				tempIntervals.add(null);
				intervalBorders.add(null);
				nrOccurrences[i] = 0;
			}
		}
		builder.chromosomes(chromosomes);
		Map<Integer, Gene[][]> annotations = GeckoInstance.getInstance().generateAnnotations(this, 
				occ);
		
		Map<Integer, Gene[][]> newAnnotations = new HashMap<Integer, Gene[][]>();
		for (Integer geneId : annotations.keySet())
			newAnnotations.put(Math.abs(geneId), annotations.get(geneId));
		
		builder.annotations(newAnnotations);
		
		List<List<List<Integer>>> intervals = new ArrayList<List<List<Integer>>>(tempIntervals.size());
		for (List<Gene[]> oldIntervalsPerGenome : tempIntervals) {
			if (oldIntervalsPerGenome != null) {
				List<List<Integer>> newIntevalsPerGenome = new ArrayList<List<Integer>>(oldIntervalsPerGenome.size());
				for (Gene[] genes : oldIntervalsPerGenome) {
					List<Integer> interval = new ArrayList<Integer>(genes.length);
					for (Gene gene : genes) {
						//String[] geneLabel = GeckoInstance.getInstance().getGenLabelMap().get(Math.abs(gene.getId()));
						interval.add(gene.getId());// >= 0 ? geneLabel : -geneLabel);
					}
					newIntevalsPerGenome.add(interval);
				}
				intervals.add(newIntevalsPerGenome);
			} else {
				intervals.add(null);
			}
		}

		
		builder.intervals(intervals);
		
		builder.intervalBorders(intervalBorders);
		
		builder.nrOfOccurrences(nrOccurrences);
		
		return builder.build();
	}
	
	/**
	 * Generates a reduced list of the gene clusters, keeping of all similar clusters only the one with the lowest p-Value.
	 * Returns a @Link SortedSet of the indices of the kept clusters.  
	 * @param allClusters The array of gene clusters
	 * @return The SortedSet, containing the indices of the clusters that are to keep
	 */
	public static SortedSet<Integer> generateReducedClusterList(GeneCluster[] allClusters) {
		return generateSimilarityReducedClusterList(allClusters);
	}
	
	/**
	 * Generates a reduced list of the gene clusters, keeping of all similar clusters only the one with the lowest p-Value.
	 * Returns a @Link SortedSet of the indices of the kept clusters.  
	 * @param allClusters The array of gene clusters
	 * @return The SortedSet, containing the indices of the clusters that are to keep
	 */
	private static SortedSet<Integer> generateSimilarityReducedClusterList(GeneCluster[] allClusters) {
		SortedSet<Integer> reducedList = new TreeSet<Integer>();
		
		GeneCluster[] tmp = Arrays.copyOf(allClusters, allClusters.length);
		Arrays.sort(tmp);
		for (GeneCluster geneCluster : tmp) {
			boolean contained = false;
			for (Iterator<Integer> it = reducedList.iterator(); it.hasNext() && !contained; ) {
				int index = it.next();
				GeneCluster cluster = allClusters[index];
				assert(cluster.getId() == index);
				if (geneCluster.isSimilar(cluster)) { // if similar
					int compare = geneCluster.bestPValue.compareTo(cluster.bestPValue);
					if (compare > 0)// if similar, but worse then the previously inserted cluster
						contained = true;
					else if (compare == 0){
						if (geneCluster.size < cluster.size)
							contained = true;
						else 
							it.remove();
					}
					else
						it.remove(); // if similar, but better then the previously inserted cluster
				}
			}
			if (!contained)
				reducedList.add(geneCluster.getId());		
		}
		return reducedList;
	}
	
	private static SortedSet<Integer> generateInternalDuplicationReducedClusterList(GeneCluster[] allClusters) {
		SortedSet<Integer> reducedList = new TreeSet<Integer>();
		List<List<GeneCluster>> clusterGroups = groupSimilarClusters(allClusters);
		for (List<GeneCluster> clusterGroup : clusterGroups) {
			while (clusterGroup.size() != 0) {
				GeneCluster bestCluster = getLargestCluster(clusterGroup);
				reducedList.add(bestCluster.getId());
				Iterator<GeneCluster> it = clusterGroup.iterator();
				while (it.hasNext()){
					GeneCluster cluster = it.next();
					if (bestCluster.isSimilar(cluster))
						it.remove();
				}
			}
		}
		return reducedList;
	}
	
	private static GeneCluster getLargestCluster(List<GeneCluster> clusterGroup){
		GeneCluster bestCluster = null;
		int highestNumberOfIntervals = -1;
		for (GeneCluster cluster : clusterGroup) {
			if (cluster.getNumberOfIntervals() > highestNumberOfIntervals) {
				highestNumberOfIntervals = cluster.getNumberOfIntervals();
				bestCluster = cluster;
			}
		}
		return bestCluster;
	}
	
	private int getNumberOfIntervals() {
		int numberOfIntervals = 0;
		for (GeneClusterOccurrence occ : allOccurrences) {
			for (Subsequence[] subseqs : occ.getSubsequences())
				numberOfIntervals += subseqs.length;
		}
		return numberOfIntervals;
	}
	
	private static List<List<GeneCluster>> groupSimilarClusters(GeneCluster[] allClusters) {
		List<List<GeneCluster>> resultList = new ArrayList<List<GeneCluster>>();
		
		for (GeneCluster cluster : allClusters){
			List<GeneCluster> newGroup = new ArrayList<GeneCluster>();
			newGroup.add(cluster);
			
			Iterator<List<GeneCluster>> listIter = resultList.iterator();
			while (listIter.hasNext()){
				List<GeneCluster> groupedClusters = listIter.next();
				for (GeneCluster groupedCluster : groupedClusters) {
					if (groupedCluster.isSimilar(cluster)){
						newGroup.addAll(groupedClusters);
						listIter.remove();
						break;
					}
				}
			}
			resultList.add(newGroup);
		}
		
		return resultList;
	}
	
	/**
	 * Tests, if this gene cluster is similar to the the other gene cluster
	 * @param other the other gene cluster
	 * @return if they are similar
	 */
	private boolean isSimilar(GeneCluster other) {
		for (int i=0; i < this.bestOccurrences.length; i++) {
			boolean similar = false;
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
								similar = true;
							if (seq.stop > oSeq.start && seq.stop < oSeq.stop)
								similar = true;
							if (oSeq.start >= seq.start && oSeq.start <= seq.stop)
								similar = true;
							if (oSeq.stop > seq.start && oSeq.stop < seq.stop)
								similar = true;
						}
					}
				}
			}
			return (similar && possibleSimilarOcc);
		}
		return false;
	}
	
	public static GeneCluster[] mergeResults(GeneCluster[] oldResults, List<GeneCluster> additionalResults) {
		return mergeResults(oldResults, additionalResults.toArray(new GeneCluster[additionalResults.size()]));
	}
	
	public static GeneCluster[] mergeResults(GeneCluster[] oldResults, GeneCluster[] additionalResults) {
		GeneCluster[] newResults;
		if (oldResults == null)
			newResults = additionalResults;
		else if(additionalResults == null)
			newResults = oldResults;
		else {
			newResults = Arrays.copyOf(oldResults, oldResults.length + additionalResults.length);
			int newId = oldResults.length;
			for (GeneCluster cluster : additionalResults) {
				cluster.id = newId;
				newResults[newId] =  cluster;
				newId++;
			}	
		}
		return newResults;
	}

	@Override
	public int compareTo(GeneCluster other) {
		return this.bestPValue.compareTo(other.bestPValue);
	}
}
