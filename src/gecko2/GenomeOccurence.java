package gecko2;

public class GenomeOccurence {
	
	private String desc;
	private int start_line;
	private int end_line;
	private int group=0;
	private boolean flagged=false;
	
	public String getDesc() {
		return desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
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
