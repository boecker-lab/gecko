package gecko2.algo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
class Genome implements Iterable<Chromosome> {
    private final List<Chromosome> chromosomes;
    private final int nr;
    private final int length;

    /**
     * 
     * @param nr the nr of the genome
     * @param chromosomes the list of chromosomes
     */
	public Genome(int nr, List<Chromosome> chromosomes) {
        this.chromosomes = new ArrayList<Chromosome>(chromosomes);
        this.nr = nr;
        int l = 0;
        for (Chromosome chr : chromosomes)
            l += chr.size();
        length = l;
	}

    public Genome(int nr, Chromosome chromosome) {
        this.chromosomes = new ArrayList<Chromosome>(1);
        this.chromosomes.add(chromosome);
        this.nr = nr;
        length = chromosome.size();
    }
    
    public Genome(Genome other) {
    	this.chromosomes = new ArrayList<Chromosome>(other.chromosomes.size());
    	for (Chromosome chr : other.chromosomes)
    		this.chromosomes.add(new Chromosome(chr));
    	this.nr = other.nr;
    	this.length = other.length;
    }

	public Chromosome get(int nr) {
        return chromosomes.get(nr);
    }
    
    public int getChromosomeCount() {
    	return chromosomes.size();
    }
    
    public int getNr(){
    	return nr;
    }

    /**
     * Returns the total length of the genome, the sum of the lengths of all chromosomes.
     * @return the length.
     */
    public int getLength() {
        return length;
    }   
    
    /**
     * Checks if the character c does not occurre on the genome
     * @param c the character
     * @return true if the character does not occurre
     */
    boolean noOcc(int c){
    	for (Chromosome chr: chromosomes){
    		if (c<0) continue;
    		if (chr.getPOS(c).length != 0)
    			return false;
    	}
    	return true;
    }
    
    /**
     * Checks if the character c does not occurre on the genome outside of the interval [l, r] in chromosome chrNr
     * @param c the character
     * @param l the left border of the interval
     * @param r the right border of the interval
     * @param chrNr the number of the chromosome the interval is located on
     * @return true if the character does not occurre outside of the interval
     */
	public boolean noOccOutsideInterval(int c, int l, int r, int chrNr) {
		for (Chromosome chr: chromosomes){
			if (chr.getNr() != chrNr){
	    		if (chr.getPOS(c).length != 0)
	    			return false;
			} else {
				int[] pos = chr.getPOS(c);
                for (int position : pos) {
                    if (position < l || position > r)
                        return false;
                }
			}
		}
		return true;
	}

    public int[] getCharFrequency(int alphabetSize) {
        int[] charFreq = new int[alphabetSize + 1];

        for (Chromosome chr : chromosomes)
            for (int i = 1; i < chr.size() + 1; i++) {  // Correct for not counted 0 termination
                if (chr.getGene(i) < 0)
                    charFreq[0]++;
                else
                    charFreq[chr.getGene(i)]++;
            }

        return charFreq;
    }
    
    /**
     * Returns a string representing the data in this genome.
     * @return a string representing the data in this genome.
     */
    @Override public String toString() {
        StringBuilder b = new StringBuilder(String.format("Genome Nr: %1$d%n", nr));
        for (Chromosome chr: this) {
            b.append(String.format("%1$s%n", chr));
        }
        return b.toString();
    }

    @Override
    public Iterator<Chromosome> iterator() {
        return chromosomes.iterator();
    }
}
