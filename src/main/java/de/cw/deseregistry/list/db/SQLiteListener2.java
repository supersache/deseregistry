package de.cw.deseregistry.list.db;

import java.lang.reflect.Method;
import java.sql.SQLException;

import de.cw.deseregistry.errorhandling.FatalException;
import de.cw.deseregistry.events.AddClzEvent;
import de.cw.deseregistry.events.AddExtendsEvent;
import de.cw.deseregistry.events.AddExtenzEvent;
import de.cw.deseregistry.events.AddImplemenzEvent;
import de.cw.deseregistry.events.AddMethodEvent;
import de.cw.deseregistry.events.ConfirmIfEvent;
import de.cw.deseregistry.events.Event;
import de.cw.deseregistry.events.ExceptionEvent;
import de.cw.deseregistry.main.Listener;
import de.cw.deseregistry.utils.Utils;

public class SQLiteListener2 implements Listener {
	
	private SQLiteDriver driv = null;
	
	public SQLiteListener2 ()
	{
		driv = new SQLiteDriver ();
	}

	@Override
	public void notify(Event e) {
		if (e instanceof AddClzEvent) {
			AddClzEvent ace = (AddClzEvent) e;
			String clzName = ace.getClassName();
			String jarLocation = ace.getJarLocation();
			try {
				driv.insertIntoClass(
						clzName, 
						jarLocation,
						false);
			}
			catch (SQLException e1) {
				e1.printStackTrace(System.err);
				throw new FatalException ("notify AddClassEvent failed.");
			}
		}
		else if (e instanceof ConfirmIfEvent) {
			ConfirmIfEvent cie = (ConfirmIfEvent) e;
			String clzName = cie.getClassName();
			
			int pk = driv.getPKForClass(clzName);
			
			try {
				driv.updateIfProperty(pk);
			}
			catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace(System.err);
				throw new FatalException ("notify ConfirmIfEvent failed.");
			}			
		}
		else if (e instanceof AddImplemenzEvent) {
			AddImplemenzEvent aie = (AddImplemenzEvent) e;
			
			int pkImplClass = 0;
			int pkIf = 0;
			
			String clz = aie.getClz ();
			String if_ = aie.getInterf ();
			
			if (clz == null || if_ == null) {
				throw new FatalException("Class not found in cache: " + clz + " " + if_);
			}
			
			pkImplClass = driv.getPKForClass (clz);
			pkIf = driv.getPKForClass        (if_);
			
			try {
				driv.insertIntoImplements (pkImplClass, pkIf);
			}
			catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace(System.err);
				throw new FatalException ("notify AddImplementsEvent failed.");
			}
		}
		else if (e instanceof AddExtenzEvent) {
			AddExtenzEvent aee = (AddExtenzEvent) e;
			
			int pkParent = driv.getPKForClass (aee.getClz ());
			int pkChild = driv.getPKForClass (aee.getSuperClz ());
			
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
