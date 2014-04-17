package gecko2.io;

import gecko2.algorithm.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClusterAnnotationReader {
	private final File f;
	private final DataSet data;
	private final List<GeneCluster> currentClusters;
	private final List<GeneCluster> newClusters;
	private final Map<String, Integer> genomeMap;
	private final Map<String, Integer> chromosomeMap;
	private final Map<Integer, Integer> genomeIndexMap;
	private int clusterId;
	
	private static final Pattern clusterStartPattern = Pattern.compile("new cluster: pValue = ([-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?), refSeq = (\\d+)");
	private static final Pattern clusterOccurrencePattern = Pattern.compile("(\\d+)(?:\\.\\d+)?:\t([^\t]+)\t\\[(\\d+),(\\d+)\\].*");
	
	
	public static List<GeneCluster> readClusterAnnotations(File f, DataSet data) {
		ClusterAnnotationReader reader = new ClusterAnnotationReader(f, data);
		if (reader.readClusterAnnotations())
			return reader.currentClusters;
		else return null;
	}
	
	private ClusterAnnotationReader(File f, DataSet data) {
		this.f = f;
		this.data = data;
		this.currentClusters = new ArrayList<>();
		
		newClusters = new ArrayList<>();
		
		genomeMap = new HashMap<>();
		chromosomeMap = new HashMap<>();
		genomeIndexMap = new HashMap<>();
		
		for (int i=0; i<data.getGenomes().length; i++) {
			for (int j=0; j<data.getGenomes()[i].getChromosomes().size(); j++){
				String name = data.getGenomes()[i].getFullChromosomeName(j);
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
				GeneClusterBuilder clusterBuilder = null;
				Map<Genome, List<Subsequence> > subseqs = new HashMap<>();
				for (String line; (line = reader.readLine()) != null; ) {
					if (!inCluster) {
						clusterBuilder = searchNewClusterStart(line);
						if (clusterBuilder != null)
							inCluster = true;
					} else {
						if (line.equals("in chromosomes:"))
							continue;
						if (!parseNextOccurrence(line, subseqs)) {
							inCluster = false;
							clusterBuilder.completeCluster(subseqs);
							if (clusterBuilder != null)
								newClusters.add(clusterBuilder.build());
							else {
								System.err.println(String.format("Could not complete cluster before line: %s", line));
								return false;
							}
							subseqs.clear();
						}
					}
				}
				if (inCluster) {
					clusterBuilder.completeCluster(subseqs);
					if (clusterBuilder != null)
						newClusters.add(clusterBuilder.build());
					else {
						System.err.println("Could not complete last cluster");
						return false;
					}
					subseqs.clear();
				}
			} finally {
				reader.close();
			}
			
		} catch (IOException | ParseException | NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		currentClusters.addAll(newClusters);
		return true;
	}
	
	private GeneClusterBuilder searchNewClusterStart(String line) throws IOException, ParseException {
		Matcher m = clusterStartPattern.matcher(line);
		if (m.matches()) {
			BigDecimal pValue = new BigDecimal(m.group(1));
			int refSeq = Integer.parseInt(m.group(2));
			return new GeneClusterBuilder(clusterId++, pValue, pValue, refSeq-1, data, genomeIndexMap);
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
			if (internalIndex == null || (internalIndex >= data.getGenomes().length)) {
				System.err.println(String.format("No valide index for genome %s found", genomeName));
				return false;
			}
			Genome genome = data.getGenomes()[internalIndex];
			if (chromNr == null || genome == null) {
				System.err.println(String.format("No matching chromosome found for %s", genomeName));
				return false;
			}
			int seqIndex = Integer.parseInt(m.group(1)) - 1;
			Integer oldInternalIndex = genomeIndexMap.get(seqIndex);
			if (oldInternalIndex == null)
				genomeIndexMap.put(seqIndex, genomeMap.get(genomeName));
			else if (!oldInternalIndex.equals(genomeMap.get(genomeName))) {
				System.err.println(String.format("Missmatch in mapping for internal genome indices at %s with external index %d", genomeName, seqIndex));
				System.err.println(String.format("Expected %d, got %d!", oldInternalIndex, genomeMap.get(genomeName)));
				return false;
			}
			
			Subsequence subseq =  new Subsequence(occStart, occEnd, chromNr, 0, BigDecimal.ZERO);
			if (subseqs.containsKey(genome))
				subseqs.get(genome).add(subseq);
			else {
				List<Subsequence> newList = new ArrayList<>();
				newList.add(subseq);
				subseqs.put(genome, newList);
			}
			return true;
		}
		return false;
	}

    private static class GeneClusterBuilder {
        DataSet data;


        final int id;
        final int refSeqIndex;
        final Parameter.OperationMode mode;
        int minTotalDistance;
        final BigDecimal pValue;
        final BigDecimal pValueCorr;
        Set<GeneFamily> genes;
        List<GeneClusterOccurrence> bestOccList;
        List<GeneClusterOccurrence> allOccList;

        public GeneClusterBuilder(int id, BigDecimal pValue, BigDecimal pValueCorr, int refSeqIndex, DataSet data, Map<Integer, Integer> genomeIndexMap) {
            this. id = id;
            this.mode = Parameter.OperationMode.reference;
            this.data = data;
            this.pValue = pValue;
            this.pValueCorr = pValueCorr;
            this.refSeqIndex = refSeqIndex;
        }

        void readGenes(BufferedReader reader) throws IOException, ParseException {
            String line = reader.readLine().trim();
            String[] genes = line.substring(1, line.length()-1).split(",");
            this.genes = new HashSet<>();
            for (String gene : genes) {
                GeneFamily geneFamily;
                if (gene.trim().equals(GeneFamily.UNKNOWN_GENE_ID)) {
                    geneFamily = data.getUnknownGeneFamily();
                } else {
                    geneFamily = geneFamilyMap.get(gene.trim());
                    if (geneFamily == null) {
                        throw new ParseException("No gene family found for key: " + gene.trim(), 0);
                    }
                }
                this.genes.add(geneFamily);
            }
        }

        public void completeCluster(Map<Genome, List<Subsequence>> subseqs) throws ParseException {
            if (refSeqIndex >= data.getGenomes().length) {
                throw new ParseException("RefSeqNr not in genomes!", 0);
            }
            Integer referenceIndex = genomeIndexMap.get(refSeqIndex);
            if (referenceIndex == null) {
                throw new ParseException("No index for reference sequence found!", 0);
            }
            Genome refGenome = data.getGenomes()[referenceIndex];
            List<Subsequence> refSeqs = subseqs.get(refGenome);
            if (refSeqs == null) {
                throw new ParseException("No reference sequence found!", 0);
            }

            Subsequence refSeq = refSeqs.get(0);
            Set<GeneFamily> refGeneSet = new HashSet<>();
            for (int i=refSeq.getStart()-1; i<refSeq.getStop(); i++) {
                refGeneSet.add(refGenome.getChromosomes().get(refSeq.getChromosome()).getGenes().get(i).getGeneFamily());
            }

            Subsequence[][] allSeqs = new Subsequence[data.getGenomes().length][];
            Subsequence[][] bestSeqs = new Subsequence[data.getGenomes().length][];
            int support = 0;
            int totalDistance = 0;
            for (int i=0; i<data.getGenomes().length; i++){
                List<Subsequence> seqs = subseqs.get(data.getGenomes()[i]);
                if (seqs == null) {
                    allSeqs[i] = new Subsequence[]{};
                    bestSeqs[i] = new Subsequence[]{};
                } else {
                    allSeqs[i] = seqs.toArray(new Subsequence[seqs.size()]);
                    support++;
                    List<Subsequence> bestOccList = new ArrayList<Subsequence>(allSeqs[i].length);
                    int minDistance = 1000000;
                    for (Subsequence seq : allSeqs[i]) {
                        Set<GeneFamily> additionalGenes = new HashSet<>();
                        Set<GeneFamily> foundGenes = new HashSet<>();
                        for (int j=seq.getStart()-1; j<seq.getStop(); j++){
                            GeneFamily geneId = data.getGenomes()[i].getChromosomes().get(seq.getChromosome()).getGenes().get(j).getGeneFamily();
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
            GeneClusterOccurrence[] allOccs = new GeneClusterOccurrence[]{new GeneClusterOccurrence(0, allSeqs, builder.getBestPValue(), totalDistance, support)};
            GeneClusterOccurrence[] bestOccs = new GeneClusterOccurrence[]{new GeneClusterOccurrence(0, bestSeqs, builder.getBestPValue(), totalDistance, support)};

            return new GeneCluster(builder.getId(), bestOccs, allOccs, refGeneSet, builder.getBestPValue(), builder.getBestPValueCorrected(), totalDistance, referenceIndex, builder.getType());
        }

        GeneCluster build() throws ParseException{
            if (genes == null)
                throw new ParseException("Missing genes when trying to complete cluster!", 0);
            if (allOccList == null || bestOccList == null)
                throw new ParseException("Missing occs when trying to complete cluster!", 0);
            return new GeneCluster(id,
                    bestOccList.toArray(new GeneClusterOccurrence[bestOccList.size()]),
                    allOccList.toArray(new GeneClusterOccurrence[allOccList.size()]),
                    genes,
                    pValue,
                    pValueCorr,
                    minTotalDistance,
                    refSeqIndex,
                    mode);
        }
}
