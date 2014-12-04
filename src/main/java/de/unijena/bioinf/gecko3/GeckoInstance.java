package de.unijena.bioinf.gecko3;

import de.unijena.bioinf.gecko3.algo.ReferenceClusterAlgorithm;
import de.unijena.bioinf.gecko3.algo.status.AlgorithmProgressListener;
import de.unijena.bioinf.gecko3.algo.status.AlgorithmStatusEvent;
import de.unijena.bioinf.gecko3.datastructures.*;
import de.unijena.bioinf.gecko3.event.DataEvent;
import de.unijena.bioinf.gecko3.event.DataListener;
import de.unijena.bioinf.gecko3.gui.GenomePainting;
import de.unijena.bioinf.gecko3.gui.Gui;
import de.unijena.bioinf.gecko3.gui.StartComputationDialog;
import de.unijena.bioinf.gecko3.io.ResultWriter;
import de.unijena.bioinf.gecko3.io.ResultWriter.ExportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class GeckoInstance {
    private static final Logger logger = LoggerFactory.getLogger(GeckoInstance.class);

	private static GeckoInstance instance;

    private DataSet data;

    private native GeneCluster[] computeClusters(int[][][] genomes, Parameter params, GeckoInstance gecko);

	public native GeneCluster[] computeReferenceStatistics(int[][][] genomes, Parameter params, GeneCluster[] cluster, GeckoInstance gecko);

    public enum ResultFilter {
        showFiltered,
        showAll,
        showSelected;

        public static final String types = "showFiltered, showAll";
    }

    private SwingWorker<List<GeneCluster>, Void> geneClusterSwingWorker = null;

	private boolean libgeckoLoaded;
	
	private File currentWorkingDirectoryOrFile;

    private List<GeneCluster> clusterSelection;
	private List<GeneCluster> reducedList;
	
	private boolean debug = false;
	private boolean animationEnabled = true;
	private Parameter lastParameter;
    private Gui gui;

    private StartComputationDialog scd = null;

    public static final int DEFAULT_MAX_GENE_NAME_LENGTH = 6;

    private int maxGeneNameLength = DEFAULT_MAX_GENE_NAME_LENGTH;

    private final EventListenerList eventListener = new EventListenerList();
	
    /**
     * Setter for the upper bound of the maximum gene name length.
     * @param maxGeneNameLength the maximum gene name length.
     */
    public void setMaxGeneNameLength(int maxGeneNameLength) {
        this.maxGeneNameLength = maxGeneNameLength;
    }
	
	/**
	 * Getter for the variable maxIdLength
	 * 
	 * @return the length of the longest id in the read data
	 */
	public int getMaxLength(GenomePainting.NameType nameType)
	{
        int maxLength;
        switch (nameType) {
            case ID: maxLength = data.getMaxIdLength(); break;
            case NAME: maxLength =  data.getMaxNameLength(); break;
            case LOCUS_TAG:maxLength =  data.getMaxLocusTagLength(); break;
            default:
                return -1;
        }
        return Math.min(maxLength, maxGeneNameLength);
	}
	
	/*
	 * DataEvents
	 */
	
	public void addDataListener(DataListener l) {
		eventListener.add(DataListener.class, l);
	}
	
	public void removeDataListener(DataListener l) {
		eventListener.remove(DataListener.class, l);
	}
	
	public synchronized void fireDataChanged() {
		for (DataListener d : eventListener.getListeners(DataListener.class) ) {
			d.dataChanged(new DataEvent(this));
		}
	}
	/*
	 * END DataEvents
	 */

    public List<GeneCluster> computeReferenceStatistics(int[][][] genomes, Parameter params, List<GeneCluster> cluster, GeckoInstance gecko) {
        return new ArrayList<>(Arrays.asList(computeReferenceStatistics(genomes, params, cluster.toArray(new GeneCluster[cluster.size()]), gecko)));
    }
	
	public Parameter getLastParameter() {
		return lastParameter;
	}
	
	public void setAnimationEnabled(boolean animationEnabled) {
		this.animationEnabled = animationEnabled;
	}
	
	public boolean isAnimationEnabled() {
		return animationEnabled;
	}
	
	public boolean isLibgeckoLoaded() {
		return libgeckoLoaded;
	}

	public void setLibgeckoLoaded(boolean libgeckoLoaded) {
		this.libgeckoLoaded = libgeckoLoaded;
	}

	public synchronized StartComputationDialog getStartComputationDialog() {
		if (scd==null)
			scd = new StartComputationDialog();
		return scd;
	}
	
	/**
	 * Adds the cluster with the given index to the cluster selection
	 * 
	 * @param cluster the cluster
	 */
	public void addToClusterSelection(GeneCluster cluster) {
		if (clusterSelection == null)
			clusterSelection = new ArrayList<>();
        if (!clusterSelection.contains(cluster))  //TODO LinkedHashSet for performance?
		    clusterSelection.add(cluster);
	}
	
	/**
	 * Clears all selected clusters
	 */
	public void clearClusterSelection() {
		clusterSelection.clear();
	}
	
	/**
	 * Returns the list of gene clusters under the given filter condition. 
	 * @param filter the filter condition
	 * @return the list of gene clusters
	 */
	public List<GeneCluster> getClusterList(ResultFilter filter) {
        switch (filter) {
            case showAll:
                return data.getClusters();
            case showFiltered:
                return reducedList;
            case showSelected:
                return clusterSelection;
        }
        throw new RuntimeException("Invalid filter tpye: " + filter);
	}

    public Color getGeneColor(GeneFamily geneFamily) {
        return data.getGeneColor(geneFamily);
    }

    public Map<String, GeneFamily> getGeneLabelMap() {
        return data.getGeneLabelMap();
    }
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public boolean isDebugEnabled() {
		return debug;
	}
	
	public void setGui(Gui gui) {
		this.gui = gui;
	}
	
	public Gui getGui() {
		return gui;
	}

    /**
     * Swap the genome at position index with the first genome
     * @param index the position of the new first genome
     */
    public void reorderGenomes(int index) {
        this.data.reorderGenomes(index);
        dataUpdated();
    }
	
	public void addReferenceGenome(Genome referenceGenome) {
        this.data.addReferenceGenome(referenceGenome);
        dataUpdated();
	}
	
	public void setClusters(List<GeneCluster> clusters, Parameter parameter) {
		this.data.setClusters(clusters, parameter);
        handleUpdatedClusterResults();
	}

    private void updateReducedDataAndSelection() {
        GeckoInstance.this.reducedList = GeneCluster.generateReducedClusterList(GeckoInstance.this.getClusters());
        GeckoInstance.this.clusterSelection = null;
    }

    private void handleUpdatedClusterResults() {
        updateReducedDataAndSelection();
        GeckoInstance.this.fireDataChanged();
        if (GeckoInstance.this.gui != null) {
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    GeckoInstance.this.gui.changeMode(Gui.Mode.SESSION_IDLE);
                }
            });
        }
    }

    private void dataUpdated() {
        if (this.gui != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    gui.updateViewscreen();
                    if (data.getGenomes() != null)
                        scd = new StartComputationDialog();
                    else
                        scd = null;
                    fireDataChanged();
                }
            });
        } else
            scd = null;
    }

    public DataSet getData() {
        return data;
    }

    public void setGeckoInstanceData(){
        setGeckoInstanceData(this.data);
    }

    public void setGeckoInstanceData(final DataSet data) {
        if (gui != null)
            gui.clearSelection();
        this.data = data;
        this.updateReducedDataAndSelection();
        if (gui != null) {
            Runnable updateGui = new Runnable() {
                @Override
                public void run() {
                    gui.updateViewscreen();
                    if (data.equals(DataSet.getEmptyDataSet()))
                        gui.changeMode(Gui.Mode.NO_SESSION);
                    else
                        gui.changeMode(Gui.Mode.SESSION_IDLE);
                }
            };

            if (SwingUtilities.isEventDispatchThread())
                updateGui.run();
            else
                SwingUtilities.invokeLater(updateGui);

            dataUpdated();
        }
    }
	
	public List<GeneCluster> getClusters() {
		return data.getClusters();
	}
	
	public File getCurrentWorkingDirectoryOrFile() {
		return currentWorkingDirectoryOrFile;
	}

	public void setCurrentWorkingDirectoryOrFile(File currentWorkingDirectoryOrFile) {
		this.currentWorkingDirectoryOrFile = currentWorkingDirectoryOrFile;
	}

	private GeckoInstance() {
		ToolTipManager.sharedInstance().setInitialDelay(0);
	}
	
	public static synchronized GeckoInstance getInstance() {
		if (instance==null) {
			instance = new GeckoInstance();
		}
		return instance;
	}

    private static final boolean USE_MEMORY_REDUCTION_DEFAULT = true;

	/**
	 * Computes the gene clusters for the given genomes with the given parameters
	 * @param data the data
	 * @param params the parameters
	 * @return the gene clusters
	 */
	public static List<GeneCluster> computeClustersJava(DataSet data, Parameter params, AlgorithmProgressListener listener) {
		return computeClustersJava(data, params, null, USE_MEMORY_REDUCTION_DEFAULT, listener);
	}

    /**
     * Computes the gene clusters for the given genomes with the given parameters
     * @param data the data
     * @param params the parameters
     * @param useMemoryReduction
     * @return the gene clusters
     */
    public static List<GeneCluster> computeClustersJava(DataSet data, Parameter params, boolean useMemoryReduction, AlgorithmProgressListener listener) {
        return computeClustersJava(data, params, null, useMemoryReduction, listener);
    }

    /**
     * Computes the gene clusters for the given genomes with the given parameters
     * @param data the data
     * @param params the parameters
     * @param genomeGrouping
     * @return the gene clusters
     */
    public static List<GeneCluster> computeClustersJava(DataSet data, Parameter params, List<Set<Integer>> genomeGrouping, AlgorithmProgressListener listener) {
        return computeClustersJava(data, params, genomeGrouping, USE_MEMORY_REDUCTION_DEFAULT, listener);
    }

    /**
	 * Computes the gene clusters for the given genomes with the given parameters
	 * @param data the data
	 * @param params the parameters
	 * @param genomeGrouping the grouping of the genomes, only one genome per group is used for quorum and p-value
     * @param useMemoryReduction
     * @param listener
	 * @return the gene clusters
	 */
	public static List<GeneCluster> computeClustersJava(DataSet data, Parameter params, List<Set<Integer>> genomeGrouping, boolean useMemoryReduction, AlgorithmProgressListener listener) {
		return ReferenceClusterAlgorithm.computeReferenceClusters(data, params, useMemoryReduction, genomeGrouping, listener);
	}
	
	/**
	 * Computes the gene clusters for the given genomes with the given parameters
	 * @param params the parameters
	 * @return the gene clusters
	 */
	public List<GeneCluster> computeClustersLibgecko(DataSet data, Parameter params) {
        int intArray[][][] = data.toIntArray();
        params.setAlphabetSize(data.getCompleteAlphabetSize());
		return new ArrayList<>(Arrays.asList(computeClusters(intArray, params, GeckoInstance.this)));
	}

    public void stopComputation() {
        geneClusterSwingWorker.cancel(true);
    }

    class GeneClusterDetectionTask extends SwingWorker<List<GeneCluster>, Void> implements AlgorithmProgressListener{
        private final Parameter p;
        private final boolean mergeResults;
        private final double groupingFactor;
        private final DataSet data;

        public GeneClusterDetectionTask(Parameter p, boolean mergeResults, double groupingFactor, DataSet data) {
            this.p = p;
            this.mergeResults = mergeResults;
            this.groupingFactor = groupingFactor;
            this.data = data;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         * <p/>
         * <p/>
         * Note that this method is executed only once.
         * <p/>
         * <p/>
         * Note: this method is executed in a background thread.
         *
         * @return the computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        protected List<GeneCluster> doInBackground() throws Exception {
            //data.printGenomeStatistics();
            //BreakPointDistance.breakPointDistance(GeckoInstance.this.genomes, false);
            //BreakPointDistance.groupGenomes(genomes, 0.1, 0.95, 0.1, false);
            //System.out.println("\n");
            //BreakPointDistance.breakPointDistance(GeckoInstance.this.genomes, true);

            List<Set<Integer>> genomeGroups = null;
            if (groupingFactor <= 1.0)
                genomeGroups = BreakPointDistance.groupGenomes(data, groupingFactor, false);

            Date before = new Date();
            if (!mergeResults){
                data.clearClusters();
            }
            List<GeneCluster> res = computeClustersJava(data, p, genomeGroups, this);
            Date after = new Date();
            setProgressStatus(100, AlgorithmStatusEvent.Task.Done);
            logger.info("Time required for computation: {}s", (after.getTime() - before.getTime()) / 1000F);
            return res;
        }

        @Override
        public void done() {
            try {
                List<GeneCluster> results = get();
                if (mergeResults)
                    GeckoInstance.this.mergeClusters(results, p);
                else
                    GeckoInstance.this.setClusters(results, p);
            } catch (CancellationException e){
                if (gui != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            gui.disableProgressBar();
                            GeckoInstance.this.gui.changeMode(Gui.Mode.SESSION_IDLE);
                        }
                    });
                }
            } catch (InterruptedException | ExecutionException e) {
                JOptionPane.showMessageDialog(gui.getMainframe(), e.getMessage(), "Error computing gene clusters", JOptionPane.ERROR_MESSAGE);
                logger.error("Error in cluster computation", e);
            }
        }

        @Override
        public void algorithmProgressUpdate(AlgorithmStatusEvent statusEvent) {
            setProgressStatus(statusEvent.getProgress(), statusEvent.getTask());
        }
    }

    public void mergeClusters(List<GeneCluster> results, Parameter p) {
        data.mergeClusters(results, p);
        handleUpdatedClusterResults();
    }

    /**
     * Creates and executes the swing worker that performs the cluster detection.
     * Returns the SwingWorker, so the calling method can wait (by calling get() ) until it is done.
     * @param p
     * @param mergeResults
     * @param genomeGroupingFactor
     * @return
     */
	public SwingWorker<List<GeneCluster>, Void> performClusterDetection(Parameter p, boolean mergeResults, double genomeGroupingFactor) {
		lastParameter = p;
        if (gui != null)
		    gui.changeMode(Gui.Mode.PREPARING_COMPUTATION);

        //p = new Parameter(Parameter.DeltaTable.the_methode_high_dist.getDeltaTable(), Parameter.DeltaTable.the_methode_high_dist.getMinimumSize(), p.getQ(), p.getOperationMode(), p.getRefType());

        geneClusterSwingWorker = new GeneClusterDetectionTask(p, mergeResults, genomeGroupingFactor, GeckoInstance.this.getData());
        geneClusterSwingWorker.execute();
        return geneClusterSwingWorker;
	}
	
	public List<GeneCluster> computeReferenceStatistics(List<GeneCluster> clusters){
		if (!this.isLibgeckoLoaded()){
			System.err.println("Running in visualization only mode! Cannot compute statistics!");
			return clusters;
		}
		int genomes[][][] = data.toIntArray();
		int maxPWDelta = 0;
		int minClusterSize = Integer.MAX_VALUE;
		int minQuorum = Integer.MAX_VALUE;
		for (GeneCluster cluster : clusters){
			maxPWDelta = Math.max(maxPWDelta, cluster.getMaxPWDist());
			minClusterSize = Math.min(minClusterSize, cluster.getMinRefSeqLength());
			minQuorum = Math.min(minQuorum, cluster.getSize());
		}
		System.out.println(String.format("D:%d, S:%d, Q:%d, for %d clusters.", maxPWDelta, minClusterSize, minQuorum, clusters.size()));
		Parameter p = new Parameter(maxPWDelta, minClusterSize, minQuorum, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(data.getCompleteAlphabetSize());
		return this.computeReferenceStatistics(genomes, p, clusters, this);
	}
	
	public void displayMessage(String message) {
		final String m = message;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(gui.getMainframe(), m);
			}
		});
	}
	
	public void setProgressStatus(int value, final AlgorithmStatusEvent.Task task) {
		if (gui != null)
            gui.setProgressStatus(value, task);
	}
	
	public Genome[] getGenomes() {
		return data.getGenomes();
	}
	
	public boolean exportResultsToFile(File f, ResultFilter filter, ExportType type) {
        setCurrentWorkingDirectoryOrFile(f);
        return ResultWriter.exportResultsToFile(f, type, filter);
	}
}
