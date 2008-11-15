package org.ucdetector.example.inheritance.bug;
/**
 *
 */
public class B extends A {
  @Override
  public void foo() {
  }

  public void barB() { // Marker YES: use protected, use final
    foo();
  }
}
