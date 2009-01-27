package org.ucdetector.example.bugs.impl;
/**
 * Bug: Bug2539795Field may not be default, because Bug2539795Field.getBar()
 * is used outside of this package! 
 */
public class Bug2539795Field {
  public String getBar() {
    return "Bar";
  }
}
