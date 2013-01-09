package gecko2.algorithm;

/**
 * @author Leon Kuchenbecker <leon.kuchenbecker@fu-berlin.de>
 *
 */
public class ClusterRequest {
	
	private int d;
	private String[] geneSequences;
	@SuppressWarnings("unused")
	private int nsequences;
	
	public ClusterRequest(int d, String[] geneSequences) {
		super();
		this.d = d;
		this.geneSequences = geneSequences;
		this.nsequences = this.geneSequences.length;
	}
	
	public int getD() {
		return d;
	}
	public void setD(int d) {
		this.d = d;
	}
	public String[] getGeneSequences() {
		return geneSequences;
	}
	public void setGeneSequences(String[] geneSequences) {
		this.geneSequences = geneSequences;
		this.nsequences = geneSequences.length;
	}
	
	

}
