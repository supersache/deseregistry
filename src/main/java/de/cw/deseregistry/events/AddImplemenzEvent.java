package de.cw.deseregistry.events;

public class AddImplemenzEvent extends Event
{
	private String clz;
	private String interf;
	
	public AddImplemenzEvent (String clz, String interf)
	{
		this.clz = clz;
		this.interf = interf;
	}

	public String getClz() {
		return clz;
	}

	public String getInterf() {
		return interf;
	}
}
