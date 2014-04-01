package gecko2.algorithm;

/**
 * Gene family id information.
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class GeneFamily {
    private int familySize;
    private final String externalId;
    private int algorithmId;

    /**
     * A new gene family with a given external id, family size 1 and algorithm id -1
     * @param externalId the String externalId
     */
    public GeneFamily(String externalId) {
        this.familySize = 1;
        this.externalId = externalId;
        this.algorithmId = -1;
    }

    /**
     * A new gene family with a given external id and, family size. Algorithm id is -1
     * @param externalId the String externalId
     * @param familySize the size of the gene family
     */
    public GeneFamily(String externalId, int familySize) {
        this.familySize = 1;
        this.externalId = externalId;
        this.algorithmId = -1;
    }

    /**
     * Adds a a gene to the geneFamily and set the algorithm id to algorithmId
     * @param algorithmId the new algorithm id
     */
    public void addGene(int algorithmId) {
        this.algorithmId = algorithmId;
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
        if (externalId.equals(Gene.UNKNOWN_GENE_ID))
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
