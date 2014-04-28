package gecko2.algo;

import gecko2.algo.util.IntArray;

import java.util.Arrays;

/**
 * Rank implements a rank table based on one chromosome.
 * In this table, the first occurrence of the character in the chromosome starting at the left border of the current interval is stored. 
 *
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
class Rank{
    private final int[] rank;

    /**
     * Constructs an rank table for an alphabet of size alphabetSize.
     * @param alphabetSize the size of the alphabet of all compared sequences.
     */
    public Rank(int alphabetSize) {
        rank = new int[alphabetSize+1];
    }

    /**
     * Returns the rank value of an character.
     * @param character the character who's value shall be returned.
     * @return the value of the character
     */
    public int getRank(int character) {
        return rank[character];
    }

    /**
     * Computes the rank table based on the chromosome chr.
     * @param chr the chromosome.
     * @param alphabetSize the size of the alphabet of all compared sequences.
     */
    void computeRank(Chromosome chr, int alphabetSize){
        IntArray.reset(rank, alphabetSize);                               // initialise rank table with the default value
        rank[0] = alphabetSize+1;                            // the terminal characters have character 0 and the highest rank

        int r=1;
        for (int i=1; i<=chr.size(); i++) {                      // all characters in the chromosome
            if (rank[chr.getGene(i)]==alphabetSize) {       // who are not already set to a rank
                rank[chr.getGene(i)] = r++;                  // get ranked by their first occurrence in the chromosome
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
            this.computeRank(chr, alphabetSize);                    // Rank has to be calculated anew
        }
        else {
            int maxRank = 0;
            int i;
            for (i=leftBorder; i<=chr.size(); i++) {                 // Iterate through substrings starting with leftBorder
                if (chr.getGene(i)!=chr.getGene(leftBorder-1)) {                        // if character is not equal to the character at position leftBorder-1
                	if (chr.getNUMDiff(leftBorder-1, i, leftBorder-1, i-1) > 0) {   // only update if first occurrence after position i
                        rank[chr.getGene(i)] = rank[chr.getGene(i)]-1;           // new rank = old rank - 1
                        maxRank = Math.max(maxRank, rank[chr.getGene(i)]);               // max rank in the interval [leftBorder, i]
                    }
                }
                else {                                      // if first occurrence of character at position leftBorder-1 is found
                    rank[chr.getGene(i)] = maxRank+1;    // character has maximal rank
                    break;                                  // update can be stopped, as the rank of all other characters is unchanged
                }
            }
            if (i==chr.size()+1) {                                      // if character at position leftBorder-1 is not part of the interval
                rank[chr.getGene(leftBorder-1)] = alphabetSize;      // he is assigned the default rank
            }
        }
    }

    /**
     * Tests if the interval [l,r] on the chromosome chr is optimal.
     * @param chr the chromosome the interval is located on.
     * @param lastChar the character that was last added to the interval.
     * @param l the left border of the interval.
     * @param r the right border of the interval.
     * @return true if the interval is optimal, else false.
     */
    public boolean isOptimalInterval(Chromosome chr, int lastChar, int l, int r) {
        if (rank[chr.getGene(l-1)] <= rank[lastChar])
            return false;

        if (rank[chr.getGene(r+1)] <= rank[lastChar])
            return false;

        if (rank[chr.getGene(l)] <= rank[lastChar] && rank[chr.getGene(r)] <= rank[lastChar]) {
            return rank[chr.getGene(l-1)] != rank[lastChar] && rank[chr.getGene(r+1)] != rank[lastChar];
        }

        if (rank[chr.getGene(l)] > rank[lastChar] && chr.getNextOCC(l) > r)
            return false;

        if (rank[chr.getGene(r)] > rank[lastChar] && chr.getPrevOCC(r) < l)
            return false;

        int l_essent = l-1;
        int r_essent = r+1;
        for (int i=l; i<=r && l_essent<l; i++)
            if (rank[chr.getGene(i)] <= rank[lastChar])
                l_essent = i;

        for (int i = r; i>=l && r_essent>r; i--)
            if (rank[chr.getGene(i)] <= rank[lastChar])
                r_essent = i;

        for (int i=l; i<l_essent; i++)
            if(chr.getNextOCC(i) > r_essent)
                return false;

        for (int i=r; i>r_essent; i--)
            if(chr.getNextOCC(i) < l_essent)
                return false;

        return true;
    }

    @Override public String toString() {
        if (rank != null)
            return (String.format("Rank: %1$s", Arrays.toString(rank)));
        else
            return "Rank: null";
    }
}