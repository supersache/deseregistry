package de.cw.deseregistry.events;

/**
 *  Dieses Event bildet die Kante Class <-> implemented Interface ab
 *
 */
public class AddExtendsEvent extends Event {

	private Class<?> classBeingExtended;
	private AddClassEvent childClassEvent;


	/**
	 * Stores an edge like String extends Object.
	 * 
	 * @param clazz The super class that is being extended, Object in the example above
	 * @param ev The AddClassEvent that was fired to add the String class in the example above
	 */
	public AddExtendsEvent (Class<?> clazz, AddClassEvent ev) { this.classBeingExtended = clazz; this.childClassEvent = ev; }


	public Class<?> getParentClass () {
		return classBeingExtended;
	}


	public AddClassEvent getChildClassEvent() {
		return childClassEvent;
	}


}