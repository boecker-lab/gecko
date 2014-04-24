package gecko2.io;

import gecko2.algorithm.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;

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
	private Set<GeneFamily> geneFamilySet;
    private GeneFamily unknownGeneFamily;
    private int numberOfGeneFamiliesWithMultipleGenes;
	
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
     * Reads all data from the file
     */
    @Override
    public DataSet readData() throws IOException, ParseException {
        try	(BufferedReader reader = Files.newBufferedReader(inputFile.toPath(), Charset.forName("UTF-8"))) {
            Map<String, GeneFamily> geneFamilyMap = new HashMap<>();
            unknownGeneFamily = GeneFamily.getNewUnknownGeneFamilyAndInitializeAlgorithmId();

            String line = reader.readLine().trim();
            if (!line.equals(DataSetWriter.GENOME_SECTION_START))
                throw new ParseException("Malformed first line: " + line, 0);
            readGenomeData(reader, geneFamilyMap);
            line = reader.readLine().trim();
            if (!line.startsWith(DataSetWriter.CLUSTER_SECTION_START))
                throw new ParseException("Malformed cluster section start: " + line, 0);
            readClusterData(reader, geneFamilyMap, unknownGeneFamily);
        } catch (IOException | ParseException e) {
            handleFailedSessionLoad();
            throw e;
        }
        return new DataSet(
                genomes,
                clusters,
                maxIdLength,
                maxNameLength,
                maxLocusTagLength,
                geneFamilySet,
                unknownGeneFamily,
                numberOfGeneFamiliesWithMultipleGenes
        );
    }

    private void readGenomeData(BufferedReader reader, Map<String, GeneFamily> geneFamilyMap) throws IOException, ParseException {
        List<Genome> genomeList = new ArrayList<>();
        Genome genome = null;
        boolean continueReading = true;
        while(continueReading) {
            String line = reader.readLine().trim();
            switch (line) {
                case DataSetWriter.GENOME_START:
                    if (genome != null)
                        throw new ParseException("Genome or chromosome not closed before new genome start.", 0);
                    genome = new Genome(reader.readLine().trim());
                    genomeList.add(genome);
                    break;
                case DataSetWriter.GENOME_END:
                    genome = null;
                    break;
                case DataSetWriter.CHROMOSOME_START:
                    if (genome == null)
                        throw new ParseException("Not in Genome at chromosome start!" , 0);
                    genome.addChromosome(readChromosome(reader, genome, geneFamilyMap));
                    break;
                case DataSetWriter.GENOME_SECTION_END:
                    if (genome != null)
                        throw new ParseException("Genome or chromosome not closed at genomes end.", 0);
                    continueReading = false;
                    break;
                default:
                    throw new ParseException("Maleformed line: " + line, 0);
            }
        }
        genomes = genomeList.toArray(new Genome[genomeList.size()]);
        geneFamilySet = new HashSet<>(geneFamilyMap.values());
        numberOfGeneFamiliesWithMultipleGenes = GeneFamily.getNumberOfGeneFamiliesWithMultipleGenes();
    }

    private Chromosome readChromosome(BufferedReader reader, Genome genome, Map<String, GeneFamily> geneFamilyMap) throws IOException, ParseException {
        Chromosome chr = new Chromosome(reader.readLine().trim(), genome);
        chr.setGenes(new ArrayList<Gene>());

        boolean continueReading = true;
        while(continueReading)  {
            String line = reader.readLine().trim();
            if (line.equals(DataSetWriter.CHROMOSOME_END))
                continueReading = false;
            else {
                String[] split = line.split(DataSetWriter.SEPERATOR);

                GeneFamily geneFamily;
                if (split[1].equals(GeneFamily.UNKNOWN_GENE_ID)) {
                    geneFamily = this.unknownGeneFamily;
                    geneFamily.addGene();
                } else {
                    if (!geneFamilyMap.containsKey(split[1])) {
                        geneFamily = new GeneFamily(split[1]);
                        geneFamilyMap.put(split[1], geneFamily);
                    } else {
                        geneFamily = geneFamilyMap.get(split[1]);
                        geneFamily.addGene();
                    }
                }

                Gene.GeneOrientation orientation;
                switch (split[0]) {
                    case "+":
                        orientation = Gene.GeneOrientation.POSITIVE;
                        break;
                    case "-":
                        orientation = Gene.GeneOrientation.NEGATIVE;
                        break;
                    default:
                        orientation = Gene.GeneOrientation.UNSIGNED;
                }

                Gene newGene = new Gene(split[4], split[2], geneFamily, orientation, split[3]);
                maxIdLength = Math.max(maxIdLength, (split[1].length()));
                maxLocusTagLength = Math.max(maxLocusTagLength, newGene.getTag().length());
                maxNameLength = Math.max(maxNameLength, newGene.getName().length());

                chr.getGenes().add(newGene);
            }
        }
        return chr;
    }

    private void readClusterData(BufferedReader reader, Map<String, GeneFamily> geneFamilyMap, GeneFamily unknownGeneFamily) throws IOException, ParseException {
        List<GeneCluster> clusterList = new ArrayList<>();
        GeneClusterBuilder builder = null;
        boolean continueReading = true;
        while(continueReading) {
            String line = reader.readLine().trim();
            switch (line) {
                case DataSetWriter.CLUSTER_START:
                    if (builder != null)
                        throw new ParseException("Cluster not closed before new cluster start.", 0);
                    builder = new GeneClusterBuilder(reader, geneFamilyMap, unknownGeneFamily);
                    builder.readGenes(reader);
                    break;
                case DataSetWriter.OCC_START:
                    if (builder == null)
                        throw new ParseException("Not in cluster at occ start!", 0);
                    builder.addOcc(readOcc(reader));
                    break;
                case DataSetWriter.CLUSTER_END:
                    if (builder == null)
                        throw new ParseException("Not in cluster at cluster end!", 0);
                    clusterList.add(builder.build());
                    builder = null;
                    break;
                case DataSetWriter.CLUSTER_SECTION_END:
                    if (builder != null)
                        throw new ParseException("Cluster not closed before end of cluster section.", 0);
                    continueReading = false;
                    break;
                default:
                    throw new ParseException("Maleformed line: " + line, 0);
            }
        }
        clusters = clusterList.toArray(new GeneCluster[clusterList.size()]);
    }

    private static class GeneClusterBuilder {
        final int id;
        final int refSeqIndex;
        final Parameter.OperationMode mode;
        final int minTotalDistance;
        final BigDecimal pValue;
        final BigDecimal pValueCorr;
        Set<GeneFamily> genes;
        List<GeneClusterOccurrence> bestOccList;
        List<GeneClusterOccurrence> allOccList;
        private final Map<String, GeneFamily> geneFamilyMap;
        private final GeneFamily unknownGeneFamily;

        GeneClusterBuilder(BufferedReader reader, Map<String, GeneFamily> geneFamilyMap, GeneFamily unknownGeneFamily) throws IOException, ParseException {
            String line = reader.readLine().trim();
            String[] clusterInfo = line.split(DataSetWriter.SEPERATOR);

            if (clusterInfo.length != 6)
                throw new ParseException("Maleformed line: " + line, 0);

            id = Integer.parseInt(clusterInfo[0]);
            refSeqIndex = Integer.parseInt(clusterInfo[1]);
            mode = Parameter.OperationMode.getOperationModeFromChar(clusterInfo[2].charAt(0));
            minTotalDistance = Integer.parseInt(clusterInfo[3]);
            pValue = new BigDecimal(clusterInfo[4]);
            pValueCorr = new BigDecimal(clusterInfo[5]);
            this.geneFamilyMap = geneFamilyMap;
            this.unknownGeneFamily = unknownGeneFamily;
        }

        void readGenes(BufferedReader reader) throws IOException, ParseException {
            String line = reader.readLine().trim();
            String[] genes = line.substring(1, line.length()-1).split(",");
            this.genes = new HashSet<>();
            for (String gene : genes) {
                GeneFamily geneFamily;
                if (gene.trim().equals(GeneFamily.UNKNOWN_GENE_ID)) {
                    geneFamily = unknownGeneFamily;
                } else {
                    geneFamily = geneFamilyMap.get(gene.trim());
                    if (geneFamily == null) {
                        throw new ParseException("No gene family found for key: " + gene.trim(), 0);
                    }
                }
                this.genes.add(geneFamily);
            }
        }

        GeneCluster build() throws ParseException{
            if (genes == null)
                throw new ParseException("Missing genes when trying to complete cluster!", 0);
            if (allOccList == null || bestOccList == null)
                throw new ParseException("Missing occs when trying to complete cluster!", 0);
            return new GeneCluster(id,
                    bestOccList.toArray(new GeneClusterOccurrence[bestOccList.size()]),
                    allOccList.toArray(new GeneClusterOccurrence[allOccList.size()]),
                    genes,
                    pValue,
                    pValueCorr,
                    minTotalDistance,
                    refSeqIndex,
                    mode);
        }

        void addOcc(GeneClusterOccurrence occ) {
            if (allOccList == null) {
                allOccList = new ArrayList<>();
                bestOccList = new ArrayList<>();
            }
            allOccList.add(occ);
            bestOccList.add(occ.getBestOccurrence());
        }
    }

    private GeneClusterOccurrence readOcc(BufferedReader reader) throws IOException, ParseException {
        boolean continueReading = true;
        int id;
        BigDecimal pValue;
        int totalDist;
        int support;

        String line = reader.readLine().trim();
        String[] occInfo = line.split(DataSetWriter.SEPERATOR);
        if (occInfo.length != 4)
            throw new ParseException("Maleformed line: " + line, 0);

        id = Integer.parseInt(occInfo[0]);
        pValue = new BigDecimal(occInfo[1]);
        support = Integer.parseInt(occInfo[2]);
        totalDist = Integer.parseInt(occInfo[3]);

        int[] numberOfSubseq = new int[genomes.length];

        List<Subsequence> subsequenceList = new ArrayList<>();

        while(continueReading) {
            line = reader.readLine().trim();
            if (line.equals(DataSetWriter.OCC_END))
                continueReading = false;
            else {
                String[] sline = line.split(DataSetWriter.SEPERATOR);
                if (sline.length != 6)
                    throw new ParseException("Maleformed line: " + line, 0);
                subsequenceList.add(new Subsequence(Integer.parseInt(sline[3]), Integer.parseInt(sline[4]), Integer.parseInt(sline[1]), Integer.parseInt(sline[2]), new BigDecimal(sline[5])));
                numberOfSubseq[Integer.parseInt(sline[0])]++;
            }
        }

        Subsequence[][] subsequences = new Subsequence[genomes.length][];

        int listIndex = 0;
        for (int i=0; i<subsequences.length; i++) {
            subsequences[i] = new Subsequence[numberOfSubseq[i]];
            for (int j=0; j<subsequences[i].length; j++) {
                subsequences[i][j] = subsequenceList.get(listIndex);
                listIndex++;
            }
        }
        return new GeneClusterOccurrence(id, subsequences, pValue, totalDist, support);
    }

    /**
	 * Method for handling errors while the file is read.
	 */
	private void handleFailedSessionLoad() {
        genomes = null;
        unknownGeneFamily = null;
        geneFamilySet = null;
        numberOfGeneFamiliesWithMultipleGenes = 0;
        clusters = null;
        maxIdLength = 0;
        maxLocusTagLength = 0;
        maxNameLength = 0;
	}
}
