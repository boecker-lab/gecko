package gecko2.event;

import gecko2.GeckoInstance;

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
