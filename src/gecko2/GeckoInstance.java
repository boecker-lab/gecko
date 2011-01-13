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
import java.util.Random;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.event.EventListenerList;

public class GeckoInstance {
	
	private File currentInputFile; 
	private Genome[] genomes= null;
	private int[] geneLabelMap;
	private HashMap<Integer, Color> colormap;
	private GeneCluster[] clusters;
	private int highlightedCluster;
	private boolean debug = true;
	private boolean animationEnabled = true;
	private File lastSavedFile = null, lastOpenedFile = null;
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
	
	public synchronized StartComputationDialog getStartComputationDialog() {
		if (scd==null)
			scd = new StartComputationDialog(genomes.length);
		return scd;
	}
	
	public void filterResults(String searchPattern) {
		if (clusters==null) return;
		String[] searchPatterns = searchPattern.split(" ");
		for (int i=0; i<searchPatterns.length; i++) 
			searchPatterns[i] = Pattern.quote(searchPatterns[i].toLowerCase());
		if (searchPatterns[0].equals("")) 
			for (GeneCluster c : clusters)
				c.setMatch(true);
		else {
			for (GeneCluster c : clusters) {
				c.setMatch(false);
				
				// Check if there is a match in the gene label
				int[] genes = c.getGenes().clone();
				for (int i=0; i<genes.length; i++)
					genes[i] = geneLabelMap[genes[i]];
				String geneString = Arrays.toString(genes);
				for (String pattern : searchPatterns) {
					if (geneString.matches(".*[\\[ ,]"+pattern+"[ ,\\]].*")) {
						c.setMatch(true);
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
											c.setMatch(true);
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
	
	private static GeckoInstance instance;
	
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
		// Handle global keyboard event
//		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
//
//			public boolean dispatchKeyEvent(KeyEvent e) {
//				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
//					if (e.getID() == KeyEvent.KEY_PRESSED) {
//						multipleScroll = true;
//					} else {
//						multipleScroll = false;
//					}
//				}
//					
//				return false;
//			}
//			
//		});
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
		gui.changeMode(Gui.MODE_PREPARING_COMPUTATION);
		new ComputationThread(p);
	}
	
	public void handleUpdatedClusterResults() {
		if (this.clusters==null) {
			this.clusters = new GeneCluster[0];
		}
		gui.getGcSelector().refresh();
		gui.changeMode(Gui.MODE_SESSION_IDLE);
	}
	
	private class ComputationThread implements Runnable {

		private Parameter p;
		
		public ComputationThread(Parameter p) {
			this.p = p;
			new Thread(this).start();
		}
		
		public void run() {
			// We do this very ugly with a 3D integer array to make things easier
			// during the 
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
			HashMap<String, Integer> groups = new HashMap<String, Integer>();
			HashMap<Integer, Integer> groupSize = new HashMap<Integer, Integer>();
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
					g.getChromosomes().add(c);
					c.setName(occ.getDesc());
					ArrayList<Gene> genes = new ArrayList<Gene>();
					// Forward file pointer to genomes first gene
					reader.jumpToLine(occ.getStart_line()+2);
					boolean printed = false;
					while (reader.getCurrentLineNumber()<=occ.getEnd_line() && (line=reader.readLine())!=null) {
						if (!line.equals("")) {
							String[] explode = line.split("\t");
							if (!printed) System.err.println("=> "+Arrays.toString(explode));
							printed=true;
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
						GeckoInstance.this.gui.changeMode(Gui.MODE_SESSION_IDLE);
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
		GeckoInstance.this.gui.changeMode(Gui.MODE_READING_GENOMES);
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
		System.err.println("CALL INIT PROGRESS");
		final int maxv = maxvalue;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				gui.getProgressbar().setMaximum(maxv);
			}
		});

	};
	
	public void setProgressStatus(int value) {
		final int v = value;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (v<gui.getProgressbar().getMaximum()) {
					gui.changeMode(Gui.MODE_COMPUTING);
					gui.getProgressbar().setValue(v);
				} else {
					gui.changeMode(Gui.MODE_FINISHING_COMPUTATION);
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
		gui.changeMode(Gui.MODE_READING_GENOMES);
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
			try {
				fis = new FileInputStream(f);
				ObjectInputStream o = new ObjectInputStream(fis);
				genomes = (Genome[]) o.readObject();
				geneLabelMap = (int[]) o.readObject();
				colormap = (HashMap<Integer, Color>) o.readObject();
				clusters = (GeneCluster[]) o.readObject();
				if (clusters!=null)
					for (GeneCluster c : clusters)
						c.setMatch(true);
				
				EventQueue.invokeLater(new Runnable() {	
					public void run() {
						gui.updateViewscreen();
						gui.updategcSelector();
						gui.changeMode(Gui.MODE_SESSION_IDLE);						
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
			} catch (ClassNotFoundException e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(gui.getMainframe(),
								"The input file is not in the right format!",
								"Wrong format",
								JOptionPane.ERROR_MESSAGE);
					}
				});
			}
		}
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


	
	
}
