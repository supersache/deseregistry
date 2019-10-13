package de.cw.deseregistry.list.db;

import java.lang.reflect.Method;
import java.sql.SQLException;

import de.cw.deseregistry.errorhandling.FatalException;
import de.cw.deseregistry.events.AddClassEvent;
import de.cw.deseregistry.events.AddExtendsEvent;
import de.cw.deseregistry.events.AddImplementsEvent;
import de.cw.deseregistry.events.AddMethodEvent;
import de.cw.deseregistry.events.Event;
import de.cw.deseregistry.events.ExceptionEvent;
import de.cw.deseregistry.main.Listener;
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
			
			String jarLocation = null;
			if (zeClass.getProtectionDomain().getCodeSource() != null) {
				jarLocation = zeClass.getProtectionDomain().getCodeSource().getLocation().getPath();
			}
			try {
				driv.insertIntoClass(
						zeClass.getName(), 
						jarLocation,
						zeClass.isInterface());				
				
			}
			catch (SQLException e1) {
				e1.printStackTrace(System.err);
				throw new FatalException ("notify AddClassEvent failed.");
			}
		}
		else if (e instanceof AddImplementsEvent) {
			AddImplementsEvent aie = (AddImplementsEvent) e;
			Class<?> implementingClass = aie.getParent().getEventClass();
			
			int pkImplClass = driv.getPKForClass(implementingClass);
			int pkIf = driv.getPKForClass(aie.getInterface());
			
			try {
				driv.insertIntoImplements (pkImplClass, pkIf);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace(System.err);
				throw new FatalException ("notify AddImplementsEvent failed.");
			}
		}
		else if (e instanceof AddExtendsEvent) {
			AddExtendsEvent aie = (AddExtendsEvent) e;
			Class<?> parentClass = aie.getParentClass();
			
			int pkParent = driv.getPKForClass(parentClass);
			int pkChild = driv.getPKForClass(aie.getChildClassEvent().getEventClass());
			
			try {
				driv.insertIntoExtends (pkChild, pkParent);
			} catch (SQLException e1) {
				e1.printStackTrace(System.err);
				throw new FatalException ("notify AddsExtendsEvent failed.");
			}
		}
		else if (e instanceof AddMethodEvent) {
			AddMethodEvent ame = (AddMethodEvent) e;
			Method m = ame.getMethod ();
			Class<?> clz = ame.getImplementingClass ();
			Class<?> declClz = m.getDeclaringClass ();
			
			String signature = Utils.signature(m, clz);
			int pkDeclClass = driv.getPKForClass (declClz);
			int pkImplClass = driv.getPKForClass (clz);
			int modifiers = m.getModifiers ();
			
			try {
				driv.insertIntoMethod(pkDeclClass, modifiers, signature);
				if (!clz.equals (declClz)) {
					// wenn die nicht gleich sind dann muss
					// clz eine (indirekte) Ableitung von declClz
					// sein
					if (!declClz.isAssignableFrom (clz)) {
						System.err.println ("Hier ist was nicht iO: " + declClz.getName() + " " + clz.getName ());
						throw new FatalException ("Illegel State encountered.");
					}
				}
			}
			catch (SQLException e2) {
				e2.printStackTrace(System.err);
				throw new FatalException ("notify AddsExtendsEvent failed.");				
			}
		}
		else if (e instanceof ExceptionEvent) {
			((ExceptionEvent)e).exc.printStackTrace();
		}
	}

}
