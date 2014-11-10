package gecko2.commandLine;

import gecko2.GeckoInstance;
import gecko2.datastructures.GeneCluster;
import gecko2.datastructures.Genome;
import gecko2.datastructures.Parameter;
import gecko2.io.DataSetWriter;
import gecko2.io.ResultWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class CommandLineExecution {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineExecution.class);

    public static void runAlgorithm(CommandLineOptions options) {
        if (!options.noComputation()) {
            Parameter.ReferenceType refType;
            if (options.getReferenceGenomeName().equals(""))
                refType = Parameter.ReferenceType.allAgainstAll;
            else {
                refType = Parameter.ReferenceType.genome;
                Genome[] genomes = GeckoInstance.getInstance().getGenomes();

                int index = -1;
                for (int i=0; i<genomes.length; i++) {
                    if (genomes[i].getName().contains(options.getReferenceGenomeName()))
                        if (index != -1)
                            throw new IllegalArgumentException(String.format("Error! Reference genome name (%s) is contained in more than one genome name (%s and %s)!", options.getReferenceGenomeName(), genomes[index].getName(), genomes[i].getName()));
                        else
                            index = i;
                }
                if (index == -1)
                    throw new IllegalArgumentException(String.format("Error! Reference genome name (%s) is not contained in any genome name!", options.getReferenceGenomeName()));

                Genome first = genomes[0];
                genomes[0] = genomes[index];
                genomes[index] = first;
            }

            Parameter parameter;
            if (options.getMaxDistance() >= 0)
                parameter = new Parameter(options.getMaxDistance(), options.getMinClusterSize(), options.getMinCoveredGenomes(), options.getOperationMode(), refType);
            else
                parameter = new Parameter(options.getDistanceTable(), options.getMinClusterSize(), options.getMinCoveredGenomes(), options.getOperationMode(), refType);


            // compute the clusters
            SwingWorker<List<GeneCluster>, Void> worker = GeckoInstance.getInstance().performClusterDetection(parameter, false, options.getGenomeGroupingFactor());
            try{
                List<GeneCluster> results = worker.get(); // Blocks until worker is done()
                GeckoInstance.getInstance().setClusters(results, parameter);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error in cluster detection!", e);
            }
        }

        File outfile = options.getOutfile();
        if (outfile != null) {
            // Remove outfile
            if (outfile.exists())
                outfile.delete();

            // Save session
            DataSetWriter.saveDataSetToFile(GeckoInstance.getInstance().getData(), options.getOutfile());
        }
        List<OutputOption> outputOptions = options.getOutputOptions();
        for (OutputOption outputOption : outputOptions) {
            outfile = outputOption.getFile();
            // Remove outfile
            if (outfile.exists())
                outfile.delete();
            ResultWriter.exportResultsToFile(outfile, outputOption.getType(), outputOption.getFilter());
        }
    }
}
