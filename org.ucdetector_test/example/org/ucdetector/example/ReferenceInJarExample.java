package org.ucdetector.example;

import org.eclipse.jdt.core.IType;

public class ReferenceInJarExample {

  /**
   * org.eclipse.jdt.internal.ui.compare -
   * org.eclipse.jdt.ui_3.4.0.v20071212-1800.jar -
   * E:\eclipse\eclipse-3.4M4\plugins -
   * org.ucdetector.JavaStructureDiffViewer.initialSelection() (potential
   * match)
   */
  public IType getRoot() { // Marker YES
    return null;
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
