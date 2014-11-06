package gecko2.event;

import gecko2.datastructures.GeneCluster;
import gecko2.datastructures.GeneClusterOccurrence;

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
	private final GeneClusterOccurrence gOcc;
	
	public LocationSelectionEvent(Object source, GeneCluster gc, GeneClusterOccurrence gOcc, int[] subselection, boolean instant) {
		super(source, gc, instant);
		this.gOcc = gOcc;
		this.subselection = subselection;
	}
	
	public LocationSelectionEvent(Object source, GeneCluster gc, GeneClusterOccurrence gOcc, int[] subselection) {
		this(source,gc,gOcc,subselection,false);
	}
	
	public int[] getsubselection() {
		return subselection;
	}
	
	public GeneClusterOccurrence getgOcc() {
		return gOcc;
	}

}
