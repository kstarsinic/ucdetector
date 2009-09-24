package org.ucdetector.example.impl;

import org.ucdetector.example.MixedExample;
import org.ucdetector.example.ReferencedByTestsExample;

public class ReferenceHolderInTestSourceFolder {
  public static void main(String[] args) {
    ReferencedByTestsExample example = new ReferencedByTestsExample();
    example.referencedByTestSourceFolder();

    MixedExample.usedOnlyByTests();
    MixedExample.usedOnlyByTests();
  }
}
