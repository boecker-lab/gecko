package gecko2.algorithm;

/**
 * An external gene family id.
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class ExternalGeneId {
    private final boolean singleGeneFamily;
    private final String id;

    /**
     *
     * @param id the String id
     * @param singleGeneFamily if the gene family contains only one gene
     */
    public ExternalGeneId(String id, boolean singleGeneFamily) {
        this.singleGeneFamily = singleGeneFamily;
        this.id = id;
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
        return singleGeneFamily;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExternalGeneId that = (ExternalGeneId) o;

        if (singleGeneFamily != that.singleGeneFamily) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (singleGeneFamily ? 1 : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
