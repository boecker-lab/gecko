package gecko2.io;

import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.Parameter;
import gecko2.algorithm.Subsequence;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

/**
 * This class models a new data type which contains the result
 * of a computeClusters run (GeneCluser array) and the configuration
 * of this run.
 * 
 * @author Hans-Martin Haase <hans-martin.haase@uni-jena.de>
 * @version 0.03
 */
public class GeneClusterResult 
{
	//=======================================================================================//
	//============================== gloabel variables ======================================//
	//=======================================================================================//
	/**
	 * Stores the GeneGluster array
	 */
	private final GeneCluster[] compResult;
	
	private final Parameter parameter;
	
	/**
	 * Stores the name of the genomes source file
	 */
	private final String gSourceFileName;
	
	//=======================================================================================//
	//================================= Constructor =========================================//
	//=======================================================================================//
	public GeneClusterResult(GeneCluster[] compRes, Parameter param, String gSourceFileName) {
		this.compResult = compRes;
		this.parameter = param;
		this.gSourceFileName = gSourceFileName;
	}
	
	

	//=======================================================================================//
	//================================== Methods ============================================//
	//=======================================================================================//
	
	/**
	 * Getter for the GeneCluster array
	 * 
	 * @return GeneCluster[] the result of computeClusters
	*/
	public GeneCluster[] getCompResult() {
		return compResult;
	}

	/**
	 * The method returns the minimum cluster size
	 * 
	 * @return the minimum cluster size
	 */
	public String getMinClusterSize() {
		return Integer.toString(parameter.getMinClusterSize());

	}

	/**
	 * The method returns the delta value as string
	 * 
	 * @return delta
	 */
	public String getDelta() {
		return Integer.toString(parameter.getDelta());
	}
	
