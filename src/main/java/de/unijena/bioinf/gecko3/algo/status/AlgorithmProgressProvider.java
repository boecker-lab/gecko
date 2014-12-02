package de.unijena.bioinf.gecko3.algo.status;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public interface AlgorithmProgressProvider {
    public void addListener(AlgorithmProgressListener listener);
    public void removeListener(AlgorithmProgressListener listener);
}
