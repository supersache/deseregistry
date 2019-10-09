package de.cw.deseregistry.driver;

import static de.cw.deseregistry.utils.IConstants.INPUT_FILE_CLASSES;
import static de.cw.deseregistry.utils.IConstants.INPUT_FILE_JARS;

import static de.cw.deseregistry.driver.Listener_Action.ADD_CLASS;
import static de.cw.deseregistry.driver.Listener_Action.ADD_INTERFACE;
import static de.cw.deseregistry.driver.Listener_Action.ADD_METHOD;
import static de.cw.deseregistry.driver.Listener_Action.CLASS_LOAD_ERROR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class Main {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		Properties conf = new Properties ();
		
		if (!readConf (args, conf)) {
			System.out.println("Cannot read central properties file, exiting ...");
			System.exit (1);
		}
        Vector <File> filesToProcess = new Vector <File> ();
        
        String inputfileParam = conf.getProperty (INPUT_FILE_JARS);
        
        if (inputfileParam == null) {
        	System.out.println ("Parameter " + INPUT_FILE_JARS + " missing in config, exiting ...");
        	System.exit(1);
        }
        File inputfile = new File (inputfileParam);

        if (!inputfile.exists ()) {
        	System.out.println ("Input file " + inputfileParam + " doesn't exist, exiting ...");
            System.exit (1);
        }
        
        String inputClassesParam = conf.getProperty (INPUT_FILE_CLASSES);
        
        if (inputClassesParam == null) {
        	System.out.println ("Parameter " + INPUT_FILE_CLASSES + " missing in config, exiting ...");
        	System.exit(1);
        }
        File inputfile2 = new File (inputClassesParam);

        if (!inputfile2.exists ()) {
        	System.out.println ("Input file " + inputClassesParam + " doesn't exist, exiting ...");
            System.exit (1);
        }
        
        List<String> classesToAnalyze = readClasses (inputfile2);
        

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
        
        /**
         *  Den eigentlichen Prozessor instrumentieren
         */
        ClassProcessorDriver cpd = clazzProcessor.newInstance();
        
        /**
         *  zu analysierende Klassen festlegen
         */
        classesToAnalyze.forEach (s -> { cpd.addClassToProcess(s); });
        
        /**
         *  Listener registrieren
         */
        ConsoleListener cl = new ConsoleListener();
        cpd.register (cl, ADD_CLASS);
        cpd.register (cl, ADD_INTERFACE);
        cpd.register(cl, ADD_METHOD);
        cpd.register (cl, CLASS_LOAD_ERROR);
        cpd.go ();
	}

	private static boolean readConf (String [] args, Properties props)
	{
		if (args.length == 0) {
			System.out.println("Usage: deseregistry <properties file>");
			return false;
		}
		
		try {
			File propsfile = new File (args [0]);
			if (!propsfile.exists () || propsfile.isDirectory()) {
				System.out.println("props file " + args [0] + " doesn't exist as file.");
				return false;
			}

			props.load (new FileInputStream (propsfile));
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private static List<String> readClasses (File file) throws FileNotFoundException, IOException
	{
		try (BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (file)))) {
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

}
