package de.cw.deseregistry.driver;

import static de.cw.deseregistry.main.Listener_Action.ADD_CLASS;
import static de.cw.deseregistry.main.Listener_Action.ADD_EXTENDS;
import static de.cw.deseregistry.main.Listener_Action.ADD_IMPLEMENTS;
import static de.cw.deseregistry.main.Listener_Action.ADD_METHOD;
import static de.cw.deseregistry.main.Listener_Action.CLASS_LOAD_ERROR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.cw.deseregistry.errorhandling.FatalException;
import de.cw.deseregistry.events.AddClassEvent;
import de.cw.deseregistry.events.AddExtendsEvent;
import de.cw.deseregistry.events.AddImplementsEvent;
import de.cw.deseregistry.events.AddMethodEvent;
import de.cw.deseregistry.events.Event;
import de.cw.deseregistry.main.IDriver;
import de.cw.deseregistry.main.Listener;
import de.cw.deseregistry.main.Listener_Action;

public class ClassProcessorDriver implements IDriver
{

	private List<File> jarfilesToProcess;
	private List<String> classesToProcess;
	private Set<Class<?>> visitedClasses;
	private List<Listener> addClassListener = new ArrayList<Listener>();
	private List<Listener> addImplementsListener = new ArrayList<Listener>();
	private List<Listener> addExtendsListener = new ArrayList<Listener>();
	private List<Listener> addMethodListener = new ArrayList<Listener>();
	private List<Listener> classLoadErrorListener = new ArrayList<Listener>();
	
	private PrintWriter visitedFile = null;

	public ClassProcessorDriver() {
		this.jarfilesToProcess = new ArrayList<File>();
		this.visitedClasses = new HashSet<Class<?>> ();
		this.classesToProcess = new ArrayList<String> ();
	}
	
	public ClassProcessorDriver (File visitedFile)
	{
		this ();
		prepareVisitedFile (visitedFile);
	}
	
	/**
	 * Kümmert sich um die Datei, die schon analysierte Klassen speichert
	 * @param visitedFile Parameter aus der Config der die Datei benennt
	 */
	private void prepareVisitedFile (File visitedFile)
	{
		try {
			if (visitedFile.exists ()) {
				/**
				 *  Wenn die Datei existiert müssen zuerst die schon besuchten Klassen
				 *  eingelesen werden damit man nachher da weitermachen kann
				 *  wo man das letzte Mal aufgehört hat
				 */
				BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (visitedFile)));
				String line = null;
				while (null != (line = br.readLine ())) {
					this.visitedClasses.add (Class.forName (line));
				}
				// wenn wir hier nicht close machen funktioniert die 
				// darauf folgende Anweisung nicht.
				br.close();
				// damit wir eine neue Datei schreiben können
				visitedFile.delete ();
			}
			this.visitedFile = new PrintWriter (new FileOutputStream (visitedFile));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
			throw new FatalException ("Cannot create Visited File.");
		}
		catch (IOException e) {
			e.printStackTrace (System.err);
			throw new FatalException ("Stress with visited file.");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace (System.err);
			throw new FatalException ("Cannot load visited class.");
		}				
	}

	public void addJarToProcess(File file) {
		this.jarfilesToProcess.add(file);
	}
	
	public void addClassToProcess (String className)
	{
		this.classesToProcess.add(className);
	}

	public void register(String clz, Listener_Action [] actions) {
		
		Listener listener = null;
		try {
			listener = (Listener) Class.forName(clz).newInstance();
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new FatalException (e);
		} 		
		
		for (Listener_Action action : actions) {
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

	private void recursiveVisit(Class clazz)
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
		AddClassEvent ev2 = new AddClassEvent(clazz);
		notify (ADD_CLASS, ev2);
		
		// jetzt wo beide Klassen im System sind können wir die Kante zwischen den
		// beiden notifizieren.
		if (clazz.getSuperclass() != null) {
			AddExtendsEvent aee = new AddExtendsEvent (clazz.getSuperclass(), ev2);
			notify (ADD_EXTENDS, aee);
		}


		Class<?>[] ifaces = clazz.getInterfaces();
		for (Class<?> iface : ifaces) {
			// Interface müssen wir erst besuchen damit 
			// die pks im Cache sind
			recursiveVisit(iface);
			
			// jetzt ist die gesamte Hierarchy von iface 
			// im Cache und wir können die Kante hinzufügen
			AddImplementsEvent ev = new AddImplementsEvent (iface, ev2);
			notify (ADD_IMPLEMENTS, ev);
		}

		Method[] methods = clazz.getDeclaredMethods();

		for (Method m : methods) {
			notify (ADD_METHOD, new AddMethodEvent (m, clazz));
		}
	}
	
	public void go () throws Exception
	{
		try {
			classesToProcess.forEach(s -> {
				try {
					Class<?> clz = Class.forName (s);
					this.recursiveVisit(clz);
				}
				catch (ClassNotFoundException e) {
					e.printStackTrace (System.err);
				}
			});
		}
		catch (FatalException e) {
			System.err.println(e.getMessage());
		}
		finally {
			this.visitedClasses.forEach(s -> { this.visitedFile.println (s.getName()); });
			this.visitedFile.close();
		}
	}
}
