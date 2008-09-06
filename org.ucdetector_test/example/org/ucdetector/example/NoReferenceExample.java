package org.ucdetector.example;

/**
 * 
 */
class NoReferenceExample {// Marker YES
  public static final String NO_REFERENCE = "NO_REFERENCE"; // no marker here, because we skip!

  public void doSomething() {// no marker here, because we skip!
  };

  @SuppressWarnings("unused")
  private int age; // Marker YES - final 

  public String name = "UCDetector";// Marker YES - final 

}
