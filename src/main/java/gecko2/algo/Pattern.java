package gecko2.algo;

import gecko2.algo.util.IntArray;

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
        pSize++;
        occ[c]++;
        IntArray.increaseAll(minDist);              // increase minDist to each sequence
        minDist[refGenomeNr]--;   // but the reference sequence by 1
        IntArray.reset(maxRemDist, -1);
    }

    /**
     * Expands a Pattern to the next reference Sequence from the chromosome.
     * @param i the previous right border of pattern that is expanded to the right.
     * @return true if the pattern could be expanded successfully.
     */
    public boolean updateToNextI_ref(int i) {
        if (i > refChr.size())                                 // If end of sequence reached
            return false;
        int c = refChr.getGene(i);
        if (refChr.getGene(getLeftBorder() - 1) == c)          // If pattern no longer left maximal
            return false;
        while (nrOccurrences(refChr.getGene(i + 1)) > 0 || refChr.getGene(i + 1) == c) // expand until the pattern is right maximal
            i++;

        addToPattern(c, i);
        return true;
    }
    
    public ListOfDeltaLocations computeNewOptimalDeltaLocations(Genome genome, int character, int pSize, AlgorithmParameters param) {
    	ListOfDeltaLocations newList = new ListOfDeltaLocations();
    	
    	for (Chromosome chr : genome){
    		int[] pos = chr.getPOS(character);
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
                        if (dRight > 1 && chr.getR(charPos, dRight) == chr.getR(charPos, dRight - 1))
                            break;

                        if (dRight > 1)  // Teste ob neues unmarkiertes Zeichen R[dRight-1] schon im Intervall vorkommt
                            if (chr.getPrevOCC(chr.getR(charPos, dRight - 1)) < Math.max(1, chr.getL(charPos, dLeft)))
                            	interveningChars++;

                        // test total distance
                        if (interveningChars > param.getMaximumDelta())
                            break;

                        // test maximality
                        if (chr.getNextOCC(chr.getL(charPos, dLeft)) < chr.getR(charPos, dRight))
                            break;

                        if (chr.getPrevOCC(chr.getR(charPos, dRight)) > chr.getL(charPos, dLeft))
                            continue;

                        // test compactness
                        if (chr.getNUMDiff(chr.getL(charPos, dLeft) + 1, chr.getR(charPos, dRight) - 1, chr.getL_prime(charPos, dLeft), chr.getR_prime(charPos, dRight)) != 0)
                            continue;

                        int charSetSize = chr.getNUM(chr.getL(charPos, dLeft) + 1, chr.getR(charPos, dRight) - 1);
                        int missingChars =  pSize - charSetSize + interveningChars;
                        int dist = missingChars + interveningChars;

                        assert (dist >= 0);

                        if (dist <= param.getMaximumDelta() && interveningChars <= param.getMaximumInsertions() && missingChars <= param.getMaximumDeletions()) {
                            assert (chr.getR(charPos, dRight) - 1 <= chr.size());

                            DeltaLocation newDeltaLoc = new DeltaLocation(genome.getNr(), chr.getNr(), chr.getL(charPos, dLeft) + 1, chr.getR(charPos, dRight) - 1, dist, missingChars, interveningChars, charSetSize, charSetSize - interveningChars, !param.useDeltaTable());
                            newList.insertDeltaLocation(newDeltaLoc);
                        }
                    }
                }
            }
    	}
    	return newList;
    }

    @Override public String toString() {
        StringBuilder b = new StringBuilder(String.format("Pattern: RefGenomeNr: %1$d, L: %2$d, R: %3$d, lastChar: %4$d, %n", refGenomeNr, l, r, lastChar));
        b.append(Arrays.toString(occ));
        return b.toString();
    }
}
