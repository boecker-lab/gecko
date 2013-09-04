package gecko2.algorithm;

import gecko2.GeckoInstance;

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
	
	public int[] toIntArray(boolean addZeros, boolean abs) {
		int array[];
		if (!addZeros) {
			array = new int[genes.size()];
			if (abs)
				for (int i=0;i<array.length;i++) 
					array[i] = Math.abs(genes.get(i).getId());
			else
				for (int i=0;i<array.length;i++) 
					array[i] = genes.get(i).getId();
		} else {
			array = new int[genes.size()+2];
			array[0] = 0;
			if (abs)
				for (int i=1;i<array.length-1;i++) 
					array[i] = Math.abs(genes.get(i-1).getId());
			else
				for (int i=1;i<array.length-1;i++) 
					array[i] = genes.get(i-1).getId();
			array[array.length-1]=0;
		}
		return array;
	}
	
	public int[] toRandomIntArray(boolean addZeros, boolean abs) {
		int array[];
		List<Gene> tmp = new ArrayList<Gene>(genes);
		Collections.shuffle(tmp);
		if (!addZeros) {
			array = new int[genes.size()];
			if (abs)
				for (int i=0;i<array.length;i++) 
					array[i] = Math.abs(tmp.get(i).getId());
			else
				for (int i=0;i<array.length;i++) 
					array[i] = tmp.get(i).getId();
		} else {
			array = new int[tmp.size()+2];
			array[0] = 0;
			if (abs)
				for (int i=1;i<array.length-1;i++) 
					array[i] = Math.abs(tmp.get(i-1).getId());
			else
				for (int i=1;i<array.length-1;i++) 
					array[i] = tmp.get(i-1).getId();
			array[array.length-1]=0;
		}
		return array;
	}
	
	public int[] toIntArray() {
		return this.toIntArray(false, false);
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
