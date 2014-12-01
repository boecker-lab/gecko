package gecko2.gui;

import gecko2.datastructures.GeneFamily;
import gecko2.datastructures.Genome;
import gecko2.event.ClusterSelectionListener;

import javax.swing.*;

public interface MultipleGenomesBrowserInterface extends ClusterSelectionListener{
	public void addSelectionListener(ClusterSelectionListener s);
	public void fireBrowserContentChanged(short type);
	
	public void clear();
    /**
     * Sets the genomes to the given array of Genomes, clearing the old genomes.
     * If the array is null, only clears the old genomes.
     * @param g
     */
    public void setGenomes(Genome[] g);

	public void clearSelection();

    public void updateGeneWidth();
    public void changeGeneElementHeight(int adjustment);
    public int getGeneElementHeight();
    public void changeNameType(GenomePainting.NameType nameType);
    public boolean canZoomIn();
    public boolean canZoomOut();

	public void centerCurrentClusterAt(GeneFamily geneFamily);
    public GeneClusterLocationSelection getClusterSelection();
	public void hideNonClusteredGenomes(boolean hide);

    public JPanel getBody();
}
