package de.cw.deseregistry.events;

/**
 *  Diese Klasse bildet einen Knoten (Klasse ohne Parentklasse) oder eine Kante (Klasse und ParentKlasse), ab
 *  beide sind ein eintrag in der CLASSES Tabelle.
 * @author kai
 *
 */
public class AddClassEvent extends Event {
	
	private Class<?> addedClass;
	private Class<?> superClass;
	/**
	 *  Primary key in the DB
	 */
	private int      class_pk;
	
	public AddClassEvent (Class<?> clazz, Class<?> superClass) { this.addedClass = clazz; this.superClass = superClass; }
	
	public int getClass_pk() {
		return class_pk;
	}

	public void setClass_pk(int class_pk) {
		this.class_pk = class_pk;
	}

	public Class<?> getEventClass () { return this.addedClass; }
	public Class<?> getSuperClass () { return this.superClass; }
	
	
	
}
