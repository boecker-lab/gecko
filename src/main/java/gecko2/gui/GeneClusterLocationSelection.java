package gecko2.gui;

import gecko2.datastructures.GeneCluster;

/**
 * Created by swinter on 18.11.2014.
 */
public class GeneClusterLocationSelection {
    private final GeneCluster cluster;
    private final int[] subselection;
    private final boolean[] flipped;
    private final int[] centerGene;

    public GeneClusterLocationSelection(GeneCluster cluster, int[] subselection, boolean[] flipped, int[] centerGene) {
        this.cluster = cluster;
        this.subselection = subselection;
        this.flipped = flipped;
        this.centerGene = centerGene;
    }

    public GeneCluster getCluster() {
        return cluster;
    }

    public int[] getSubselection() {
        return subselection;
    }

    public boolean[] isFlipped() {
        return flipped;
    }

    public int[] getCenterGene() {
        return centerGene;
    }
}
