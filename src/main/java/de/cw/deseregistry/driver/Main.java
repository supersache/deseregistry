package de.cw.deseregistry.driver;

import static de.cw.deseregistry.driver.Listener_Action.ADD_CLASS;
import static de.cw.deseregistry.driver.Listener_Action.ADD_EXTENDS;
import static de.cw.deseregistry.driver.Listener_Action.ADD_IMPLEMENTS;
import static de.cw.deseregistry.driver.Listener_Action.ADD_METHOD;
import static de.cw.deseregistry.driver.Listener_Action.CLASS_LOAD_ERROR;
import static de.cw.deseregistry.utils.IConstants.DB_FILE;
import static de.cw.deseregistry.utils.IConstants.INPUT_FILE_CLASSES;
import static de.cw.deseregistry.utils.IConstants.INPUT_FILE_JARS;
import static de.cw.deseregistry.utils.IConstants.MAINT_VISITED_FILE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import de.cw.deseregistry.list.db.SQLiteListener;

public class Main
{
	private Vector <File> filesToProcess;
	private File inputfile;
	private File fileClassesToAnalyze;
	private File dbfile;
	private File visited;
	private List<String> classesToAnalyze;
	
	private ClassProcessorDriver cpd;
	
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		Properties conf = new Properties ();
		
		if (!readConf (args, conf)) {
			System.out.println("Cannot read central properties file, exiting ...");
			System.exit (1);
		}
		
