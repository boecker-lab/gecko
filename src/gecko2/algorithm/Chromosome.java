package gecko2.algorithm;

import gecko2.GeckoInstance;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Chromosome implements Serializable {
	
	private static final long serialVersionUID = -1724388125243376854L;
	private MouseListener chromosomeMouseListener;
	
	private String name;
	private List<Gene> genes;
	
	// Debug
	public static Chromosome createSampleGenome(String name, int minid, int maxid) {
		List<Gene> genes = new ArrayList<Gene>();
		for (int i=minid; i<=maxid; i++)
			genes.add(new Gene("Gene "+i,i));
		return new Chromosome(name,genes);			
	}
	
	public Chromosome() {
		this (null, null);
	}
	
	public Chromosome(String name, List<Gene> genes) {
		this.name = name;
		this.genes = genes;
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
	
	public int[] toIntArray() {
		return this.toIntArray(false, false);
	}
	
	private class ChromosomeMouseListener extends MouseAdapter implements Serializable {
		
		private static final long serialVersionUID = -1016912396598206487L;

		@Override
		public void mouseEntered(MouseEvent e) {
			GeckoInstance.getInstance().getGui().setInfobarText(Chromosome.this.name);
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			super.mouseExited(e);
			GeckoInstance.getInstance().getGui().setInfobarText("");
		}

		
	}



}
