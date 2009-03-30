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
 */
public class Bug2721955 {
	// 1
	public static final String UNUSED_1 = "1";
	// 2
	public static final String UNUSED_2 = "2";
	// 3
	public static final String UNUSED_3 = "2";
	// 4
	public static final String UNUSED_4 = "2";
	// ------------------------------------------------------------------------
	// PRIVATE
	// ------------------------------------------------------------------------
	// 2
	private static final String UNUSED_PRIVATE_1 = "1";
	// 2
	private static final String UNUSED_PRIVATE_2 = "1";
	// 3
	private static final String UNUSED_PRIVATE_3 = "1";
	// 4
	private static final String UNUSED_PRIVATE_4 = "1";
}
