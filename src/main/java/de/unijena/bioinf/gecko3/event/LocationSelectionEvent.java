/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
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

package de.unijena.bioinf.gecko3.event;

import de.unijena.bioinf.gecko3.datastructures.GeneCluster;
import de.unijena.bioinf.gecko3.datastructures.GeneFamily;

import java.util.Arrays;

/**
 * 
 * @author Leon Kuchenbecker
 *
 */
public class LocationSelectionEvent extends ClusterSelectionEvent {
	
	/**
	 * Random generated serialization UID
	 */
	private static final long serialVersionUID = -7045332477999665994L;
	
	private final int[] subselection;
	private final boolean includeSubOptimalOccurrences;
	private final GeneFamily alignmentGeneFamily;

	public LocationSelectionEvent(Object source, GeneCluster gc, boolean includeSubOptimalOccurrences, int[] subselection) {
		this(source, gc, includeSubOptimalOccurrences, subselection, null);
	}
	
	public LocationSelectionEvent(Object source, GeneCluster gc, boolean includeSubOptimalOccurrences, int[] subselection, GeneFamily alignmentGeneFamily) {
		super(source, gc);
		this.includeSubOptimalOccurrences = includeSubOptimalOccurrences;
		this.subselection = subselection;
		this.alignmentGeneFamily = alignmentGeneFamily;
	}
	
	public int[] getSubselection() {
		return subselection;
	}

	public GeneFamily getAlignmentGeneFamily() {
		return alignmentGeneFamily;
	}

	public boolean includeSubOptimalOccurrences() {
		return includeSubOptimalOccurrences;
	}

}
