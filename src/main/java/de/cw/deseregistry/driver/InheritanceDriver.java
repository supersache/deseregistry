package de.cw.deseregistry.driver;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.cw.deseregistry.list.db.SQLiteDriver;

public class InheritanceDriver implements IDriver {
	
	private static Logger LOGGER = Logger.getLogger(InheritanceDriver.class.getName());

	private IBackCaller cb = null;
	private SQLiteDriver sqld = null;
	
	public InheritanceDriver (boolean wirdignoriert) {
		this.sqld = SQLiteDriver.getInstance();
	}
	
	@Override
	public void addJarToProcess(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBackCaller(IBackCaller vif) {
		this.cb = vif;
	}

	@Override
	public void go() throws IOException {
		
		/**
		 * 
		           clzname,parentclassname,serializable,invocationhandler
		Child (a) ==>  com/sap/engine/services/rmi_p4/reflect/P4InvocationHandler,com/sap/.../reflect/AbstractInvocationHandler,1,0
		Parent(b) =>  com/sap/engine/services/rmi_p4/reflect/AbstractInvocationHandler,java/lang/Object,0,1
		
		Ziel ist hier alle diese Konstellationen abzufangen.
		Vaterklasse ist hier invocationHandler aber das steht nach dem ersten LAuf noch nicht in der DB
		
		Query wäre hier
		
		select child.clzName from CLASSES child, CLASSES parent on child.parentClazzName = parent.clzName where
		    parent.invocationHandler = 1 and child.invocationHandler = 0
		    
		und admit updateQuery:
			
		update CLASSES set invocationHandler=1 where clzName in (... query oben)
		
		und das ganze für serializabe, comparable und invocationHandler
		
		 */
		System.out.println("[+] Updating invocationHandler inheritance");
		try {
			int invHdl = sqld.performInheritanceUpdateQuery("invocationHandler");
			System.out.println("[+] " + invHdl + " rows updated with invocationHandler = 1.");
		}
		catch (SQLException e) {
			LOGGER.log (Level.SEVERE, "SQL Exception when updating invocationHandler inheritance", e);
			System.out.println("[-] Error :-( See logs for more details.");
		}

		System.out.println("[+] Updating serializable inheritance");
		try {
			int invHdl = sqld.performInheritanceUpdateQuery("serializable");
			System.out.println("[+] " + invHdl + " rows updated with serializable = 1.");
		}
		catch (SQLException e) {
			LOGGER.log (Level.SEVERE, "SQL Exception when updating serializable inheritance", e);
			System.out.println("[-] Error :-( See logs for more details.");
		}

		System.out.println("[+] Updating comparable inheritance");
		try {
			int invHdl = sqld.performInheritanceUpdateQuery("comparable");
			System.out.println("[+] " + invHdl + " rows updated with comparable = 1.");
		}
		catch (SQLException e) {
			LOGGER.log (Level.SEVERE, "SQL Exception when updating comparable inheritance", e);
			System.out.println("[-] Error :-( See logs for more details.");
		}
	}
}
