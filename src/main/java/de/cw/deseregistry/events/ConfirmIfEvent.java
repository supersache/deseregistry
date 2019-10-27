package de.cw.deseregistry.events;

public class ConfirmIfEvent extends Event
{	
	private String className;
	
	public String getClassName() {
		return className;
	}
	
	public ConfirmIfEvent (String className) { this.className = className; }
}
