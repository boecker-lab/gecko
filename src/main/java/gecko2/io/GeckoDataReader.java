package gecko2.io;

import gecko2.algorithm.DataSet;
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
     * Reads all data from the file
     * @throws IOException if an IO problem occurred while reading the file
     * @throws ParseException if the file format is wrong
     */
    public DataSet readData() throws IOException, ParseException;
}

