package org.ucdetector.example;

import org.ucdetector.example.classes.AnnotationExample;

public class NoUcdAnnotationExample {

	@SuppressWarnings( { "NO_UCD", "unused" })
	/** javadoc 1 */
	// comment 1
	private final static String UNUSED_WITH_SINGLE_MEMBER_ANNOTATION = "1";

	/** javadoc 1 */
	@SuppressWarnings( { "NO_UCD", "unused" })
	// comment 1
	private final String unusedMember = "1";

	// comment 2
	/** javadoc 1 */
	@java.lang.SuppressWarnings("NO_UCD")
	public void unused() {

	}

	@AnnotationExample(parameterExmaple = "1")
	@SuppressWarnings("NO_UCD")
	public static final String UNUSED_WITH_NORMAL_ANNOTATION = "2";

	// @SuppressWarnings("NO_UCD")
	public static final String UNUSED_2 = "3"; // Marker YES: unused code

	@SuppressWarnings("hello")
	public static final String UNUSED_3 = "3"; // Marker YES: unused code

}
