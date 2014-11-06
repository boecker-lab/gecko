package gecko2.datastructures;

/**
 * Gene family id information.
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class GeneFamily {
    public static final String UNKNOWN_GENE_ID = "0";

    /**
     * The number of gene families with more than one gene that were read since the last call to
     * {@link #getNewUnknownGeneFamilyAndInitializeAlgorithmId() getNewUnknownGeneFamilyAndInitializeAlgorithmId()}
     *
     * @return the number of gene families with more than one gene
     */
    public static int getNumberOfGeneFamiliesWithMultipleGenes() {
        return algorithmIdCounter - 1;
    }

    private static int algorithmIdCounter;

    private int familySize;
    private final String externalId;
    private int algorithmId;

    /**
     * A new gene family with a given external id, family size 1 and algorithm id -1.
     * Use {@link #getNewUnknownGeneFamilyAndInitializeAlgorithmId() getNewUnknownGeneFamilyAndInitializeAlgorithmId()}
     * method before to initialize algorithm id starting from 1.
     * @param externalId the String externalId
     */
    public GeneFamily(String externalId) {
        this(externalId, -1, 1);
    }

    /**
     * A new gene family with a given external id, family size and algorithm id
     * @param externalId the String externalId
     * @param algorithmId the the new algorithm id
     * @param familySize the size of the gene family
     */
    private GeneFamily(String externalId, int algorithmId, int familySize) {
        this.familySize = familySize;
        this.externalId = externalId;
        this.algorithmId = algorithmId;
    }

    /**
     * Only for Unit Testing
     * @param externalId
     * @param algorithmId
     * @param familySize
     * @return
     */
    public static GeneFamily getTestGeneFamily(String externalId, int algorithmId, int familySize){
        return new GeneFamily(externalId, algorithmId, familySize);
    }

    public static GeneFamily getNewUnknownGeneFamilyAndInitializeAlgorithmId() {
        algorithmIdCounter = 1;
        return new GeneFamily(UNKNOWN_GENE_ID, -1, 0);
    }

    /**
     * Adds a a gene to the geneFamily and set the algorithm id to the next free algorithmId if the externalId is
     * not {@link #UNKNOWN_GENE_ID UNKNOWN_GENE_ID} and the algorithmId has not been set.
     * Use {@link #getNewUnknownGeneFamilyAndInitializeAlgorithmId() getNewUnknownGeneFamilyAndInitializeAlgorithmId()}
     * method before to initialize algorithm id starting from 1.
     */
    public void addGene() {
        if (this.algorithmId == -1 && !this.externalId.equals(UNKNOWN_GENE_ID))
            this.algorithmId = algorithmIdCounter++;
        this.familySize++;
    }

    /**
     *
     * @return the String externalId
     */
    public String getExternalId() {
        return externalId;
    }

    public int getAlgorithmId() {
        return algorithmId;
    }

    /**
     *
     * @return true if the gene family contains only one gene
     */
    public boolean isSingleGeneFamily() {
        if (externalId.equals(UNKNOWN_GENE_ID))
            return true;
        return 1 == familySize;
    }

    public int getFamilySize() {
        return familySize;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneFamily that = (GeneFamily) o;

        if (familySize != that.familySize) return false;
        if (!externalId.equals(that.externalId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = familySize;
        result = 31 * result + externalId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GeneFamily{" +
                "externalId='" + externalId + "', size=" + familySize +
                '}';
    }
}
