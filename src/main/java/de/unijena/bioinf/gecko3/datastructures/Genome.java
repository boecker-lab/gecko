/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.datastructures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;


public class Genome implements Serializable {
	private static final long serialVersionUID = 370380955909547007L;

    private static final Logger logger = LoggerFactory.getLogger(Genome.class);

	private final List<Chromosome> chromosomes;
	private String name;
	
	public Genome() {
		this("");
	}
	
	public Genome(String name) {
		chromosomes = new ArrayList<>();
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
		List<Gene> geneList = new ArrayList<>(s.getStop() - (s.getStart()-1));
		for (int i=s.getStart()-1; i<s.getStop(); i++)
			geneList.add(chromosomes.get(s.getChromosome()).getGenes().get(i));
		return geneList.toArray(new Gene[geneList.size()]);
	}

    /**
     * Adds all gene families in the given SubSequence  to the given set.
     * Constructs a new @link HashSet if geneFamilies is null.
     * @param subSequence
     * @param geneFamilies
     * @return
     */
    public Set<GeneFamily> addGeneFamiliesOfSubSequence(Subsequence subSequence, Set<GeneFamily> geneFamilies){
        if (geneFamilies == null)
            geneFamilies = new HashSet<>();
        Chromosome chr = getChromosomes().get(subSequence.getChromosome());
        for (int index = subSequence.getStart()-1; index < subSequence.getStop(); index++){
            geneFamilies.add(chr.getGenes().get(index).getGeneFamily());
        }
        return geneFamilies;
    }
	
	public int getTotalGeneNumber() {
		int geneNumber = 0;
		for (Chromosome chr : chromosomes) {
			geneNumber += chr.getGenes().size();
		}
		return geneNumber;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Genome genome = (Genome) o;

        if (!chromosomes.equals(genome.chromosomes)) return false;
        if (!name.equals(genome.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = chromosomes.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public String toString() {
		if (getChromosomes().size()>1)
				return getName() + " [and more...]";
		else
			return getName();
	}

}
