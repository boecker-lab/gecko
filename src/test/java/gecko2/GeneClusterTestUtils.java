package gecko2;

import static org.junit.Assert.assertTrue;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.Parameter;
import gecko2.algorithm.Subsequence;
import gecko2.io.CogFileReader;
import gecko2.io.GeneClusterResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;


/**
 * This class implements static methods for storing a GeneCluster array in a file and reading from a file.
 * 
 * @author Hans-Martin Haase <hans-martin.haase@uni-jena.de>
 * @version 0.10
 *
 */
public class GeneClusterTestUtils {
	//===============================================================================//
	//============================= Methods =========================================//
	//===============================================================================//
	
	/**
	 * Compares two BigDecimals for equal values, up to a precision after the first significant value 
	 * @param expected expected value
	 * @param actual actual value
	 * @param precision how many digits after the first significant value will be compared
	 */
	public static void  assertEqualsBigDecimal(BigDecimal expected, BigDecimal actual, int precision) {
		String[] split = expected.toString().split("E");
		int expExp = 0;
		if (split.length == 2)
			expExp = Integer.parseInt(split[1]);
		
		BigInteger exp = expected.scaleByPowerOfTen(expExp+precision).toBigInteger();
		BigInteger act = actual.scaleByPowerOfTen(expExp+precision).toBigInteger();

	    assertTrue("expected:<"+expected.toString()+"> but was:<"+actual.toString()+">", exp.equals(act));
	    
	    split = actual.toString().split("E");
		int expAct = 0;
		if (split.length == 2)
			expAct = Integer.parseInt(split[1]);
		
		exp = expected.scaleByPowerOfTen(expAct+precision).toBigInteger();
		act = actual.scaleByPowerOfTen(expAct+precision).toBigInteger();

	    assertTrue("expected:<"+expected.toString()+"> but was:<"+actual.toString()+">", exp.equals(act));
	}
	
