package de.unijena.bioinf.gecko3.algo;

import de.unijena.bioinf.gecko3.algo.status.AlgorithmProgressListener;
import de.unijena.bioinf.gecko3.algo.status.AlgorithmProgressProvider;
import de.unijena.bioinf.gecko3.algo.status.AlgorithmStatusEvent;
import de.unijena.bioinf.gecko3.datastructures.DataSet;
import de.unijena.bioinf.gecko3.datastructures.GeneCluster;
import de.unijena.bioinf.gecko3.datastructures.Parameter;

import java.math.BigDecimal;
import java.util.*;

public class ReferenceClusterAlgorithm implements AlgorithmProgressProvider {
	private final GenomeList genomes;
	private final AlgorithmParameters param;
	private final Map<Integer, Integer> genomeGroupMapping;
	private final int nrOfGenomeGroups;
	private final boolean useGenomeGrouping;

    private final List<AlgorithmProgressListener> progressListeners;
    private int maxProgressValue;
    private int progressValue;

    /**
     * Computes reference gene clusters for the given dataset and the given parameters
     * @param data the genomes
     * @param params the parameters
     * @param useMemoryReduction if memory reduction should be used
     * @param genomeGrouping each set contains the index of all genomes that contribute to quorum and p-value only once
     * @param listener the progress listener
     * @return the gene clusters
     */
    public static List<GeneCluster> computeReferenceClusters(DataSet data, Parameter params, boolean useMemoryReduction, List<Set<Integer>> genomeGrouping, AlgorithmProgressListener listener) {
        int[][][] intArray;
        if (!useMemoryReduction) {
            intArray = data.toIntArray();
            params.setAlphabetSize(data.getCompleteAlphabetSize());
        } else {
            intArray = data.toReducedIntArray();
            params.setAlphabetSize(data.getReducedAlphabetSize());
        }


        List<ReferenceCluster> refCluster = computeReferenceClusters(intArray, params, genomeGrouping, listener);

        List<GeneCluster> result = new ArrayList<>(refCluster.size());

        if (useMemoryReduction){
            int[][][] runLengthMergedLookup = DataSet.createRunLengthMergedLookup(intArray);
            for (ReferenceCluster cluster : refCluster)
                cluster.correctMergedPositions(runLengthMergedLookup, intArray);
        }
        for (int i = 0; i < refCluster.size(); i++)
            result.add(new GeneCluster(i, refCluster.get(i), data));

        return result;
    }
	
	/**
	 * Computes reference gene clusters for the given list of genomes and the given parameters
	 * @param genomes the genomes
	 * @param param the parameters
	 * @return the gene clusters
	 */
	static List<ReferenceCluster> computeReferenceClusters(int[][][] genomes, Parameter param) {
		return computeReferenceClusters(genomes, param, null, null);
	}

    /**
     * Computes reference gene clusters for the given list of genomes and the given parameters
     * @param genomes the genomes
     * @param param the parameters
     * @param genomeGrouping each set contains the index of all genomes that contribute to quorum and p-value only once
     * @return the gene clusters
     */
    static List<ReferenceCluster> computeReferenceClusters(int[][][] genomes, Parameter param, List<Set<Integer>> genomeGrouping) {
        return computeReferenceClusters(genomes, param, genomeGrouping, null);
    }



	/**
	 * Computes reference gene clusters for the given list of genomes and the given parameters
	 * @param genomes the genomes
	 * @param param the parameters
	 * @param genomeGrouping each set contains the index of all genomes that contribute to quorum and p-value only once
     * @param listener the progress listener
	 * @return the gene clusters
	 */
	private static List<ReferenceCluster> computeReferenceClusters(int[][][] genomes, Parameter param, List<Set<Integer>> genomeGrouping, AlgorithmProgressListener listener) {
        if (!param.useJavaAlgorithm())
			throw new IllegalArgumentException("invalid parameters");

        GenomeList data;
		if (param.getAlphabetSize() >= 0)
			data = new GenomeList(genomes, param.getAlphabetSize());
		else {
			data = new GenomeList(genomes);
			param.setAlphabetSize(data.getAlphabetSize());
		}

		AlgorithmParameters algoParameters = new AlgorithmParameters(param, param.getAlphabetSize(), data.size());
		
		if (!checkParameters(algoParameters)) 
			throw new IllegalArgumentException("invalid parameters");
		
		ReferenceClusterAlgorithm refClusterAlgorithm = new ReferenceClusterAlgorithm(data, algoParameters, genomeGrouping);
        refClusterAlgorithm.addListener(listener);
		
		return refClusterAlgorithm.computeRefClusters();
	}
	
    private static boolean checkParameters(AlgorithmParameters param) {
        return true;
    }
	
