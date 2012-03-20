package gecko2.algorithm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Genome implements Serializable {

	private static final long serialVersionUID = 370380955909547007L;

	private List<Chromosome> chromosomes;
	
	public Genome() {
		chromosomes = new ArrayList<Chromosome>();
	}
		
	public List<Chromosome> getChromosomes() {
		return Collections.unmodifiableList(chromosomes);
	}
	
	public void addChromosome(Chromosome chr) {
		chromosomes.add(chr);
	}

	public Gene[] getSubsequence(Subsequence s) {
		List<Gene> geneList = new ArrayList<Gene>();
		for (int i=s.getStart()-1; i<s.getStop(); i++) 
			geneList.add(chromosomes.get(s.getChromosome()).getGenes().get(i));
		return geneList.toArray(new Gene[0]);
	}

}
