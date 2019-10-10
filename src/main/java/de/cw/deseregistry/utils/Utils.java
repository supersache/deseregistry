package de.cw.deseregistry.utils;

import java.lang.reflect.Method;

public class Utils {
	public static String signature (Method m, Class<?> implementingClass)
	{
		String s = m.toGenericString ();
		//System.out.println(s);
		
		String clzName = implementingClass.getName();
		//System.out.println(clzName);
		
		int idx = s.indexOf (clzName);
		//System.out.println(idx);
		int len = clzName.length ();
		//System.out.println(len);
		
		return new StringBuffer ().append(s.substring(0, idx))
				                  .append(s.substring(idx + len + 1)).toString();
	}
	
	public static void main (String [] args) throws NoSuchMethodException, SecurityException, ClassNotFoundException
	{
		Class <?> iClz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
		System.out.println(signature (iClz.getDeclaredMethod ("readObject", java.io.ObjectInputStream.class), iClz));
	}
}
