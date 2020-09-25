package de.cw.deseregistry.events;

public class AddFieldEvent extends Event {
	private int access;
	private String name;
	private String signature;

	public AddFieldEvent(int access, String name, String signature)
	{
		this.access = access;
		this.name = name;
		this.signature = signature;
	}

	public int getAccess() {
		return access;
	}

	public String getName() {
		return name;
	}

	public String getSignature() {
		return signature;
	}
}