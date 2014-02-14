package gecko2.algorithm;

/**
 * An external gene family id.
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class ExternalGeneId {
    private final int familySize;
    private final String id;

    /**
     *
     * @param id the String id
     * @param familySize if the gene family size
     */
    public ExternalGeneId(String id, int familySize) {
        this.familySize = familySize;
        this.id = id;
    }

    /**
     * An ExternalGeneId for Genes without homology information
     * @param numberOfUnknownGenes
     * @return
     */
    public static ExternalGeneId getUnknownGeneID(int numberOfUnknownGenes) {
        return new ExternalGeneId(Gene.UNKNOWN_GENE_ID, numberOfUnknownGenes);
    }

    /**
     *
     * @return the String id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return true if the gene family contains only one gene
     */
    public boolean isSingleGeneFamily() {
        if (id.equals(Gene.UNKNOWN_GENE_ID))
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

        ExternalGeneId that = (ExternalGeneId) o;

        if (familySize != that.familySize) return false;
        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = familySize;
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ExternalGeneId{" +
                "id='" + id + "', size=" + familySize +
                '}';
    }
}
