package gecko2.algorithm;

import gecko2.GeckoInstance;

import java.awt.*;
import java.io.Serializable;

public class Gene implements Serializable {
	private static final long serialVersionUID = 7903694077854093398L;

    public enum GeneOrientation {
        POSITIVE(1, "+"), NEGATIVE(-1, "-"), UNSIGNED(1, "");
        private final int sign;
        private final String encoding;
        GeneOrientation(int sign, String encoding){
            this.sign = sign;
            this.encoding = encoding;
        }
        public int getSign(){
            return sign;
        }
        public String getEncoding() {
            return encoding;
        }
    }

	private final String name;
	private final String tag;
	private final String annotation;
    private final GeneFamily geneFamily;
    private final GeneOrientation orientation;

    public Gene(GeneFamily geneFamily){
        this("", geneFamily, GeneOrientation.POSITIVE);
    }

	public Gene(String name, GeneFamily geneFamily, GeneOrientation orientation) {
		this(name, geneFamily, orientation, null);
	}
	
	public Gene(String name, GeneFamily geneFamily, GeneOrientation orientation, String annotation) {
        this(name, "", geneFamily, orientation, annotation);
	}
	
	public Gene(String name, String tag, GeneFamily geneFamily, GeneOrientation orientation, String annotation) {
		this.name = name;
		this.tag = tag;
		this.geneFamily = geneFamily;
		this.annotation = annotation;
        this.orientation = orientation;
	}
	
	public Gene(Gene other) {
		this.name = other.name;
		this.tag = other.tag;
		this.geneFamily = other.geneFamily;
		this.annotation = other.annotation;
        this.orientation = other.orientation;
	}

    public boolean isUnknown() {
        return geneFamily.isSingleGeneFamily();
    }

    public int getFamilySize() {
        return geneFamily.getFamilySize();
    }

    public GeneFamily getGeneFamily() {
        return geneFamily;
    }

    /**
     * Returns the external id from the input file
     * @return
     */
    public String getExternalId(){
        return geneFamily.getExternalId();
    }

    public int getAlgorithmId() {
        return getGeneFamily().getAlgorithmId();
    }

    public Color getGeneColor() {
        return GeckoInstance.getInstance().getGeneColor(geneFamily);
    }
	
	public String getName() {
		return name;
	}
	
	public String getTag() {
		return tag;
	}

	public String getAnnotation() {
		return annotation;
	}

	public String getSummary() {
		if (annotation==null)
			if (name!=null && !name.equals("----")) {
				return name;
			} else
				return "[no annotation available]";
		if (name==null || name.equals(""))
			if (tag == null || tag.equals(""))
				return "---- - "+ annotation;
			else
				return tag+" - "+annotation;

		return name+" - "+annotation;
	}
	
	@Override
	public String toString() {
		return "["+geneFamily+","+name+","+annotation+"]";
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gene gene = (Gene) o;

        if (!annotation.equals(gene.annotation)) return false;
        if (!geneFamily.equals(gene.geneFamily)) return false;
        if (!name.equals(gene.name)) return false;
        if (orientation != gene.orientation) return false;
        if (!tag.equals(gene.tag)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + tag.hashCode();
        result = 31 * result + annotation.hashCode();
        result = 31 * result + geneFamily.hashCode();
        result = 31 * result + orientation.hashCode();
        return result;
    }

    public GeneOrientation getOrientation() {
        return orientation;
    }
}
