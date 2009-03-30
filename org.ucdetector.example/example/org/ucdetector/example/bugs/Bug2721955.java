package org.ucdetector.example.bugs;

/**
 * i had two constants, both with UCD Marker QuickFix "Delete Code"
 * 
 * <pre>
 * public static final String CON1 = &quot;1&quot;;
 * public static final String CON2 = &quot;2&quot;;
 * </pre>
 * 
 * I ran the quick fix on the first one and the both UDC Marker was deleted. The
 * code was only changed for the first quick fix.
 * <p>
 * Deleting markers, also deleted next marker, when there was no comment or
 * other stuff in between
 * <p>
 * This class is used to test Quickfix!
 */
public class Bug2721955 {
	// ------------------------------------------------------------------------
	// FIELDS
	// ------------------------------------------------------------------------
	public static final String UNUSED_1 = "1";// Marker YES: unused code
	public static final String UNUSED_2 = "2";// Marker YES: unused code
	public static final String UNUSED_3 = "2";// Marker YES: unused code
	public static final String UNUSED_4 = "2";// Marker YES: unused code
	// ------------------------------------------------------------------------
	// FIELDS WITH COMMENTS
	// ------------------------------------------------------------------------
	// 1
	public static final String UNUSED_5 = "1";// Marker YES: unused code
	// 2
	public static final String UNUSED_6 = "1";// Marker YES: unused code
	// 3
	public static final String UNUSED_7 = "1";// Marker YES: unused code
	// 4
	public static final String UNUSED_8 = "1";// Marker YES: unused code

	// ------------------------------------------------------------------------
	// METHODS
	// ------------------------------------------------------------------------
	public void unused_1() {// Marker YES: unused code
	}

	public void unused_2() {// Marker YES: unused code
	}

	public void unused_3() {// Marker YES: unused code
	}

	public void unused_4() {// Marker YES: unused code
	}

	// ------------------------------------------------------------------------
	// METHODS WITH COMMENTS
	// ------------------------------------------------------------------------
	public void unused_5() {// Marker YES: unused code
	}

	// comment
	public void unused_6() {// Marker YES: unused code
	}

	// comment
	public void unused_7() {// Marker YES: unused code
	}

	// comment
	public void unused_8() {// Marker YES: unused code
	}
	// comment
}
