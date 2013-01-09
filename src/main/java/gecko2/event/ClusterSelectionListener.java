package gecko2.event;

import java.util.EventListener;

/**
 * 
 * @author Leon Kuchenbecker <lkuchenb@inf.fu-berlin.de>
 *
 */
public interface ClusterSelectionListener extends EventListener {
	
	void selectionChanged(ClusterSelectionEvent e);

}
