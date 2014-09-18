package gecko2.algorithm;

import gecko2.algorithm.util.MutableInteger;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * All information to genomes, gene families and gene clusters
 *
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class DataSet {
    private Genome[] genomes;
    private List<GeneCluster> clusters;

    private final int maxIdLength;
    private final int maxNameLength;
    private final int maxLocusTagLength;

    private final Set<GeneFamily> geneFamilySet;
    private final GeneFamily unknownGeneFamily;
    private final int numberOfGeneFamiliesWithMultipleGenes;
    private Map<GeneFamily, Color> colorMap;

    public static DataSet getEmptyDataSet() {
        return new DataSet(null, null, 0, 0, 0, null, null, 0);
    }

    public DataSet(Genome[] genomes, int maxIdLength, int maxNameLength, int maxLocusTagLength, Set<GeneFamily> geneFamilySet, GeneFamily unknownGeneFamily, int numberOfGeneFamiliesWithMultipleGenes) {
        this(genomes, null, maxIdLength, maxNameLength, maxLocusTagLength, geneFamilySet, unknownGeneFamily, numberOfGeneFamiliesWithMultipleGenes);
    }

    public DataSet(Genome[] genomes,
                   List<GeneCluster> clusters,
                   int maxIdLength,
                   int maxNameLength,
                   int maxLocusTagLength,
                   Set<GeneFamily> geneFamilySet,
                   GeneFamily unknownGeneFamily,
                   int numberOfGeneFamiliesWithMultipleGenes) {
        this.genomes = genomes;
        this.setClusters(clusters==null ? new ArrayList<GeneCluster>() : clusters);
        this.maxIdLength = maxIdLength;
        this.maxNameLength = maxNameLength;
        this.maxLocusTagLength = maxLocusTagLength;
        this.geneFamilySet = geneFamilySet;
        this.unknownGeneFamily = unknownGeneFamily;
        this.numberOfGeneFamiliesWithMultipleGenes = numberOfGeneFamiliesWithMultipleGenes;
        this.colorMap = null;
    }

    public static int[][][] createRunLengthMergedLookup(int[][][] intArray){
        int[][][] help2 = new int[intArray.length][][];
        for (int i=0;i<intArray.length;i++){
            help2[i] = new int[intArray[i].length][];
            for (int j=0;j<intArray[i].length;j++){
                List<Integer> help = new ArrayList<>();
                for (int m=0;m<intArray[i][j].length;m++){
                    if(intArray[i][j][m]<-1)
                        help.add(m);
                }
                help2[i][j] = new int[help.size()];
                for (int m=0;m<help.size();m++){
                    help2[i][j][m]=help.get(m);
                }
            }
        }

        return help2;
    }

    /**
	 * Generates an int array from the genomes
	 * @return an int array, containing all the genes
	 */
	public int[][][] toIntArray() {
		return toIntArray(false, false, true, true);
	}

    /**
     * Generates an int array from the genomes
     * @return an int array, containing all the genes
     */
    public int[][][] toReducedIntArray() {
        return toIntArray(true, false, true, true);
    }

    /**
     * Generates an int array from the genomes
     * @return an int array, containing all the genes
     */
    public int[][][] toSignedIntArray() {
        return toIntArray(false, false, false, false);
    }

    public int[][][] toSignedRandomIntArray() {
        return toIntArray(false, true, false, false);
    }

    /**
     * Generates an int array from the genomes
     * @param useMemoryReduction if memory reduction should be applied
     * @return an int array, containing all the genes
     */
    private int[][][] toIntArray(boolean useMemoryReduction, boolean randomizeGeneOrder, boolean addZeros, boolean absolute) {
        int genomeArray[][][] = new int[genomes.length][][];
        MutableInteger unHomologeGeneFamilyId = new MutableInteger(numberOfGeneFamiliesWithMultipleGenes + 1);
        for (int i=0;i<genomes.length;i++) {
            genomeArray[i] = new int[genomes[i].getChromosomes().size()][];
            for (int j=0;j<genomeArray[i].length;j++)
                if (useMemoryReduction)
                    genomeArray[i][j] = genomes[i].getChromosomes().get(j).toReducedIntArray(true);
                else if (randomizeGeneOrder)
                    genomeArray[i][j] = genomes[i].getChromosomes().get(j).toRandomIntArray(unHomologeGeneFamilyId, addZeros, absolute);
                else
                    genomeArray[i][j] = genomes[i].getChromosomes().get(j).toIntArray(unHomologeGeneFamilyId, addZeros, absolute);
        }
        return genomeArray;
    }

    /**
     * Print the int array generated by Genome.toIntArray
     * @param genomes the genomes
     */
    public static void printIntArray(int[][][] genomes) {
        for (int[][] genome : genomes) {
            StringBuilder builder = new StringBuilder();
            for (int[] chromosome : genome)
                builder.append(Arrays.toString(chromosome));
            System.out.println(builder.toString());
        }
    }

    /**
     * Print statistics of gene family sizes for all genomes
     */
    public void printGenomeStatistics(){
        printGenomeStatistics(-1, -1);
    }

    /**
     * Print statistics of gene family sizes for all genomes with genomeSize +/- genomeSizeDelta genes.
     * @param genomeSize  The number of genes that is needed for a genome to be reported. -1 will report statistics for all genomes.
     * @param genomeSizeDelta The maximum deviation form the genomeSize for a genome to be reported
     */
    public void printGenomeStatistics(int genomeSize, int genomeSizeDelta) {
        int[][] alphabetPerGenome = new int[genomes.length][getCompleteAlphabetSize() + 1];
        String[][] annotations = new String[genomes.length][getCompleteAlphabetSize() + 1];

        SortedMap<Integer,Integer> summedFamilySizes = new TreeMap<>();
        int nrReportedGenomes = 0;
        List<Integer> genomeSizes = new ArrayList<>();
        // Generate family sizes per genome and print it
        for (int n=0; n<genomes.length; n++){
            Genome g = genomes[n];
            if (genomeSize != -1 && (g.getTotalGeneNumber() < genomeSize-genomeSizeDelta || g.getTotalGeneNumber() > genomeSize + genomeSizeDelta))
                continue;
            else {
                nrReportedGenomes++;
                genomeSizes.add(g.getTotalGeneNumber());
            }
            for (Chromosome chr : g.getChromosomes()) {
                for (Gene gene : chr.getGenes()) {
                    alphabetPerGenome[n][Math.abs(gene.getAlgorithmId())]++;
                    //if (n!=0)
                    annotations[n][Math.abs(gene.getAlgorithmId())] = gene.getAnnotation();
                    //else
                    //annotations[n][Math.abs(gene.getExternalId())] = String.format("%s: %s", chr.getName().substring(24), gene.getAnnotation());
                }
            }
            SortedMap<Integer,Integer> familySizes = new TreeMap<>();
            for (int i=1; i<alphabetPerGenome[n].length; i++){
                // add family size for this genome
                Integer fS = familySizes.get(alphabetPerGenome[n][i]);
                if (fS != null)
                    familySizes.put(alphabetPerGenome[n][i], ++fS);
                else
                    familySizes.put(alphabetPerGenome[n][i], 1);

                // add summed family sizes
                fS = summedFamilySizes.get(alphabetPerGenome[n][i]);
                if (fS != null)
                    summedFamilySizes.put(alphabetPerGenome[n][i], ++fS);
                else
                    summedFamilySizes.put(alphabetPerGenome[n][i], 1);
            }

            Integer nonOccFamilies = familySizes.get(0);
            System.out.println(String.format("%s: %d genes, %d gene families", g.getName(), g.getTotalGeneNumber(), (nonOccFamilies == null) ? alphabetPerGenome[n].length-1 : alphabetPerGenome[n].length - 1 - nonOccFamilies));
            for (Map.Entry<Integer, Integer> entry : familySizes.entrySet()){
                System.out.println(String.format("%d\t%d", entry.getKey(), entry.getValue()));
            }
            System.out.println();
        }

        for (Integer size : genomeSizes)
            System.out.print(size + ", ");
        System.out.println("");
        System.out.println(String.format("Sum of %d genomes:", nrReportedGenomes));
        for (Map.Entry<Integer, Integer> entry : summedFamilySizes.entrySet()){
            if (entry.getKey() != 0)
                System.out.println(String.format("%d\t%d", entry.getKey(), entry.getValue()));
        }
        System.out.println();

        // generate complete family sizes and print it
        int[] alphabet = new int[getCompleteAlphabetSize() + 1];
        for (Genome g : genomes){
            if (genomeSize != -1 && (g.getTotalGeneNumber() < genomeSize-genomeSizeDelta || g.getTotalGeneNumber() > genomeSize + genomeSizeDelta))
                continue;
            for (Chromosome chr : g.getChromosomes())
                for (Gene gene : chr.getGenes())
                    alphabet[Math.abs(gene.getAlgorithmId())]++;
        }
        SortedMap<Integer,Integer> familySizes = new TreeMap<>();
        for (int i=1; i<alphabet.length; i++){
            Integer fS = familySizes.get(alphabet[i]);
            if (fS != null)
                familySizes.put(alphabet[i], ++fS);
            else
                familySizes.put(alphabet[i], 1);
        }

        System.out.println("Complete:");
        for (Map.Entry<Integer, Integer> entry : familySizes.entrySet()){
            System.out.println(String.format("%d\t%d", entry.getKey(), entry.getValue()));
        }

			/*
			for(int j=0; j<alphabetPerGenome.length; j++) {
				System.out.print(String.format("%s\t", genomes[j].getName()));
			}
			System.out.println("");
			for (int i=0; i<alphabet.length; i++) {
				for(int j=0; j<alphabetPerGenome.length; j++) {
					System.out.print(String.format("%d\t", alphabetPerGenome[j][i]));
				}
				System.out.println("");
			}
			for(int j=0; j<alphabetPerGenome.length; j++) {
				System.out.print(String.format("%s\t", genomes[j].getName()));
			}
			System.out.println("");
			*/
			/*
			for (int i=0; i<alphabet.length; i++) {
				if (alphabetPerGenome[0][i] == 1) {
					boolean print = false;
					StringBuilder builder = new StringBuilder();
					builder.append("\"").append(annotations[0][i]).append("\"");
					for(int j=1; j<alphabetPerGenome.length; j++) {
						if (alphabetPerGenome[j][i] == 1) {
							builder.append("\t").append("\"").append(annotations[j][i]).append("\"");
							print = true;
						} else
							builder.append("\t").append("-");
					}
					if (print)
						System.out.println(builder.toString());
				}
			}*/
    }

    public Genome[] getGenomes() {
        return genomes;
    }

    public void reorderGenomes(int index) {
        Genome first = genomes[0];
        genomes[0] = genomes[index];
        genomes[index] = first;
    }


    /**
     * Adds a new reference Genome to the data set
     * @param newReferenceGenome
     */
    public void addReferenceGenome(Genome newReferenceGenome) {
        Genome[] oldGenomes = genomes;
        genomes = new Genome[oldGenomes.length+1];
        genomes[0] = newReferenceGenome;
        System.arraycopy(oldGenomes, 0, genomes, 1, oldGenomes.length);
    }

    public List<GeneCluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<GeneCluster> clusters) {
        this.clusters = correctInvalidClusters(clusters, genomes, 3);
    }

    private static List<GeneCluster> correctInvalidClusters(List<GeneCluster> clusters, Genome[] genomes, int minClusterSize){
        if (genomes == null)
            return clusters;
        List<GeneCluster> cleanedCluster = new ArrayList<>(clusters.size());
        for (GeneCluster cluster : clusters){
            if (!cluster.invalidMultiGeneFamilyGeneCluster(minClusterSize, genomes)){
                cleanedCluster.add(cluster);
            }
        }
        return cleanedCluster;
    }

    public int getMaxIdLength() {
        return maxIdLength;
    }

    public int getMaxNameLength() {
        return maxNameLength;
    }

    public int getMaxLocusTagLength() {
        return maxLocusTagLength;
    }

    public Set<GeneFamily> getGeneFamilySet() {
        return geneFamilySet;
    }

    public GeneFamily getUnknownGeneFamily() {
        return unknownGeneFamily;
    }

    public Map<String,GeneFamily> getGeneLabelMap() {
        Map<String, GeneFamily> geneFamilyMap = new HashMap<>();
        geneFamilyMap.put(unknownGeneFamily.getExternalId(), unknownGeneFamily);
        for (GeneFamily geneFamily : geneFamilySet)
            geneFamilyMap.put(geneFamily.getExternalId(), geneFamily);
        return geneFamilyMap;
    }

    public int getCompleteAlphabetSize() {
        return geneFamilySet.size() + unknownGeneFamily.getFamilySize();
    }

    public int getReducedAlphabetSize() {
        return numberOfGeneFamiliesWithMultipleGenes;
    }

    private Map<GeneFamily, Color> getColorMap() {
        if (colorMap == null) {
            Random r = new Random();
            colorMap = new HashMap<>();
            for (GeneFamily geneFamily : geneFamilySet)
                if (!geneFamily.isSingleGeneFamily())
                    colorMap.put(geneFamily, new Color(r.nextInt(240), r.nextInt(240), r.nextInt(240)));
        }
        return colorMap;
    }

    public Color getGeneColor(GeneFamily geneFamily) {
        return getColorMap().get(geneFamily);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSet dataSet = (DataSet) o;

        if (numberOfGeneFamiliesWithMultipleGenes != dataSet.numberOfGeneFamiliesWithMultipleGenes) return false;
        if (!clusters.equals(dataSet.clusters)) return false;
        if (geneFamilySet != null ? !geneFamilySet.equals(dataSet.geneFamilySet) : dataSet.geneFamilySet != null)
            return false;
        if (!Arrays.equals(genomes, dataSet.genomes)) return false;
        if (unknownGeneFamily != null ? !unknownGeneFamily.equals(dataSet.unknownGeneFamily) : dataSet.unknownGeneFamily != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = genomes != null ? Arrays.hashCode(genomes) : 0;
        result = 31 * result + (clusters != null ? clusters.hashCode() : 0);
        result = 31 * result + (geneFamilySet != null ? geneFamilySet.hashCode() : 0);
        result = 31 * result + (unknownGeneFamily != null ? unknownGeneFamily.hashCode() : 0);
        result = 31 * result + numberOfGeneFamiliesWithMultipleGenes;
        return result;
    }

    public void clearClusters() {
        clusters.clear();
    }
}
