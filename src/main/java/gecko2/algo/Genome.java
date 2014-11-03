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

    /**
     *  Needed for statistics calculations.
     *  Position 0 contains the total length of the genome.
     *  Positions i contains the total number of intervals of length i that fit into the genome.
     */
    private final List<Integer> possibleIntervalsPerIntervalLength;

    /**
     * 
     * @param nr the nr of the genome
     * @param chromosomes the list of chromosomes
     */
	public Genome(int nr, List<Chromosome> chromosomes) {
        this.chromosomes = new ArrayList<>(chromosomes);
        this.nr = nr;
        int l = 0;
        for (Chromosome chr : chromosomes)
            l += chr.getTotalGeneNumber();
        possibleIntervalsPerIntervalLength = new ArrayList<>();
        possibleIntervalsPerIntervalLength.add(l);
	}

    public Genome(int nr, Chromosome chromosome) {
        this.chromosomes = new ArrayList<>(1);
        this.chromosomes.add(chromosome);
        this.nr = nr;
        possibleIntervalsPerIntervalLength = new ArrayList<>();
        possibleIntervalsPerIntervalLength.add(chromosome.getTotalGeneNumber());
    }
    
    public Genome(Genome other) {
    	this.chromosomes = new ArrayList<>(other.chromosomes.size());
    	for (Chromosome chr : other.chromosomes)
    		this.chromosomes.add(new Chromosome(chr));
    	this.nr = other.nr;
        possibleIntervalsPerIntervalLength = new ArrayList<>(other.possibleIntervalsPerIntervalLength);
    }

	public Chromosome get(int nr) {
        return chromosomes.get(nr);
    }
    
    public int getNr(){
    	return nr;
    }

    /**
     * Returns the total length of the genome, the sum of the lengths of all chromosomes.
     * @return the length.
     */
    public int getLength() {
        return possibleIntervalsPerIntervalLength.get(0);
    }

    public int getNrOfPossibleIntervals(int intervalLength) {
        if (possibleIntervalsPerIntervalLength.size() <= intervalLength) {
            for (int i=possibleIntervalsPerIntervalLength.size(); i<=intervalLength; i++){
                possibleIntervalsPerIntervalLength.add(computePossibleIntervals(i));
            }
        }
        return possibleIntervalsPerIntervalLength.get(intervalLength);
    }

    private int computePossibleIntervals(int intervalLength) {
        int possibleIntervals = 0;
        for (Chromosome chr : chromosomes)
            possibleIntervals += Math.max(0, chr.getTotalGeneNumber()-intervalLength+1);
        return possibleIntervals;
    }

    /**
     * Checks if the character c does not occur on the genome
     * @param c the character
     * @return 0 if the character does occur, values > 0 for how many characters do not occur
     */
    int noOcc(int c){
        if (c < 0)
            return -c;

    	for (Chromosome chr: chromosomes){
    		if (chr.getPOS(c).length != 0)
    			return 0;
    	}
    	return 1;
    }
    
    /**
     * Checks if the character c does not occur on the genome outside of the interval [l, r] in chromosome chrNr
     * @param c the character
     * @param l the left border of the interval
     * @param r the right border of the interval
     * @param chrNr the number of the chromosome the interval is located on
     * @return 0 if the character does occur, values > 0 for how many characters do not occur
     */
	public int noOccOutsideInterval(int c, int l, int r, int chrNr) {
        if (c<0)
            return -c;

		for (Chromosome chr: chromosomes){
			if (chr.getNr() != chrNr){
	    		if (chr.getPOS(c).length != 0)
	    			return 0;
			} else {
				int[] pos = chr.getPOS(c);
                for (int position : pos) {
                    if (position < l || position > r)
                        return 0;
                }
			}
		}
		return 1;
	}

    public int[] getCharFrequency(int alphabetSize) {
        int[] charFreq = new int[alphabetSize + 1];

        for (Chromosome chr : chromosomes)
            for (int i = 1; i < chr.getEffectiveGeneNumber() + 1; i++) {  // Correct for not counted 0 termination
                if (chr.getGene(i) < 0)                 // singleton genes have negative ids,
                    charFreq[0] -= chr.getGene(i);      // so subtract to add the number of singleton genes
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
