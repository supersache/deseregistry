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
	private AddClassEvent parent;


	/**
	 * 
	 * @param clazz class object representing the interface class
	 * @param parent Event object fired for the class that is implementing this interface
	 */
	public AddIfEvent (Class<?> clazz, AddClassEvent parent) { this.addedInterface = clazz; this.parent = parent; }
	
	public AddClassEvent getParent() {
		return parent;
	}

	public Class<?> getInterface () { return this.addedInterface; }
}
