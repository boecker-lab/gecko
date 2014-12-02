package de.unijena.bioinf.gecko3.event;

import de.unijena.bioinf.gecko3.datastructures.GeneCluster;

/**
 * 
 * @author Leon Kuchenbecker <lkuchenb@inf.fu-berlin.de>
 *
 */
public class LocationSelectionEvent extends ClusterSelectionEvent {
	
	/**
	 * Random generated serialization UID
	 */
	private static final long serialVersionUID = -7045332477999665994L;
	
	private final int[] subselection;
	private final boolean includeSubOptimalOccurrences;
	
	public LocationSelectionEvent(Object source, GeneCluster gc, boolean includeSubOptimalOccurrences, int[] subselection, boolean instant) {
		super(source, gc, instant);
		this.includeSubOptimalOccurrences = includeSubOptimalOccurrences;
		this.subselection = subselection;
	}
	
	public LocationSelectionEvent(Object source, GeneCluster gc, boolean includeSubOptimalOccurrences, int[] subselection) {
		this(source,gc,includeSubOptimalOccurrences,subselection,false);
	}
	
	public int[] getsubselection() {
		return subselection;
	}
	
	public boolean includeSubOptimalOccurrences() {
		return includeSubOptimalOccurrences;
	}

}
