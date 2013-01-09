package gecko2.io;

import gecko2.algorithm.Chromosome;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClusterAnnotationReader {
	public static boolean readClusterAnnotations(File f, Genome[] genomes, GeneCluster[] currentClusters) {
		Pattern clusterStartPattern = Pattern.compile("new cluster: pValue = ([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?), refSeq = (\\d+)");
		Pattern clusterOccurrencePattern = Pattern.compile("\\d+\\.?\\d*:\t([^\t]+)\t\\[(\\d+),(\\d+\\)])");
		
		Map<String, Genome> genomeMap = new HashMap<String,Genome>();
		Map<String, Chromosome> chromosomeMap = new HashMap<String,Chromosome>();
		for (Genome g : genomes) {
			for (Chromosome chr : g.getChromosomes()) {
				genomeMap.put(chr.getName(), g);
				chromosomeMap.put(chr.getName(), chr);
			}
		}
		
		List<GeneCluster> newClusters = new ArrayList<GeneCluster>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			boolean inCluster = false;
			GeneCluster newCluster = null;
			for (String line; (line = reader.readLine()) != null; ) {
				if (!inCluster) {
					Matcher m = clusterStartPattern.matcher(line);
					if (m.matches()) {
						inCluster = true;
						double pValue = Double.parseDouble(m.group(1));
						int refSeq = Integer.parseInt(m.group(2));
						newCluster = new GeneCluster(currentClusters.length + newClusters.size(), null, null, null, pValue, -1, refSeq, 'r');
					}
				} else {
					if (line.equals("in chromosomes:"))
						continue;
					Matcher m = clusterOccurrencePattern.matcher(line);
					if (m.matches()) {
						String genomeName = m.group(1);
						int occStart = Integer.parseInt(m.group(2));
						int occEnd = Integer.parseInt(m.group(3));
					} else {
						inCluster = false;
						newClusters.add(newCluster);
					}
				}
			}
			
		} catch (IOException e) {
		e.printStackTrace();
		return false;
		}
		return true;
	}
}
