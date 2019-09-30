package de.cw.deseregistry.driver;

import de.cw.deseregistry.events.Event;

public interface Listener {
	public void notify (Event e);
}
