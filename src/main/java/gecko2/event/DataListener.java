package gecko2.event;

import gecko2.GeckoInstance;

import java.util.EventListener;

public interface DataListener extends EventListener {
    /**
     * This method is called when the data, i.e. the genomes or clusters currently
     * observed in this session are changed.
     *
     * @param e The data event that references the {@link gecko2.GeckoInstance} object
     *          handled the data update
     */
	public void dataChanged(DataEvent e);
}