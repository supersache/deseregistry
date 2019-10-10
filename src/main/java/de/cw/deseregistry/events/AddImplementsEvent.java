package de.cw.deseregistry.events;

/**
 * Dieses Event bildet die Kante Class <-> implemented Interface ab
 *
 */
public class AddImplementsEvent extends Event {

	private Class<?> addedInterface;

	private AddClassEvent parent;

	/**
	 * Handles the case like String implements CharSequence
	 * @param clazz the implemented interface, CharSequence in the example above
	 * @param parent the implementing class, String in the example above
	 */
	public AddImplementsEvent(Class<?> _interface, AddClassEvent parent) {
		this.addedInterface = _interface;
		this.parent = parent;
	}

	public AddClassEvent getParent() {
		return parent;
	}

	public Class<?> getInterface() {
		return this.addedInterface;
	}
}
