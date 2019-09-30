package de.cw.deseregistry.events;

public class AddClassEvent extends Event {
	
	private Class<?> addedClass;
	/**
	 *  Primary key in the DB
	 */
	private int      class_pk;
	
	public AddClassEvent (Class<?> clazz) { this.addedClass = clazz; }
	
	public int getClass_pk() {
		return class_pk;
	}

	public void setClass_pk(int class_pk) {
		this.class_pk = class_pk;
	}

	public Class<?> getEventClass () { return this.addedClass; }
	
	
	
}
