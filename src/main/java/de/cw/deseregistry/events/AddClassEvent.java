package de.cw.deseregistry.events;

/**
 *  Diese Klasse bildet einen Knoten (Klasse ohne Parentklasse) oder eine Kante (Klasse und ParentKlasse), ab
 *  beide sind ein eintrag in der CLASSES Tabelle.
 * @author kai
 *
 */
public class AddClassEvent extends Event {
	
	private Class<?> addedClass;
	
	public AddClassEvent (Class<?> clazz) { this.addedClass = clazz; }

	public Class<?> getEventClass () { return this.addedClass; }
	
	
}
