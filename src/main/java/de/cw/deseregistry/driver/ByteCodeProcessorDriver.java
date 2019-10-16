package de.cw.deseregistry.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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
	}
	
	private byte [] getByteCode (String className)
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
				byte _bb_ = new byte [len];
				FileInputStream fis = new FileInputStream (f);
				
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
