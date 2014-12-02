package de.unijena.bioinf.gecko3.util;

import de.unijena.bioinf.gecko3.io.GenomeOccurrence;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SortUtils {
	
	public static void resortGenomeOccurences(List<GenomeOccurrence> occs) {
		Collections.sort(occs, new GenomeOccurenceComparator());
	}
	
	public static void resortGenomeOccurencesByStart(List<GenomeOccurrence> occs) {
		Collections.sort(occs, new GenomeOccurenceStartComparator());
	}

}

class GenomeOccurenceComparator implements Comparator<GenomeOccurrence>, Serializable {
	public int compare(GenomeOccurrence o1, GenomeOccurrence o2) {
		if (o1.getGroup() == o2.getGroup())
			return o1.getDesc().compareTo(o2.getDesc());
		else
			return (int) -Math.signum(o1.getGroup()-o2.getGroup());
	}
}

class GenomeOccurenceStartComparator implements Comparator<GenomeOccurrence>, Serializable {
	public int compare(GenomeOccurrence o1, GenomeOccurrence o2) {
		if (o1.getStart_line()<o2.getStart_line())
			return -1;
		if (o1.getStart_line()==o2.getStart_line())
			return 0;
		return 1;
	}
}
