package org.ucdetector.example.methods;
/**
 */
public class ConstructorExample {
  /** we ignore default constructors! */
  public ConstructorExample() {
  }

  /** we dont't ignore constructors with parameter! */
  public ConstructorExample(String parameter) { // Marker YES: unused code
  }

  
  public ConstructorExample(int i,String parameter) { // Marker YES: unused code
  }
}
