package org.ucdetector.example.methods;

import org.eclipse.jdt.core.IType;

public class MethodReferenceInJarExample {

  /**
   * This method will be ignored, because it is a BEAM MEHTOD!!
   */
  public IType getRoot() {
    return null;
  }

  /**
   * Potential matches in eclipse.jars must be ignored!
   */
  public String getLabel(Object object) { // Marker YES: unused code
    return "";
  }

  /**
   * There are many references found in jars,
   * but this method is skipped, because it is a method from 
   * <code>java.lang.Object</code>
   */
  @Override
  public String toString() {
    return super.toString();
  }
}
