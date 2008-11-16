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
   * NO marker! Method is implemented!
   */
  void unusedMethod();

  /**
   * NO marker! Method is implemented!
   */
  String overridenMethod();

  public void useProtected();

}
