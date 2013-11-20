package gecko2.io;

import gecko2.GenomeOccurence;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.Gene;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;
import gecko2.exceptions.LinePassedException;
import gecko2.util.SortUtils;

import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements an reader for .cog file.
 * 
 * The code is originally from GeckoInstance.java and is modified.
 */
public class CogFileReader implements GeckoDataReader {

	/**
	 * Storing place for the colorMap
	 */
	private Map<Integer, Color> colorMap;
	
	/**
	 * Storing place for the geneLabelMap 
	 */
	private Map<Integer, String[]> geneLabelMap;
	
	/**
	 * Storing place for the genomes.
	 */
	private Genome[] genomes;
	
	/**
	 * Storing place for the length of the longest id.
	 */
	private int maxIdLength;

    /**
     * Storing place for the length of the longest name.
     */
    private int maxNameLength;

    /**
     * Storing place for the length of the longest locus tag.
     */
    private int maxLocusTagLength;

    /**
     * The list of genome occurrences. Used for choosing which genome to import.
     */
    private List<GenomeOccurence> occs;
	
	/**
	 * Pattern list for getGenomeName and getChromosomeName
	 */
	private static final Pattern nameSplitPattern = Pattern.compile(",|chrom(?:osome)?|(?:mega)?plasmid|scaffold|(?:super)?cont(?:ig)?|unmap(?:ped)?|chr[_ ]?\\d+|complete genome", Pattern.CASE_INSENSITIVE);


    /**
     * The input File
     */
    private final File inputFile;
    private final List<Integer> genomeList;
	
	public CogFileReader(File inputFile) {
		this(inputFile, null);
	}