	private String getDeltaTable() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		if (parameter.getDeltaTable() != null) {
			for (int i=0; i<parameter.getDeltaTable().length; i++) {
				builder.append(Arrays.toString(parameter.getDeltaTable()[i]));
				if (i != parameter.getDeltaTable().length-1)
					builder.append(", ");
			}
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * The method returns the quorum value (number of genomes which shall contain the cluster) as string
	 * 
	 * @return the quorum value
	 */
	public String getQuorum() {
		return Integer.toString(parameter.getQ());
	}

	/**
	 * The method returns the quorum type as string
	 * 
	 * @return the quorum type
	 */
	public String getQuorumtype() {
		return Integer.toString(parameter.getQtype());
	}

	/**
	 * The method returns the operation mode as string
	 * 
	 * @return the operation mode
	 */
	public String getOperationMode() {
		return String.valueOf(parameter.getOperationMode());
	}

	/**
	 * The method returns the reference type as string
	 * 
	 * @return refType
	 */
	public String getRefType() {
		return String.valueOf(parameter.getRefType());
	}

	/**
	 * The method returns the name of the genomes source file
	 * 
	 * @return the name of the genomes source file
	 */
	public String getgSourceFileName() {
		return gSourceFileName;
	}
	
	/**
	 * The method returns a Parameter object. codingTable and alphabetSize are not set.
	 * 
	 * @return Parameter the parameter object
	 */
	public Parameter getParameterSet()
	{
		return parameter;
	}
	
	/**
	 * Method for writing a GeneClusterResult object to a file.
	 * (This includes informations about the configuration of the computeClusters run)
	 * 
	 * @param storingPlace File handler for the storing of the input
	 */
	public void writeToFile(File storingPlace) {
		StringBuilder toFile = new StringBuilder();
		
		// at first create the configuration output
		toFile.append("S=").append(getMinClusterSize()).append("\t");
		if (parameter.getDelta() > 0)
			toFile.append("D=").append(getDelta()).append("\t");
		else
			toFile.append("DT=").append(getDeltaTable()).append("\t");
		toFile.append("Q=").append(getQuorum()).append("\t");
		toFile.append("QT=").append(getQuorumtype()).append("\t");
		toFile.append("OM=").append(getOperationMode()).append("\t");
		toFile.append("RT=").append(getRefType()).append("\t");
		toFile.append("FN=").append(getgSourceFileName()).append("\n");
		
		try {
			BufferedWriter streamToFile = new BufferedWriter(new FileWriter(storingPlace, true));
			
			// fill the StringBuilder with the GeneCluster informations
			for (GeneCluster cluster : getCompResult()) {
				toFile.append("[");
				
				// add genes
	            for (int i = 0; i < cluster.getGenes().length; i++) {    
	            	if (i != cluster.getGenes().length - 1)
	                    toFile.append(cluster.getGenes()[i]).append(", ");
	                else {
	                    toFile.append(cluster.getGenes()[i]);
	                }
	            }
	            
	            // add informations which are provided by a simple data type
	            toFile.append("]\tdist=").append(cluster.getMinTotalDist()).append("\trefSeq=").append(cluster.getRefSeqIndex()).append("\tpValue=").append(cluster.getBestPValue().stripTrailingZeros()).append("\tCpValue=").append(cluster.getBestPValueCorrected().stripTrailingZeros()).append("\ttype=").append(cluster.getType()).append("\n");
	            
	            // add informations from the complex data types
	            // BestOccurences
	            for (int i = 0; i < cluster.getOccurrences().length; i++) {
	            	// add informations which are provided by a simple data type
	            	toFile.append("BO[").append(i).append("]\t").append("id=").append(cluster.getOccurrences()[i].getId());
	            	toFile.append("\tpValue=").append(cluster.getOccurrences()[i].getBestpValue().stripTrailingZeros());
	            	toFile.append("\tdist=").append(cluster.getOccurrences()[i].getTotalDist());
	            	toFile.append("\tsupport=").append(cluster.getOccurrences()[i].getSupport()).append("\n");
	            	
	            	// add the sequence informations
	            	for (Subsequence[] subsequences : cluster.getOccurrences()[i].getSubsequences()) {
		                for (Subsequence subsequence : subsequences) {
		                	toFile.append("(");
		                	toFile.append(subsequence.getStart()).append(", ");
		                	toFile.append(subsequence.getStop()).append(", ");
		                	toFile.append(subsequence.getChromosome()).append(", ");
		                	toFile.append(subsequence.getDist()).append(", ");
		                	toFile.append(subsequence.getpValue().stripTrailingZeros()).append(")\t");
		                }              
		                toFile.append("\n");
		            }
	            	
		            //toFile.append("\n");
	            }
	              
	            // AllOccurences
	            for (int i = 0; i < cluster.getAllOccurrences().length; i++) {
	            	// add informations which are provided by a simple data type
	            	toFile.append("AO[").append(i).append("]\t").append("id=").append(cluster.getAllOccurrences()[i].getId());
	            	toFile.append("\tpValue=").append(cluster.getAllOccurrences()[i].getBestpValue().stripTrailingZeros());
	            	toFile.append("\tdist=").append(cluster.getAllOccurrences()[i].getTotalDist());
	            	toFile.append("\tsupport=").append(cluster.getAllOccurrences()[i].getSupport()).append("\n");
	            	
	            	// add the sequence informations
	            	for (Subsequence[] subsequences : cluster.getAllOccurrences()[i].getSubsequences()) {
		                for (Subsequence subsequence : subsequences) {
		                	toFile.append("(");
		                	toFile.append(subsequence.getStart()).append(", ");
		                	toFile.append(subsequence.getStop()).append(", ");
		                	toFile.append(subsequence.getChromosome()).append(", ");
		                	toFile.append(subsequence.getDist()).append(", ");
		                	toFile.append(subsequence.getpValue().stripTrailingZeros()).append(")\t");
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
		catch (FileNotFoundException e) {
			System.out.println("File not found.");
		}
		catch (IOException e){
			System.out.println("Errors occured while writing into the file.");
		}
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
        List<GeneClusterOccurrence> bestOcc = new ArrayList<GeneClusterOccurrence>();
        int gcIndex = 0;
		
		BufferedReader reader = new BufferedReader(new java.io.FileReader(resultFile));
		try {
			String[] s = reader.readLine().split("\t");
			
			if (!s[0].contains("S=")) {
	            throw new DataFormatException("Header line corrupt! Missing S=<minClusterSize>");
	        }
	        
	        int minClusterSize = Integer.parseInt(s[0].substring(2));
	        
	        if (!(s[1].contains("D=") || s[1].contains("DT="))) {
	            throw new DataFormatException("Header line corrupt! Missing D=<distance>, or DT=[distance table]");
	        }
	        
	        int distance = -1;
	        int[][] distanceTable = null;
	        if (s[1].contains("D="))
	        		distance = Integer.parseInt(s[1].substring(2));
	        else {
	        	Pattern p = Pattern.compile("\\[(\\d+), (\\d+), (\\d+)\\]");
	        	Matcher m = p.matcher(s[1].substring(3));
	        	List<int[]> deltaList = new ArrayList<int[]>();
	        	boolean found = m.find();
	        	while (found){
	        		deltaList.add(new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))});
	        		found = m.find();
	        	}
	        	distanceTable = new int[deltaList.size()][];
	        	for (int i=0; i<deltaList.size(); i++)
	        		distanceTable[i] = deltaList.get(i);
	        }
	        
	        if (!s[2].contains("Q=")) {
	            throw new DataFormatException("Header line corrupt! Missing Q=<quorum>");
	        }
	        
	        int quorum = Integer.parseInt(s[2].substring(2));
	        
	        if (!s[3].contains("QT=")) {
	            throw new DataFormatException("Header line corrupt! Missing QT=<quorumType>");
	        }
	        
	        short quorumType = Short.parseShort(s[3].substring(3));
	        
	        if (!s[4].contains("OM=")) {
	            throw new DataFormatException("Header line corrupt! Missing OM=<operationMode>");
	        }
	        
	        char operationMode = ' ';
	        char oM = s[4].substring(3).trim().charAt(0);
	        char[] modes = {'m', 'c', 'r'};
	       
	        for (char oModes : modes) {
	            if (oM == oModes) {
	                operationMode = oModes;
	                break;
	            }
	        }
	        
	        if (operationMode == ' ') {
	            throw new DataFormatException("Header line corrupt! Could not parse <operationMode>");
	        }
	        
	        if (!s[5].contains("RT=")) {
	            throw new DataFormatException("Header line corrupt! Missing RT=<referenceType>");
	        }
	        
	        char referenceType = ' ';
	        char rT = s[5].substring(3).trim().charAt(0);
	        char[] types2 = {'d', 'g', 'c'};
	       
	        for (char rTypes : types2) {
	            if (rT == rTypes) {
	                referenceType = rTypes;
	                break;
	            }
	        }
	        
	        if (referenceType == ' ') {
	            throw new DataFormatException("Header line corrupt! Could not parse <referenceType>");
	        }
	                 
	        if (!s[6].contains("FN=")) {
	            throw new DataFormatException("Header line corrupt! Missing FN=<fileName>");
	        }
	        Parameter p;
	        if (distance >= 0)
	        	p = new Parameter(distance, minClusterSize, quorum, quorumType, operationMode, referenceType);
	        else
	        	p = new Parameter(distanceTable, minClusterSize, quorum, quorumType, operationMode, referenceType);
	        
	        String fileName = s[6].substring(3).trim();

	        // cache the next tree lines
	        String line1 = reader.readLine();
	        
	        // dynamic list for the unknown size of the later array
	        List<GeneCluster> geneClusters = new ArrayList<GeneCluster>();
	        
	        // read the geneCluster array from the file
	        while (line1 != null) {
	            s = line1.split("\t");
	            String[] g = s[0].substring(1, s[0].length() - 1).split(",");
	            int[] genes = new int[g.length];
	            
	            for (int i = 0; i < g.length; i++) {
	                genes[i] = Integer.parseInt(g[i].trim());
	            }
	            
	            if (!s[1].contains("dist=")) {
	                 throw new DataFormatException("Line corrupt! Missing dist in: " + Arrays.toString(s));
	            }
	            
	            int dist = Integer.parseInt(s[1].substring(5));
	            
	            if (!s[2].contains("refSeq=")) {
	                throw new DataFormatException("Line corrupt! Missing refSeq in: " + Arrays.toString(s));
	            }
	            
	            int refSeq = Integer.parseInt(s[2].substring(7));
	            
	            if (!s[3].contains("pValue=")) {
	                throw new DataFormatException("Line corrupt! Missing pValue in: " + Arrays.toString(s));
	            }
	            
	            BigDecimal pValue = new BigDecimal(s[3].substring(7));
	            
	            if (!s[4].contains("CpValue=")) {
	                throw new DataFormatException("Line corrupt! Missing pValue in: " + Arrays.toString(s));
	            }
	            
	            BigDecimal c_pValue = new BigDecimal(s[4].substring(8));
	            
	            if (!s[5].contains("type=")) {
	                throw new DataFormatException("Line corrupt! Missing type in: " + Arrays.toString(s));
	            }
	            
	            char type = s[5].charAt(5);
	            
	            line1 = reader.readLine();
	            s = line1.split("\t");
	                  
	            while(!line1.contains("AO[")) {
	            	s = line1.split("\t");
	            	
	            	if (!s[0].contains("BO[")) {
	                	throw new DataFormatException("Line corrupt! Missing index in: " + Arrays.toString(s));
	            	}
	            
	            	if (!s[1].contains("id=")) {
	                     throw new DataFormatException("Line corrupt! Missing id in: " + Arrays.toString(s));
	                }
	                
	                int id = Integer.parseInt(s[1].substring(3));
	                
	                if (!s[2].contains("pValue=")) {
	                     throw new DataFormatException("Line corrupt! Missing pValue in: " + Arrays.toString(s));
	                }
	                
	                BigDecimal pValue2 = new BigDecimal(s[2].substring(7));
	                
	                if (!s[3].contains("dist=")) {
	                     throw new DataFormatException("Line corrupt! Missing dist in: " + Arrays.toString(s));
	                }
	                
	                int dist2 = Integer.parseInt(s[3].substring(5));
	                
	                if (!s[4].contains("support=")) {
	                     throw new DataFormatException("Line corrupt! Missing support in: " + Arrays.toString(s));
	                }
	                
	                int support = Integer.parseInt(s[4].substring(8));
	                
	                line1 = reader.readLine();
	                
	            	List<Subsequence[]> subsequences = new ArrayList<Subsequence[]>();
	            	
	            	bestOcc = new ArrayList<GeneClusterOccurrence>();
	            	
	            	// read single i1.0ndex of bestOccurrence until the next starts
	            	while (!line1.contains("BO[") && !line1.contains("AO[")) {
	            		s = line1.split("\t");
	            		
	            		while(s[0].equals("")) {
	            			line1 = reader.readLine();
	                        s = line1.split("\t");
	                        Subsequence[] subseqs = {};
	                        subsequences.add(subseqs);
	                    }
	            		
	            		if (!line1.contains("AO[") && !line1.contains("BO[")) {
	            			Subsequence[] subseqs = new Subsequence[s.length];
	            			for (int i = 0; i < s.length; i++) {
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
	            	
	            	for (int k = 0; k < subsequences.size(); k++) {
	            		subs[k] = subsequences.get(k);
	            	}
	            	
	            	// create the bestOccurrence object
	            	GeneClusterOccurrence bestOccurrence = new GeneClusterOccurrence(id, subs, pValue2, dist2, support);
	            	bestOcc.add(bestOccurrence);
	            }

                List<GeneClusterOccurrence> allOcc = new ArrayList<GeneClusterOccurrence>();
	           
	            // now AllOccurrences
	            while(!s[0].matches("\\[.*,.*\\]") && line1 != null ) {
	            	s = line1.split("\t");
	            	
	            	if (!s[0].contains("AO[")) {
	                	throw new DataFormatException("Line corrupt! Missing index in: " + Arrays.toString(s));
	            	}
	            
	            	if (!s[1].contains("id=")) {
	                     throw new DataFormatException("Line corrupt! Missing id in: " + Arrays.toString(s));
	                }
	                
	                int id = Integer.parseInt(s[1].substring(3));
	                
	                if (!s[2].contains("pValue=")) {
	                     throw new DataFormatException("Line corrupt! Missing pValue in: " + Arrays.toString(s));
	                }
	                
	                BigDecimal pValue2 = new BigDecimal(s[2].substring(7));
	                
	                if (!s[3].contains("dist=")) {
	                     throw new DataFormatException("Line corrupt! Missing dist in: " + Arrays.toString(s));
	                }
	                
	                int dist2 = Integer.parseInt(s[3].substring(5));
	                
	                if (!s[4].contains("support=")) {
	                     throw new DataFormatException("Line corrupt! Missing support in: " + Arrays.toString(s));
	                }
	                
	                int support = Integer.parseInt(s[4].substring(8));
	                                
	                line1 = reader.readLine();
	                
	            	List<Subsequence[]> subsequences = new ArrayList<Subsequence[]>();
	            	
	            	// read the sequences
	            	while (!s[0].matches("\\[.*,.*\\]") && line1 != null && !line1.contains("AO[")) {
	            		s = line1.split("\t");
	            		
	            		while(s[0].equals("")) {
	                        Subsequence[] subseqs = {};
	                        subsequences.add(subseqs);
	                        
	            			line1 = reader.readLine();
	            			if (line1 == null)
	            				break;
	                        s = line1.split("\t");
	                    }
	            		
	            		if (line1 == null) 
	            			break;
	      
	            		if (!s[0].contains("AO[") && !s[0].matches("\\[.*,.*\\]")) {
	            			Subsequence[] subseqs = new Subsequence[s.length];
	            			for (int i = 0; i < s.length; i++) {
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
	            	
	            	for (int k = 0; k < subsequences.size(); k++) {
	            		subs[k] = subsequences.get(k);
	            	}
	            	
	            	// generate allOccurrence array
	            	GeneClusterOccurrence allOccurrences = new GeneClusterOccurrence(id, subs, pValue2, dist2, support);
	            	allOcc.add(allOccurrences);
	            }
	            
	            // generate a real GeneClusterOccurrence array (best occurrence)
	        	GeneClusterOccurrence[] bo = new GeneClusterOccurrence[bestOcc.size()];
	        	
	        	for (int k = 0; k < bestOcc.size(); k++) {
	        		bo[k] = bestOcc.get(k);
	        	}
	            
	        	// generate a real GeneClusterOccurrence array (all occurrence)
	        	GeneClusterOccurrence[] ao = new GeneClusterOccurrence[allOcc.size()];
	        	
	        	for (int k = 0; k < allOcc.size(); k++) {
	        		ao[k] = allOcc.get(k);
	        	}
	            
	            GeneCluster cluster = new GeneCluster(gcIndex, bo, ao, genes, pValue, c_pValue, dist, refSeq, type);
	            geneClusters.add(cluster);
	            gcIndex++;
	        }
	       	GeneCluster[] geneCluster = new GeneCluster[geneClusters.size()];
	    	
	    	for (int k = 0; k < geneClusters.size(); k++) {
	    		geneCluster[k] = geneClusters.get(k);
	    	}
	        
	        // generate a real GeneCluster array
	        return new GeneClusterResult(geneCluster, p, fileName);
		} finally {
			reader.close();
		}
	}
}
