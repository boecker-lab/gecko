package gecko2.gui;

import gecko2.datastructures.GeneFamily;
import gecko2.datastructures.Genome;
import gecko2.event.ClusterSelectionListener;

import javax.swing.*;

public interface MultipleGenomesBrowserInterface extends ClusterSelectionListener{
	public void addSelectionListener(ClusterSelectionListener s);
	public void fireBrowserContentChanged(short type);
	
	public void clear();
	public void clearSelection();

    /**
     * Sets the genomes to the given array of Genomes, clearing the old genomes.
     * If the array is null, only clears the old genomes.
     * @param g
     */
	public void setGenomes(Genome[] g);
    public void updateGeneSize();
    public void changeNameType(GenomePainting.NameType nameType);

	public void centerCurrentClusterAt(GeneFamily geneFamily);
    public GeneClusterLocationSelection getClusterSelection();
	public void changeGeneElementHight(int adjustment);
	public void hideNonClusteredGenomes(boolean hide);

    public JPanel getBody();
}
