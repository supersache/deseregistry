package de.cw.deseregistry.list.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class SQLiteDriver
{
	private Connection conn = null;
	private Map<String,Integer> classPkMap = new HashMap<String,Integer> ();	
	
	public SQLiteDriver ()
	{
		String dbfile = System.getProperty("SQLITEDBFILE");
		if (dbfile == null) {
			throw new RuntimeException ("System Property SQLITEDBFILE empty, cannot load SQLiteDriver...");
		}
		
		try {
			conn = DriverManager.getConnection ("jdbc:sqlite:" + dbfile);
		} catch (SQLException e) {
			throw new RuntimeException (e);
		}
	}
	
	public void insertIntoClass (String name, String pkg, String jar, int pkSuperClass, boolean isInterface)
			throws SQLException
	{
		final String sql = "INSERT TO CLASS (NAME,PACKAGE,JAR,SUPER,IS_INF) VALUES (?,?,?,?,?)";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString	(1, name);
			stmt.setString 	(2, pkg);
			stmt.setString	(3, jar);
			stmt.setInt		(4, pkSuperClass);
			stmt.setInt		(5, isInterface?1:0);
			
			stmt.execute ();
			
			/**
			 *  wenn wir eine Klasse einfügen
			 *  könnte es sein dass wir den generierten
			 *  PK brauchen weil wir noch die Kindklasse
			 *  eintragen wollen im nächsten Schritt
			 */
			ResultSet st = stmt.getGeneratedKeys();
			while (st.next()) {
				int key = st.getInt (1);
				classPkMap.put(pkg + "." + name, key);
			}
		}
	}

	
	public void insertIntoClass (String name, String pkg, String jar, boolean isInterface)
			throws SQLException
	{
		final String sql = "INSERT TO CLASS (NAME,PACKAGE,JAR,IS_INF) VALUES (?,?,?,?)";
		
		try (PreparedStatement stmt = conn.prepareStatement (sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString	(1, name);
			stmt.setString 	(2, pkg);
			stmt.setString	(3, jar);
			stmt.setInt		(5, isInterface?1:0);
			
			stmt.execute ();
			
			/**
			 *  wenn wir eine Klasse einfügen
			 *  könnte es sein dass wir den generierten
			 *  PK brauchen weil wir noch die Kindklasse
			 *  eintragen wollen im nächsten Schritt
			 */
			ResultSet st = stmt.getGeneratedKeys ();
			while (st.next ()) {
				int key = st.getInt (1);
				classPkMap.put (pkg + "." + name, key);
			}
		}
	}
	
	public void insertIntoImplIf (int pkClass, int pkInf)
			throws SQLException
	{
		final String sql = "INSERT TO IMPL_IF (CLASS,IMPL_INF) VALUES (?,?)";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt		(1, pkClass);
			stmt.setInt		(2, pkInf);			
			stmt.execute ();
		}
	}
}
