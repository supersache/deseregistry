package de.cw.deseregistry.events;

import java.lang.reflect.Method;

/**
 * Diese Klasse bildet die Kante Class <-> implementierte Methode ab.
 * 
 */
public class AddMethodEvent extends AddFieldEvent
{
	private String [] exceptions;
	
	public AddMethodEvent (int access, String name, String signature, String [] exceptions)
	{
		super (access, name, signature);
		this.exceptions = exceptions;
	}

	public String[] getExceptions() {
		return exceptions;
	}
}
