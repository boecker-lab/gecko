package gecko2.algo;

import java.util.List;

public class DeltaLocation implements Comparable<DeltaLocation> {
	private int genomeNr;
	private final int chrNr;
	private final int l;
	private final int r;
	private int distance;
	private final int size;
	private int hitCount;
	private double pValue;
	
	public DeltaLocation(int genomeNr, int chrNr, int l, int r,
			int distance, int size, int hitCount) {
		this.genomeNr = genomeNr;
		this.chrNr = chrNr;
		this.l = l;
		this.r = r;
		this.distance = distance;
		this.size = size;
		this.hitCount = hitCount;
		pValue = -1.0;
	}
	
	
	
	public DeltaLocation(DeltaLocation org) {
		this.genomeNr = org.genomeNr;
		this.chrNr = org.chrNr;
		this.l = org.l;
		this.r = org.r;
		this.distance = org.distance;
		this.size = org.size;
		this.hitCount = org.hitCount;
		this.pValue = org.pValue;
	}

	public int getGenomeNr() {
		return genomeNr;
	}
	
	public void setGenomeNr(int genomeNr) {
		this.genomeNr = genomeNr;
	}

	public int getChrNr() {
		return chrNr;
	}

	public int getL() {
		return l;
	}

	public int getR() {
		return r;
	}

	public int getDistance() {
		return distance;
	}

	public int size() {
		return size;
	}

	public int getHitCount() {
		return hitCount;
	}

	public double getpValue() {
		return pValue;
	}

	public void setpValue(double pValue) {
		this.pValue = pValue;
	}

	public void increaseHitCount() {
		hitCount++;
	}

	public void increaseDistance() {
		distance++;
	}
	
	public boolean isNested(DeltaLocation other){
		if (genomeNr != other.genomeNr)
			return false;
		
		if (chrNr != other.chrNr)
			return false;
		
		if (l < other.l)
			return false;
		
		if (l > other.r)
			return false;
		
		if (r > other.r)
			return false;
		
		return true;
	}

	public boolean isInheritableWithoutC(Chromosome chromosome, int delta, int c) {
		if (distance > delta)
			return false;
		if (chromosome.getGene(l-1) == c)
			return false;
		if (chromosome.getGene(r+1) == c)
			return false;
		return true;
	}

	@Override
	public int compareTo(DeltaLocation o) {
		int distDiff = this.distance - o.distance;
		if (distDiff != 0)
			return distDiff;

		int hitCountDiff = o.hitCount - this.hitCount;
		if (hitCountDiff != 0)
			return hitCountDiff;
		
		int chrNrDiff = this.chrNr - o.chrNr;
		if (chrNrDiff != 0)
			return chrNrDiff;
		
		int lDiff = this.l - o.l;
		if (lDiff != 0)
			return lDiff;
		
		return this.r - o.r;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chrNr;
		result = prime * result + distance;
		result = prime * result + hitCount;
		result = prime * result + l;
		result = prime * result + r;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeltaLocation other = (DeltaLocation) obj;
		if (chrNr != other.chrNr)
			return false;
		if (distance != other.distance)
			return false;
		if (hitCount != other.hitCount)
			return false;
		if (l != other.l)
			return false;
		if (r != other.r)
			return false;
		return true;
	}



	public boolean isFirstRefOcc(List<ListOfDeltaLocations> otherLocs) {
		for (int k=0; k<=genomeNr; k++) {
			for (DeltaLocation otherLoc : otherLocs.get(k)){
				if (otherLoc.getDistance() == 0) {
					if (k < genomeNr)
						return false;
					if (otherLoc.getChrNr() < chrNr)
						return false;
					if (otherLoc.getChrNr() == chrNr){
						if(otherLoc.getL() < l)
							return false;
						if(otherLoc.getL() == l && otherLoc.getR() < r)
							return false;
					}
				}
			}
		}
		return true;
	}

	public int leftMostEssentialChar(Chromosome chr) {
		if (l > r)
			return -1;
		for (int i = l; i<=r; i++){
			int c = chr.getNextOCC(i);
			if(c > r)
				return chr.getGene(i);
		}
		throw new RuntimeException("No left border found!");
	}
	
	public int rightMostEssentialChar(Chromosome chr) {
		if (l > r)
			return -1;
		for (int i = r; i>=l; i--){
			if(chr.getPrevOCC(i) < l)
				return chr.getGene(i);
		}
		throw new RuntimeException("No right border found!");
	}
	
	/**
	 * Checks if the DeltaLocation is contained in any other DeltaLocation in the list
	 * @param dLocList the list of other DeltaLocations
	 * @return true if contained
	 */
	public boolean isPartOfOtherDeltaLocation(List<DeltaLocation> dLocList) {
		for (DeltaLocation otherLoc : dLocList) {
			if (otherLoc.chrNr == this.chrNr) {
				if ((otherLoc.l >= this.l && otherLoc.l <= this.r) ||
						(otherLoc.r >= this.l && otherLoc.r <= this.r))
					return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "DeltaLocation [genomeNr=" + genomeNr + ", chrNr=" + chrNr
				+ ", l=" + l + ", r=" + r + ", distance=" + distance
				+ ", size=" + size + ", hitCount=" + hitCount + ", pValue="
				+ pValue + "]";
	}
}
