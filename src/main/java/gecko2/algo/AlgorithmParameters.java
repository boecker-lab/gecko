package gecko2.algo;

import gecko2.algorithm.Parameter;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class AlgorithmParameters{
    private final int delta;                // the parameter delta the algorithm is started with
    private final int[][] deltaTable;
    private final boolean useDeltaTable;
	private final int minClusterSize;       // minimal size of each cluster
	private int maxUncoveredGenomes;
	private final int minCoveredGenomes;
    private int nrOfGenomes;
    private final int alphabetSize;
    
    private final boolean singleReference;
    private final boolean refInRef;
    
    private final static int DELTA_TABLE_SIZE = 3;
    
    private final static int[][] HIGHLY_CONSERVED_DELTA_TABLE = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 0, 1}, {2, 1, 2}, {3, 2, 3}, {4, 2, 4}, {5, 3, 5}, {6, 3, 6}};
    private final static int[][] LOW_CONSERVED_DELTA_TABLE = new int[][]{{0, 0, 0}, {0, 0, 0}, {1, 0, 1}, {1, 1, 1}, {2, 1, 2}, {3, 2, 3}, {3, 3, 3}, {4, 3, 4}, {5, 3, 5}, {6, 4, 6}};
    private final static int[][] LICHTHEIMIA_DELTA_TABLE = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 1, 1}, {2, 2, 2}, {3, 3, 3}, {4, 4, 4}};
    private final static int[][] LICHTHEIMIA_INNER_GENOME_DELTA_TABLE = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 1, 1}, {2, 2, 2}};
    private final static int[][] STATISTIC_PAPER_DELTA_TABLE = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 1, 1}, {2, 2, 2}, {3, 3, 3}, {5, 5, 5}};

    
	public AlgorithmParameters(int delta, int minClusterSize, int q, int nrOfGenomes, int alphabetSize, boolean singleReference) {
		this(delta, minClusterSize, q, nrOfGenomes, alphabetSize, singleReference, false);
	}

	public AlgorithmParameters(int delta, int minClusterSize, int q, int nrOfGenomes, int alphabetSize, boolean singleReference, boolean refInRef) {
		this(delta, null, minClusterSize, q, nrOfGenomes, alphabetSize, singleReference, refInRef);
	}
	
	public AlgorithmParameters(int[][] deltaTable, int minClusterSize, int q, int nrOfGenomes, int alphabetSize, boolean singleReference) {
		this(deltaTable, minClusterSize, q, nrOfGenomes, alphabetSize, singleReference, false);
	}
	
	public AlgorithmParameters(int[][] deltaTable, int minClusterSize, int q, int nrOfGenomes, int alphabetSize, boolean singleReference, boolean refInRef) {
		this(-1, deltaTable, minClusterSize, q, nrOfGenomes, alphabetSize, singleReference, refInRef);
	}
	
	public AlgorithmParameters(Parameter p, int alphabetSize, int nrOfGenomes) {
		this(p.getDelta(), p.getDeltaTable(), p.getMinClusterSize(), p.getQ(), nrOfGenomes, alphabetSize, (p.getRefType() == 'd') ? false : true, p.searchRefInRef());
		if (!p.useJavaAlgorithm())
			throw new IllegalArgumentException("Parameters not compatible to Java mode.");
	}
	
	public static AlgorithmParameters getHighlyConservedParameters(Parameter p, int alphabetSize, int nrOfGenomes) {
		return new AlgorithmParameters(-1, HIGHLY_CONSERVED_DELTA_TABLE, 3, p.getQ(), nrOfGenomes, alphabetSize, (p.getRefType() == 'd') ? false : true, p.searchRefInRef());
	}
	
	public static AlgorithmParameters getHighlyConservedParameters(int q, int nrOfGenomes, int alphabetSize, boolean singleReference, boolean refInRef) {
		return new AlgorithmParameters(-1, HIGHLY_CONSERVED_DELTA_TABLE, 3, q, nrOfGenomes, alphabetSize, singleReference, refInRef);
	}
	
	public static AlgorithmParameters getLowConservedParameters(Parameter p, int alphabetSize, int nrOfGenomes) {
		return new AlgorithmParameters(-1, LOW_CONSERVED_DELTA_TABLE, 3, p.getQ(), nrOfGenomes, alphabetSize, (p.getRefType() == 'd') ? false : true, p.searchRefInRef());
	}
	
	public static AlgorithmParameters getLowConservedParameters(int q, int nrOfGenomes, int alphabetSize, boolean singleReference, boolean refInRef) {
		return new AlgorithmParameters(-1, LOW_CONSERVED_DELTA_TABLE, 3, q, nrOfGenomes, alphabetSize, singleReference, refInRef);
	}
	
	public static AlgorithmParameters getLichtheimiaParameters(Parameter p, int alphabetSize, int nrOfGenomes) {
		return new AlgorithmParameters(-1, LICHTHEIMIA_DELTA_TABLE, 3, p.getQ(), nrOfGenomes, alphabetSize, (p.getRefType() == 'd') ? false : true, p.searchRefInRef());
	}
	
	public static AlgorithmParameters getLichtheimiaInnerGenomeParameters(Parameter p, int alphabetSize, int nrOfGenomes) {
		return new AlgorithmParameters(-1, LICHTHEIMIA_INNER_GENOME_DELTA_TABLE, 3, p.getQ(), nrOfGenomes, alphabetSize, (p.getRefType() == 'd') ? false : true, true);
	}
	
	public static AlgorithmParameters getStatisticPaperGenomeParameters(Parameter p, int alphabetSize, int nrOfGenomes) {
		return new AlgorithmParameters(-1, STATISTIC_PAPER_DELTA_TABLE, 4, p.getQ(), nrOfGenomes, alphabetSize, (p.getRefType() == 'd') ? false : true, p.searchRefInRef());
	}
	
	public static AlgorithmParameters getLichtheimiaParameters(int q, int nrOfGenomes, int alphabetSize, boolean singleReference, boolean refInRef) {
		return new AlgorithmParameters(-1, LICHTHEIMIA_DELTA_TABLE, 3, q, nrOfGenomes, alphabetSize, singleReference, refInRef);
	}
	
	private AlgorithmParameters(int delta, int[][] deltaTable, int minClusterSize, int q, int nrOfGenomes, int alphabetSize, boolean singleReference, boolean refInRef) {
		if (delta >= 0 && deltaTable != null)
			throw new IllegalArgumentException("Invalid delta and deltaTable values. Cannot use both!");
		if (delta < 0 && deltaTable == null)
			throw new IllegalArgumentException("Invalid delta and deltaTable values. Must use one!");
		
		this.delta = delta;
		if (deltaTable == null) {
			this.deltaTable = null;
			this.useDeltaTable = false;
		} else {
			this.useDeltaTable = true;
			if (!checkDeltaTable(deltaTable))
				throw new IllegalArgumentException("Invalid delta table!");
			
			this.deltaTable = new int[deltaTable.length][];
			for (int i=0; i<deltaTable.length; i++){
				this.deltaTable[i] = new int[DELTA_TABLE_SIZE];
				System.arraycopy(deltaTable[i], 0, this.deltaTable[i], 0, DELTA_TABLE_SIZE);
			}
		}
		this.minClusterSize = minClusterSize;
        if (q == 0) {
            this.maxUncoveredGenomes = 0;
            this.minCoveredGenomes = nrOfGenomes;
        } else {
            this.maxUncoveredGenomes = nrOfGenomes-q;
            this.minCoveredGenomes = q;
        }
        this.nrOfGenomes = nrOfGenomes;
        this.alphabetSize = alphabetSize;
        this.singleReference = singleReference;
        this.refInRef = refInRef;
	}
	
	/**
	 * Checks if a valide delta table has been supplied
	 * @param deltaTable the delta table
	 * @return true if the table is valid, else false
	 */
	private static boolean checkDeltaTable(int[][] deltaTable){
		int ins=0;
		int del=0;
		int total=0;
		for (int i=0; i<deltaTable.length; i++) {
			if (deltaTable[i].length != DELTA_TABLE_SIZE)
				return false;
			
			// allowed distance must not decrease
			if (deltaTable[i][0] < ins) 
				return false;
			ins = deltaTable[i][0];
			
			if (deltaTable[i][1] < del)
				return false;
			del = deltaTable[i][1];
			
			// allowed total must be larger than insertions and deletions
			if (deltaTable[i][2] < total || deltaTable[i][2] < deltaTable[i][1] || deltaTable[i][2] < deltaTable[i][0])
				return false;
			total = deltaTable[i][2];
			
			
		}
		return true;
	}

    public int getAlphabetSize() {
		return alphabetSize;
	}

	public int getNrOfGenomes() {
        return nrOfGenomes;
    }

	public int getMinClusterSize() {
		return minClusterSize;
	}
	
	public int getDeltaInsertions(int clusterSize){
		if (!useDeltaTable)
			return delta;
				
		if (clusterSize >= deltaTable.length)
			return deltaTable[deltaTable.length-1][0];
		else
			return deltaTable[clusterSize][0];
	}
	
	public int getMaximumInsertions() {
		if (!useDeltaTable)
			return delta;
		
		return deltaTable[deltaTable.length-1][0];
	}
	
	public int getDeltaDeletions(int clusterSize){
		if (!useDeltaTable)
			return delta;
		
		if (clusterSize >= deltaTable.length)
			return deltaTable[deltaTable.length-1][1];
		else
			return deltaTable[clusterSize][1];
	}
	
	public int getMaximumDeletions() {
		if (!useDeltaTable)
			return delta;
		
		return deltaTable[deltaTable.length-1][1];
	}
	
	public int getDeltaTotal(int clusterSize){
		if (!useDeltaTable)
			return delta;
		
		if (clusterSize >= deltaTable.length)
			return deltaTable[deltaTable.length-1][2];
		else
			return deltaTable[clusterSize][2];
	}
	
	public int getMaximumDelta(){
		if (!useDeltaTable)
			return delta;
				
		return deltaTable[deltaTable.length-1][2];
	}
	
	public int[][] getDeltaTable() {
		return deltaTable;
	}
	
	public boolean useDeltaTable() {
		return useDeltaTable;
	}

    private boolean useQuorum() {
        return (maxUncoveredGenomes != nrOfGenomes);
    }

	public int getMaxUncoveredGenomes() {
		return maxUncoveredGenomes;
	}
	
	public int getMinCoveredGenomes() {
		return minCoveredGenomes;
	}

	void increaseNrOfGenomes(){
		if (useQuorum()) {
			maxUncoveredGenomes++;
			nrOfGenomes++;
		}
	}
	
	void decreaseNrOfGenomes(){
		if (useQuorum()) {
			maxUncoveredGenomes--;
			nrOfGenomes--;
		}
	}

    /*public int maxUnusedSequences() {
        return nrOfGenomes-minCoveredGenomes;
    }*/
    
    public boolean useSingleReference() {
    	return singleReference;
    }
    

	public boolean searchRefInRef() {
		return refInRef;
	}

    public String toString() {
        return String.format("Delta: %1$d Size: %2$d",  delta, minClusterSize);
    }
}