	private ReferenceClusterAlgorithm(GenomeList genomes, AlgorithmParameters param, List<Set<Integer>> genomeGrouping) {
		this.genomes = genomes;
		this.param = param;
		if (genomeGrouping == null) {
			genomeGroupMapping = null;
			nrOfGenomeGroups = genomes.size();
			useGenomeGrouping = false;
		} else {
			nrOfGenomeGroups = genomeGrouping.size();
			useGenomeGrouping = nrOfGenomeGroups != genomes.size();
			genomeGroupMapping = new HashMap<>();
			for (int groupId=0; groupId<genomeGrouping.size(); groupId++){
				for (Integer genomeNr: genomeGrouping.get(groupId))
					genomeGroupMapping.put(genomeNr, groupId);
			}
		}

        progressListeners = new ArrayList<>();
        for (int i=0; i < ((param.useSingleReference()) ? 1 : genomes.size()); i++) {
            for (Chromosome chr : genomes.get(i))
                maxProgressValue += chr.getEffectiveGeneNumber();
        }
        progressValue = 0;
	}
	
	private List<ReferenceCluster> computeRefClusters(){
		System.out.println("Computing Gene Clusters!");
		
		long startTime = System.nanoTime();
		
        if (param.getNrOfGenomes() != genomes.size())
            throw new RuntimeException("Number of genomes in param does not equal number of genomes!");
		
		genomes.initializeForCalculation(param.getMaximumDelta());
		List<ReferenceCluster> refClusterList = new ArrayList<>();
		
		int refGenomeCount = 1;
		if (!param.useSingleReference())
			refGenomeCount = genomes.size();
        
		for (int i=0; i<refGenomeCount; i++)
			detectReferenceGeneClusterFromSingleGenome(i, refClusterList);
		
		long calcTime = System.nanoTime();		
		System.out.println("Doing Statistics!");
		
		for (ReferenceCluster cluster : refClusterList) {
            cluster.setGeneContent(genomes);
            cluster.setBestCombined_pValue(BigDecimal.ZERO);
            cluster.setBestCombined_pValueCorrected(BigDecimal.ZERO);
        }

        genomes.removeCalculationFields();
		
		Statistics.computeReferenceStatistics(genomes, refClusterList, param.getMaximumDelta(), param.useSingleReference(), nrOfGenomeGroups, genomeGroupMapping, progressListeners);
		
		long statTime = System.nanoTime();
		System.out.println(String.format("Calculation: %fs",(calcTime - startTime)/1.0E09));
		System.out.println(String.format("Statistics: %fs",(statTime - calcTime)/1.0E09));
		
		return refClusterList;
	}
	
	private void detectReferenceGeneClusterFromSingleGenome(int referenceGenomeNr, List<ReferenceCluster> refClusterList){
		if (param.searchRefInRef())
			genomes.appendCopyOfReferenceGenome(referenceGenomeNr, param);
		
		for (Chromosome referenceChromosome : genomes.get(referenceGenomeNr)){
			detectReferenceGeneClusterFromSingleChromosome(referenceGenomeNr, referenceChromosome, refClusterList);
		}
		
		if (param.searchRefInRef()) {
			Iterator<ReferenceCluster> refIt = refClusterList.iterator();
			while (refIt.hasNext()) {
				if (!refIt.next().mergeAdditionalReferenceHits(genomes.size()))
					refIt.remove();
			}
			genomes.removeCopyOfReferenceGenome(param);
		}
	}
	
	private boolean useGenomeGrouping() {
		return useGenomeGrouping;
	}
	
