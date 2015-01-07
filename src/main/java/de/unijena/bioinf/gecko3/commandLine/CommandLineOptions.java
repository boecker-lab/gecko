/*
 * Copyright 2014 Sascha Winter
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
import de.unijena.bioinf.gecko3.datastructures.Parameter;
import de.unijena.bioinf.gecko3.io.ExportType;
import de.unijena.bioinf.gecko3.io.ResultWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static org.kohsuke.args4j.spi.Messages.*;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class CommandLineOptions {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineOptions.class);

    /*
     * Algorithm parameters
     */
    @Option(name = "-nC", aliases = "--noComputation", usage = "Don't compute clusters, only read data an write output.")
    private boolean noComputation = false;

    @Option(name="-d", aliases = "--distance", usage = "The maximum allowed distance. Not compatible with \"-dT\".")
    private int maxDistance = -1;

    @Option(name="-dT", aliases = "--distanceTable", usage = "A string of arrays of maximum allowed distances.\n" +
            "Each array has to have 4 elements, the maximum number of additions, the maximum number of losses," +
            "the maximum sum of deletions and losses and the minimum size the parameters apply to.\n" +
            "e.g. \"[1, 0, 1, 3],[2, 1, 2, 4]\" allows 1 loss for size 3 and 2 losses and/or 1 deletions for size 4.\n" +
            "Has to be a single string, so either contained in \"\" or not containing any blanks. \n" +
            "Not compatible with \"-d\" and \"-s\".",
            handler = DistanceTableOptionHandler.class)
    private int[][] distanceTable;

    @Option(name="-s", aliases = "--size", usage = "The minimum cluster size. Not compatible with \"-dT\".")
    private int minClusterSize = -1;

    @Option(name="-q", aliases = "--quorum", usage = "The minimum number of covered genomes.")
    private int minCoveredGenomes = 0;

    @Option(name="-gGF", aliases = "--genomeGroupingFactor", usage = "All genomes with lower breakpoint distance are treated as one group.")
    private double genomeGroupingFactor = 1.1;

    //@Option(name="-o", aliases = "--operationMode", usage = "The operation mode, [reference] cluster (default), [median] gene cluster, or [center] gene cluster.")
    private Parameter.OperationMode operationMode = Parameter.OperationMode.reference;

    @Option(name="-r", aliases = "--referenceGenomeName", usage = "Name of the reference genome.\n" +
            "If not set all genomes are used as reference.\n" +
            "The name has to be uniquely contained at the beginning of a single genome.")
    private String referenceGenomeName = "";

    /*
     * Files
     */
    @Option(name="-in", aliases = "--Infile", required = true, usage = "The .gck or .cog input file.")
    private File infile = null;


    @Option(name="-gL", aliases = "--genomeList", handler=GenomeListOptionHandler.class, usage = "The indices of the genomes that shall be imported from the .cog file.\n" +
            "A String containing a comma separated list of integers (\"1, 3, 5, 8\").\n" +
            "Has to be a single string, so either contained in \"\" or not containing any blanks.")
    private List<Integer> genomeList = null;

    @Option(name="-out", aliases = "--Outfile", usage = "The output .gck file for later use with the gui.")
    private File outfile = null;

    @Option(name="-rO", aliases = "--resultOutput", usage = "Write the filtered clusters to a File in different formats.\n" +
            "ExportType must be one of: " + ExportType.types + "\n" +
            "ResultFilter must be one of: " + GeckoInstance.ResultFilter.types, handler = OutputOptionHandler.class)
    private List<OutputOption> outputOptions = new ArrayList<>();

    /*
     * Others
     */
    @Option(name="-gui", usage = "Start the gui.", help=true)
    private boolean gui = false;

    @Option(name="-h", aliases = "--help", usage = "Show this help and exit.", help= true)
    private boolean help = false;

    /*
     * Getters
     */
    public int getMaxDistance() {
        return maxDistance;
    }

    public int[][] getDistanceTable() {
        return distanceTable;
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

    public List<OutputOption> getOutputOptions() {
        return outputOptions;
    }

    public boolean useGui() {
        return gui;
    }

    public boolean noComputation() {
        return noComputation;
    }

    public boolean showHelp() {
        return help;
    }

    /**
     * Validates all the parameters
     * @return
     */
    public void validate(CmdLineParser parser) throws CmdLineException {
        if (gui && distanceTable == null && maxDistance < 0 && minClusterSize < 0){
            noComputation = true;
        }
        if (!noComputation){
            if ((distanceTable == null || distanceTable.length == 0) && (maxDistance < 0))
                throw new CmdLineException(parser, "Not running gui or no computation and missing either \"-d\" or \"-dT\" or distance < 0.");
            if ((distanceTable != null && distanceTable.length > 0) && (maxDistance >= 0))
                throw new CmdLineException(parser, "Not running gui or no computation and both \"-d\" and \"-dT\" set.");
            if (minClusterSize >= 0 && distanceTable != null)
                throw new CmdLineException(parser, "Using distance table und minimum cluster size set.");
            if (minClusterSize < 0 && maxDistance >= 0)
                throw new CmdLineException(parser, "Using single distance value and minimum cluster size < 0 or not set.");
        }
    }

    public static class DistanceTableOptionHandler extends OptionHandler<int[][]> {
        public DistanceTableOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super int[][]> setter) {
            super(parser, option, setter);
            if (setter.asFieldSetter()==null)
                throw new IllegalArgumentException("MapOptionHandler can only work with fields");
        }

        @Override
        public int parseArguments(Parameters params) throws CmdLineException {
            String full = params.getParameter(0).trim();
            String[] delimitedStrings = full.split("\\]");
            Map<Integer, int[]> mapping = new HashMap<>();
            int maxSize = 0;
            for (String delimitedString : delimitedStrings) {
                String cleanedString = delimitedString.substring(delimitedString.lastIndexOf("[") + 1);
                String[] singleValues = cleanedString.split(",");

                if (singleValues.length != 4) {
                    CmdLineException e = new CmdLineException(owner, ILLEGAL_OPERAND, params.getParameter(-1), full);
                    logger.warn("Malformed parameters at {}", singleValues, e);
                    throw e;
                } try {
                    int size = Integer.parseInt(singleValues[3].trim());
                    if (size < 0 || mapping.containsKey(size)){
                        CmdLineException e = new CmdLineException(owner, ILLEGAL_OPERAND, params.getParameter(-1), full);
                        logger.warn("Size < 0 or duplicate size {}", size, e);
                        throw e;
                    }
                    if (size > maxSize)
                        maxSize = size;
                    int[] d = new int[3];
                    d[0] = Integer.parseInt(singleValues[0].trim());
                    d[1] = Integer.parseInt(singleValues[1].trim());
                    d[2] = Integer.parseInt(singleValues[2].trim());
                    mapping.put(size, d);
                } catch (NumberFormatException e){
                    CmdLineException ex = new CmdLineException(owner, String.format("%s is ont a valid value for %s", full, params.getParameter(-1)), e);
                    logger.warn("Not a number in {}", singleValues, ex);
                    throw ex;
                }
            }
            int[][] table = new int[maxSize+1][];
            int[] lastValues = new int[]{-1, -1, -1};
            for (int i=0; i<=maxSize; i++){
                int[] values = mapping.get(i);
                if (values == null)
                    table[i] = Arrays.copyOf(lastValues, lastValues.length);
                else {
                    table[i] = values;
                    lastValues = values;
                }
            }
            setter.asFieldSetter().addValue(table);
            return 1;
        }

        @Override
        public String getDefaultMetaVariable() {
            return "<[N,N,N,N],[N,N,N,N],...>";
        }
    }


    public static class GenomeListOptionHandler extends DelimitedOptionHandler<Integer> {
        public GenomeListOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Integer> setter) {
            super(parser, option, setter, ",", new IntegerTrimmingOptionHandler(parser, option, setter));
        }
    }

    public static class IntegerTrimmingOptionHandler extends OneArgumentOptionHandler<Integer> {
        public IntegerTrimmingOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Integer> setter) {
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


    /**
     * Option handler for reading output options, similar to @link{StringArrayOptionHandler}
     */
    public static class OutputOptionHandler extends OptionHandler<OutputOption> {
        public OutputOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super OutputOption> setter) {
            super(parser, option, setter);
        }

        @Override
        public int parseArguments(Parameters params) throws CmdLineException {
            int counter=0;
            File file = null;
            ExportType exportType = null;
            GeckoInstance.ResultFilter filterType = null;

            for (; counter<params.size(); counter++) {
                String param = params.getParameter(counter);

                if(param.startsWith("-")) {
                    break;
                }
                try {
                    switch (counter) {
                        case 0:
                            exportType = ExportType.valueOf(param.trim());
                            break;
                        case 1:
                            filterType = GeckoInstance.ResultFilter.valueOf(param.trim());
                            break;
                        case 2:
                            file = new File(param);
                            break;
                        default:
                            break;
                    }
                } catch (IllegalArgumentException e){
                    StringBuilder builder = new StringBuilder();
                    for (int i=0; i<=counter; i++)
                        builder.append(" ").append(params.getParameter(i));
                    CmdLineException ex = new CmdLineException(owner,  String.format("%s is not a valid value for %s", params.getParameter(counter), counter==0 ? "ExportType" : "ResultFilter"), e);
                    logger.warn("Malformed parameters for {}{} at {}", params.getParameter(-1), builder.toString(), params.getParameter(counter), ex);
                    throw ex;
                }
            }
            if (counter != 3){
                StringBuilder builder = new StringBuilder();
                for (int i=0; i<counter; i++)
                    builder.append(" ").append(params.getParameter(i));
                CmdLineException e = new CmdLineException(owner, ILLEGAL_OPERAND, params.getParameter(-1), builder.toString());
                logger.warn("Malformed parameters for {}, more or less than 3 at \"{}\"", params.getParameter(-1), builder.toString(), e);
                throw e;
            }
            setter.addValue(new OutputOption(file, exportType, filterType));

            return counter;
        }

        @Override
        public String getDefaultMetaVariable() {
            return "<ExportType ResultFilter FILE>";
        }
    }
}
