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

package de.unijena.bioinf.gecko3.algo;

import de.unijena.bioinf.gecko3.algo.util.IntArray;

import java.util.Arrays;

/**
 * Rank implements a rank table based on one chromosome.
 * In this table, the first occurrence of the character in the chromosome starting at the left border of the current interval is stored. 
 *
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
class Rank{
    private final int[] rank;

    private final int DEFAULT_RANK;
    private final int MAX_RANK;

    /**
     * Constructs an rank table for an alphabet of size alphabetSize.
     * @param alphabetSize the size of the alphabet of all compared sequences.
     */
    public Rank(int alphabetSize) {
        rank = new int[alphabetSize+1];
        DEFAULT_RANK = alphabetSize;
        MAX_RANK = alphabetSize + 1;
    }

    /**
     * Returns the rank value of an character.
     * @param character the character who's value shall be returned.
     * @return the rank value of the character
     */
    public int getRank(int character) {
        return rank[character];
    }

    /**
     * Computes the rank table based on the chromosome chr.
     * @param chr the chromosome.
     */
    void computeRank(Chromosome chr){
        IntArray.reset(rank, DEFAULT_RANK);                               // initialise rank table with the default value
        rank[0] = MAX_RANK;                            // the terminal characters have character 0 and the highest rank

        int r=1;
        for (int i=1; i<=chr.getEffectiveGeneNumber(); i++) {                      // all characters in the chromosome
            if (chr.getGene(i)>=0){
            	if (rank[chr.getGene(i)]==DEFAULT_RANK) {        // who are not already set to a rank
            		rank[chr.getGene(i)] = r++;                  // get ranked by their first occurrence in the chromosome
            	}
            }
        }
    }
    
    /**
     * Updates the values of rank based on chr[leftBorder, |chr|].
     * @param chr the chromosome the rank is calculated for.
     * @param leftBorder the start position of the interval on the chromosome.
     * @param alphabetSize the size of the alphabet of all compared sequences.
     */
    public void updateRank(Chromosome chr, int leftBorder, int alphabetSize){
        if (leftBorder==1) {                                        // if starting to iterate through a new sequence (leftBorder is 1)
            this.computeRank(chr);                    // Rank has to be calculated anew
        }
        else {
            int maxRank = 0;
            int i;
            for (i=leftBorder; i<=chr.getEffectiveGeneNumber(); i++) {                 // Iterate through substrings starting with leftBorder
                if (chr.getGene(i)<0 || chr.getGene(leftBorder-1)<0) continue;
            	if (chr.getGene(i)!=chr.getGene(leftBorder-1)) {                        // if character is not equal to the character at position leftBorder-1
                	if (chr.getNUMDiff(leftBorder-1, i, leftBorder-1, i-1) > 0) {   // only update if first occurrence after position i
                        rank[chr.getGene(i)] = rank[chr.getGene(i)]-1;           // new rank = old rank - 1
                        maxRank = Math.max(maxRank, rank[chr.getGene(i)]);               // max rank in the interval [leftBorder, i]
                    }
                }
                else {                                      // if first occurrence of character at position leftBorder-1 is found
                    rank[chr.getGene(i)] = maxRank+1; // character has maximal rank
                    if(chr.getGene(i)<0) rank[0]--;
                    break;                                  // update can be stopped, as the rank of all other characters is unchanged
                }
            }
            if (chr.getGene(leftBorder-1)>=0 && i==chr.getEffectiveGeneNumber()+1) {                                      // if character at position leftBorder-1 is not part of the interval
                rank[chr.getGene(leftBorder-1)] = alphabetSize;      // he is assigned the default rank
            }
        }
    }

    @Override public String toString() {
        return String.format("Rank: %1$s", Arrays.toString(rank));
    }
}
