package gecko2.io;

import gecko2.CommandLineInterface;
import gecko2.GeckoInstance;
import gecko2.GenomeOccurence;
import gecko2.exceptions.LinePassedException;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.Gene;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;
import gecko2.gui.Gui;
import gecko2.gui.Gui.Mode;
import gecko2.util.PrintUtils;
import gecko2.util.SortUtils;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements an reader for .cog file.
 * 
 * The code is originally from GeckoInstance.java and is modified.
 */
public class CogFileReader {

	/**
	 * Storing place for the colorMap
	 */
	private HashMap<Integer, Color> colorMap;
	
	/**
	 * Storing place for the geneLabelMap 
	 */
	private HashMap<Integer, String[]> geneLabelMap;
	
	/**
	 * Storing place for the genomes.
	 */
	private Genome[] genomes;
	
	/**
	 * Storing place for the length of the longest id.
	 */
	private int maxIdLength;
	
	/**
	 * Pattern list for getGenomeName and getChromosomeName
	 */
	private static final Pattern nameSplitPattern = Pattern.compile(",|chrom(?:osome)?|(?:mega)?plasmid|scaffold|(?:super)?cont(?:ig)?|unmap(?:ped)?|chr[_ ]?\\d+|complete genome", Pattern.CASE_INSENSITIVE);
	
	/**
	 * 0 gui session, 1 cli session
	 */
	private byte sessionType = 0;
	
	public CogFileReader(byte sType) {
		
		sessionType = sType;
		
	}
	
	/**
	 * The method generates the genome name for the grouping of the genes.
	 * 
	 * @param s whole description string from the input file
	 * @return the genome name
	 */
	private String getGenomeName(String s) {		
		Matcher match = nameSplitPattern.matcher(s);
		if (match.find(1))  // do not try to find the pattern at the first position, or Chromobacterium will loose its name
			return s.substring(0,  match.start()).trim();
		
		return s;
	}
	
	/**
	 * The method generates the chromosome name for setting the name of the chromosome.
	 * 
	 * @param s whole description string from the input file
	 * @return the chromosome name
	 */
	private String getChromosomeName(String s) {
		Matcher match = nameSplitPattern.matcher(s);
		if (match.find())
			return s.substring(match.start()).trim();
	
		return "";
	}
	
	/**
	 * The Method tests whether the entered id is from the old integer type.
	 * If so we look if the string id is a row of zeros if so we replace the mass of zeros by one zero.
	 * 
	 * @param id current string id
	 * @return 0 (as string) if id contains only zeros else the unmodified id
	 */
	private String testOldIdFormat(String id) {
		
		try {
			int newID = Integer.parseInt(id);
			return Integer.toString(newID);
		} catch (NumberFormatException e) {}
		
		return id;
	}	
	
