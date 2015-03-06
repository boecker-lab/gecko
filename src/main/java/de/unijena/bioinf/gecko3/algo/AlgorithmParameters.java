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

import de.unijena.bioinf.gecko3.datastructures.Parameter;

import java.util.Arrays;

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

    private final boolean noStatistics;
	
	public AlgorithmParameters(Parameter p, int alphabetSize, int nrOfGenomes) {
		this(p.getDelta(), p.getDeltaTable(), p.getMinClusterSize(), p.getQ(), nrOfGenomes, alphabetSize, (p.getRefType() != Parameter.ReferenceType.allAgainstAll), p.searchRefInRef(), p.noStatistics());
		if (!p.useJavaAlgorithm())
			throw new IllegalArgumentException("Parameters not compatible to Java mode.");
	}
	
	private AlgorithmParameters(int delta, int[][] deltaTable, int minClusterSize, int q, int nrOfGenomes, int alphabetSize, boolean singleReference, boolean refInRef, boolean noStatistics) {
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
			if (isDeltaTableInvalid(deltaTable))
				throw new IllegalArgumentException("Invalid delta table!");
			
			this.deltaTable = new int[deltaTable.length][];
			for (int i=0; i<deltaTable.length; i++){
				this.deltaTable[i] = new int[Parameter.DELTA_TABLE_SIZE];
				System.arraycopy(deltaTable[i], 0, this.deltaTable[i], 0, Parameter.DELTA_TABLE_SIZE);
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
        this.noStatistics = noStatistics;
	}
	
	/**
	 * Checks if a valid delta table has been supplied
	 * @param deltaTable the delta table
	 * @return true if the table is valid, else false
	 */
	private static boolean isDeltaTableInvalid(int[][] deltaTable){
		int ins=0;
		int del=0;
		int total=0;
        for (int[] deltaTableColumn : deltaTable) {
            if (deltaTableColumn.length != Parameter.DELTA_TABLE_SIZE)
                return true;

            // allowed distance must not decrease
            if (deltaTableColumn[0] < ins)
                return true;
            ins = deltaTableColumn[0];

            if (deltaTableColumn[1] < del)
                return true;
            del = deltaTableColumn[1];

            // allowed total must be larger than insertions and deletions
            if (deltaTableColumn[2] < total || deltaTableColumn[2] < deltaTableColumn[1] || deltaTableColumn[2] < deltaTableColumn[0])
                return true;
            total = deltaTableColumn[2];


        }
		return false;
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

    public boolean noStatistics() {
        return noStatistics;
    }

    public String toString() {
		if (delta >= 0)
        	return String.format("Delta: %1$d Size: %2$d",  delta, minClusterSize);
		else
			return String.format("DeltaTable: %s Size: %2$d", Arrays.deepToString(deltaTable), minClusterSize);
    }
}
