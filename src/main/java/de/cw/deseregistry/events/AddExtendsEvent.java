package de.cw.deseregistry.events;

/**
 *  Dieses Event bildet die Kante Class <-> implemented Interface ab
 *
 */
public class AddExtendsEvent extends Event {

	private Class<?> classBeingExtended;
	private AddClassEvent childClassEvent;


	public AddExtendsEvent (Class<?> clazz, AddClassEvent ev) { this.classBeingExtended = clazz; this.childClassEvent = ev; }


	public Class<?> getParentClass () {
		return classBeingExtended;
	}


	public AddClassEvent getChildClassEvent() {
		return childClassEvent;
	}


}