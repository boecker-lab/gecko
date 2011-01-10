package gecko2.algorithm;

import java.io.Serializable;
public class GeneCluster implements Serializable {

	private static final long serialVersionUID = -5371037483783752995L;

	int id;
	Subsequence[] subsequences;
	int[] genes;
	int size;
	private boolean match;
	private double pValue;
	private int totalDist;
	// The index of the subsequence containing the reference genecluster
	private int refSeqIndex;
	
	public double getpValue() {
		return pValue;
	}
	
	public int getTotalDist() {
		return totalDist;
	}
	
	public boolean isMatch() {
		return match;
	}
	
	public void setMatch(boolean match) {
		this.match = match;
	}
	
	public GeneCluster() {
		match = true;
		// This constructer is used in the native methode of GeckoInstance!
		// TODO check if this is still true
	}
	
	public int getRefSeqIndex() {
		return refSeqIndex;
	}
		
	public GeneCluster(int id, int[][] subsequences, int[] genes, double pValue, int totalDist, int refSeqIndex) {
		System.err.println("REFCLUSTER "+refSeqIndex);
		match=true;
		size=0;
		this.pValue = pValue;
		this.refSeqIndex = refSeqIndex;
		this.totalDist = totalDist;
		this.subsequences = new Subsequence[subsequences.length];
		for (int i=0;i<subsequences.length;i++) {
			if (subsequences[i][0]>=0 && subsequences[i][1]>=0) size++;
			this.subsequences[i] = new Subsequence(subsequences[i][0],subsequences[i][1], subsequences[i][2], subsequences[i][3]);
		}
		this.genes = genes;
		this.id = id;
	}
	
//	public GeneCluster(int id, Subsequence[] subsequences, int[] genes) {
//		this.id = id;
//		this.subsequences = subsequences;
//		this.genes = genes;
//		match=true;
//	}
	
	public int getId() {
		return id;
	}
	
	public Subsequence[] getSubsequences() {
		return subsequences;
	}
	
	public void setSubsequences(Subsequence[] subsequences) {
		size=0;
		for (Subsequence s : subsequences)
			if (s.getStart()>=0 && s.getStop()>=0) size++;
		this.subsequences = subsequences;
	}
	
	public int[] getGenes() {
		return genes;
	}
	
	public void setGenes(int[] genes) {
		this.genes = genes;
	}
	
	public int getSize() {
		return size;
	}

}
