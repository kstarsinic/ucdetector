package org.ucdetector.example.impl;

import org.ucdetector.example.ReferencedByTestsExample;

public class JUnitReferenceHolder {

  /**
   * This IS a JUnit test method
   */
  public void testHoldReference() {
    ReferencedByTestsExample example = new ReferencedByTestsExample();
    example.referencedByTestMethod();
    example.referencedByTestClassAndNormalClass();
  }

  /**
   * This is NOT a JUnit test method
   */
  public static void testHoldReferenceStatic() {
    ReferencedByTestsExample example = new ReferencedByTestsExample();
    example.referencedByWrongTestMethodStatic();
  }

  /**
   * This is NOT a JUnit test method
   */
  public void testHoldReference(int i) {
    ReferencedByTestsExample example = new ReferencedByTestsExample();
    example.referencedByWrongTestMethod();
  }

}
