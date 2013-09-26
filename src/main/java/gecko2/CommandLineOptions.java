package gecko2;

import gecko2.algorithm.Parameter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.DelimitedOptionHandler;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.Setter;

import java.io.File;
import java.util.List;

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

    @Option(name="-o", aliases = "--operationMode", usage = "the operation mode, [reference] cluster (default), [median] gene cluster, or [center] gene cluster.")
    private Parameter.OperationMode operationMode = Parameter.OperationMode.reference;

    @Option(name="-r", aliases = "--referenceGenomeName", usage = "name of the reference genome.\nIf not set all genomes are used as reference.\nThe name has to be uniquely contained in a single genome")
    private String referenceGenomeName = "";

    /*
     * Files
     */
    @Option(name="-in", aliases = "--Infile", usage = "the .gck or .cog input file")
    private File infile = null;


    @Option(name="-gL", aliases = "--genomeList", handler=GenomeListOptionHandler.class, usage = "the indices of the genomes that shall be imported from the .cog file.\n A String containing a comma separated list of integers (\"1, 3, 5, 8\")")
    private List<Integer> genomeList = null;
    /*private void setGenomesList(String line){
        String[] sLine = line.split(",");
        genomeList = new ArrayList<Integer>(sLine.length);
        for (String index : sLine)
            genomeList.add(Integer.parseInt(index));
    }*/

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

    public Parameter.OperationMode getOperationMode() {
        return operationMode;
    }

    public String getReferenceGenomeName() {
        return referenceGenomeName;
    }

    public File getInfile() {
        return infile;
    }

    public List<Integer> getGenomeList() {
        return genomeList;
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

    public static class GenomeListOptionHandler extends DelimitedOptionHandler<Integer> {
        public GenomeListOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Integer> setter) {
            super(parser, option, setter, ",", new GenomeOptionHandler(parser, option,setter));
        }
    }

    public static class GenomeOptionHandler extends OneArgumentOptionHandler<Integer> {
        public GenomeOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Integer> setter) {
            super(parser, option, setter);
        }

        @Override
        protected Integer parse(String argument) throws CmdLineException {
            try {
                return Integer.parseInt(argument.trim());
            } catch (NumberFormatException e) {
                throw new CmdLineException(owner, e);
            }
        }
    }



}
