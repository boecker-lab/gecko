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

import de.unijena.bioinf.gecko3.datastructures.Gene;

/**
 * A gene used for painting, with the additional info, if it is part of a cluster
 */
public class GeneForPainting {
    public static final GeneForPainting CHROMOSOME_START = new GeneForPainting(null, false, true, false);
    public static final GeneForPainting CHROMOSOME_END = new GeneForPainting(null, false, false, true);

    private final Gene gene;
    private final boolean partOfCluster;
    private final boolean chromosome_start;
    private final boolean chromosome_end;

    public GeneForPainting(Gene gene, boolean partOfCluster) {
        this(gene, partOfCluster, false, false);
    }

    private GeneForPainting(Gene gene, boolean partOfCluster, boolean chromosome_start, boolean chromosome_end){
        this.gene = gene;
        this.partOfCluster = partOfCluster;
        this.chromosome_start = chromosome_start;
        this.chromosome_end = chromosome_end;
    }

    public static GeneForPainting getChromosomeStart() {
        return CHROMOSOME_START;
    }

    public static GeneForPainting getChromosomeEnd() {
        return CHROMOSOME_END;
    }

    public Gene getGene() {
        return gene;
    }

    public boolean isPartOfCluster() {
        return partOfCluster;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneForPainting that = (GeneForPainting) o;

        if (chromosome_end != that.chromosome_end) return false;
        if (chromosome_start != that.chromosome_start) return false;
        if (partOfCluster != that.partOfCluster) return false;
        if (gene != null ? !gene.equals(that.gene) : that.gene != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = gene != null ? gene.hashCode() : 0;
        result = 31 * result + (partOfCluster ? 1 : 0);
        result = 31 * result + (chromosome_start ? 1 : 0);
        result = 31 * result + (chromosome_end ? 1 : 0);
        return result;
    }
}