    /**
     * genomeList is only used for readData(), to still be able to choose genome occurrences
     * @param inputFile
     * @param genomeList
     */
    public CogFileReader(File inputFile, List<Integer> genomeList) {
        this.inputFile = inputFile;
        if (genomeList == null)
            this.genomeList = null;
        else
            this.genomeList = new ArrayList<Integer>(genomeList);
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
	private String convertToValidIdFormat(String id) {
        id = id.trim();
		try {
			int newID = Integer.parseInt(id);
			return Integer.toString(newID);
		} catch (NumberFormatException e) {}
		
		return id;
	}
	
	/**
	 * This method computes the occurrences of the genes.
	 *
	 * @return a ArrayList with the occurrences of the genes
	 * @throws FileNotFoundException
	 */
	public void importGenomesOccs() throws FileNotFoundException	{
		occs = new ArrayList<GenomeOccurence>();
		Map<Integer, Integer> groupSize = new HashMap<Integer, Integer>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			try {
				String line;
				GenomeOccurence add = new GenomeOccurence();
				Map<String, Integer> groups = new HashMap<String, Integer>();

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
						occs.add(add);
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
					occs.add(add);
				}
			} finally{
				reader.close();
			}
			
			// Remove singleton groups
			for (GenomeOccurence occ : occs) {
				if (groupSize.get(occ.getGroup()) == 1)	{
					occ.setGroup(0);
				}
			}
		} 
		catch (IOException e) {
			if (e instanceof FileNotFoundException)	{
				throw (FileNotFoundException) e;
			}
			e.printStackTrace();
		}
	}
	
	/**
	 * The method reads the .cog file and set/generates the global variables.
	 *
	 * @throws IOException
	 * @throws ParseException
	 */
	public void readFileContent() throws IOException, ParseException{
        SortUtils.resortGenomeOccurencesByStart(occs);

		Map<Integer, Genome> groupedGenomes = new HashMap<Integer, Genome>();
		List<Genome> ungroupedGenomes = new ArrayList<Genome>();
		String line;
        CountedReader reader = null;
        List<String[]> stringidlist = new ArrayList<String[]>();
        try {
            reader = new CountedReader(new FileReader(inputFile));
            // This is a bit dirty we look only into the first index of the array and store it in this map
            // But it seems like containsKey can't handle arrays as key.
            HashMap<String, Integer> backmap = new HashMap<String, Integer>();
            Random r = new Random();
            this.colorMap = new HashMap<Integer, Color>();
            this.maxIdLength = -1;
            this.maxNameLength = -1;
            this.maxLocusTagLength = -1;

            for (GenomeOccurence occ : occs) {
                if (!occ.isFlagged())
                    continue;
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

                Chromosome chr = new Chromosome(occ.getChromosomeName(), g);
                g.addChromosome(chr);
                chr.setName(occ.getChromosomeName());
                ArrayList<Gene> genes = new ArrayList<Gene>();

                // Forward file pointer to genomes first gene
                reader.jumpToLine(occ.getStart_line() + 2);
                while (reader.getCurrentLineNumber() <= occ.getEnd_line() && (line = reader.readLine()) != null) {
                    if (!line.equals("")) {
                        String[] explode = line.split("\t");
                        String[] ids = explode[0].split(",");
                        for (int j = 0; j < ids.length; j++)
                            ids[j] = this.convertToValidIdFormat(ids[j]);

                        int sign = explode[1].equals("-") ? -1 : 1;

                        for (String id : ids) {
                            String[] single_id_array = {id}; // We split multi id genes into multiple genes.

                            if (single_id_array[0].length() > maxIdLength)
                                maxIdLength = single_id_array[0].length();

                            if (explode.length > 5 && explode[5].length() > maxNameLength)
                                maxNameLength = explode[5].length();

                            if (explode[3].length() > maxLocusTagLength) {
                                maxLocusTagLength = explode[3].length();
                                if (explode.length <= 5 && explode[3].length() > maxNameLength)
                                    maxNameLength = explode[3].length();
                            }

                            if (!isUnhomologe(single_id_array) && backmap.containsKey(single_id_array[0])) {
                                if (explode.length > 5)
                                    genes.add(new Gene(explode[5], explode[3], sign * backmap.get(single_id_array[0]), explode[4], false));
                                else
                                    genes.add(new Gene(explode[3], sign * backmap.get(single_id_array[0]), explode[4], false));
                            } else {
                                stringidlist.add(single_id_array);
                                int intid = stringidlist.size();

                                if (!isUnhomologe(single_id_array)) {
                                    this.colorMap.put(intid, new Color(r.nextInt(240), r.nextInt(240), r.nextInt(240)));
                                    backmap.put(single_id_array[0], intid);
                                }
                                if (explode.length > 5)
                                    genes.add(new Gene(explode[5], explode[3], sign * intid, explode[4], isUnhomologe(single_id_array)));
                                else
                                    genes.add(new Gene(explode[3], sign * intid, explode[4], isUnhomologe(single_id_array)));
                            }
                        }
                    }
                }
                // TODO handle the case where EOF is reached before endline
                chr.setGenes(genes);
            }
        } catch (LinePassedException e) {
            throw new ParseException(e.getMessage(), 0);
        }  finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        this.geneLabelMap = new HashMap<Integer, String[]>();

        for (int j = 1; j < stringidlist.size() + 1; j++) {
            this.geneLabelMap.put(j, stringidlist.get(j - 1));
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
     * Method for handling errors while the file is read.
     */
    private void handleFailedSessionLoad() {
        genomes = null;
        geneLabelMap = null;
        colorMap = null;
        maxIdLength = 0;
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

    /**
     * @return the maxNameLength from the input file
     */
    @Override
    public int getMaxNameLength() {
        return maxNameLength;
    }

    /**
     * @return the maxLocusTagLength from the input file
     */
    @Override
    public int getMaxLocusTagLength() {
        return maxLocusTagLength;
    }

    /**
     * Always returns an empty GeneCluster[]. .cog files don't contain gene cluster data.
     * @return the gene clusters
     */
    @Override
    public GeneCluster[] getGeneClusters() {
        return new GeneCluster[0];
    }

    /**
     * Reads all data from the file
     *
     * @throws java.io.IOException      if an IO problem occurred while reading the file
     * @throws java.text.ParseException if the file format is wrong
     */
    @Override
    public void readData() throws IOException, ParseException {
        // Read all occs
        importGenomesOccs();

        // Choose genomes to import
        if (genomeList != null){
            for (Integer selectedGenome : genomeList) {
                occs.get(selectedGenome).setFlagged(true);
            }
        } else {
            for (GenomeOccurence occ : occs)
                occ.setFlagged(true);
        }

        // Import the genomes
        readFileContent();
    }

    /**
     * Get the genome occurrences.
     * @return
     */
    public List<GenomeOccurence> getOccs() {
        return occs;
    }
}
