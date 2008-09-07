package org.ucdetector.example;

/**
 * javadoc
 */
public class InterfaceImplExample extends AbstractInterfaceImplExample {

	/**
	 * javadoc
	 */
	public String overridenMethod() {
		return "overridenMethod";
	}

	protected String overrideMe() { // Marker YES: unused code
		return "overrideMe";
	}

	@Override
	protected void noMarkerForAbstractMethod() {

	}

	public void unusedMethod() {

	}
}
