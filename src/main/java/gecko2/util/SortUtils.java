package gecko2.util;

import gecko2.GenomeOccurence;

import java.io.Serializable;
import java.util.*;


public class SortUtils {
	
	public static void resortGenomeOccurences(List<GenomeOccurence> occs) {
		Collections.sort(occs, new GenomeOccurenceComparator());
	}
	
	public static void resortGenomeOccurencesByStart(List<GenomeOccurence> occs) {
		Collections.sort(occs, new GenomeOccurenceStartComparator());
	}
	
	public static Map<String[], Integer> invertIntArray(Map<Integer, String[]> orig) {
		Map<String[], Integer> result = new HashMap<String[], Integer>();
		for (int i = 0; i < orig.size() ; i++)
			result.put(orig.get(i), i);
		return result;		
	}
	
}

class GenomeOccurenceComparator implements Comparator<GenomeOccurence>, Serializable {
	public int compare(GenomeOccurence o1, GenomeOccurence o2) {
		if (o1.getGroup() == o2.getGroup())
			return o1.getDesc().compareTo(o2.getDesc());
		else
			return (int) -Math.signum(o1.getGroup()-o2.getGroup());
	}
}

class GenomeOccurenceStartComparator implements Comparator<GenomeOccurence>, Serializable {
	public int compare(GenomeOccurence o1, GenomeOccurence o2) {
		if (o1.getStart_line()<o2.getStart_line())
			return -1;
		if (o1.getStart_line()==o2.getStart_line())
			return 0;
		return 1;
	}
}
