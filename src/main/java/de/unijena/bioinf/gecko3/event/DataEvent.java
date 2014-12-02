package de.unijena.bioinf.gecko3.event;

import de.unijena.bioinf.gecko3.GeckoInstance;

import java.util.EventObject;

public class DataEvent extends EventObject {

	private static final long serialVersionUID = 2868748485667954767L;

	public DataEvent(GeckoInstance source) {
		super(source);
	}
	
	@Override
	public GeckoInstance getSource() {
		return (GeckoInstance) super.getSource();
	}
	
}
