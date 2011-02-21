package jgecko.geneSequence.view.browser.event;

import jgecko.geneSequence.view.browser.MultipleGenomesBrowser;

import java.util.EventObject;

/**
 * @author Leon Kuchenbecker <lkuchenb@inf.fu-berlin.de>
 */
public class BrowserContentEvent extends EventObject{
    public enum EventType {scrollValueChanged, zoomFactorChanged}

	private final EventType eventType;
	private static final long serialVersionUID = 1382632021469547584L;

	public BrowserContentEvent(MultipleGenomesBrowser source, EventType eventType) {
		super(source);
		this.eventType = eventType;
	}

	public EventType getEventType() {
		return eventType;
	}

	@Override
	public MultipleGenomesBrowser getSource() {
		return (MultipleGenomesBrowser) super.getSource();
	}
}
