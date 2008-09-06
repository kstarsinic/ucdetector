package org.ucdetector.example;
/**
 * {@link #documentedUnusedMethod()} UCDetctor should ignore
 */
public class JavaDocExampleMethod {

  void documentedUnusedMethod() {// Marker YES, because we ignore javadoc references
  }

  public void usedMethod() {

  }
}
