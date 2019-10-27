package de.cw.deseregistry.events;

public class AddExtenzEvent extends Event
{
	private String clz;
	private String superClz;
	
	public AddExtenzEvent (String clz, String superClz)
	{
		this.clz = clz;
		this.superClz = superClz;
	}

	public String getClz() {
		return clz;
	}

	public String getSuperClz() {
		return superClz;
	}
	
	
}
