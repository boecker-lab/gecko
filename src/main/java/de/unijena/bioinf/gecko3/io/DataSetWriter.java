/*
 * Copyright 2014 Sascha Winter
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;


/**
 * The class implements a writer which writes a gecko session to a file.
 */
public class DataSetWriter{
    private static final Logger logger = LoggerFactory.getLogger(DataSetWriter.class);

    final static String SEPERATOR = "\t";

    final static String GENOME_SECTION_START = "<genomes>";
    final static String GENOME_SECTION_END = "</genomes>";
    final static String GENOME_START = "<genome>";
    final static String GENOME_END = "</genome>";
    final static String CHROMOSOME_START = "<chromosome>";
    final static String CHROMOSOME_END = "</chromosome>";

    final static String CLUSTER_SECTION_START = "<clusters>";
    final static String CLUSTER_SECTION_END = "</clusters>";
    final static String PARAMETERS_START = "<parameters>";
    final static String PARAMETERS_END = "</parameters>";
    final static String CLUSTER_START = "<cluster>";
    final static String CLUSTER_END = "</cluster>";
    final static String OCC_START = "<occ>";
    final static String OCC_END = "</occ>";

	/**
	 * Saves the current gecko session to a given file
	 * @param f The file to write to
	 */
	public static boolean saveDataSetToFile(DataSet data, File f)
	{
        boolean returnValue = true;

        try (BufferedWriter out = Files.newBufferedWriter(f.toPath(), Charset.forName("UTF-8"))) {
            writeGenomes(out, data.getGenomes());
            writeClusters(out, data.getClusters(), data.getParameters());

        }
        catch (IOException e) {
            logger.warn("Unable to write dataset", e);
            returnValue = false;
        }
        return returnValue;
	}

    private static void writeGenomes(BufferedWriter out, Genome[] genomes) throws  IOException {
        out.write(GENOME_SECTION_START);
        out.newLine();
        for (Genome genome : genomes) {
            out.write(GENOME_START);
            out.newLine();
            out.write(genome.getName());
            out.newLine();
            for (Chromosome chr : genome.getChromosomes()) {
                out.write(CHROMOSOME_START);
                out.newLine();
                out.write(chr.getName());
                out.newLine();
                for (Gene gene : chr.getGenes()) {
                    out.write(gene.getOrientation().getEncoding() + SEPERATOR + gene.getExternalId() + SEPERATOR + gene.getTag() + SEPERATOR + gene.getAnnotation() + SEPERATOR + gene.getName() + SEPERATOR + (gene.isUnknown() ? 1 : gene.getFamilySize()));
                    out.newLine();
                }
                out.write(CHROMOSOME_END);
                out.newLine();
            }
            out.write(GENOME_END);
            out.newLine();
        }
        out.write(GENOME_SECTION_END);
        out.newLine();
    }

    private static void writeClusters(BufferedWriter out, List<GeneCluster> clusters, Parameter parameters) throws  IOException {
        out.write(CLUSTER_SECTION_START);
        out.newLine();
        if (parameters != null){
            out.write(PARAMETERS_START);
            out.newLine();
            writeParameters(out, parameters);
            out.newLine();
            out.write(PARAMETERS_END);
            out.newLine();
        }
        for (GeneCluster cluster : clusters){
            out.write(CLUSTER_START);
            out.newLine();
            out.write(cluster.getId() + SEPERATOR + cluster.getRefSeqIndex() + SEPERATOR + cluster.getType().getCharMode() + SEPERATOR + cluster.getMinTotalDist() + SEPERATOR + cluster.getBestPValue() + SEPERATOR + cluster.getBestPValueCorrected());
            out.newLine();
            out.write(cluster.getGeneFamilyString());
            out.newLine();
            out.write(OCC_START);
            out.newLine();
            out.write(cluster.getOccurrences(true).getId() + SEPERATOR + cluster.getOccurrences(true).getBestpValue() + SEPERATOR + cluster.getOccurrences(true).getSupport() + SEPERATOR + cluster.getOccurrences(true).getTotalDist());
            out.newLine();
            for (int i=0; i<cluster.getOccurrences(true).getSubsequences().length; i++) {
                for (Subsequence sub : cluster.getOccurrences(true).getSubsequences()[i]){
                    out.write(i + SEPERATOR + sub.getChromosome() + SEPERATOR + sub.getDist() + SEPERATOR + sub.getStart() + SEPERATOR + sub.getStop() + SEPERATOR + sub.getpValue());
                    out.newLine();
                }
            }
            out.write(OCC_END);
            out.newLine();
            out.write(CLUSTER_END);
            out.newLine();
        }
        out.write(CLUSTER_SECTION_END);
        out.newLine();
    }

    /**
     * Writes the parameters. parameters must not be null!
     * @param out
     * @param p must not be null
     * @throws IOException
     */
    private static void writeParameters(BufferedWriter out, Parameter p) throws  IOException {
        out.write(p.getOperationModeChar() + SEPERATOR + p.getRefTypeChar() + SEPERATOR + (p.searchRefInRef()? 1 : 0) + SEPERATOR + p.getMinClusterSize() + SEPERATOR + p.getQ() + SEPERATOR);
        if (p.useDeltaTable())
            out.write(Arrays.deepToString(p.getDeltaTable()));
        else
            out.write(Integer.toString(p.getDelta()));
    }
}
