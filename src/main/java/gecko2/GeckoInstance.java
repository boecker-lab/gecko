package gecko2;

import gecko2.algo.ReferenceClusterAlgorithm;
import gecko2.algorithm.*;
import gecko2.event.DataEvent;
import gecko2.event.DataListener;
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
	private Map<Integer, String[]> geneLabelMap = new HashMap<Integer, String[]>();
	private Map<Integer, Color> colormap;
	
	private GeneCluster[] clusters;
	private SortedSet<Integer> clusterSelection;
	private ResultFilter filterSelection = ResultFilter.showAll;
	private SortedSet<Integer> reducedList;
	private String filterString;
	
	private boolean debug = false;
	private boolean animationEnabled = true;
	private File lastSavedFile = null, lastOpenedFile = null, lastExportedFile = null;
	private Parameter lastParameter;
	
	private int geneElementHight;
	private int geneElementWidth;
	
	private int maxIdLength;

    private final EventListenerList eventListener = new EventListenerList();
	
	/**
	 * Setter for the variable maxIdLength which is the length of the
	 * longest appearing id.
	 * 
	 * @param length longest id
	 */
	public void setMaxIdLength(int length)
	{
		this.maxIdLength = length;
	}
	
	/**
	 * Getter for the variable maxIdLength
	 * 
	 * @return the length of the longest id in the read data
	 */
	public int getMaxIdLength()
	{
		return this.maxIdLength;
	}
	
	/**
	 * Setter for the colormap
	 * 
	 * @param colormap the colormap
	 */
	public void setColorMap(Map<Integer, Color> colormap)
	{
		this.colormap = colormap;
	}
	
	/**
	 * Setter for the geneLabelMap
	 * 
	 * @param geneLabelMap the gene label map
	 */
	public void setGeneLabelMap(Map<Integer, String[]> geneLabelMap)
	{
		this.geneLabelMap = geneLabelMap;
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
	
	
	private Gui gui;
	
	private StartComputationDialog scd = null;
	
	private final static int MAX_GENEELEMENT_HIGHT = 40;
	private final static int MIN_GENEELEMENT_HIGHT = 9;
	public final static int DISPLAY_GENEELEMENT_HIGHT = 20;
	
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
					genes[i] = (Integer) geneLabelMap.keySet().toArray()[genes[i]];
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
		ArrayList<GeneCluster> result = new ArrayList<GeneCluster>(clusters.length);
		
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
		return geneElementWidth;
	}
	
	public void setGeneElementHight(int geneElementHight) {
		this.geneElementHight = geneElementHight;
		this.geneElementWidth = (int) Math.round(geneElementHight*1.75);
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
		if (genomes!=null && GeckoInstance.this.gui != null)
			scd = new StartComputationDialog(genomes.length);
		else
			scd = null;
		if (GeckoInstance.this.gui != null)
			GeckoInstance.this.gui.updateViewscreen();
		this.fireDataChanged();
	}
	
	public void setClusters(GeneCluster[] clusters) {
		this.clusters = clusters;
        this.reducedList = null;
		
		if (gui != null) {
			gui.getGcSelector().refresh();
		}
	}

    public void setGeckoInstanceFromReader(final GeckoDataReader reader) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GeckoInstance.getInstance().setClusters(reader.getGeneClusters());
                GeckoInstance.getInstance().setGeneLabelMap(reader.getGeneLabelMap());
                GeckoInstance.getInstance().setColorMap(reader.getColorMap());
                GeckoInstance.getInstance().setGenomes(reader.getGenomes());
                GeckoInstance.getInstance().setMaxIdLength(reader.getMaxIdLength());
                if (gui != null){
                    gui.updateViewscreen();
                    gui.updategcSelector();
                    gui.changeMode(Gui.Mode.SESSION_IDLE);
                }
            }
        });

        GeckoInstance.getInstance().fireDataChanged();
    }
	
	public GeneCluster[] getClusters() {
		return clusters;
	}
	
	/**
	 * Creates a Map that assigns an array of Gene Object to each gene id. Each array element refers
	 * to one of the currently observed genomes.
	 * @param c The GeneCluster whose genes should be associated with their annotations
	 * @return A map with the gene id as the key and an array of Gene Objects as the value. The i-th
	 * array element refers to the i-th currently observed genome, e.g. array[3] assigned to id 5 is
	 * a reference to the Gene Object with id 5 in genome 3. If an array element is a null reference
	 * a gene with that id does not occur in the subsequence (that is refered by the GeneCluster)
	 * of that genome.
	 */
	public HashMap<Integer, Gene[]> generateAnnotations(GeneCluster c, GeneClusterOccurrence gOcc, int[] subselection) {
		int[] genes = c.getGenes();
		Subsequence[][] subsequences = gOcc.getSubsequences();
		HashMap<Integer, Gene[]> map = new HashMap<Integer, Gene[]>();
		for (int gene : genes) {
			if (geneLabelMap.get(gene) != null)
				map.put(gene, new Gene[subsequences.length]);
		}
		for (int seqnum=0; seqnum<subsequences.length; seqnum++) {
			if (subsequences[seqnum].length<=subselection[seqnum] || 
					subselection[seqnum]==GeneClusterOccurrence.GENOME_NOT_INCLUDED)
				continue;
			Subsequence subseq = subsequences[seqnum][subselection[seqnum]];
			Chromosome chromosome = genomes[seqnum].getChromosomes().get(subseq.getChromosome());
			for (int i=subseq.getStart()-1; i<subseq.getStop(); i++) {
				Gene gene = chromosome.getGenes().get(i);
				if (map.containsKey(Math.abs(gene.getId()))) {
					Gene[] g = map.get(Math.abs(gene.getId()));
					if (g[seqnum]==null) g[seqnum]=gene;
				}
			}
		}
		return map;
	}
	
	public HashMap<Integer, Gene[][]> generateAnnotations(GeneCluster c, GeneClusterOccurrence gOcc) {
		int[] genes = c.getGenes();
		Subsequence[][] subsequences = gOcc.getSubsequences();
		HashMap<Integer, Gene[][]> map = new HashMap<Integer, Gene[][]>();
		for (int gene : genes) {
			if (geneLabelMap.get(gene) != null) {
				Gene[][] geneArray = new Gene[subsequences.length][];
				for (int i=0; i<subsequences.length; i++)
					geneArray[i] = new Gene[subsequences[i].length];
				map.put(gene, geneArray);
			}
		}
		for (int seqnum=0; seqnum<subsequences.length; seqnum++) {
			for (int i=0; i<subsequences[seqnum].length; i++) {
				Subsequence subseq = subsequences[seqnum][i];
				Chromosome chromosome = genomes[seqnum].getChromosomes().get(subseq.getChromosome());
				for (int j=subseq.getStart()-1; j<subseq.getStop(); j++) {
					Gene gene = chromosome.getGenes().get(j);
					if (map.containsKey(Math.abs(gene.getId()))) {
						Gene[][] g = map.get(Math.abs(gene.getId()));
						if (g[seqnum][i]==null)
							g[seqnum][i] = gene;
					}
				}
			}
		}
		return map;
	}	

	public Map<Integer, Color> getColormap() {
		return colormap;
	}
	
	public Map<Integer, String[]> getGenLabelMap() {
		return geneLabelMap;
	}
	
	public File getCurrentInputFile() {
		return currentInputFile;
	}

	public void setCurrentInputFile(File currentInputFile) {
		this.currentInputFile = currentInputFile;
	}

	private GeckoInstance() {
		this.clusters = new GeneCluster[0];
		this.setGeneElementHight(20);
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
		p.setAlphabetSize(geneLabelMap.size());
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
				GeckoInstance.this.clusters = mergeResults(GeckoInstance.this.clusters, res);
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
		
		/**
		 * Print statistics of gene family sizes for all genomes
		 * @param genomes The genomes
		 * @param alphabetSize The size of the alphabet
		 */
		private void printGenomeStatistics(Genome[] genomes, int alphabetSize){
			printGenomeStatistics(genomes, alphabetSize, -1, -1);
		}
		
		/**
		 * Print statistics of gene family sizes for all genomes with genomeSize +/- genomeSizeDelta genes.
		 * @param genomes The genomes
		 * @param alphabetSize The size of the alphabet
		 * @param genomeSize  The number of genes that is needed for a genome to be reported. -1 will report statistics for all genomes.
		 * @param genomeSizeDelta The maximum deviation form the genomeSize for a genome to be reported
		 */
		private void printGenomeStatistics(Genome[] genomes, int alphabetSize, int genomeSize, int genomeSizeDelta) {
			int[][] alphabetPerGenome = new int[genomes.length][alphabetSize + 1];
			String[][] annotations = new String[genomes.length][alphabetSize + 1];
						
			SortedMap<Integer,Integer> summedFamilySizes = new TreeMap<Integer, Integer>();
			int nrReportedGenomes = 0;
			List<Integer> genomeSizes = new ArrayList<Integer>();
			// Generate family sizes per genome and print it
			for (int n=0; n<genomes.length; n++){
				Genome g = genomes[n];
				if (genomeSize != -1 && (g.getTotalGeneNumber() < genomeSize-genomeSizeDelta || g.getTotalGeneNumber() > genomeSize + genomeSizeDelta))
					continue;
				else {
					nrReportedGenomes++;
					genomeSizes.add(g.getTotalGeneNumber());
				}
				for (Chromosome chr : g.getChromosomes()) {
					for (Gene gene : chr.getGenes()) {
						alphabetPerGenome[n][Math.abs(gene.getId())]++;
						//if (n!=0)
							annotations[n][Math.abs(gene.getId())] = gene.getAnnotation();
						//else
							//annotations[n][Math.abs(gene.getId())] = String.format("%s: %s", chr.getName().substring(24), gene.getAnnotation());
					}
				}
				SortedMap<Integer,Integer> familySizes = new TreeMap<Integer, Integer>();
				for (int i=1; i<alphabetPerGenome[n].length; i++){
					// add family size for this genome
					Integer fS = familySizes.get(alphabetPerGenome[n][i]);
					if (fS != null)
						familySizes.put(alphabetPerGenome[n][i], ++fS);
					else
						familySizes.put(alphabetPerGenome[n][i], 1);
					
					// add summed family sizes				
					fS = summedFamilySizes.get(alphabetPerGenome[n][i]);
					if (fS != null)
						summedFamilySizes.put(alphabetPerGenome[n][i], ++fS);
					else
						summedFamilySizes.put(alphabetPerGenome[n][i], 1);
				}

				Integer nonOccFamilies = familySizes.get(0);
				System.out.println(String.format("%s: %d genes, %d gene families", g.getName(), g.getTotalGeneNumber(), (nonOccFamilies == null) ? alphabetPerGenome[n].length-1 : alphabetPerGenome[n].length - 1 - nonOccFamilies));
				for (Entry<Integer, Integer> entry : familySizes.entrySet()){
					System.out.println(String.format("%d\t%d", entry.getKey(), entry.getValue()));
				}
				System.out.println();
			}
			
			for (Integer size : genomeSizes)
				System.out.print(size + ", ");
			System.out.println("");
			System.out.println(String.format("Sum of %d genomes:", nrReportedGenomes));
			for (Entry<Integer, Integer> entry : summedFamilySizes.entrySet()){
				if (entry.getKey() != 0)
					System.out.println(String.format("%d\t%d", entry.getKey(), entry.getValue()));
			}
			System.out.println();
			
			// generate complete family sizes and print it
			int[] alphabet = new int[p.getAlphabetSize() + 1];
			for (Genome g : genomes){
				if (genomeSize != -1 && (g.getTotalGeneNumber() < genomeSize-genomeSizeDelta || g.getTotalGeneNumber() > genomeSize + genomeSizeDelta))
					continue;
				for (Chromosome chr : g.getChromosomes())
					for (Gene gene : chr.getGenes())
						alphabet[Math.abs(gene.getId())]++;
			}
			SortedMap<Integer,Integer> familySizes = new TreeMap<Integer, Integer>();
			for (int i=1; i<alphabet.length; i++){
				Integer fS = familySizes.get(alphabet[i]);
				if (fS != null)
					familySizes.put(alphabet[i], ++fS);
				else
					familySizes.put(alphabet[i], 1);				
			}

			System.out.println("Complete:");
			for (Entry<Integer, Integer> entry : familySizes.entrySet()){
				System.out.println(String.format("%d\t%d", entry.getKey(), entry.getValue()));
			}
			
			/*
			for(int j=0; j<alphabetPerGenome.length; j++) {
				System.out.print(String.format("%s\t", genomes[j].getName()));
			}
			System.out.println("");
			for (int i=0; i<alphabet.length; i++) {
				for(int j=0; j<alphabetPerGenome.length; j++) {
					System.out.print(String.format("%d\t", alphabetPerGenome[j][i]));
				}
				System.out.println("");
			}
			for(int j=0; j<alphabetPerGenome.length; j++) {
				System.out.print(String.format("%s\t", genomes[j].getName()));
			}
			System.out.println("");
			*/
			/*
			for (int i=0; i<alphabet.length; i++) {
				if (alphabetPerGenome[0][i] == 1) {
					boolean print = false;
					StringBuilder builder = new StringBuilder();
					builder.append("\"").append(annotations[0][i]).append("\"");
					for(int j=1; j<alphabetPerGenome.length; j++) {
						if (alphabetPerGenome[j][i] == 1) {
							builder.append("\t").append("\"").append(annotations[j][i]).append("\"");
							print = true;
						} else 
							builder.append("\t").append("-");
					}
					if (print)
						System.out.println(builder.toString());
				}
			}*/
		}
		
		private GeneCluster[] mergeResults(GeneCluster[] oldResults, GeneCluster[] additionalResults) {
			GeneCluster[] newResults;
			if (oldResults == null)
				newResults = additionalResults;
			else if(additionalResults == null)
				newResults = oldResults;
			else {
				newResults = Arrays.copyOf(oldResults, oldResults.length + additionalResults.length);
				int newId = oldResults.length;
				for (GeneCluster cluster : additionalResults) {
					cluster.setId(newId);
					newResults[newId] =  cluster;
					newId++;
				}	
			}
			return newResults;
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
		p.setAlphabetSize(geneLabelMap.size());
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
