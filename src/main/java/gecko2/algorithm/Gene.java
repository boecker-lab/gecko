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
		this(name, name, id, null);
	}
	
	public Gene(String name, int id, String annotation) {
		this.name = name;
		this.tag = name;
		this.id = id;
		this.annotation = annotation;
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
     * @return
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

    public static void setGeneLabelMap(Map<Integer, ExternalGeneId> geneLabelMap) {
        Gene.geneLabelMap = geneLabelMap;
        colorMap = null;
    }

    public static Map<Integer, ExternalGeneId> getGeneLabelMap() {
        return geneLabelMap;
    }

    public static int getAlphabetSize() {
        return geneLabelMap.size() - 1 + geneLabelMap.get(0).getFamilySize();
    }

    public static boolean isSingleGeneFamily(int geneId) {
        ExternalGeneId eId = geneLabelMap.get(geneId);
        return eId != null ? eId.isSingleGeneFamily() : true;
    }

    private static int getFamilySize(int geneId) {
        ExternalGeneId eId = geneLabelMap.get(geneId);
        return eId != null ? eId.getFamilySize() : 1;
    }

    public static Set<Integer> getIntegerAlphabet() {
        return geneLabelMap.keySet();
    }

    public static Map<Integer, Color> getColorMap() {
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
