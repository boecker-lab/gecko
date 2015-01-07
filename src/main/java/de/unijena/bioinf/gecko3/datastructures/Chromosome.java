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

import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.util.MutableInteger;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Chromosome implements Serializable {
	
	private static final long serialVersionUID = -1724388125243376854L;
	private final MouseListener chromosomeMouseListener;
	
	private final Genome parent;
	private String name;
	private List<Gene> genes;
	
	public Chromosome(String name, Genome parent) {
		this.name = name;
		this.genes = null;
		this.parent = parent;
		this.chromosomeMouseListener = new ChromosomeMouseListener();
	}
	
	public Chromosome(String name, List<Gene> genes, Genome parent) {
		this.name = name;
		this.genes = genes;
		this.parent = parent;
		this.chromosomeMouseListener = new ChromosomeMouseListener();
	}
	
	public MouseListener getChromosomeMouseListener() {
		return chromosomeMouseListener;
	}

	public void setGenes(List<Gene> genes) {
		this.genes = genes;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public List<Gene> getGenes() {
		return genes;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chromosome that = (Chromosome) o;

        if (!genes.equals(that.genes)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + genes.hashCode();
        return result;
    }

    /**
     * Returns the int array from the list of genes.
     * @param genes the list of Gene
     * @param unHomologueGeneFamilyId the first id that is used for un-homologue genes
     * @param addZeros if the array should begin and end with 0
     * @param abs use absolute gene ids, incompatible with useReduction
     * @return the array of gene ids
     */
    private static int[] toIntArray(List<Gene> genes, MutableInteger unHomologueGeneFamilyId, boolean addZeros, boolean abs) {
        int array[] = addZeros?new int[genes.size()+2]:new int[genes.size()];
        final int offset = addZeros?1:0;

        if (addZeros) {
            array[0] = 0;
            array[array.length-1] = 0;
        }

        for (int i=0;i<genes.size();i++) {
            int family;
            if (genes.get(i).isUnknown()) {
                family = unHomologueGeneFamilyId.getValue();
                unHomologueGeneFamilyId.setValue(family + 1);
            } else {
                family = genes.get(i).getAlgorithmId();
            }
            array[i+offset] = abs ? family : family * genes.get(i).getOrientation().getSign();
        }

        return array;
    }

    /**
     * Returns the int array for this chromosome. All un-homologue genes will have id -1
     * @param addZeros if the array should begin and end with 0
     * @return the array of gene ids
     */
    public int[] toReducedIntArray(boolean addZeros) {
        List<Integer> ar = new ArrayList<>(genes.size()+2);

        if (addZeros)
            ar.add(0);
        for (int i=0;i<genes.size();i++) {
            if(!genes.get(i).isUnknown()){
                ar.add(genes.get(i).getAlgorithmId());
            } else if(ar.get(ar.size()-1)<=-1){
                ar.set(ar.size()-1,ar.get(ar.size()-1)-1);
            } else {
                ar.add(-1);
            }
        }
        if (addZeros)
            ar.add(0);

        int array[] = new int[ar.size()];
        for(int i=0;i<ar.size();i++){
            array[i] = ar.get(i);
        }
        return array;
    }

    public int[] toIntArray(MutableInteger unHomologueGeneFamilyId, boolean addZeros, boolean abs) {
        return toIntArray(genes, unHomologueGeneFamilyId, addZeros, abs);
	}

    public int[] toRandomIntArray(MutableInteger unHomologueGeneFamilyId, boolean addZeros, boolean abs) {
        List<Gene> tmp = new ArrayList<>(genes);
        Collections.shuffle(tmp);
        return toIntArray(tmp, unHomologueGeneFamilyId, addZeros, abs);
    }
	
	private Genome getParent() {
		return parent;
	}
	
	public String getFullName() {
		if (getParent().getName() == null)
			return getName();
		else if (getName() == null)
			return getParent().getName();
		else if (getParent().getName().equals(getName()))
			return getName();
		else 
			return getParent().getName() + " " + getName();
	}
	
	private class ChromosomeMouseListener extends MouseAdapter implements Serializable {
		
		private static final long serialVersionUID = -1016912396598206487L;

		@Override
		public void mouseEntered(MouseEvent e) {
			
			String infotext = "";
					
			if (Chromosome.this.getChromosomeMouseListener().equals(Chromosome.this.chromosomeMouseListener)) {
					
				infotext = getFullName();
			}
			
			GeckoInstance.getInstance().getGui().setInfobarText(infotext);
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			super.mouseExited(e);
			GeckoInstance.getInstance().getGui().setInfobarText("");
		}
	}
}
