package de.unijena.bioinf.gecko3.event;

import java.util.EventListener;

/**
 * 
 * @author Leon Kuchenbecker <lkuchenb@inf.fu-berlin.de>
 *
 */
public interface ClusterSelectionListener extends EventListener {
	
	void selectionChanged(ClusterSelectionEvent e);

}
