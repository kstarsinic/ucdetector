/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.ucdetector.util.MarkerFactory;

/**
 * Detect naming conventions for classes/methods/fields
 */
@SuppressWarnings("nls")
public class CheckNameConventionIterator extends AdditionalIterator {
  private int typeCount;

  @Override
  protected void handleType(IType type) throws CoreException {
    String className = type.getElementName();
    if (!type.isAnonymous() && !startsWithUpper(className)) {
      createMarker(type, "Class name should start with upper case", ANALYZE_MARKER_EXAMPLE);
    }
    if (typeCount == 0) {
      System.out.println("classname ='" + className + "'");
      createMarker(type, "Example marker! For class '" + className + "'", ANALYZE_MARKER_EXAMPLE);
    }
    typeCount++;
  }

  @Override
  protected void handleField(IField field) throws CoreException {
    String fieldName = field.getElementName();
    int flags = field.getFlags();
    if (Flags.isStatic(flags) && Flags.isFinal(flags)) {
      if (!isConstantName(fieldName)) {
        createMarker(field, "Constant '" + fieldName
            + "' should use upper case or '_'", ANALYZE_MARKER_EXAMPLE);
      }
    }
    else if (field.isEnumConstant()) {
      if (!isConstantName(fieldName)) {
        createMarker(field, "Enum Constant '" + fieldName
            + "' should use upper case or '_'", ANALYZE_MARKER_EXAMPLE);
      }
    }
    else if (!Flags.isFinal(flags) && !Flags.isStatic(flags)
        && startsWithUpper(fieldName)) {
      createMarker(field, "Field '" + fieldName
          + "' should start with lower case", ANALYZE_MARKER_EXAMPLE);
    }
  }

  @Override
  protected void handleMethod(IMethod method) throws CoreException {
    if (method.isConstructor()) {
      // ignore constructor!
    }
    else if (startsWithUpper(method.getElementName())) {
      createMarker(method, "Method '" + method.getElementName()
          + "' should start with lower case", ANALYZE_MARKER_EXAMPLE);
    }
  }

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement)
      throws CoreException {
    MarkerFactory.deleteMarkers(javaElement);
  }

  @Override
  public String getJobName() {
    return "Check Name Convention Job";
  }

  // -------------------------------------------------------------------------
  // helper
  // -------------------------------------------------------------------------
  private static boolean startsWithUpper(String name) {
    if (name == null || name.length() == 0) {
      return false;
    }
    return (Character.isUpperCase(name.charAt(0)));
  }

  private static final boolean isConstantName(String name) {
    char[] chars = name.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (!Character.isUpperCase(chars[i]) && chars[i] != '_'
          && !Character.isDigit(chars[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * do a simple "report"
   */
  @Override
  public String getMessage() {
    return "Checked " + typeCount + " classes.\r\n"
        + "Created at least one example marker.\r\n" + "Check problems view!";
  }
}
