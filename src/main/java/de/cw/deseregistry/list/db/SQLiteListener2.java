package de.cw.deseregistry.list.db;

import java.util.logging.Logger;

public class SQLiteListener2 {
	
	private static Logger LOGGER = Logger.getLogger(SQLiteListener2.class.getName());
	private SQLiteDriver driv = null;
	
	public SQLiteListener2 ()
	{
		driv = new SQLiteDriver ();
	}

	//@Override
//	public void notify(Event e) {
//		if (e instanceof AddClzEvent) {
//			AddClzEvent ace = (AddClzEvent) e;
//			String clzName = ace.getClassName();
//			String jarLocation = ace.getJarLocation();
//			try {
//				driv.insertIntoClass(
//						clzName, 
//						jarLocation,
//						false);
//			}
//			catch (SQLException e1) {
//				LOGGER.log (Level.SEVERE, "SQLException during insertIntoClass for " + e, e1);
//				throw new FatalException ("SQLException during insertIntoClass for " + e);
//			}
//		}
//		else if (e instanceof ConfirmIfEvent) {
//			ConfirmIfEvent cie = (ConfirmIfEvent) e;
//			String clzName = cie.getClassName();
//			
//			Integer pk = driv.getPKForClass(clzName);
//			if (pk == null) {
//				throw new FatalException (clzName + " not found in cache for " + e);
//			}
//			
//			try {
//				driv.updateIfProperty(pk);
//			}
//			catch (SQLException e1) {
//				LOGGER.log (Level.SEVERE, "SQLException during updateIfProperty for " + e + " and pk=" + pk, e1);
//				throw new FatalException ("SQLException during updateIfProperty for " + e + " and pk=" + pk);
//			}			
//		}
//		else if (e instanceof AddImplemenzEvent) {
//			AddImplemenzEvent aie = (AddImplemenzEvent) e;
//			
//			int pkImplClass = 0;
//			int pkIf = 0;
//			
//			String clz = aie.getClz ();
//			String if_ = aie.getInterf ();
//			
//			if (clz == null || if_ == null) {
//				throw new FatalException("Class not found in cache: " + clz + " " + if_);
//			}
//			
//			pkImplClass = driv.getPKForClass (clz);
//			pkIf = driv.getPKForClass        (if_);
//			
//			try {
//				driv.insertIntoImplements (pkImplClass, pkIf);
//			}
//			catch (SQLException e1) {
//				LOGGER.log (Level.SEVERE, "SQLException during insertIntoImplements for " + e, e1);
//				throw new FatalException ("SQLException during insertIntoImplements for " + e);
//			}
//		}
//		else if (e instanceof AddExtenzEvent) {
//			AddExtenzEvent aee = (AddExtenzEvent) e;
//			
//			Integer pkParent = driv.getPKForClass (aee.getClz ());
//			Integer pkChild = driv.getPKForClass (aee.getSuperClz ());
//			
//			if (pkParent == null) {
//				throw new FatalException (aee.getClz () + " not found in cache for " + e);
//			}
//
//			if (pkChild == null) {
//				throw new FatalException (aee.getSuperClz () + " not found in cache for " + e);
//			}
//			
//			try {
//				driv.insertIntoExtends (pkChild, pkParent);
//			} catch (SQLException e1) {
//				LOGGER.log (Level.SEVERE, "SQLException during insertIntoExtends for " + e, e1);
//				throw new FatalException ("SQLException during insertIntoExtends for " + e);
//			}
//		}
//		else if (e instanceof AddMethodEvent) {
//			AddMethodEvent ame = (AddMethodEvent) e;
//			Method m = ame.getMethod ();
//			Class<?> clz = ame.getImplementingClass ();
//			Class<?> declClz = m.getDeclaringClass ();
//			
//			String signature = Utils.signature(m, clz);
//			int pkDeclClass = driv.getPKForClass (declClz);
//			int pkImplClass = driv.getPKForClass (clz);
//			int modifiers = m.getModifiers ();
//			
//			try {
//				driv.insertIntoMethod(pkDeclClass, modifiers, signature);
//				if (!clz.equals (declClz)) {
//					// wenn die nicht gleich sind dann muss
//					// clz eine (indirekte) Ableitung von declClz
//					// sein
//					if (!declClz.isAssignableFrom (clz)) {
//						LOGGER.log (Level.SEVERE, "Hier ist was nicht iO: " + declClz.getName() + " " + clz.getName ());
//						throw new FatalException ("Illegel State encountered.");
//					}
//				}
//			}
//			catch (SQLException e2) {
//				LOGGER.log (Level.SEVERE, "SQLException during insertIntoMethod for " + e, e2);
//				throw new FatalException ("SQLException during insertIntoMethod for " + e);				
//			}
//		}
//		else if (e instanceof ExceptionEvent) {
//			((ExceptionEvent)e).exc.printStackTrace();
//		}
//	}

}
