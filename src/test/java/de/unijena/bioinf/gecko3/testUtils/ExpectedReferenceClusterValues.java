package de.unijena.bioinf.gecko3.testUtils;

import java.math.BigDecimal;
import java.util.List;

/**
* @author Sascha Winter (sascha.winter@uni-jena.de)
*/
public class ExpectedReferenceClusterValues {
    private final List<Integer> geneContent;
    private final BigDecimal bestCombined_pValue;
    private final BigDecimal bestCombined_pValueCorrected;
    private final int[] minimumDistances;
    private final int genomeNr;
    private final int chrNr;
    private final int coveredGenomes;
    private final ExpectedDeltaLocationValues[][] allDeltaLocations;
    private final int size;

    public ExpectedReferenceClusterValues(List<Integer> geneContent, int[] minimumDistances, int genomeNr, int chrNr, int coveredGenomes, ExpectedDeltaLocationValues[][] allDeltaLocations) {
        this(geneContent, null, null, minimumDistances, genomeNr, chrNr, coveredGenomes, allDeltaLocations);
    }

    public ExpectedReferenceClusterValues(List<Integer> geneContent, BigDecimal bestCombined_pValue, BigDecimal bestCombined_pValueCorrected, int[] minimumDistances, int genomeNr, int chrNr, int coveredGenomes, ExpectedDeltaLocationValues[][] allDeltaLocations) {
        this.geneContent = geneContent;
        this.bestCombined_pValue = bestCombined_pValue;
        this.bestCombined_pValueCorrected = bestCombined_pValueCorrected;
        this.minimumDistances = minimumDistances;
        this.genomeNr = genomeNr;
        this.chrNr = chrNr;
        this.coveredGenomes = coveredGenomes;
        this.allDeltaLocations = allDeltaLocations;
        int tmpSize = 0;
        for (Integer i : geneContent){
            if (i<0)
                tmpSize -= i;
            else
                tmpSize++;
        }
        this.size = tmpSize;
    }

    public List<Integer> getGeneContent() {
        return geneContent;
    }

    public int getSize() {
        return size;
    }

    public BigDecimal getBestCombined_pValue() {
        return bestCombined_pValue;
    }

    public BigDecimal getBestCombined_pValueCorrected() {
        return bestCombined_pValueCorrected;
    }

    public int[] getMinimumDistances() {
        return minimumDistances;
    }

    public int getGenomeNr() {
        return genomeNr;
    }

    public int getChrNr() {
        return chrNr;
    }

    public int getCoveredGenomes() {
        return coveredGenomes;
    }

    public ExpectedDeltaLocationValues[][] getAllDeltaLocations() {
        return allDeltaLocations;
    }
}
