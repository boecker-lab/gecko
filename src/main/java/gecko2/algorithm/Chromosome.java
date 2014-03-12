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

    private static int[] toIntArray(List<Gene> genes, boolean addZeros, boolean abs, boolean useReduction) {
        int array[] = addZeros?new int[genes.size()+2]:new int[genes.size()];
        final int offset = addZeros?1:0;

        if (addZeros) {
            array[0] = 0;
            array[array.length-1] = 0;
        }

        for (int i=0;i<genes.size();i++) {
            int geneId = useReduction?genes.get(i).getHomologyId():genes.get(i).getId();
            array[i+offset] = abs?Math.abs(geneId):geneId;
        }

        return array;
    }

    public int[] toIntArray(boolean addZeros, boolean abs) {
        return toIntArray(genes, addZeros, abs, false);
	}

    public int[] toReducedIntArray(boolean addZeros) {
        return toIntArray(genes, addZeros, true, true);
    }
	
	public int[] toRandomIntArray(boolean addZeros, boolean abs) {
		List<Gene> tmp = new ArrayList<>(genes);
		Collections.shuffle(tmp);
        return toIntArray(tmp, addZeros, abs, false);
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
