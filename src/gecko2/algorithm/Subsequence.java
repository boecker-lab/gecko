package gecko2.algorithm;

import java.io.Serializable;

public class Subsequence implements Serializable {
	
	private static final long serialVersionUID = 2522802683054385603L;

	int start;
	int stop;
	int chromosome;
	int dist;
	
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
	
	public void setStart(int start) {
		this.start = start;
	}
	
	public void setStop(int stop) {
		this.stop = stop;
	}
	
	public Subsequence(int start, int stop, int chromosome, int dist) {
		this.start = start;
		this.stop = stop;
		this.chromosome = chromosome;
		this.dist = dist;
	}
	
	public int getChromosome() {
		return chromosome;
	}
	
	public boolean isValid() {
		return (start<=stop);
	}
	
}
