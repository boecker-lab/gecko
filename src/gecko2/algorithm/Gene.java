package gecko2.algorithm;

import java.io.Serializable;

public class Gene implements Serializable {
	
	private static final long serialVersionUID = 7903694077854093398L;
	private String name;
	private int id;
	private String annotation;
	private boolean unknown = false;
	
	public boolean isUnknown() {
		return unknown;
	}
	
	public Gene() {
	}
	
	public Gene(String name, int id) {
		this(name, id, null,false);
	}
	
	public Gene(String name, int id, String annotation, boolean unknown) {
		this.unknown = unknown;
		this.name = name;
		this.id = id;
		this.annotation = annotation;
	}
	
	public Gene(Gene other) {
		this.name = other.name;
		this.id = other.id;
		this.annotation = other.annotation;
		this.unknown = other.unknown;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getAnnotation() {
		return annotation;
	}
	
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	
	public String getSummary() {
		if (annotation==null)
			if (name!=null && !name.equals("----")) {
				return name;
			} else
				return "[no annotation available]";
		if (name==null)
			return "---- -"+annotation;
		return name+" - "+annotation;
	}
	
	@Override
	public String toString() {
		return "["+id+","+name+","+annotation+"]";
	}
	
	
}
