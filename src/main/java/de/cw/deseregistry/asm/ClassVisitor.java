package de.cw.deseregistry.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import de.cw.deseregistry.events.AddClassEvent;
import de.cw.deseregistry.events.AddFieldEvent;
import de.cw.deseregistry.events.AddMethodEvent;
import de.cw.deseregistry.events.Event;

public class ClassVisitor implements org.objectweb.asm.ClassVisitor {

	private List<Event> l = new ArrayList<Event>();

	public List<Event> getEvents() {
		return l;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String supername, String[] interfaces) {
		l.add (new AddClassEvent (access, name, supername, interfaces));
	}

	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void visitAttribute(Attribute arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		l.add (new AddFieldEvent (access, name, desc));
		return null;
	}

	@Override
	public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		l.add (new AddMethodEvent (access, name, desc, exceptions));
		return null;
	}

	@Override
	public void visitOuterClass(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitSource(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

}
