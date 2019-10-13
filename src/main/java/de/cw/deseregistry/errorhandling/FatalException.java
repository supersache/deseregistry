package de.cw.deseregistry.errorhandling;

public class FatalException extends RuntimeException {

	public FatalException (String s) { super (s); }
	public FatalException (Exception e) { super (e); }
}
