package gecko2;

import gecko2.gui.Gui;
import gecko2.io.CogFileReader;
import gecko2.io.GckFileReader;
import gecko2.io.GeckoDataReader;
import gecko2.util.LibraryUtils;
import gecko2.util.LibraryUtils.PlatformNotSupportedException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;

public class Gecko2 {

	public static void main (String[] args) {
        String lcOSName = System.getProperty("os.name").toLowerCase();
        System.err.println("You are running "+
                System.getProperty("os.arch")+
                "-Java on "+System.getProperty("os.name"));

        /*
         * Check for command line parameters
         */
        CommandLineOptions options = new CommandLineOptions();
        CmdLineParser parser = new CmdLineParser(options);

        if (args.length > 1) {
            try {
                parser.parseArgument(args);
            } catch (CmdLineException e) {
                printUsage(System.err, parser, e);
                return;
            }
        }

        if ((args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) || options.showHelp()) {
            printUsage(System.out, parser, null);
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
        } catch (Throwable t) {}

        /*
         * Try to load libgecko
         */
		boolean libgeckoLoaded = false;

        try {
            LibraryUtils.loadLibrary("libgecko2");
            libgeckoLoaded = true;
        } catch (PlatformNotSupportedException e) {
            e.printStackTrace();
            System.err.println("Not able to load c library! Using Java only version");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Not able to load c library! Using Java only version");
        }

        GeckoInstance instance = GeckoInstance.getInstance();
        instance.setLibgeckoLoaded(libgeckoLoaded);

        /*
         * Load input genomes from .gck or .cog
         */
        File infile = options.getInfile();
        if (args.length == 1) {
            infile = new File(args[0]);
        }

        GeckoDataReader reader = null;
        if (infile != null) {
            String extension = infile.getPath().substring(infile.getPath().lastIndexOf(".") + 1);

            if (extension.equals("gck")) {
                reader = new GckFileReader(infile);
            } else if (extension.equals("cog")) {
                reader = new CogFileReader(infile, options.getGenomeList());
            } else {
                printUsage(System.err, parser, new CmdLineException(parser, "Input file is not of type .gck or .cog!"));
                return;
            }
            try {
                reader.readData();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (args.length <= 1 || options.useGui()) {
            Gui.startUp();
        }

        if (reader != null)
            GeckoInstance.getInstance().setGeckoInstanceFromReader(reader);


        if (args.length > 1) {
            CommandLineExecution.runAlgorithm(options, reader.getGeneLabelMap().size());
        }

	}

    private static void printUsage(PrintStream out, CmdLineParser parser, Exception e) {
        if (e!= null)
            out.println(e.getMessage());
        out.println("java -jar Gecko2.jar [optional Input.gck/.cog] for Gui mode or");
        out.println("java -jar Gecko2.jar [options...]");
        parser.printUsage(out);
    }
}
