package jgecko.geneSequence.view.browser.event;

import java.util.EventListener;

/**
 * The listener interface for receiving events when the content of a {@link jgecko.geneSequence.view.browser.GenomeBrowser} changes.
 * @author Leon Kuchenbecker <lkuchenb@inf.fu-berlin.de>
 */
public interface BrowserContentListener extends EventListener {
    void browserContentChanged(BrowserContentEvent e);
}
