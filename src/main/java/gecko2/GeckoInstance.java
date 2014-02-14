package gecko2;

import gecko2.algo.ReferenceClusterAlgorithm;
import gecko2.algorithm.*;
import gecko2.event.DataEvent;
import gecko2.event.DataListener;
import gecko2.gui.GenomePainting;
import gecko2.gui.Gui;
import gecko2.gui.StartComputationDialog;
import gecko2.io.GeckoDataReader;
import gecko2.io.ResultWriter;
import gecko2.io.ResultWriter.ExportType;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class GeckoInstance {
	private static GeckoInstance instance;

    private native GeneCluster[] computeClusters(int[][][] genomes, Parameter params, GeckoInstance gecko);
	public native GeneCluster[] computeReferenceStatistics(int[][][] genomes, Parameter params, GeneCluster[] cluster, GeckoInstance gecko);
	
	public enum ResultFilter {showAll, showFiltered, showSelected}
	
	private boolean libgeckoLoaded;
	
	private File currentInputFile; 
	private Genome[] genomes = null;
	
	private GeneCluster[] clusters;
	private SortedSet<Integer> clusterSelection;
	private ResultFilter filterSelection = ResultFilter.showAll;
	private SortedSet<Integer> reducedList;
	private String filterString;
	
	private boolean debug = false;
	private boolean animationEnabled = true;
	private File lastSavedFile = null, lastOpenedFile = null, lastExportedFile = null;
	private Parameter lastParameter;
    private Gui gui;

    private StartComputationDialog scd = null;
	
	private int geneElementHight;
    public static final int DEFAULT_GENE_HIGHT = 20;
    private final static int MAX_GENEELEMENT_HIGHT = 40;
    private final static int MIN_GENEELEMENT_HIGHT = 9;
	
	private int maxIdLength;
    private int maxNameLength;
    private int maxLocusTagLength;

    private final EventListenerList eventListener = new EventListenerList();
	
	/**
	 * Setter for the variable maxIdLength which is the length of the
	 * longest appearing id.
	 * 
	 * @param idLength longest id length
	 */
	public void setMaxLengths(int idLength, int nameLength, int locusTagLength)
	{
		this.maxIdLength = idLength;
        this.maxNameLength = nameLength;
        this.maxLocusTagLength = locusTagLength;
	}
	
	/**
	 * Getter for the variable maxIdLength
	 * 
	 * @return the length of the longest id in the read data
	 */
	public int getMaxLength(GenomePainting.NameType nameType)
	{
        switch (nameType) {
            case ID: return maxIdLength;
            case NAME: return maxNameLength;
            case LOCUS_TAG:return maxLocusTagLength;
            default:
                return -1;
        }
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
	
	public File getLastExportedFile() {
		return lastExportedFile;
	}
	
	public Parameter getLastParameter() {
		return lastParameter;
	}

	public File getLastOpenedFile() {
		return lastOpenedFile;
	}
	
	public File getLastSavedFile() {
		return lastSavedFile;
	}
	
	public void setLastOpenedFile(File lastOpenedFile) {
		this.lastOpenedFile = lastOpenedFile;
	}
	
	public void setLastSavedFile(File lastSavedFile) {
		this.lastSavedFile = lastSavedFile;
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
			scd = new StartComputationDialog(genomes.length);
		return scd;
	}
	
	/**
	 * Enable or disable result filtering by the selection
	 * 
	 * @param filter true to filter by the selection, false to disable filtering
	 */
	public void filterBy(ResultFilter filter) {
		filterSelection = filter;
		filterResults();
	}
	
	/**
	 * Adds the cluster with the given index to the cluster selection
	 * 
	 * @param clusterIndex the index of the cluster
	 */
	public void addToClusterSelection(int clusterIndex) {
		if (clusterSelection == null)
			clusterSelection = new TreeSet<Integer>();
		clusterSelection.add(clusterIndex);
		filterResults();
	}
	
	/**
	 * Clears all selected clusters
	 */
	public void clearClusterSelection() {
		clusterSelection.clear();
		filterResults();
	}

	/**
	 * Adds the cluster with the given index to the list of hidden clusters
	 *
	 * @param clusterIndex the index of the cluster
	 */
	public void addToReducedList(int clusterIndex) {
		if (reducedList == null)
			reducedList = new TreeSet<Integer>();
		reducedList.add(clusterIndex);
		filterResults();
	}

	/**
	 * Adds the cluster with the given index to the list of hidden clusters
	 *
	 * @param toHide the index of the cluster to hide
	 */
	public void addToReducedList(SortedSet<Integer> toHide) {
		if (reducedList == null)
			reducedList = new TreeSet<Integer>();
		reducedList.addAll(toHide);
		filterResults();
	}
	
	/**
	 * Sets the string used for filtering the cluster results and updates the results
	 * 
	 * @param filterString the new filter string, must not be null
	 */
	public void setFilterString(String filterString) {
		if (filterString == null)
			throw new NullPointerException("filterString must not be null");
		this.filterString = filterString;
		filterResults();
	}
	
	/**
	 * Filter the results with the last filter string
	 */
	private void filterResults() {
		if (clusters==null) return;
		if (filterString == null)
			filterString = "";
		if (clusterSelection == null)
			clusterSelection = new TreeSet<Integer>();
		if (reducedList == null)
			reducedList = GeneCluster.generateReducedClusterList(clusters);
		if (filterString.equals("")) {
			for (GeneCluster c : clusters) {
				c.setMatch(false);
				c.setMatch(applyFilter(c.getId()));
			}
		}
		else {
			String[] searchPatterns = filterString.split(" ");
			for (int i=0; i<searchPatterns.length; i++) 
				searchPatterns[i] = Pattern.quote(searchPatterns[i].toLowerCase());

			for (GeneCluster c : clusters) {
				c.setMatch(false);
				
				// Check if there is a match in the gene label
				int[] genes = c.getGenes().clone();
				for (int i=0; i<genes.length; i++)
                    genes[i] = (Integer) Gene.getIntegerAlphabet().toArray()[genes[i]];
				String geneString = Arrays.toString(genes);
				for (String pattern : searchPatterns) {
					if (geneString.matches(".*[\\[ ,]"+pattern+"[ ,\\]].*")) {
						c.setMatch(applyFilter(c.getId()));
						break;
					}
				}
				
				// Check if there is a match in the genes annotations
				if (!c.isMatch()) {
					
					// ... no comment ...
					for (GeneClusterOccurrence gOcc : c.getAllOccurrences()) {
						for (int genome=0; genome<gOcc.getSubsequences().length; genome++) {
							Subsequence[] subseqs = gOcc.getSubsequences()[genome];
							for (Subsequence s : subseqs) {
								for (Gene g : genomes[genome].getSubsequence(s)) {
									for (String pattern: searchPatterns)
										if (g.getSummary().toLowerCase().matches(".*"+pattern+".*")) {
											c.setMatch(applyFilter(c.getId()));
											break;
										}
									if (c.isMatch()) break;
								}
								if (c.isMatch()) break;
							}
							if (c.isMatch()) break;
						}				
						if (c.isMatch()) break;
					}
				}
			}
		}
		gui.getGcSelector().refresh();	
	}
	
	/**
	 * Tests, if the cluster with the given index is in given filter set
	 * @param clusterIndex the index of the cluster
	 * @param filter the used filtering
	 * @return true if the cluster is displayed in the given filter set
	 */
	private boolean applyFilter(int clusterIndex, ResultFilter filter) {
		switch (filter) {
		case showSelected:
			if (clusterSelection.contains(clusterIndex))
				return true;
			break;
		case showFiltered:
			if (reducedList.contains(clusterIndex))
				return true;
			break;
		default:
			return true;
		}
		return false;
	}
	
	/**
	 * Tests, if the cluster with the given index is in the current filter set
	 * @param clusterIndex the index of the cluster
	 * @return true if the cluster is displayed in the current filter set
	 */
	private boolean applyFilter(int clusterIndex) {
		return applyFilter(clusterIndex, filterSelection);
	}
	
	/**
	 * Returns the list of gene clusters under the given filter condition. 
	 * @param filter the filter condition
	 * @return the list of gene clusters
	 */
	private List<GeneCluster> getClusterList(ResultFilter filter) {
		ArrayList<GeneCluster> result = new ArrayList<>(clusters.length);
		
		for (int i=0; i<clusters.length; i++) {
			if (applyFilter(i, filter))
				result.add(clusters[i]);
		}
		
		result.trimToSize();
		return result;		
	}
	
	
	public int getGeneElementHight() {
		return geneElementHight;
	}
	
	public int getGeneElementWidth() {
        return (int) Math.round(geneElementHight*1.75);
	}
	
	public void setGeneElementHight(int geneElementHight) {
		this.geneElementHight = geneElementHight;
	}
	
	public boolean canZoomIn() {
		return (geneElementHight<=MAX_GENEELEMENT_HIGHT);
	}
	
	public boolean canZoomOut() {
		return (geneElementHight>=MIN_GENEELEMENT_HIGHT);
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
	
	public void setGenomes(Genome[] genomes) {
		this.genomes = genomes;
        if (this.gui != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (GeckoInstance.this.genomes != null)
                        scd = new StartComputationDialog(GeckoInstance.this.genomes.length);
                    else
                        scd = null;
                    gui.updateViewscreen();
                }
            });
        } else
            scd = null;
		this.fireDataChanged();
	}
	
	public void setClusters(GeneCluster[] clusters) {
		this.clusters = clusters;
        this.reducedList = null;
		
		if (gui != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    gui.getGcSelector().refresh();
                }
            });
        }
	}

    public void setGeckoInstanceFromReader(final GeckoDataReader reader) {
        setMaxLengths(reader.getMaxIdLength(), reader.getMaxNameLength(), reader.getMaxLocusTagLength());
        setClusters(reader.getGeneClusters());
        Gene.setGeneLabelMap(reader.getGeneLabelMap());
        setGenomes(reader.getGenomes());
        if (gui != null){
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    gui.updateViewscreen();
                    gui.updategcSelector();
                    gui.changeMode(Gui.Mode.SESSION_IDLE);
                }
            });
        }

        GeckoInstance.getInstance().fireDataChanged();
    }
	
	public GeneCluster[] getClusters() {
		return clusters;
	}
	
	public File getCurrentInputFile() {
		return currentInputFile;
	}

	public void setCurrentInputFile(File currentInputFile) {
		this.currentInputFile = currentInputFile;
	}

	private GeckoInstance() {
		this.clusters = new GeneCluster[0];
		this.setGeneElementHight(DEFAULT_GENE_HIGHT);
		ToolTipManager.sharedInstance().setInitialDelay(0);
	}
	
	public static synchronized GeckoInstance getInstance() {
		if (instance==null) {
			instance = new GeckoInstance();
		}
		return instance;
	}
	
	/**
	 * Computes the gene clusters for the given genomes with the given paramters
	 * @param genomes the genomes
	 * @param params the paramters
	 * @return the gene clusters
	 */
	public GeneCluster[] computeClustersJava(int[][][] genomes, Parameter params) {
		return computeClustersJava(genomes, params, null);
	}
	
	/**
	 * Computes the gene clusters for the given genomes with the given paramters
	 * @param genomes the genomes
	 * @param params the paramters
	 * @param genomeGrouping the grouping of the genomes, only one genome per group is used for quorum and p-value
	 * @return the gene clusters
	 */
	public GeneCluster[] computeClustersJava(int[][][] genomes, Parameter params, List<Set<Integer>> genomeGrouping) {
		return ReferenceClusterAlgorithm.computeReferenceClusters(genomes, params, genomeGrouping);
	}
	
	/**
	 * Computes the gene clusters for the given genomes with the given paramters
	 * @param genomes the genomes
	 * @param params the paramters
	 * @return the gene clusters
	 */
	public GeneCluster[] computeClustersLibgecko(int[][][] genomes, Parameter params) {
		return computeClusters(genomes, params, GeckoInstance.this);
	}
	
	/**
	 * Computes the gene clusters for the given genomes with the given paramters
	 * @param genomes the genomes
	 * @param params the paramters
	 * @return the gene clusters
	 */
	public GeneCluster[] computeClusters(int[][][] genomes, Parameter params) {
		return computeClustersJava(genomes, params);
		//return computeClustersLibgecko(genomes, params);
	}
	
	public ExecutorService performClusterDetection(Parameter p, boolean mergeResults, double genomeGroupingFactor) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
		lastParameter = p;
		p.setAlphabetSize(Gene.getAlphabetSize());
        if (gui != null)
		    gui.changeMode(Gui.Mode.PREPARING_COMPUTATION);
		ClusterComputationRunnable clusterComputation = new ClusterComputationRunnable(p, mergeResults, genomeGroupingFactor);

        executor.submit(clusterComputation);
        return executor;
	}
	
	void handleUpdatedClusterResults() {
		if (this.clusters==null) {
			this.clusters = new GeneCluster[0];
		}
		this.reducedList = GeneCluster.generateReducedClusterList(this.clusters);
		this.clusterSelection = null;
		this.filterString = null;
		gui.getGcSelector().refresh();
		gui.changeMode(Gui.Mode.SESSION_IDLE);
	}
	
	private class ClusterComputationRunnable implements Runnable {

		private final Parameter p;
		private final boolean mergeResults;
        private final double groupingFactor;
		
		public ClusterComputationRunnable(Parameter p){
			this(p, false, -1.0);
		}
		
		public ClusterComputationRunnable(Parameter p, boolean mergeResults, double groupingFactor) {
			this.p = p;
			this.mergeResults = mergeResults;
            this.groupingFactor = groupingFactor;
		}
		
		public void run() {
			//printGenomeStatistics(GeckoInstance.this.genomes, p.getAlphabetSize(), 1500, 250);
			//BreakPointDistance.breakPointDistance(GeckoInstance.this.genomes, false);
			//BreakPointDistance.groupGenomes(genomes, 0.1, 0.95, 0.1, false);
			//System.out.println("\n");
			//BreakPointDistance.breakPointDistance(GeckoInstance.this.genomes, true);
			
			// We do this very ugly with a 3D integer array to make things easier
			// during the JNI<->JAVA phase
            List<Set<Integer>> genomeGroups = null;
            if (groupingFactor <= 1.0)
                genomeGroups = BreakPointDistance.groupGenomes(genomes, groupingFactor, false);

            int genomes[][][] = Genome.toIntArray(GeckoInstance.this.genomes);
			
			Date before = new Date();
			GeneCluster[] res = computeClustersJava(genomes, p, genomeGroups);
			Date after = new Date();
			System.err.println("Time required for computation: "+(after.getTime()-before.getTime())/1000F+"s");
			if (mergeResults)				
				GeckoInstance.this.clusters = GeneCluster.mergeResults(GeckoInstance.this.clusters, res);
			else
				GeckoInstance.this.clusters = res;
            if (gui != null) {
			    EventQueue.invokeLater(new Runnable() {
				    public void run() {
					    GeckoInstance.this.handleUpdatedClusterResults();
				    }
			    });
            }
		}
	}
	
	public GeneCluster[] computeReferenceStatistics(GeneCluster[] clusters){
		if (!this.isLibgeckoLoaded()){
			System.err.println("Running in visualization only mode! Cannot compute statistics!");
			return clusters;
		}
		int genomes[][][] = new int[GeckoInstance.this.genomes.length][][];
		for (int i=0;i<genomes.length;i++) {
			genomes[i] = new int[GeckoInstance.this.genomes[i].getChromosomes().size()][];
			for (int j=0;j<genomes[i].length;j++)
				genomes[i][j] = GeckoInstance.this.genomes[i].getChromosomes().get(j).toIntArray(true, true);
		}
		int maxPWDelta = 0;
		int minClusterSize = Integer.MAX_VALUE;
		int minQuorum = Integer.MAX_VALUE;
		for (GeneCluster cluster : clusters){
			maxPWDelta = Math.max(maxPWDelta, cluster.getMaxPWDist());
			minClusterSize = Math.min(minClusterSize, cluster.getMinRefSeqLength());
			minQuorum = Math.min(minQuorum, cluster.getSize());
		}
		System.out.println(String.format("D:%d, S:%d, Q:%d, for %d clusters.", maxPWDelta, minClusterSize, minQuorum, clusters.length));
		Parameter p = new Parameter(maxPWDelta, minClusterSize, minQuorum, Parameter.QUORUM_NO_COST, Parameter.OperationMode.reference, Parameter.ReferenceType.allAgainstAll);
		p.setAlphabetSize(Gene.getAlphabetSize());
		return this.computeReferenceStatistics(genomes, p, clusters, this);
	}
	
	public void displayMessage(String message) {
		final String m = message;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(gui.getMainframe(), m);
			}
		});
	}
	
	public void initProgressBar(int maxvalue) {
		final int maxv = maxvalue;
		if (gui != null)
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					gui.getProgressbar().setMaximum(maxv);
				}
			});

	}
	
	public void setProgressStatus(int value) {
		final int v = value;
		if (gui != null)
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (v<gui.getProgressbar().getMaximum()) {
						gui.changeMode(Gui.Mode.COMPUTING);
						gui.getProgressbar().setValue(v);
					} else if (v<gui.getProgressbar().getMaximum()*2) {
                        gui.changeMode(Gui.Mode.DOING_STATISTICS);
                        gui.getProgressbar().setValue(v-gui.getProgressbar().getMaximum());
                    } else {
						gui.changeMode(Gui.Mode.FINISHING_COMPUTATION);
					}
				}
			});
	}
	
	public Genome[] getGenomes() {
		return genomes;
	}
	
	public void setLastOpendFile(File lastOpenedFile) {
		this.lastOpenedFile = lastOpenedFile;
	}
	
	public boolean exportResultsToFile(File f, ResultFilter filter, ExportType type) {
		lastExportedFile = f;
		filterResults();
		List<String> genomeNames = new ArrayList<String>(genomes.length);
		for (Genome genome : genomes)
			genomeNames.add(genome.getName());
		return ResultWriter.exportResultsToFileNEW2(f, getClusterList(filter), genomeNames, type);
	}
}
