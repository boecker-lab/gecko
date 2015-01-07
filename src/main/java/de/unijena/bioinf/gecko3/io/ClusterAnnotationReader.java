/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.io;

import de.unijena.bioinf.gecko3.datastructures.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(ClusterAnnotationReader.class);

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
		try (BufferedReader reader = new BufferedReader(new FileReader(f))){
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
        } catch (IOException | ParseException | NumberFormatException e) {
			logger.warn("Unable to read clusters", e);
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
        final DataSet data;
        final Map<Integer, Integer> genomeIndexMap;
        final int id;
        final int refSeqIndexUnmapped;
        Integer refSeqIndex;
        final Parameter.OperationMode mode;
        final BigDecimal pValue;
        final BigDecimal pValueCorr;

        int minTotalDistance;
        Set<GeneFamily> genes;
        GeneClusterOccurrence bestOccs;
        GeneClusterOccurrence allOccs;

        public GeneClusterBuilder(int id, BigDecimal pValue, BigDecimal pValueCorr, int refSeqIndex, DataSet data, Map<Integer, Integer> genomeIndexMap) throws ParseException{
            this.id = id;
            this.mode = Parameter.OperationMode.reference;
            this.data = data;
            this.pValue = pValue;
            this.pValueCorr = pValueCorr;
            this.genomeIndexMap = genomeIndexMap;
            this.refSeqIndexUnmapped = refSeqIndex;
        }

        public void completeCluster(Map<Genome, List<Subsequence>> subseqs) throws ParseException {
            if (refSeqIndexUnmapped >= data.getGenomes().length) {
                throw new ParseException("RefSeqNr not in genomes!", 0);
            }
            this.refSeqIndex = genomeIndexMap.get(refSeqIndexUnmapped);
            if (this.refSeqIndex == null) {
                throw new ParseException("No index for reference sequence found!", 0);
            }

            Genome refGenome = data.getGenomes()[refSeqIndex];
            List<Subsequence> refSeqs = subseqs.get(refGenome);
            if (refSeqs == null) {
                throw new ParseException("No reference sequence found!", 0);
            }

            Subsequence refSeq = refSeqs.get(0);
            genes = new HashSet<>();
            for (int i = refSeq.getStart() - 1; i < refSeq.getStop(); i++) {
                genes.add(refGenome.getChromosomes().get(refSeq.getChromosome()).getGenes().get(i).getGeneFamily());
            }

            Subsequence[][] allSeqs = new Subsequence[data.getGenomes().length][];
            Subsequence[][] bestSeqs = new Subsequence[data.getGenomes().length][];
            int support = 0;
            minTotalDistance = 0;
            for (int i = 0; i < data.getGenomes().length; i++) {
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
                        for (int j = seq.getStart() - 1; j < seq.getStop(); j++) {
                            GeneFamily geneId = data.getGenomes()[i].getChromosomes().get(seq.getChromosome()).getGenes().get(j).getGeneFamily();
                            if (genes.contains(geneId))
                                foundGenes.add(geneId);
                            else
                                additionalGenes.add(geneId);
                        }
                        int distance = additionalGenes.size() + genes.size() - foundGenes.size();
                        seq.setDist(distance);
                        if (distance < minDistance) {
                            bestOccList.clear();
                            minDistance = distance;
                        }
                        if (distance == minDistance)
                            bestOccList.add(seq);
                    }
                    bestSeqs[i] = bestOccList.toArray(new Subsequence[bestOccList.size()]);
                    minTotalDistance += minDistance;
                }
            }
            allOccs = new GeneClusterOccurrence(0, allSeqs, pValue, minTotalDistance, support);
            bestOccs = new GeneClusterOccurrence(0, bestSeqs, pValue, minTotalDistance, support);
        }

        GeneCluster build() throws ParseException {
            if (genes == null)
                throw new ParseException("Missing genes when trying to complete cluster!", 0);
            if (allOccs == null || bestOccs == null)
                throw new ParseException("Missing occs when trying to complete cluster!", 0);
            return new GeneCluster(id,
                    bestOccs,
                    allOccs,
                    genes,
                    pValue,
                    pValueCorr,
                    minTotalDistance,
                    refSeqIndex,
                    mode);
        }
    }
}
