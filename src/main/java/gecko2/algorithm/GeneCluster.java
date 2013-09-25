package gecko2.algorithm;

import gecko2.GeckoInstance;
import gecko2.algo.DeltaLocation;
import gecko2.algo.ReferenceCluster;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * 
 * @author Leon Kuchenbecker <lkuchenb@inf.fu-berlin.de>
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 */
public class GeneCluster implements Serializable, Comparable<GeneCluster> {
	
	public final static char TYPE_MEDIAN = 'm';
	public final static char TYPE_CENTER = 'c';
	public final static char TYPE_REFERENCE = 'r';

	private static final long serialVersionUID = -5371037483783752995L;

	private int id;
	private final int[] genes;
	private int size;
	private boolean match;
	private final BigDecimal bestPValue;
	private final BigDecimal bestPValueCorrected;
	private int minTotalDist;
	private final GeneClusterOccurrence[] bestOccurrences;
	private final GeneClusterOccurrence[] allOccurrences;
	private final char type;
	// The index of the subsequence containing the reference genecluster
	private final int refSeqIndex;
	
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
				if (seq.getDist() == 0)
					minLength = Math.min(minLength, (seq.getStop() - seq.getStart()) + 1);
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
					maxPWDist = Math.max(maxPWDist, seq.getDist());
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
					minPWDist = Math.min(minPWDist, seq.getDist());
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
						minDist = Math.min(minDist, seq.getDist());
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
					Gene[] interval = new Gene[seq.getStop() - seq.getStart() + 1];
					Chromosome chrom = GeckoInstance.getInstance().getGenomes()[i].getChromosomes().get(chrNr);
					int[] border = new int[]{seq.getStart(), seq.getStop()};
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
				builder.setDistance(-1, i);
				chromosomes.add(null);
				tempIntervals.add(null);
				intervalBorders.add(null);
				nrOccurrences[i] = 0;
			}
		}
		builder.chromosomes(chromosomes);
		Map<Integer, Gene[][]> annotations = generateAnnotations(occ);
		
		Map<Integer, Gene[][]> newAnnotations = new HashMap<Integer, Gene[][]>();
		for (Map.Entry<Integer, Gene[][]> entry : annotations.entrySet())
			newAnnotations.put(Math.abs(entry.getKey()), entry.getValue());
		
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
	
	@Override
	public int compareTo(GeneCluster other) {
		return this.bestPValue.compareTo(other.bestPValue);
	}
	
	/**
	 * Returns the tags of the gene in the reference occurrence
	 * @return the tags of the gene in the reference occurrence
	 */
	public String getReferenceTags() {
		Subsequence seq = bestOccurrences[0].getSubsequences()[getRefSeqIndex()][0];
		Genome genome = GeckoInstance.getInstance().getGenomes()[getRefSeqIndex()];
		List<String> tags = new ArrayList<String>();
		for (int index = seq.getStart()-1; index < seq.getStop(); index++){
			String newTag = genome.getChromosomes().get(seq.getChromosome()).getGenes().get(index).getTag();
			boolean merged = false;
			for (int i=0; i<tags.size(); i++) {
				String tag = tags.get(i);
				if (newTag.length() > 3 && tag.length() > 3 && newTag.substring(0, 3).equals(tag.substring(0,3))) {
					String mergedTag = tag.concat(newTag.substring(3));
					tags.set(i, mergedTag);
					merged = true;
					break;
				}
			}
			if (! merged)
				tags.add(newTag);
		}
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (String tag : tags){
			if (! first)
				builder.append(", ");
			else
				first = false;
			builder.append(tag);
		}
		return builder.toString();
	}

    /**
     * Creates a Map that assigns an array of Gene Object to each gene id. Each array element refers
     * to one of the currently observed genomes.
     * @return A map with the gene id as the key and an array of Gene Objects as the value. The i-th
     * array element refers to the i-th currently observed genome, e.g. array[3] assigned to id 5 is
     * a reference to the Gene Object with id 5 in genome 3. If an array element is a null reference
     * a gene with that id does not occur in the subsequence (that is refered by the GeneCluster)
     * of that genome.
     */
    public Map<Integer, Gene[]> generateAnnotations(GeneClusterOccurrence gOcc, int[] subselection) {
        GeckoInstance instance = GeckoInstance.getInstance();

        Subsequence[][] subsequences = gOcc.getSubsequences();
        HashMap<Integer, Gene[]> map = new HashMap<Integer, Gene[]>();
        for (int gene : genes) {
            if (instance.getGenLabelMap().get(gene) != null)
                map.put(gene, new Gene[subsequences.length]);
        }
        for (int seqnum=0; seqnum<subsequences.length; seqnum++) {
            if (subsequences[seqnum].length<=subselection[seqnum] ||
                    subselection[seqnum]==GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                continue;
            Subsequence subseq = subsequences[seqnum][subselection[seqnum]];
            Chromosome chromosome = instance.getGenomes()[seqnum].getChromosomes().get(subseq.getChromosome());
            for (int i=subseq.getStart()-1; i<subseq.getStop(); i++) {
                Gene gene = chromosome.getGenes().get(i);
                if (map.containsKey(Math.abs(gene.getId()))) {
                    Gene[] g = map.get(Math.abs(gene.getId()));
                    if (g[seqnum]==null) g[seqnum]=gene;
                }
            }
        }
        return map;
    }

    public static int getMaximumIdLength(Map<Integer, Gene[]> annotations) {
        int maxIdLength = -1;

        for (Integer geneId : annotations.keySet())
            if (geneId > maxIdLength)
                maxIdLength = geneId;

        return maxIdLength;
    }

    public Map<Integer, Gene[][]> generateAnnotations(GeneClusterOccurrence gOcc) {
        GeckoInstance instance = GeckoInstance.getInstance();
        Subsequence[][] subsequences = gOcc.getSubsequences();
        HashMap<Integer, Gene[][]> map = new HashMap<Integer, Gene[][]>();
        for (int gene : genes) {
            if (instance.getGenLabelMap().get(gene) != null) {
                Gene[][] geneArray = new Gene[subsequences.length][];
                for (int i=0; i<subsequences.length; i++)
                    geneArray[i] = new Gene[subsequences[i].length];
                map.put(gene, geneArray);
            }
        }
        for (int seqnum=0; seqnum<subsequences.length; seqnum++) {
            for (int i=0; i<subsequences[seqnum].length; i++) {
                Subsequence subseq = subsequences[seqnum][i];
                Chromosome chromosome = instance.getGenomes()[seqnum].getChromosomes().get(subseq.getChromosome());
                for (int j=subseq.getStart()-1; j<subseq.getStop(); j++) {
                    Gene gene = chromosome.getGenes().get(j);
                    if (map.containsKey(Math.abs(gene.getId()))) {
                        Gene[][] g = map.get(Math.abs(gene.getId()));
                        if (g[seqnum][i]==null)
                            g[seqnum][i] = gene;
                    }
                }
            }
        }
        return map;
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
						if (seq.getChromosome() == oSeq.getChromosome()) {
							if (seq.getStart() >= oSeq.getStart() && seq.getStart() <= oSeq.getStop())
								similar = true;
							if (seq.getStop() > oSeq.getStart() && seq.getStop() < oSeq.getStop())
								similar = true;
							if (oSeq.getStart() >= seq.getStart() && oSeq.getStart() <= seq.getStop())
								similar = true;
							if (oSeq.getStop() > seq.getStart() && oSeq.getStop() < seq.getStop())
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
}
