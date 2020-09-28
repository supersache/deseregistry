package de.cw.deseregistry.list.db;

import static de.cw.deseregistry.utils.ClazzAnalysisResult.i1;
import static de.cw.deseregistry.utils.ClazzAnalysisResult.i2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cw.deseregistry.utils.ClazzAnalysisResult;

//Tabelle CLASSES wurde folgendermassen erzeugt:
//
/*
CREATE TABLE CLASSES(
clzName TEXT NOT NULL,
 parentClazzName TEXT NOT NULL,
 serializable INTEGER,
  probablySerializable INTEGER,
  invocationHandler INTEGER,
  comparable INTEGER,
  implementsReadObject INTEGER,
  implementsEquals INTEGER,
  implementsHashCode INTEGER,
  implementsCompareTo INTEGER,
  implementsFinalize INTEGER,
  PRIMARY KEY (clzName, parentClazzName));
			    */

public class SQLiteDriver
{
	private Connection conn = null;
	private Map<String,Integer> classPkMap = new HashMap<String,Integer> ();
	private static SQLiteDriver instance;
	
	private SQLiteDriver ()
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
	
	public static SQLiteDriver getInstance ()
	{
		if (instance == null)
			instance = new SQLiteDriver ();
		
		return instance;
	}
	
	public Integer getPKForClass (Class<?> clz) throws SQLException
	{
		String classname = clz.getName();
		if (classname == null) {
			final String sql = "select ID from CLASSES where NAME=?";

			try (PreparedStatement stmt = conn.prepareStatement (sql)) {
				stmt.setString	(1, classname);
				ResultSet rs = stmt.executeQuery();
				if (rs.getFetchSize()>=1) {
					return rs.getInt(0);
				}
			}
		}
		
		return classPkMap.get (classname);
	}
	
	public Integer getPKForClass (String clz)
	{
		return classPkMap.get (clz);
	}
	
	public void updateIfProperty (Integer pk) throws SQLException
	{
		final String sql = "UPDATE CLASSES SET IS_INF=1 where ID=?";
		
		try (PreparedStatement stmt = conn.prepareStatement (sql)) {
			stmt.setInt	(1, pk);
			stmt.executeUpdate ();
		}
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
	
	public void insertIntoClasses (ClazzAnalysisResult car) throws SQLException
	{
		final String sql = "INSERT INTO CLASSES (clzName, parentClazzName, serializable, probablySerializable, invocationHandler" +
					",comparable,implementsReadObject,implementsEquals,implementsHashCode,implementsCompareTo,implementsFinalize) "+
				    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		try (PreparedStatement stmt = conn.prepareStatement (sql)) {
			stmt.setString (1, car.getClzName());
			stmt.setString (2, car.getParentClazzName());
			stmt.setInt (3, i1(car.serializable));
			stmt.setInt (4, i1(car.probablySerializable));
			stmt.setInt (5, i1(car.invocationHandler));
			stmt.setInt (6, i1(car.comparable));
			stmt.setInt (7, i1(car.implementsReadObject));
			stmt.setInt (8, i1(car.implementsEquals));
			stmt.setInt (9, i1(car.implementsHashCode));
			stmt.setInt (10, i1(car.implementsCompareTo));
			stmt.setInt (11, i1(car.implementsFinalize));
			stmt.execute    ();
		}
	}

	public boolean existsClazzEntry (String clazzName) throws SQLException
	{
		final String sql = "SELECT count(*) from CLASSES where clzName = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement (sql)) {
			stmt.setString	(1, clazzName);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			int count = rs.getInt("count(*)");
			if (count > 1) {
				throw new DataIntegrityException("Too many (" + rs.getFetchSize() + ") hits for clazz " + clazzName);
			}
			
			return count == 1;
		}
	}
	
	public List<String> getAllClassNames () throws SQLException
	{
		final String sql = "select clzName from CLASSES";
		List<String> clzs = new ArrayList<String> ();
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next ()) {
				clzs.add (rs.getString (1));
			}
		}
		
		return clzs;
	}
	
	public void updateClazzInterfaces (ClazzAnalysisResult car) throws SQLException
	{
		final String sql = "UPDATE CLASSES set serializable=?, probablySerializable=?,invocationHandler=?,comparable=? " +
					       "where clzName = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement (sql)) {
			stmt.setInt (1, i1(car.serializable));
			stmt.setInt (2, i1(car.probablySerializable));
			stmt.setInt (3, i1(car.invocationHandler));
			stmt.setInt (4, i1(car.comparable));
			stmt.setString (5, car.getClzName());
			stmt.execute    ();
		}
	}

	public ClazzAnalysisResult[] getMyChildClasses(String myName) throws SQLException
	{
		final String sql = "SELECT * from CLASSES where parentClazzName = ?";
		List<ClazzAnalysisResult> res = new ArrayList<ClazzAnalysisResult> ();
		
		try (PreparedStatement stmt = conn.prepareStatement (sql)) {
			stmt.setString	(1, myName);
			ResultSet rs = stmt.executeQuery();
			/*
			CREATE TABLE CLASSES(
			clzName TEXT NOT NULL,
			 parentClazzName TEXT NOT NULL,
			 serializable INTEGER,
			  probablySerializable INTEGER,
			  invocationHandler INTEGER,
			  comparable INTEGER,
			  implementsReadObject INTEGER,
			  implementsEquals INTEGER,
			  implementsHashCode INTEGER,
			  implementsCompareTo INTEGER,
			  implementsFinalize INTEGER,
			  PRIMARY KEY (clzName, parentClazzName));
						    */
			while(rs.next()) {
				String cn = rs.getString (1);
				String pcn = rs.getString (2);
				int ser = rs.getInt (3);
				int pser = rs.getInt (4);
				int inv = rs.getInt  (5);
				int cmp = rs.getInt (6);
				int ro = rs.getInt (7);
				int eq = rs.getInt (8);
				int hc = rs.getInt (9);
				int ct = rs.getInt (10);
				int fin = rs.getInt (11);
				
				res.add (new ClazzAnalysisResult(cn, pcn, i2(ser), i2(pser), i2(inv), i2(cmp), i2(ro), i2(eq), i2(hc), i2(ct), i2(fin)));
			} 
		}
		
		return res.toArray(new ClazzAnalysisResult[0]);
	}
	
	public int performInheritanceUpdateQuery (String inheritedInterface)
		throws IllegalArgumentException, SQLException
	{
		String sql = getInheritanceQuery(inheritedInterface);
		try (PreparedStatement stmt = conn.prepareStatement (sql)) {
			stmt.execute    ();
			
			return stmt.getUpdateCount();
		}		
	}

	private static String getInheritanceQuery (String interfaceName)
	{
		if (!interfaceName.equals("comparable") && !interfaceName.equals("invocationHandler") && 
		    !interfaceName.equals ("serializable")) {
			
			throw new IllegalArgumentException ("only the three magic interfaces are allowed");
		}
		
		return String.format ("update CLASSES set %s=1 where clzName in (" +
				"select child.clzName from CLASSES child inner join CLASSES parent on child.parentClazzName = parent.clzName where " +
			    "parent.%s = 1 and child.%s = 0"
			+ ")", interfaceName, interfaceName,interfaceName);
				
	}
}