	/**
	 * Method for writing a GeneClusterResult object to a file.
	 * (This includes informations about the configuration of the computeClusters run)
	 * 
	 * @param geneClusRes a GeneClusterResult object
	 * @param storingPlace File handler for the storing of the input
	 */
	public static void writeToFile(GeneClusterResult geneClusRes, File storingPlace)
	{
		StringBuilder toFile = new StringBuilder();
		
		// at first create the configuration output
		toFile.append("S=").append(geneClusRes.getMinClusterSize()).append("\t");
		toFile.append("D=").append(geneClusRes.getDelta()).append("\t");
		toFile.append("Q=").append(geneClusRes.getQuorum()).append("\t");
		toFile.append("QT=").append(geneClusRes.getQuorumtype()).append("\t");
		toFile.append("OM=").append(geneClusRes.getOperationMode()).append("\t");
		toFile.append("RT=").append(geneClusRes.getRefType()).append("\t");
		toFile.append("FN=").append(geneClusRes.getgSourceFileName()).append("\n");
		
		try 
		{
			FileWriter streamToFile = new FileWriter(storingPlace, true);
			
			// fill the StringBuilder with the GeneCluster informations
			for (GeneCluster cluster : geneClusRes.getCompResult()) 
			{
				toFile.append("[");
				
				// add genes
	            for (int i = 0; i < cluster.getGenes().length; i++)
	            {    
	            	if (i != cluster.getGenes().length - 1)
	                    toFile.append(cluster.getGenes()[i]).append(", ");
	                else
	                {
	                    toFile.append(cluster.getGenes()[i]);
	                }
	            }
	            
	            // add informations which are provided by a simple data type
	            toFile.append("]\tdist=").append(cluster.getMinTotalDist()).append("\trefSeq=").append(cluster.getRefSeqIndex()).append("\tpValue=").append(cluster.getBestPValue()).append("\ttype=").append(cluster.getType()).append("\n");
	            
	            // add informations from the complex data types
	            // BestOccurences
	            for (int j = 0; j < cluster.getOccurrences().length; j++)
	            {
	            	// add informations which are provided by a simple data type
	            	toFile.append("BO[" + j +"]\t").append("id=").append(cluster.getOccurrences()[j].getId());
	            	toFile.append("\tpValue=").append(cluster.getOccurrences()[j].getBestpValue());
	            	toFile.append("\tdist=").append(cluster.getOccurrences()[j].getTotalDist());
	            	toFile.append("\tsupport=").append(cluster.getOccurrences()[j].getSupport()).append("\n");
	            	
	            	// add the sequence informations
	            	for (Subsequence[] subsequences : cluster.getOccurrences()[j].getSubsequences()) 
	            	{
		                for (Subsequence subsequence : subsequences) 
		                {
		                	toFile.append("(");
		                	toFile.append(subsequence.getStart()).append(", ");
		                	toFile.append(subsequence.getStop()).append(", ");
		                	toFile.append(subsequence.getChromosome()).append(", ");
		                	toFile.append(subsequence.getDist()).append(", ");
		                	toFile.append(subsequence.getpValue()).append(")\t");
			            	//toFile.append(String.format("(%d, %d, %d, %d, %f, %d, %d)\t",subsequence.getStart(), subsequence.getStop(), subsequence.getChromosome(), subsequence.getDist(), subsequence.getpValue(), subsequence.getContigSpanningFirst(), subsequence.getContigSpanningSecond()));
		                }
		                
		                toFile.append("\n");
		            }
	            	
		            //toFile.append("\n");
	            }
	              
	            // AllOccurences
	            for (int k = 0; k < cluster.getAllOccurrences().length; k++)
	            {
	            	// add informations which are provided by a simple data type
	            	toFile.append("AO[" + k +"]\t").append("id=").append(cluster.getAllOccurrences()[k].getId());
	            	toFile.append("\tpValue=").append(cluster.getAllOccurrences()[k].getBestpValue());
	            	toFile.append("\tdist=").append(cluster.getAllOccurrences()[k].getTotalDist());
	            	toFile.append("\tsupport=").append(cluster.getAllOccurrences()[k].getSupport()).append("\n");
	            	
	            	// add the sequence informations
	            	for (Subsequence[] subsequences : cluster.getAllOccurrences()[k].getSubsequences()) 
	            	{
		                for (Subsequence subsequence : subsequences) 
		                {
		                	toFile.append("(");
		                	toFile.append(subsequence.getStart()).append(", ");
		                	toFile.append(subsequence.getStop()).append(", ");
		                	toFile.append(subsequence.getChromosome()).append(", ");
		                	toFile.append(subsequence.getDist()).append(", ");
		                	toFile.append(subsequence.getpValue()).append(")\t");
		                    //toFile.append(String.format("(%d, %d, %d, %d, %f, %d, %d)\t",subsequence.getStart(), subsequence.getStop(), subsequence.getChromosome(), subsequence.getDist(), subsequence.getpValue(), subsequence.getContigSpanningFirst(), subsequence.getContigSpanningSecond()));
		                }
		                toFile.append("\n");
		            }
		            //toFile.append("\n");
	            }    
	        }
			
			streamToFile.write(toFile.toString());
			streamToFile.flush();
			streamToFile.close();
			
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File not found.");
		}
		catch (IOException f)
		{
			System.out.println("Errors occured while writing into the file.");
		}
	}
			
	/**
	 * The method launches computeCluster for a given parameter set and 
	 * genomes from a .cog file
	 * 
	 * @param inputCogFile File object to the input file
	 * @param p Parameter set
	 */
	public static void generateRefClusterFile(File inputCogFile, Parameter p, File outputFile) throws FileNotFoundException, IOException, LinePassedException {
		// Load the native library for the computeClusters run
		ReferenceClusterTest.loadLibGecko2();
		
			// generate the GeneCluster array from the input file
		GeckoInstance.getInstance();
		GeckoInstance.getInstance().setCurrentInputFile(inputCogFile);
		CogFileReader reader = new CogFileReader();
		ArrayList<GenomeOccurence> genOcc = reader.importGenomes(inputCogFile);
		reader.readFileContent(genOcc);

		int genomes[][][] = new int[reader.getGenomes().length][][];

		for (int i = 0; i < genomes.length; i++) 
		{
			genomes[i] = new int[reader.getGenomes()[i].getChromosomes().size()][];

			for (int j = 0; j < genomes[i].length; j++)
			{
				genomes[i][j] = reader.getGenomes()[i].getChromosomes().get(j).toIntArray(true, true);
			}
		}

		// define default parameter set
		if (p == null)
		{
			p = new Parameter(3, 7, reader.getGenomes().length, Parameter.QUORUM_NO_COST, 'r', 'd');
		}

		p.setAlphabetSize(reader.getGeneLabelMap().size());

		GeneCluster[] result = GeckoInstance.getInstance().computeClusters(genomes, p, GeckoInstance.getInstance());//GeckoInstance.getInstance().getClusters();

		GeneClusterResult gcResult = new GeneClusterResult(result, p.getMinClusterSize(), p.getDelta(), p.getQ(), p.getQtype(), p.getOperationMode(), p.getRefType(), inputCogFile.getName());

		if (outputFile.exists()) {
			System.err.println("Error: File " + outputFile.getAbsolutePath() + " exists already. Delete it manually if you want to continue!");
			System.exit(1);
		} else {
			System.out.println(outputFile.getAbsolutePath());
			outputFile.createNewFile();
		}

		writeToFile(gcResult, outputFile);
	}

