package gecko2.io;

import gecko2.algorithm.Gene;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.GeneClusterOutput;
import gecko2.algorithm.Subsequence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ResultWriter {
	public static boolean exportResultsToFileNEW2(File f, List<GeneCluster> clusters, int[] geneLabelMap, List<String> genomeNames) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			
			Collections.sort(clusters, new Comparator<GeneCluster>() {
				@Override
				public int compare(GeneCluster o1, GeneCluster o2) {
					return Double.compare(o1.getBestPValue(), o2.getBestPValue());
				}
			});
			writer.write(String.format("Detected %d gene clusters.\n", clusters.size()));
			writer.write("General statistics:\n");
			
			int[] clusterSizes = new int[50];
			int[] intervalLength = new int[50];
			int[][] intervalLengths = new int[genomeNames.size()][50];
			int[] intervalLengthMult = new int[50];
			int[][] intervalLengthsMult = new int[genomeNames.size()][50];
			List<String> clusterTabel = new ArrayList<String>(clusters.size());
			for (int c=0; c<clusters.size(); c++){
				GeneCluster cluster = clusters.get(c);
				
				StringBuilder builder = new StringBuilder();
				builder.append(c).append("\t").append(cluster.getGenes().length).append("\t");
				
				clusterSizes[cluster.getGenes().length]++;
				List<Integer> bestOccsOnSameChrom = new ArrayList<Integer>();
				List<Integer> allOccsOnSameChrom = new ArrayList<Integer>();
				int totalOccs = 0;
				int [] clusterLengths = new int[50];
				for (GeneClusterOccurrence occ : cluster.getOccurrences()){
					for (int i=0; i<occ.getSubsequences().length; i++) {
						Subsequence[] subseq = occ.getSubsequences()[i];
						if (subseq != null) {
							if (subseq.length != 0) {
								intervalLength[subseq[0].getStop() - subseq[0].getStart() + 1]++;
								intervalLengths[i][subseq[0].getStop() - subseq[0].getStart() + 1]++;
							}
							int bestOccs = 0;
							for (Subsequence sub : subseq) {
								intervalLengthMult[sub.getStop() - sub.getStart() + 1]++;
								intervalLengthsMult[i][sub.getStop() - sub.getStart() + 1]++;
								clusterLengths[sub.getStop() - sub.getStart() + 1]++;
								bestOccs++;
								totalOccs++;
							}
							//if (bestOccs != 0)     // uncomment, if many non occs in genomes are expected 
							bestOccsOnSameChrom.add(bestOccs);
						}
					}
				}
				for (GeneClusterOccurrence occ : cluster.getAllOccurrences()){
					for (int i=0; i<occ.getSubsequences().length; i++) {
						Subsequence[] subseq = occ.getSubsequences()[i];
						if (subseq != null) {
							int allOccs = 0;
							for (Subsequence sub : subseq) {
								allOccs++;
							}
							//if (allOccs != 0)      // uncomment, if many non occs in genomes are expected 
							allOccsOnSameChrom.add(allOccs);
						}
					}
				}

				builder.append(totalOccs).append("\t[");
				
				boolean first = true;
				for (int i=0; i<clusterLengths.length; i++) {
					if (clusterLengths[i] != 0) {
						if (!first)
							builder.append(", ");
						else
							first = false;
						builder.append(i).append(":").append(clusterLengths[i]);
					}
				}
				builder.append("]\t[");
				
				first = true;
				for (Integer i: bestOccsOnSameChrom) {
					if (!first)
						builder.append(", ");
					else
						first = false;
					builder.append(i);
				}
				
				builder.append("]\t[");
				
				first = true;
				for (Integer i: allOccsOnSameChrom) {
					if (!first)
						builder.append(", ");
					else
						first = false;
					builder.append(i);
				}
				builder.append("]\n");
				clusterTabel.add(builder.toString());
			}
			
			writer.write("Genes per cluster \t #clusters\n");
			for (int i=0; i<clusterSizes.length; i++) {
				if (clusterSizes[i] != 0)
					writer.write(String.format("%d \t %d\n", i, clusterSizes[i]));
			}
			writer.write("\n");
			
			int lenSum = 0;
			int multLenSum = 0;
			writer.write("Interval Lengths \t #intervals \t #multiple intervals \n");
			for (int i=0; i<intervalLength.length; i++) {
				if (intervalLength[i] != 0 || intervalLengthMult[i] != 0) {
					writer.write(String.format("%d \t %d \t %d \n", i, intervalLength[i], intervalLengthMult[i]));
					lenSum += intervalLength[i];
					multLenSum += intervalLengthMult[i];
				}
			}
			writer.write(String.format("Sum \t %d \t %d \n", lenSum, multLenSum));
			writer.write("\n");
			
			writer.write("Cluster \t #Genes \t Occurrences \t Lengths:# \t BestOccs per Chromosome \t AllOccs per Chromosome\n");
			for (String line : clusterTabel)
				writer.write(line);
			writer.write("\n");
			
			for (int i=0; i<intervalLengths.length; i++) {
				boolean contained = false;
				for (Integer len : intervalLengths[i])
					if (len != 0) {
						contained = true;
						break;
					}
				if (contained) {
					writer.write(String.format("%s: \n", genomeNames.get(i)));
					writer.write("lengths \t #intervals \t # multiple intervals \n");
					for (int j=0; j<intervalLengths[i].length; j++) {
						if (intervalLengths[i][j] != 0 || intervalLengthsMult[i][j] != 0)
							writer.write(String.format("%d \t %d \t %d \n", j, intervalLengths[i][j], intervalLengthsMult[i][j]));
					}
					writer.write("\n");
				}
			}
			
			for (GeneCluster cluster : clusters) {
				GeneClusterOutput clusterData = cluster.generateGeneClusterOutput();			
				writer.write(String.format("new cluster: pValue = %e, refSeq = %d\n", clusterData.getPValue(), clusterData.getRefSeq()+1));
				writer.write("in chromosomes:\n");
				for (int i=0; i<clusterData.getChromosomes().size(); i++)
					if (clusterData.getChromosomes().get(i) != null) {
						writer.write(String.format("%d:\t%s\t", i+1, clusterData.getChromosomes().get(i)));
						for (Integer gene : clusterData.getIntervals().get(i))
							if (gene >= 0)
								writer.write(String.format("%d>  ", gene));
							else
								writer.write(String.format("<%d  ", Math.abs(gene)));
						writer.write("\n");
					}
				writer.write("ChromosomeNr:");
				for (int i=0; i<clusterData.getDistances().length; i++)
					if (clusterData.getDistances()[i] >= 0)
						writer.write(String.format("\t%d", i+1));
				writer.write("\n");
				writer.write("Distances:");
				for (int i=0; i<clusterData.getDistances().length; i++)
					if (clusterData.getDistances()[i] >= 0)
						writer.write(String.format("\t%d", clusterData.getDistances()[i]));
				writer.write("\n");
				writer.write("Occurrences:");
				for (int i=0; i<clusterData.getDistances().length; i++)
					if (clusterData.getDistances()[i] >= 0)
						writer.write(String.format("\t%d", clusterData.getNrOfOccurrences()[i]));
				writer.write("\n");
				
				Map<Integer, Gene[]>geneAnnotations = clusterData.getGeneAnnotations();
				for (Integer geneid : geneAnnotations.keySet()) {
					Gene[] annotation = geneAnnotations.get(geneid);
					for (int i=0; i<annotation.length; i++) {
						if (0==i)
							writer.write(String.format("%d:", geneid));
						if (annotation[i] != null)
							writer.write(String.format("\t%d: %s\n", i+1, annotation[i].getSummary()));
						else if (clusterData.getChromosomes().get(i) != null)
							writer.write(String.format("\t%d: ---\n", i+1));
					}
				}
				writer.write("\n");
			}
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean exportResultsToFileNEW(File f, List<GeneCluster> clusters, int[] geneLabelMap) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			
			Collections.sort(clusters, new Comparator<GeneCluster>() {
				@Override
				public int compare(GeneCluster o1, GeneCluster o2) {
					return Double.compare(o1.getBestPValue(), o2.getBestPValue());
				}
			});
			
			writer.write(String.format("gene cluster list of size %d\n", clusters.size()));
			
			for (GeneCluster cluster : clusters) {
				writer.write(String.format("K = %d\n", 0)); //TODO 0
				writer.write(String.format("new Cluster: pValue = %e\n", cluster.getBestPValue()));
				writer.write("gene content: " + geneContentString(cluster.getGenes()) + "\n");
				writer.write(String.format("             type: %c, min total dist: %d, refGenomeNr: %d\n", 'r', cluster.getMinTotalDist(), cluster.getRefSeqIndex())); //TODO 'r'
				writer.write("\n");
				writer.write("best clusters:\n");
				for (GeneClusterOccurrence occ : cluster.getOccurrences()) {
					writeClusterOccurrence(occ, writer);
				}
				writer.write("\n");
				writer.write("all clusters:\n");
				for (GeneClusterOccurrence occ : cluster.getAllOccurrences()) {
					writeClusterOccurrence(occ, writer);
				}
			}
			
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static String geneContentString(int[] geneContent) {
		StringBuilder builder = new StringBuilder(geneContent.length * 2);
		for (int gene : geneContent)
			builder.append(gene).append(" ");
		return builder.toString();
	}
	
	private static void writeClusterOccurrence(GeneClusterOccurrence occ, Writer writer) throws IOException {
		writer.write(String.format("next ACI: pValue: %e, total dist: %d, support: %d, number of refOccs: %d\n",
				occ.getBestpValue(),
				occ.getTotalDist(),
				occ.getSupport(),
				0)); //TODO 0
		for (int i=0; i<occ.getSubsequences().length; i++) {
			for (Subsequence subsequence : occ.getSubsequences()[i]) {
				writer.write(String.format("\t\tgenome %d, chr %d: [%d,%d] dist: %d, pValue: %e%s\n",
						i,
						subsequence.getChromosome(),
						subsequence.getStart(),
						subsequence.getStop(),
						subsequence.getDist(),
						subsequence.getpValue(),
						subsequence.getDist() == 0 ? " RefOCC" : ""));
			}			
		}
		writer.write("finished\n");
	}
	
	public static boolean exportResultsToFile(File f, List<GeneCluster> clusters, int[] geneLabelMap) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
			for (GeneCluster c : clusters) {
				for (GeneClusterOccurrence occ : c.getAllOccurrences()) {
					/*
					 * Write the columns <ClusterId> <OccurrenceID> <Score> <totalDist>
					 */
					fw.write(Integer.toString(c.getId())+
							'\t'+
							occ.getId()+
							'\t'+
							occ.getBestScore()+
							"\t"+
							occ.getTotalDist()+
							"\t");
					/*
					 * Write the column <GeneSet>
					 */
					for (int i=0; i<c.getGenes().length; i++) {
						fw.write(Integer.toString(geneLabelMap[c.getGenes()[i]]));
						if (i<c.getGenes().length-1) fw.write(";");
					}
					/*
					 * Write the start-end lists for each genome
					 */
					fw.write("\t");
					for (int s=0;s<occ.getSubsequences().length;s++) {
						Subsequence[] sub = occ.getSubsequences()[s];
						if (sub!=null && sub.length!=0) 
							for (int i=0; i<sub.length; i++) {
								fw.write(sub[i].getChromosome()+ ":" + sub[i].getStart()+"-"+sub[i].getStop());
								if (i<sub.length-1) fw.write(";");
							}	
						if (s<occ.getSubsequences().length-1)
							fw.write("\t");
					}
					fw.write("\n");
				}
			}
			fw.flush();
			fw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		
	}
}
