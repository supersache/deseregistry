package de.cw.deseregistry.events;

/**
 *  Dieses Event bildet die Kante Class <-> implemented Interface ab
 *
 */
public class AddImplementsEvent extends Event {

	private Class<?> addedInterface;
	/**
	 *  primary key of the implementing class
	 */
	private AddClassEvent parent;


	public AddImplementsEvent (Class<?> clazz, AddClassEvent parent) { this.addedInterface = clazz; this.parent = parent; }
	
	public AddClassEvent getParent() {
		return parent;
	}

	public Class<?> getInterface () { return this.addedInterface; }
}
