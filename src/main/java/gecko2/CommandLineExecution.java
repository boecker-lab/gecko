package gecko2;

import gecko2.algorithm.Genome;
import gecko2.algorithm.Parameter;
import gecko2.io.SessionWriter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class CommandLineExecution {
    public static void runAlgorithm(CommandLineOptions options, int alphabetSize) {
        Parameter.ReferenceType refType;
        if (options.getReferenceGenomeIndex() == -1)
            refType = Parameter.ReferenceType.allAgainstAll;
        else {
            refType = Parameter.ReferenceType.genome;
            Genome[] genomes = GeckoInstance.getInstance().getGenomes();

            if (options.getReferenceGenomeIndex() >= genomes.length)
                throw new IllegalArgumentException(String.format("Error! Reference genome index (%d) is higher than total number of genomes (%d)!", options.getReferenceGenomeIndex(), genomes.length));

            Genome first = genomes[0];
            genomes[0] = genomes[options.getReferenceGenomeIndex()];
            genomes[options.getReferenceGenomeIndex()] = first;
        }

        Parameter parameter = new Parameter(options.getMaxDistance(), options.getMinClusterSize(), options.getMinCoveredGenomes(), Parameter.QUORUM_NO_COST, options.getOperationMode(), refType);
        parameter.setAlphabetSize(alphabetSize);

        // compute the clusters
        ExecutorService executor = GeckoInstance.getInstance().performClusterDetection(parameter, false, options.getGenomeGroupingFactor());
        executor.shutdown();
        try{
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Remove outfile
        if (options.getOutfile().exists())
            options.getOutfile().delete();

        // Save session
        SessionWriter.saveSessionToFile(options.getOutfile());
    }
}
