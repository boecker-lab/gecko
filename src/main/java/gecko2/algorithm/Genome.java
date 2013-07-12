package gecko2.algorithm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Genome implements Serializable {

	private static final long serialVersionUID = 370380955909547007L;

	private List<Chromosome> chromosomes;
	private String name;
	
	public Genome() {
		this("");
	}
	
	public Genome(String name) {
		chromosomes = new ArrayList<Chromosome>();
		this.name = name;
	}
	
	public void setName(String name) {
		
		this.name = name;
	}
	
	public String getName() {
		
		return this.name;
	}
	
	/**
	 * Gets the full combined name of the genome + the chromosome
	 * @param nr the nr of the chromosome
	 * @return the name, either "genomeName + " " + chromName" or "genomeName", if chromName is "".
	 */
	public String getFullChromosomeName(int nr){
		Chromosome chr = chromosomes.get(nr);
		if (chr.getName().equals(""))
			return name;
		else
			return name + " " + chr.getName();
	}
	
	public List<Chromosome> getChromosomes() {
		return Collections.unmodifiableList(chromosomes);
	}
	
	public void addChromosome(Chromosome chr) {
		chromosomes.add(chr);
	}

	public Gene[] getSubsequence(Subsequence s) {
		List<Gene> geneList = new ArrayList<Gene>(s.getStop() - (s.getStart()-1));
		for (int i=s.getStart()-1; i<s.getStop(); i++) 
			geneList.add(chromosomes.get(s.getChromosome()).getGenes().get(i));
		return geneList.toArray(new Gene[0]);
	}
	
	public int getTotalGeneNumber() {
		int geneNumber = 0;
		for (Chromosome chr : chromosomes) {
			geneNumber += chr.getGenes().size();
		}
		return geneNumber;
	}
	
	/**
	 * Generates an int array from the genomes
	 * @param genomes the genomes
	 * @return an int array, containing all the genes
	 */
	public static int[][][] toIntArray(Genome[] genomes) {
		int genomeArray[][][] = new int[genomes.length][][];
		for (int i=0;i<genomes.length;i++) {
			genomeArray[i] = new int[genomes[i].getChromosomes().size()][];
			for (int j=0;j<genomeArray[i].length;j++)
				genomeArray[i][j] = genomes[i].getChromosomes().get(j).toIntArray(true, true);
		}
		return genomeArray;
	}

}
