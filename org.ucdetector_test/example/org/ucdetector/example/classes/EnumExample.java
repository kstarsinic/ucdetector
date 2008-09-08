package org.ucdetector.example.classes;

public enum EnumExample {
  USED, //
  UNUSED, // Marker YES: unused code

  /**
   * it is not possible to make enum constants public, protected or private! 
   * */
  CHANGE_TO_PROTECTED, //
  /**
   * it is not possible to make enum constants public, protected or private! 
   * */
  CHANGE_TO_PRIVATE;

  public String toString() {
    return CHANGE_TO_PRIVATE.name();
  }
}
