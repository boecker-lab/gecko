package gecko2.io;

import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.Genome;
import gecko2.algorithm.Subsequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClusterAnnotationReader {
	private File f;
	private Genome[] genomes;
	private List<GeneCluster> currentClusters;
	List<GeneCluster> newClusters;
	Map<String, Integer> genomeMap;
	Map<String, Integer> chromosomeMap;
	Map<Integer, Integer> genomeIndexMap;
	int clusterId;
	
	private static Pattern clusterStartPattern = Pattern.compile("new cluster: pValue = ([-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?), refSeq = (\\d+)");
	private static Pattern clusterOccurrencePattern = Pattern.compile("(\\d+)(?:\\.\\d+)?:\t([^\t]+)\t\\[(\\d+),(\\d+)\\].*");
	
	
	public static List<GeneCluster> readClusterAnnotations(File f, Genome[] genomes) {
		ClusterAnnotationReader reader = new ClusterAnnotationReader(f, genomes);
		if (reader.readClusterAnnotations())
			return reader.currentClusters;
		else return null;
	}
	
	private ClusterAnnotationReader(File f, Genome[] genomes) {
		this.f = f;
		this.genomes = genomes;
		this.currentClusters = new ArrayList<GeneCluster>();
		
		newClusters = new ArrayList<GeneCluster>();
		
		genomeMap = new HashMap<String,Integer>();
		chromosomeMap = new HashMap<String,Integer>();
		genomeIndexMap = new HashMap<Integer,Integer>();
		
		for (int i=0; i<genomes.length; i++) {
			for (int j=0; j<genomes[i].getChromosomes().size(); j++){
				String name = genomes[i].getFullChromosomeName(j);
				genomeMap.put(name, i);
				chromosomeMap.put(name, j);
			}
		}
		clusterId = 0;
	}
		
	private boolean readClusterAnnotations() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			try {
				boolean inCluster = false;
				GeneCluster newCluster = null;
				Map<Genome, List<Subsequence> > subseqs = new HashMap<Genome, List<Subsequence>>();
				for (String line; (line = reader.readLine()) != null; ) {
					if (!inCluster) {
						newCluster = searchNewClusterStart(line);
						if (newCluster != null)
							inCluster = true;
					} else {
						if (line.equals("in chromosomes:"))
							continue;
						if (!parseNextOccurrence(line, subseqs)) {
							inCluster = false;
							newCluster = completeCluster(newCluster, subseqs);
							if (newCluster != null)
								newClusters.add(newCluster);
							else {
								System.err.println(String.format("Could not complete cluster before line: %s", line));
								return false;
							}
							subseqs.clear();
						}
					}
				}
				if (inCluster) {
					newCluster = completeCluster(newCluster, subseqs);
					if (newCluster != null)
						newClusters.add(newCluster);
					else {
						System.err.println("Could not complete last cluster");
						return false;
					}
					subseqs.clear();
				}
			} finally {
				reader.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		currentClusters.addAll(newClusters);
		return true;
	}
	
	private GeneCluster searchNewClusterStart(String line) {
		Matcher m = clusterStartPattern.matcher(line);
		if (m.matches()) {
			BigDecimal pValue = new BigDecimal(m.group(1));
			int refSeq = Integer.parseInt(m.group(2));
			return new GeneCluster(clusterId++, null, null, null, pValue, pValue, -1, refSeq-1, 'r');
		} else
			return null;
	}
	
	private boolean parseNextOccurrence(String line, Map<Genome, List<Subsequence> > subseqs) {
		Matcher m = clusterOccurrencePattern.matcher(line);
		if (m.matches()) {
			String genomeName = m.group(2);
			int occStart = Integer.parseInt(m.group(3));
			int occEnd = Integer.parseInt(m.group(4));
			Integer chromNr = chromosomeMap.get(genomeName);
			Integer internalIndex = genomeMap.get(genomeName);
			if (internalIndex == null || (internalIndex >= genomes.length)) {
				System.err.println(String.format("No valide index for genome %s found", genomeName));
				return false;
			}
			Genome genome = genomes[internalIndex];
			if (chromNr == null || genome == null) {
				System.err.println(String.format("No matching chromosome found for %s", genomeName));
				return false;
			}
			int seqIndex = Integer.parseInt(m.group(1)) - 1;
			Integer oldInternalIndex = genomeIndexMap.get(seqIndex);
			if (oldInternalIndex == null)
				genomeIndexMap.put(seqIndex, genomeMap.get(genomeName));
			else if (oldInternalIndex != genomeMap.get(genomeName)) {
				System.err.println(String.format("Missmatch in mapping for internal genome indices at %s with external index %d", genomeName, seqIndex));
				System.err.println(String.format("Expected %d, got %d!", oldInternalIndex, genomeMap.get(genomeName)));
				return false;
			}
			
			Subsequence subseq =  new Subsequence(occStart, occEnd, chromNr, 0, BigDecimal.ZERO);
			if (subseqs.containsKey(genome))
				subseqs.get(genome).add(subseq);
			else {
				List<Subsequence> newList = new ArrayList<Subsequence>();
				newList.add(subseq);
				subseqs.put(genome, newList);
			}
			return true;
		}
		return false;
	}
	
	private GeneCluster completeCluster(GeneCluster cluster, Map<Genome, List<Subsequence>> subseqs) {
		if (cluster.getRefSeqIndex() >= genomes.length) {
			System.err.println("RefSeqNr not in genomes!");
			return null;
		}
		Integer referenceIndex = genomeIndexMap.get(cluster.getRefSeqIndex());
		if (referenceIndex == null) {
			System.err.println("No index for reference sequence found!");
			return null;
		}
		Genome refGenome = genomes[referenceIndex];
		List<Subsequence> refSeqs = subseqs.get(refGenome);
		if (refSeqs == null) {
			System.err.println("No reference sequence found!");
			return null;
		}
		
		Subsequence refSeq = refSeqs.get(0);
		Set<Integer> refGeneSet = new HashSet<Integer>();
		for (int i=refSeq.getStart()-1; i<refSeq.getStop(); i++) {
			refGeneSet.add(Math.abs(refGenome.getChromosomes().get(refSeq.getChromosome()).getGenes().get(i).getId()));
		}
		
		Subsequence[][] allSeqs = new Subsequence[genomes.length][];
		Subsequence[][] bestSeqs = new Subsequence[genomes.length][];
		int support = 0;
		int totalDistance = 0;
		for (int i=0; i<genomes.length; i++){
			List<Subsequence> seqs = subseqs.get(genomes[i]);
			if (seqs == null) {
				allSeqs[i] = new Subsequence[]{};
				bestSeqs[i] = new Subsequence[]{};
			} else {
				allSeqs[i] = seqs.toArray(new Subsequence[seqs.size()]);
				support++;
				List<Subsequence> bestOccList = new ArrayList<Subsequence>(allSeqs[i].length);
				int minDistance = 1000000;
				for (Subsequence seq : allSeqs[i]) {
					Set<Integer> additionalGenes = new HashSet<Integer>();
					Set<Integer> foundGenes = new HashSet<Integer>();
					for (int j=seq.getStart()-1; j<seq.getStop(); j++){
						int geneId = Math.abs(genomes[i].getChromosomes().get(seq.getChromosome()).getGenes().get(j).getId());
						if (refGeneSet.contains(geneId))
							foundGenes.add(geneId);
						else
							additionalGenes.add(geneId);
					}
					int distance = additionalGenes.size() + refGeneSet.size() - foundGenes.size();
					seq.setDist(distance);
					if (distance < minDistance) {
						bestOccList.clear();
						minDistance = distance;
					}
					if (distance == minDistance)
						bestOccList.add(seq);
				}
				bestSeqs[i] = bestOccList.toArray(new Subsequence[bestOccList.size()]);
				totalDistance += minDistance;
			}
		}
		GeneClusterOccurrence[] allOccs = new GeneClusterOccurrence[]{new GeneClusterOccurrence(0, allSeqs, cluster.getBestPValue(), totalDistance, support)};
		GeneClusterOccurrence[] bestOccs = new GeneClusterOccurrence[]{new GeneClusterOccurrence(0, bestSeqs, cluster.getBestPValue(), totalDistance, support)};
		
		int[] genes = new int[refGeneSet.size()];
		int i=0;
		for (Integer gene : refGeneSet) {
			genes[i] = gene;
			i++;
		}
		
		GeneCluster newCluster = new GeneCluster(cluster.getId(), bestOccs, allOccs, genes, cluster.getBestPValue(), cluster.getBestPValueCorrected(), totalDistance, referenceIndex, cluster.getType());	
		return newCluster;
	}
}
