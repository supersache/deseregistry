package de.cw.deseregistry.events;

public class ProgressBarListener
{
	private int totalNumClasses;
	private int classesVisited;
	private int totalNotifications;

	public ProgressBarListener ()
	{
		this.totalNumClasses = Integer.parseInt (
				System.getProperty ("TOTAL_NUMBER_CLASSES"));
		this.classesVisited = 0;
		this.totalNotifications = 0;
	}
	
//	@Override
//	public void notify(Event e) {
//		if (e instanceof AddClzEvent) {
//			this.classesVisited ++;
//		}
//		if (this.totalNotifications++ % 10 == 0) {
//			NumberFormat nf = NumberFormat.getInstance (Locale.GERMAN);
//			nf.setMinimumFractionDigits(2);
//			nf.setMaximumFractionDigits(2);
//			double percentage = (double)classesVisited * 100.0 / (double) totalNumClasses;
//			System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
//			System.out.print ("Progress: " + nf.format (percentage) + " out of " +
//					totalNumClasses);
//		}
//		
//		
//	}

	
}
