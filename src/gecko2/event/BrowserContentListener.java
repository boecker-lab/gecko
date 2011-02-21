package jgecko.geneSequence.view.browser.event;

import java.util.EventListener;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public interface BrowserContentListener extends EventListener {
    void browserContentChanged(BrowserContentEvent e);
}
