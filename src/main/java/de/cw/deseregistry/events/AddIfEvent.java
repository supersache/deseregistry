package de.cw.deseregistry.events;

public class AddIfEvent extends Event {

	private Class<?> addedInterface;
	/**
	 *  primary key of the implementing class
	 */
	private int class_pk;
	
	public int getClass_pk() {
		return class_pk;
	}

	public void setClass_pk(int class_pk) {
		this.class_pk = class_pk;
	}

	public AddIfEvent (Class<?> clazz) { this.addedInterface = clazz; }
	
	public Class<?> getInterface () { return this.addedInterface; }
}
