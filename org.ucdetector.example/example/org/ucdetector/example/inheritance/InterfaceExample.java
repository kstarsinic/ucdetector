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
  void overriddenButUnusedMethod(); // Marker YES: unused code
  
  // causes compile error in InterfaceImplExample
  //void unusedMethodNotOverridden();
  

  public void useProtected();

}
