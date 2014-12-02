package de.unijena.bioinf.gecko3.event;

import java.util.EventListener;

public interface DataListener extends EventListener {
    /**
     * This method is called when the data, i.e. the genomes or clusters currently
     * observed in this session are changed.
     *
     * @param e The data event that references the {@link de.unijena.bioinf.gecko3.GeckoInstance} object
     *          handled the data update
     */
	public void dataChanged(DataEvent e);
}