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

import com.lowagie.text.DocumentException;
import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.*;
import de.unijena.bioinf.gecko3.gui.GeneClusterPicture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResultWriter {
    private static final Logger logger = LoggerFactory.getLogger(ResultWriter.class);

    public static boolean exportResultsToFile(File file, ExportType type, GeckoInstance.ResultFilter filter) {
        GeckoInstance gecko = GeckoInstance.getInstance();
        return exportResultsToFile(file, gecko.getClusterList(filter), gecko.getData().getGenomeNames(), type);
    }

	public static boolean exportResultsToFile(File file, List<GeneCluster> clusters, List<String> genomeNames, ExportType type) {
        Collections.sort(clusters, new Comparator<GeneCluster>() {
            @Override
            public int compare(GeneCluster o1, GeneCluster o2) {
                return o1.getBestPValue().compareTo(o2.getBestPValue());
            }
        });
        boolean writtenSuccessfully = false;
        switch (type) {
            case clusterData:
                writtenSuccessfully = writeGeneClusterOutput(file, clusters);
                break;
            case clusterStatistics:
                writtenSuccessfully = writeGeneClusterStatistic(file, clusters, genomeNames);
                break;
            case table:
                writtenSuccessfully = writeGeneClusterTable(file, clusters);
                break;
            case geneNameTable:
                writtenSuccessfully = writeGeneClusterGeneNameTable(file, clusters, genomeNames, type.getAdditionalExportParameters());
                break;
            case clusterConservation:
                writtenSuccessfully = writeClusterConservationFile(file, clusters, genomeNames);
                break;
            case clusterGenomeInformation:
                writtenSuccessfully = writeClusterGenomeInformation(file, clusters, genomeNames);
                break;
            case referenceClusterTags:
                writtenSuccessfully = writeReferenceClusterTags(file, clusters);
                break;
            case latexTable:
                writtenSuccessfully = writeGeneClusterLatexTable(file, clusters);
                break;
            case internalDuplications:
                writtenSuccessfully = writeInternalDuplicationsData(file, clusters, genomeNames);
                break;
            case pdf:
                writtenSuccessfully = writeGeneClusterToPdf(file, clusters, type.getAdditionalExportParameters());
                break;
            case zippedPdfs:
                writtenSuccessfully = writeGeneClusterPdfToZip(file, clusters, type.getAdditionalExportParameters());
                break;
        }
        return writtenSuccessfully;
	}

    private static boolean writeGeneClusterToPdf(File file, List<GeneCluster> clusters, ExportType.AdditionalExportParameters additionalParameters) {
        List<GeneClusterPicture> pictures = new ArrayList<>(clusters.size());
        for (GeneCluster cluster : clusters)
            pictures.add(new GeneClusterPicture(cluster, additionalParameters.getNameType(), additionalParameters.clusterHeader(), true));

        try (GeneClusterToPDFWriter pdfWriter = new GeneClusterToPDFWriter(new BufferedOutputStream(new FileOutputStream(file)))) {
            pdfWriter.write(pictures);
        } catch (IOException | DocumentException e) {
            logger.warn("Write error", e);
            return false;
        }
        return true;
    }

    private static boolean writeGeneClusterPdfToZip(File file, List<GeneCluster> clusters, ExportType.AdditionalExportParameters additionalParameters) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
             GeneClusterToPDFWriter pdfWriter = new GeneClusterToPDFWriter(zipOutputStream)) {
            for (int i=0; i<clusters.size(); i++) {
                zipOutputStream.putNextEntry(new ZipEntry("cluster" + clusters.get(i).getId() + ".pdf"));
                GeneClusterPicture picture = new GeneClusterPicture(clusters.get(i), additionalParameters.getNameType(), additionalParameters.clusterHeader(), true);
                pdfWriter.write(picture);
                zipOutputStream.closeEntry();
            }
        } catch (IOException | DocumentException e) {
            logger.warn("Write error", e);
            return false;
        }
        return true;
    }

    private static boolean writeGeneClusterTable(File f, List<GeneCluster> clusters) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))){
            writer.write("%%ID\t#GeneFamilies\t#Genomes\tMin PWDistance\tMax PWDistance\tAvg PWDistance\nScore\nFDR Score\nPValue\nFDR PValue\nReference Gene Names");
            writer.newLine();
            for (int i=0; i<clusters.size(); i++) {
                GeneCluster cluster = clusters.get(i);
                writer.write(String.format("%d\t%d\t%d\t%d\t%d\t%.1f\t%.2f\t%.2f\t%.4g\t%.4g\t%s", cluster.getId(), cluster.getGeneFamilies().size(), cluster.getSize(), cluster.getMinPWDist(), cluster.getMaxPWDist(), cluster.getAvgPWDist(), cluster.getBestScore(), cluster.getBestCorrectedScore(), cluster.getBestPValue(), cluster.getBestPValueCorrected(), cluster.getReferenceGeneNames()));
                writer.newLine();
            }
            //writer.write("No of genes & No of genomes & min. $\\delta$ & max. $\\delta$ & avg. $\\delta$ & pValue & corrected pValue & \\\\\n");
        } catch (IOException e) {
            logger.warn("Write error", e);
            return false;
        }
        return true;
	}

    private static boolean writeClusterConservationFile(File f, List<GeneCluster> clusters, List<String> genomeNames) {
        boolean useAllOccurrences = true;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))){
            writer.write("%%For each cluster occurrence:");
            writer.newLine();
            writer.write("%%Genome\t#missing genes\t#additional genes\tgene order conservation\tgene conservation");
            writer.newLine();
            writer.write("%%Gene order conservation is either \"+\" for singleton genes or genes not in the reference occurrence, or the gene family id.");
            writer.newLine();
            writer.write("%%Gene conservation gives the gene family order in the last line above the occurrences. For each occurrence, for each gene family, 1 means conserved, 0 means not conserved. Singleton gene families are excluded.");
            writer.newLine();

            for (GeneCluster cluster : clusters){
                writer.write(String.format("Cluster ID: %d\t p-Value: %.2f", cluster.getId(), cluster.getBestCorrectedScore()));
                writer.newLine();

                List<GeneFamily> sortedGeneFamilies = new ArrayList<>(cluster.getGeneFamilies());
                Collections.sort(sortedGeneFamilies);
                StringBuilder builder = new StringBuilder();
                for (GeneFamily family : sortedGeneFamilies)
                    builder.append(family.getExternalId()).append(" ");
                writer.write(String.format("\t\t\tGene order for gene conservation:\t%s", builder.toString()));
                writer.newLine();

                GeneClusterOccurrence occ = cluster.getOccurrences(useAllOccurrences);
                for (int i=0; i<occ.getSubsequences().length; i++) {
                    if (occ.getSubsequences()[i].length == 0)
                        continue;
                    List<String> geneConservation = cluster.getGeneConservation(i, useAllOccurrences);
                    List<String> geneContained = cluster.getGeneContained(i, useAllOccurrences);
                    for (int j=0; j<occ.getSubsequences()[i].length; j++){
                        writer.write(String.format("%s\t%s\t%s" , genomeNames.get(i), geneConservation.get(j), geneContained.get(j)));
                        writer.newLine();
                    }
                }
                writer.newLine();
            }
        } catch (IOException e) {
            logger.warn("Write error", e);
            return false;
        }
        return true;
    }

    private static boolean writeClusterGenomeInformation(File file, List<GeneCluster> clusters, List<String> genomeNames) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            for (GeneCluster cluster : clusters){
                writer.write(String.format("Cluster ID: %d\t p-Value: %.2f\n", cluster.getId(), cluster.getBestCorrectedScore()));
                GeneClusterOccurrence occ = cluster.getOccurrences(false);
                for (int i=0; i<occ.getSubsequences().length; i++) {
                    if (occ.getSubsequences()[i].length != 0) {
                        writer.write(genomeNames.get(i));
                        writer.newLine();
                    }
                }
                writer.newLine();
            }
        } catch (IOException e) {
            logger.warn("Write error", e);
            return false;
        }
        return true;
    }

    private static boolean writeReferenceClusterTags(File file, List<GeneCluster> clusters) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (GeneCluster cluster : clusters){
                writer.write(String.format("%d, ", cluster.getId()));
                writer.write(cluster.getLocusTags(cluster.getRefSeqIndex()));
                writer.newLine();
            }
        } catch (IOException e){
            logger.warn("Write error", e);
            return false;
        }
        return true;
    }


    private static boolean writeGeneClusterGeneNameTable(File f, List<GeneCluster> clusters, List<String> genomeNames, ExportType.AdditionalExportParameters additionalExportParameters) {
        Set<String> genomeNamesUsedInOutput = additionalExportParameters.getGenomeNames();
        boolean printReference = genomeNamesUsedInOutput.contains(ExportType.AdditionalExportParameters.REFERENCE_GENOME);
        int nrOfGenomes = genomeNamesUsedInOutput.size() - (printReference ? 1 : 0);
        int[] genomesForNaming = new int[nrOfGenomes];
        int j=0;
        for (int i=0; i<genomeNames.size(); i++){
            if (genomeNamesUsedInOutput.contains(genomeNames.get(i))) {
                genomesForNaming[j] = i;
                j++;
            }
        }
        if (j != genomesForNaming.length){
            logger.error("Not enough genomes added in gene name table writer!\nFinal indices {}, should use genomes {} from all genomes {}", genomesForNaming, genomeNamesUsedInOutput, genomeNames);
            throw new InvalidParameterException("Could not match all genomes from AdditionalExportParameters to genomeNames");
        }


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))){
            writer.write("ID \t corrected pValue \t No of genes \t No of genomes");
            if (printReference) {
                writer.write("\t" + ExportType.AdditionalExportParameters.REFERENCE_GENOME);
            }
            for (int i=0; i<genomesForNaming.length; i++){
                writer.write("\t" + genomeNames.get(genomesForNaming[i]));
            }
            if (printReference) {
                writer.write("\t" + ExportType.AdditionalExportParameters.REFERENCE_GENOME);
            }
            for (int i=0; i<genomesForNaming.length; i++){
                writer.write("\t" + genomeNames.get(genomesForNaming[i]));
            }
            if (printReference) {
                writer.write("\t" + ExportType.AdditionalExportParameters.REFERENCE_GENOME);
            }
            for (int i=0; i<genomesForNaming.length; i++){
                writer.write("\t" + genomeNames.get(genomesForNaming[i]));
            }
            writer.newLine();
            for (GeneCluster cluster : clusters) {
                writer.write(String.format("%d\t%.2f\t%d\t%d", cluster.getId(), cluster.getBestCorrectedScore(), cluster.getNoOfGenesInRefOcc(), cluster.getSize()));
                if (printReference) {
                    writer.write("\t"+cluster.getReferenceGeneNames());
                }
                for (int i=0; i<genomesForNaming.length; i++){
                    writer.write("\t"+cluster.getGeneNames(genomesForNaming[i]));
                }
                if (printReference) {
                    writer.write("\t"+cluster.getReferenceGeneOrientations());
                }
                for (int i=0; i<genomesForNaming.length; i++){
                    writer.write("\t"+cluster.getGeneOrientations(genomesForNaming[i]));
                }
                if (printReference) {
                    writer.write("\t"+cluster.getReferenceLocusTags());
                }
                for (int i=0; i<genomesForNaming.length; i++){
                    writer.write("\t"+cluster.getLocusTags(genomesForNaming[i]));
                }
                //writer.write("\t"+cluster.getGenomeString());
                writer.newLine();
            }

        } catch (IOException e) {
            logger.warn("Write error", e);
            return false;
        }
        return true;
    }

	private static boolean writeGeneClusterLatexTable(File f, List<GeneCluster> clusters) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))){
            //writer.write("No of genes & No of genomes & min. $\\delta$ & max. $\\delta$ & avg. $\\delta$ & pValue & corrected pValue & \\\\\n");
            for (int i=0; i<clusters.size(); i++) {
                GeneCluster cluster = clusters.get(i);
                writer.write(String.format("%d & %d & %d & %d & %d & %.1f & %.2f & %.2f & %s \\\\%n", cluster.getId(), cluster.getGeneFamilies().size(), cluster.getSize(), cluster.getMinPWDist(), cluster.getMaxPWDist(), cluster.getAvgPWDist(), cluster.getBestScore(), cluster.getBestCorrectedScore(), cluster.getReferenceGeneNames()));
                System.out.println(String.format("%d\t%.8g\t%.8g", cluster.getId(), cluster.getBestPValue(), cluster.getBestPValueCorrected()));
            }
        } catch (IOException e) {
            logger.warn("Write error", e);
            return false;
        }
        return true;
	}

    private static boolean writeGeneClusterStatistic(File f, List<GeneCluster> clusters, List<String> genomeNames) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))){
            final boolean HIDE_NON_OCCS = false; // if true, don't print all the non occurrences

            writer.write(String.format("Detected %d gene clusters.%n", clusters.size()));

            final int MAXIMUM_CLUSTER_SIZE = 100;

            writer.write("General statistics:\n");

            List<List<Integer>> distancesPerGenome = new ArrayList<>(genomeNames.size());
            List<List<Integer>> clusterSizesPerGenome = new ArrayList<>(genomeNames.size());
            List<List<Integer>> conservedGenesPerGenome = new ArrayList<>(genomeNames.size());
            for (int i=0; i<genomeNames.size(); i++){
                distancesPerGenome.add(new ArrayList<Integer>());
                clusterSizesPerGenome.add(new ArrayList<Integer>());
                conservedGenesPerGenome.add(new ArrayList<Integer>());
            }
            List<Set<GeneFamily>> conservedGenes = new ArrayList<>(genomeNames.size());
            for (int i=0; i<genomeNames.size(); i++)
                conservedGenes.add(new HashSet<GeneFamily>());
            int[] totalConservedGenes = new int[genomeNames.size()];

            int[] clusterSizes = new int[MAXIMUM_CLUSTER_SIZE];
            int[] intervalLength = new int[MAXIMUM_CLUSTER_SIZE];
            int[][] intervalLengths = new int[genomeNames.size()][MAXIMUM_CLUSTER_SIZE];
            int[] intervalLengthMult = new int[MAXIMUM_CLUSTER_SIZE];
            int[][] intervalLengthsMult = new int[genomeNames.size()][MAXIMUM_CLUSTER_SIZE];
            List<String> clusterTabel = new ArrayList<>(clusters.size());
            for (int c=0; c<clusters.size(); c++){
                GeneCluster cluster = clusters.get(c);
                GeneClusterOutput clusterData = cluster.generateGeneClusterOutput(true);

                Set<GeneFamily> refSet = new HashSet<>();
                List<Gene> refGenes = clusterData.getIntervals().get(clusterData.getRefSeq()).get(0);
                for (Gene gene : refGenes)
                    refSet.add(gene.getGeneFamily());

                for (int i=0; i<genomeNames.size(); i++){
                    if (clusterData.getChromosomes().get(i) == null) {
                        conservedGenesPerGenome.get(i).add(-1);
                        continue;
                    }
                    Set<GeneFamily> notContained = new HashSet<>() ;
                    int missingGenes = clusterData.getDistances()[i];
                    for (Gene gene: clusterData.getIntervals().get(i).get(0)) {
                        if (!refSet.contains(gene.getGeneFamily())) {
                            if (!notContained.contains(gene.getGeneFamily())) {
                                missingGenes--;
                                notContained.add(gene.getGeneFamily());
                            }
                        }
                        if (i!=clusterData.getRefSeq() && refSet.contains(gene.getGeneFamily())) {
                            if (!conservedGenes.get(i).contains(gene.getGeneFamily())) {
                                conservedGenes.get(i).add(gene.getGeneFamily());
                                totalConservedGenes[i]++;
                            }
                            if (!conservedGenes.get(clusterData.getRefSeq()).contains(gene.getGeneFamily())) {
                                conservedGenes.get(clusterData.getRefSeq()).add(gene.getGeneFamily());
                                totalConservedGenes[clusterData.getRefSeq()]++;
                            }
                        }
                    }
                    distancesPerGenome.get(i).add(clusterData.getDistances()[i]);
                    conservedGenesPerGenome.get(i).add(cluster.getGeneFamilies().size() - missingGenes);  // nr_of_genes - missing_genes
                    clusterSizesPerGenome.get(i).add(cluster.getGeneFamilies().size());
                }

                StringBuilder builder = new StringBuilder();
                builder.append(c).append("\t").append(cluster.getGeneFamilies().size()).append("\t");

                clusterSizes[cluster.getGeneFamilies().size()]++;
                List<Integer> bestOccsOnSameChrom = new ArrayList<>();
                List<Integer> allOccsOnSameChrom = new ArrayList<>();
                int totalOccs = 0;
                int [] clusterLengths = new int[MAXIMUM_CLUSTER_SIZE];
                for (int i=0; i< cluster.getOccurrences(false).getSubsequences().length; i++) {
                    Subsequence[] subseq =  cluster.getOccurrences(false).getSubsequences()[i];
                    if (subseq != null) {
                        if (subseq.length != 0) {
                            intervalLength[subseq[0].getStop() - subseq[0].getStart() + 1]++;
                            intervalLengths[i][subseq[0].getStop() - subseq[0].getStart() + 1]++;
                        }
                        int bestOccs = 0;
                        for (Subsequence sub : subseq) {
                            intervalLengthMult[sub.getStop() - sub.getStart() + 1]++;
                            intervalLengthsMult[i][sub.getStop() - sub.getStart() + 1]++;
                            clusterLengths[sub.getStop() - sub.getStart() + 1]++;
                            bestOccs++;
                            totalOccs++;
                        }
                        if (!HIDE_NON_OCCS || bestOccs != 0)
                            bestOccsOnSameChrom.add(bestOccs);
                    }
                }
                for (int i=0; i<cluster.getOccurrences(true).getSubsequences().length; i++) {
                    Subsequence[] subseq = cluster.getOccurrences(true).getSubsequences()[i];
                    if (subseq != null) {
                        int allOccs = 0;
                        allOccs =subseq.length;
                        if (!HIDE_NON_OCCS || allOccs != 0)
                            allOccsOnSameChrom.add(allOccs);
                    }
                }

                builder.append(totalOccs).append("\t[");

                boolean first = true;
                for (int i=0; i<clusterLengths.length; i++) {
                    if (clusterLengths[i] != 0) {
                        if (!first)
                            builder.append(", ");
                        else
                            first = false;
                        builder.append(i).append(":").append(clusterLengths[i]);
                    }
                }
                builder.append("]\t[");

                first = true;
                for (int i=0; i<conservedGenesPerGenome.size(); i++) {
                    int cGenes = conservedGenesPerGenome.get(i).get(conservedGenesPerGenome.get(i).size() - 1);

                    if (HIDE_NON_OCCS && cGenes == -1) continue;

                    if (!first)
                        builder.append(", ");
                    else
                        first = false;
                    builder.append(cGenes != -1 ? cGenes : "-");
                }
                builder.append("]\t[");

                first = true;
                for (Integer i: bestOccsOnSameChrom) {
                    if (!first)
                        builder.append(", ");
                    else
                        first = false;
                    builder.append(i);
                }

                builder.append("]\t[");

                first = true;
                for (Integer i: allOccsOnSameChrom) {
                    if (!first)
                        builder.append(", ");
                    else
                        first = false;
                    builder.append(i);
                }
                builder.append("]\n");
                clusterTabel.add(builder.toString());
            }

            writer.write("Genes per cluster \t #clusters\n");
            for (int i=0; i<clusterSizes.length; i++) {
                if (clusterSizes[i] != 0)
                    writer.write(String.format("%d \t %d\n", i, clusterSizes[i]));
            }
            writer.write("\n");

            int maxDist = 0;
            int maxSize = 0;
            int maxGenes = 0;
            writer.write("Genome \t # intervals \t conserved genes \t avg. cluster size \t avg. distance \t avg. # conserved genes\n");
            for (int i=0; i<genomeNames.size(); i++) {
                int sumIntervals = distancesPerGenome.get(i).size();
                if (HIDE_NON_OCCS && sumIntervals == 0)
                    continue;
                int sumClusterSize = 0;
                for (Integer v : clusterSizesPerGenome.get(i)) {
                    sumClusterSize += v;
                    if (v > maxSize) maxSize = v;
                }
                int sumDistance = 0;
                for (Integer v : distancesPerGenome.get(i)) {
                    sumDistance += v;
                    if (v > maxDist) maxDist = v;
                }
                int sumConservedGenesPerCluster = 0;
                for (Integer v : conservedGenesPerGenome.get(i)) {
                    if (v > 0) sumConservedGenesPerCluster += v;
                    if (v > maxGenes) maxGenes = v;
                }

                writer.write(String.format("%s \t %d \t %d \t %f \t %f \t %f\n", genomeNames.get(i), sumIntervals, totalConservedGenes[i],((float)sumClusterSize)/sumIntervals, ((float)sumDistance)/sumIntervals, ((float)sumConservedGenesPerCluster)/sumIntervals));
            }
            writer.write("\n");

            int lenSum = 0;
            int multLenSum = 0;
            writer.write("Interval Lengths \t #intervals \t #multiple intervals \n");
            for (int i=0; i<intervalLength.length; i++) {
                if (intervalLength[i] != 0 || intervalLengthMult[i] != 0) {
                    writer.write(String.format("%d \t %d \t %d \n", i, intervalLength[i], intervalLengthMult[i]));
                    lenSum += intervalLength[i];
                    multLenSum += intervalLengthMult[i];
                }
            }
            writer.write(String.format("Sum \t %d \t %d \n", lenSum, multLenSum));
            writer.write("\n");

            // Write #clusters per distance
            int[][] distances = new int[genomeNames.size()][maxDist+1];
            for (int i=0; i<genomeNames.size(); i++)
                for (Integer v : distancesPerGenome.get(i))
                    distances[i][v]++;

            for (int i=-1; i<genomeNames.size(); i++) {
                if (i<0)
                    writer.write("Distance");
                else
                    writer.write(String.format("\t%s", genomeNames.get(i)));
            }
            writer.write("\n");
            for (int d=0; d<=maxDist; d++) {
                for (int i=-1; i<genomeNames.size(); i++) {
                    if (i<0)
                        writer.write(String.format("%d", d));
                    else
                        writer.write(String.format("\t%d", distances[i][d]));
                }
                writer.write("\n");
            }
            writer.write("\n");

            // Write #clusters per cluster size
            int[][] clusterSize = new int[genomeNames.size()][maxSize+1];
            for (int i=0; i<genomeNames.size(); i++)
                for (Integer v : clusterSizesPerGenome.get(i))
                    clusterSize[i][v]++;

            for (int i=-1; i<genomeNames.size(); i++) {
                if (i<0)
                    writer.write("ClusterSize");
                else
                    writer.write(String.format("\t%s", genomeNames.get(i)));
            }
            writer.write("\n");
            for (int d=0; d<=maxSize; d++) {
                for (int i=-1; i<genomeNames.size(); i++) {
                    if (i<0)
                        writer.write(String.format("%d", d));
                    else
                        writer.write(String.format("\t%d", clusterSize[i][d]));
                }
                writer.write("\n");
            }
            writer.write("\n");

            // Write #clusters per conserved genes
            int[][] genes = new int[genomeNames.size()][maxGenes+1];
            for (int i=0; i<genomeNames.size(); i++)
                for (Integer v : conservedGenesPerGenome.get(i))
                    if (v >= 0) genes[i][v]++;

            for (int i=-1; i<genomeNames.size(); i++) {
                if (i<0)
                    writer.write("ConservedGenes");
                else
                    writer.write(String.format("\t%s", genomeNames.get(i)));
            }
            writer.write("\n");
            for (int d=0; d<=maxGenes; d++) {
                for (int i=-1; i<genomeNames.size(); i++) {
                    if (i<0)
                        writer.write(String.format("%d", d));
                    else
                        writer.write(String.format("\t%d", genes[i][d]));
                }
                writer.write("\n");
            }
            writer.write("\n");

            writer.write("Cluster\t# Genes\tOccurrences\tLengths: #\t# conserved Genes\tBestOccs per Chromosome\tAllOccs per Chromosome\n");
            for (String line : clusterTabel)
                writer.write(line);
            writer.write("\n");

            for (int i=0; i<intervalLengths.length; i++) {
                boolean contained = false;
                for (Integer len : intervalLengths[i])
                    if (len != 0) {
                        contained = true;
                        break;
                    }
                if (contained) {
                    writer.write(String.format("%s: \n", genomeNames.get(i)));
                    writer.write("lengths \t #intervals \t # multiple intervals \n");
                    for (int j=0; j<intervalLengths[i].length; j++) {
                        if (intervalLengths[i][j] != 0 || intervalLengthsMult[i][j] != 0)
                            writer.write(String.format("%d \t %d \t %d \n", j, intervalLengths[i][j], intervalLengthsMult[i][j]));
                    }
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            logger.warn("Write error", e);
            return false;
        }
        return true;
    }


	private static boolean writeGeneClusterOutput(File f, List<GeneCluster> clusters) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))){
            final boolean HIDE_NON_OCCS = false; // if true, don't print all the non occurrences

            writer.write(String.format("Detected %d gene clusters.%n", clusters.size()));

            for (GeneCluster cluster : clusters) {
                writeSingleGeneClusterData(writer, cluster);
            }
            return true;
        } catch (IOException e) {
            logger.warn("Write error", e);
            return false;
        }
	}

	private static boolean writeInternalDuplicationsData(File f, List<GeneCluster> clusters, List<String> genomeNames) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))){
            final boolean HIDE_NON_OCCS = true; // if true, don't print all the non occurrences

            writer.write(String.format("Detected %d duplications.%n", clusters.size()));

            writer.write("General statistics:\n");

            List<List<Integer>> distancesPerGenome = new ArrayList<>(genomeNames.size());
            List<List<Integer>> clusterSizesPerGenome = new ArrayList<>(genomeNames.size());
            List<List<Integer>> conservedGenesPerGenome = new ArrayList<>(genomeNames.size());
            List<Set<GeneFamily>> conservedGenes = new ArrayList<>(genomeNames.size());
            for (String genomeName : genomeNames) {
                distancesPerGenome.add(new ArrayList<Integer>());
                clusterSizesPerGenome.add(new ArrayList<Integer>());
                conservedGenesPerGenome.add(new ArrayList<Integer>());
                conservedGenes.add(new HashSet<GeneFamily>());
            }
            int[] totalConservedGenes = new int[genomeNames.size()];

            int[] clusterSizes = new int[50];
            int[] intervalLength = new int[50];
            int[][] intervalLengths = new int[genomeNames.size()][50];
            int[] intervalLengthMult = new int[50];
            int[][] intervalLengthsMult = new int[genomeNames.size()][50];
            List<String> clusterTabel = new ArrayList<>(clusters.size());
            for (int c=0; c<clusters.size(); c++){
                GeneCluster cluster = clusters.get(c);
                GeneClusterOutput clusterData = cluster.generateGeneClusterOutput(true);

                Set<GeneFamily> refSet = new HashSet<>();
                List<List<Gene>> refGenes = clusterData.getIntervals().get(clusterData.getRefSeq());

                for (List<Gene> geneList : refGenes)
                    for (Gene gene : geneList)
                        refSet.add(gene.getGeneFamily());

                for (int i=0; i<genomeNames.size(); i++){
                    if (clusterData.getChromosomes().get(i) == null) {
                        conservedGenesPerGenome.get(i).add(-1);
                        continue;
                    }
                    Set<GeneFamily> notContained = new HashSet<>() ;
                    int missingGenes = clusterData.getDistances()[i];
                    for (List<Gene> genes : clusterData.getIntervals().get(i)) {
                        for (Gene gene: genes) {
                            GeneFamily geneFamily = gene.getGeneFamily();
                            if (!refSet.contains(geneFamily)) {
                                if (!notContained.contains(geneFamily)) {
                                    missingGenes--;
                                    notContained.add(geneFamily);
                                }
                            }
                            if (i!=clusterData.getRefSeq() && refSet.contains(geneFamily)) {
                                if (!conservedGenes.get(i).contains(geneFamily)) {
                                    conservedGenes.get(i).add(geneFamily);
                                    totalConservedGenes[i]++;
                                }
                                if (!conservedGenes.get(clusterData.getRefSeq()).contains(geneFamily)) {
                                    conservedGenes.get(clusterData.getRefSeq()).add(geneFamily);
                                    totalConservedGenes[clusterData.getRefSeq()]++;
                                }
                            }
                        }
                    }
                    distancesPerGenome.get(i).add(clusterData.getDistances()[i]);
                    conservedGenesPerGenome.get(i).add(cluster.getGeneFamilies().size() - missingGenes);  // nr_of_genes - missing_genes
                    clusterSizesPerGenome.get(i).add(cluster.getGeneFamilies().size());
                }

                StringBuilder builder = new StringBuilder();
                builder.append(c).append("\t").append(cluster.getGeneFamilies().size()).append("\t");

                clusterSizes[cluster.getGeneFamilies().size()]++;
                List<Integer> bestOccsOnSameChrom = new ArrayList<>();
                List<Integer> allOccsOnSameChrom = new ArrayList<>();
                int totalOccs = 0;
                int [] clusterLengths = new int[50];
                for (int i=0; i<cluster.getOccurrences(false).getSubsequences().length; i++) {
                    Subsequence[] subseq = cluster.getOccurrences(false).getSubsequences()[i];
                    if (subseq != null) {
                        if (subseq.length != 0) {
                            totalOccs++;
                            intervalLength[subseq[0].getStop() - subseq[0].getStart() + 1]++;
                            intervalLengths[i][subseq[0].getStop() - subseq[0].getStart() + 1]++;
                        }
                        int bestOccs = 0;
                        for (Subsequence sub : subseq) {
                            intervalLengthMult[sub.getStop() - sub.getStart() + 1]++;
                            intervalLengthsMult[i][sub.getStop() - sub.getStart() + 1]++;
                            clusterLengths[sub.getStop() - sub.getStart() + 1]++;
                            bestOccs++;
                        }
                        if (!HIDE_NON_OCCS || bestOccs != 0)
                            bestOccsOnSameChrom.add(bestOccs);
                    }
                }
                for (int i=0; i<cluster.getOccurrences(true).getSubsequences().length; i++) {
                    Subsequence[] subseq = cluster.getOccurrences(true).getSubsequences()[i];
                    if (subseq != null) {
                        int allOccs = subseq.length;
                        if (!HIDE_NON_OCCS || allOccs != 0)
                            allOccsOnSameChrom.add(allOccs);
                    }
                }

                builder.append(totalOccs).append("\t[");

                boolean first = true;
                for (int i=0; i<clusterLengths.length; i++) {
                    if (clusterLengths[i] != 0) {
                        if (!first)
                            builder.append(", ");
                        else
                            first = false;
                        builder.append(i).append(":").append(clusterLengths[i]);
                    }
                }
                builder.append("]\t[");

                first = true;
                for (List<Integer> aConservedGenesPerGenome : conservedGenesPerGenome) {
                    int cGenes = aConservedGenesPerGenome.get(aConservedGenesPerGenome.size() - 1);

                    if (HIDE_NON_OCCS && cGenes == -1) continue;

                    if (!first)
                        builder.append(", ");
                    else
                        first = false;
                    builder.append(cGenes != -1 ? cGenes : "-");
                }
                builder.append("]\t[");

                first = true;
                for (Integer i: allOccsOnSameChrom) {
                    if (!first)
                        builder.append(", ");
                    else
                        first = false;
                    builder.append(i);
                }
                builder.append("]\n");
                clusterTabel.add(builder.toString());
            }

            writer.write("Genes per cluster \t #clusters\n");
            for (int i=0; i<clusterSizes.length; i++) {
                if (clusterSizes[i] != 0)
                    writer.write(String.format("%d \t %d%n", i, clusterSizes[i]));
            }
            writer.write("\n");

            int lenSum = 0;
            int multLenSum = 0;
            writer.write("Interval Lengths \t #intervals \t #multiple intervals \n");
            for (int i=0; i<intervalLength.length; i++) {
                if (intervalLength[i] != 0 || intervalLengthMult[i] != 0) {
                    writer.write(String.format("%d \t %d \t %d %n", i, intervalLength[i], intervalLengthMult[i]));
                    lenSum += intervalLength[i];
                    multLenSum += intervalLengthMult[i];
                }
            }
            writer.write(String.format("Sum \t %d \t %d %n", lenSum, multLenSum));
            writer.write("\n");

            writer.write("Cluster\t#Genes\tOccurrences on Chromosomes\tLengths:#\t #conserved Genes\tAllOccs per Chromosome\n");
            for (String line : clusterTabel)
                writer.write(line);
            writer.write("\n");

            for (GeneCluster cluster : clusters) {
                writeSingleGeneClusterData(writer, cluster);
            }
            return true;
        } catch (IOException e) {
            logger.warn("Write error", e);
            return false;
        }
	}

	private static void writeSingleGeneClusterData(Writer writer, GeneCluster cluster) throws IOException {
		GeneClusterOutput clusterData = cluster.generateGeneClusterOutput(true);
		writer.write(String.format("new cluster: ID = %d, pValue = %e, refSeq = %d%n", clusterData.getId(), clusterData.getPValue(), clusterData.getRefSeq()+1));
		writer.write("in chromosomes:\n");
		for (int i=0; i<clusterData.getChromosomes().size(); i++)
			if (clusterData.getChromosomes().get(i) != null) {
				for (int j=0; j<clusterData.getChromosomes().get(i).size(); j++) {
					String chrNo;
					if (clusterData.getChromosomes().get(i).size() <= 1)
						chrNo = String.valueOf(i+1);
					else
						chrNo = String.format("%d.%d", i+1, j);
					
					writer.write(String.format("%s:\t%s\t", chrNo, clusterData.getChromosomes().get(i).get(j)));
					
					int[] borders = clusterData.getIntervalBorders().get(i).get(j);
					writer.write(String.format("[%d,%d]\t", borders[0], borders[1]));
							
					for (Gene gene : clusterData.getIntervals().get(i).get(j))
						if (gene.getOrientation().equals(Gene.GeneOrientation.POSITIVE))
							writer.write(String.format("%s>  ", gene.getExternalId()));
						else
							writer.write(String.format("<%s  ", gene.getExternalId()));
					writer.write("\n");
				}
			}
		writer.write("ChromosomeNr:");
		for (int i=0; i<clusterData.getDistances().length; i++)
			if (clusterData.getDistances()[i] >= 0)
				writer.write(String.format("\t%d", i+1));
		writer.write("\n");
		writer.write("Distances:");
		for (int i=0; i<clusterData.getDistances().length; i++)
			if (clusterData.getDistances()[i] >= 0)
				writer.write(String.format("\t%d", clusterData.getDistances()[i]));
		writer.write("\n");
		writer.write("Occurrences:");
		for (int i=0; i<clusterData.getDistances().length; i++)
			if (clusterData.getDistances()[i] >= 0)
				writer.write(String.format("\t%d", clusterData.getNrOfOccurrences()[i]));
		writer.write("\n");
		
		Map<GeneFamily, Gene[][]>geneAnnotations = clusterData.getGeneAnnotations();

        for (Map.Entry<GeneFamily, Gene[][]> entry : geneAnnotations.entrySet()) {
			Gene[][] annotation = entry.getValue();
			for (int i=0; i<annotation.length; i++) {
				if (0==i)
					writer.write(String.format("%s:", entry.getKey().getExternalId()));
				for (int j=0; j<annotation[i].length; j++){
					String chrNo;
					if (annotation[i].length <= 1)
						chrNo = String.valueOf(i+1);
					else
						chrNo = String.format("%d.%d", i+1, j);
					
					if (annotation[i][j] != null) 
						writer.write(String.format("\t%s: %s%n", chrNo, annotation[i][j].getSummary()));
					else if (clusterData.getChromosomes().get(i) != null)
						writer.write(String.format("\t%s: ---%n", chrNo));
				}
			}
		}
		writer.write("\n");
	}
	
	public static boolean exportResultsToFileNEW(File f, List<GeneCluster> clusters) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));

			Collections.sort(clusters, new Comparator<GeneCluster>() {
				@Override
				public int compare(GeneCluster o1, GeneCluster o2) {
					return o1.getBestPValue().compareTo(o2.getBestPValue());
				}
			});

			writer.write(String.format("gene cluster list of size %d%n", clusters.size()));

			for (GeneCluster cluster : clusters) {
				writer.write(String.format("K = %d%n", 0)); //TODO 0
				writer.write(String.format("new Cluster: pValue = %e%n", cluster.getBestPValue()));
				writer.write("gene content: " + geneContentString(cluster.getGeneFamilies()) + "\n");
				writer.write(String.format("             type: %c, min total dist: %d, refGenomeNr: %d%n", 'r', cluster.getMinTotalDist(), cluster.getRefSeqIndex())); //TODO 'r'
				writer.write("%n");
				writer.write("best clusters:%n");
                writeClusterOccurrence(cluster.getOccurrences(false), writer);
				writer.write("%n");
				writer.write("all clusters:%n");
                writeClusterOccurrence(cluster.getOccurrences(true), writer);
			}

			writer.close();
			return true;
		} catch (IOException e) {
			logger.warn("Write error", e);
			return false;
		}
	}
	
	private static String geneContentString(Set<GeneFamily> geneContent) {
		StringBuilder builder = new StringBuilder(geneContent.size() * 2);
		for (GeneFamily geneFamily : geneContent)
			builder.append(geneFamily.getExternalId()).append(" ");
		return builder.toString();
	}
	
	private static void writeClusterOccurrence(GeneClusterOccurrence occ, Writer writer) throws IOException {
		writer.write(String.format("next ACI: pValue: %e, total dist: %d, support: %d, number of refOccs: %d%n",
				occ.getBestpValue(),
				occ.getTotalDist(),
				occ.getSupport(),
				0)); //TODO 0
		for (int i=0; i<occ.getSubsequences().length; i++) {
			for (Subsequence subsequence : occ.getSubsequences()[i]) {
				writer.write(String.format("\t\tgenome %d, chr %d: [%d,%d] dist: %d, pValue: %e%s%n",
						i,
						subsequence.getChromosome(),
						subsequence.getStart(),
						subsequence.getStop(),
						subsequence.getDist(),
						subsequence.getpValue(),
						subsequence.getDist() == 0 ? " RefOCC" : ""));
			}			
		}
		writer.write("finished%n");
	}
}
