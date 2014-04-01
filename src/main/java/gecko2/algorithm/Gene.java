package gecko2.algorithm;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Gene implements Serializable {
	private static final long serialVersionUID = 7903694077854093398L;

    public static final String UNKNOWN_GENE_ID = "0";

    private static Set<GeneFamily> geneFamilySet;
    private static GeneFamily unknownGeneFamily;
    private static Map<GeneFamily, Color> colorMap;

    public enum GeneOrientation {
        NEGATIVE(-1), POSITIVE(1), UNSIGNED(1);
        final int sign;
        GeneOrientation(int sign){
            this.sign = sign;
        }
        public int getSign(){
            return sign;
        }
    }

	private final String name;
	private final String tag;
	private final String annotation;
    private final GeneFamily geneFamily;
    private final GeneOrientation orientation;

	public Gene(String name, GeneFamily geneFamily, GeneOrientation orientation) {
		this(name, geneFamily, orientation, null);
	}
	
	public Gene(String name, GeneFamily geneFamily, GeneOrientation orientation, String annotation) {
        this(name, name, geneFamily, orientation, annotation);
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
        return Gene.getGeneColor(geneFamily);
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
				return "---- -"+annotation;
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

    public static void setGeneFamilySet(Set<GeneFamily> geneFamilySet, GeneFamily unknownGeneFamily) {
        Gene.geneFamilySet = geneFamilySet;
        Gene.unknownGeneFamily = unknownGeneFamily;
        colorMap = null;
    }

    public static int getAlphabetSize() {
        System.out.println("alphabetSize: " + (geneFamilySet.size() - 1 + unknownGeneFamily.getFamilySize()) + " compressed: " + (geneFamilySet.size() - 1));
        return geneFamilySet.size() - 1 + unknownGeneFamily.getFamilySize();
    }

    /*public static boolean isUnknownGene(int geneId) {
        GeneFamily eId = geneFamilySet.get(Math.abs(geneId));
        return eId != null ? eId.getExternalId().equals(Gene.UNKNOWN_GENE_ID) : true;
    }

    public static boolean isSingleGeneFamily(int geneId) {
        GeneFamily eId = geneFamilySet.get(Math.abs(geneId));
        return eId != null ? eId.isSingleGeneFamily() : true;
    }

    public static Set<Integer> getIntegerAlphabet() {
        return geneFamilySet.keySet();
    }*/

    private static Map<GeneFamily, Color> getColorMap() {
        if (colorMap == null) {
            Random r = new Random();
            colorMap = new HashMap<>();
            for (GeneFamily geneFamily : geneFamilySet)
                if (!geneFamily.isSingleGeneFamily())
                    colorMap.put(geneFamily, new Color(r.nextInt(240), r.nextInt(240), r.nextInt(240)));
        }
        return colorMap;
    }

    public static Color getGeneColor(GeneFamily geneFamily) {
        return getColorMap().get(geneFamily);
    }

    public GeneOrientation getOrientation() {
        return orientation;
    }
}
