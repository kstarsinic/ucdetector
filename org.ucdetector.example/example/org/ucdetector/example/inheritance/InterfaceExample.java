package org.ucdetector.example.inheritance;

public interface InterfaceExample {
  // -------------------------------------------------------------------------
  // UNUSED
  // -------------------------------------------------------------------------
  public static final String UNUSED = "UNUSED"; // Marker YES: unused code
  /**
   * Due to the Java Language Spec members in interfaces can't be private.
   *   Therefore UCDetector should not propose to set constants to private, when
   *   these constants are only used within the same interface (e.g. when used to
   *   calculate other constant values).
   *   ACTUAL BEHAVIOR
   * 
   *   UCDetector proposes to make constants in interfaces private. This will
   *   produce invalid Java source.
   * @see https://sourceforge.net/tracker/index.php?func=detail&aid=2269486&group_id=219599&atid=1046865
   */
  public static final String BUG_2269486 = "USED";

  public static final String UNUSED_2 = "UNUSED_2" + BUG_2269486; // Marker YES: unused code

  // -------------------------------------------------------------------------
  // USED
  // -------------------------------------------------------------------------
  public static final String USED = "USED";

  /**
   * Fix Bug [ 2153699 ] Find unused interface methods
   */
  void unusedMethod(); // Marker YES: unused code

  /**
   * Fix Bug [ 2153699 ] Find unused interface methods
   */
  String overridenMethod(); // Marker YES: unused code

  public void useProtected();

}
