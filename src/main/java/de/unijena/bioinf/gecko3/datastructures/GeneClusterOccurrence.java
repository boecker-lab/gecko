package de.unijena.bioinf.gecko3.datastructures;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * This class models an occurrence of a {@link GeneCluster} (which is uniquely
 * identified by the set of genes it contains).
 * 
 * @author Leon Kuchenbecker <lkuchenb@inf.fu-berlin.de>
 *
 */
public class GeneClusterOccurrence implements Serializable {
	
	/**
	 * Random generated serialization UID
	 */
	private static final long serialVersionUID = 657846431928908396L;
	private final int id;									// A unique ID for every occurrence (unique within every cluster)
	private final Subsequence[][] subsequences;			// The list of subsequences
	private final BigDecimal pValue;							// The pValue of this occurrence
	private final int dist;								// The distance of this occurrence
	private final int support;							// The number of genomes that support this occurrence
	
	public static final int GENOME_NOT_INCLUDED = -1;
	
	public GeneClusterOccurrence(int id, Subsequence[][] subsequences, double pValueBase, int pValueExp, int totalDist, int support) {
		this(id, subsequences, (new BigDecimal(pValueBase)).scaleByPowerOfTen(pValueExp), totalDist, support);
	}
	
	/**
	 * Create a new {@link GeneClusterOccurrence} object.
	 * @param id The ID of this {@link GeneClusterOccurrence} (unique within each {@link GeneCluster})
	 * @param subsequences The subsequences that describe this occurrence, where subsequence[i]
	 * contains the subsequences on genome i
	 * @param pValue The pValue of this occurrence
	 * @param totalDist The distance of this occurrence
	 */
	public GeneClusterOccurrence(int id, Subsequence[][] subsequences, BigDecimal pValue, int totalDist, int support) {
		super();
		this.id = id;
		this.subsequences = subsequences;
		this.pValue = pValue;
		this.dist = totalDist;
		this.support = support;
	}

    /**
     * Generates a new gene cluster occurrence, that only contains the best occurrences on each genome
     * @return a new gene cluster occurrence, that only contains the best occurrences on each genome
     */
    public GeneClusterOccurrence getBestOccurrence() {
        Subsequence[][] subs = new Subsequence[subsequences.length][];
        for (int i=0; i< subs.length; i++) {
            int minDist = Integer.MAX_VALUE;
            List<Subsequence> subList = new ArrayList<>(subsequences[i].length);
            for (int j=0; j<subsequences[i].length; j++){
                if (subsequences[i][j].getDist() < minDist) {
                    minDist = subsequences[i][j].getDist();
                    subList.clear();
                    subList.add(subsequences[i][j]);
                } else if (subsequences[i][j].getDist() == minDist)
                    subList.add(subsequences[i][j]);
            }
            subs[i] = subList.toArray(new Subsequence[subList.size()]);
        }

        return new GeneClusterOccurrence(id, subs, pValue, dist, support);
    }
	
	/**
	 * Returns the number of genomes that support this occurrence
	 * @return the number of genomes that support this occurrence
	 */
	public int getSupport() {
		return support;
	}
	
	/**
	 * Returns the best score of this occurrence
	 * @return the best score of this occurrence
	 */
	public double getBestScore() {
		return -getBigDecimalLog(pValue);
	}
	
	private static double getBigDecimalLog(BigDecimal value){
		String s = value.toString();
		String[] split = s.split("E");
		double base = Double.parseDouble(split[0]);
		double log = 0.0;
		if (split.length == 2){
			log = Double.parseDouble(split[1]);
			
		}
		log += Math.log10(base);
		return log;
	}
	
	/**
	 * Returns the ID of this {@link GeneClusterOccurrence}.
	 * @return The ID of this {@link GeneClusterOccurrence}
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the array of subsequences that describe this {@link GeneClusterOccurrence}.
	 * @return The array of subsequences that describe this {@link GeneClusterOccurrence}.
	 */
	public Subsequence[][] getSubsequences() {
		return subsequences;
	}
	
	/**
	 * Returns the best pValue
	 * @return The best pValue
	 */
	public BigDecimal getBestpValue() {
		return pValue;
	}
	
	/**
	 * Returns the distance
	 * @return The distance
	 */
	public int getTotalDist() {
		return dist;
	}
		

}
