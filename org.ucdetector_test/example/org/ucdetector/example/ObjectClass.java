package org.ucdetector.example;

import java.io.Serializable;

/**
 * No markers in overridden methods of Object, 
 * methods of Object should be skipped during search!
 */
public class ObjectClass implements Serializable {
  static final long serialVersionUID = -629941578936022146L;

  public void unused() { // Marker YES

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
  public int hashCode(String s) { // Marker YES
    return 0;
  }

  public Object clone(String s) { // Marker YES
    return new Object();
  }

  public String toString(String s) { // Marker YES
    return "hello";
  }

  public void finalize(String s) { // Marker YES
  }

  public boolean equals(String s, Object o) { // Marker YES
    return super.equals(o);
  }
}
