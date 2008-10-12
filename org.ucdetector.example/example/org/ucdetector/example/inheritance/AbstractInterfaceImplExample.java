package org.ucdetector.example.inheritance;

/**
 * no markers here
 */
abstract class AbstractInterfaceImplExample implements InterfaceExample {

  AbstractInterfaceImplExample(int i) {
    abstractMethodUsed();
  }

  protected abstract void abstractMethod(); // Marker YES: unused code

  public abstract void abstractMethodUsed();

}
