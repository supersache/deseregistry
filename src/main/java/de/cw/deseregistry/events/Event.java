package de.cw.deseregistry.events;

import java.lang.reflect.Method;

public class Event {
	public Class<?> getEventClass () throws IllegalAccessException {
		throw new IllegalAccessException ("whatever");
	}
	
	public Class<?> getInterface () throws IllegalAccessException {
		throw new IllegalAccessException ("whatever");
	}
	
	public Method getMethod () throws IllegalAccessException {
		throw new IllegalAccessException ("whatever");
	}
}
