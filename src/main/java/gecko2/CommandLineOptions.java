package gecko2;

import org.kohsuke.args4j.Option;

import java.io.File;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class CommandLineOptions {

    /*
     * Algorithm parameters
     */
    @Option(name="-d", aliases = "--distance", required = true, usage = "the maximum allowed distance")
    private int maxDistance;

    @Option(name="-s", aliases = "--size", required = true, usage = "the minimum cluster size")
    private int minClusterSize;

    @Option(name="-q", aliases = "--quorum", usage = "the minimum number of covered genomes")
    private int minCoveredGenomes = 0;

    @Option(name="-gGF", aliases = "--genomeGroupingFactor", usage = "all genomes with lower breakpoint distance are treated as one group")
    private double genomeGroupingFactor = 1.1;

    @Option(name="-r", aliases = "--referenceGenomeIndex", usage = "index of the reference genome.\nIf not set all genomes are used as reference")
    private int referenceGenome = -1;

    /*
     * Files
     */
    @Option(name="-i", aliases = "--Infile", usage = "the .gck input file")
    private File infile = null;

    @Option(name="-out", aliases = "--Outfile", required = true, usage = "the output file")
    private File outfile;

    /*
     * Others
     */
    @Option(name="-gui", usage = "start the gui")
    private boolean gui = false;

    @Option(name="-h", aliases = "--help", usage = "show this help and exit")
    private boolean help = false;

    /*
     * Getters
     */
    public int getMaxDistance() {
        return maxDistance;
    }

    public int getMinClusterSize() {
        return minClusterSize;
    }

    public int getMinCoveredGenomes() {
        return minCoveredGenomes;
    }

    public double getGenomeGroupingFactor() {
        return genomeGroupingFactor;
    }

    public int getReferenceGenome() {
        return referenceGenome;
    }

    public File getInfile() {
        return infile;
    }

    public File getOutfile() {
        return outfile;
    }

    public boolean useGui() {
        return gui;
    }

    public boolean showHelp() {
        return help;
    }
}
