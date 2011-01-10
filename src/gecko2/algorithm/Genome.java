package gecko2.algorithm;

import java.io.Serializable;
import java.util.ArrayList;


public class Genome implements Serializable {

	private static final long serialVersionUID = 370380955909547007L;

	private ArrayList<Chromosome> chromosomes;
	
	public Genome() {
		chromosomes = new ArrayList<Chromosome>();
	}
		
	public ArrayList<Chromosome> getChromosomes() {
		return chromosomes;
	}
	

}