		Main m = new Main ();
		m.prepare(conf);
		m.process ();
	}

	private static boolean readConf (String [] args, Properties props)
	{
		if (args.length == 0) {
			System.out.println ("Usage: deseregistry <properties file>");
			return false;
		}
		
		try {
			File propsfile = new File (args [0]);
			if (!propsfile.exists () || propsfile.isDirectory ()) {
				System.out.println("props file " + args [0] + " doesn't exist as file.");
				return false;
			}

			props.load (new FileInputStream (propsfile));
		}
		catch (Exception e) {
			e.printStackTrace (System.err);
			return false;
		}
		
		return true;
	}
	
	private List<String> readClasses () throws FileNotFoundException, IOException
	{
		try (BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (this.fileClassesToAnalyze)))) {
			String line;
			List<String> l = new ArrayList<String> ();
			while (null != (line = br.readLine())) {				
				// Comments
				if (line.charAt(0) == '#')
					continue;
//				if (line.charAt (line.length () - 1) == '\n')
//					line = 
				l.add(line.trim ());
			}
			
			return l;
		}
	}
	
	@SuppressWarnings("unchecked")
	/* package private */
	void prepare (Properties conf) throws ClassNotFoundException, 
				InstantiationException,
				IllegalAccessException,
				IllegalArgumentException,
				InvocationTargetException,
				NoSuchMethodException,
				SecurityException,
				FileNotFoundException,
				IOException
	{
        this.filesToProcess = new Vector <File> ();
        
        String inputfileParam = conf.getProperty (INPUT_FILE_JARS);
        
        if (inputfileParam == null) {
        	System.err.println ("Parameter " + INPUT_FILE_JARS + " missing in config, exiting ...");
        	System.exit(1);
        }
        this.inputfile = new File (inputfileParam);

        if (!inputfile.exists ()) {
        	System.err.println ("Input file " + inputfileParam + " doesn't exist, exiting ...");
            System.exit (1);
        }
        
        String inputClassesParam = conf.getProperty (INPUT_FILE_CLASSES);
        
        if (inputClassesParam != null) {
	        this.fileClassesToAnalyze = new File (inputClassesParam);
	
	        if (!fileClassesToAnalyze.exists ()) {
	        	System.err.println ("Input file " + inputClassesParam + " doesn't exist, exiting ...");
	            System.exit (1);
	        }
        }
        else {
        	// if inputClassesParam is empty, there need to be jar files to analyze
        }
        
        String dbfileParam = conf.getProperty (DB_FILE);        
        if (dbfileParam == null) {
        	System.out.println ("Parameter " + DB_FILE + " missing in config, exiting ...");
        	System.exit(1);
        }
        this.dbfile = new File (dbfileParam);

        if (!dbfile.exists ()) {
        	System.out.println ("Input file " + inputClassesParam + " doesn't exist, exiting ...");
            System.exit (1);
        }
        
        /**
         *  Für's Laden des DB driver, vielleicht etwas umständlich
         */
        System.setProperty("SQLITEDBFILE", dbfileParam);
        
        if (this.fileClassesToAnalyze != null)
        	this.classesToAnalyze = readClasses ();
        
        String visitedParam = conf.getProperty (MAINT_VISITED_FILE);
        if (visitedParam == null) {
        	System.err.println ("Parameter " + MAINT_VISITED_FILE + " missing in config, exiting ...");
        	System.exit (1);
        }
        
        File visited = new File (visitedParam);
        
        /**
         *  Den ClassProzessor laden wir per eigenem ClassLoader,
         *  weil ich will dass er alle Klassen aus den jar-files
         *  per Class.forName laden kann, aber ich will natürlich
         *  nicht beim Start alle jar files per -cp einbinden will
         */
        Class<ClassProcessorDriver> clazzProcessor = null;

        /**
         * Hier müssen wir die Fälle "input ist jar-file" und
         * "input ist directory abhandeln.
         */
        if (inputfile.isDirectory ()) {
        	// hier wird durch das Verzeichnis iteriert und
        	// alle jars werden hinzugefügt
        	File [] jarfiles = inputfile.listFiles((f, s) -> { return s.endsWith (".jar"); });
            Vector<URL> v = new Vector<URL> ();
            for (File jarfile : jarfiles) {
                v.add (jarfile.toURI ().toURL ());
                filesToProcess.add (jarfile);
            }
            clazzProcessor = (Class<ClassProcessorDriver>) Class.forName ("de.cw.deseregistry.driver.ClassProcessorDriver", true, 
                new URLClassLoader (v.toArray (new URL[0]), Main.class.getClassLoader ()));
        }
        else {
            clazzProcessor = (Class<ClassProcessorDriver>) Class.forName ("de.cw.deseregistry.driver.ClassProcessorDriver", true, 
                new URLClassLoader (new URL [] { inputfile.toURI().toURL () }, Main.class.getClassLoader ()));
            /**
             *  hier wird nur die Datei selber hinzugefügt, da jar-file
             */
            filesToProcess.add (inputfile);
        }
        
        /*
         *  wir haben hier das input Verzeichnis oder jar file...
         *  nur wenn fileClassesToAnalyze leer ist wird das prozessiert.
         *  In diesem Fall muss da aber wirklich was drinstehen !!!!
         */
        if (this.fileClassesToAnalyze==null) {
        	if (this.filesToProcess.size() == 0) {
        		System.err.println ("Keine Input Klassen, nichts zu tun! Exiting ...");
        		System.exit (1);
        	}
        	
        	// die folgende Methode wird nun (hoffentlich) Klassen hinzufügen
        	Enumeration<File> e = this.filesToProcess.elements();
        	while (e.hasMoreElements()) {
        		this.addClassesFromJar(e.nextElement());
        	}
        }
        this.cpd = clazzProcessor.getConstructor (java.io.File.class).newInstance (visited);

        this.classesToAnalyze.forEach (s -> { this.cpd.addClassToProcess(s); });
	}

	public void process () throws Exception
	{
		SQLiteListener cl = new SQLiteListener ();
        cpd.register (cl, ADD_CLASS);
        cpd.register (cl, ADD_IMPLEMENTS);
        cpd.register (cl, ADD_EXTENDS);
        cpd.register(cl, ADD_METHOD);
        cpd.register (cl, CLASS_LOAD_ERROR);
        cpd.go ();
	}
	
	private void addClassesFromJar (File jarFile) throws IOException
	{
	    ZipFile zipFile = new ZipFile(jarFile);

	    Enumeration<? extends ZipEntry> entries = zipFile.entries();

	    while (entries.hasMoreElements ()) {
	        ZipEntry entry = entries.nextElement();
	        String name = entry.getName();
	        if (!name.endsWith(".class"))
	        	continue;
	        
	        String className = name.replace ('/', '.').substring (0, name.length ()-6);
	        this.classesToAnalyze.add (className);
	    } 
	}
}

