package de.cw.deseregistry.driver;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;

import de.cw.deseregistry.asm.ClassVisitor;
import de.cw.deseregistry.driver.IBackCaller.NotificationType;
import de.cw.deseregistry.events.AddClassEvent;
import de.cw.deseregistry.events.AddFieldEvent;
import de.cw.deseregistry.events.AddMethodEvent;
import de.cw.deseregistry.events.Event;
import de.cw.deseregistry.list.db.DataIntegrityException;
import de.cw.deseregistry.list.db.SQLiteDriver;
import de.cw.deseregistry.utils.ClazzAnalysisResult;

public class ByteCodeProcessorDriver implements IDriver {

	private static Logger LOGGER = Logger.getLogger(ByteCodeProcessorDriver.class.getName());

	private List<File> jarfileToProcess = new ArrayList<File>();
	private Map<String, File> classToJarfileMap = new HashMap<String, File>();
	private IBackCaller backcaller;
	private boolean skipDbCheck = false;

	private static SQLiteDriver sqld = SQLiteDriver.getInstance ();

	public ByteCodeProcessorDriver(boolean skipDbParentCheck) {
		this.skipDbCheck = skipDbParentCheck;
	}

	@Override
	public void addJarToProcess(File file) {
		this.jarfileToProcess.add(file);
	}

	@Override
	public void go() throws IOException {
		for (File file : this.jarfileToProcess) {
			process(file);
		}
	}

	private void process(File fileToProcess) throws IOException {
		try (FileInputStream fis = new FileInputStream(fileToProcess);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ZipInputStream zis = new ZipInputStream(bis)) {
			ZipEntry entry = null;
			byte[] buffer = new byte[1024];

			while (null != (entry = zis.getNextEntry())) {
				List<Event> evts = null;
				String clzName = entry.getName();
				if (!clzName.endsWith(".class"))
					continue;
				else
					clzName = clzName.substring(0, clzName.length() - 6);

				// if (this.visitedClasses.contains (clzName)) {
				// continue;
				// }
				// else {
				// this.visitedClasses.add (clzName);
				// }

				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				int len;
				while ((len = zis.read(buffer)) > 0) {
					byte[] newBuffer = new byte[len];
					System.arraycopy(buffer, 0, newBuffer, 0, len);
					baos.write(newBuffer);
				}

				byte[] classByteCode = baos.toByteArray();
				try {
					process0(clzName, classByteCode);
				}
				catch (NotSerializableException e) {

				}
				catch (DataIntegrityException e) {
					LOGGER.severe(e.getMessage());
				}
				catch (SQLException e) {
					LOGGER.log(Level.SEVERE, "SQL exception with class " + clzName, e);
				}
				catch (RuntimeException e) {
					LOGGER.log(Level.SEVERE, "Unexpected exception with class " + clzName, e);
				}
			}
		}
	}

	@Override
	public void setBackCaller(IBackCaller vif) {
		this.backcaller = vif;
	}

