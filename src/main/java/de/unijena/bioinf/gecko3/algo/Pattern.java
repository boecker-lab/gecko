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

import java.util.Arrays;

/**
 * The Pattern class implements a data structure to store the information about a pattern.
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
class Pattern {
    private final int refGenomeNr;                                // number of the reference genome
    private final Chromosome refChr;                              // the reference chromosome
    private final int l;                                          // left boarder of the reference interval
    private int r;                                          // right boarder of the reference interval
    private int lastChar;                                   // last added character
    private int pSize;                                      // the number of characters in the pattern
    private final int[] occ;                           // occurrence array of the pattern  //TODO either a BoolArray or store the number of occurrences and not just if it occurs.
    private final int[] minDist;                       // minimal distance between the pattern and each sequence
    private final int[] maxRemDist;                    // maximum distance left for occurrences in each sequence
    
    /**
     * Generates the data structure to store information about a pattern.
     * @param alphabetSize the size of the alphabet.
     * @param K the number of genomes in the data set.
     * @param param the parameters the algorithm is started with.
     * @param refGenomeNr the number of the genome the pattern is located on.
     * @param refChr the chromosome the pattern is located on.
     * @param l the left border of the pattern.
     */
    public Pattern(int alphabetSize, int K, AlgorithmParameters param, int refGenomeNr, Chromosome refChr, int l) {
        this.refGenomeNr = refGenomeNr;
        this.refChr = refChr;
        this.l = l;
        this.r = -1;
        this.lastChar = 0;
        this.pSize = 0;
        this.occ = new int[alphabetSize+1];
        this.minDist = new int[K];
        this.maxRemDist = IntArray.newIntArray(K, param.getMaximumDelta());
    }

    /**
     * Returns the left border of the pattern.
     * @return the left border of the pattern.
     */
    public int getLeftBorder() {
        return l;
    }

    /**
     * Returns the right border of the pattern.
     * @return the right border of the pattern.
     */
    public int getRightBorder() {
        return r;
    }

    /**
     * Returns the number of occurrences of c in the pattern.
     * @param c the character the occurrence is checked of.
     * @return the number of times c occurred.
     */
    int nrOccurrences(int c) {
        return occ[c];
    }

    /**
     * Returns the number of the reference genome the pattern is located on.
     * @return the number of the reference genome.
     */
    public int getRefGenomeNr() {
        return refGenomeNr;
    }
    
    public int getLastChar() {
		return lastChar;
	}
    
	public int getSize() {
		return pSize;
	}
	
	public int getRefChromosomeNr() {
		return refChr.getNr();
	}

	/**
     * Adds a new character to the pattern.
     * @param c The int to be added to the pattern.
     * @param r the new right boarder of the pattern.
     */
    private void addToPattern(int c, int r) {
        this.lastChar = c;
        this.r = r;
        if (c>=0) {
            occ[c]++;
            pSize++;
        }
        else
            pSize-=c;
        IntArray.increaseAll(minDist);
        minDist[refGenomeNr]--;
        IntArray.reset(maxRemDist, -1);
    }

    /**
     * Expands a Pattern to the next reference Sequence from the chromosome.
     * @param i the previous right border of pattern that is expanded to the right.
     * @return true if the pattern could be expanded successfully.
     */
    public boolean updateToNextI_ref(int i) {
        if (i > refChr.getEffectiveGeneNumber())                                 // If end of sequence reached
            return false;
        int c = refChr.getGene(i);
        if (c >= 0 && refChr.getGene(getLeftBorder() - 1) == c)          // If pattern no longer left maximal
            return false;

        while (i < refChr.getEffectiveGeneNumber() && refChr.getGene(i + 1) > 0
                && (nrOccurrences(refChr.getGene(i + 1)) > 0 || refChr.getGene(i + 1) == c)) // expand until the pattern is right maximal
            i++;

        addToPattern(c, i);
        return true;
    }
    
    public ListOfDeltaLocations computeNewOptimalDeltaLocations(Genome genome, AlgorithmParameters param) {
    	ListOfDeltaLocations newList = new ListOfDeltaLocations();

		if (lastChar<0)
			return newList;
		
    	for (Chromosome chr : genome){
    		int[] pos = chr.getPOS(lastChar);
    		if (pos.length == 0)
    			continue;
    		
    		int prev_p=0;
            for (int charPos : pos) {
                for (int dLeft = param.getMaximumDelta() + 1; dLeft >= 1; dLeft--) {
                    if (chr.getL(charPos, dLeft) < prev_p)
                        continue;

                    while (dLeft > 1 && chr.getL(charPos, dLeft) == chr.getL(charPos, dLeft - 1))
                        dLeft--;

                    int interveningChars = dLeft - 1;
                    
                    for (int dRight = 1; dRight <= param.getMaximumDelta() + 1; dRight++) {
                        int leftBorder = chr.getL(charPos, dLeft);
                        int rightBorder = chr.getR(charPos, dRight);

                        if (dRight > 1)  // Check if new unmarked char R[dRight-1] is already contained in the interval
                            if (chr.getPrevOCC(chr.getR(charPos, dRight - 1)) < Math.max(1, leftBorder))
                            	interveningChars++;

                        if (dRight > 1 && chr.getR(charPos, dRight) == chr.getR(charPos, dRight - 1))  // either right end of genome, could break
                            continue;  // or merged non occ gene (-x : x>1), so interveningChars++ and continue

                        // test total distance
                        if (interveningChars > param.getMaximumDelta())
                            break;

                        // test maximality
                        if (chr.getNextOCC(leftBorder) < rightBorder)
                            break;

                        if (chr.getPrevOCC(rightBorder) > leftBorder)
                            continue;

                        // test compactness
                        if (chr.getNUMDiff(leftBorder + 1, rightBorder - 1, chr.getL_prime(charPos, dLeft), chr.getR_prime(charPos, dRight)) != 0)
                            continue;

                        int charSetSize = chr.getNUM(leftBorder + 1, rightBorder - 1);
                        int missingChars =  pSize - charSetSize + interveningChars;
                        int dist = missingChars + interveningChars;

                        assert (dist >= 0);

                        if (dist <= param.getMaximumDelta() && interveningChars <= param.getMaximumInsertions() && missingChars <= param.getMaximumDeletions()) {
                            assert (rightBorder - 1 <= chr.getEffectiveGeneNumber());

                            DeltaLocation newDeltaLoc = new DeltaLocation(genome.getNr(), chr.getNr(), leftBorder + 1, rightBorder - 1, dist, missingChars, interveningChars, charSetSize, charSetSize - interveningChars, !param.useDeltaTable());
                            newList.insertDeltaLocation(newDeltaLoc);
                        }
                    }
                }
            }
    	}
        return newList;
    }

    @Override public String toString() {
        return String.format("Pattern: RefGenomeNr: %1$d, L: %2$d, R: %3$d, lastChar: %4$d, %n", refGenomeNr, l, r, lastChar)
                + Arrays.toString(occ);
    }
}
