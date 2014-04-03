package gecko2.testUtils;

import gecko2.testUtils.ExpectedDeltaLocationValues;

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

    public ExpectedReferenceClusterValues(List<Integer> geneContent, int[] minimumDistances, int genomeNr, int chrNr, int coveredGenomes, ExpectedDeltaLocationValues[][] allDeltaLocations) {
        this.geneContent = geneContent;
        this.bestCombined_pValue = null;
        this.bestCombined_pValueCorrected = null;
        this.minimumDistances = minimumDistances;
        this.genomeNr = genomeNr;
        this.chrNr = chrNr;
        this.coveredGenomes = coveredGenomes;
        this.allDeltaLocations = allDeltaLocations;
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
    }

    public List<Integer> getGeneContent() {
        return geneContent;
    }

    public int getSize() {
        return geneContent.size();
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
