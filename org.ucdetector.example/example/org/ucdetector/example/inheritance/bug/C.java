package org.ucdetector.example.inheritance.bug;
/**
 *
 */
public class C extends B {
  public void foo() {
    barC();
  }

  public void barC() { // Marker YES: use private
    foo();
    barB();
  }
}
