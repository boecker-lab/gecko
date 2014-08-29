package gecko2.algo.status;

import java.util.EventListener;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public interface AlgorithmProgressListener extends EventListener{
    public void algorithmProgressUpdate(AlgorithmStatusEvent statusEvent);
}
