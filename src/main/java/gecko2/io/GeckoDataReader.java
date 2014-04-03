package gecko2.io;

import gecko2.algorithm.GeneFamily;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;

import java.io.IOException;
import java.text.ParseException;
import java.util.Set;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public interface GeckoDataReader {
    /**
     * A getter for the complete set of gene families
     *
     * @return the gene family set
     */
    public Set<GeneFamily> getGeneFamilySet();

    /**
     * A getter for the total number of gene families that contain more than 1 gene
     *
     * @return the number of gene families that contain more than 1 gene
     */
    public int getNumberOfGeneFamiliesWithMultipleGenes();

    /**
     * A getter for the gene family grouping all genes with no gene family information
     *
     * @return the unknown gene family
     */
    public GeneFamily getUnknownGeneFamily();

    /**
     * @return the genomes from the input file
     */
    public Genome[] getGenomes();

    /**
     * @return the maxIdLength from the input file
     */
    public int getMaxIdLength();

    /**
     * @return the maxNameLength from the input file
     */
    public int getMaxNameLength();

    /**
     * @return the maxLocusTagLength from the input file
     */
    public int getMaxLocusTagLength();

    /**
     * @return the gene clusters
     */
    public GeneCluster[] getGeneClusters();

    /**
     * Reads all data from the file
     * @throws IOException if an IO problem occurred while reading the file
     * @throws ParseException if the file format is wrong
     */
    public void readData() throws IOException, ParseException;
}
