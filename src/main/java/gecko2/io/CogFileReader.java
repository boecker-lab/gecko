package gecko2.io;

import gecko2.algorithm.*;
import gecko2.exceptions.LinePassedException;
import gecko2.util.SortUtils;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements an reader for .cog file.
 * 
 * The code is originally from GeckoInstance.java and is modified.
 */
public class CogFileReader implements GeckoDataReader {
	/**
	 * Storing place for the geneLabelMap 
	 */
	private Set<GeneFamily> geneFamilySet;
    private GeneFamily unknownGeneFamily;
    private int numberOfGeneFamiliesWithMultipleGenes;
	
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
    private List<GenomeOccurrence> occs;
	
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
     * @param inputFile the input file
     * @param genomeList the list of genomes that will be imported
     */
    public CogFileReader(File inputFile, List<Integer> genomeList) {
        this.inputFile = inputFile;
        if (genomeList == null)
            this.genomeList = null;
        else
            this.genomeList = new ArrayList<>(genomeList);
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
        if (id.equals(""))
            return GeneFamily.UNKNOWN_GENE_ID;
		
		return id;
	}
	
	/**
	 * This method computes the occurrences of the genes.
	 *
	 * @throws FileNotFoundException
	 */
	public void importGenomesOccs() throws FileNotFoundException	{
		occs = new ArrayList<>();
		Map<Integer, Integer> groupSize = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            GenomeOccurrence add = new GenomeOccurrence();
            Map<String, Integer> groups = new HashMap<>();

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
                    add = new GenomeOccurrence();
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
			
			// Remove singleton groups
			for (GenomeOccurrence occ : occs) {
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

		Map<Integer, Genome> groupedGenomes = new HashMap<>();
		List<Genome> ungroupedGenomes = new ArrayList<>();

        this.unknownGeneFamily = GeneFamily.getNewUnknownGeneFamilyAndInitializeAlgorithmId();
        /*
            Maps each possible homologue gene (string) id to the gene family of this gene
         */
        Map<String, GeneFamily> geneFamilyMap = new HashMap<>();

        try (CountedReader reader = new CountedReader(new FileReader(inputFile))){
            this.maxIdLength = -1;
            this.maxNameLength = -1;
            this.maxLocusTagLength = -1;

            for (GenomeOccurrence occ : occs) {
                if (!occ.isFlagged())
                    continue;
                Genome g;
                if (occ.getGroup() == 0) {
                    // If the group id is zero than we have a single chromosome genome,
                    // therefore we have to create a new genome
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

                // Forward file pointer to genomes first gene
                reader.jumpToLine(occ.getStart_line() + 2);

                List<Gene> genes = readChromosome(reader, occ.getEnd_line(), geneFamilyMap);

                // TODO handle the case where EOF is reached before endline
                c.setGenes(genes);
            }
        } catch (LinePassedException e) {
            throw new ParseException(e.getMessage(), 0);
        }

        geneFamilySet = new HashSet<>(geneFamilyMap.values());
        numberOfGeneFamiliesWithMultipleGenes = GeneFamily.getNumberOfGeneFamiliesWithMultipleGenes();
		
		this.genomes = new Genome[groupedGenomes.size() + ungroupedGenomes.size()];

        int i = 0;
        for (Genome x : ungroupedGenomes) {
            this.genomes[i++] = x;
        }

        for (Genome x : groupedGenomes.values()) {
            this.genomes[i++] = x;
        }
	}

    /**
     * Reads all the genes of one chromosome
     * @param reader the reader that is used
     * @param endLine the last line that contains a gene of the chromosome
     * @param geneFamilyMap the mapping of external (String) ids to internal geneFamilies, is modified
     * @return the list of all genes
     * @throws IOException
     */
    private List<Gene> readChromosome(CountedReader reader, int endLine, Map<String, GeneFamily> geneFamilyMap) throws  IOException{
        List<Gene> genes = new ArrayList<>();

        String line;
        while (reader.getCurrentLineNumber() <= endLine && (line = reader.readLine()) != null) {
            if (!line.equals("")) {
                parseGeneLine(line, genes, geneFamilyMap);
            }
        }
        return genes;
    }

    /**
     * Parses one gene containing line of the cog file, append all contained genes to the given list
     * @param line the line that is parsed
     * @param genes the list the new found genes will be appended to, is modified
     * @param geneFamilyMap the mapping of external (String) ids to internal geneFamilies, is modified
     */
    private void parseGeneLine(String line, List<Gene> genes, Map<String, GeneFamily> geneFamilyMap) {
        String[] explode = line.split("\t");
        String[] ids = explode[0].split(",");
        for (int j = 0; j < ids.length; j++)
            ids[j] = this.convertToValidIdFormat(ids[j]);

        Gene.GeneOrientation orientation;
        switch (explode[1]) {
            case "+":
                orientation = Gene.GeneOrientation.POSITIVE;
                break;
            case "-":
                orientation = Gene.GeneOrientation.NEGATIVE;
                break;
            default:
                orientation = Gene.GeneOrientation.UNSIGNED;
        }

        for (String singleId : ids) {   // We split multi id genes into multiple genes.
            if (singleId.length() > maxIdLength)
                maxIdLength = singleId.length();

            if (explode.length > 5 && explode[5].length() > maxNameLength)
                maxNameLength = explode[5].length();

            if (explode[3].length() > maxLocusTagLength) {
                maxLocusTagLength = explode[3].length();
                if (explode.length <= 5 && explode[3].length() > maxNameLength)
                    maxNameLength = explode[3].length();
            }

            GeneFamily geneFamily;
            if (isUnHomologe(singleId)) {
                geneFamily = this.unknownGeneFamily;
                geneFamily.addGene();
            } else {
                if (!geneFamilyMap.containsKey(singleId)) {
                    geneFamily = new GeneFamily(singleId);
                    geneFamilyMap.put(singleId, geneFamily);
                }
                else {
                    geneFamily = geneFamilyMap.get(singleId);
                    geneFamily.addGene();
                }
            }

            Gene gene;
            if (explode.length > 5)
                gene = new Gene(explode[5], explode[3], geneFamily, orientation, explode[4]);
            else
                gene = new Gene(explode[3], geneFamily, orientation, explode[4]);
            genes.add(gene);
        }
    }

	private boolean isUnHomologe(String id) {
		return id.equals(GeneFamily.UNKNOWN_GENE_ID);
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
	public Set<GeneFamily> getGeneFamilySet() {
        return geneFamilySet;
    }

    /**
     * A getter for the total number of gene families that contain more than 1 gene
     *
     * @return the number of gene families that contain more than 1 gene
     */
    @Override
    public int getNumberOfGeneFamiliesWithMultipleGenes() {
        return numberOfGeneFamiliesWithMultipleGenes;
    }

    /**
     * A getter for the gene family grouping all genes with no gene family information
     * @return the unknown gene family
     */
    public GeneFamily getUnknownGeneFamily() {
        return unknownGeneFamily;
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
            for (GenomeOccurrence occ : occs)
                occ.setFlagged(true);
        }

        // Import the genomes
        readFileContent();
    }

    /**
     * Get the genome occurrences.
     * @return the genome occurrences
     */
    public List<GenomeOccurrence> getOccs() {
        return occs;
    }

    private static class IntId{
        int id;
        int size;

        IntId(int id) {
            this.id = Math.abs(id);
            this.size = 1;
        }
    }
}
