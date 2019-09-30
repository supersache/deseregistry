package de.cw.deseregistry.events;

import java.lang.reflect.Method;

public class AddMethodEvent extends Event {

	private Method addedMethod;
	public AddMethodEvent (Method m) { this.addedMethod = m; }
	
	public Method getMethod () { return this.addedMethod; }	
	
}