	private void detectReferenceGeneClusterFromSingleChromosome(int referenceGenomeNr, Chromosome referenceChromosome, List<ReferenceCluster> refClusterList){
		for (int l = 1; l <= referenceChromosome.getEffectiveGeneNumber(); l++){
            fireProgressUpdateEvent(new AlgorithmStatusEvent(progressValue++, AlgorithmStatusEvent.Task.ComputingClusters));
			genomes.updateLeftBorder(l, referenceChromosome, referenceGenomeNr);
			Pattern pattern = new Pattern(genomes.getAlphabetSize(), genomes.size(), param, referenceGenomeNr, referenceChromosome, l);
			
			// Gene does not occur in any other Genome and does not occur in chr[i,...]
			if (referenceChromosome.getGene(l) < 0 || (referenceChromosome.getNextOCC(l) > referenceChromosome.getEffectiveGeneNumber() && genomes.zeroOccs(referenceGenomeNr, referenceChromosome.getNr(), l, param.searchRefInRef())))
				continue;
			
			int r = l;
			int[] noOccCount = new int[genomes.size()];
			List<ListOfDeltaLocations> oldLists = new ArrayList<>(genomes.size());
			for (int i=0; i<genomes.size(); i++)
				oldLists.add(new ListOfDeltaLocations());
			
			while(pattern.updateToNextI_ref(r)) {
				r = pattern.getRightBorder();

                for (ListOfDeltaLocations dLocList : oldLists)
					dLocList.removeNonInheritableElements(genomes, pattern.getLastChar(), param.getMaximumDelta());
				
				for (int i=0; i<genomes.size(); i++){
					if (param.searchRefInRef() && i==genomes.size()-1){
                        noOccCount[i] += genomes.get(i).noOccOutsideInterval(pattern.getLastChar(), l, r, referenceChromosome.getNr());
					} else {
                        noOccCount[i] += genomes.get(i).noOcc(pattern.getLastChar());
					}
				}
				
				if (countNonOccs(noOccCount, param.getMaximumDelta()) > param.getMaxUncoveredGenomes())
					break;
				
				oldLists.get(referenceGenomeNr).emptyList();
				DeltaLocation refdLoc = DeltaLocation.getReferenceLocation(referenceGenomeNr, referenceChromosome.getNr(), l, r, pattern.getSize());
				oldLists.get(referenceGenomeNr).insertDeltaLocation(refdLoc);
				
				int minHitCoveredCount = 0;
				boolean[] containedGenomeClusters = new boolean[nrOfGenomeGroups];
				
				for (int k=0; k<genomes.size(); k++){
					if(k != referenceGenomeNr){
                        ListOfDeltaLocations newList = pattern.computeNewOptimalDeltaLocations(genomes.get(k), param);
						
						if (param.searchRefInRef() && k == genomes.size()-1){
							newList.removeRefDLocReferenceHit(pattern, referenceChromosome.getNr());
						}
						
						oldLists.get(k).mergeLists(newList);
						oldLists.get(k).checkForValidDeltaTableLocations(param, pattern.getSize());
					}
					if (oldLists.get(k).minHitsCovered()) {
						if (!useGenomeGrouping())
							minHitCoveredCount++;
						else if (!containedGenomeClusters[genomeGroupMapping.get(k)]){
							minHitCoveredCount++;
							containedGenomeClusters[genomeGroupMapping.get(k)] = true;
						}
					}
				}
				
				if (pattern.getSize() >= param.getMinClusterSize() &&
						minHitCoveredCount >= param.getMinCoveredGenomes() &&
						refdLoc.isFirstRefOcc(oldLists) &&
						occursInValid_dLocs(refdLoc.leftMostEssentialChar(referenceChromosome), oldLists, referenceGenomeNr) &&
						occursInValid_dLocs(refdLoc.rightMostEssentialChar(referenceChromosome), oldLists, referenceGenomeNr)) {
					
					List<ListOfDeltaLocations> listCopy = new ArrayList<>(genomes.size());
					
					for (int i=0; i<genomes.size(); i++)
						listCopy.add((oldLists.get(i).getOptimalCopy()));
					
					if (refdLoc.isFirstRefOcc(listCopy)){
						ReferenceCluster newCluster = new ReferenceCluster(pattern, listCopy, param.searchRefInRef(),  nrOfGenomeGroups, genomeGroupMapping);
						check_and_insert_refCluster(refClusterList, newCluster);
					}
				}
				r = pattern.getRightBorder()+1;
			}
		}
	}
	
	private boolean check_and_insert_refCluster(
			List<ReferenceCluster> refClusterList, ReferenceCluster newCluster) {
		Iterator<ReferenceCluster> otherClusterIt = refClusterList.iterator();
		while(otherClusterIt.hasNext()){
			ReferenceCluster otherCluster = otherClusterIt.next();
			if (newCluster.areAll_dLocsNested(otherCluster))
				return false;
			if (otherCluster.areAll_dLocsNested(newCluster)){
				otherClusterIt.remove();
			}
		}
		
		refClusterList.add(newCluster);
		return true;
	}

	private boolean occursInValid_dLocs(int c, List<ListOfDeltaLocations> lists, int referenceGenomeNr){
		for (int k=0; k<lists.size(); k++){
			if (k == referenceGenomeNr)
				continue;
			if (lists.get(k).valid_dLocContainsCharacter(c, genomes))
				return true;
		}
		return false;
	}

	private int countNonOccs(int[] noOccCount, int delta) {
		int nonOccs = 0;
        for (int aNoOccCount : noOccCount)
            if (aNoOccCount > delta)
                nonOccs++;
		return nonOccs;
	}

    @Override
    public void addListener(AlgorithmProgressListener listener) {
        if (listener != null) {
            progressListeners.add(listener);
            listener.algorithmProgressUpdate(new AlgorithmStatusEvent(maxProgressValue, AlgorithmStatusEvent.Task.Init));
            listener.algorithmProgressUpdate(new AlgorithmStatusEvent(progressValue, AlgorithmStatusEvent.Task.ComputingClusters));
        }
    }

    @Override
    public void removeListener(AlgorithmProgressListener listener) {
        if (listener != null)
            progressListeners.remove(listener);
    }

    private void fireProgressUpdateEvent(AlgorithmStatusEvent statusEvent){
        for (AlgorithmProgressListener listener : progressListeners)
            listener.algorithmProgressUpdate(statusEvent);
    }
}
