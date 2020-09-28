package de.cw.deseregistry.list.db;

import java.sql.SQLException;

public class DataIntegrityException extends SQLException
{
	public DataIntegrityException (String msg) { super (msg); }
}
