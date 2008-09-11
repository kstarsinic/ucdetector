package org.ucdetector.example.inheritance.bug;
/**
 *
 */
public class B extends A {
  public void foo() {
  }

  public void barB() { // Marker YES: use protected
    foo();
  }
}
