package gecko2.io;

import gecko2.GeckoInstance;
import gecko2.GenomeOccurence;
import gecko2.LinePassedException;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Parameter;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class is a command line interface for gecko2.
 * 
 * Exit status descriptions
 * 0	program ended normally
 * 1	input file does not exist
 * 2	output file already exists and overwriting is not allowed
 * 3	unknown command line parameter
 * 4	cog file has a false format
 * 5    line number behind eof given (CountedReader)
 * 6	problems while reading the file (CogFileReader or CountedReader)
 * 7	readers pointer already points at a line after the given line number (CountedReader)
 * 8	used in {@link gecko2.Gecko2#main(String[]) Gecko2}
 * 9	used in {@link gecko2.io.GckFileReader#handleFailedSessionLoad() GckFileReader}
 * 10	the input gck file == output gck file
 * 11	maxDist smaller than 0
 * 12	minimal cluster size smaller than 1 
 * 13	minimal genome number is smaller 2 or greater than the number of genomes
 * 14	wrong format of genome selection
 * 15	wrong file extension of the output file
 * 
 * @author Hans-Martin Haase <hmhaase at pclinuxosusers dot de> 
 * @version 0.06
 */
public class CLI {

	/**
	 * Maximum distance of the cluster sequences.
	 * Default value is 3.
	 */
	private byte maxDist = 3;
	
	/**
	 * Minimum size of the clusters.
	 * Default value is 7
	 */
	private byte minClusSize = 7;
	
	/**
	 * Minimum number of genomes which contains the cluster.
	 */
	private int minGenomeNum = -1;
	
	/**
	 * Operation mode.
	 * r  Reference
	 * m  Median
	 * c  Center
	 */
	private char opMode = 'r';
	
	/**
	 * Reference type
	 * d  all against all
	 * g  fixed genome
	 * m  manual cluster
	 */
	private char refType = 'd';
	
	/**
	 * The input cog or gck file.
	 */
	private File inFile;
	
	/**
	 * The output gck file.
	 */
	private File gckFile;
	
	/**
	 * The parameter set for the computation
	 */
	private Parameter parameter;
	
	/**
	 * The 3 dimensional integer array for the computation.
	 */
	private int[][][] genomes;
	
	/**
	 * Type of the input file 0 means .cog and 1 .gck
	 */
	private byte inFileType = -1;
	
	/**
	 * ArrayList with the custom selection of genomes.
	 */
	private ArrayList<Integer> selectedGenomes = null;
	
	/**
	 * The constructor launches the parse, preparation, launchCalculation and saveSession method.
	 * 
	 * @param optionsLine the options line given on the command line
	 * @param extLib true if a external libgecko shall be used else false
	 */
	public CLI(String[] optionsLine, boolean extLib) {
		
		parse(optionsLine, extLib);
		
		try {
			
			preparation();
			
		} 
		catch (EOFException e) {
			
			System.err.println("End of file reached because of passing a line number which does not exist.");
			e.printStackTrace();
			System.exit(5);
		} 
		catch (IOException e) {
			
			System.err.println("Problems occured while reading the input file.");
			e.printStackTrace();
			System.exit(6);
			
		} 
		catch (LinePassedException e) {
			
			System.err.println("The readers pointer already points at a line after the given line number.");
			e.printStackTrace();
			System.exit(7);
		}
		
		checkParameters();
		launchCalculation();
		saveSession();
	}
	
	/**
	 * The method parses the command line parameter line and sets the required variables.
	 * 
	 * @param opLine the command line parameter line
	 * @param extLib true if a external libgecko is used else false
	 */
	private void parse(String[] opLine, boolean extLib) {
		
		// define cli parameter patterns
		Pattern maDist = Pattern.compile("--maxDist=[0-9]+");
		Pattern miSize = Pattern.compile("--minCSize=[0-9]+");
		Pattern miGNum = Pattern.compile("--minGNum=[0-9]+");
		Pattern searchMode = Pattern.compile("--sm=[rmc]");
		Pattern rft = Pattern.compile("--rfT=[dgm]");
		
		// index 0 is library or .cog file so we do not need to
		// check that indices
		int start = 1;
		
		if (extLib) {
			
			start = 2;
		}
		
		// check cog/gck input file
		String inputFile = opLine[start - 1].split(":")[0];
		boolean cogFile = false;
		
		
		if (inputFile.endsWith(".cog")) {
			
			cogFile = true;
		}
		
		// check whether specific genomes are selected (just for cog files)
		if (opLine[start - 1].split(":").length > 1 && cogFile == true) {
			
			selectedGenomes = new ArrayList<Integer>();
			
			for (String g : opLine[start - 1].split(":")[1].split(",")) {
				
				if (g.contains("-")) {
					
					String[] str = g.split("-");
					
					if (str.length > 2) {
						
						System.err.println("Wrong format of the genome selection query.");
						System.exit(14);
					}
					
					int startSelec = Integer.parseInt(str[0]);
					int endSelec = Integer.parseInt(str[1]);
					
					for (int i = startSelec; i < endSelec + 1; i++) {
						
						selectedGenomes.add(i);
					}
				}
				else {
					
					selectedGenomes.add(Integer.parseInt(g));
				}
			}
		}
		
		// check input file
		if (testFile(new File(inputFile), 'c', false)) {
			
			this.inFile = new File(inputFile);
			this.inFileType = 0;
		}
		else {
			
			if (testFile(new File(inputFile), 'g', false)) {
				
				this.inFile = new File(inputFile);
				this.inFileType = 1;
			}
			else {
			
				System.err.println("Input file does not exist. Aborting ...");
				System.exit(1);
			}
		}
		
		// check gck output file
		String gckFileIn = opLine[opLine.length - 1];
		
		if (testFile(new File(gckFileIn), 'g', true)) {
			
			System.err.println("Aborting ...");
			System.exit(2);
		}
		else {
			
			this.gckFile = new File(gckFileIn);
		}
		
		// if input gck then check that the file is not the output file also
		if (inFileType == 1) {
			
			if (gckFile.getName().equals(inFile.getName())) {
				
				System.err.println("The input .gck file is also the output .gck file.\nThat's not allowed.");
				System.exit(10);
			}
		}
		
		// check for options
		for (int i = start ; i < opLine.length - 1; i++) {
			
			Matcher optMatcher = rft.matcher(opLine[i]);
			
			if (optMatcher.find()) {
				
				optMatcher.usePattern(Pattern.compile("--rfT="));
				refType = optMatcher.replaceAll("").charAt(0);
			}
			else {
				
				optMatcher = searchMode.matcher(opLine[i]);
				
				if (optMatcher.find()) {
					
					optMatcher.usePattern(Pattern.compile("--sm="));
					opMode = optMatcher.replaceAll("").charAt(0);
				}
				else {
					
					optMatcher = miGNum.matcher(opLine[i]);
					
					if (optMatcher.find()) {
						
						optMatcher.usePattern(Pattern.compile("--minGNum="));
						minGenomeNum = Integer.parseInt(optMatcher.replaceAll(""));
					}
					else {
						
						optMatcher = miSize.matcher(opLine[i]);
						
						if (optMatcher.find()) {
							
							optMatcher.usePattern(Pattern.compile("--minCSize="));
							minClusSize = (byte) Integer.parseInt(optMatcher.replaceAll(""));
						}
						else {
							
							optMatcher = maDist.matcher(opLine[i]);
							
							if (optMatcher.find()) {
								
								optMatcher.usePattern(Pattern.compile("--maxDist="));
								maxDist = (byte) Integer.parseInt(optMatcher.replaceAll(""));
							}
							else {
								
								System.err.println("Unknown command line parameter or wrong value " + opLine[i] + ". Aborting ...");
								System.exit(3);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * The method prepares the necessary data structures for the computation.
	 * 
	 * @throws EOFException from cog file reader
	 * @throws IOException from cog file reader or gck file reader
	 * @throws LinePassedException from cog file reader
	 */
	private void preparation() throws EOFException, IOException, LinePassedException {
		
		// read the input file
		GeckoInstance.getInstance().setCurrentInputFile(inFile);
		
		if (this.inFileType == 0) {
		
			// input file is a .cog file
			CogFileReader reader = new CogFileReader((byte) 1);
			ArrayList<GenomeOccurence> genOcc;
	
			genOcc = reader.importGenomes(inFile);
			
			// select specified genomes
			if (selectedGenomes != null) {
				
				for (int i = 0; i < selectedGenomes.size(); i++) {
					
					genOcc.get(selectedGenomes.get(i)).setFlagged(true);
				}
			}
			else {
			
				for (GenomeOccurence occ : genOcc) {
				
					occ.setFlagged(true);
				}
			}
			
			reader.readFileContent(genOcc);
		
			// create the 3 dimensional int array for the computation
			genomes = new int[reader.getGenomes().length][][];
		
			for (int k = 0; k < genomes.length; k++) 
			{
				genomes[k] = new int[reader.getGenomes()[k].getChromosomes().size()][];		
				for (int j = 0; j < genomes[k].length; j++)
					genomes[k][j] = reader.getGenomes()[k].getChromosomes().get(j).toIntArray(true, true);
			}

			// if no minimum number of genomes is given we use all
			if (minGenomeNum == -1) 
				minGenomeNum = reader.getGenomes().length;
			
			// create the parameter set for computeClusters
			parameter = new Parameter(maxDist, minClusSize, minGenomeNum, Parameter.QUORUM_NO_COST, opMode, refType);
			parameter.setAlphabetSize(reader.getGeneLabelMap().size());		
			
			// store important information in the gecko instance for the 
			// saving after the computation
			GeckoInstance.getInstance().setGenomes(reader.getGenomes());
			GeckoInstance.getInstance().setColorMap(reader.getColorMap());
			GeckoInstance.getInstance().setGeneLabelMap(reader.getGeneLabelMap());
			GeckoInstance.getInstance().setMaxIdLength(reader.getMaxIdLength());
			GeckoInstance.getInstance().setSessionType((byte) 1);
		}
		else {
			// now we have a gck file as input
			GckFileReader reader = new GckFileReader();
			reader.setSessionType(1);
			reader.loadSessionFromFile(inFile);
		
			// if no minimum number of genomes is given we use all
			if (minGenomeNum == -1) 
				minGenomeNum = reader.getGenomes().length;
			
			// create the parameter set for computeClusters
			parameter = new Parameter(maxDist, minClusSize, minGenomeNum, Parameter.QUORUM_NO_COST, opMode, refType);
			parameter.setAlphabetSize(reader.getGeneLabelMap().size());		
		
			// store important information in the gecko instance for the 
			// saving after the computation
			GeckoInstance.getInstance().setGenomes(reader.getGenomes());
			GeckoInstance.getInstance().setColorMap(reader.getColorMap());
			GeckoInstance.getInstance().setGeneLabelMap(reader.getGeneLabelMap());
			GeckoInstance.getInstance().setMaxIdLength(reader.getMaxIdLength());
			GeckoInstance.getInstance().setSessionType(1);
		}
	}
	
	/**
	 * The method tests the given file for existence and if the given file is the 
	 * output file and exists than it asks for overwriting.
	 * 
	 * @param fileToTest the file to test for existence
	 * @param suffix c or g dependend on the input file c for cog, for gck
	 * @param out true if the file is the output file
	 * @return true if the file does exist, false if the file does not exist or shall be overwritten
	 */
	private boolean testFile(File fileToTest, char suffix, boolean out) {
		
		boolean result = false;
		
		if (suffix == 'c') {
			
			if (!fileToTest.exists() || fileToTest.getName().endsWith(".gck") || !fileToTest.getName().endsWith(".cog")) {
				
				//System.err.println("The given input file " + fileToTest.getAbsolutePath() + " does not exist.");
				result = false;
			}
			else {
				
				result = true;
			}
		}
		
		if (suffix == 'g') {
			
			if (!fileToTest.getName().endsWith(".gck")) {
				
				System.err.println("The file " + fileToTest.getName() + " have to end with .gck");
				System.exit(15);
			}
			
			if (fileToTest.exists() && out == true) {
				
				System.err.println("The given output file " + fileToTest.getAbsolutePath() + " already exists.");
				System.out.println("Do you want to overwrite the file? [Y|n]");
				
				BufferedReader answerIn = new BufferedReader(new InputStreamReader(System.in));
				
				String tester = "";
				
				try {
					
					tester = answerIn.readLine();
					
				} catch (IOException e) {
					
					System.err.println("An error occurred while reading your command line input. \nSo your input file won't be overwritten.");
					tester = "no";
				}
				
				Pattern yes = Pattern.compile("yes|Yes|YES|y|Y");
				Matcher ansMatcher = yes.matcher(tester);
				
				if (ansMatcher.find()) {
					
					result = false;
				}
				else {
					
					result = true;
				}
			}
			else {
				
				if (fileToTest.exists()) {
					
					result = true;
				}
				else {
					
					result = false;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * This method handles file error if some thing is wrong with the input cog file.
	 * 
	 * @param error Type of the error.
	 */
	public static void handleFileError(short error) {
		
		System.err.println("The input file is not a valid COG file. Wrong file format. Aborting ...");
		System.exit(4);
	}
	
	/**
	 * The method starts the cluster computation and stores the results in the current GeckoInstance.
	 */
	private void launchCalculation() {
		
		// compute the clusters
		GeneCluster[] clusters = GeckoInstance.getInstance().computeClusters(genomes, parameter);
		
		// set the cluster in the session for saving the session
		GeckoInstance.getInstance().setClusters(clusters);
	}
	
	/**
	 * The method shows the help text for the command line interface.
	 */
	public static void showHelp() {
		
		System.out.println("This is the help for Gecko2.\n" +
				"\n" +
				"Note: Without any command line option Gecko2 will be started in graphical mode. \n" +
				"Usage: gecko2.jar [libgecko2] [options1] <input .cog or .gck file> [options2] <output .gck file> \n\n" + 
				"libgecko2            optional path to an external libgecko2 library \n\n" +
				"Options1: \n" + 
				"     -h | --help     show this help \n" + 
				"     -g | --gui      start Gecko2 in graphical mode \n\n" +
				"Options2: \n" +
				"     --maxDist=x     maximum distance of the cluster sequences [default = 3] \n" +
				"     --minCSize=y    minimal size of the clusters [default = 7] \n" + 
				"     --minGNum=z     minimal number of genomes which contain the cluster sequence \n" + 
				"                     [default all genomes] \n" + 
				"     --sm=r|m|c      search mode r reference, m median, c center [default r] \n" + 
				"     --rfT=d|g|m     reference type d all against all, g fixed genome, m manual cluster \n" + 
				"                     [default d] \n" + 
				"\n" +
				"Default values are taken if no other options are specified."
				);
	}
	
	/**
	 * The method saves the computation result and other important information in a session file and 
	 * closes the program.
	 */
	private void saveSession() {
		
		SessionWriter.saveSessionToFile(gckFile);
		System.exit(0);
	}
	
	/**
	 * This method checks the parameters which require a integer value whether the values are valid.
	 */
	private void checkParameters() {
		
		if (maxDist < 0) {
			
			System.err.println("Impossible situation occurred: Distance between the clusters smaller than zero.");
			System.exit(11);
		}
		
		if (minClusSize < 1) {
			
			System.err.println("Impossible situation occurred: The cluster size have to be at least 1.");
			System.exit(12);
		}
		
		if (minGenomeNum < 2 || minGenomeNum > this.genomes.length) {
			
			System.err.println("Impossible situation occurred: The minimal genome number is smaller than 2 or \nhigher than the number of genomes.");
			System.exit(13);
		}
	}
}
