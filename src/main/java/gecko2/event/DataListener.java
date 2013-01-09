package gecko2.event;

import gecko2.GeckoInstance;

import java.util.EventListener;

public interface DataListener extends EventListener {
	/**
	 * This method is called when the input data, i.e. the genomes currently
	 * observed in this session changed.
	 * @param e The data event that references the {@link GeckoInstance} object
	 * handled the data update
	 */
	public void dataChanged(DataEvent e);
}