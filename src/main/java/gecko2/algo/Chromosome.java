package gecko2.algo;

import gecko2.algo.util.IntArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

    private int delta;
    
    // values used for lazy updating L_prime and R_prime
    private boolean updatePrimes;
    private int leftBorderForPrimes;
    private int geneForPrimes;
    private Rank rank;
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
        this.pos = this.computePOS(alphabetSize);

        this.L = new int[this.genes.length][];
        this.R = new int[this.genes.length][];
        this.L_prime = new int[this.genes.length][];
        this.R_prime = new int[this.genes.length][];
        for (int i = 0; i < this.genes.length; i++) {
            L[i] = new int[maxDelta + 2];
            R[i] = IntArray.newIntArray(maxDelta + 2, this.getEffectiveGeneNumber() + 1);
            L_prime[i] = new int[maxDelta + 2];
            R_prime[i] = IntArray.newIntArray(maxDelta + 2, this.getEffectiveGeneNumber() + 1);
        }
        this.delta = maxDelta;

        this.prevOcc = this.computePrevOcc(alphabetSize);
        this.nextOcc = this.computeNextOcc(alphabetSize);
    }

    private int[][] computePOS(int alphabetSize) {
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
    
    private int[] computePrevOcc(int alphabetSize) {
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

    private int[] computeNextOcc(int alphabetSize) {
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
     * Computes the difference between NUM[l1][r1] and NUM[l2][r2]
     * @param l1 left border of interval 1
     * @param r1 right border of interval 1
     * @param l2 left border of interval 2
     * @param r2 right border of interval 2
     * @return difference between NUM[l1][r1] and NUM[l2][r2]
     */
    public int getNUMDiff(int l1, int r1, int l2, int r2) {
    	int numCount = 0;
    	
    	// if only right border changes
    	if (r1 >= r2 && l1 == l2){
    		for (int i=r1; i>r2; i--)
    			if (prevOcc[i] < l1)
    				numCount++;
    		return numCount;
    	}
    	
    	// if only right border changes
    	if (r1 < r2 && l1 == l2)
    	{
    		for (int i=r2; i>r1; i--)
    			if (prevOcc[i] < l1)
    				numCount--;
    		return numCount;
    	}
    	
    	// if only left border changes
    	if (l1 <= l2 && r1 == r2)
    	{
    		for (int i=l1; i<l2; i++)
    			if (nextOcc[i] > r1)
    				numCount++;
    		return numCount;
    	}
    	
    	// if only left border changes
    	if (l1 > l2 && r1 == r2)
    	{
    		for (int i=l2; i<l1; i++)
    			if (nextOcc[i] > r1)
    				numCount--;
    		return numCount;
    	}

    	// if both border change, very slow!
    	for (int i=l1; i<=r1; i++)
    		if(prevOcc[i] < l1)
    			numCount++;

    	for (int i=l2; i<=r2; i++)
    		if(prevOcc[i] < l2)
    			numCount--;

    	return numCount;
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
                    if(this.getNUMDiff(j, i, j+1, i) > 0) {                         // if unmarked char found for the 1st time
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
        for (int i=0; i<genes.length; i++) {
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
                    if(this.getNUMDiff(i, j, i, j-1) > 0) {                         // if unmarked char found for the 1st time
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
                        if (this.getR(j, l) > lastOcc) {                            // because c_old is a new mismatch left of i
                            for (int p = delta + 1; p > l; p--)
                                R[j][p] = this.getR(j, p - 1);                   // shift all higher entries in R
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
        for (int i=0; i<genes.length; i++) {
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
    
    public void updateL_R_prime(Rank rank, int leftBorder, int c_old, int alphabetSize) {
    	this.rank = rank;
    	this.leftBorderForPrimes = leftBorder;
    	this.geneForPrimes = c_old;
    	this.alphabetSize = alphabetSize;
    	this.updatePrimes = true;
    }
    
    public int getL_prime (int pos, int diff) {
    	if (updatePrimes) {
    		updateL_prime();
    		updateR_prime();
    		this.updatePrimes = false;
    	}
        return L_prime[pos][diff];
    }
    
    private void updateL_prime() {
    	int maxUpdateRank;
    	if (geneForPrimes<0){
    		maxUpdateRank = alphabetSize;
    	} else {
    		maxUpdateRank = (leftBorderForPrimes==1) ? alphabetSize+1 : rank.getRank(geneForPrimes);
    	}
    	for(int j=1;j<=this.getEffectiveGeneNumber();j++){
			if(genes[j]<0)
                continue;
			if(rank.getRank(genes[j])<=maxUpdateRank) {
				for(int d=1; d<=delta+1; d++) {
					for(int l=this.L[j][d]+1; l<=j; l++) {
						if(genes[l] >= 0 && rank.getRank(genes[l]) <= rank.getRank(genes[j])) {
							L_prime[j][d] = l;
							break;
						}
					}
				}
			}
    	}
    }
    
    public int getR_prime (int pos, int diff) {
    	if (updatePrimes) {
    		updateL_prime();
    		updateR_prime();
    		this.updatePrimes = false;
    	}
        return R_prime[pos][diff];
    }
    
    private void updateR_prime() {
    	int maxUpdateRank;
    	if (geneForPrimes<0){
    		maxUpdateRank = alphabetSize;
    	} else {
    		maxUpdateRank = (leftBorderForPrimes==1) ? alphabetSize+1 : rank.getRank(geneForPrimes);
    	}
    	for(int j=1;j<=this.getEffectiveGeneNumber();j++){
			if (genes[j]<0)
                continue;
    		if(rank.getRank(genes[j])<=maxUpdateRank) {
				for(int d=1; d<=delta+1; d++) {
					for(int l=this.R[j][d]-1; l>=j; l--) {
						if(genes[l] >= 0 && rank.getRank(genes[l]) <= rank.getRank(genes[j])) {
							R_prime[j][d] = l;
							break;
						}
					}
				}
			}
    	}
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
