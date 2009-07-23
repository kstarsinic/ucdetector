package org.ucdetector.example.inheritance;

/**
 * javadoc
 */
public class InterfaceImplExample extends AbstractInterfaceImplExample {

	public InterfaceImplExample() {
		this(0);
	}

	// no final markers here!!
	InterfaceImplExample(int i) { // Marker YES: use private
		super(i);
	}

	/**
	 * javadoc
	 */
	public void overriddenButUnusedMethod() {
		useProtected();
	}

	protected String overrideMe() { // Marker YES: unused code
		return "overrideMe";
	}

	@Override
	protected void abstractMethod() {

	}

	@Override
	public void abstractMethodUsed() {

	}

	public void useProtected() {
	}
}
