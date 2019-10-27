package de.cw.deseregistry.events;

public class AddClzEvent extends Event
{
	private String clz;
	private String jarLocation;
	
	public String getJarLocation() {
		return jarLocation;
	}

	public void setJarLocation(String jarLocation) {
		this.jarLocation = jarLocation;
	}

	public AddClzEvent (String clzName)
	{
		this.clz = clzName;
	}
	
	public String getClassName () { return clz; }
}
