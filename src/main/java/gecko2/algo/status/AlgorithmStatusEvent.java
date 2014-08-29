package gecko2.algo.status;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class AlgorithmStatusEvent {
    public enum Task {
        Init, ComputingClusters, ComputingStatistics, Done
    }

    private final int progress;
    private final Task task;

    public AlgorithmStatusEvent(int progress, Task task) {
        this.progress = progress;
        this.task = task;
    }

    public int getProgress() {
        return progress;
    }

    public Task getTask() {
        return task;
    }
}
