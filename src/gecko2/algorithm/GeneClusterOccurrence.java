package gecko2.algorithm;

import java.io.Serializable;
import java.math.BigDecimal;


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
	private int id;									// A unique ID for every occurrence (unique within every cluster)
	private Subsequence[][] subsequences;			// The list of subsequences
	private BigDecimal pValue;							// The pValue of this occurrence
	private int dist;								// The distance of this occurrence
	private int support;							// The number of genomes that support this occurrence
	
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
