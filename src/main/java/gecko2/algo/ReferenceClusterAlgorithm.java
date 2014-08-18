package gecko2.algo;

import gecko2.algorithm.Parameter;

import java.util.*;

public class ReferenceClusterAlgorithm {
	private final GenomeList genomes;
	private final AlgorithmParameters param;
	private final Map<Integer, Integer> genomeGroupMapping;
	private final int nrOfGenomeGroups;
	private final boolean useGenomeGrouping;

	/**
	 * Computes reference gene clusters for the given list of genomes and the given parameters
	 * @param genomes the genomes
	 * @param param the parameters
	 * @return the gene clusters
	 */
	public static List<ReferenceCluster> computeReferenceClusters(int[][][] genomes, Parameter param) {
		return computeReferenceClusters(genomes, param, null);
	}

    private static int[] getSizes(int[][][] genomes) {
        int[] result = new int[genomes.length];
        for(int l = 0; l<genomes.length;l++){
            for(int m = 0; m<genomes[l].length; m++){
                result[l] += genomes[l][m].length - 2;
            }
        }
        return result;
    }

    private static void printGenomes(int[][][] genomes) {
        for(int l = 0; l<genomes.length;l++){
            for(int m = 0; m<genomes[l].length; m++){
                for(int x = 0; x<genomes[l][m].length;x++){
                    System.out.print(genomes[l][m][x] + " ");
                }
                System.out.println("");
            }
        }
    }



	/**
	 * Computes reference gene clusters for the given list of genomes and the given parameters
	 * @param genomes the genomes
	 * @param param the parameters
	 * @param genomeGrouping each set contains the index of all genomes that contribute to quorum and p-value only once
	 * @return the gene clusters
	 */
	public static List<ReferenceCluster> computeReferenceClusters(int[][][] genomes, Parameter param, List<Set<Integer>> genomeGrouping) {
		if (!param.useJavaAlgorithm())
			throw new IllegalArgumentException("invalid parameters");

        GenomeList data;
		if (param.getAlphabetSize() >= 0)
			data = new GenomeList(genomes, param.getAlphabetSize());
		else {
			data = new GenomeList(genomes);
			param.setAlphabetSize(data.getAlphabetSize());
		}
		//AlgorithmParameters algoParameters = AlgorithmParameters.getLowConservedParameters(param, param.getAlphabetSize(), data.size());
		//AlgorithmParameters algoParameters = AlgorithmParameters.getHighlyConservedParameters(param, param.getAlphabetSize(), data.size());
		//AlgorithmParameters algoParameters = AlgorithmParameters.getLichtheimiaParameters(param, param.getAlphabetSize(), data.size());
		//AlgorithmParameters algoParameters = AlgorithmParameters.getStatisticPaperGenomeParameters(param, param.getAlphabetSize(), data.size());
		//AlgorithmParameters algoParameters = AlgorithmParameters.getFiveProteobacterDeltaTableTestParameters(param.getAlphabetSize(), data.size());
		
		AlgorithmParameters algoParameters = new AlgorithmParameters(param, param.getAlphabetSize(), data.size());
		
		if (!checkParameters(algoParameters)) 
			throw new IllegalArgumentException("invalid parameters");
		
		ReferenceClusterAlgorithm refClusterAlgorithm = new ReferenceClusterAlgorithm(data, algoParameters, genomeGrouping);
		
		List<ReferenceCluster> refCluster = refClusterAlgorithm.computeRefClusters();

        return refCluster;
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
	}
	
	private List<ReferenceCluster> computeRefClusters(){
		System.out.println("Computing Gene Clusters!");
		
		Runtime rt = Runtime.getRuntime();
        long startTime = System.nanoTime();
		long startMemory = rt.totalMemory()-rt.freeMemory();
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
        long endOfCalcMemory = rt.totalMemory()-rt.freeMemory();
		System.out.println("Doing Statistics!");
		
		for (ReferenceCluster cluster : refClusterList)
			cluster.setGeneContent(genomes);

        //long statisticsStartTime = System.nanoTime();
		//Statistics.computeReferenceStatistics(genomes, refClusterList, param.getMaximumDelta(), param.useSingleReference(), nrOfGenomeGroups, genomeGroupMapping);
		
		//long statTime = System.nanoTime();
		System.out.println(String.format("Calculation: %fs",(calcTime - startTime)/1.0E09));
		//System.out.println(String.format("Statistics: %fs",(statTime - calcTime)/1.0E09));
        System.out.println(String.format("Memory Difference: %dkB", (startMemory-endOfCalcMemory)/(8*1024)));

        //TODO if (reduce) for (ReferenceCluster cluster : refClusterList) {cluster.fixPosition()}

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
		for (int l = 1; l <= referenceChromosome.size(); l++){
			genomes.updateLeftBorder(l, referenceChromosome, referenceGenomeNr, param);
			Pattern pattern = new Pattern(genomes.getAlphabetSize(), genomes.size(), param, referenceGenomeNr, referenceChromosome, l);
			int refMis = 0;
			
			// Gene does not occure in any other Genome and does not occure in chr[i,...]
			if (referenceChromosome.getGene(l) < 0 || (referenceChromosome.getNextOCC(l) > referenceChromosome.size() && genomes.zeroOccs(referenceGenomeNr, referenceChromosome.getNr(), l, param.searchRefInRef())))
				continue;
			
			int r = l;
			int[] noOccCount = new int[genomes.size()];
			List<ListOfDeltaLocations> oldLists = new ArrayList<>(genomes.size());
			for (int i=0; i<genomes.size(); i++)
				oldLists.add(new ListOfDeltaLocations());
			
			while(pattern.updateToNextI_ref(r)) {
				r = pattern.getRightBorder();
				if(referenceChromosome.getGene(r)<0)
					refMis+= -referenceChromosome.getGene(r);
				for (ListOfDeltaLocations dLocList : oldLists) //möglicher fehler
					dLocList.removeNonInheritableElements(genomes, pattern.getLastChar(), param.getMaximumDelta());
				
				for (int i=0; i<genomes.size(); i++){
					if (param.searchRefInRef() && i==genomes.size()-1){
						if (genomes.get(i).noOccOutsideInterval(pattern.getLastChar(), l, r, referenceChromosome.getNr()))
							noOccCount[i]++; // TODO Fix for < 0
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
                        ListOfDeltaLocations newList = pattern.computeNewOptimalDeltaLocations(genomes.get(k), pattern.getLastChar(), pattern.getSize(), param, refMis);
						
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
	
}
