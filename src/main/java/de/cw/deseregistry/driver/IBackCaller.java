package de.cw.deseregistry.driver;

public interface IBackCaller
{
	public void notify (NotificationType type, Object details);
	public Object getParam (String pName);
	public boolean classExists (String className);
	
	public static enum NotificationType {
		CLASS_PROCESSED,
		CLASS_INSERTED,
		CLASS_UPDATED
	};
}
