package org.ucdetector.example;

/**
 * UnusedClassExample
 */
public class UnusedClassExample {// Marker YES
	/**
	 * javadoc
	 */
	//
	public static final String UNUSED = "UNUSED";// no marker: skipped

	public static String MAY_BE_FINAL = "USE_FINAL";// Marker YES (final)

	/**
	 * javadoc
	 */
	public static String unused() {// no marker: skipped
		return "hello";
	}
}
