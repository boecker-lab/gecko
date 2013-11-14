package gecko2.io;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import gecko2.GeckoInstance;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;
import gecko2.gui.GenomePainting;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * The class implements a writer which write a gecko session to a file.
 * The code of this file is exported from GeckoInstance.java and modified.
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.03
 */
public class SessionWriter 
{
	/**
	 * Saves the current gecko session to a given file
	 * @param f The file to write to
	 */
	public static boolean saveSessionToFile(File f) 
	{
		GeckoInstance.getInstance().setLastSavedFile(f);
        ObjectOutputStream o = null;
        boolean returnValue = true;


        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());

        try (BufferedWriter out = Files.newBufferedWriter(f.toPath(), Charset.forName("UTF-8"))) {
        //try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f)); ObjectOutputStream out = xstream.createObjectOutputStream(zos)) {
        //    zos.putNextEntry(new ZipEntry(f.getName()));
            writeGenomes(out, GeckoInstance.getInstance().getGenomes());
            writeLengthInformation(out, GenomePainting.NameType.values());
            writeGeneLabelMap(out, GeckoInstance.getInstance().getGenLabelMap());
            writeColorMap(out, GeckoInstance.getInstance().getColormap());
            writeClusters(out, GeckoInstance.getInstance().getClusters());

        }
        catch (IOException e) {
            e.printStackTrace();
            returnValue = false;
        }
        return returnValue;
	}

    private static void writeClusters(Writer out, GeneCluster[] clusters) {


    }

    private static void writeColorMap(Writer out, Map<Integer, Color> colormap) {


    }

    private static void writeGeneLabelMap(Writer out, Map<Integer, String[]> geneLabelMap) {


    }

    private static void writeLengthInformation(Writer out, GenomePainting.NameType[] values) {


    }

    private static void writeGenomes(Writer out, Genome[] genomes) {

    }


}
