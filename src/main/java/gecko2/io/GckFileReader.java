package gecko2.io;

import gecko2.GeckoInstance;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;

import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * The class implements a reader for .gck files (session files).
 * The code is exported from GeckoInstance.java and modified.
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.03
 */
public class GckFileReader implements GeckoDataReader {
	/**
	 * Storing place for the colorMap
	 */
	private Map<Integer, Color> colorMap;
	
	/**
	 * Storing place for the geneLabelMap 
	 */
	private Map<Integer, String[]> geneLabelMap;
	
	/**
	 * Storing place for the genomes.
	 */
	private Genome[] genomes;
	
	/**
	 * Storing place for the gene clusters.
	 */
	private GeneCluster[] clusters;
	
	/**
	 * Storing place for the length of the longest id.
	 */
	private int maxIdLength;

    /**
     * Storing place for the length of the longest name.
     */
    private int maxNameLength;

    /**
     * Storing place for the length of the longest locus tag.
     */
    private int maxLocusTagLength;

    /**
     * The input file
     */
    private final File inputFile;

    public GckFileReader(File gckInputFile) {
        inputFile = gckInputFile;
    }

	/**
	 * The method is a getter for the geneLabelMap which contains the relation between external ID
	 * and internal ID from the gene names
	 * 
	 * @return the geneLabelMap (HashMap)
	 */
	public Map<Integer, String[]> getGeneLabelMap() {
		return this.geneLabelMap;
	}
	
	
	/**
	 * Getter for the colorMap.
	 * 
	 * @return the colorMap of the input file
	 */
	public Map<Integer, Color> getColorMap() {
		return colorMap;
	}

	/**
	 * @return the genomes from the input file.
	 */
	public Genome[] getGenomes() {
		return genomes;
	}

	/**
	 * @return the maxIdLength from the input file
	 */
	public int getMaxIdLength() {
		return maxIdLength;
	}

    /**
     * @return the maxNameLength from the input file
     */
    @Override
    public int getMaxNameLength() {
        return maxNameLength;
    }

    /**
     * @return the maxLocusTagLength from the input file
     */
    @Override
    public int getMaxLocusTagLength() {
        return maxLocusTagLength;
    }

    /**
     * @return the gene clusters
     */
    public GeneCluster[] getGeneClusters(){
        return clusters;
    }

    /**
     * Reads all data from the file
     */
    public void readData() throws IOException, ParseException {
        GeckoInstance.getInstance().setLastOpendFile(inputFile);

        ObjectInputStream o = null;

        try	{
            o = new ObjectInputStream(new FileInputStream(inputFile));
            genomes = (Genome[]) o.readObject();
            geneLabelMap = (HashMap<Integer, String[]>) o.readObject();
            colorMap = (HashMap<Integer, Color>) o.readObject();
            clusters = (GeneCluster[]) o.readObject();
            maxIdLength = (Integer) o.readObject();
            try {
                maxNameLength = (Integer) o.readObject();
                maxLocusTagLength = (Integer) o.readObject();
            } catch (EOFException e) {  // old format did not serialize this.
                maxNameLength = Genome.getMaxNameLength(genomes);
                maxLocusTagLength = Genome.getMaxLocusTagLength(genomes);
            }

            if (clusters != null) {
                for (GeneCluster c : clusters)
                    c.setMatch(true);
            }
        }
        catch (IOException e) {
            handleFailedSessionLoad();
            throw e;
        }
        catch (ClassNotFoundException e) {
            handleFailedSessionLoad();
            throw new ParseException(e.getMessage(), 0);
        }
        catch (ClassCastException e) {
            handleFailedSessionLoad();
            throw new ParseException(e.getMessage(), 0);
        }
        finally {
            if (o!=null) {
                try {
                    o.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
		
	/**
	 * Method for handling errors while the file is read.
	 */
	private void handleFailedSessionLoad() {
        genomes = null;
        geneLabelMap = null;
        colorMap = null;
        clusters = null;
        maxIdLength = 0;
	}
}
