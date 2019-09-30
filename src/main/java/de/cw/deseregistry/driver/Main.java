package de.cw.deseregistry.driver;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

public class Main {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
        String inputfilename = args [0];
        File   inputfile = new File (args [0]);
        Vector <File> filesToProcess = new Vector <File> ();

        if (!inputfile.exists ()) {
            // error handling
            System.exit (1);
        }

        Class<ClassProcessorDriver> clazzProcessor = null;

        if (inputfile.isDirectory ()) {
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

            filesToProcess.add (inputfile);
        }

        final ClassProcessorDriver myProcessor = (ClassProcessorDriver) clazzProcessor.newInstance ();
        filesToProcess.forEach(f -> { myProcessor.addJarToProcess (f); });
	}

}
