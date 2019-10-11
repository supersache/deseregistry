package de.cw.deseregistry.driver;

import de.cw.deseregistry.events.AddClassEvent;
import de.cw.deseregistry.events.AddImplementsEvent;
import de.cw.deseregistry.events.AddMethodEvent;
import de.cw.deseregistry.events.Event;
import de.cw.deseregistry.events.ExceptionEvent;
import de.cw.deseregistry.main.Listener;
import de.cw.deseregistry.utils.Utils;

public class ConsoleListener implements Listener {

	@Override
	public void notify (Event e) {
//		if (e instanceof AddClassEvent) {
//			AddClassEvent ace = (AddClassEvent) e;
//			if (ace.getEventClass().isInterface()) {
//				System.out.println ("Interface added => " + ace.getEventClass().getName() + " (" + ace.getSuperClass() + ")");				
//			}
//			else {
//				System.out.println ("Class added => " + ace.getEventClass().getName() + " (" + ace.getSuperClass() + ")");
//			}
//		}
//		else if (e instanceof AddImplementsEvent) {
//			AddImplementsEvent aie = (AddImplementsEvent) e;
//			AddClassEvent parent = (AddClassEvent) aie.getParent ();
//			
//			System.out.println ("Interface found for Class => " + aie.getInterface ().getName () + ", " + parent.getEventClass ());
//		}
//		else if (e instanceof AddMethodEvent) {
//			AddMethodEvent ame = (AddMethodEvent) e;
//			System.out.println ("Method found => " + Utils.signature (ame.getMethod()));
//		}
//		else if (e instanceof ExceptionEvent) {
//			((ExceptionEvent)e).exc.printStackTrace();
//		}
	}

}
