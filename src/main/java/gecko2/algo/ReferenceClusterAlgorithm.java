package gecko2.algo;

import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Parameter;

import java.util.*;

public class ReferenceClusterAlgorithm {
	private final GenomeList genomes;
	private final AlgorithmParameters param;
	private final Map<Integer, Integer> genomeGroupMapping;
	private final int nrOfGenomeGroups;
	private final boolean useGenomeGrouping;
	
	private static class MemRed{
		ArrayList<Integer> id;
		ArrayList<Integer> anzahl;
		
		MemRed(){
			this.anzahl = new ArrayList<Integer>();
			this.id = new ArrayList<Integer>();
		}
		
		public void anlegen(int x){
			this.id.add(x);
			this.anzahl.add(1);
		}
		
		private void hinzufuegen(int x){
			this.anzahl.set(this.id.indexOf(x), (this.anzahl.get(this.id.indexOf(x))+1));
		}
		
		public void suche(int x){
			if(this.id.contains(x)){
				hinzufuegen(x);
			} else {
				anlegen(x);
			}
		}
		
		public int[] umschreiben(){
			int[] y = new int[this.id.size()];
			for(int i=0;i<this.id.size();i++){
				y[i]=this.id.get(i);
			}
			
			return y;
		}
	}
	
	private static int[][][] memReducer(int[][][] genomes, Parameter p){
		MemRed help = new MemRed();
		
		for(int l = 0; l<genomes.length;l++){
			for(int m = 0; m<genomes[l].length; m++){
				for(int x = 0; x<genomes[l][m].length;x++){
					help.suche(genomes[l][m][x]);
				}
			}
		}
		
		
		
		for(int l = 0; l<genomes.length;l++){
			for(int m = 0; m<genomes[l].length; m++){
				MemRed help2 = new MemRed();
				for(int x = 0; x<genomes[l][m].length;x++){
					if(help.anzahl.get(help.id.indexOf(genomes[l][m][x]))>1){
						help2.anlegen(genomes[l][m][x]);
					} else if(help2.id.isEmpty() != true) {
						if(help2.id.get(help2.id.size()-1) < 0){
							help2.id.set(help2.id.size()-1, help2.id.get(help2.id.size()-1)-1);
						} else {
							help2.anlegen(-1);
						}
					} else {
						help2.anlegen(-1);
					}
				}
			genomes[l][m] = help2.umschreiben();
			}
		}
		
		return genomes;
	}

	private static int[][][] memSort(int[][][] genomes){
		int[][][] h;
		for(int l=0;l<genomes.length;l++){
			for(int m=0;m<genomes[l].length;m++){
				MemRed x = new MemRed();
				for(int n=0;n<genomes[l][m].length;n++){
					if(genomes[l][m][n]<0){
						
					} else {
						x.anlegen(genomes[l][m][n]);
					}
				}
				genomes[l][m] = x.umschreiben();
			}
		}
		
		return genomes;
	}
	
	/**
	 * Computes reference gene clusters for the given list of genomes and the given parameters
	 * @param genomes the genomes
	 * @param param the parameters
	 * @return the gene clusters
	 */
	public static GeneCluster[] computeReferenceClusters(int[][][] genomes, Parameter param) {
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
	public static GeneCluster[] computeReferenceClusters(int[][][] genomes, Parameter param, List<Set<Integer>> genomeGrouping) {
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
		
		GeneCluster[] result = new GeneCluster[refCluster.size()];
		for (int i=0; i<refCluster.size(); i++)
			result[i] = new GeneCluster(i, refCluster.get(i));
		return result;
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
		
		long startTime = System.nanoTime();
		
        if (param.getNrOfGenomes() != genomes.size())
            throw new RuntimeException("Number of genomes in param does not equal number of genomes!");
		
		genomes.initializeForCalculation(param.getMaximumDelta());
		List<ReferenceCluster> refClusterList = new ArrayList<ReferenceCluster>();
		
		int refGenomeCount = 1;
		if (!param.useSingleReference())
			refGenomeCount = genomes.size();
		for (int i=0; i<refGenomeCount; i++)
			detectReferenceGeneClusterFromSingleGenome(i, refClusterList);
		
		long calcTime = System.nanoTime();		
		System.out.println("Doing Statistics!");
		
		for (ReferenceCluster cluster : refClusterList)
			cluster.setGeneContent(genomes);
		
		Statistics.computeReferenceStatistics(genomes, refClusterList, param.getMaximumDelta(), param.useSingleReference(), nrOfGenomeGroups, genomeGroupMapping);
		
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
		for (int l = 1; l <= referenceChromosome.size(); l++){
			genomes.updateLeftBorder(l, referenceChromosome, referenceGenomeNr, param);
			Pattern pattern = new Pattern(genomes.getAlphabetSize(), genomes.size(), param, referenceGenomeNr, referenceChromosome, l);
			
			// Gene does not occure in any other Genome and does not occure in chr[i,...]
			if (referenceChromosome.getNextOCC(l) > referenceChromosome.size() && genomes.zeroOccs(referenceGenomeNr, referenceChromosome.getNr(), l, param.searchRefInRef()))
				continue;
			
			int r = l;
			int[] noOccCount = new int[genomes.size()];
			List<ListOfDeltaLocations> oldLists = new ArrayList<ListOfDeltaLocations>(genomes.size());
			for (int i=0; i<genomes.size(); i++)
				oldLists.add(new ListOfDeltaLocations());
			
			while(pattern.updateToNextI_ref(r)) {
				r = pattern.getRightBorder();
				
				for (ListOfDeltaLocations dLocList : oldLists)
					dLocList.removeNonInheritableElements(genomes, pattern.getLastChar(), param.getMaximumDelta());
				
				for (int i=0; i<genomes.size(); i++){
					if (param.searchRefInRef() && i==genomes.size()-1){
						if (genomes.get(i).noOccOutsideInterval(pattern.getLastChar(), l, r, referenceChromosome.getNr()))
							noOccCount[i]++;
					} else {
						if (genomes.get(i).noOcc(pattern.getLastChar()))
							noOccCount[i]++;
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
						ListOfDeltaLocations newList = pattern.computeNewOptimalDeltaLocations(genomes.get(k), pattern.getLastChar(), pattern.getSize(), param);
						
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
					
					List<ListOfDeltaLocations> listCopy = new ArrayList<ListOfDeltaLocations>(genomes.size());
					
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
