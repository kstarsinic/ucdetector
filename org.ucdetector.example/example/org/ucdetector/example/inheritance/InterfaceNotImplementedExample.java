package org.ucdetector.example.inheritance;
public interface InterfaceNotImplementedExample {

  // interface fields can't be private
  public static final String CLASS_NAME = InterfaceNotImplementedExample.class // Marker YES: unused code
      .getName();

  /**
   * Fix Bug [ 2153699 ] Find unused interface methods
   */
  public void unusedMethod(); // Marker YES: unused code
}
