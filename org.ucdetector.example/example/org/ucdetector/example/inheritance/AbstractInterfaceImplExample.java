package org.ucdetector.example.inheritance;

/**
 * no markers here
 */
abstract class AbstractInterfaceImplExample implements InterfaceExample {

  AbstractInterfaceImplExample(int i) {
    abstractMethodUsed();
  }

  // no marker here, because method is overridden
  protected abstract void abstractMethod();

  public abstract void abstractMethodUsed();

}
