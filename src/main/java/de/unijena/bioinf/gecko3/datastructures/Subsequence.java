package de.unijena.bioinf.gecko3.datastructures;

import java.io.Serializable;
import java.math.BigDecimal;

public class Subsequence implements Serializable {
	
	private static final long serialVersionUID = 2522802683054385603L;

	private final int start;
    private final int stop;
    private final int chromosome;
    private int dist;
    private final BigDecimal pValue;
	
	public void setDist(int dist) {
		this.dist = dist;
	}
	
	public int getDist() {
		return dist;
	}
	
	public int getStart() {
		return start;
	}
	
	public int getStop() {
		return stop;
	}
	
	public BigDecimal getpValue() {
		return pValue;
	}

	public Subsequence(int start, int stop, int chromosome, int dist, double pValueBase, int pValueExp) {
		this(start, stop, chromosome, dist, (new BigDecimal(pValueBase)).scaleByPowerOfTen(pValueExp));
	}
	
	public Subsequence(int start, int stop, int chromosome, int dist, BigDecimal pValue) {
		this.start = start;
		this.stop = stop;
		this.chromosome = chromosome;
		this.dist = dist;
		this.pValue = pValue;
	}
	
	public int getChromosome() {
		return chromosome;
	}
	
	public boolean isValid() {
		return (start<=stop);
	}

	@Override
	public String toString() {
		return "Subsequence [start=" + start + ", stop=" + stop
				+ ", chromosome=" + chromosome + ", dist=" + dist + ", pValue="
				+ pValue + "]";
	}
	
	
}
