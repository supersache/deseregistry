package de.cw.deseregistry.events;

import java.lang.reflect.Method;

/**
 *  Diese Klasse bildet die Kante Class <-> implementierte Methode ab.
 *  
 */
public class AddMethodEvent extends Event {

	private Method addedMethod;
	public AddMethodEvent (Method m) { this.addedMethod = m; }
	
	public Method getMethod () { return this.addedMethod; }	
	
}
