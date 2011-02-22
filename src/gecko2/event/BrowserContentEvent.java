package jgecko.geneSequence.view.browser.event;

import jgecko.geneSequence.view.browser.MultipleGenomesBrowser;

import java.util.EventObject;

/**
 * A event that indicates changes to a {@link jgecko.geneSequence.view.browser.GenomeBrowser}
 * @author Leon Kuchenbecker <lkuchenb@inf.fu-berlin.de>
 */
public class BrowserContentEvent extends EventObject{
    /**
     * The different types of {@link BrowserContentEvent}s
     */
    public enum EventType {
        /** the scroll value of a {@link jgecko.geneSequence.view.browser.GenomeBrowser} changed */
        scrollValueChanged,
        /** the zoom factor of a {@link jgecko.geneSequence.view.browser.GenomeBrowser} changed*/
        zoomFactorChanged
    }

	private final EventType eventType;
	private static final long serialVersionUID = 1382632021469547584L;

    /**
     * Constructs a new {@code BrowserContentEvent} with the specified {@link MultipleGenomesBrowser} source and an event type
     * @param source the {@link MultipleGenomesBrowser} that fired the event
     * @param eventType either {@code scrollValueChanged} or {@code zoomFactorChanged}
     */
	public BrowserContentEvent(MultipleGenomesBrowser source, EventType eventType) {
		super(source);
		this.eventType = eventType;
	}

    /**
     * Return the type of the event
     * @return the event type
     */
	public EventType getEventType() {
		return eventType;
	}

    /**
     * Returns the source of the event
     * @return the source {@link MultipleGenomesBrowser}
     */
	@Override
	public MultipleGenomesBrowser getSource() {
		return (MultipleGenomesBrowser) super.getSource();
	}
}
