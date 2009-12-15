package org.ucdetector.example.classes;
/**
 * Bug 2900561: Compile error for: static variable
 * <p>
 * Submitted: Boomschakalacka ( boomschakalacka ) - 2009-11-19 17:29
 * <p>
 * UCDetector reports enum fields correctly as having zero references, if the
 * fields are never referenced directly. But I think this is a highly unsafe
 * report, because unlike class constants, enums can also be accessed by
 * iteration using the values() Method of the enumeration. So far, UCD
 * doesn't take this into account.
 * <p>
 * @see https://sourceforge.net/tracker/?func=detail&aid=2900561&group_id=219599&atid=1046865
 * <p>
 * Browse code at:
 * http://ucdetector.svn.sourceforge.net/viewvc/ucdetector/trunk/org.ucdetector.example/example/org/ucdetector/example/classes/EnumExampleUsedWithValues.java?view=markup
 */
public enum EnumExampleUsedWithValues {
  //TODO: make detection optional
  TEST, // Marker YES: unused code 
  TEST2, // Marker YES: unused code
}
