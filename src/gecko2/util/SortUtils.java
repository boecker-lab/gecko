package gecko2.util;

import gecko2.GenomeOccurence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class SortUtils {
	
	public static void resortGenomeOccurences(ArrayList<GenomeOccurence> occs) {
		Collections.sort(occs, new GenomeOccurenceComparator());
	}
	
	public static void resortGenomeOccurencesByStart(ArrayList<GenomeOccurence> occs) {
		Collections.sort(occs, new GenomeOccurenceStartComparator());
	}
	
}

class GenomeOccurenceComparator implements Comparator<GenomeOccurence> {
	public int compare(GenomeOccurence o1, GenomeOccurence o2) {
		if (o1.getGroup() == o2.getGroup())
			return o1.getDesc().compareTo(o2.getDesc());
		else
			return (int) -Math.signum(o1.getGroup()-o2.getGroup());
	}
}

class GenomeOccurenceStartComparator implements Comparator<GenomeOccurence> {
	public int compare(GenomeOccurence o1, GenomeOccurence o2) {
		if (o1.getStart_line()<o2.getStart_line())
			return -1;
		if (o1.getStart_line()==o2.getStart_line())
			return 0;
		return 1;
	}
}
