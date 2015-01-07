/*
 * Copyright 2014 Sascha Winter
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

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
