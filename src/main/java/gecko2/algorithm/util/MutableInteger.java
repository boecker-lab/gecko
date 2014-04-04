package gecko2.algorithm.util;

/**
 * A mutable integer class
 *
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class MutableInteger {
    int value;

    public MutableInteger(int value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
