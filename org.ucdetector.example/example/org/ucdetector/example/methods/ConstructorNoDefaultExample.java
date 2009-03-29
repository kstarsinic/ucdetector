package org.ucdetector.example.methods;
/**
 */
public class ConstructorNoDefaultExample { 
 

  /** we dont't ignore constructors with parameter! */
  public ConstructorNoDefaultExample(String parameter) { // Marker YES: unused code
  }

  
  public ConstructorNoDefaultExample(int i,String parameter) { // Marker YES: use protected
  }
}
