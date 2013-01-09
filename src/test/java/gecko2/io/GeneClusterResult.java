package gecko2.io;

import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Parameter;

/**
 * This class models a new data type which contains the result
 * of a computeClusters run (GeneCluser array) and the configuration
 * of this run.
 * 
 * @author Hans-Martin Haase <hans-martin.haase@uni-jena.de>
 * @version 0.03
 */
public class GeneClusterResult 
{
	//=======================================================================================//
	//============================== gloabel variables ======================================//
	//=======================================================================================//
	/**
	 * Stores the GeneGluster array
	 */
	private GeneCluster[] compResult;
	
	/**
	 * Stores the minimal cluster size
	 */
	private String minClusterSize;
	
	/**
	 * Stores delta
	 */
	private String delta;
	
	/**
	 * Stores the number of genomes which shall contain the cluster
	 */
	private String quorum;
	
	/**
	 * Stores the Quorum type
	 */
	private String quorumtype;
	
	/**
	 * Stores the operation mode
	 */
	private String operationMode;
	
	/**
	 * Stores the reference type
	 */
	private String refType;
	
	/**
	 * Stores the name of the genomes source file
	 */
	private String gSourceFileName;
	
	
	

	//=======================================================================================//
	//================================== Methods ============================================//
	//=======================================================================================//
	
	/**
	 * Getter for the GeneCluster array
	 * 
	 * @return GeneCluster[] the result of computeClusters
	*/
	public GeneCluster[] getCompResult() {
		return compResult;
	}

	/**
	 * The method sets the global variable compResult
	 * 
	 * @param compResult a GeneCluster array
	 */
	private void setCompResult(GeneCluster[] compResult) {
		this.compResult = compResult;
	}

	/**
	 * The method returns the minimum cluster size as string
	 * 
	 * @return the minimum cluster size
	 */
	public String getMinClusterSize() {
		return minClusterSize;
	}

	/**
	 * The method sets the global variable minClusterSize
	 * 
	 * @param minClusterSize the minimal size of the clusters
	 */
	private void setMinClusterSize(int minClusterSize) {
		this.minClusterSize = Integer.toString(minClusterSize);
	}

	/**
	 * The method returns the delta value as string
	 * 
	 * @return delta
	 */
	public String getDelta() {
		return delta;
	}

	/**
	 * The method sets the global variable delta
	 * 
	 * @param delta
	 */
	private void setDelta(int delta) {
		this.delta = Integer.toString(delta);
	}

	/**
	 * The method returns the quorum value (number of genomes which shall contain the cluster) as string
	 * 
	 * @return the quorum value
	 */
	public String getQuorum() {
		return quorum;
	}

	/**
	 * The method sets the global variable quorum
	 * 
	 * @param quorum
	 */
	private void setQuorum(int quorum) {
		this.quorum = Integer.toString(quorum);
	}

	/**
	 * The method returns the quorum type as string
	 * 
	 * @return the quorum type
	 */
	public String getQuorumtype() {
		return quorumtype;
	}

	/**
	 * The method sets the global variable quorumtype
	 * 
	 * @param quorumtype
	 */
	private void setQuorumtype(int quorumtype) {
		this.quorumtype = Integer.toString(quorumtype);
	}

	/**
	 * The method returns the operation mode as string
	 * 
	 * @return the operation mode
	 */
	public String getOperationMode() {
		return operationMode;
	}

	/**
	 * The method sets the global variable operationMode
	 * 
	 * @param operationMode
	 */
	private void setOperationMode(char operationMode) {
		this.operationMode = Character.toString(operationMode);
	}

	/**
	 * The method returns the reference type as string
	 * 
	 * @return refType
	 */
	public String getRefType() {
		return refType;
	}

	/**
	 * The method sets the global variable refType
	 * 
	 * @param refType
	 */
	private void setRefType(char refType) {
		this.refType = Character.toString(refType);
	}
	
	/**
	 * The method returns the name of the genomes source file
	 * 
	 * @return the name of the genomes source file
	 */
	public String getgSourceFileName() {
		return gSourceFileName;
	}
	
	/**
	 * The method sets the global variable gSourceFileName
	 * 
	 * @param gSourceFileName name of the genomes source file
	 */
	private void setgSourceFileName(String gSourceFileName) {
		this.gSourceFileName = gSourceFileName;
	}
	
	/**
	 * The method returns a Parameter object. codingTable and alphabetSizr are not set.
	 * 
	 * @return Parameter object
	 */
	public Parameter getParameterSet()
	{
		Parameter p = new Parameter(Integer.parseInt(this.getDelta()), 
									Integer.parseInt(this.getMinClusterSize()),
									Integer.parseInt(this.getQuorum()),
									Short.parseShort(this.getQuorumtype()),
									this.getOperationMode().charAt(0), 
									this.getRefType().charAt(0)
								   );
		
				
		return p;
	}


	//=======================================================================================//
	//================================= Constructor =========================================//
	//=======================================================================================//
	public GeneClusterResult(GeneCluster[] compRes, int minClusterSize, int delta, int quorum, int quorumType, char operationMode, char refType, String gSourceFileName) 
	{
		this.setCompResult(compRes);
		this.setMinClusterSize(minClusterSize);
		this.setDelta(delta);
		this.setQuorum(quorum);
		this.setQuorumtype(quorumType);
		this.setOperationMode(operationMode);
		this.setRefType(refType);
		this.setgSourceFileName(gSourceFileName);
	}

}
