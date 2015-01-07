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

package de.unijena.bioinf.gecko3;

import de.unijena.bioinf.gecko3.datastructures.DataSet;
import de.unijena.bioinf.gecko3.commandLine.CommandLineExecution;
import de.unijena.bioinf.gecko3.commandLine.CommandLineOptions;
import de.unijena.bioinf.gecko3.exceptions.DefaultUncaughtExceptionHandler;
import de.unijena.bioinf.gecko3.gui.Gui;
import de.unijena.bioinf.gecko3.io.CogFileReader;
import de.unijena.bioinf.gecko3.io.GckFileReader;
import de.unijena.bioinf.gecko3.io.GeckoDataReader;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;

public class Gecko3 {
    private static final Logger logger = LoggerFactory.getLogger(Gecko3.class);

    public static void main (String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
        String lcOSName = System.getProperty("os.name").toLowerCase();
        logger.info("You are running {}-Java on {}", System.getProperty("os.arch"), System.getProperty("os.name"));

        /*
            * Check for command line parameters
            */
        CommandLineOptions options = new CommandLineOptions();
        CmdLineParser parser = new CmdLineParser(options);

        if (args.length > 1) {
            try {
                parser.parseArgument(args);
                options.validate(parser);
            } catch (CmdLineException e) {
                printUsage(System.err, parser, e);
                return;
            }
        }

        if ((args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) || options.showHelp()) {
            printUsage(System.out, parser);
            return;
        }

            /*
            * Only for Mac
            */
        boolean IS_MAC = lcOSName.startsWith("mac os x");

        if (IS_MAC) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Gecko\u00B2");
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e) {
            logger.warn("Unable to set look and feel", e);
        }

        boolean libgeckoLoaded = false;

        GeckoInstance instance = GeckoInstance.getInstance();
        instance.setGeckoInstanceData(DataSet.getEmptyDataSet());
        instance.setLibgeckoLoaded(libgeckoLoaded);

        /*
         * Load input genomes from .gck or .cog
         */
        File infile = options.getInfile();
        if (args.length == 1) {
            infile = new File(args[0]);
        }

        if (args.length <= 1 || options.useGui()) {
            try {
                Gui.startUp();
            } catch (java.awt.HeadlessException e) {
                printUsage(System.out, parser, "You are running headless java, but trying to start the gui!");
                return;
            }
        }

        if (infile != null) {
            String extension = infile.getPath().substring(infile.getPath().lastIndexOf(".") + 1);
            GeckoDataReader reader = null;

            if (extension.equals("gck")) {
                reader = new GckFileReader(infile);
            } else if (extension.equals("cog")) {
                reader = new CogFileReader(infile, options.getGenomeList());
            } else {
                printUsage(System.err, parser, new CmdLineException(parser, "Input file is not of type .gck or .cog!"));
                return;
            }
            try {
                DataSet data = reader.readData();
                instance.setCurrentWorkingDirectoryOrFile(infile);
                instance.setGeckoInstanceData(data);
            } catch (IOException | ParseException e) {
                logger.error("Unable to load file {}!", infile, e);
            }
        }

        if (args.length > 1) {
            CommandLineExecution.runAlgorithm(options);
        }
    }

    private static void printUsage(PrintStream out, CmdLineParser parser) {
        printUsage(out, parser, "");
    }

    private static void printUsage(PrintStream out, CmdLineParser parser, Exception exception) {
        printUsage(out, parser, exception.getMessage());
    }

    private static void printUsage(PrintStream out, CmdLineParser parser, String errorText) {
        if (errorText!= null && !errorText.equals(""))
            out.println(errorText);
        out.println("java -jar Gecko3.jar [optional Input.gck/.cog] for Gui mode or");
        out.println("java -jar Gecko3.jar [options...]");
        parser.printUsage(out);
    }
}
