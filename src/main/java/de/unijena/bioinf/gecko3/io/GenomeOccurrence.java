package de.unijena.bioinf.gecko3.io;

public class GenomeOccurrence {
	
	private String desc;
	private String genomeName;
	private String chromosomeName;
	private int start_line;
	private int end_line;
	private int group=0;
	private boolean flagged=false;
	
	public String getGenomeSelectorText(){
		if (chromosomeName.isEmpty())
			return String.format("<html><B>%s</B></html>", genomeName); 
		else
			return String.format("<html><B>%s</B> <I>%s</I></html>", genomeName, chromosomeName); 
	}
	
	public String getDesc() {
		return desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getGenomeName() {
		return genomeName;
	}

	public void setGenomeName(String genomeName) {
		this.genomeName = genomeName;
	}

	public String getChromosomeName() {
		return chromosomeName;
	}

	public void setChromosomeName(String chromosomeName) {
		this.chromosomeName = chromosomeName;
	}

	public int getStart_line() {
		return start_line;
	}
	
	public void setStart_line(int start_line) {
		this.start_line = start_line;
	}
	
	public int getEnd_line() {
		return end_line;
	}
	
	public void setEnd_line(int end_line) {
		this.end_line = end_line;
	}
	
	public void setGroup(int group) {
		this.group = group;
	}
	
	public int getGroup() {
		return group;
	}
	
	public boolean isFlagged() {
		return flagged;
	}
	
	public void setFlagged(boolean flagged) {
		this.flagged = flagged;
	}

	@Override
	public String toString() {
		return "["+desc+"," +start_line+"," +end_line+"," +group+"," +flagged +"]";
	}
}
