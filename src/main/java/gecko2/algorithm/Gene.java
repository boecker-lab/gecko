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

    private static Map<Integer, ExternalGeneId> geneLabelMap;
    private static Map<Integer, Color> colorMap;

	private final String name;
	private final String tag;
	private final int id;
	private final String annotation;

	public Gene(String name, int id) {
		this(name, id, null);
	}
	
	public Gene(String name, int id, String annotation) {
        this(name, name, id, annotation);
	}
	
	public Gene(String name, String tag, int id, String annotation) {
		this.name = name;
		this.tag = tag;
		this.id = id;
		this.annotation = annotation;
	}
	
	public Gene(Gene other) {
		this.name = other.name;
		this.tag = other.tag;
		this.id = other.id;
		this.annotation = other.annotation;
	}

    /**
     * Returns the internal integer id
     * @return the gene family id
     */
	public int getId() {
		return id;
	}

    public boolean isUnknown() {
        return Gene.isSingleGeneFamily(id);
    }

    public int getFamilySize() {
        return Gene.getFamilySize(id);
    }

    /**
     * Returns the external id from the input file
     * @return
     */
    public String getExternalId(){
        return Gene.getExternalId(id);
    }

    public Color getGeneColor() {
        return Gene.getGeneColor(id);
    }

    public static String getExternalId(int id) {
        ExternalGeneId externalGeneId = Gene.geneLabelMap.get(Math.abs(id));
        return externalGeneId == null ? Gene.UNKNOWN_GENE_ID : externalGeneId.getId();
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
		return "["+id+","+name+","+annotation+"]";
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gene gene = (Gene) o;

        if (id != gene.id) return false;
        if (!annotation.equals(gene.annotation)) return false;
        if (!name.equals(gene.name)) return false;
        if (!tag.equals(gene.tag)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + tag.hashCode();
        result = 31 * result + id;
        result = 31 * result + annotation.hashCode();
        return result;
    }

    public static void setGeneLabelMap(Map<Integer, ExternalGeneId> geneLabelMap) {
        Gene.geneLabelMap = geneLabelMap;
        colorMap = null;
    }

    public static Map<ExternalGeneId, Integer> getInverseGeneLabelMap() {
        Map<ExternalGeneId, Integer> result = new HashMap<>();
        for (Map.Entry<Integer, ExternalGeneId> entry : geneLabelMap.entrySet())
            result.put(entry.getValue(), entry.getKey());
        return result;
    }

    public static int getAlphabetSize() {
        System.out.println("alphabetSize: " + (geneLabelMap.size() - 1 + geneLabelMap.get(0).getFamilySize()) + " compressed: " + (geneLabelMap.size() - 1));
        return geneLabelMap.size() - 1 + geneLabelMap.get(0).getFamilySize();
    }

    public static boolean isUnknownGene(int geneId) {
        ExternalGeneId eId = geneLabelMap.get(Math.abs(geneId));
        return eId != null ? eId.getId().equals(Gene.UNKNOWN_GENE_ID) : true;
    }

    public static boolean isSingleGeneFamily(int geneId) {
        ExternalGeneId eId = geneLabelMap.get(Math.abs(geneId));
        return eId != null ? eId.isSingleGeneFamily() : true;
    }

    private static int getFamilySize(int geneId) {
        ExternalGeneId eId = geneLabelMap.get(Math.abs(geneId));
        return eId != null ? eId.getFamilySize() : 1;
    }

    public static Set<Integer> getIntegerAlphabet() {
        return geneLabelMap.keySet();
    }

    private static Map<Integer, Color> getColorMap() {
        if (colorMap == null) {
            Random r = new Random();
            colorMap = new HashMap<>();
            for (Map.Entry<Integer, ExternalGeneId> entry : geneLabelMap.entrySet())
                if (!entry.getValue().isSingleGeneFamily())
                    colorMap.put(entry.getKey(), new Color(r.nextInt(240), r.nextInt(240), r.nextInt(240)));
        }
        return colorMap;
    }

    public static Color getGeneColor(int id) {
        return getColorMap().get(Math.abs(id));
    }
}
