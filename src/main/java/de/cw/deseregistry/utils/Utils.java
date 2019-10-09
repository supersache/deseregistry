package de.cw.deseregistry.utils;

import java.lang.reflect.Method;

public class Utils {
	public static String signature (Method m)
	{
		return m.toGenericString ();
	}
}
