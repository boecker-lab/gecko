package gecko2.algorithm;

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
