package de.cw.deseregistry.events;

import java.util.HashSet;
import java.util.Set;

/**
 *  Diese Klasse bildet einen Knoten (Klasse ohne Parentklasse) oder eine Kante (Klasse und ParentKlasse), ab
 *  beide sind ein eintrag in der CLASSES Tabelle.
 * @author kai
 *
 */
public class AddClassEvent extends Event {
	
	private int access;
	private String name;
	private String superClass;
	private Set<String> interfaces;
	
	public AddClassEvent (int access, String name, String superClass, String [] interfaces)
	{
		this.access = access;
		this.name = name;
		this.superClass = superClass;
		this.interfaces = new HashSet<String> ();
		for (String if_ : interfaces) {
			this.interfaces.add (if_);
		}
	}
	
	public int getAccess ()
	{
		return access;
	}

	public String getName() {
		return name;
	}

	public String getSuperClass() {
		return superClass;
	}

	public Set<String> getInterfaces()
	{
		return interfaces;
	}
}
