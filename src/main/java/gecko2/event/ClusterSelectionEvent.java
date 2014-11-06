package gecko2.event;

import gecko2.datastructures.GeneCluster;

import java.util.EventObject;

public class ClusterSelectionEvent extends EventObject {

	/**
	 * Random generated serialization UID
	 */
	private static final long serialVersionUID = -9184708680298812763L;
	private final GeneCluster selection;
	private final boolean instantDisplay;
	
	public ClusterSelectionEvent(Object source, GeneCluster selection, boolean instantDisplay) {
		super(source);
		this.selection = selection;
		this.instantDisplay = instantDisplay;
	}
	
	public ClusterSelectionEvent(Object source, GeneCluster selection) {
		this(source,selection,false);
	}

	public boolean isInstantDisplayEnabled() {
		return instantDisplay;
	}
	
	public GeneCluster getSelection() {
		return selection;
	}

}
