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
	
	public Integer getPKForClass (Class<?> clz)
	{
		String classname = clz.getName();
		return classPkMap.get (classname);
	}
	
	public Integer getPKForClass (String clz)
	{
		return classPkMap.get (clz);
	}
	
	public void insertIntoClass (String name, String jar, boolean isInterface)
			throws SQLException
	{
		final String sql = "INSERT INTO CLASSES (NAME,JAR,IS_INF) VALUES (?,?,?)";
		
		try (PreparedStatement stmt = conn.prepareStatement (sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString	(1, name);
			stmt.setString	(2, jar);
			stmt.setInt		(3, isInterface?1:0);
			
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
				classPkMap.put (name, key);
			}
		}
	}
	
	/**
	 * Adds a new relationship between implementing clazz and interface (clazz implements interface)
	 * @param pkClass implementing class
	 * @param pkInf interface being implemented
	 * @throws SQLException
	 */
	public void insertIntoImplements (int pkClass, int pkInf)
		throws SQLException
	{
		final String sql = "INSERT INTO IMPLEMENTS (CLASS,IMPL_INF) VALUES (?,?)";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt		(1, pkClass);
			stmt.setInt		(2, pkInf);			
			stmt.execute    ();
		}
	}
	

	/**
	 *  Adds a new relationship between class and superclass (clazz extends superclass)
	 * @param pkClass clazz
	 * @param pkSuper parent class (superclass)
	 * @throws SQLException
	 */
	public void insertIntoExtends (int pkClass, int pkSuper)
		throws SQLException
	{
		final String sql = "INSERT INTO EXTENDS (CLASS,SUPER) VALUES (?,?)";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt		(1, pkClass);
			stmt.setInt		(2, pkSuper);			
			stmt.execute    ();
		}
	}
	
	/**
	 * Inserts an entry into the hybrid table METHOD
	 * @param pkDeclClass Class that declares the method. E.g. hashCode() is declared 
	 *                    by java.lang.Object.
	 * @param pkClass Class that implements the method
	 * @param decorator public,protected,private,package
	 * @param finasta  bitmap: final, native, static
	 * @param signature function signature
	 * @throws SQLException
	 */
	public void insertIntoMethod (int pkDeclClass, int modifier, String signature)
		throws SQLException
	{
		final String sql = "INSERT INTO METHOD (DECL_CLASS,MODIFIER, SIGNATURE) VALUES (?,?,?)";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt		(1, pkDeclClass);
			stmt.setString  (3, signature);
			stmt.setInt     (2, modifier);
			stmt.execute    ();
		}
	}
	
	public void insertIntoOverrides (int pkClass, int pkMethod)
		throws SQLException
	{
		final String sql = "INSERT INTO OVERRIDES (OVERRIDING_CLASS, OVERRIDDEN_METHOD) VALUES (?, ?)";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt		(1, pkClass);
			stmt.setInt     (2, pkMethod);
			stmt.execute    ();
		}
	}
}
