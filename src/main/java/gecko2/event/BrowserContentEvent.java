package gecko2.event;

import gecko2.gui.MultipleGenomesBrowserInterface;

import java.util.EventObject;

public class BrowserContentEvent extends EventObject {
	
	public static final short SCROLL_VALUE_CHANGED = 1;
	public static final short ZOOM_FACTOR_CHANGED = 2;
	
	private final short eventType;
	private static final long serialVersionUID = 1382632021469547584L;

	public BrowserContentEvent(MultipleGenomesBrowserInterface source, short eventType) {
		super(source);
		this.eventType = eventType;
	}
	
	public short getEventType() {
		return eventType;
	}
	
	@Override
	public MultipleGenomesBrowserInterface getSource() {
		return (MultipleGenomesBrowserInterface) super.getSource();
	}
}

