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

import de.unijena.bioinf.gecko3.algo.util.IntArray;

import java.util.*;

/**
 * Chromosome stores the gene sequence of one chromosome.
 *
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
class Chromosome { 
    private final int nr;                 //nr of the chromosome in the genome
	private final int[] genes;
    private int[] prevOcc;
    private int[] nextOcc;
    private int[][] pos;
    private int[][] L;
    private int[][] R;
    private int[][] L_prime;
    private int[][] R_prime;

    private int[][] L_prime_OLD;
    private int[][] R_prime_OLD;

    private int delta;
    private int alphabetSize;

    /**
     * Constructs a new instance of a Chromosome from a List of Integers representing the gene homologies. Assigns a number to the chromosome and appends terminal character 0 to the chromosome. Calculates some additional members of the Chromosome, based on the alphabet size of the whole set of genomes and the parameters of the algorithm.
     *
     * @param genes        the list of Integers that represents the homologies of the genes.
     * @param number       the number of the chromosome.
     */
    public Chromosome(List<Integer> genes, int number) {
        this.genes = IntArray.newZeroTerminatedInstance(genes);
        this.nr = number;
        this.pos = null;
        this.prevOcc = null;
        this.nextOcc = null;
        this.L = null;
        this.R = null;
        this.L_prime = null;
        this.R_prime = null;
    }
    
    /**
     * Constructs a new instance of a Chromosome from a List of Integers representing the gene homologies.
     * Assigns a number to the chromosome and appends terminal character 0 to the chromosome.
     *
     * @param genes        the list of Integers that represents the homologies of the genes.
     * @param number       the number of the chromosome.
     */
    public Chromosome(int[] genes, int number, boolean zeroTerminated) {
    	if (zeroTerminated)
    		this.genes = IntArray.newZeroTerminatedInstance(genes);
    	else
    		this.genes = IntArray.newIntArray(genes, zeroTerminated);
        this.nr = number;
        this.pos = null;
        this.prevOcc = null;
        this.nextOcc = null;
        this.L = null;
        this.R = null;
        this.L_prime = null;
        this.R_prime = null;
    }

    public Chromosome(Chromosome other) {
		this.genes = Arrays.copyOf(other.genes, other.genes.length);
		this.nr = other.nr;
        this.pos = null;
        this.prevOcc = null;
        this.nextOcc = null;
        this.L = null;
        this.R = null;
        this.L_prime = null;
        this.R_prime = null;
	}

	/**
     * Initializes the Chromosome for the calculation of gene clusters.
     * @param alphabetSize the size of the complete alphabet.
     * @param maxDelta the maximum allowed distance
     */
    public void initializeForCalculation(int alphabetSize, int maxDelta) {
        this.alphabetSize = alphabetSize;
        this.pos = this.computePOS();

        this.L = new int[this.genes.length][];
        this.R = new int[this.genes.length][];
        this.L_prime = new int[this.genes.length][];
        this.R_prime = new int[this.genes.length][];
        this.L_prime_OLD = new int[this.genes.length][];
        this.R_prime_OLD = new int[this.genes.length][];
        for (int i = 0; i < this.genes.length; i++) {
            L[i] = new int[maxDelta + 2];
            R[i] = IntArray.newIntArray(maxDelta + 2, this.getEffectiveGeneNumber() + 1);
            L_prime[i] = new int[maxDelta + 2];
            R_prime[i] = IntArray.newIntArray(maxDelta + 2, this.getEffectiveGeneNumber() + 1);
            L_prime_OLD[i] = new int[maxDelta + 2];
            R_prime_OLD[i] = IntArray.newIntArray(maxDelta + 2, this.getEffectiveGeneNumber() + 1);
        }
        this.delta = maxDelta;

        this.prevOcc = this.computePrevOcc();
        this.nextOcc = this.computeNextOcc();
    }

    private int[][] computePOS() {
        List<LinkedList<Integer>> tmp = new ArrayList<>(alphabetSize+1);
        for (int i=0; i<=alphabetSize; i++) {
            tmp.add(null);
        }
        for (int i=1; i<=this.getEffectiveGeneNumber(); i++) {       // genes starts and ends with 0 that is not part of the genome
            if (genes[i] < 0)
                continue;
            if (tmp.get(genes[i])==null) {
                tmp.set(genes[i], new LinkedList<Integer>());
            }
            tmp.get(genes[i]).add(i);
        }
        int[][] newPos = new int[alphabetSize+1][];
        for (int i=0; i<=alphabetSize; i++) {
            if (tmp.get(i) == null) {
                newPos[i] = new int[0];
            }
            else {
                newPos[i] = new int[tmp.get(i).size()];
                int j = 0;
                for (Integer p : tmp.get(i)) {
                    newPos[i][j] = p;
                    j++;
                }
            }
        }
        return newPos;
    }
    
    private int[] computePrevOcc() {
        int[] occ = new int[alphabetSize + 1];//max(this.genes)+1];
        int[] newPrevOcc = new int[this.getEffectiveGeneNumber() + 2];//max(this.genes)+1];

        for (int i = 1; i <= this.getEffectiveGeneNumber(); i++) {
        	if(genes[i]>=0){
            	newPrevOcc[i] = occ[genes[i]];
            	occ[genes[i]] = i;
        	}
        }

        return newPrevOcc;
    }

    private int[] computeNextOcc() {
        int[] occ = IntArray.newIntArray(alphabetSize + 1, this.getEffectiveGeneNumber() + 1);
        int[] newNextOcc = IntArray.newIntArray(this.getEffectiveGeneNumber() + 2, this.getEffectiveGeneNumber() + 1);

        for (int i = this.getEffectiveGeneNumber() + 1; i >= 1; i--) {
        	if(genes[i]>=0){
        		newNextOcc[i] = occ[genes[i]];
        		occ[genes[i]] = i;
        	}
        }

        return newNextOcc;
    }

    /**
     * Returns the number of the chromosome.
     * @return the number of the chromosome.
     */
    public int getNr() {
        return nr;
    }

    /**
     * Returns the gene family id of the gene at position n in the chromosome.
     * @param n the position of the gene.
     * @return the id of the gene.
     */
    public int getGene(int n) {
        return genes[n];
    }

    /**
     * Returns the effective number of genes in the chromosome, counting -2 as one gene
     * @return the effective number of genes in the chromosome.
     */
    public int getEffectiveGeneNumber() {
        return (genes.length-2);   //genes begins and ends with a zero that is not part of the chromosome
    }

    /**
     * Returns the total gene number of the chromosome, taking into account negative genes,
     * e.g. -2 will add 2 genes.
     * @return the total number of genes
     */
    public int getTotalGeneNumber() {
        int geneNumber = genes.length-2;
        for (int i=1; i<genes.length-1; i++)
            if (genes[i] < -1)
                geneNumber -= genes[i]+1;
        return geneNumber;
    }

    /**
     * The number of different characters in the interval [l, r].
     * @param l the left border of the interval.
     * @param r the right border of the interval.
     * @return the number of different characters in the interval.
     */
    public int getNUM(int l, int r) {
        int numCount = 0;
        for (int i=l; i<=r; i++) {
            if (genes[i] < 0)
                numCount-=genes[i];
            else if (prevOcc[i] < l)
                numCount++;
        }
        return numCount;
    }

    /**
     * Checks if the character at position l-1 appears in the interval [l, r]
     * @param l
     * @param r
     * @return
     */
    public boolean previousInInterval(int l, int r) {
        return nextOcc[l-1] <= r;
    }

    /**
     * Checks if the character at position r+1 appears in the interval [l, r]
     * @param l
     * @param r
     * @return
     */
    public boolean nextInInterval(int l, int r) {
        return prevOcc[r+1] >= l;
    }

    /**
     * Tests if both intervals [l1, r1] and [l2, r2] contain identical characters.
     * @param l1
     * @param r1
     * @param l2
     * @param r2
     * @return
     */
    public boolean intervalContentIdentical(int l1, int r1, int l2, int r2) {
        if (l1 == l2) {
            int rightmostToTest = Math.max(r1, r2);
            int leftmostToTest = Math.min(r1, r2);
            for (int i=rightmostToTest; i>leftmostToTest; i--){
                if (prevOcc[i] < l1)
                    return false;
            }
        }

        if (r1 == r2) {
            int leftmostToTest = Math.min(l1, l2);
            int rightmostToTest = Math.max(l1, l2);
            for (int i=rightmostToTest; i<leftmostToTest; i++){
                if (nextOcc[i] > r1)
                    return false;
            }
        }

        // test larger interval first, can often stop if too many found
        int largeL, largeR, smallL, smallR;
        // maximum num in the smaller interval
        int maxNUM;
        if (r1-l1 >= r2-l2) {
            largeL = l1;
            largeR = r1;
            smallL = l2;
            smallR = r2;
            maxNUM = r2-l2+1;
        } else {
            largeL = l2;
            largeR = r2;
            smallL = l1;
            smallR = r1;
            maxNUM = r1-l1+1;
        }

        int numCount = 0;
        for (int i=largeL; i<=largeR; i++) {
            if (prevOcc[i] < largeL){
                numCount++;
                if (numCount > maxNUM)
                    return false;
            }
        }

        for (int i=smallL; i<=smallR; i++) {
            if (prevOcc[i] < smallL) {
                numCount--;
            } else {
                maxNUM--;
                if (numCount > maxNUM)
                    return false;
            }
        }
        return 0 == numCount;
    }

    /**
     * Returns the list of positions of the specified character c.
     * @param c the character for which the positions shall be returned.
     * @return the IntArray of positions of the character.
     */
    public int[] getPOS(int c) {
        return (c < 0) ? new int[0] : pos[c];
    }

    /**
     * Returns the next occurrence of the character at position index in the chromosome.
     * @param index the position of the character.
     * @return the position of the next occurrence of the character.
     */
    public int getNextOCC(int index) {
        return nextOcc[index];
    }

    /**
     * Returns the previous occurrence of the character at position index in the chromosome.
     * @param index the position of the character.
     * @return the position of the previous occurrence of the character.
     */
    public int getPrevOCC(int index) {
        return prevOcc[index];
    }

    /**
     * Computes the matrix L.
     * <br>L holds the max. maxDist positions of the next unmarked characters left of each position in the chromosome.
     * <br>rank is used to determine unmarked characters. An unmarked character must have a higher rank then the character at the current position.
     * @param rank the rank array of the current reference interval.
     */
    void computeL(Rank rank){
        resetL();                                           
        for (int i=1; i<=this.getEffectiveGeneNumber(); i++) {
            L[i][0] = i;                                 // no mismatch left of position is the position
            int d = 1;
            if (genes[i] <0)
                continue;

            for (int j=i-1; j>0 && d<=delta+1; j--) {                                 // search for unmarked char left of i
                if (genes[j] < 0) {
                    int k = Math.abs(genes[j]);
                    while(d<=delta+1 && k>0){
                        L[i][d]=j;
                        d++;
                        k--;
                    }
                    continue;
                }

                if (rank.getRank(genes[j]) > rank.getRank(genes[i])) {  // if unmarked char found
                    if(!this.previousInInterval(j+1, i)) {              // if unmarked char found for the 1st time
                        L[i][d] = j;
                        d++;
                    }
                }
            }
        }
    }

    /**
     * Calculate new values for L for positions with characters with rank smaller than c_old
     * @param rank the rank array of the current reference interval.
     * @param c_old must not be < 0
     */
    private void updateL_characterRankSmallerC_Old(Rank rank, int c_old){
        int lastOcc = 0;
        for (int j=1; j<=this.getEffectiveGeneNumber(); j++){
            if (genes[j] < 0)
                continue;

            if (lastOcc!=0) {                                                       // if c_old has already occurred in the list
                if (rank.getRank(genes[j]) < rank.getRank(c_old)) {             // if rank of character smaller than the new rank of c_old
                    for (int l=1; l<=delta+1; l++) {                              // test if entries for position i in array L change,
                        if  (this.getL(j, l) < lastOcc) {                            // because c_old is a new mismatch left of i
                            for (int p=delta+1; p>l; p--)
                                L[j][p] = this.getL(j, p-1);                   // shift all higher entries in L
                            L[j][l] = lastOcc;                                // and insert the new mismatch position
                            break;                                                  // no further changes in L[j] possible
                        }
                    }
                }
            }                                       // if c_old not yet read, L[j] cannot change
            if (this.getGene(j) == c_old) {
                lastOcc = j;                        // new occurrence of c_old read
            }
        }
    }

    /**
     * Calculate new values for L for positions with character c_old
     * @param rank the rank array of the current reference interval.
     * @param c_old must not be < 0
     */
    private void updateL_characterEqualsC_Old(Rank rank, int c_old){
        if (getPOS(c_old).length == 0)
            return;

        int[] c_old_L = new int[delta+2];

        for (int j=1; j<=this.getEffectiveGeneNumber(); j++) {
            if (genes[j]<0) {
                if (-genes[j] <= delta + 1)
                    System.arraycopy(c_old_L, 1, c_old_L, (-genes[j]+1), (c_old_L.length -(-genes[j]+1)));
                
                for (int k=Math.min(-genes[j], delta+1); k>0; k--) {
                	c_old_L[k] = j;
                }
            }
            else if(rank.getRank(genes[j]) > rank.getRank(c_old)) {
                int prevOcc = delta + 1;          // the sign is at last position per default

                for (int d=1; d<=delta+1; d++) {
                    if (genes[j] == genes[c_old_L[d]]) {    // search for the first entry in the neighbor array
                        prevOcc = d;                                        // that has the char chr[j], and store the position in prevOcc
                        break;
                    }
                }

                System.arraycopy(c_old_L, 1, c_old_L, 2, prevOcc - 1);      // shift all entries between position 1 and the old occurrence of c_old
                c_old_L[1] = j;
            }

            if (genes[j] == c_old) {                                  // if occurrence of the c_old found
                System.arraycopy(c_old_L, 1, L[j], 1, delta + 1);   // replace all entries in L[j] with the entries in c_old_L
            }
        }
    }

    /**
     * Updates or computes the matrix L.
     * <br>L holds the max. maxDist positions of the next unmarked characters left of each position in the chromosome.
     * <br>rank is used to determine unmarked characters. An unmarked character must have a higher rank then the character at the current position.
     * @param rank the rank array of the current reference interval.
     * @param c_old the character that was last added to the reference interval. Must not be < 0.
     */
    void updateL(Rank rank, int c_old){
        updateL_characterRankSmallerC_Old(rank, c_old);
        updateL_characterEqualsC_Old(rank, c_old);
    }

    /**
     * Resets the matrix L to its default value 0.
     */
    private void resetL () {
        for (int i=0; i<L.length; i++) {
            IntArray.reset(L[i], 0);
        }
    }

    /**
     * Returns the next position to the left of position pos of the interval with diff unmarked characters.
     * @param pos the position based on which the unmarked position is determined.
     * @param diff the number of unmarked characters between pos and the returned value.
     * @return the position of the unmarked character.
     */
    public int getL (int pos, int diff) {
        return L[pos][diff];
    }

    /**
     * Computes the matrix R
     * <br>R holds the max. maxDist positions of the next unmarked characters right of each position in the chromosome.
     * <br>rank is used to determine unmarked characters. An unmarked character must have a higher rank then the character at the current position.
     * @param rank the rank array of the current reference interval.
     */
    void computeR(Rank rank){
    	resetR();
        for (int i=1; i<=this.getEffectiveGeneNumber(); i++) {
            R[i][0] = i;                          // first mismatch right of position is the position
            int d = 1;
            
            if(genes[i]<0)
                continue;

            for (int j=i+1; j<=this.getEffectiveGeneNumber() && d<=delta+1; j++) {                   // search for unmarked char right of i
                if (genes[j]<0 ) {
                    int k = 0;
                    while(d<=delta+1 && k<Math.abs(genes[j])){
                        R[i][d] = j;
                        d++;
                        k++;
                    }
                    continue;
                }
                if (rank.getRank(genes[j]) > rank.getRank(genes[i])) {  // if unmarked char found
                    if(!this.nextInInterval(i, j-1)) {                         // if unmarked char found for the 1st time
                        R[i][d] = j;
                        d++;
                    }
                }
            }
    	}
    }

    /**
     * Calculate new values for R for positions with characters with rank smaller than c_old
     * @param rank the rank array of the current reference interval.
     * @param c_old must not be < 0
     */
    private void updateR_characterRankSmallerC_Old(Rank rank, int c_old){
        int lastOcc = this.getEffectiveGeneNumber()+1;

        for (int j=this.getEffectiveGeneNumber(); j>=1; j--) {
            if (genes[j] < 0)
                continue;
            if (lastOcc != this.getEffectiveGeneNumber() + 1) {                                            // if c_old has already occurred in the list
                if (rank.getRank(genes[j]) < rank.getRank(c_old)) {       // if rank of character smaller than the new rank of c_old
                    for (int l = 1; l <= delta + 1; l++) {                              // test if entries for position i in array R change,
                        if (R[j][l] > lastOcc) {                            // because c_old is a new mismatch left of i
                            for (int p = delta + 1; p > l; p--)
                                R[j][p] = R[j][p - 1];                   // shift all higher entries in R
                            R[j][l] = lastOcc;                                // and insert the new mismatch position
                            break;                                                  // no further changes in R[j] possible
                        }
                    }
                }
            }                                       // if c_old not yet read, R[j] cannot change
            if (genes[j] == c_old) {
                lastOcc = j;                        // new occurrence of c_old read
            }
        }
    }

    /**
     * Calculate new values for R for positions with character c_old
     * @param rank the rank array of the current reference interval.
     * @param c_old must not be < 0
     */
    private void updateR_characterEqualsC_Old(Rank rank, int c_old){
        int[] c_old_R = IntArray.newIntArray(delta+2, this.getEffectiveGeneNumber()+1);

        for (int j=this.getEffectiveGeneNumber(); j>=1; j--) {
            if (genes[j]<0) {
                if (-genes[j] <= delta + 1)
                    System.arraycopy(c_old_R, 1, c_old_R, (-genes[j]+1),(c_old_R.length-(-genes[j]+1)));

                for (int k=Math.min(-genes[j], delta+1); k>0; k--) {
                    c_old_R[k] = j;
                }
            } else if (rank.getRank(genes[j]) > rank.getRank(c_old)) {
                int prevOcc = delta + 1;          // the sign is at last position per default

                for (int p=1; p<=delta+1; p++) {
                    if (genes[j] >= 0 && genes[j] == genes[c_old_R[p]]) {    // search for the first entry in the neighbor array
                        prevOcc = p;                                        // that has the char chr[j], and store the position in prevOcc
                        break;
                    }
                }

                System.arraycopy(c_old_R, 1, c_old_R, 2, prevOcc - 1);    // shift all entries between position 1 and the old occurrence of c_old
                c_old_R[1] = j;
            }

            if (genes[j] == c_old) {                                  // if occurrence of the c_old found
                System.arraycopy(c_old_R, 1, R[j], 1, delta + 1);   // replace all entries in R[j] with the entries in c_old_R
            }
        }
    }

    /**
     * Updates or computes the matrix R.
     * <br>R holds the max. maxDist positions of the next unmarked characters right of each position in the chromosome.
     * <br>rank is used to determine unmarked characters. An unmarked character must have a higher rank then the character at the current position.
     * @param rank the rank array of the current reference interval.
     * @param c_old the character that was last added to the reference interval. Must not be < 0
     */
    void updateR(Rank rank, int c_old){
        updateR_characterRankSmallerC_Old(rank, c_old);
        updateR_characterEqualsC_Old(rank, c_old);
    }

    /**
     * Resets the matrix R to its default value chr.size()+1.
     */
    private void resetR () {
        for (int i=0; i<R.length; i++) {
            IntArray.reset(R[i], this.getEffectiveGeneNumber()+1);
        }
    }

    /**
     * Returns the next position to the right of position pos of the interval with diff unmarked characters.
     * @param pos the position based on which the unmarked position is determined.
     * @param diff the number of unmarked characters between pos and the returned value.
     * @return the position of the unmarked character.
     */
    public int getR (int pos, int diff) {
        return R[pos][diff];
    }

    public void updateL_R_prime_OLD(Rank rank, int leftBorder, int c_old) {
        updateL_prime_OLD(rank, leftBorder, c_old);
        updateR_prime_OLD(rank, leftBorder, c_old);
    }

    private void comparePrimes(int[][] expected, int[][] actual, String type) {
        for (int i=0; i<expected.length; i++) {
            for (int j=0; j<expected[i].length; j++)
                if (expected[i][j] != actual[i][j])
                    System.out.println(type);
        }
    }

    private void updateL_prime_OLD(Rank rank, int leftBorder, int c_old) {
        int maxUpdateRank;
        if (c_old<0){
            maxUpdateRank = alphabetSize;
        } else {
            maxUpdateRank = (leftBorder==1) ? alphabetSize+1 : rank.getRank(c_old);
        }
        for(int j=1;j<=this.getEffectiveGeneNumber();j++){
            if(genes[j]<0)
                continue;
            if(rank.getRank(genes[j])<=maxUpdateRank) {
                for(int d=1; d<=delta+1; d++) {
                    for(int l=this.L[j][d]+1; l<=j; l++) {
                        if(genes[l] >= 0 && rank.getRank(genes[l]) <= rank.getRank(genes[j])) {
                            L_prime_OLD[j][d] = l;
                            break;
                        }
                    }
                }
            }
        }
        //comparePrimes(L_prime_OLD, L_prime, "L");
    }

    private void updateR_prime_OLD(Rank rank, int leftBorder, int c_old) {
        int maxUpdateRank;
        if (c_old<0){
            maxUpdateRank = alphabetSize;
        } else {
            maxUpdateRank = (leftBorder==1) ? alphabetSize+1 : rank.getRank(c_old);
        }
        for(int j=1;j<=this.getEffectiveGeneNumber();j++){
            if (genes[j]<0)
                continue;
            if(rank.getRank(genes[j])<=maxUpdateRank) {
                for(int d=1; d<=delta+1; d++) {
                    for(int l=this.R[j][d]-1; l>=j; l--) {
                        if(genes[l] >= 0 && rank.getRank(genes[l]) <= rank.getRank(genes[j])) {
                            R_prime_OLD[j][d] = l;
                            break;
                        }
                    }
                }
            }
        }
        //comparePrimes(R_prime_OLD, R_prime, "R");
    }

    /**
     * Helper class for the computation of the next RankedNeighbors
     */
    private static class RankedNeighbors {
        final int position;
        final int[] neighbors;
        int arrayIndex;
        int lastRank;

        RankedNeighbors(int position, int c, int delta, Rank rank, int defaultValue){
            this.position = position;
            this.neighbors = IntArray.newIntArray(delta+1, defaultValue);
            arrayIndex = 0;
            lastRank = rank.getRank(c);
        }

        public boolean testAndAddPosition(int position, int rank) {
            if (rank < lastRank) {
                neighbors[arrayIndex] = position;
                arrayIndex++;
            }
            return arrayIndex == neighbors.length;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RankedNeighbors that = (RankedNeighbors) o;

            return (position == that.position);
        }

        @Override
        public int hashCode() {
            return position;
        }
    }

    /**
     * Resets the matrix L_prime to its default value 0.
     */
    private void resetL_prime () {
        for (int i=0; i<L_prime.length; i++) {
            IntArray.reset(L_prime[i], 0);
        }
    }

    void computeL_prime(Rank rank) {
        resetL_prime();
        for(int j=1;j<=this.getEffectiveGeneNumber();j++){
            if(genes[j]<0)
                continue;
            int last_match = j+1;
            for(int d=1; d<=delta+1; d++) {
                boolean notFound = true;
                for(int l=L[j][d]+1; l<last_match && notFound; l++) {
                    if(genes[l] >= 0 && rank.getRank(genes[l]) <= rank.getRank(genes[j])) {
                        L_prime[j][d] = l;
                        notFound = false;
                    }
                }
                if (notFound)
                    L_prime[j][d] = L_prime[j][d-1];
                last_match = L[j][d]+1;
            }
        }
    }

    private Map<Integer, int[]> getNextRankingsRightOfChar(Rank rank, int c){
        Map<Integer, int[]> lowerRankedNeighbors = new HashMap<>();
        Set<RankedNeighbors> updatingNeighbors = new HashSet<>();
        int oldRank = rank.getRank(c);

        for (int i=1; i<=getEffectiveGeneNumber(); i++) {
            if (genes[i] < 0)
                continue;
            if (genes[i] == c) {
                updatingNeighbors.add(new RankedNeighbors(i, c, delta, rank, getEffectiveGeneNumber()+1));
            } else if (rank.getRank(genes[i]) < oldRank) {
                Iterator<RankedNeighbors> iterator = updatingNeighbors.iterator();
                while (iterator.hasNext()){
                    RankedNeighbors neighbors = iterator.next();
                    if (neighbors.testAndAddPosition(i, rank.getRank(genes[i]))) {
                        lowerRankedNeighbors.put(neighbors.position, neighbors.neighbors);
                        iterator.remove();
                    }
                }
            }
        }
        for (RankedNeighbors neighbors : updatingNeighbors)
            lowerRankedNeighbors.put(neighbors.position, neighbors.neighbors);
        return lowerRankedNeighbors;
    }

    int[] getNextRankingsRightOfPos(Rank rank, int pos){
        int[] lowerRankingNeighbors = IntArray.newIntArray(delta+1,  getEffectiveGeneNumber()+1);

        int index = 0;
        int lastRank = rank.getRank(genes[pos]);
        for (int i=pos+1; i<=getEffectiveGeneNumber(); i++) {
            if (rank.getRank(genes[i]) < lastRank) {
                lowerRankingNeighbors[index] = i;
                lastRank = rank.getRank(genes[i]);
                index++;
                if (index == lowerRankingNeighbors.length)
                    break;
            }
        }
        return lowerRankingNeighbors;
    }

    /**
     * Updates or computes the matrix L_prime.
     * <br>L_prime holds the max. maxDist positions of the next characters right of the corresponding L.
     * <br>rank is used to determine marked characters. An marked character must have a lower or equal rank then the character at the current position.
     * @param rank the rank array of the current reference interval.
     * @param c_old the character that was last added to the reference interval. Must not be < 0.
     */
    void updateL_prime(Rank rank, int c_old){
        updateL_primeCharacterRankSmallerC_Old(rank, c_old);
        updateL_primeCharacterEqualsC_Old(rank, c_old);
    }

    private void updateL_primeCharacterRankSmallerC_Old(Rank rank, int c_old) {
        Map<Integer, int[]> lowerRankedNeighbors = getNextRankingsRightOfChar(rank, c_old);

        for (int i=1;i<=this.getEffectiveGeneNumber(); i++){
            if (genes[i]<0)
                continue;
            if (rank.getRank(genes[i]) < rank.getRank(c_old)) {
                for (int d=1; d<=delta+1; d++) {
                    int[] newPossitions = null;
                    if (genes[L_prime[i][d]] == c_old)
                        newPossitions = lowerRankedNeighbors.get(L_prime[i][d]);
                    else if (L[i][d] >= L_prime[i][d]) {
                        for (int d_j = d; d >= 1; d_j--) {
                            if (genes[L[i][d_j]] == c_old) {
                                newPossitions = lowerRankedNeighbors.get(L[i][d_j]);
                                break;
                            }
                        }
                    }
                    if (newPossitions != null) {
                        for (int j=0; j< newPossitions.length; j++){
                            if (rank.getRank(genes[newPossitions[j]]) <= rank.getRank(genes[i])) {
                                L_prime[i][d] = newPossitions[j];
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateL_primeCharacterEqualsC_Old(Rank rank, int c_old) {
        int[] pos = getPOS(c_old);

        for (int pos_c_old : pos) {
            int last_match = pos_c_old + 1;
            for(int d=1; d<=delta+1; d++) {
                boolean notFound = true;
                for(int l=L[pos_c_old][d]+1; l<last_match && notFound; l++) {
                    if(genes[l] >= 0 && rank.getRank(genes[l]) <= rank.getRank(c_old)) {
                        L_prime[pos_c_old][d] = l;
                        notFound = false;
                    }
                    if (notFound)
                        L_prime[pos_c_old][d] = L_prime[pos_c_old][d-1];
                    last_match = L[pos_c_old][d]+1;
                }
            }
        }
    }

    /**
     * Resets the matrix R_prime to its default value chr.size()+1.
     */
    private void resetR_prime () {
        for (int i=0; i<R_prime.length; i++) {
            IntArray.reset(R_prime[i], getEffectiveGeneNumber()+1);
        }
    }

    void computeR_prime(Rank rank) {
        resetR_prime();
        for(int j=1;j<=getEffectiveGeneNumber();j++){
            if (genes[j]<0)
                continue;
            int lastEnd = j-1;  // we only need to scan before the last scanned position for different d
            for(int d=1; d<=delta+1; d++) {
                boolean notFound = true;
                for (int l = R[j][d] - 1; l > lastEnd && notFound; l--) {
                    if (genes[l] >= 0 && rank.getRank(genes[l]) <= rank.getRank(genes[j])) {
                        R_prime[j][d] = l;
                        notFound = false;
                    }
                }
                if (notFound)
                    R_prime[j][d] = R_prime[j][d - 1];
                lastEnd = R[j][d] - 1;
            }
        }
    }

    private Map<Integer, int[]> getNextRankingsLeftOfChar(Rank rank, int c){
        Map<Integer, int[]> lowerRankedNeighbors = new HashMap<>();
        Set<RankedNeighbors> updatingNeighbors = new HashSet<>();
        int oldRank = rank.getRank(c);

        for (int i=getEffectiveGeneNumber(); i>=1; i--) {
            if (genes[i] < 0)
                continue;
            if (genes[i] == c) {
                updatingNeighbors.add(new RankedNeighbors(i, c, delta, rank, 0));
            } else if (rank.getRank(genes[i]) < oldRank) {
                Iterator<RankedNeighbors> iterator = updatingNeighbors.iterator();
                while (iterator.hasNext()){
                    RankedNeighbors neighbors = iterator.next();
                    if (neighbors.testAndAddPosition(i, rank.getRank(genes[i]))) {
                        lowerRankedNeighbors.put(neighbors.position, neighbors.neighbors);
                        iterator.remove();
                    }
                }
            }
        }
        for (RankedNeighbors neighbors : updatingNeighbors)
            lowerRankedNeighbors.put(neighbors.position, neighbors.neighbors);
        return lowerRankedNeighbors;
    }

    int[] getNextRankingsLeftOfPos(Rank rank, int pos){
        int[] lowerRankingNeighbors = IntArray.newIntArray(delta + 1, 0);

        int index = 0;
        int lastRank = rank.getRank(genes[pos]);
        for (int i=pos-1; i>=1; i--) {
            if (rank.getRank(genes[i]) < lastRank) {
                lowerRankingNeighbors[index] = i;
                lastRank = rank.getRank(genes[i]);
                index++;
                if (index == lowerRankingNeighbors.length)
                    break;
            }
        }
        return lowerRankingNeighbors;
    }

    /**
     * Updates or computes the matrix R_prime.
     * <br>R_prime holds the max. maxDist positions of the next characters left of the corresponding R.
     * <br>rank is used to determine marked characters. An marked character must have a lower or equal rank then the character at the current position.
     * @param rank the rank array of the current reference interval.
     * @param c_old the character that was last added to the reference interval. Must not be < 0.
     */
    void updateR_prime(Rank rank, int c_old){
        updateR_primeCharacterRankSmallerC_Old(rank, c_old);
        updateR_primeCharacterEqualsC_Old(rank, c_old);
    }

    private void updateR_primeCharacterRankSmallerC_Old(Rank rank, int c_old) {
        Map<Integer, int[]> lowerRankedNeighbors = getNextRankingsLeftOfChar(rank, c_old);

        for (int i=this.getEffectiveGeneNumber();i>=1; i--){
            if (genes[i]<0)
                continue;
            if (rank.getRank(genes[i]) < rank.getRank(c_old)) {
                for (int d=1; d<=delta+1; d++) {
                    // check if needs to be updated and get possible update positions
                    int[] newPossitions = null;
                    if (genes[R_prime[i][d]] == c_old)
                        newPossitions = lowerRankedNeighbors.get(R_prime[i][d]);
                    else if (R[i][d] <= R_prime[i][d]) {
                        for (int d_j = d; d >= 1; d_j--) {
                            if (genes[R[i][d_j]] == c_old) {
                                newPossitions = lowerRankedNeighbors.get(R[i][d_j]);
                                break;
                            }
                        }
                    }

                    if (newPossitions != null) {
                        for (int j=0; j< newPossitions.length; j++){
                            if (rank.getRank(genes[newPossitions[j]]) <= rank.getRank(genes[i])) {
                                R_prime[i][d] = newPossitions[j];
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateR_primeCharacterEqualsC_Old(Rank rank, int c_old) {
        int[] pos = getPOS(c_old);

        for (int pos_c_old : pos) {
            int lastEnd = pos_c_old-1;  // we only need to scan before the last scanned position for different d
            for(int d=1; d<=delta+1; d++) {
                boolean notFound = true;
                for (int l = R[pos_c_old][d] - 1; l > lastEnd && notFound; l--) {
                    if (genes[l] >= 0 && rank.getRank(genes[l]) <= rank.getRank(c_old)) {
                        R_prime[pos_c_old][d] = l;
                        notFound = false;
                    }
                }
                if (notFound)
                    R_prime[pos_c_old][d] = R_prime[pos_c_old][d - 1];
                lastEnd = R[pos_c_old][d] - 1;
            }
        }
    }

    public int getL_prime (int pos, int diff) {
        if (L_prime[pos][diff] != L_prime_OLD[pos][diff])
            throw new RuntimeException("L");
        return L_prime[pos][diff];
    }
    
    public int getR_prime (int pos, int diff) {
        if (R_prime[pos][diff] != R_prime_OLD[pos][diff])
            throw new RuntimeException("R");
        return R_prime[pos][diff];
    }

    @Override public String toString() {
        return String.format("Chromosome Nr: %1$d => %2$s", nr, Arrays.toString(genes));
    }

    /**
     * Removes all fields used only in computation of gene clusters, so they can be garbage collected
     */
    public void removeCalculationFields() {
        this.pos = null;

        this.L = null;
        this.R = null;
        this.L_prime = null;
        this.R_prime = null;

        this.prevOcc = null;
        this.nextOcc = null;
    }
}
