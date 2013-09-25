package gecko2.algorithm;

import gecko2.GeckoInstance;

import java.io.Serializable;

public class Gene implements Serializable {
	
	private static final long serialVersionUID = 7903694077854093398L;
	private final String name;
	private final String tag;
	private final int id;
	private final String annotation;
	private final boolean unknown;
	
	public boolean isUnknown() {
		return unknown;
	}
	
	public Gene(String name, int id) {
		this(name, name, id, null,false);
	}
	
	public Gene(String name, int id, String annotation, boolean unknown) {
		this.unknown = unknown;
		this.name = name;
		this.tag = name;
		this.id = id;
		this.annotation = annotation;
	}
	
	public Gene(String name, String tag, int id, String annotation, boolean unknown) {
		this.unknown = unknown;
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
		this.unknown = other.unknown;
	}

    /**
     * Returns the internal integer id
     * @return
     */
	public int getId() {
		return id;
	}

    /**
     * Returns the external id from the input file
     * @return
     */
    public String getExternalId(){
        return Gene.getExternalId(id);
    }

    public static String getExternalId(int id) {
        return GeckoInstance.getInstance().getGenLabelMap().get(Math.abs(id))[0];
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
