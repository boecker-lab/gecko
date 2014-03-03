package gecko2.io;

import gecko2.GeckoInstance;
import gecko2.algorithm.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;


/**
 * The class implements a writer which write a gecko session to a file.
 * The code of this file is exported from GeckoInstance.java and modified.
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.03
 */
public class SessionWriter
{

    final static String SEPERATOR = "\t";

    final static String GENOME_SECTION_START = "<genomes>";
    final static String GENOME_SECTION_END = "</genomes>";
    final static String GENOME_START = "<genome>";
    final static String GENOME_END = "</genome>";
    final static String CHROMOSOME_START = "<chromosome>";
    final static String CHROMOSOME_END = "</chromosome>";

    final static String CLUSTER_SECTION_START = "<clusters>";
    final static String CLUSTER_SECTION_END = "</clusters>";
    final static String CLUSTER_START = "<cluster>";
    final static String CLUSTER_END = "</cluster>";
    final static String OCC_START = "<occ>";
    final static String OCC_END = "</occ>";

	/**
	 * Saves the current gecko session to a given file
	 * @param f The file to write to
	 */
	public static boolean saveSessionToFile(File f) 
	{
		GeckoInstance.getInstance().setLastSavedFile(f);
        boolean returnValue = true;

        try (BufferedWriter out = Files.newBufferedWriter(f.toPath(), Charset.forName("UTF-8"))) {
        //try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f)); ObjectOutputStream out = xstream.createObjectOutputStream(zos)) {
        //    zos.putNextEntry(new ZipEntry(f.getName()));
            writeGenomes(out, GeckoInstance.getInstance().getGenomes());
            writeClusters(out, GeckoInstance.getInstance().getClusters());

        }
        catch (IOException e) {
            e.printStackTrace();
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
                    out.write(gene.getId() + SEPERATOR + gene.getExternalId() + SEPERATOR + gene.getTag() + SEPERATOR + gene.getAnnotation() + SEPERATOR + gene.getName() + SEPERATOR + gene.getFamilySize());
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

    private static void writeClusters(BufferedWriter out, GeneCluster[] clusters) throws  IOException {
        out.write(CLUSTER_SECTION_START);
        out.newLine();
        for (GeneCluster cluster : clusters){
            out.write(CLUSTER_START);
            out.newLine();
            out.write(cluster.getId() + SEPERATOR + cluster.getRefSeqIndex() + SEPERATOR + cluster.getType().getCharMode() + SEPERATOR + cluster.getMinTotalDist() + SEPERATOR + cluster.getBestPValue() + SEPERATOR + cluster.getBestPValueCorrected());
            out.newLine();
            out.write(Arrays.toString(cluster.getGenes()));
            out.newLine();
            for (GeneClusterOccurrence occ : cluster.getAllOccurrences()) {
                out.write(OCC_START);
                out.newLine();
                out.write(occ.getId() + SEPERATOR + occ.getBestpValue() + SEPERATOR + occ.getSupport() + SEPERATOR + occ.getTotalDist());
                out.newLine();
                for (int i=0; i<occ.getSubsequences().length; i++) {
                    for (Subsequence sub : occ.getSubsequences()[i]){
                        out.write(i + SEPERATOR + sub.getChromosome() + SEPERATOR + sub.getDist() + SEPERATOR + sub.getStart() + SEPERATOR + sub.getStop() + SEPERATOR + sub.getpValue());
                        out.newLine();
                    }
                }
                out.write(OCC_END);
                out.newLine();
            }
            out.write(CLUSTER_END);
            out.newLine();
        }
        out.write(CLUSTER_SECTION_END);
        out.newLine();
    }
}
