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

package de.unijena.bioinf.gecko3.gui;

import de.unijena.bioinf.gecko3.datastructures.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * All information concerning a specific cluster location, including with occ is selected.
 */
public class GeneClusterLocationSelection {
    private final GeneCluster cluster;
    private final Genome[] genomes;
    private final int[] subselection;
    private final boolean includeSubOptimalOccurrences;
    private final boolean[] flipped;

    /*
     * Cluster alignment information, might be null
     */
    private final GeneFamily alignmentGeneFamily;
    private final int[] alignmentGeneCluster;
    private final int[] alignmentGeneChromosome;

    /*
     * Painting specific information
     */
    private int paintWidth;
    private int leftPaintWidth;
    private int[] paintOffset;

    public GeneClusterLocationSelection(Genome[] genomes, GeneCluster cluster, int[] subselection, boolean includeSubOptimalOccurrences){
        this(genomes, cluster, subselection, includeSubOptimalOccurrences, null, null, null, null);
    }

    public GeneClusterLocationSelection(Genome[] genomes, GeneCluster cluster, int[] subselection, boolean includeSubOptimalOccurrences, boolean[] flipped, GeneFamily alignmentGeneFamily, int[] alignmentGeneCluster, int[] alignmentGeneChromosome) {
        this.genomes = genomes;
        this.cluster = cluster;
        this.subselection = subselection;
        this.includeSubOptimalOccurrences = includeSubOptimalOccurrences;
        this.flipped = flipped;
        this.alignmentGeneFamily = alignmentGeneFamily;
        this.alignmentGeneCluster = alignmentGeneCluster;
        this.alignmentGeneChromosome = alignmentGeneChromosome;
        this.paintWidth = -1;
    }


    /**
     * A new cluster location selection, based on the previous selections, but centered on the given GeneFamily
     * @param geneFamily
     * @return
     */
    public GeneClusterLocationSelection getGeneClusterLocationSelection(GeneFamily geneFamily) {
        return cluster.getGeneClusterLocationSelection(geneFamily, subselection, includeSubOptimalOccurrences, genomes);
    }

    public GeneCluster getCluster() {
        return cluster;
    }

    public Genome getGenome(int genomeIndex) {
        return genomes[genomeIndex];
    }

    public int[] getSubselection() {
        return subselection;
    }

    public boolean includeSubOptimalOccurrences() {
        return includeSubOptimalOccurrences;
    }

    public boolean isFlipped(int genomeIndex) {
        if (flipped == null)
            return false;
        return flipped[genomeIndex];
    }

