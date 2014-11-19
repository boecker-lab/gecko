package gecko2.gui;

import gecko2.datastructures.GeneCluster;
import gecko2.datastructures.GeneClusterOccurrence;
import gecko2.datastructures.Subsequence;

/**
 * Created by swinter on 18.11.2014.
 */
public class GeneClusterLocationSelection {
    private final GeneCluster cluster;
    private final int[] subselection;
    private final boolean includeSubOptimalOccurrences;
    private final boolean[] flipped;
    private final int[] centerGene;

    public GeneClusterLocationSelection(GeneCluster cluster, int[] subselection, boolean includeSubOptimalOccurrences, boolean[] flipped, int[] centerGene) {
        this.cluster = cluster;
        this.subselection = subselection;
        this.includeSubOptimalOccurrences = includeSubOptimalOccurrences;
        this.flipped = flipped;
        this.centerGene = centerGene;
    }

    public GeneCluster getCluster() {
        return cluster;
    }

    public int[] getSubselection() {
        return subselection;
    }

    public boolean isIncludeSubOptimalOccurrences() {
        return includeSubOptimalOccurrences;
    }

    public boolean[] isFlipped() {
        return flipped;
    }

    public int[] getCenterGene() {
        return centerGene;
    }

    public Subsequence getSubsequence(int i){
        if (subselection[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
            return null;
        return cluster.getOccurrences(includeSubOptimalOccurrences).getSubsequences()[i][subselection[i]];
    }

    public int getSubsequenceLength() {
        return cluster.getOccurrences(includeSubOptimalOccurrences).getSubsequences().length;
    }
}
