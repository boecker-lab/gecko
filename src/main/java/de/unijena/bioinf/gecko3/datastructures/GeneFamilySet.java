/*
 * Copyright 2014 Sascha Winter
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

package de.unijena.bioinf.gecko3.datastructures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by swinter on 18.12.2014.
 */
public class GeneFamilySet {
    private final Set<GeneFamily> knowGeneFamilies;
    private final GeneFamily unknownGeneFamily;
    private int numberOfGeneFamiliesWithMultipleGenes;

    /**
     * Just a lazy initialized helper for getGeneFamily
     */
    private final Map<String, GeneFamily> geneFamilyMap;

    /**
     * Initialises a new Gene family set
     */
    public GeneFamilySet() {
        knowGeneFamilies = new HashSet<>();
        unknownGeneFamily = GeneFamily.getNewUnknownGeneFamily();
        numberOfGeneFamiliesWithMultipleGenes = 0;
        geneFamilyMap = new HashMap<>();
        geneFamilyMap.put(unknownGeneFamily.getExternalId(), unknownGeneFamily);
    }

    public int getNumberOfGeneFamiliesWithMultipleGenes() {
        return numberOfGeneFamiliesWithMultipleGenes;
    }

    /**
     * Adds a gene for the given String id, creating a new GeneFamily or expanding an existing one
     * @param id
     * @return the GeneFamily for the given String id
     */
    public GeneFamily addGene(String id) {
        if (id.equals(GeneFamily.UNKNOWN_GENE_ID)) {
            GeneFamily geneFamily = unknownGeneFamily;
            addGene(geneFamily);
            return geneFamily;
        } else{
            if (!geneFamilyMap.containsKey(id)) {
                GeneFamily geneFamily = new GeneFamily(id);
                geneFamilyMap.put(id, geneFamily);
                knowGeneFamilies.add(geneFamily);
                return geneFamily;
            } else {
                GeneFamily geneFamily = geneFamilyMap.get(id);
                addGene(geneFamily);
                return geneFamily;
            }
        }
    }

    void addGene(GeneFamily geneFamily){
        if (geneFamily.getAlgorithmId() == -1 && !geneFamily.getExternalId().equals(GeneFamily.UNKNOWN_GENE_ID)) {
            this.numberOfGeneFamiliesWithMultipleGenes++;
            geneFamily.setAlgorithmId(numberOfGeneFamiliesWithMultipleGenes);
        }
        geneFamily.addGene();
    }

    public int getCompleteAlphabetSize() {
        return knowGeneFamilies.size() + unknownGeneFamily.getFamilySize();
    }

    public GeneFamily getUnknownGeneFamily() {
        return unknownGeneFamily;
    }

    public Set<GeneFamily> getKnowGeneFamilies() {
        return knowGeneFamilies;
    }

    public GeneFamily getGeneFamily(String externalId){
        String id = GeneFamily.convertToValidIdFormat(externalId);
        return geneFamilyMap.get(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneFamilySet that = (GeneFamilySet) o;

        if (numberOfGeneFamiliesWithMultipleGenes != that.numberOfGeneFamiliesWithMultipleGenes) return false;
        if (!knowGeneFamilies.equals(that.knowGeneFamilies)) return false;
        if (!unknownGeneFamily.equals(that.unknownGeneFamily)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = knowGeneFamilies.hashCode();
        result = 31 * result + unknownGeneFamily.hashCode();
        result = 31 * result + numberOfGeneFamiliesWithMultipleGenes;
        return result;
    }
}
