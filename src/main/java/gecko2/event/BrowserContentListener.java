package gecko2.event;

import java.util.EventListener;

public interface BrowserContentListener extends EventListener {
	void browserContentChanged(BrowserContentEvent e);
}

