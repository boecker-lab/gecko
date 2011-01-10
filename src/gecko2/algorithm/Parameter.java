package gecko2.algorithm;

public class Parameter {
	
	private int delta;
	private int minClusterSize;
	private int alphabetSize;
	private int[] codingTable;
	private int q;
	private short qtype;
	private char operationMode;
	
	// used to interrupt the c function when requested by the user.
	private volatile boolean run = true;
	
	public static final short QUORUM_NO_COST = 1;
	public static final short QUORUM_DELTA_OVER_NGENOMES = 2;
	public static final short QUORUM_MEDIAN_SIZE = 3;
	
	public void setRun(boolean run) {
		this.run = run;
	}
	
	public boolean isRun() {
		return run;
	}	
	
	public Parameter(int delta, int minClusterSize, int q, short qtype, char operationMode) {
		this.delta = delta;
		this.minClusterSize = minClusterSize;
		this.q= q;
		this.qtype = qtype;
		this.operationMode = operationMode;
	}
		
	public void setOperationMode(char opmode) {
		this.operationMode = opmode;
	}
	
	public char getOperationMode() {
		return operationMode;
	}
	
	public void setCodingTable(int[] codingTable) {
		this.codingTable = codingTable;
	}
	
	public int[] getCodingTable() {
		return codingTable;
	}
	
	public void setDelta(int delta) {
		this.delta = delta;
	}
	
	public int getDelta() {
		return delta;
	}
	
	public void setMinClusterSize(int minClusterSize) {
		this.minClusterSize = minClusterSize;
	}
	
	public int getMinClusterSize() {
		return minClusterSize;
	}
	
	public void setAlphabetSize(int alphabetSize) {
		this.alphabetSize = alphabetSize;
	}
	
	public int getAlphabetSize() {
		return alphabetSize;
	}

	public int getQ() {
		return q;
	}

	public void setQ(int q) {
		this.q = q;
	}

	public short getQtype() {
		return qtype;
	}

	public void setQtype(short qtype) {
		this.qtype = qtype;
	}

	
}
