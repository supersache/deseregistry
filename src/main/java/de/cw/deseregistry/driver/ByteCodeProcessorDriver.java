package de.cw.deseregistry.driver;

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

import de.cw.deseregistry.errorhandling.FatalException;
import de.cw.deseregistry.main.IDriver;
import de.cw.deseregistry.main.Listener;
import de.cw.deseregistry.main.Listener_Action;

public class ByteCodeProcessorDriver implements IDriver {

	private List<File> jarfilesToProcess;
	private Set<String> visitedClasses;
	private List<String> classToProcess = new ArrayList<String> ();
	private List<Listener> addClassListener = new ArrayList<Listener>();
	private List<Listener> addImplementsListener = new ArrayList<Listener>();
	private List<Listener> addExtendsListener = new ArrayList<Listener>();
	private List<Listener> addMethodListener = new ArrayList<Listener>();
	private List<Listener> classLoadErrorListener = new ArrayList<Listener>();
	private PrintWriter visitedFile = null;
	
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
	
	
	/**
	 * 
	 */
	private Map<String,File> classToJarfileMap = new HashMap<String, File> ();
	
	
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
	public void register (String listenerClzName, Listener_Action[] actions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void go() throws Exception
	{
		fillClassToJarMap ();
		String s = this.classToProcess.get(0);
		byte [] bb = this.getByteCode(s);
	}
	
	private byte [] getByteCode (String className)
			throws IOException
	{
		final String CMD = "/bin/sh -c $@|sh . echo cd /tmp/ && jar -xvf %s %s";
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
