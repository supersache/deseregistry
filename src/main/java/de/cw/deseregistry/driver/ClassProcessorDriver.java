package de.cw.deseregistry.driver;

import static de.cw.deseregistry.driver.Listener_Action.ADD_CLASS;
import static de.cw.deseregistry.driver.Listener_Action.ADD_INTERFACE;
import static de.cw.deseregistry.driver.Listener_Action.ADD_METHOD;
import static de.cw.deseregistry.driver.Listener_Action.CLASS_LOAD_ERROR;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.cw.deseregistry.events.AddClassEvent;
import de.cw.deseregistry.events.AddIfEvent;
import de.cw.deseregistry.events.AddMethodEvent;
import de.cw.deseregistry.events.Event;
import de.cw.deseregistry.events.ExceptionEvent;

public class ClassProcessorDriver {

	private List<File> jarfilesToProcess;
	private List<String> classesToProcess;
	private Set<Class<?>> visitedClasses;
	private List<Listener> addClassListener = new ArrayList<Listener>();
	private List<Listener> addInterfaceListener = new ArrayList<Listener>();
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
		} else if (action == ADD_INTERFACE) {
			addInterfaceListener.add(listener);
		} else if (action == ADD_METHOD) {
			addMethodListener.add(listener);
		}
	}

	private void notify(Listener_Action action, Event e) {
		if (action == ADD_CLASS) {
			addClassListener.forEach(l -> {
				l.notify(e);
			});
		} else if (action == ADD_INTERFACE) {
			addInterfaceListener.forEach(l -> {
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

		// wir müssen beim Vater anfangen damit der im Cache ist
		recursiveVisit(clazz.getSuperclass());
		
		// erst wenn die ganze Kette zu java.lang.Object erledigt ist
		// fangen wir mit dem Rest an
		AddClassEvent ev2 = new AddClassEvent(clazz, clazz.getSuperclass());
		notify (ADD_CLASS, ev2);


		Class<?>[] ifaces = clazz.getInterfaces();
		for (Class<?> iface : ifaces) {
			// Interface müssen wir erst besuchen damit 
			// die pks im Cache sind
			recursiveVisit(iface);
			
			// jetzt ist die gesamte Hierarchy von iface 
			// im Cache und wir können die Kante hinzufügen
			AddIfEvent ev = new AddIfEvent (iface, ev2);
			notify (ADD_INTERFACE, ev);
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
