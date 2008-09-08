package org.ucdetector.example.methods;

import java.io.Serializable;

/**
 * No markers in overridden methods of Object, 
 * methods of Object should be skipped during search!
 */
public class ObjectClassMethodsExample implements Serializable {
  static final long serialVersionUID = -629941578936022146L;

  public void unused() { // Marker YES: unused code

  }

  //	private static final long serialVersionUID = 1L;
  // ------------------------------------------------------------------------
  // NO OBJECT
  // ------------------------------------------------------------------------
  public int hashCode() {
    return 0;
  }

  public Object clone() {
    return new Object();
  }

  public String toString() {
    return "hello";
  }

  public void finalize() {
  }

  public boolean equals(Object o) {
    return super.equals(o);
  }

  // ------------------------------------------------------------------------
  // NO OBJECT
  // ------------------------------------------------------------------------
  public int hashCode(String s) { // Marker YES: unused code
    return 0;
  }

  public Object clone(String s) { // Marker YES: unused code
    return new Object();
  }

  public String toString(String s) { // Marker YES: unused code
    return "hello";
  }

  public void finalize(String s) { // Marker YES: unused code
  }

  public boolean equals(String s, Object o) { // Marker YES: unused code
    return super.equals(o);
  }
}
