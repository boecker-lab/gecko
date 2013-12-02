package gecko2.algorithm;

import gecko2.GeckoInstance;

import java.io.Serializable;
import java.util.Map;

public class Gene implements Serializable {

    static Map<Integer, ExternalGeneId> getGenLabelMap;
	
	private static final long serialVersionUID = 7903694077854093398L;
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

    public static boolean isSingleGeneFamily(int id) {
        return GeckoInstance.getInstance().getGenLabelMap().get(Math.abs(id)).isSingleGeneFamily();
    }

    /**
     * Returns the external id from the input file
     * @return
     */
    public String getExternalId(){
        return Gene.getExternalId(id);
    }

    public static String getExternalId(int id) {
        ExternalGeneId externalGeneId = GeckoInstance.getInstance().getGenLabelMap().get(Math.abs(id));
        return externalGeneId == null ? "0" : externalGeneId.getId();
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
	
	
}
