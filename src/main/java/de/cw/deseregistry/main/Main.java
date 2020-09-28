package de.cw.deseregistry.main;

import static de.cw.deseregistry.utils.IConstants.DB_FILE;
import static de.cw.deseregistry.utils.IConstants.DB_SKIP_PARENT_CHECK;
import static de.cw.deseregistry.utils.IConstants.DRIVER_CLASS_FILE;
import static de.cw.deseregistry.utils.IConstants.INPUT_FILE_JARS;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.cw.deseregistry.driver.IBackCaller;
import de.cw.deseregistry.driver.IDriver;
import de.cw.deseregistry.list.db.SQLiteDriver;


public class Main implements IBackCaller {
	
	/**
	 *  input file. @TODO was genau steht hier drin????
	 */
	private File inputfile;
	
	private IDriver cpd;
	
	private Set<String> knownClasses = new HashSet<String> ();

	private static Logger LOGGER = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) throws Exception     
	{
		LOGGER.log (Level.FINE, "Pilsken");
		Properties conf = new Properties();

		if (!readConf(args, conf)) {
			System.out.println("Cannot read central properties file, exiting ...");
			System.exit(1);
		}

		Main m = new Main();
		m.prepare(conf);
		m.process();
	}

	private static boolean readConf(String[] args, Properties props) {
		if (args.length == 0) {
			System.err.println("Usage: deseregistry <properties file>");
			return false;
		}

		try {
			File propsfile = new File(args[0]);
			if (!propsfile.exists() || propsfile.isDirectory()) {
				System.err.println("props file " + args[0] + " doesn't exist as file.");
				return false;
			}

			props.load(new FileInputStream(propsfile));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception in readConf", e);
			return false;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	/* package private */
	void prepare(Properties conf) throws ReflectiveOperationException, SQLException
	{

		System.out.println("[!] Getting input file");
		String inputfileParam = conf.getProperty(INPUT_FILE_JARS);

		if (inputfileParam == null) {
			System.err.println("Parameter " + INPUT_FILE_JARS + " missing in config, exiting ...");
			System.exit(1);
		}
		this.inputfile = new File(inputfileParam);

		if (!inputfile.exists()) {
			System.err.println("Input file " + inputfileParam + " doesn't exist, exiting ...");
			System.exit(1);
		}
		System.out.println("[+] ok");
		
		boolean skipDB = conf.getProperty(DB_SKIP_PARENT_CHECK).equalsIgnoreCase("true");
//
//		String inputClassesParam = conf.getProperty(INPUT_FILE_CLASSES);
//
//		if (inputClassesParam != null) {
//			this.fileClassesToAnalyze = new File(inputClassesParam);
//
//			if (!fileClassesToAnalyze.exists()) {
//				System.err.println("Input file " + inputClassesParam + " doesn't exist, exiting ...");
//				System.exit(1);
//			}
//		} else {
//			// if inputClassesParam is empty, there need to be jar files to analyze
//		}

		System.out.println("[!] Getting DB file");
		String dbfileParam = conf.getProperty(DB_FILE);
		if (dbfileParam == null) {
			System.err.println("Parameter " + DB_FILE + " missing in config, exiting ...");
			System.exit(1);
		}

		if (!new File(dbfileParam).exists()) {
			System.err.println("DB File " + dbfileParam + " doesn't exist, exiting ...");
			System.exit(1);
		}
		System.out.println("[+] ok");
		
		System.setProperty("SQLITEDBFILE", dbfileParam);

		/**
		 * Für's Laden des DB driver, vielleicht etwas umständlich
		 */
//		System.setProperty("SQLITEDBFILE", dbfileParam);

//		if (this.fileClassesToAnalyze != null)
//			this.classesToAnalyze = readClasses();
//		else
//			this.classesToAnalyze = new ArrayList<String>();
//
//		String visitedParam = conf.getProperty(MAINT_VISITED_FILE);
//		if (visitedParam == null) {
//			System.err.println("Parameter " + MAINT_VISITED_FILE + " missing in config, exiting ...");
//			System.exit(1);
//		}
//
//		File visited = new File(visitedParam);

		System.out.println("[!] Getting processor class");
		String driverClazz = conf.getProperty(DRIVER_CLASS_FILE);
		if (driverClazz == null) {
			System.err.println("Parameter " + DRIVER_CLASS_FILE + " missing in config, exiting ...");
			System.exit(1);
		}

		/**
		 * Den ClassProzessor instantiieren
		 */
		Class<? extends IDriver> clazzProcessor = (Class<? extends IDriver>) Class.forName(driverClazz);
		
		this.cpd = (IDriver) clazzProcessor.getConstructor(Boolean.TYPE).newInstance(skipDB);
		this.cpd.setBackCaller(this);
		System.out.println("[+] Ok");
		
		if (inputfile.isDirectory()) {
			// process all jar files in the directory
			File [] files = inputfile.listFiles (pathname -> pathname.toString().endsWith(".jar"));
			for (File file: files) {
				this.cpd.addJarToProcess (file);				
			}
		}
		else {
			this.cpd.addJarToProcess (inputfile);
		}
		
		/**
		 * Get all known classes
		 */
		System.out.println("[!] Reading known classes from DB");
		this.prepareClasses();
		System.out.println("[+] Ok");
	}

	public void process() throws Exception
	{
		cpd.go();
	}

	@Override
	public void notify(NotificationType type, Object details) {
		if (type==NotificationType.CLASS_INSERTED) {
			System.out.println("[+] Class " + details.toString () + " inserted.");
		}
		if (type==NotificationType.CLASS_UPDATED) {
			System.out.println("[+] Class " + details.toString () + " updated.");
		}
	}
	
	@Override
	public Object getParam (String key) {
		return null;
	}
	
	@Override
	public boolean classExists (String className)
	{
		return this.knownClasses.contains(className);
	}
	
	private void prepareClasses () throws SQLException
	{
		SQLiteDriver sqld = SQLiteDriver.getInstance ();
		
		List<String> all = sqld.getAllClassNames ();
		
		this.knownClasses.addAll(all);
	}
}
