package de.cw.deseregistry.driver;

public interface IBackCaller
{
	public void addVisitedClass (String className);
	public void notify (NotificationType type);
	
	public static enum NotificationType {
		CLASS_PROCESSED
	};
}
