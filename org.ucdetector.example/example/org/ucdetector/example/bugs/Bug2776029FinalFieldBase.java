package org.ucdetector.example.bugs;

/**
 * Bug 2776029: Final for fields initialized in subclass constructors
 * <p>
 * Submitted: Nobody/Anonymous ( nobody ) - 2009-04-20 12:56
 * <p>
 * Hello,<br>
 * it seems that UCDetector add a final marker for fields that are Initialized
 * only in subclass constructor. In fact, the marker shouldn't not appear
 * because the final keyword imply initialization in the same class
 * constructor(s).
 * <p>
 * 
 * @see "https://sourceforge.net/tracker/?func=detail&aid=2776029&group_id=219599&atid=1046865"
 */
public class Bug2776029FinalFieldBase {
	private int fieldBaseLocal; // Marker YES: use final

	/** Bug 2776029: No final marker should be here! */
	int initializedBySuper;

	/** Bug 2776029: No final marker should be here! */
	int initializedBySuperAndLocal;

	public Bug2776029FinalFieldBase() {
		fieldBaseLocal = 2;
		initializedBySuperAndLocal = 2;
		System.out.println(fieldBaseLocal);
		System.out.println(initializedBySuper);
		System.out.println(initializedBySuperAndLocal);
	}
}
