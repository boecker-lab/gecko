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

import static org.junit.Assert.assertEquals;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class GeneFamilyTest {

    /**
     * Returns gene families constructed for the pairs (externalId[i], geneFamilySize[i])
     * The algorithm ids will be matching for the external ids, but might not directly match the ones produced
     * by real readers
     *
     * @param externalId
     * @param geneFamilySize
     * @return
     */
    public static GeneFamily[] getTestGeneFamilies(String[] externalId, int[] geneFamilySize) {
        assertEquals(externalId.length, geneFamilySize.length);
        GeneFamily[] geneFamilies = new GeneFamily[externalId.length];
        int algorithmId = 1;
        for (int i=0; i<externalId.length; i++){
            if (geneFamilySize[i] > 1)
                geneFamilies[i] = GeneFamily.getTestGeneFamily(externalId[i], algorithmId++, geneFamilySize[i]);
            else
                geneFamilies[i] = GeneFamily.getTestGeneFamily(externalId[i], -1, geneFamilySize[i]);
        }
        return geneFamilies;
    }
}