    public Subsequence getSubsequence(int i){
        if (subselection[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
            return null;
        return cluster.getOccurrences(includeSubOptimalOccurrences).getSubsequences()[i][subselection[i]];
    }

    public List<GeneFamily> getGeneFamilies(){
        List<GeneFamily> geneFamilies = new ArrayList<>();
        Set<GeneFamily> helperSet = new HashSet<>();
        for (int i = 0; i < getTotalGenomeNumber(); i++){
            if (subselection[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                continue;
            Gene[] genes = genomes[i].getSubsequence(getSubsequence(i));
            for (Gene gene : genes){
                if (!gene.getGeneFamily().isUnknownGeneFamily() || helperSet.add(gene.getGeneFamily()))
                    geneFamilies.add(gene.getGeneFamily());
            }
        }
        return geneFamilies;
    }

    public GeneFamily getAlignmentGeneFamily() {
        return alignmentGeneFamily;
    }

    /**
     * Returns the chromosome position of the gene used for alignment, or -1 if not aligned
     * @param genomeIndex
     * @return
     */
    public int getAlignmentGenePosition(int genomeIndex){
        if (subselection[genomeIndex] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
            return -1;
        return alignmentGeneChromosome[genomeIndex];
    }

    /**
     * The total number of genomes in the dataset, including genomes not in the cluster
     * @return
     */
    public int getTotalGenomeNumber() {
        return cluster.getOccurrences(includeSubOptimalOccurrences).getSubsequences().length;
    }

    /**
     * Computes the number of genes that have to be used to paint the gene cluster.
     * Without alignment to a certain gene, that is the max number of genes in a subsequence.
     * Otherwise it is the max number of genes left and right of the aligning gene in the cluster.
     */
    private void computePaintWidths(){
        if (alignmentGeneCluster == null) {
            paintWidth = 0;
            for (int i = 0; i < getTotalGenomeNumber(); i++) {
                if (subselection[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                    continue;
                Subsequence subSequence = getSubsequence(i);
                int newWidth = subSequence.getStop() - subSequence.getStart() + 1;
                if (newWidth > paintWidth) {
                    paintWidth = newWidth;
                }
            }
        } else {
            leftPaintWidth = 0;
            int maxRightWidth = 0;
            for (int i=0; i < getTotalGenomeNumber(); i++) {
                if (subselection[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                    continue;
                Subsequence subSequence = getSubsequence(i);
                int width = subSequence.getStop() - subSequence.getStart() + 1;
                if (alignmentGeneCluster[i] != -1) {
                    if (isFlipped(i)) {
                        leftPaintWidth = Math.max(width - alignmentGeneCluster[i] - 1, leftPaintWidth);
                        maxRightWidth = Math.max(alignmentGeneCluster[i], maxRightWidth);
                    } else {
                        leftPaintWidth = Math.max(alignmentGeneCluster[i], leftPaintWidth);
                        maxRightWidth = Math.max(width - alignmentGeneCluster[i] - 1, maxRightWidth);
                    }
                } else {
                    paintWidth = Math.max(width, paintWidth);
                }
            }
            leftPaintWidth = Math.max(leftPaintWidth, paintWidth/2);
            paintWidth = Math.max(leftPaintWidth + maxRightWidth + 1, paintWidth);
        }
    }

    private void computePaintOffsets() {
        if (paintWidth < 0)
            computePaintWidths();
        paintOffset = new int[getTotalGenomeNumber()];
        for (int i = 0; i < getTotalGenomeNumber(); i++) {
            if (subselection[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
                continue;
            Subsequence subSequence = getSubsequence(i);
            int seqLength = subSequence.getStop() - subSequence.getStart() + 1;
            if (alignmentGeneCluster == null || alignmentGeneCluster[i] == -1) {
                // half cluster sequence + 1 if number of genes in the sequence is no straight
                if (paintWidth % 2 == 0)
                    paintOffset[i] = paintWidth/2 - (seqLength/2 + seqLength%2);
                else
                    paintOffset[i] = paintWidth/2 - seqLength/2;
            } else {
                if (isFlipped(i))
                    paintOffset[i] = leftPaintWidth-(seqLength- alignmentGeneCluster[i]);
                else
                    paintOffset[i] = leftPaintWidth- alignmentGeneCluster[i];
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

    public GeneForPainting[] getGenesForPainting(final int genomeIndex, final int NR_ADDITIONAL_GENES) {
        GeneForPainting[] geneArray = new GeneForPainting[getMaxClusterLocationWidth() + 2 * NR_ADDITIONAL_GENES];

        // determine start setOff if not in refPaintGenome
        int setOff = NR_ADDITIONAL_GENES + getGeneOffset(genomeIndex);
        Subsequence subsequence = getSubsequence(genomeIndex);
        if (!subsequence.isValid())
            return geneArray;

        int geneIndex;
        if (isFlipped(genomeIndex)){
            geneIndex = subsequence.getStop() + setOff;
        } else {
            geneIndex = subsequence.getStart() - 1 - setOff; // Paint setOff many additional genes (or gaps) that are not part of the cluster
        }

        for (int paintIndex = 0; paintIndex < getMaxClusterLocationWidth() + 2 * NR_ADDITIONAL_GENES; paintIndex++) {
            if (!isFlipped(genomeIndex)) {
                if (geneIndex < -1)  // skip not in chromosome
                    geneArray[paintIndex] = null;
                else if (geneIndex == -1)
                    geneArray[paintIndex] = GeneForPainting.getChromosomeStart();
                else if (geneIndex >= getGenome(genomeIndex).getChromosomes().get(subsequence.getChromosome()).getGenes().size()) {
                    geneArray[paintIndex] = GeneForPainting.getChromosomeEnd();
                    break;
                } else {
                    boolean partOfCluster = subsequence.getStart() - 1 <= geneIndex && geneIndex < subsequence.getStop();
                    geneArray[paintIndex] = new GeneForPainting(
                            getGenome(genomeIndex).getChromosomes().get(subsequence.getChromosome()).getGenes().get(geneIndex),
                            partOfCluster);
                }
                geneIndex++;
            } else {
                if (geneIndex > getGenome(genomeIndex).getChromosomes().get(subsequence.getChromosome()).getGenes().size()) // skip not in chromosome
                    geneArray[paintIndex] = null;
                else if (geneIndex == getGenome(genomeIndex).getChromosomes().get(subsequence.getChromosome()).getGenes().size())
                    geneArray[paintIndex] = GeneForPainting.getChromosomeStart();
                else if (geneIndex == -1) {
                    geneArray[paintIndex] = GeneForPainting.getChromosomeEnd();
                    break;
                } else {
                    boolean partOfCluster = subsequence.getStart() - 1 <= geneIndex && geneIndex < subsequence.getStop();
                    geneArray[paintIndex] = new GeneForPainting(
                            getGenome(genomeIndex).getChromosomes().get(subsequence.getChromosome()).getGenes().get(geneIndex),
                            partOfCluster);
                }
                geneIndex--;
            }
        }
        return geneArray;
    }
}