	private void process0(String clzName, byte[] classByteCode)
			throws IOException, NotSerializableException, SQLException {
		List<Event> evts = new ArrayList<Event>();
		byte[] bb = classByteCode;
		if (bb != null) {
			ClassReader cr = new ClassReader(bb);
			ClassVisitor cv = null;
			cr.accept(cv = new ClassVisitor(), ClassReader.SKIP_CODE);
			evts = cv.getEvents();
		} else {
			throw new IOException("Byte code for class code cannot be retrieved.");
		}

		String parentClazzName = null;
		boolean probablySerializable = false;
		boolean serializable = false;
		boolean invocationHandler = false;
		boolean implementsEquals = false;
		boolean implementsHashCode = false;
		boolean implementsReadObject = false;
		boolean implementsCompareTo = false;
		boolean implementsFinalize = false;
		boolean comparable = false;

		for (Event e : evts) {
			if (e instanceof AddClassEvent) {
				AddClassEvent ace = (AddClassEvent) e;
				if (!clzName.equals(ace.getName())) {
					LOGGER.warning(String.format("%s not supposed to yield clz name %s", clzName, ace.getName()));
				}
				clzName = ace.getName();
				parentClazzName = ace.getSuperClass();
				serializable = ace.getInterfaces().contains("java/io/Serializable");
				comparable = ace.getInterfaces().contains("java/lang/Comparable");
				invocationHandler = ace.getInterfaces().contains("java/lang/reflect/InvocationHandler");
			} else if (e instanceof AddMethodEvent) {
				AddMethodEvent ame = (AddMethodEvent) e;
				if (isReadObject(ame)) {
					probablySerializable = true;
					implementsReadObject = true;
				} else if (isEquals(ame)) {
					implementsEquals = true;
				} else if (isHashCode(ame)) {
					implementsHashCode = true;
				} else if (isFinalize(ame)) {
					implementsFinalize = true;
				} else if (isCompareTo(ame)) {
					implementsCompareTo = true;
				}
			} else if (e instanceof AddFieldEvent) {
				AddFieldEvent afee = (AddFieldEvent) e;

				if (isSerialVersionUID(afee)) {
					probablySerializable = true;
				}
			}
		}

		ClazzAnalysisResult car = new ClazzAnalysisResult(clzName, parentClazzName, probablySerializable, serializable,
				invocationHandler, implementsEquals, implementsHashCode, implementsReadObject, implementsCompareTo,
				implementsFinalize, comparable);

		/**
		 *  we'll be doing the child class stuff in another class
		 */
		//checkChildClasses (car);

		/** angesichts der Tatsache, dass alle relevanten Interfaces auch
		 *  durch Vererbung "zur Klasse" kommen können, erscheint es angebracht,
		 *  doch alle Klassen zu speichern und nicht nur die, die wir als Serializable
		 *  erkannt haben.
		 */
//		if (!car.serializable && !car.probablySerializable) {
//			throw new NotSerializableException(clzName);
//		}

		boolean clazzExists = backcaller.classExists(clzName);
		if (!clazzExists) {
			sqld.insertIntoClasses(car);
			backcaller.notify(NotificationType.CLASS_INSERTED, clzName);
		}
	}

	/**
	 * checks if there is an entry in the DB for the parent class and takes the
	 * interfaces of the parent if available
	 * 
	 * @param car
	 * @return true if sth has changed
	 * @throws SQLException
	 */
	private void checkChildClasses (ClazzAnalysisResult car) throws SQLException
	{
		ClazzAnalysisResult[] childCars = sqld.getMyChildClasses(car.getClzName ());

		for (ClazzAnalysisResult cc : childCars) {
			/**
			 * wir berechnen den hashcode um zu sehen ob sich was verändert hat
			 */
			int hc = cc.hashCode();
			
			cc.inherit(car);
			int nhc = cc.hashCode();
			
			if (hc != nhc) {
				// die Vererbung der Attribute
				// auf die Kind-klasse hat zu Veränderung geführt
				sqld.updateClazzInterfaces(cc);
				backcaller.notify(NotificationType.CLASS_UPDATED, cc.getClzName());
			}
		}
	}

	private static boolean isSerialVersionUID(AddFieldEvent ev) {
		return (ev.getName().equals("serialVersionUID") && ev.getAccess() == (ACC_FINAL | ACC_PRIVATE | ACC_STATIC)
				&& ev.getSignature().equals("J"));
	}

	private static boolean isReadObject(AddMethodEvent ev) {
		return (ev.getName().equals("readObject") && ev.getAccess() == (ACC_PRIVATE)
				&& ev.getSignature().equals("(Ljava/io/ObjectInputStream;)V"));
	}

	private static boolean isEquals(AddMethodEvent ev) {
		return (ev.getName().equals("equals") && ev.getAccess() == (ACC_PUBLIC)
				&& ev.getSignature().equals("(Ljava/lang/Object;)Z"));
	}

	private static boolean isHashCode(AddMethodEvent ev) {
		return (ev.getName().equals("hashCode") && ev.getAccess() == (ACC_PUBLIC)
				&& ev.getSignature().equals("(Ljava/lang/Object;)I"));
	}

	private static boolean isCompareTo(AddMethodEvent ev) {
		return (ev.getName().equals("compareTo") && ev.getAccess() == (ACC_PUBLIC)
				&& ev.getSignature().equals("(Ljava/lang/Object;)Z"));
	}

	private static boolean isFinalize(AddMethodEvent ev) {
		return (ev.getName().equals("finalize") && ev.getAccess() == (ACC_PUBLIC) && ev.getSignature().equals("()V"));
	}
}
