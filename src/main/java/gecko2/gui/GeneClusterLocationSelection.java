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
    private final int[] alignmentGene;

    private int refPaintGenome;
    private int paintWidth;
    private int leftPaintWidth;
    private int[] paintOffset;

    public GeneClusterLocationSelection(GeneCluster cluster, int[] subselection, boolean includeSubOptimalOccurrences, boolean[] flipped, int[] alignmentGene) {
        this.cluster = cluster;
        this.subselection = subselection;
        this.includeSubOptimalOccurrences = includeSubOptimalOccurrences;
        this.flipped = flipped;
        this.alignmentGene = alignmentGene;
        this.refPaintGenome = -1;
        this.paintWidth = -1;
    }

    public GeneCluster getCluster() {
        return cluster;
    }

    public int[] getSubselection() {
        return subselection;
    }

    public boolean isFlipped(int genomeIndex) {
        return flipped[genomeIndex];
    }

    public Subsequence getSubsequence(int i){
        if (subselection[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
            return null;
        return cluster.getOccurrences(includeSubOptimalOccurrences).getSubsequences()[i][subselection[i]];
    }

    public int getSubsequenceLength() {
        return cluster.getOccurrences(includeSubOptimalOccurrences).getSubsequences().length;
    }

    private void computePaintWidths(){
        if (alignmentGene == null) {
            paintWidth = 0;
            for (int i = 0; i < getSubsequenceLength(); i++) {
                if (subselection[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                    continue;
                Subsequence subSequence = getSubsequence(i);
                int newWidth = subSequence.getStop() - subSequence.getStart() + 1;
                if (newWidth > paintWidth) {
                    paintWidth = newWidth;
                    refPaintGenome = i;
                }
            }
        } else {
            leftPaintWidth = 0;
            int rigthWidth = 0;
            for (int i=0; i < getSubsequenceLength(); i++) {
                if (subselection[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                    continue;
                Subsequence subSequence = getSubsequence(i);
                int width = subSequence.getStop() - subSequence.getStart() + 1;
                if (alignmentGene[i] != -1) {
                    if (isFlipped(i)) {
                        leftPaintWidth = Math.max(width - alignmentGene[i], leftPaintWidth);
                        rigthWidth = Math.max(alignmentGene[i], rigthWidth);
                    } else {
                        leftPaintWidth = Math.max(alignmentGene[i], leftPaintWidth);
                        rigthWidth = Math.max(width - alignmentGene[i], rigthWidth);
                    }
                } else {
                    paintWidth = Math.max(width, paintWidth);
                }
            }
            leftPaintWidth = Math.max(leftPaintWidth, paintWidth/2);
            paintWidth = Math.max(leftPaintWidth + rigthWidth, paintWidth);
        }
    }

    private void computePaintOffsets() {
        if (paintWidth < 0)
            computePaintWidths();
        paintOffset = new int[getSubsequenceLength()];
        for (int i = 0; i < getSubsequenceLength(); i++) {
            if (subselection[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                continue;
            Subsequence subSequence = getSubsequence(i);
            int seqLength = subSequence.getStop() - subSequence.getStart() + 1;
            if (alignmentGene == null || alignmentGene[i] == -1) {
                // half cluster sequence + 1 if number of genes in the sequence is no straight
                if (paintWidth % 2 == 0)
                    paintOffset[i] = paintWidth/2 - (seqLength/2 + seqLength%2);
                else
                    paintOffset[i] = paintWidth/2 - seqLength/2;
            } else {
                if (isFlipped(i))
                    paintOffset[i] = leftPaintWidth-(seqLength-alignmentGene[i]);
                else
                    paintOffset[i] = leftPaintWidth-alignmentGene[i];
            }
        }
    }

    /**
     * Gives the width (number of genes) of the cluster location, taking into account the gene alignment
     * @return
     */
    public int getMaxClusterLocationWidth(){
        if (paintWidth < 0)
            computePaintWidths();
        return paintWidth;
    }

    public int getGeneOffset(int genomeIndex) {
        if (paintOffset == null)
            computePaintOffsets();
        return paintOffset[genomeIndex];
    }
}
