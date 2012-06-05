package gecko2;

import gecko2.algorithm.Chromosome;
import gecko2.algorithm.Gene;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.Genome;
import gecko2.algorithm.Parameter;
import gecko2.algorithm.Subsequence;
import gecko2.event.DataEvent;
import gecko2.event.DataListener;
import gecko2.gui.Gui;
import gecko2.gui.StartComputationDialog;
import gecko2.io.ResultWriter;
import gecko2.util.PrintUtils;
import gecko2.util.SortUtils;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.event.EventListenerList;

public class GeckoInstance {
	public enum ResultFilter {showAll, showFiltered, showSelected};
	
	private static GeckoInstance instance;
	
	private boolean libgeckoLoaded;
	
	private File currentInputFile; 
	private Genome[] genomes= null;
	private int[] geneLabelMap;
	private HashMap<Integer, Color> colormap;
	
	private GeneCluster[] clusters;
	private SortedSet<Integer> clusterSelection;
	private ResultFilter filterSelection = ResultFilter.showAll;
	private SortedSet<Integer> reducedList;
	private boolean filterReducedList;
	private int highlightedCluster;
	private String filterString;
	
	private boolean debug = false;
	private boolean animationEnabled = true;
	private File lastSavedFile = null, lastOpenedFile = null, lastExportedFile = null;
	private Parameter lastParameter;
	
	private int geneElementHight;
	private int geneElementWidth;
	
	/*
	 * DataEvents
	 */
	private EventListenerList eventListener = new EventListenerList();

	
	public void addDataListener(DataListener l) {
		eventListener.add(DataListener.class, l);
	}
	
	public void removeDataListener(DataListener l) {
		eventListener.remove(DataListener.class, l);
	}
	
	protected synchronized void fireDataChanged() {
		for (DataListener d : eventListener.getListeners(DataListener.class) ) {
			d.dataChanged(new DataEvent(this));
		}
	}
	/*
	 * END DataEvents
	 */
	
	
	private Gui gui;
	
	private StartComputationDialog scd = null;
	
