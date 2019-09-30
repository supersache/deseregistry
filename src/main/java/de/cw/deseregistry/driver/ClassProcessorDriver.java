package de.cw.deseregistry.driver;

import static de.cw.deseregistry.driver.Listener_Action.ADD_CLASS;
import static de.cw.deseregistry.driver.Listener_Action.ADD_INTERFACE;
import static de.cw.deseregistry.driver.Listener_Action.ADD_METHOD;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.cw.deseregistry.events.AddClassEvent;
import de.cw.deseregistry.events.AddIfEvent;
import de.cw.deseregistry.events.AddMethodEvent;
import de.cw.deseregistry.events.Event;

public class ClassProcessorDriver {

	private Vector<File> jarfilesToProcess;
	private List<Listener> addClassListener = new ArrayList<Listener>();
	private List<Listener> addInterfaceListener = new ArrayList<Listener>();
	private List<Listener> addMethodListener = new ArrayList<Listener>();

	public ClassProcessorDriver() {
		this.jarfilesToProcess = new Vector<File>();
	}

	public void addJarToProcess(File file) {
		this.jarfilesToProcess.add(file);
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
	}

	private void recursiveVisit(Class clazz) throws Exception {

		AddClassEvent ev2 = new AddClassEvent(clazz);
		
		if (clazz.isInterface()) {
			// has been notified before already
		}
		else {
			notify(ADD_CLASS, ev2);
		}

		recursiveVisit(clazz.getSuperclass());

		Class<?>[] ifaces = clazz.getInterfaces();
		for (Class<?> iface : ifaces) {
			AddIfEvent ev = new AddIfEvent (iface);
			ev.setClass_pk (ev2.getClass_pk());
			notify (ADD_INTERFACE, ev);
			recursiveVisit(iface);
		}

		Method[] methods = clazz.getDeclaredMethods();

		for (Method m : methods) {
			notify (ADD_METHOD, new AddMethodEvent (m));
		}
	}
}
