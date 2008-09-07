package org.ucdetector.example;

/**
 * 
 */
class NoReferenceExample {// Marker YES: unused code
  public static final String NO_REFERENCE = "NO_REFERENCE"; // no marker here, because we skip!

  public void doSomething() {// no marker here, because we skip!
  };

  @SuppressWarnings("unused")
  private int age; // Marker YES: use final 

  public String name = "UCDetector";// Marker YES: use final

}
