package gecko2.datastructures;

import gecko2.GeckoInstance;
import gecko2.algo.DeltaLocation;
import gecko2.algo.ReferenceCluster;
import gecko2.gui.GeneClusterLocationSelection;

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
	private static final long serialVersionUID = -5371037483783752995L;

    private static final String noOCC = "*";

	private int id;
	private final Set<GeneFamily> geneFamilies;
	private int size;
	private boolean match;
	private final BigDecimal bestPValue;
	private final BigDecimal bestPValueCorrected;
	private int minTotalDist;
	private final GeneClusterOccurrence bestOccurrences;
	private final GeneClusterOccurrence allOccurrences;
	private final Parameter.OperationMode type;
	// The index of the subsequence containing the reference genecluster
	private final int refSeqIndex;

	public GeneCluster(int id, 
			GeneClusterOccurrence bestOccurrences,
			GeneClusterOccurrence allOccurrences,
            Set<GeneFamily> geneFamilies,
            BigDecimal bestPValue,
			BigDecimal bestPValueCorrected,
			int minTotalDist, 
			int refSeqIndex,
			Parameter.OperationMode type)
	{
		match=true;
		//TODO check if this right
		size=0;
		if (allOccurrences!=null)
			for (Subsequence[] subSeqs : allOccurrences.getSubsequences())
				if (subSeqs.length>0) size++;

		this.bestPValue = bestPValue;
		this.bestPValueCorrected = bestPValueCorrected;
		this.bestOccurrences = bestOccurrences;
		this.allOccurrences = allOccurrences;
        this.geneFamilies = new HashSet<>(geneFamilies);
		this.refSeqIndex = refSeqIndex;
		this.minTotalDist = minTotalDist;
		this.type = type;

		this.id = id;
	}

    public GeneCluster(int id, ReferenceCluster refCluster, DataSet data){
        this.id = id;
        this.match = true;
        this.bestPValue = refCluster.getBestCombined_pValue();
        this.bestPValueCorrected = refCluster.getBestCombined_pValueCorrected();
        this.refSeqIndex = refCluster.getGenomeNr();
        this.type = Parameter.OperationMode.reference;

        this.size = 0;
        this.minTotalDist = 0;
        int[] minDistances = refCluster.getMinimumDistances();
        for (Integer dist : minDistances){
            if (dist >= 0){
                this.size++;
                this.minTotalDist += dist;
            }
        }

        geneFamilies = new HashSet<>();
        for (int i=refCluster.getLeftBorder()-1; i<refCluster.getRightBorder() && geneFamilies.size()<refCluster.getSize(); i++){
            geneFamilies.add(data.getGenomes()[refCluster.getGenomeNr()].getChromosomes().get(refCluster.getChrNr()).getGenes().get(i).getGeneFamily());
        }


        Subsequence[][] bestSubseqs = new Subsequence[refCluster.getAllDeltaLocations().size()][];
        Subsequence[][] allSubseqs = new Subsequence[refCluster.getAllDeltaLocations().size()][];
        for (int i=0; i<refCluster.getAllDeltaLocations().size(); i++){
            List<Subsequence> allSub = new ArrayList<>(refCluster.getDeltaLocations(i).size());
            List<Subsequence> bestSub = new ArrayList<>(refCluster.getDeltaLocations(i).size());
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
        this.bestOccurrences = new GeneClusterOccurrence(0, bestSubseqs, refCluster.getBestCombined_pValue(), minTotalDist, refCluster.getCoveredGenomes());
        this.allOccurrences = new GeneClusterOccurrence(0, allSubseqs, refCluster.getBestCombined_pValue(), minTotalDist, refCluster.getCoveredGenomes());
    }
	
	public Parameter.OperationMode getType() {
		return type;
	}

    public GeneClusterOccurrence getOccurrences(boolean includeSubOptimalOccurrences){
        return includeSubOptimalOccurrences ? allOccurrences : bestOccurrences;
    }
	
	public BigDecimal getBestPValue() {
		return bestPValue;
	}

    public BigDecimal getBestPValueCorrected() {
        return bestPValueCorrected;
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
	
	public int getMinRefSeqLength() {
		if (refSeqIndex < 0)
			return -1;
		int minLength = Integer.MAX_VALUE;
        if (refSeqIndex >= bestOccurrences.getSubsequences().length)
            return -1;
        Subsequence[] refSeqs = bestOccurrences.getSubsequences()[refSeqIndex];
        for (Subsequence seq : refSeqs){
            if (seq.getDist() == 0)
                minLength = Math.min(minLength, (seq.getStop() - seq.getStart()) + 1);
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
        for (Subsequence[] refSeqs : allOccurrences.getSubsequences())
            for (Subsequence seq : refSeqs){
                maxPWDist = Math.max(maxPWDist, seq.getDist());
            }
		return maxPWDist;
	}
	
	/**
	 * Returns the minimum pairwise distance for any non reference occurrence
	 * @return the minimum pairwise distance
	 */
	public int getMinPWDist(){
		int minPWDist = minTotalDist;
        for (int i=0; i<allOccurrences.getSubsequences().length; i++){
            if (i == refSeqIndex)
                continue;
            Subsequence[] refSeqs = allOccurrences.getSubsequences()[i];
            for (Subsequence seq : refSeqs){
                minPWDist = Math.min(minPWDist, seq.getDist());
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
        int avgPVDist = 0;
        int seqs = 0;

        for (int i=0; i<allOccurrences.getSubsequences().length; i++){
            if (i == refSeqIndex)
                continue;
            Subsequence[] refSeqs = allOccurrences.getSubsequences()[i];
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
		
		return avgDist;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
		
	public Set<GeneFamily> getGeneFamilies() {
		return geneFamilies;
	}

    public String getGeneFamilyString() {
        int iMax = geneFamilies.size() - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        int i=0;
        for (GeneFamily geneFamily : geneFamilies) {
            b.append(geneFamily.getExternalId());
            if (i++ == iMax)
                b.append(']');
            else
                b.append(", ");
        }
        return b.toString();
    }
	
	public int getSize() {
		return size;
	}

    /**
     * Check if the gene cluster has an occurrence in the given genome
     * @param genomes the indices of the genomes
     * @return
     */
    public boolean hasNoOccurrenceInGenomes(Set<Integer> genomes){
        for (Integer genomeIndex : genomes) {
            for (int i = 0; i < allOccurrences.getSubsequences()[genomeIndex].length; i++) {
                if (allOccurrences.getSubsequences()[genomeIndex][i].isValid()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if the gene cluster has an occurrence in the given genome
     * @param genomes the indices of the genomes
     * @return
     */
    public boolean hasOccurrenceInAllGenomes(Set<Integer> genomes){
        for (Integer genomeIndex : genomes) {
            boolean hasOccurrence = false;
            for (int i = 0; i < allOccurrences.getSubsequences()[genomeIndex].length; i++) {
                if (allOccurrences.getSubsequences()[genomeIndex][i].isValid()) {
                    hasOccurrence = true;
                }
            }
            if (!hasOccurrence)
                return false;
        }
        return true;
    }
	
	/**
	 * Generates a @link GeneClusterOutput object, that contains all information about the gene cluster.
	 * @return a GeneClusterOutput object
	 */
	public GeneClusterOutput generateGeneClusterOutput(boolean includeSubOptimalOccurrences) {
		GeneClusterOccurrence occ = getOccurrences(includeSubOptimalOccurrences);
		
		GeneClusterOutput.Builder builder = new GeneClusterOutput.Builder(occ.getSubsequences().length, this.id);
		builder.pValue(this.getBestPValue());
		builder.refSeq(this.getRefSeqIndex());

		int[] nrOccurrences = new int[occ.getSubsequences().length];
		List<List<String>> chromosomes = new ArrayList<>(occ.getSubsequences().length);
		List<List<Gene[]>> tempIntervals = new ArrayList<>(occ.getSubsequences().length);
		List<List<int[]>> intervalBorders = new ArrayList<>(occ.getSubsequences().length);
		for (int  i=0; i<occ.getSubsequences().length; i++) {
			if (occ.getSubsequences()[i].length > 0) {
				builder.setDistance(occ.getSubsequences()[i][0].getDist(), i);
				nrOccurrences[i] = occ.getSubsequences()[i].length;
				
				List<Gene[]> tempInt = new ArrayList<>(occ.getSubsequences()[i].length);
				List<String> tmpChrom = new ArrayList<>(occ.getSubsequences()[i].length);
				List<int[]> borders = new ArrayList<>(occ.getSubsequences()[i].length);
				
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
		Map<GeneFamily, Gene[][]> annotations = generateAnnotations(includeSubOptimalOccurrences);
		
		Map<GeneFamily, Gene[][]> newAnnotations = new HashMap<>();
		for (Map.Entry<GeneFamily, Gene[][]> entry : annotations.entrySet())
			newAnnotations.put(entry.getKey(), entry.getValue());
		
		builder.annotations(newAnnotations);
		
		List<List<List<Gene>>> intervals = new ArrayList<>(tempIntervals.size());
		for (List<Gene[]> oldIntervalsPerGenome : tempIntervals) {
			if (oldIntervalsPerGenome != null) {
				List<List<Gene>> newIntevalsPerGenome = new ArrayList<>(oldIntervalsPerGenome.size());
				for (Gene[] genes : oldIntervalsPerGenome) {
					List<Gene> interval = new ArrayList<>(genes.length);
					for (Gene gene : genes) {
						//String[] geneLabel = GeckoInstance.getInstance().getGenLabelMap().get(Math.abs(gene.getExternalId()));
						interval.add(gene);// >= 0 ? geneLabel : -geneLabel);
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
	public static List<GeneCluster> generateReducedClusterList(List<GeneCluster> allClusters) {
		return generateSimilarityReducedClusterList(allClusters);
	}
	
	/**
	 * Generates a reduced list of the gene clusters, keeping of all similar clusters only the one with the lowest p-Value.
	 * Returns a @Link SortedSet of the indices of the kept clusters.  
	 * @param allClusters The list of gene clusters
	 * @return the reduced list of gene clusters
	 */
	private static List<GeneCluster> generateSimilarityReducedClusterList(List<GeneCluster> allClusters) {
        if (allClusters.isEmpty())
            return new ArrayList<>();
		List<GeneCluster> reducedList = new ArrayList<>();

        List<GeneCluster> geneClustersCopy = new ArrayList<>(allClusters);
		Collections.sort(geneClustersCopy);
		for (GeneCluster geneCluster : geneClustersCopy) {
            boolean contained = false;
			for (Iterator<GeneCluster> it = reducedList.iterator(); it.hasNext() && !contained; ) {
                GeneCluster cluster = it.next();
				if (geneCluster.isSimilar(cluster)) { // if similar
					int compare = geneCluster.bestPValue.compareTo(cluster.bestPValue);
					if (compare > 0) // if similar, but worse then the previously inserted cluster
						contained = true;
					else if (compare == 0){ // if similar, and same p-value, keep the one with bigger size
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
				reducedList.add(geneCluster);
		}
		return reducedList;
	}
	
	private static SortedSet<Integer> generateInternalDuplicationReducedClusterList(GeneCluster[] allClusters) {
		SortedSet<Integer> reducedList = new TreeSet<>();
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
        for (Subsequence[] subseqs : allOccurrences.getSubsequences())
            numberOfIntervals += subseqs.length;
		return numberOfIntervals;
	}
	
	@Override
	public int compareTo(GeneCluster other) {
		return this.bestPValue.compareTo(other.bestPValue);
	}

    public int getNoOfGenesInRefOcc() {
        Subsequence seq = bestOccurrences.getSubsequences()[getRefSeqIndex()][0];

        return seq.getStop() - seq.getStart() + 1;
    }

    public boolean invalidMultiGeneFamilyGeneCluster(int minClusterSize, Genome[] genomes) {
        Subsequence seq = bestOccurrences.getSubsequences()[getRefSeqIndex()][0];
        Genome genome = genomes[getRefSeqIndex()];

        Set<GeneFamily> nonMergedGeneFamilies = getNonMergedGeneFamilies(seq, genome);
        List<Set<GeneFamily>> mergedGeneFamilies = getMergedGeneFamilies(seq, genome, nonMergedGeneFamilies);
        if (!mergedGeneFamilies.isEmpty()) {
            if (nonMergedGeneFamilies.size() + mergedGeneFamilies.size() < minClusterSize){
                return true;
            }
            //return correctForMergedGeneFamilie(parameters, nonMergedGeneFamilies, mergedGeneFamilies, genomes);
        }
        return false;
    }

    /*private boolean correctForMergedGeneFamilie(Parameter parameter, Set<GeneFamily> nonMergedGeneFamilies, List<Set<GeneFamily>> mergedGeneFamilies, Genome[] genomes) {
        GeneClusterOccurrence occ = getOccurrences()[0];
        for (int i=0; i<occ.getSubsequences().length; i++){
            if (occ.getSubsequences()[i].length == 0)
                continue;
            if (i == getRefSeqIndex())
                continue;

            Genome genome = genomes[i];
            for (Subsequence seq : occ.getSubsequences()[i]){
                Set<GeneFamily> geneFamilies = getGeneFamiliesOfSubsequence(seq, genome);
                int nonMissingFamilies = 0;
                for (GeneFamily geneFamily : geneFamilies){
                    if (nonMergedGeneFamilies.contains(geneFamily))
                        nonMissingFamilies++;
                    else {
                        for (Set<GeneFamily> gFSet : mergedGeneFamilies){
                            if (gFSet.contains(geneFamily))
                                nonMissingFamilies++;
                        }
                }}
            }
        }

    }*/

    private static List<Set<GeneFamily>> getMergedGeneFamilies(Subsequence seq, Genome genome, Set<GeneFamily> nonMergedGeneFamilies){
        List<Set<GeneFamily>> mergedGeneFamilies = new ArrayList<>();

        String locusTag = "";
        Set<GeneFamily> locusTagCombinedSet = new HashSet<>();
        for (int index = seq.getStart()-1; index < seq.getStop(); index++){
            Gene gene = genome.getChromosomes().get(seq.getChromosome()).getGenes().get(index);
            if (locusTag.equals("") || !locusTag.equals(gene.getTag())){  // new locus tag
                if (locusTagCombinedSet.size()>1)
                    mergedGeneFamilies.add(new HashSet<>(locusTagCombinedSet));
                locusTagCombinedSet.clear();
            }
            if (!nonMergedGeneFamilies.contains(gene.getGeneFamily()))
                locusTagCombinedSet.add(gene.getGeneFamily());
            locusTag = gene.getTag();
        }
        if (locusTagCombinedSet.size()>1)
            mergedGeneFamilies.add(locusTagCombinedSet);
        return mergedGeneFamilies;
    }

    private static Set<GeneFamily> getNonMergedGeneFamilies(Subsequence seq, Genome genome){
        String locusTag = "";
        GeneFamily lastGeneFamily = null;
        Set<GeneFamily> nonMergedGeneFamilies = new HashSet<>();
        for (int index = seq.getStart()-1; index < seq.getStop(); index++){
            Gene gene = genome.getChromosomes().get(seq.getChromosome()).getGenes().get(index);
            if (!(locusTag.equals("")) && locusTag.equals(gene.getTag())) {
                lastGeneFamily=null;
                locusTag = gene.getTag();
            } else {
                if (lastGeneFamily != null)
                    nonMergedGeneFamilies.add(lastGeneFamily);
                lastGeneFamily = gene.getGeneFamily();
                locusTag = gene.getTag();
            }
        }
        if (lastGeneFamily != null)
            nonMergedGeneFamilies.add(lastGeneFamily);
        return nonMergedGeneFamilies;
    }


    Set<GeneFamily> getGeneFamiliesOfSubsequence(Subsequence seq, Genome genome){
        Set<GeneFamily> geneFamilies = new HashSet<>();
        for (int index = seq.getStart()-1; index < seq.getStop(); index++){
            geneFamilies.add(genome.getChromosomes().get(seq.getChromosome()).getGenes().get(index).getGeneFamily());
        }
        return geneFamilies;
    }

    public GeneClusterLocationSelection getDefaultLocationSelection(boolean includeSubOptimalOccurrences){
        int[] subSelection = getDefaultSubSelection(includeSubOptimalOccurrences);
        GeneFamily geneFamily = getBestConservedGeneFamily();
        return getGeneClusterLocationSelection(geneFamily, subSelection, includeSubOptimalOccurrences);
    }

    public GeneClusterLocationSelection getGeneClusterLocationSelection(GeneFamily geneFamily, int[] subSelection, boolean includeSubOptimalOccurrences){
        int[] alignmentGenesCluster = getGeneFamilyClusterPositions(geneFamily, subSelection, includeSubOptimalOccurrences);
        int[] alignmentGenesGlobal = getGeneFamilyChromosomePositions(geneFamily, subSelection, includeSubOptimalOccurrences);
        boolean[] flipped = getClusterAlignmentFlipped(subSelection, alignmentGenesGlobal, includeSubOptimalOccurrences);
        return new GeneClusterLocationSelection(this, subSelection, includeSubOptimalOccurrences, flipped, alignmentGenesCluster, alignmentGenesGlobal);
    }

    private GeneFamily getBestConservedGeneFamily() {
        return null;
    }

    public boolean[] getClusterAlignmentFlipped(int[] subselection, int[] alignmentGenesGlobal, boolean includeSubOptimalOccurrences) {
        ArrayList<Integer> minus = new ArrayList<>();
        ArrayList<Integer> plus = new ArrayList<>();
        for (int i=0; i< alignmentGenesGlobal.length;i++) {
            // If genome i is not in the cluster, skip
            if (alignmentGenesGlobal[i] == -1)
                continue;
            List<Chromosome> genes = GeckoInstance.getInstance().getGenomes()[i].getChromosomes();
            Subsequence s = this.getOccurrences(includeSubOptimalOccurrences).getSubsequences()[i][subselection[i]];
            if (genes.get(s.getChromosome()).getGenes().get(alignmentGenesGlobal[i]).getOrientation().equals(Gene.GeneOrientation.NEGATIVE))
                minus.add(i);
            else
                plus.add(i);
        }
        boolean[] flipped = new boolean[subselection.length];
        if (minus.size()>plus.size()) {
            for (Integer i : plus)
                flipped[i] = true;
            for (Integer i : minus)
                flipped[i] = false;
        } else {
            for (Integer i : plus)
                flipped[i] = false;
            for (Integer i : minus)
                flipped[i] = true;
        }
        return flipped;
    }

    public int[] getGeneFamilyChromosomePositions(GeneFamily geneFamily, int[] subSelection, boolean includeSubOptimalOccurrences){
        GeneClusterOccurrence gOcc = getOccurrences(includeSubOptimalOccurrences);
        int[] centerGenes = new int[gOcc.getSubsequences().length];
        Arrays.fill(centerGenes, -1);
        for (int i=0; i<centerGenes.length;i++) {
            // If genome i is not in the cluster, skip
            if (subSelection[i]==GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                continue;
            List<Chromosome> genes = GeckoInstance.getInstance().getGenomes()[i].getChromosomes();
            Subsequence s = gOcc.getSubsequences()[i][subSelection[i]];
            for (int j=s.getStart()-1;j<s.getStop();j++) {
                if (genes.get(s.getChromosome()).getGenes().get(j).getGeneFamily().equals(geneFamily)) {
                    centerGenes[i] = j;
                    break;
                }
            }
        }
        return centerGenes;
    }

    public int[] getGeneFamilyClusterPositions(GeneFamily geneFamily, int[] subSelection, boolean includeSubOptimalOccurrences) {
        GeneClusterOccurrence gOcc = getOccurrences(includeSubOptimalOccurrences);
        int[] alignmentGenes = new int[gOcc.getSubsequences().length];
        Arrays.fill(alignmentGenes, -1);
        for (int i=0; i<alignmentGenes.length;i++) {
            // If genome i is not in the cluster, skip
            if (subSelection[i]==GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                continue;
            List<Chromosome> genes = GeckoInstance.getInstance().getGenomes()[i].getChromosomes();
            Subsequence s = gOcc.getSubsequences()[i][subSelection[i]];
            int localPosition = 0;
            for (int j=s.getStart()-1;j<s.getStop();j++, localPosition++) {
                if (genes.get(s.getChromosome()).getGenes().get(j).getGeneFamily().equals(geneFamily)) {
                    alignmentGenes[i] = localPosition;
                    break;
                }
            }
        }
        return alignmentGenes;
    }

    /**
     * Generates the default/initial sub-selection of cluster occurrences.
     * @param includeSubOptimalOccurrences if sub-optimal occurrences should also be included
     * @return an int[] with the index of each cluster occurrence that shall be used.
     */
    public int[] getDefaultSubSelection(boolean includeSubOptimalOccurrences) {
        GeneClusterOccurrence gOcc = getOccurrences(includeSubOptimalOccurrences);

        int[] subselections = new int[GeckoInstance.getInstance().getGenomes().length];

        for (int i = 0; i < subselections.length; i++) {
            if (gOcc.getSubsequences()[i].length == 0) {
                subselections[i] = GeneClusterOccurrence.GENOME_NOT_INCLUDED;
            }
            else {
                subselections[i] = 0;
            }
        }
        return subselections;
    }

    /**
	 * Tests, if this gene cluster is similar to the the other gene cluster
	 * @param other the other gene cluster
	 * @return if they are similar
	 */
	private boolean isSimilar(GeneCluster other) {
        boolean similar = false;
        boolean possibleSimilarOcc = true;
        if (this.bestOccurrences == null || other.bestOccurrences == null)
            return false;
        for (int genome=0; genome < this.bestOccurrences.getSubsequences().length; genome++) {
            Subsequence[] subsequences = this.bestOccurrences.getSubsequences()[genome];
            Subsequence[] otherSubsequences = other.bestOccurrences.getSubsequences()[genome];
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
	
	public static List<GeneCluster> mergeResults(List<GeneCluster> oldResults, List<GeneCluster> additionalResults) {
		if (oldResults == null)
			return additionalResults;
		else if(additionalResults == null)
			return oldResults;
		else {
			int newId = oldResults.size();
			for (GeneCluster cluster : additionalResults) {
				cluster.id = newId;
				oldResults.add(cluster);
				newId++;
			}
            return oldResults;
		}
	}
	
	private static List<List<GeneCluster>> groupSimilarClusters(GeneCluster[] allClusters) {
		List<List<GeneCluster>> resultList = new ArrayList<>();
		
		for (GeneCluster cluster : allClusters){
			List<GeneCluster> newGroup = new ArrayList<>();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneCluster that = (GeneCluster) o;

        if (minTotalDist != that.minTotalDist) return false;
        if (size != that.size) return false;
        if (!geneFamilies.equals(that.geneFamilies)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = geneFamilies.hashCode();
        result = 31 * result + size;
        result = 31 * result + minTotalDist;
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GeneCluster{" +
                "id=" + id +
                ", geneFamilies=" + getGeneFamilyString() +
                ", size=" + size +
                ", bestPValue=" + bestPValue +
                ", minTotalDist=" + minTotalDist +
                ", refSeqIndex=" + refSeqIndex +
                '}';
    }

    /* SOME OUTPUT HELPER STUFF */

    /**
     * Returns the gene of the given occ, in the given genome and the given subselection.
     * @param includeSubOptimalOccurrences
     * @param genomeIndex
     * @param subselection
     * @return
     */
    public List<Gene> getGenes(boolean includeSubOptimalOccurrences, int genomeIndex, int subselection){
        List<Gene> genes = new ArrayList<>();
        Subsequence seq = getOccurrences(includeSubOptimalOccurrences).getSubsequences()[genomeIndex][subselection];
        Genome genome = GeckoInstance.getInstance().getGenomes()[genomeIndex];
        for (int index = seq.getStart()-1; index < seq.getStop(); index++){
            genes.add(genome.getChromosomes().get(seq.getChromosome()).getGenes().get(index));
        }
        return genes;
    }

    public String getGenomeString() {
        Subsequence[][] seqs = bestOccurrences.getSubsequences();
        Genome[] genomes = GeckoInstance.getInstance().getGenomes();
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<seqs.length; i++) {
            if (seqs[i].length > 0)
                builder.append(genomes[i].getName()).append("; ");
        }
        return builder.toString();
    }

    /**
     * Returns the tags of the gene in the reference occurrence
     * @return the tags of the gene in the reference occurrence
     */
    public String getReferenceGeneNames() {
        return getGeneNames(getRefSeqIndex());
    }

    public String getGeneNames(int genome_index) {
        if (bestOccurrences.getSubsequences()[genome_index].length == 0)
            return noOCC;
        Subsequence seq = bestOccurrences.getSubsequences()[genome_index][0];
        Genome genome = GeckoInstance.getInstance().getGenomes()[genome_index];

        SortedMap<String, List<String>> nameMap = new TreeMap<>();
        for (int index = seq.getStart()-1; index < seq.getStop(); index++){
            String newName = genome.getChromosomes().get(seq.getChromosome()).getGenes().get(index).getName();
            boolean merged = false;
            if (newName.length() > 3) {
                String prefix = newName.substring(0, 3);
                String suffix = newName.substring(3);
                if (nameMap.containsKey(prefix)){
                    nameMap.get(prefix).add(suffix);
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(suffix);
                    nameMap.put(prefix, list);
                }
            } else {
                if (nameMap.containsKey(newName))
                    nameMap.get(newName).add("-");
                else {
                    List<String> list = new ArrayList<>();
                    list.add("-");
                    nameMap.put(newName, list);
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String prefix : nameMap.keySet()){
            if (!first)
                builder.append(", ");
            else
                first = false;

            addSamePrefixString(prefix, nameMap.get(prefix), builder);
        }
        return builder.toString();
    }

    public String getLocusTags(int genome_index){
        if (bestOccurrences.getSubsequences()[genome_index].length == 0)
            return noOCC;

        Subsequence seq = bestOccurrences.getSubsequences()[genome_index][0];
        Genome genome = GeckoInstance.getInstance().getGenomes()[genome_index];

        StringBuilder builder = new StringBuilder();
        for (int index = seq.getStart()-1; index < seq.getStop(); index++) {
            if (index != seq.getStart()-1) {
                builder.append(", ");
            }
            builder.append(genome.getChromosomes().get(seq.getChromosome()).getGenes().get(index).getTag());
        }
        return builder.toString();
    }

    /**
     * A list of strings, that indicate how well the cluster occs in the given genome are conserved.
     * Each string starts with the number of missing genes, then the number of additional genes and
     * finally for each gene in the cluster "+" if it is missing in the reference occ, or a singleton
     * gene, or the external gene family id.
     * @param genome_index
     * @param allOccurrences
     * @return
     */
    public List<String> getGeneConservation(int genome_index, boolean allOccurrences) {
        GeneClusterOccurrence occ = getOccurrences(allOccurrences);
        List<String> results = new ArrayList<>(occ.getSubsequences()[genome_index].length);

        for (Subsequence seq : occ.getSubsequences()[genome_index]){
            Genome genome = GeckoInstance.getInstance().getGenomes()[genome_index];
            Set<GeneFamily> containedGenes = new HashSet<>();
            StringBuilder builder = new StringBuilder();
            for (int index = seq.getStart()-1; index < seq.getStop(); index++) {
                Gene gene = genome.getChromosomes().get(seq.getChromosome()).getGenes().get(index);
                if (gene.getGeneFamily().isSingleGeneFamily()) {
                    builder.append("+ ");
                } else if (geneFamilies.contains(gene.getGeneFamily())){
                    containedGenes.add(gene.getGeneFamily());
                    builder.append(gene.getGeneFamily().getExternalId());
                    builder.append(" ");
                } else {
                    builder.append("+ ");
                }
            }
            int missingGenes = geneFamilies.size() - containedGenes.size();
            results.add(String.format("%d\t%d\t%s", missingGenes, seq.getDist()-missingGenes, builder.toString()));
        }
        return results;
    }

    public String getGeneOrientations(int genome_index) {
        if (bestOccurrences.getSubsequences()[genome_index].length == 0)
            return noOCC;

        Subsequence seq = bestOccurrences.getSubsequences()[genome_index][0];
        Genome genome = GeckoInstance.getInstance().getGenomes()[genome_index];

        StringBuilder builder = new StringBuilder();
        for (int index = seq.getStart()-1; index < seq.getStop(); index++){
            builder.append(genome.getChromosomes().get(seq.getChromosome()).getGenes().get(index).getOrientation().getEncoding());
        }
        return builder.toString();
    }

    private void addSamePrefixString(String prefix, List<String> suffixes, StringBuilder builder) {
        Collections.sort(suffixes);
        if (prefix.trim().equals("")) {
            for (Iterator<String> it = suffixes.iterator(); it.hasNext(); ){
                String suffix = it.next();
                builder.append(suffix);
                if (it.hasNext())
                    builder.append(", ");
            }
        } else {
            builder.append(prefix);
            for (String suffix : suffixes)
                builder.append(suffix.trim().equals("") ? "-" : suffix);
        }
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
    public Map<GeneFamily, Gene[]> generateAnnotations(boolean includeSubOptimalOccurrences, int[] subselection) {
        GeckoInstance instance = GeckoInstance.getInstance();

        Subsequence[][] subsequences = getOccurrences(includeSubOptimalOccurrences).getSubsequences();
        HashMap<GeneFamily, Gene[]> map = new HashMap<>();
        for (GeneFamily gene : geneFamilies) {
            if (!gene.isSingleGeneFamily())
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
                if (map.containsKey(gene.getGeneFamily())) {
                    Gene[] g = map.get(gene.getGeneFamily());
                    if (g[seqnum]==null) g[seqnum]=gene;
                }
            }
        }
        return map;
    }

    public static int getMaximumIdLength(Map<GeneFamily, Gene[]> annotations) {
        int maxIdLength = -1;

        for (GeneFamily geneFamily : annotations.keySet())
            if (geneFamily.getExternalId().length() > maxIdLength)
                maxIdLength = geneFamily.getExternalId().length();

        return maxIdLength;
    }

    public Map<GeneFamily, Gene[][]> generateAnnotations(boolean includeSubOptimalOccurrences) {
        GeckoInstance instance = GeckoInstance.getInstance();
        Subsequence[][] subsequences = getOccurrences(includeSubOptimalOccurrences).getSubsequences();
        HashMap<GeneFamily, Gene[][]> map = new HashMap<>();
        for (GeneFamily geneFamily : geneFamilies) {
            if (!geneFamily.isSingleGeneFamily()) {
                Gene[][] geneArray = new Gene[subsequences.length][];
                for (int i=0; i<subsequences.length; i++)
                    geneArray[i] = new Gene[subsequences[i].length];
                map.put(geneFamily, geneArray);
            }
        }
        for (int seqnum=0; seqnum<subsequences.length; seqnum++) {
            for (int i=0; i<subsequences[seqnum].length; i++) {
                Subsequence subseq = subsequences[seqnum][i];
                Chromosome chromosome = instance.getGenomes()[seqnum].getChromosomes().get(subseq.getChromosome());
                for (int j=subseq.getStart()-1; j<subseq.getStop(); j++) {
                    Gene gene = chromosome.getGenes().get(j);
                    if (map.containsKey(gene.getGeneFamily())) {
                        Gene[][] g = map.get(gene.getGeneFamily());
                        if (g[seqnum][i]==null)
                            g[seqnum][i] = gene;
                    }
                }
            }
        }
        return map;
    }
}