	/**
	 * The method reads a file which contains the the configuration and the result of the
	 * computeClusters method. The file format have to be the same like defined in the 
	 * writeToFile method.
	 * 
	 * - first line the configuration of computeClusters run
	 * - the following lines contain the splited geneCluster array
	 * 
	 * @param resultFile Files object with the file with the results
	 * @return a geneClusterResult object
	 * @throws DataFormatException if the file format is wrong
	 * @throws IOException if the file isn't readable
	 */
	public static GeneClusterResult readResultFile(File resultFile) throws DataFormatException, IOException
	{
		List<GeneClusterOccurrence> allOcc = new ArrayList<GeneClusterOccurrence>();
        List<GeneClusterOccurrence> bestOcc = new ArrayList<GeneClusterOccurrence>();
        int gcIndex = 0;
		
		BufferedReader reader = new BufferedReader(new java.io.FileReader(resultFile));
		try {
			String[] s = reader.readLine().split("\t");
	        
	        if (!s[0].contains("S="))
	        {
	            throw new DataFormatException("Header line corrupt! Missing S=<minClusterSize>");
	        }
	        
	        int minClusterSize = Integer.parseInt(s[0].substring(2));
	        
	        if (!s[1].contains("D="))
	        {
	            throw new DataFormatException("Header line corrupt! Missing D=<distance>");
	        }
	        
	        int distance = Integer.parseInt(s[1].substring(2));
	        
	        if (!s[2].contains("Q="))
	        {
	            throw new DataFormatException("Header line corrupt! Missing Q=<quorum>");
	        }
	        
	        int quorum = Integer.parseInt(s[2].substring(2));
	        
	        if (!s[3].contains("QT="))
	        {
	            throw new DataFormatException("Header line corrupt! Missing QT=<quorumType>");
	        }
	        
	        int quorumType = Integer.parseInt(s[3].substring(3));
	        
	        if (!s[4].contains("OM="))
	        {
	            throw new DataFormatException("Header line corrupt! Missing OM=<operationMode>");
	        }
	        
	        char operationMode = ' ';
	        char oM = s[4].substring(3).trim().charAt(0);
	        char[] modes = {'m', 'c', 'r'};
	       
	        for (char oModes : modes) 
	        {
	            if (oM == oModes) {
	                operationMode = oModes;
	                break;
	            }
	        }
	        
	        if (operationMode == ' ')
	        {
	            throw new DataFormatException("Header line corrupt! Could not parse <operationMode>");
	        }
	        
	        if (!s[5].contains("RT="))
	        {
	            throw new DataFormatException("Header line corrupt! Missing RT=<referenceType>");
	        }
	        
	        char referenceType = ' ';
	        char rT = s[5].substring(3).trim().charAt(0);
	        char[] types2 = {'d', 'g', 'c'};
	       
	        for (char rTypes : types2) 
	        {
	            if (rT == rTypes) {
	                referenceType = rTypes;
	                break;
	            }
	        }
	        
	        if (referenceType == ' ')
	        {
	            throw new DataFormatException("Header line corrupt! Could not parse <referenceType>");
	        }
	                 
	        if (!s[6].contains("FN="))
	        {
	            throw new DataFormatException("Header line corrupt! Missing FN=<fileName>");
	        }
	        
	        String fileName = (s[6].substring(3));

	        // cache the next tree lines
	        String line1 = reader.readLine();
	        
	        // dynamic list for the unknown size of the later array
	        List<GeneCluster> geneClusters = new ArrayList<GeneCluster>();
	        
	        // read the geneCluster array from the file
	        while (line1 != null)
	        {
	            s = line1.split("\t");
	            String[] g = s[0].substring(1, s[0].length() - 1).split(",");
	            int[] genes = new int[g.length];
	            
	            for (int i = 0; i < g.length; i++)
	            {
	                genes[i] = Integer.parseInt(g[i].trim());
	            }
	            
	            if (!s[1].contains("dist="))
	            {
	                 throw new DataFormatException("Line corrupt! Missing dist in: " + s);
	            }
	            
	            int dist = Integer.parseInt(s[1].substring(5));
	            
	            if (!s[2].contains("refSeq="))
	            {
	                throw new DataFormatException("Line corrupt! Missing refSeq in: " + s);
	            }
	            
	            int refSeq = Integer.parseInt(s[2].substring(7));
	            
	            if (!s[3].contains("pValue="))
	            {
	                throw new DataFormatException("Line corrupt! Missing pValue in: " + s);
	            }
	            
	            BigDecimal pValue = new BigDecimal(s[3].substring(7));
	            
	            if (!s[4].contains("type="))
	            {
	                throw new DataFormatException("Line corrupt! Missing type in: " + s);
	            }
	            
	            char type = s[4].charAt(5);
	            
	            line1 = reader.readLine();
	            s = line1.split("\t");
	                  
	            while(!line1.contains("AO["))
	            {	
	            	s = line1.split("\t");
	            	
	            	if (!s[0].contains("BO["))
	            	{
	                	throw new DataFormatException("Line corrupt! Missing index in: " + s);
	            	}
	            
	            	if (!s[1].contains("id="))
	                {
	                     throw new DataFormatException("Line corrupt! Missing id in: " + s);
	                }
	                
	                int id = Integer.parseInt(s[1].substring(3));
	                
	                if (!s[2].contains("pValue="))
	                {
	                     throw new DataFormatException("Line corrupt! Missing pValue in: " + s);
	                }
	                
	                BigDecimal pValue2 = new BigDecimal(s[2].substring(8));
	                
	                if (!s[3].contains("dist="))
	                {
	                     throw new DataFormatException("Line corrupt! Missing dist in: " + s);
	                }
	                
	                int dist2 = Integer.parseInt(s[3].substring(5));
	                
	                if (!s[4].contains("support="))
	                {
	                     throw new DataFormatException("Line corrupt! Missing support in: " + s);
	                }
	                
	                int support = Integer.parseInt(s[4].substring(8));
	                
	                line1 = reader.readLine();
	                
	            	List<Subsequence[]> subsequences = new ArrayList<Subsequence[]>();
	            	
	            	bestOcc = new ArrayList<GeneClusterOccurrence>();
	            	
	            	// read single i1.0ndex of bestOccurrence until the next starts
	            	while (!line1.contains("BO[") && !line1.contains("AO[")) 
	            	{
	            		s = line1.split("\t");
	            		
	            		while(s[0].equals("")) 
	            		{
	            			line1 = reader.readLine();
	                        s = line1.split("\t");
	                        Subsequence[] subseqs = {};
	                        subsequences.add(subseqs);
	                    }
	            		
	            		if (!line1.contains("AO[") && !line1.contains("BO["))
	            		{
	            			Subsequence[] subseqs = new Subsequence[s.length];
	            			for (int i = 0; i < s.length; i++)
	            			{
	            				// create the Subsequence object
	            				String[] occ = s[i].substring(1, s[i].length()-1).split(",");
	            				subseqs[i] = new Subsequence(
	            						Integer.parseInt(occ[0].trim()),
	            						Integer.parseInt(occ[1].trim()),
	            						Integer.parseInt(occ[2].trim()),
	            						Integer.parseInt(occ[3].trim()),
	            						new BigDecimal(occ[4].trim()));
	            			}
	            			
	            			// add subsequence to the "array"
	            			subsequences.add(subseqs);
	            		
	            			line1 = reader.readLine();
	            			s = line1.split("\t");
	            		}
	            	}
	            	
	            	// generate a real Subsequence array
	            	Subsequence[][] subs = new Subsequence[subsequences.size()][];
	            	
	            	for (int k = 0; k < subsequences.size(); k++)
	            	{
	            		subs[k] = subsequences.get(k);
	            	}
	            	
	            	// create the bestOccurrence object
	            	GeneClusterOccurrence bestOccurrence = new GeneClusterOccurrence(id, subs, pValue2, dist2, support);
	            	bestOcc.add(bestOccurrence);            	
	            }
	            
	            allOcc = new ArrayList<GeneClusterOccurrence>();
	           
	            // now AllOccurrences
	            while(!s[0].matches("\\[.*,.*\\]") && line1 != null )
	            {
	            	s = line1.split("\t");
	            	
	            	if (!s[0].contains("AO["))
	            	{
	                	throw new DataFormatException("Line corrupt! Missing index in: " + s);
	            	}
	            
	            	if (!s[1].contains("id="))
	                {
	                     throw new DataFormatException("Line corrupt! Missing id in: " + s);
	                }
	                
	                int id = Integer.parseInt(s[1].substring(3));
	                
	                if (!s[2].contains("pValue="))
	                {
	                     throw new DataFormatException("Line corrupt! Missing pValue in: " + s);
	                }
	                
	                BigDecimal pValue2 = new BigDecimal(s[2].substring(7));
	                
	                if (!s[3].contains("dist="))
	                {
	                     throw new DataFormatException("Line corrupt! Missing dist in: " + s);
	                }
	                
	                int dist2 = Integer.parseInt(s[3].substring(5));
	                
	                if (!s[4].contains("support="))
	                {
	                     throw new DataFormatException("Line corrupt! Missing support in: " + s);
	                }
	                
	                int support = Integer.parseInt(s[4].substring(8));
	                                
	                line1 = reader.readLine();
	                
	            	List<Subsequence[]> subsequences = new ArrayList<Subsequence[]>();
	            	
	            	// read the sequences
	            	while (!s[0].matches("\\[.*,.*\\]") && line1 != null && !line1.contains("AO[")) 
	            	{
	            		s = line1.split("\t");
	            		
	            		while(s[0].equals("")) 
	            		{
	            			line1 = reader.readLine();
	                        s = line1.split("\t");
	                        Subsequence[] subseqs = {};
	                        subsequences.add(subseqs);
	                    }
	      
	            		if (!s[0].contains("AO[") && !s[0].matches("\\[.*,.*\\]"))
	            		{
	            			Subsequence[] subseqs = new Subsequence[s.length];
	            			for (int i = 0; i < s.length; i++)
	            			{
	                				// create the Subsequence object
	                				String[] occ = s[i].substring(1, s[i].length()-1).split(",");
	                				subseqs[i] = new Subsequence(
	                					Integer.parseInt(occ[0].trim()),
	                					Integer.parseInt(occ[1].trim()),
	                					Integer.parseInt(occ[2].trim()),
	                					Integer.parseInt(occ[3].trim()),
	                					new BigDecimal(occ[4].trim()));
	            			}
	            			
	            			// add subsequence to the "array"
	                    	subsequences.add(subseqs);
	            		
	                    	line1 = reader.readLine();
	            		}
	            	}
	            	
	            	// generate a real Subsequence array
	            	Subsequence[][] subs = new Subsequence[subsequences.size()][];
	            	
	            	for (int k = 0; k < subsequences.size(); k++)
	            	{
	            		subs[k] = subsequences.get(k);
	            	}
	            	
	            	// generate allOccurrence array
	            	GeneClusterOccurrence allOccurrences = new GeneClusterOccurrence(id, subs, pValue2, dist2, support);
	            	allOcc.add(allOccurrences);
	            }
	            
	            // generate a real GeneClusterOccurrence array (best occurrence)
	        	GeneClusterOccurrence[] bo = new GeneClusterOccurrence[bestOcc.size()];
	        	
	        	for (int k = 0; k < bestOcc.size(); k++)
	        	{
	        		bo[k] = bestOcc.get(k);
	        	}
	            
	        	// generate a real GeneClusterOccurrence array (all occurrence)
	        	GeneClusterOccurrence[] ao = new GeneClusterOccurrence[allOcc.size()];
	        	
	        	for (int k = 0; k < allOcc.size(); k++)
	        	{
	        		ao[k] = allOcc.get(k);
	        	}
	            
	            GeneCluster cluster = new GeneCluster(gcIndex, bo, ao, genes, pValue, pValue, dist, refSeq, type);
	            geneClusters.add(cluster);
	            gcIndex++;
	        }
	       	GeneCluster[] geneCluster = new GeneCluster[geneClusters.size()];
	    	
	    	for (int k = 0; k < geneClusters.size(); k++)
	    	{
	    		geneCluster[k] = geneClusters.get(k);
	    	}
	        
	        // generate a real GeneCluster array
	        return new GeneClusterResult(geneCluster, minClusterSize, distance, quorum, quorumType, operationMode, referenceType, fileName);
		} finally {
			reader.close();
		}
	}
	
	public static void main(String[] args)
	{
		File inCogFile = null;
		File outFile = null;
		Parameter p = new Parameter(3, 6, 4, Parameter.QUORUM_NO_COST, 'r', 'd');
		try {
			inCogFile = new File(GeneClusterTestUtils.class.getResource("/fiveProteobacter.cog").toURI());
			outFile = new File("src/test/resources/fiveProteobacterD3S6Q4.txt");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			generateRefClusterFile(inCogFile, p, outFile);	
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LinePassedException e) {
			e.printStackTrace();
		}
	}
}
