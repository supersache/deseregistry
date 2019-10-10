package de.cw.deseregistry.list.db;

import java.sql.SQLException;

import de.cw.deseregistry.driver.Listener;
import de.cw.deseregistry.events.AddClassEvent;
import de.cw.deseregistry.events.AddIfEvent;
import de.cw.deseregistry.events.AddMethodEvent;
import de.cw.deseregistry.events.Event;
import de.cw.deseregistry.events.ExceptionEvent;
import de.cw.deseregistry.utils.Utils;

public class SQLiteListener implements Listener {
	
	private SQLiteDriver driv = null;
	
	public SQLiteListener ()
	{
		driv = new SQLiteDriver ();
	}

	@Override
	public void notify(Event e) {
		if (e instanceof AddClassEvent) {
			AddClassEvent ace = (AddClassEvent) e;
			Class<?> zeClass = ace.getEventClass();
			Class<?> suClass = ace.getSuperClass();
			
			String jarLocation = null;
			if (zeClass.getProtectionDomain().getCodeSource() != null) {
				jarLocation = zeClass.getProtectionDomain().getCodeSource().getLocation().getPath();
			}
			try {
				if (suClass != null) {
					// super class sollte schon in der DB sein...
					Integer pk = driv.getPKForClass(suClass);
					if (pk == null) {
						System.out.println("WARNING !!!! Notifying " + zeClass.getCanonicalName() + " but " + suClass.getCanonicalName() + " not in cache!!!");
						return;
					}
					driv.insertIntoClass(
							zeClass.getName (), 
							jarLocation,
							pk.intValue(), zeClass.isInterface());
				}
				else {
					driv.insertIntoClass(
							zeClass.getName(), 
							jarLocation,
							zeClass.isInterface());				
				}
			}
			catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
		}
		else if (e instanceof AddIfEvent) {
			AddIfEvent aie = (AddIfEvent) e;
			Class<?> implementingClass = aie.getParent().getEventClass();
			
			int pkImplClass = driv.getPKForClass(implementingClass);
			int pkIf = driv.getPKForClass(aie.getInterface());
			
			try {
				driv.insertIntoImplIf (pkImplClass, pkIf);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else if (e instanceof AddMethodEvent) {
		}
		else if (e instanceof ExceptionEvent) {
			((ExceptionEvent)e).exc.printStackTrace();
		}
	}

}
