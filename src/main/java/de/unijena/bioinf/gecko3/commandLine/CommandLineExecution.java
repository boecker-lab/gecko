/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
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

package de.unijena.bioinf.gecko3.commandLine;

import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.GeneCluster;
import de.unijena.bioinf.gecko3.datastructures.Genome;
import de.unijena.bioinf.gecko3.datastructures.Parameter;
import de.unijena.bioinf.gecko3.io.DataSetWriter;
import de.unijena.bioinf.gecko3.io.ResultWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
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

                GeckoInstance.getInstance().reorderGenomes(index);
            }

            Parameter parameter;
            if (options.getMaxDistance() >= 0)
                parameter = new Parameter(options.getMaxDistance(), options.getMinClusterSize(), options.getMinCoveredGenomes(), options.getOperationMode(), refType, options.refInRef(), options.noStatistics());
            else {
                int[][] distanceValues = options.getDistanceTable();
                int[][] distanceTable = new int[distanceValues.length][];
                int[] lastValues = new int[]{0, 0, 0};
                int minSize = Integer.MAX_VALUE;
                for (int i=0; i<distanceTable.length; i++){
                    if (distanceValues[i][0] < 0)
                        distanceTable[i] = Arrays.copyOf(lastValues, lastValues.length);
                    else {
                        distanceTable[i] = distanceValues[i];
                        lastValues = distanceTable[i];
                        if (i < minSize)
                            minSize = i;
                    }
                }
                parameter = new Parameter(distanceTable, minSize, options.getMinCoveredGenomes(), options.getOperationMode(), refType, options.refInRef(), options.noStatistics());
            }

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
