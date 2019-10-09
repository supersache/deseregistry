package de.cw.deseregistry.events;

/**
 *  Dieses Event bildet die Kante Class <-> implemented Interface ab
 *
 */
public class AddIfEvent extends Event {

	private Class<?> addedInterface;
	/**
	 *  primary key of the implementing class
	 */
	private Event parent;


	public AddIfEvent (Class<?> clazz, Event parent) { this.addedInterface = clazz; this.parent = parent; }
	
	public Event getParent() {
		return parent;
	}

	public Class<?> getInterface () { return this.addedInterface; }
}
