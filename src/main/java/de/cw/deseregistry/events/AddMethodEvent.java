package de.cw.deseregistry.events;

import java.lang.reflect.Method;

/**
 * Diese Klasse bildet die Kante Class <-> implementierte Methode ab.
 * 
 */
public class AddMethodEvent extends Event {

	private Method addedMethod;
	private Class<?> implementingClass;

	public AddMethodEvent(Method m, Class<?> implementingClass) {
		this.addedMethod = m;
		this.implementingClass = implementingClass;
	}

	public Method getMethod() {
		return this.addedMethod;
	}

	public Class<?> getImplementingClass() {
		return this.implementingClass;
	}
}
