package gecko2.algorithm;

public class Parameter {
	
    private final static int DELTA_TABLE_SIZE = 3;
	
	private int delta;
	private int[][] deltaTable;
	private int minClusterSize;
	private int alphabetSize;
	private int q;
	private short qtype;
	private char refType;
	private char operationMode;
	private boolean searchRefInRef;
	
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
	
	public Parameter(int delta, int minClusterSize, int q, short qtype, char operationMode, char refType) {
		this(delta, minClusterSize, q, qtype, operationMode, refType, false);
	}
	
	public Parameter(int delta, int minClusterSize, int q, short qtype, char operationMode, char refType, boolean searchRefInRef) {
		if (searchRefInRef && operationMode != 'r')
			throw new IllegalArgumentException("Searching the reference occurrence in the reference genome is only compatible with reference mode!");
		if (refType != 'd' && refType != 'g' && refType != 'c')
			throw new IllegalArgumentException("Invalid reference Type. Only (d)efault, (g)enome or (c)luster are valid!");
		
		this.delta = delta;
		this.deltaTable = null;
		this.minClusterSize = minClusterSize;
		this.q = q;
		this.qtype = qtype;
		this.operationMode = operationMode;
		this.refType = refType;
		this.alphabetSize = -1;
		this.searchRefInRef = searchRefInRef;
	}
	
	public Parameter(int[][] deltaTable, int minClusterSize, int q, short qtype, char operationMode, char refType) {
		this(deltaTable, minClusterSize, q, qtype, operationMode, refType, false);
	}
	
	public Parameter(int[][] deltaTable, int minClusterSize, int q, short qtype, char operationMode, char refType, boolean searchRefInRef) {
		if (operationMode != 'r')
			throw new IllegalArgumentException("Delta table is only compatible with reference mode!");
		
		this.delta = -1;
		this.deltaTable = new int[deltaTable.length][];
		for (int i=0; i<deltaTable.length; i++){
        	if (deltaTable[i].length != DELTA_TABLE_SIZE)
        		throw new IllegalArgumentException("Invalid delta table! Must contain arrays of length " + DELTA_TABLE_SIZE + ".");
        	this.deltaTable[i] = new int[DELTA_TABLE_SIZE];
        	System.arraycopy(deltaTable[i], 0, this.deltaTable[i], 0, DELTA_TABLE_SIZE);
        }
		this.minClusterSize = minClusterSize;
		this.q = q;
		this.qtype = qtype;
		this.operationMode = operationMode;
		this.refType = refType;
		this.alphabetSize = -1;
		this.searchRefInRef = searchRefInRef;
	}
	
	public char getRefType() {
		return refType;
	}
		
	public void setOperationMode(char opmode) {
		this.operationMode = opmode;
	}
	
	public char getOperationMode() {
		return operationMode;
	}
	
	public void setDelta(int delta) {
		this.delta = delta;
	}
	
	public int getDelta() {
		return delta;
	}
	
	public int[][] getDeltaTable() {
		return deltaTable;
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
	
	public boolean searchRefInRef() {
		return searchRefInRef;
	}
	
	public boolean useJavaAlgorithm() {
		return operationMode == 'r';
	}
}