	/**
	 * This method computes the occurrences of the genes.
	 * 
	 * @param file File object for the .cog file
	 * @return a ArrayList with the occurrences of the genes
	 * @throws FileNotFoundException
	 */
	public ArrayList<GenomeOccurence> importGenomes(File file) throws FileNotFoundException	{	
		ArrayList<GenomeOccurence> genomeOccurennces = new ArrayList<GenomeOccurence>();
		HashMap<Integer, Integer> groupSize = new HashMap<Integer, Integer>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			try {
				String line;
				GenomeOccurence add = new GenomeOccurence();
				HashMap<String, Integer> groups = new HashMap<String, Integer>();

				int curline = 0;
				int j = 1;
				
				if ((line = reader.readLine()) != null)	{	
					add.setDesc(line);
					String genomeName = getGenomeName(line);
					String chromName = getChromosomeName(line);
					add.setGenomeName(genomeName);
					add.setChromosomeName(chromName);
					groups.put(genomeName, j);
					add.setGroup(j);
					groupSize.put(j, 1);
					add.setStart_line(curline);
				}
				
				boolean next = false;
				
				while ((line = reader.readLine()) != null) {
					curline++;
					
					if (next) {
						add.setEnd_line(curline - 2);
						genomeOccurennces.add(add);
						add = new GenomeOccurence();
						add.setDesc(line);
						String genomeName = getGenomeName(line);
						String chromName = getChromosomeName(line);
						add.setGenomeName(genomeName);
						add.setChromosomeName(chromName);
						
						if (!groups.containsKey(genomeName)) {
							j++;
							groups.put(genomeName, j);
							add.setGroup(j);
							groupSize.put(j, 1);
						} 
						else {		
							int group = groups.get(genomeName);
							add.setGroup(group);
							groupSize.put(group, groupSize.get(group) + 1);
						}
						
						add.setStart_line(curline);
						next = false;
					}
					
					if (line.equals("")) {
						next = true;
					}
				}
				
				
				if (next) {
					add.setEnd_line(curline - 1); 
				}
				else {
					add.setEnd_line(curline);
				}
				
				if (add.getDesc() != null) {
					genomeOccurennces.add(add);
				}
			} finally{
				reader.close();
			}
			
			// Remove singleton groups
			for (GenomeOccurence occ : genomeOccurennces) {
				if (groupSize.get(occ.getGroup()) == 1)	{
					occ.setGroup(0);
				}
			}
			
			// Return result
			return genomeOccurennces;
		} 
		catch (IOException e) {
			if (e instanceof FileNotFoundException)	{
				throw (FileNotFoundException) e;
			}
			
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * The method reads the .cog file and set/generates the global variables.
	 * Variables: colormap, genes, geneLabelMap
	 * 
	 * @param occs gene occurences in the genomes
	 * @throws IOException
	 * @throws LinePassedException
	 */
	public void readFileContent(ArrayList<GenomeOccurence> occs) throws IOException, LinePassedException {
		
		
		Gui gui = GeckoInstance.getInstance().getGui();
		if (gui != null)
			gui.changeMode(Mode.READING_GENOMES);
		
		HashMap<Integer, Genome> groupedGenomes = new HashMap<Integer, Genome>();
		ArrayList<Genome> ungroupedGenomes = new ArrayList<Genome>();
		String line;
		CountedReader reader = new CountedReader(new FileReader(GeckoInstance.getInstance().getCurrentInputFile()));
		
		ArrayList<String[]> stringidlist = new ArrayList<String[]>();
		
		// This is a bit dirty we look only into the first index of the array and store it in this map
		// But it seems like containsKey can't handle arrays as key.
		HashMap<String, Integer> backmap = new HashMap<String, Integer>();
		Random r = new Random();
		this.colorMap = new HashMap<Integer, Color>();
		int maxIdWidth = 0;

        for (GenomeOccurence occ : occs) {
            Genome g;
            if (occ.getGroup() == 0) {
                // If the group id is zero than we have a single chromosome genome,
                // therefore we have to greate a new genome
                g = new Genome();
                g.setName(occ.getGenomeName());
                ungroupedGenomes.add(g);
            } else {
                // If the group id is not zero we need to check if we already created
                // a genome for that group id and if not create a new one
                if (!groupedGenomes.containsKey(occ.getGroup())) {
                    g = new Genome();
                    g.setName(occ.getGenomeName());
                    groupedGenomes.put(occ.getGroup(), g);
                } else {
                    g = groupedGenomes.get(occ.getGroup());
                }
            }

            Chromosome c = new Chromosome(occ.getChromosomeName(), g);
            g.addChromosome(c);
            c.setName(occ.getChromosomeName());
            ArrayList<Gene> genes = new ArrayList<Gene>();

            // Forward file pointer to genomes first gene
            reader.jumpToLine(occ.getStart_line() + 2);
            while (reader.getCurrentLineNumber() <= occ.getEnd_line() && (line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    String[] explode = line.split("\t");
                    String[] ids = explode[0].split(",");
                    for (int j = 0; j < ids.length; j++)
                        ids[j] = this.testOldIdFormat(ids[j]);

                    int sign;
                    if (explode[1].equals("-"))
                        sign = -1;
                    else
                        sign = 1;

                    if (ids[0].length() > maxIdWidth)
                        maxIdWidth = ids[0].length();

                    if (!isUnhomologe(ids) && backmap.containsKey(ids[0])) {
                        if (explode.length > 5)
                            genes.add(new Gene(explode[5], explode[3], sign * backmap.get(ids[0]), explode[4], false));
                        else
                            genes.add(new Gene(explode[3], sign * backmap.get(ids[0]), explode[4], false));
                    } else {
                        stringidlist.add(ids);
                        int intid = stringidlist.size();

                        if (!isUnhomologe(ids)) {
                            this.colorMap.put(intid, new Color(r.nextInt(240), r.nextInt(240), r.nextInt(240)));
                            backmap.put(ids[0], intid);
                        }
                        if (explode.length > 5)
                            genes.add(new Gene(explode[5], explode[3], sign * intid, explode[4], isUnhomologe(ids)));
                        else
                            genes.add(new Gene(explode[3], sign * intid, explode[4], isUnhomologe(ids)));
                    }
                }
            }

            this.maxIdLength = maxIdWidth;

            // Thank you for the not existing autoboxing on arrays...
            this.geneLabelMap = new HashMap<Integer, String[]>();

            for (int j = 1; j < stringidlist.size() + 1; j++) {
                this.geneLabelMap.put(j, stringidlist.get(j - 1));
            }

            // TODO handle the case where EOF is reached before endline
            c.setGenes(genes);
            this.genomes = new Genome[groupedGenomes.size()];
            int j = 0;
            for (Genome x : groupedGenomes.values()) {
                this.genomes[j] = x;
                j++;
            }
        }
		
		this.genomes = new Genome[groupedGenomes.size() + ungroupedGenomes.size()]; {
			int i = 0;
			for (Genome x : ungroupedGenomes) {	
				this.genomes[i++] = x;
			}
			
			for (Genome x : groupedGenomes.values()) {
				this.genomes[i++] = x;
			}
		}		
	}
	
	private boolean isUnhomologe(String[] ids) {
		return (ids[0] == null || ids[0].equals("0") || ids[0].equals(""));
	}
	
	
	/**
	 * This class implements a thread for the reading process.
	 * The code is exported from GeckoInstance.java and modified.
	 */
	private class GenomeReadingThread implements Runnable {

		private final ArrayList<GenomeOccurence> occs;
		
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

				CogFileReader.this.readFileContent(occs);
				
				
				
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						
						/* If someone knows why we have to add the following three lines here */
						/* please tell it to me. I really want to know this :P */
						GeckoInstance.getInstance().setGeneLabelMap(CogFileReader.this.geneLabelMap);
						GeckoInstance.getInstance().setColorMap(CogFileReader.this.colorMap);
						GeckoInstance.getInstance().setGenomes(CogFileReader.this.genomes);
						GeckoInstance.getInstance().setMaxIdLength(CogFileReader.this.maxIdLength);
						
						System.out.println("sessiont:  " + sessionType);
						
						if (CogFileReader.this.sessionType == 0) {
						
							GeckoInstance.getInstance().getGui().changeMode(Gui.Mode.SESSION_IDLE);
							GeckoInstance.getInstance().getGui().updateViewscreen();
						}
						
						GeckoInstance.getInstance().fireDataChanged();
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
	
	/**
	 * First error type
	 */
	private static final short ERROR_FILEFORMAT = 1;
	
	/**
	 * Second error type
	 */
	private static final short ERROR_FILEIO = 2;
	
	
	/**
	 * This method handles errors which are related to the input file.
	 * 
	 * @param errorType 
	 */
	private void handleParsingError(final short errorType) {
		this.genomes = null;
		GeckoInstance.getInstance().setClusters(new GeneCluster[0]);
		
		if (sessionType == 0) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					GeckoInstance.getInstance().getGui().handleFileError(errorType);
				}
			});
		}
		else {
			
			CommandLineInterface.handleFileError(errorType);
		}
	}
	
	/**
	 * This method launches a readingThread.
	 * 
	 * @param occs ArrayList of type GenomeOccurence
	 */
	public void readGenomes(ArrayList<GenomeOccurence> occs)  {
		//GeckoInstance.this.gui.changeMode(Gui.Mode.READING_GENOMES);
		new GenomeReadingThread(occs);	
	}
	
	
	/**
	 * The method is a getter for the colormap. Which contains the relation between the gene and his
	 * color in the GUI.
	 *   
	 * @return the colorMap (HashMap)
	 */
	public Map<Integer, Color> getColorMap()
	{
		return this.colorMap;
	}
	
	/**
	 * The method is the getter for the genomes.
	 * 
	 * @return an array of Genome objects
	 */
	public Genome[] getGenomes()
	{
		return this.genomes;
	}
	
	/**
	 * The method is a getter for the geneLabelMap which contains the relation between external ID
	 * and internal ID from the gene names
	 * 
	 * @return the geneLabelMap (HashMap)
	 */
	public Map<Integer, String[]> getGeneLabelMap()
	{
		return this.geneLabelMap;
	}
	
	/**
	 * The method returns the length of the longest id
	 * 
	 * @return length of the longest id
	 */
	public int getMaxIdLength() {
		
		return this.maxIdLength;
	}
	
}
