package gecko2;

import gecko2.algorithm.Parameter;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class CommandLineExecution {
    public static void runAlgorithm(CommandLineOptions options) {
        Parameter parameter = new Parameter(options.getMaxDistance(), options.getMinClusterSize(), options.getMinCoveredGenomes(), Parameter.QUORUM_NO_COST, opMode, refType);
        parameter.setAlphabetSize(reader.getGeneLabelMap().size());

    }
}
