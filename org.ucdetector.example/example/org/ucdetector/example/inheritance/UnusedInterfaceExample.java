package org.ucdetector.example.inheritance;
// interfaces can't be private
public interface UnusedInterfaceExample {

  // interface methods can't be private
  public static final String CLASS_NAME = UnusedInterfaceExample.class.getName();  // Marker YES: unused code
}
