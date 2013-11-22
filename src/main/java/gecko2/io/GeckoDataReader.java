package gecko2.io;

import gecko2.algorithm.ExternalGeneId;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;

import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public interface GeckoDataReader {
    /**
     * The method is a getter for the geneLabelMap which contains the relation between external ID
     * and internal ID from the gene names
     *
     * @return the geneLabelMap (HashMap)
     */
    public Map<Integer, ExternalGeneId> getGeneLabelMap();

    /**
     * @return the genomes from the input file.
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
