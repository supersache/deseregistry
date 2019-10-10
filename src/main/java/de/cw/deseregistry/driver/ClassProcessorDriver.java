package de.cw.deseregistry.driver;

import static de.cw.deseregistry.driver.Listener_Action.ADD_CLASS;
import static de.cw.deseregistry.driver.Listener_Action.ADD_IMPLEMENTS;
import static de.cw.deseregistry.driver.Listener_Action.ADD_EXTENDS;
import static de.cw.deseregistry.driver.Listener_Action.ADD_METHOD;
import static de.cw.deseregistry.driver.Listener_Action.CLASS_LOAD_ERROR;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.cw.deseregistry.events.AddClassEvent;
import de.cw.deseregistry.events.AddImplementsEvent;
import de.cw.deseregistry.events.AddMethodEvent;
import de.cw.deseregistry.events.Event;
import de.cw.deseregistry.events.ExceptionEvent;

public class ClassProcessorDriver {

	private List<File> jarfilesToProcess;
	private List<String> classesToProcess;
	private Set<Class<?>> visitedClasses;
	private List<Listener> addClassListener = new ArrayList<Listener>();
	private List<Listener> addImplementsListener = new ArrayList<Listener>();
	private List<Listener> addExtendsListener = new ArrayList<Listener>();
	private List<Listener> addMethodListener = new ArrayList<Listener>();
	private List<Listener> classLoadErrorListener = new ArrayList<Listener>();

	public ClassProcessorDriver() {
		this.jarfilesToProcess = new ArrayList<File>();
		this.visitedClasses = new HashSet<Class<?>> ();
		this.classesToProcess = new ArrayList<String> ();
	}

	public void addJarToProcess(File file) {
		this.jarfilesToProcess.add(file);
	}
	
	public void addClassToProcess (String className)
	{
		this.classesToProcess.add(className);
	}

	public void register(Listener listener, Listener_Action action) {
		if (action == ADD_CLASS) {
			addClassListener.add(listener);
		} else if (action == ADD_IMPLEMENTS) {
			addImplementsListener.add(listener);
		} else if (action == ADD_EXTENDS) {
			addExtendsListener.add(listener);
		} else if (action == ADD_METHOD) {
			addMethodListener.add(listener);
		}
	}

	private void notify(Listener_Action action, Event e) {
		if (action == ADD_CLASS) {
			addClassListener.forEach(l -> {
				l.notify(e);
			});
		} else if (action == ADD_IMPLEMENTS) {
			addImplementsListener.forEach(l -> {
				l.notify(e);
			});
		} else if (action == ADD_EXTENDS) {
			addExtendsListener.forEach(l -> {
				l.notify(e);
			});
		} else if (action == ADD_METHOD) {
			addMethodListener.forEach(l -> {
				l.notify(e);
			});
		}
		else if (action == CLASS_LOAD_ERROR) {
			classLoadErrorListener.forEach(l -> {
				l.notify (e);
			});
		}
	}

	private void recursiveVisit(Class clazz) throws Exception
	{
		if (clazz == null)
			return;
		
		if (visitedClasses.contains (clazz))
			return;
		else
			visitedClasses.add (clazz);

		AddClassEvent ev2 = new AddClassEvent(clazz, clazz.getSuperclass());
		notify (ADD_CLASS, ev2);

		recursiveVisit(clazz.getSuperclass());

		Class<?>[] ifaces = clazz.getInterfaces();
		for (Class<?> iface : ifaces) {
			AddImplementsEvent ev = new AddImplementsEvent (iface, ev2);
			notify (ADD_INTERFACE, ev);
			recursiveVisit(iface);
		}

		Method[] methods = clazz.getDeclaredMethods();

		for (Method m : methods) {
			notify (ADD_METHOD, new AddMethodEvent (m));
		}
	}
	
	public void go () throws Exception
	{
		classesToProcess.forEach(s -> {
			try { 
				Class<?> clz = Class.forName (s);
				this.recursiveVisit(clz);
			}
			catch (Throwable e) {
				ExceptionEvent excev = new ExceptionEvent ();
				excev.exc = e;
				notify (Listener_Action.CLASS_LOAD_ERROR, excev);
			}
		});
	}
}
