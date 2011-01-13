package gecko2.algorithm;

import java.io.Serializable;

/**
 * 
 * @author Leon Kuchenbecker <lkuchenb@inf.fu-berlin.de>
 *
 */
public class GeneCluster implements Serializable {
	
	public final static char TYPE_MEDIAN = 'm';
	public final static char TYPE_CENTER = 'c';
	public final static char TYPE_REFERENCE = 'r';

	private static final long serialVersionUID = -5371037483783752995L;

	int id;
	int[] genes;
	int size;
	private boolean match;
	private double bestPValue;
	private int minTotalDist;
	private GeneClusterOccurrence[] bestOccurrences;
	private GeneClusterOccurrence[] allOccurrences;
	private char type;
	// The index of the subsequence containing the reference genecluster
	private int refSeqIndex;
	
	public char getType() {
		return type;
	}
	
	public GeneClusterOccurrence[] getAllOccurrences() {
		return allOccurrences;
	}
	
	public double getBestPValue() {
		return bestPValue;
	}
	
	public int getMinTotalDist() {
		return minTotalDist;
	}
	
	public boolean isMatch() {
		return match;
	}
	
	public void setMatch(boolean match) {
		this.match = match;
	}
	
	public int getRefSeqIndex() {
		return refSeqIndex;
	}
	
	public GeneClusterOccurrence[] getOccurrences() {
		return bestOccurrences;
	}
		
	public GeneCluster(int id, 
			GeneClusterOccurrence[] bestOccurrences, 
			GeneClusterOccurrence[] allOccurrences, 
			int[] genes, 
			double bestPValue, 
			int minTotalDist, 
			int refSeqIndex,
			char type) 
	{
		match=true;
		//TODO check if this right
		size=0;
		if (allOccurrences!=null && allOccurrences.length!=0)
			for (Subsequence[] subSeqs : allOccurrences[0].getSubsequences())
				if (subSeqs.length>0) size++;
		
		this.bestPValue = bestPValue;
		this.bestOccurrences = bestOccurrences;
		this.allOccurrences = allOccurrences;
		this.refSeqIndex = refSeqIndex;
		this.minTotalDist = minTotalDist;
		this.type = type;

		this.genes = genes;
		this.id = id;
	}
	
	public int getId() {
		return id;
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
