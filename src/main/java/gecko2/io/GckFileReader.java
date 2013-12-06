package gecko2.io;

import gecko2.GeckoInstance;
import gecko2.algorithm.*;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;
import java.util.List;

/**
 * The class implements a reader for .gck files (session files).
 * The code is exported from GeckoInstance.java and modified.
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.03
 */
public class GckFileReader implements GeckoDataReader {
	/**
	 * Storing place for the geneLabelMap 
	 */
	private Map<Integer, ExternalGeneId> geneLabelMap;
	
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
	public Map<Integer, ExternalGeneId> getGeneLabelMap() {
		return this.geneLabelMap;
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
        try	(BufferedReader reader = Files.newBufferedReader(inputFile.toPath(), Charset.forName("UTF-8"))) {
            String line = reader.readLine().trim();
            if (!line.equals(SessionWriter.GENOME_SECTION_START))
                throw new ParseException("Malformed first line: " + line, 0);
            readGenomeData(reader);
            line = reader.readLine().trim();
            if (!line.startsWith(SessionWriter.CLUSTER_SECTION_START))
                throw new ParseException("Malformed cluster section start: " + line, 0);
            readClusterData(reader);
        }catch (IOException | ParseException e) {
            handleFailedSessionLoad();
            throw e;
        }
    }

    private void readGenomeData(BufferedReader reader) throws IOException, ParseException {
        List<Genome> genomeList = new ArrayList<>();
        geneLabelMap = new HashMap<>();
        Genome genome = null;
        while(true) {
            String line = reader.readLine().trim();
            switch (line) {
                case SessionWriter.GENOME_START:
                    genome = new Genome(reader.readLine().trim());
                    genomeList.add(genome);
                    break;
                case SessionWriter.GENOME_END:
                    genome = null;
                    break;
                case SessionWriter.CHROMOSOME_START:
                    if (genome == null)
                        throw new ParseException("Not in Genome at chromosome start!" , 0);
                    genome.addChromosome(readChromosome(reader, genome));
                    break;
                case SessionWriter.GENOME_SECTION_END:
                    if (genome != null)
                        throw new ParseException("Genome or chromosome not closed at genomes end.", 0);
                    genomes = genomeList.toArray(new Genome[genomeList.size()]);
                    return;
                default:
                    throw new ParseException("Maleformed line: " + line, 0);
            }
        }
    }

    private Chromosome readChromosome(BufferedReader reader, Genome genome) throws IOException, ParseException {
        Chromosome chr = new Chromosome(reader.readLine().trim(), genome);
        chr.setGenes(new ArrayList<Gene>());

        while (true) {
            String line = reader.readLine().trim();
            if (line.equals(SessionWriter.CHROMOSOME_END))
                return chr;
            else {
                String[] split = line.split("\t");
                Gene newGene = new Gene(split[4], split[2], Integer.parseInt(split[0]), split[3]);
                maxIdLength = Math.max(maxIdLength, (split[0].startsWith("-") ? split[0].length()-1 : split[0].length()));
                maxLocusTagLength = Math.max(maxLocusTagLength, newGene.getTag().length());
                maxNameLength = Math.max(maxNameLength, newGene.getName().length());
                ExternalGeneId label = geneLabelMap.get(newGene.getId());
                String newLabel = split[1];
                if (label == null)
                    geneLabelMap.put(newGene.getId(), new ExternalGeneId(newLabel, Integer.parseInt(split[5])));
                else if (label.equals(newLabel))
                    throw new ParseException(String.format("Conflicting gene labels %s and %s!", newLabel, label), 0);
                chr.getGenes().add(newGene);
            }
        }
    }

    private void readClusterData(BufferedReader reader) throws IOException, ParseException {

    }

    /**
	 * Method for handling errors while the file is read.
	 */
	private void handleFailedSessionLoad() {
        genomes = null;
        geneLabelMap = null;
        clusters = null;
        maxIdLength = 0;
        maxLocusTagLength = 0;
        maxNameLength = 0;
	}
}
