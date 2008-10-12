package org.ucdetector.example.inheritance;

public interface InterfaceExample {
  // -------------------------------------------------------------------------
  // UNUSED
  // -------------------------------------------------------------------------
  public static final String UNUSED = "UNUSED"; // Marker YES: unused code

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
