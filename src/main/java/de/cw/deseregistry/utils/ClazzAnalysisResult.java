package de.cw.deseregistry.utils;

public class ClazzAnalysisResult {
	
	private String clzName = null;
	private String parentClazzName = null;
	public boolean probablySerializable = false;
	public boolean serializable = false;
	public boolean invocationHandler = false;
	public boolean implementsEquals = false;
	public boolean implementsHashCode = false;
	public boolean implementsReadObject = false;
	public boolean implementsCompareTo = false;
	public boolean implementsFinalize = false;
	public boolean comparable = false;
	
	public ClazzAnalysisResult (String name, String parent, boolean ps, boolean ser, boolean inv, boolean eq, boolean hc, boolean ro, boolean ct, boolean fin, boolean comp)
	{
		this.clzName = name; this.parentClazzName = parent;
		
		this.probablySerializable = ps;
		this.serializable = ser;
		this.invocationHandler = inv;
		this.implementsEquals = eq;
		this.implementsHashCode = hc;
		this.implementsReadObject = ro;
		this.implementsCompareTo = ct;
		this.implementsFinalize = fin;
		this.comparable = comp;
	}

	public String getClzName() {
		return clzName;
	}

	public String getParentClazzName() {
		return parentClazzName;
	}
	
	/**
	 * Inherit interface implementation from one object to another
	 */
	public void inherit (ClazzAnalysisResult car)
	{
		if (car.probablySerializable)
			this.probablySerializable = car.probablySerializable;
		if (car.serializable)
			this.serializable = car.serializable;
		if (car.invocationHandler)
			this.invocationHandler = car.invocationHandler;
		if (car.comparable)
			this.comparable = car.comparable;
	}
	
	public static int i1 (boolean b) { return b?1:0; }
	public static boolean i2 (int i) { return i==1; }
}
