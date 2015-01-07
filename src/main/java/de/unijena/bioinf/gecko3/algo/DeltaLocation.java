/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.algo;

import java.util.List;

public class DeltaLocation implements Comparable<DeltaLocation> {
	private static final int MIN_HIT_COUNT = 2;
	
	private int genomeNr;
	private final int chrNr;
	private int l;
	private int r;
	private int distance;
	private int missingChars;
	private int additionalChars;
	private final int size;
	private int hitCount;
	private double pValue;
	private boolean valid;
	
	public DeltaLocation(int genomeNr, int chrNr, int l, int r,
			int distance, int missingChars, int additionalChars, int size, int hitCount, boolean valid) {
		this.genomeNr = genomeNr;
		this.chrNr = chrNr;
		this.l = l;
		this.r = r;
		this.distance = distance;
		this.missingChars = missingChars;
		this.additionalChars = additionalChars;
		this.size = size;
		this.hitCount = hitCount;
		this.pValue = -1.0;
		this.valid = valid;
	}
	
	public DeltaLocation(DeltaLocation org) {
		this.genomeNr = org.genomeNr;
		this.chrNr = org.chrNr;
		this.l = org.l;
		this.r = org.r;
		this.distance = org.distance;
		this.missingChars = org.missingChars;
		this.additionalChars = org.additionalChars;
		this.size = org.size;
		this.hitCount = org.hitCount;
		this.pValue = org.pValue;
		this.valid = org.valid;
	}
	
	public static DeltaLocation getReferenceLocation(int referenceGenomeNr,
			int referenceChromosomeNr, int l, int r, int patternSize) {
		return new DeltaLocation(referenceGenomeNr, referenceChromosomeNr, l, r, 0, 0, 0, patternSize, patternSize, true);
	}
	
	public static DeltaLocation getArtificialDeltaLocation(int genomeNr, int distance) {
		return new DeltaLocation(genomeNr, -1, 0, 0, distance, 0, 0, 0, 0, false);
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
	
	public void setL(int x){
		this.l = x;
	}
	
	public void setR(int x){
		this.r = x;
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
        distance--;
        additionalChars--;
	}

    /**
     * Increase the distance and missing chars by value
     */
	public void increaseDistance(int value) {
		distance+=value;
		missingChars+=value;
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
        if (c < 0)
            return true;
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

	/**
	 * Sets the valid flag for the delta location. 
	 * If not using delta table, always true.
	 * If using delta table, true if the additional chars, 
	 * missing chars and total distance are smaller than the 
	 * entries in the delta table for the given cluster size
	 * @param param the parameters
	 * @param clusterSize the cluster size
	 */
	public void checkForDeltaTableValidity(AlgorithmParameters param,
			int clusterSize) {
		valid = !param.useDeltaTable() || missingChars <= param.getDeltaDeletions(clusterSize) &&
                additionalChars <= param.getDeltaInsertions(clusterSize) &&
                distance <= param.getDeltaTotal(clusterSize);
	}

	/**
	 * Returns if the delta location is valid. You have to call @link checkForDeltaTableValidity first!
	 * @return if the delta location is valid
	 */
	public boolean isValid() {
		return valid && hitCount >= MIN_HIT_COUNT;
	}
}
