package gecko2.algo;

import java.util.*;

/**
 *
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
class GenomeList {

    private final List<Genome> genomes;
    private Rank rank;
    private final int alphabetSize;
    
    private boolean containsReferenceCopy;

    /**
     * Constructor for generating a SetOfSequences from a lists of Integers.
     * @param genomes the lists of Integers.
     */
    GenomeList(List<List<List<Integer>>> genomes) {
        SortedSet<Integer> genes = new TreeSet<Integer>();
        int alphSize = 0;
        int i = 0;
        this.genomes = new ArrayList<Genome>(genomes.size());
        for (List<List<Integer>> genome : genomes) {
            List<Chromosome> chromosomes = new ArrayList<Chromosome>(genome.size());
            int j=0;
            for (List<Integer> chromosome : genome) {
                for (Integer gene : chromosome) {
                    if (gene.equals(0))
                        throw new IllegalArgumentException("0 genes not permitted!");
                    if (genes.add(gene))
                        alphSize++;
                }
                chromosomes.add(new Chromosome(chromosome, j));
                j++;
            }
            Genome g = new Genome(i, chromosomes);
            this.genomes.add(g);
            i++;
        }
        this.alphabetSize = alphSize;
        this.containsReferenceCopy = false;
    }
    
    /**
     * Constructor for generating a SetOfSequences from an array of integer with given alphabet size.
     * @param genomes the lists of Integers.
     * @param alphabetSize the alphabet size
     */
    GenomeList(int[][][] genomes, int alphabetSize) {
        int i = 0;
        this.genomes = new ArrayList<>(genomes.length);
        for (int[][] genome : genomes) {
            List<Chromosome> chromosomes = new ArrayList<>(genome.length);
            int j=0;
            for (int[] chromosome : genome) {
                chromosomes.add(new Chromosome(chromosome, j, false));
                j++;
            }
            Genome g = new Genome(i, chromosomes);
            this.genomes.add(g);
            i++;
        }
        this.alphabetSize = alphabetSize;
        this.containsReferenceCopy = false;
    }
    
    /**
     * Constructor for generating a SetOfSequences from an array of integer.
     * @param genomes the lists of Integers.
     */
    GenomeList(int[][][] genomes) {
        Set<Integer> genes = new HashSet<>();
        genes.add(0);
        int alphSize = 0;
        int i = 0;
        this.genomes = new ArrayList<>(genomes.length);
        for (int[][] genome : genomes) {
            List<Chromosome> chromosomes = new ArrayList<>(genome.length);
            int j=0;
            for (int[] chromosome : genome) {
                for (Integer gene : chromosome) {
                	if (gene >= 0 && genes.add(gene))
                		alphSize++;
                }
                chromosomes.add(new Chromosome(chromosome, j, false));
                j++;
            }
            Genome g = new Genome(i, chromosomes);
            this.genomes.add(g);
            i++;
        }
        this.alphabetSize = alphSize;      
        this.containsReferenceCopy = false;
    }
    
    /**
     * Appends a copy of the reference genome to the genome list. 
     * Used for searching reference occurrences in the reference genome.
     * @param referenceGenomeNr the number of the reference genome.
     */
    void appendCopyOfReferenceGenome(int referenceGenomeNr, AlgorithmParameters param) {
    	if (containsReferenceCopy)
    		throw new RuntimeException("Trying to append additional reference copy!");
    	Genome newGenome = new Genome(genomes.get(referenceGenomeNr));
    	for (Chromosome chr : newGenome) {
            chr.initializeForCalculation(alphabetSize, param.getMaximumDelta());
        }
    	genomes.add(newGenome);
    	param.increaseNrOfGenomes();
    	this.containsReferenceCopy = true;
    }
    
    void removeCopyOfReferenceGenome(AlgorithmParameters param) {
    	if (!containsReferenceCopy)
    		throw new RuntimeException("Trying to remove non existing reference copy!");
    	
    	genomes.remove(genomes.size()-1);
    	param.decreaseNrOfGenomes();
    	this.containsReferenceCopy = false;
    }
    
    /**
     * Initializes the SetOfSequences for a calculation of gene clusters.
     * @param maxDelta the maximum distance
     */
    void initializeForCalculation(int maxDelta) {
        for (Genome g : genomes) {
            for (Chromosome chr : g) {
                chr.initializeForCalculation(alphabetSize, maxDelta);
            }
        }
        rank = new Rank(alphabetSize);
    }

    /**
     * Returns the genome with the number n.
     * @param n the number of the genome.
     * @return the genome with the number n.
     */
    public Genome get(int n) {
        return genomes.get(n);
    }

    /**
     * Returns the number of genomes in the SetOfSequences.
     * @return the number of genomes in the SetOfSequences.
     */
    public int size() {
        return genomes.size();
    }

    /**
     * Returns the size of the alphabet of all sequences.
     * @return the size of the alphabet of all sequences.
     */
    public int getAlphabetSize() {
        return alphabetSize;
    }

    /**
     * Updates the left border of the pattern to the position leftBorder in the reference chromosome refChr from the genome refGenomeNr.
     * @param leftBorder the new left border of the pattern.
     * @param refChr the reference chromosome.
     * @param refGenomeNr the number of the genome the reference chromosome is located on.
     * @param param the parameters the algorithm is started with.
     */
    public void updateLeftBorder(int leftBorder, Chromosome refChr, int refGenomeNr, AlgorithmParameters param) {
        rank.updateRank(refChr, leftBorder, alphabetSize);  //TODO rank really in seqSet? Alternative Rank in Pattern
        this.updateL(refGenomeNr, leftBorder, param.getMaximumDelta(), refChr.getGene(leftBorder - 1));
        this.updateR(refGenomeNr, leftBorder, param.getMaximumDelta(), refChr.getGene(leftBorder - 1));
        this.updateL_R_prime(refGenomeNr, leftBorder, param.getMaximumDelta(), refChr.getGene(leftBorder - 1));
    }
    
    public boolean zeroOccs(int refGenomeNr, int refChrNr, int position, boolean searchRefInRef){
    	int c = genomes.get(refGenomeNr).get(refChrNr).getGene(position);
    	
    	for (int l=0; l<genomes.size(); l++){
    		if (l==refGenomeNr)
    			continue;
    			
    		if (searchRefInRef && l == genomes.size()-1){
    			//TODO refInRef
    		} else {
    			if (!genomes.get(l).noOcc(c))
    				return false;
    		}
    	}
    	return true;
    }

    /**
     * Updates or computes the matrix L for each genome in the SetOfGenomes.
     *
     * L holds the max. maxDist positions of the next unmarked characters left of each position in the chromosome.
     * The array rank is used to determine unmarked characters.
     * @param refGenomeNr the number of the current reference genome.
     * @param i the start position of the current reference interval on the reference chromosome.
     * @param maxDist the maximal possible distance for a valid gene cluster.
     * @param c_old the character that was last added to the reference interval.
     */
    private void updateL(int refGenomeNr, int i, int maxDist, int c_old) {  //TODO parallel?
        for (int k=0; k<this.size(); k++) {
            if (k==refGenomeNr) {
                    continue;
            }

            for (Chromosome chr: genomes.get(k)) {
                chr.updateL(rank, i, maxDist, c_old);
            }
        }
    }

    /**
     * Updates or computes the matrix R for each genome in the SetOfGenomes.
     *
     * R holds the max. maxDist positions of the next unmarked characters right of each position in the chromosome.
     * The array rank is used to determine unmarked characters.
     * @param refGenomeNr the number of the current reference genome.
     * @param i the start position of the current reference interval on the reference chromosome.
     * @param maxDist the maximal possible distance for a valid gene cluster.
     * @param c_old the character that was last added to the reference interval.
     */
    private void updateR(int refGenomeNr, int i, int maxDist, int c_old){
        for (int k=0; k<this.size(); k++) {
            if (k==refGenomeNr) {               // not needed for the reference sequence
                    continue;
            }

            for (Chromosome chr: genomes.get(k)) {
                chr.updateR(rank, i, maxDist, c_old);
            }
        }
    }
    
    private void updateL_R_prime(int refGenomeNr, int leftBorder, int delta,
			int gene) {
		for (int k=0; k<this.size(); k++) {
			if (k != refGenomeNr) {
				for (Chromosome c: genomes.get(k)) {
					c.updateL_R_prime(rank, leftBorder, delta, gene, alphabetSize);
				}
			}
		}
		
	}
    
    private int neg(int gen){
    	if(gen > 0) return gen;
    	else return 0;
    }
    
    int[][] charFrequencies() {
    	int[][] charFreq = new int[genomes.size()][alphabetSize+1];
    	
    	for (int k=0; k<genomes.size(); k++)
    		for (Chromosome chr : genomes.get(k))
    			for (int i=0; i<chr.size()+1; i++)  // Correct for not counted 0 termination
    				charFreq[k][neg(chr.getGene(i))]++;
 
    	return charFreq;
    }

    @Override public String toString() {
        StringBuilder b = new StringBuilder(String.format("Alphabet size: %1$d%n", alphabetSize));
        for (Genome genome : genomes) {
            b.append(genome);
        }
        return b.toString();
    }
}