	public final static int MAX_GENEELEMENT_HIGHT = 40;
	public final static int MIN_GENEELEMENT_HIGHT = 9;
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
	 * @param clusterIndex the index of the cluster
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
					genes[i] = geneLabelMap[genes[i]];
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
		if (genomes!=null)
			scd = new StartComputationDialog(genomes.length);
		else
			scd = null;
		GeckoInstance.this.gui.updateViewscreen();
		this.fireDataChanged();
	}
	
	public int getHighlightedCluster() {
		return highlightedCluster;
	}
	
	public void setClusters(GeneCluster[] clusters) {
		this.clusters = clusters;
		gui.getGcSelector().refresh();
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
			if (geneLabelMap[gene]!=0)
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

	public HashMap<Integer, Color> getColormap() {
		return colormap;
	}
	
	public int[] getGenLabelMap() {
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
	
	public void performClusterDetection(Parameter p) {
		this.lastParameter = p;
		p.setAlphabetSize(geneLabelMap.length-1);
		p.setCodingTable(geneLabelMap);
		gui.changeMode(Gui.Mode.PREPARING_COMPUTATION);
		new ComputationThread(p);
	}
	
	public void handleUpdatedClusterResults() {
		if (this.clusters==null) {
			this.clusters = new GeneCluster[0];
		}
		this.reducedList = GeneCluster.generateReducedClusterList(this.clusters);
		this.clusterSelection = null;
		this.filterString = null;
		gui.getGcSelector().refresh();
		gui.changeMode(Gui.Mode.SESSION_IDLE);
	}
	
	private class ComputationThread implements Runnable {

		private Parameter p;
		
		public ComputationThread(Parameter p) {
			this.p = p;
			new Thread(this).start();
		}
		
		public void run() {
			//printGenomeStatistics();
			// We do this very ugly with a 3D integer array to make things easier
			// during the JNI<->JAVA phase
			int genomes[][][] = new int[GeckoInstance.this.genomes.length][][];
			for (int i=0;i<genomes.length;i++) {
				genomes[i] = new int[GeckoInstance.this.genomes[i].getChromosomes().size()][];
				for (int j=0;j<genomes[i].length;j++)
					genomes[i][j] = GeckoInstance.this.genomes[i].getChromosomes().get(j).toIntArray(true, true);
			}
			Date before = new Date();
			GeneCluster[] res = computeClusters(genomes, p, GeckoInstance.this);
			Date after = new Date();
			System.err.println("Time required for computation: "+(after.getTime()-before.getTime())/1000F+"s");
			GeckoInstance.this.clusters = res;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					GeckoInstance.this.handleUpdatedClusterResults();
				}
			});
		}
		
		private void printGenomeStatistics() {
			for (Genome g : GeckoInstance.this.genomes){
				int totalLength = 0;
				int[] alphabet = new int[p.getAlphabetSize() + 1];
				for (Chromosome chr : g.getChromosomes()) {
					totalLength += chr.getGenes().size();
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

				Integer nonOccFamilies = familySizes.get(0);
				System.out.println(String.format("%s: %d genes, %d gene families", g.getChromosomes().get(0).getName(), totalLength, (nonOccFamilies == null) ? alphabet.length-1 : alphabet.length - 1 - nonOccFamilies));
				for (Entry<Integer, Integer> entry : familySizes.entrySet()){
					System.out.println(String.format("%d:\t%d", entry.getKey(), entry.getValue()));
				}
				System.out.println();
			}
			
			int[] alphabet = new int[p.getAlphabetSize() + 1];
			for (Genome g : GeckoInstance.this.genomes)
				for (Chromosome chr : g.getChromosomes())
					for (Gene gene : chr.getGenes())
						alphabet[Math.abs(gene.getId())]++;
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
				System.out.println(String.format("%d:\t%d", entry.getKey(), entry.getValue()));
			}
		}

	}
	
	private String getGenomeName(String s) {
		String name = s.split(", |,| chromosome| plasmid| megaplasmid")[0];
		return name;
	}
	
	public ArrayList<GenomeOccurence> importGenomes(File file) throws FileNotFoundException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			ArrayList<GenomeOccurence> genomeOccurennces = new ArrayList<GenomeOccurence>();
			GenomeOccurence add = new GenomeOccurence();
			Map<String, Integer> groups = new HashMap<String, Integer>();
			Map<Integer, Integer> groupSize = new HashMap<Integer, Integer>();
			int curline = 0;
			if ((line = reader.readLine()) != null) {
				add.setDesc(line);
				groups.put(getGenomeName(line),1);
				add.setGroup(1);
				groupSize.put(1, 1);
				add.setStart_line(curline);
			}
			boolean next = false;
			while ((line = reader.readLine()) != null) {
				curline++;
				if (next) {
					add.setEnd_line(curline-2);
					genomeOccurennces.add(add);
					add = new GenomeOccurence();
					add.setDesc(line);
					String genomeName = getGenomeName(line);
					if (!groups.containsKey(genomeName)) {
						groups.put(genomeName, groups.size());
						add.setGroup(groups.size()-1);
						groupSize.put(groups.size()-1,1);
					} else {
						int group = groups.get(genomeName);
						add.setGroup(group);
						groupSize.put(group, groupSize.get(group)+1);
					}
					add.setStart_line(curline);
					next = false;
				}
				if (line.equals("")) next = true;
			}
			if (next) add.setEnd_line(curline-1); else add.setEnd_line(curline);
			if (add.getDesc()!=null) genomeOccurennces.add(add);
			reader.close();
			this.currentInputFile = file;
			// Remove singleton groups
			for (GenomeOccurence occ : genomeOccurennces)
				if (groupSize.get(occ.getGroup())==1)
					occ.setGroup(0);
			// Return result
			return genomeOccurennces;
		} catch (IOException e) {
			if (e instanceof FileNotFoundException) throw (FileNotFoundException) e;
			e.printStackTrace();
		}
		return null;
	}
	
	private class GenomeReadingThread implements Runnable {

		private ArrayList<GenomeOccurence> occs;
		
		public GenomeReadingThread(ArrayList<GenomeOccurence> occs) {
			this.occs = occs;
			SortUtils.resortGenomeOccurencesByStart(this.occs);
			new Thread(this).start();
		}
		
		public void run() {
			try { 
				PrintUtils.printDebug("Reading these occurences:");
				for (GenomeOccurence occ : occs)
					PrintUtils.printDebug(occ.toString());
				HashMap<Integer, Genome> groupedGenomes = new HashMap<Integer, Genome>();
				ArrayList<Genome> ungroupedGenomes = new ArrayList<Genome>();
				//GeckoInstance.this.genomes = new Chromosome[occs.size()];
				String line;
				CountedReader reader = new CountedReader(new FileReader(GeckoInstance.this.currentInputFile));
				ArrayList<Integer> intidlist = new ArrayList<Integer>();
				HashMap<Integer, Integer> backmap = new HashMap<Integer, Integer>();
				Random r = new Random();
				GeckoInstance.this.colormap = new HashMap<Integer, Color>();
				for (int i=0; i<occs.size(); i++) {
					GenomeOccurence occ = occs.get(i);
					// TODO
					Genome g;
					if (occ.getGroup()==0) {
						// If the group id is zero than we have a single chromosome genome,
						// therefore we have to greate a new genome
						g = new Genome();
						ungroupedGenomes.add(g);
					} else {
						// If the group id is not zero we need to check if we already created
						// a genome for that group id and if not create a new one
						if (!groupedGenomes.containsKey(occ.getGroup())) {
							g = new Genome();
							groupedGenomes.put(occ.getGroup(), g);
						} else g=groupedGenomes.get(occ.getGroup());
					}
					Chromosome c = new Chromosome();
					g.addChromosome(c);
					c.setName(occ.getDesc());
					ArrayList<Gene> genes = new ArrayList<Gene>();
					// Forward file pointer to genomes first gene
					reader.jumpToLine(occ.getStart_line()+2);
					while (reader.getCurrentLineNumber()<=occ.getEnd_line() && (line=reader.readLine())!=null) {
						if (!line.equals("")) {
							String[] explode = line.split("\t");
							int id = Integer.parseInt(explode[0]);
							int sign; if (explode[1].equals("-")) sign=-1; else sign = 1;
							if (id!=0 && backmap.containsKey(id)) {
								genes.add(new Gene(explode[3], sign*backmap.get(id), explode[4], false));
							} else {
								intidlist.add(id);
								int intid = intidlist.size();
								if (id!=0) {
									colormap.put(intid, new Color(r.nextInt(240),r.nextInt(240),r.nextInt(240)));
									backmap.put(id,intid);
									genes.add(new Gene(explode[3], sign*intid, explode[4], false));
								} else
									genes.add(new Gene(explode[3], sign*intid, explode[4], true));
							}
						}
					}
					// Thank you for the not existing autoboxing on arrays...
					GeckoInstance.this.geneLabelMap = new int[intidlist.size()+1];
					for (int j=1;j<GeckoInstance.this.geneLabelMap.length; j++) {
						GeckoInstance.this.geneLabelMap[j] = intidlist.get(j-1);
					}
					// TODO handle the case where EOF is reached before endline
					c.setGenes(genes);
					GeckoInstance.this.genomes = new Genome[groupedGenomes.size()];
					{ 
						int j=0;
						for (Genome x : groupedGenomes.values()) {
							GeckoInstance.this.genomes[j] = x;
							j++;
						}
					}
				}
				GeckoInstance.this.genomes = new Genome[groupedGenomes.size()+ungroupedGenomes.size()];
				{
					int i=0;
					for (Genome x : ungroupedGenomes)
						GeckoInstance.this.genomes[i++] = x;
					for (Genome x : groupedGenomes.values())
						GeckoInstance.this.genomes[i++] = x;					
				}
					
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						GeckoInstance.this.gui.changeMode(Gui.Mode.SESSION_IDLE);
						GeckoInstance.this.gui.updateViewscreen();
						GeckoInstance.this.fireDataChanged();
					}
				});
			} catch (NumberFormatException e) {
				e.printStackTrace();
				handleParsingError(ERROR_FILEFORMAT);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				handleParsingError(ERROR_FILEIO);
			} catch (EOFException e) {
				e.printStackTrace();
				handleParsingError(ERROR_FILEIO);
			} catch (IOException e) {
				e.printStackTrace();
				handleParsingError(ERROR_FILEIO);
			} catch (LinePassedException e) {
				e.printStackTrace();
				handleParsingError(ERROR_FILEFORMAT);
			}
		}
		
	}
	
	public static final short ERROR_FILEFORMAT = 1;
	public static final short ERROR_FILEIO = 2;
	
	private void handleParsingError(final short errorType) {
		this.genomes = null;
		clusters = new GeneCluster[0];
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				gui.handleFileError(errorType);
			}
		});
	}
	
	public void readGenomes(ArrayList<GenomeOccurence> occs)  {
		GeckoInstance.this.gui.changeMode(Gui.Mode.READING_GENOMES);
		new GenomeReadingThread(occs);
	}	
	
	private native GeneCluster[] computeClusters(int[][][] genomes, Parameter params, GeckoInstance gecko);
	
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

	};
	
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
	
	/**
	 * Reads a gecko session from a file
	 * @param f The file to read from
	 */
	public void loadSessionFromFile(File f)  {
		lastOpenedFile = f;
		gui.changeMode(Gui.Mode.READING_GENOMES);
		new SessionLoadingThread(f);
	}
	
	class SessionLoadingThread implements Runnable {
		private File f;
		
		public SessionLoadingThread(File f) {
			this.f = f;
			new Thread(this).start();
		}
		
		@SuppressWarnings("unchecked")
		public void run() {
			FileInputStream fis = null;
			ObjectInputStream o = null;
			try {
				fis = new FileInputStream(f);
				o = new ObjectInputStream(fis);
				genomes = (Genome[]) o.readObject();
				geneLabelMap = (int[]) o.readObject();
				colormap = (HashMap<Integer, Color>) o.readObject();
				clusters = (GeneCluster[]) o.readObject();
				if (clusters!=null)
					for (GeneCluster c : clusters)
						c.setMatch(true);
				
				EventQueue.invokeLater(new Runnable() {	
					public void run() {
						reducedList = null;
						gui.updateViewscreen();
						gui.updategcSelector();
						GeckoInstance.this.fireDataChanged();
						gui.changeMode(Gui.Mode.SESSION_IDLE);

					}
				});
			} catch (IOException e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(gui.getMainframe(), 
								"An error occured while reading the file!",
								"Error", 
								JOptionPane.ERROR_MESSAGE);
					}
				});
				handleFailedSessionLoad();
			} catch (ClassNotFoundException e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(gui.getMainframe(),
								"The input file is not in the right format!",
								"Wrong format",
								JOptionPane.ERROR_MESSAGE);
					}
				});
				handleFailedSessionLoad();
			}
		}
	}
	
	private void handleFailedSessionLoad() {
		genomes = null;
		geneLabelMap = null;
		colormap = null;
		clusters = null;
		gui.changeMode(Gui.Mode.SESSION_IDLE);		
	}
		
	/**
	 * Saves the current gecko session to a given file
	 * @param f The file to write to
	 */
	public boolean saveSessionToFile(File f) {
		lastSavedFile = f;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			ObjectOutputStream o = new ObjectOutputStream( fos );
			o.writeObject(genomes);
			o.writeObject(geneLabelMap);
			o.writeObject(colormap);
			o.writeObject(clusters);
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (fos!=null) fos.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		  
	}
	
	public boolean exportResultsToFile(File f, ResultFilter filter) {
		lastExportedFile = f;
		filterResults();
		List<String> genomeNames = new ArrayList<String>(genomes.length);
		for (Genome genome : genomes)
			genomeNames.add(genome.getChromosomes().get(0).getName());
		return ResultWriter.exportResultsToFileNEW2(f, getClusterList(filter), geneLabelMap, genomeNames);
	}
}
