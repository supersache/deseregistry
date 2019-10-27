package de.cw.deseregistry.driver;

import static de.cw.deseregistry.main.Listener_Action.ADD_CLASS;
import static de.cw.deseregistry.main.Listener_Action.ADD_EXTENDS;
import static de.cw.deseregistry.main.Listener_Action.ADD_IMPLEMENTS;
import static de.cw.deseregistry.main.Listener_Action.ADD_METHOD;
import static de.cw.deseregistry.main.Listener_Action.CONFIRM_IF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;

import de.cw.deseregistry.asm.ClassVisitor;
import de.cw.deseregistry.errorhandling.FatalException;
import de.cw.deseregistry.events.AddClzEvent;
import de.cw.deseregistry.events.ConfirmIfEvent;
import de.cw.deseregistry.events.AddExtenzEvent;
import de.cw.deseregistry.events.AddImplemenzEvent;
import de.cw.deseregistry.events.Event;
import de.cw.deseregistry.main.IDriver;
import de.cw.deseregistry.main.Listener;
import de.cw.deseregistry.main.Listener_Action;

public class ByteCodeProcessorDriver implements IDriver {

	private List<File> 		 jarfilesToProcess;
	private Set<String> 	 visitedClasses;
	private List<String> 	 classToProcess = new ArrayList<String> ();
	private List<Listener>   addClassListener = new ArrayList<Listener>();
	private List<Listener>   addImplementsListener = new ArrayList<Listener>();
	private List<Listener>   addExtendsListener = new ArrayList<Listener>();
	private List<Listener>   addMethodListener = new ArrayList<Listener>();
	private List<Listener>   confirmIfListener = new ArrayList<Listener> ();
	private List<Listener>   classLoadErrorListener = new ArrayList<Listener>();
	private PrintWriter 	 visitedFile = null;
	private Map<String,File> classToJarfileMap = new HashMap<String, File> ();
	
	public ByteCodeProcessorDriver (File visitedFile)
	{
		prepareVisitedFile (visitedFile);
		jarfilesToProcess = new ArrayList<File> ();
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
					this.visitedClasses.add (line);
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
	}
	
	
	@Override
	public void addJarToProcess(File file)
	{
		this.jarfilesToProcess.add (file);
	}

	@Override
	public void addClassToProcess(String className)
	{
		classToProcess.add (className);
	}

	@Override
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
			} else if (action == CONFIRM_IF) {
				confirmIfListener.add(listener);
			}
		}
	}

	@Override
	public void go() throws Exception
	{
		fillClassToJarMap ();
		for (String s : this.classToProcess) {
			if (this.visitedClasses.contains (s)) {
				continue;
			}
		}
	}
	
	private void recursiveVisit (String clzName) throws IOException
	{
		if (this.visitedClasses.contains(clzName)) {
			return;
		}

		byte [] bb = this.getByteCode (clzName);
		ClassReader cr = new ClassReader (bb);
		ClassVisitor cv = null;
		cr.accept (cv = new ClassVisitor (), ClassReader.SKIP_CODE);
		List<Event> evts = cv.getEvents ();
		
		// Now there can be only one class event
		// and one super class event. We need to
		// process the superclass first
		AddClzEvent ace = null;
		AddExtenzEvent aee = null;
		
		//
		// we need to get the 2 events
		//
		for (Event e: evts) {
			if (e instanceof AddClzEvent) {
				ace = (AddClzEvent) e;
			}
			if (e instanceof AddExtenzEvent) {
				aee = (AddExtenzEvent) e;
			}
		}
		evts.remove (ace);
		if (aee != null) {
			evts.remove (aee);
		}
		
		//
		// recursively do the class hierarchy
		//
		if (aee != null) {
			recursiveVisit (aee.getSuperClz());
		}

		// then notify the add class
		notify (ace);
		
		//
		// Now  the  remaining  events  should  be 
		// interface listener and AddMethodDingens
		//
		for (Event e: evts) {
			AddImplemenzEvent aie = (AddImplemenzEvent) e;
			
			// Erst müssen wir sicherstellen dass die Klassen
			// im Cache sind (SQLiteDriver.getPKForClass ())
			recursiveVisit (aie.getInterf());
			
			// now notify that this is an interface
			notify (new ConfirmIfEvent(aie.getInterf()));
		}
	}
	
	private void notify (List<Event> li)
	{
		for (Event e: li) {
			notify (e);
		}
	}
	
	private void notify (Event e)
	{
		if (e instanceof AddClzEvent) {
			AddClzEvent e2 = (AddClzEvent) e;
			File jar = this.classToJarfileMap.get (e2.getClassName ());
			if (jar != null)
				e2.setJarLocation (jar.getAbsolutePath ());
			
			for (Listener l : this.addClassListener) {
				l.notify(e);
			}
		}
		else if (e instanceof AddExtenzEvent) {
			for (Listener l : this.addExtendsListener) {
				l.notify(e);
			}
		}
		else if (e instanceof AddImplemenzEvent) {
			for (Listener l : this.addImplementsListener) {
				l.notify(e);
			}
		}
		else if (e instanceof ConfirmIfEvent) {
			for (Listener l : this.confirmIfListener) {
				l.notify(e);
			}
		}
	}
	
	private byte [] getByteCode (String className)
			throws IOException
	{
		final String CMD = "/bin/sh -c $@|sh . echo cd /tmp/ && jar -xvf %s '%s'";
		File jarFile = this.classToJarfileMap.get (className);
		
		try {
			Process p = Runtime.getRuntime ().exec (String.format(CMD, jarFile.getAbsoluteFile(), className + ".class"));
			int rc = p.waitFor ();
			if (rc != 0) {
				throw new IOException ("Process terminated with rc != 0");
			}
			else {
				File f = new File ("/tmp/" + className + ".class");
				long len = f.length ();
				if (len > new Long(Integer.MAX_VALUE)) {
					throw new IOException ("Class file too big.");
				}
				byte[] _bb_ = new byte [(int) len];
				FileInputStream fis = new FileInputStream (f);
				fis.read(_bb_);
				return _bb_;
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
	}
	
	/**
	 *  runs through all jar files and stores the class/jarfile tuple
	 *  in the map so that later on one can check in which jar file
	 *  a specific class is located.
	 * @throws IOException 
	 * @throws ZipException 
	 */
	private void fillClassToJarMap () throws ZipException, IOException
	{
		for (File f: jarfilesToProcess) {
			ZipFile zipFile = new ZipFile(f);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();
				if (!name.endsWith(".class"))
					continue;

				String className = name.substring(0, name.length() - 6);
				this.classToJarfileMap.put (className, f);
				this.addClassToProcess(className);
			}
		}
	}
}
