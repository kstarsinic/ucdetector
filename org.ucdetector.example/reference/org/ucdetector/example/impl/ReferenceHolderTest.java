package org.ucdetector.example.impl;

import org.ucdetector.example.ReferencedByTestsExample;

public class ReferenceHolderTest {

  public static void main(String[] args) {
    ReferencedByTestsExample example = new ReferencedByTestsExample();
    example.referencedByTestClass();
    example.referencedByTestClassAndNormalClass();
  }
}
