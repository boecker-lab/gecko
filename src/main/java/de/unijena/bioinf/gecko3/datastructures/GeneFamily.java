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

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Gene family id information.
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class GeneFamily implements Comparable<GeneFamily>{
    public static final String UNKNOWN_GENE_ID = "0";

    private int familySize;
    private final String externalId;
    private int algorithmId;

    /**
     * A new gene family with a given external id, family size 1 and algorithm id -1.
     * @param externalId the String externalId
     */
    GeneFamily(String externalId) {
        this(externalId, -1, 1);
    }

    /**
     * A new gene family with a given external id, family size and algorithm id
     * @param externalId the String externalId
     * @param algorithmId the the new algorithm id
     * @param familySize the size of the gene family
     */
    private GeneFamily(String externalId, int algorithmId, int familySize) {
        this.familySize = familySize;
        this.externalId = externalId;
        this.algorithmId = algorithmId;
    }

    /**
     * Only for Unit Testing
     * @param externalId
     * @param algorithmId
     * @param familySize
     * @return
     */
    public static GeneFamily getTestGeneFamily(String externalId, int algorithmId, int familySize){
        return new GeneFamily(externalId, algorithmId, familySize);
    }

    public static GeneFamily getUnknownTestGeneFamily(int familySize){
        GeneFamily testFamily = getNewUnknownGeneFamily();
        testFamily.familySize = familySize;
        return testFamily;
    }

    static GeneFamily getNewUnknownGeneFamily(){
        return new GeneFamily(UNKNOWN_GENE_ID, -1, 0);
    }

    /*public static GeneFamily getNewUnknownGeneFamilyAndInitializeAlgorithmId() {
        algorithmIdCounter = 1;
        return new GeneFamily(UNKNOWN_GENE_ID, -1, 0);
    }*/

    /**
     * Sets the algorithm id of the geneFamily
     *
     * @param id
     */
    void setAlgorithmId(int id){
        this.algorithmId = id;
    }

    /**
     * Adds a a gene to the geneFamily.
     */
    void addGene() {
        this.familySize++;
    }

    /**
     *
     * @return the String externalId
     */
    public String getExternalId() {
        return externalId;
    }

    public int getAlgorithmId() {
        return algorithmId;
    }

    /**
     *
     * @return true
     */
    public boolean isUnknownGeneFamily() {
        return externalId.equals(UNKNOWN_GENE_ID);
    }

    /**
     *
     * @return true if the gene family contains only one gene
     */
    public boolean isSingleGeneFamily() {
        if (externalId.equals(UNKNOWN_GENE_ID))
            return true;
        return 1 == familySize;
    }

    public int getFamilySize() {
        return familySize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneFamily family = (GeneFamily) o;

        if (!externalId.equals(family.externalId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return externalId.hashCode();
    }

    @Override
    public String toString() {
        return "GeneFamily{" +
                "externalId='" + externalId + "', size=" + familySize +
                '}';
    }

    /**
     * Computes a color mapping for the given gene families to colors.
     * @param geneFamilies a collection of gene families. If you want always the same color has to have a guaranteed ordering (so not a set)
     * @param unknownGeneFamily may be null, otherwise Color.GRAY is assigned to unknown genes
     * @param randomSeed the random seed may be null.
     * @return
     */
    public static Map<GeneFamily, Color> prepareColorMap(Collection<GeneFamily> geneFamilies, GeneFamily unknownGeneFamily, Integer randomSeed) {
        if (randomSeed == null)
            Uniform.staticSetRandomEngine(new MersenneTwister());
        else
            Uniform.staticSetRandomEngine(new MersenneTwister(randomSeed));
        Map<GeneFamily, Color> colorMap = new HashMap<>();
            for (GeneFamily geneFamily : geneFamilies)
                colorMap.put(geneFamily, new Color(Uniform.staticNextIntFromTo(0,240), Uniform.staticNextIntFromTo(0,240), Uniform.staticNextIntFromTo(0,240)));
        if (unknownGeneFamily != null)
            colorMap.put(unknownGeneFamily, Color.GRAY);
        return colorMap;
    }

    @Override
    public int compareTo(GeneFamily o) {
        return externalId.compareTo(o.getExternalId());
    }

    /**
     * Converts any String id into a valid string id.
     * Removes leading 0 from integer ids and maps 0 and "" to UNKNOWN_GENE_ID
     *
     * @param id current string id
     * @return modified id
     */
    public static String convertToValidIdFormat(String id) {
        id = id.trim();
        try {
            int newID = Integer.parseInt(id);
            return Integer.toString(newID);
        } catch (NumberFormatException e) {}
        if (id.equals(""))
            return GeneFamily.UNKNOWN_GENE_ID;

        return id;
    }
}
