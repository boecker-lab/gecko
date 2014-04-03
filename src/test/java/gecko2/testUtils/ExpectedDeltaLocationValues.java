package gecko2.testUtils;

/**
* @author Sascha Winter (sascha.winter@uni-jena.de)
*/
public class ExpectedDeltaLocationValues {
    private final int chrNr;
    private final int l;
    private final int r;
    private final int distance;
    private final double pValue;

    public ExpectedDeltaLocationValues(int chrNr, int l, int r, int distance) {
        this.chrNr = chrNr;
        this.l = l;
        this.r = r;
        this.distance = distance;
        this.pValue = -1.0;
    }

    public ExpectedDeltaLocationValues(int chrNr, int l, int r, int distance, double pValue) {
        this.chrNr = chrNr;
        this.l = l;
        this.r = r;
        this.distance = distance;
        this.pValue = pValue;
    }

    public int getChrNr() {
        return chrNr;
    }

    public int getL() {
        return l;
    }

    public int getR() {
        return r;
    }

    public int getDistance() {
        return distance;
    }

    public double getpValue() {
        return pValue;
    }
}
