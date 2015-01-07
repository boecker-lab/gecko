/*
 * Copyright 2014 Sascha Winter
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

package de.unijena.bioinf.gecko3.datastructures;

public class Parameter {

    public enum OperationMode {
        reference('r'), median('m'), center('c');

        private final char shortForm;

        private OperationMode(char c) {
            shortForm = c;
        }

        public char getCharMode() {
            return shortForm;
        }

        public static OperationMode getOperationModeFromChar(char c) {
            switch (c) {
                case 'r':
                    return reference;
                case 'm':
                    return median;
                case 'c':
                    return center;
                default:
                    throw new IllegalArgumentException("Only 'r', 'c' or 'm' are supported!");
            }
        }

        public static OperationMode[] getSupported() {
            return new OperationMode[]{OperationMode.reference};
        }
    }

    public enum ReferenceType {
        allAgainstAll('a', "all against all"), genome('g', "fixed genome"), cluster('c', "manual cluster");

        private final char shortForm;
        private final String text;

        private ReferenceType(char c, String text) {
            shortForm = c;
            this.text = text;
        }

        public char getCharMode() {
            return shortForm;
        }

        public String toString() {
            return text;
        }

        public static ReferenceType getReferenceTypeFromChar(char c) {
            switch (c) {
                case 'a':
                    return allAgainstAll;
                case 'd':  // old default case, for compatibility reasons
                    return allAgainstAll;
                case 'g':
                    return genome;
                case 'c':
                    return cluster;
                default:
                    throw new IllegalArgumentException("Only 'r', 'c' or 'm' are supported!");
            }
        }

        public static ReferenceType[] getSupported() {
            return values();
            //return new ReferenceType[]{ReferenceType.allAgainstAll, ReferenceType.genome};
        }
    }

    public enum DeltaTable {
        highly_conserved("Default"), low_conserved("Low conserved"), relaxed("Relaxed"), lichtheimia("Lichtheimia"), lichtheimia_inner("Lichtheimia Inner"), statistic_paper("Statistics"), test_five_proteobacter("Test five proteobacter");

        private final String text;

        private DeltaTable(String text) {
            this.text = text;
        }

        /**
         * Returns the distance tables for the given task, an array of int arrays
         * The position in the array of arrays gives the minimum size the parameters apply to.
         * The contained int array contains maximum added genes, maximum lost genes, maximum sum of gain and loss.
         * @return
         */
        public int[][] getDeltaTable() {
            switch (this) {
                case highly_conserved:
                    return new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 0, 1}, {2, 1, 2}, {3, 2, 3}, {4, 2, 4}, {5, 3, 5}, {6, 3, 6}};
                case low_conserved:
                    return new int[][]{{0, 0, 0}, {0, 0, 0}, {1, 0, 1}, {1, 1, 1}, {2, 1, 2}, {3, 2, 3}, {3, 3, 3}, {4, 3, 4}, {5, 3, 5}, {6, 4, 6}};
                case relaxed:
                    return new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 0, 1}, {1, 1, 1}, {2, 1, 2}, {3, 2, 3}, {4, 2, 4}, {6, 3, 6}, {8, 4, 8}, {10, 5, 10}};
                case lichtheimia:
                    return new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 1, 1}, {2, 2, 2}, {3, 3, 3}, {4, 4, 4}};
                case lichtheimia_inner:
                    return new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 1, 1}, {2, 2, 2}};
                case statistic_paper:
                    return new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {1, 1, 1}, {2, 2, 2}, {3, 3, 3}, {5, 5, 5}};
                case test_five_proteobacter:
                    return new int[][]{{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {1,1,1}, {2,2,2}, {3,3,3}, {5,5,5}};

            }
            throw new RuntimeException("Should not happen!");
        }

        public int getMinimumSize() {
            switch (this) {
                case highly_conserved:
                    return 3;
                case low_conserved:
                    return 3;
                case relaxed:
                    return 2;
                case lichtheimia:
                    return 3;
                case lichtheimia_inner:
                    return 3;
                case statistic_paper:
                    return 4;
                case test_five_proteobacter:
                    return 4;

            }
            throw new RuntimeException("Should not happen!");
        }

        /**
         * Wrapper method for values() that only returns the currently supported subset of values
         * @return the supported subset of values
         */
        public static DeltaTable[] getSupported() {
            // Support all values
            //return values();

            // Support only a subset of values
            return new DeltaTable[]{highly_conserved, relaxed, statistic_paper};
        }

        public static DeltaTable getDefault() {
            return highly_conserved;
        }

        @Override
        public String toString(){
            return text;
        }
    }
	
    public final static int DELTA_TABLE_SIZE = 3;

	private final int delta;
	private final int[][] deltaTable;
	private final int minClusterSize;
	private final int q;
	private final ReferenceType refType;
	private final OperationMode operationMode;
	private final boolean searchRefInRef;
    private int alphabetSize;
	
	public Parameter(int delta, int minClusterSize, int q, OperationMode operationMode, ReferenceType refType) {
		this(delta, minClusterSize, q, operationMode, refType, false);
	}
	
	public Parameter(int delta, int minClusterSize, int q, OperationMode operationMode, ReferenceType refType, boolean searchRefInRef) {
		if (searchRefInRef && operationMode != OperationMode.reference)
			throw new IllegalArgumentException("Searching the reference occurrence in the reference genome is only compatible with reference mode!");
		if (delta < 0)
            throw new IllegalArgumentException("Delta has to be >= 0!");

        this.delta = delta;
		this.deltaTable = null;
		this.minClusterSize = minClusterSize;
		this.q = q;
		this.operationMode = operationMode;
		this.refType = refType;
		this.alphabetSize = -1;
		this.searchRefInRef = searchRefInRef;
	}
	
	public Parameter(int[][] deltaTable, int minClusterSize, int q, OperationMode operationMode, ReferenceType refType) {
		this(deltaTable, minClusterSize, q, operationMode, refType, false);
	}
	
	public Parameter(int[][] deltaTable, int minClusterSize, int q, OperationMode operationMode, ReferenceType refType, boolean searchRefInRef) {
		if (operationMode != OperationMode.reference)
			throw new IllegalArgumentException("Delta table is only compatible with reference mode!");

        if (deltaTable==null || deltaTable.length<1)
            throw new IllegalArgumentException("Delta table must not be null or empty!");
		
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
		this.operationMode = operationMode;
		this.refType = refType;
		this.alphabetSize = -1;
		this.searchRefInRef = searchRefInRef;
    }
	
	public ReferenceType getRefType() {
		return refType;
	}

    public char getRefTypeChar() {
        return refType.getCharMode();
    }
	
	public OperationMode getOperationMode() {
		return operationMode;
	}

    public char getOperationModeChar() {
        return operationMode.getCharMode();
    }
	
	public int getDelta() {
		return delta;
	}
	
	public int[][] getDeltaTable() {
		return deltaTable;
	}

    public boolean useDeltaTable() {
        return delta == -1;
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
	
	public boolean searchRefInRef() {
		return searchRefInRef;
	}
	
	public boolean useJavaAlgorithm() {
		return operationMode == OperationMode.reference;
	}
}
