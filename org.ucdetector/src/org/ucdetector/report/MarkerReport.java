/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.ucdetector.preferences.WarnLevel;
import org.ucdetector.util.MarkerFactory;

/**
 *
 */
public class MarkerReport implements IUCDetctorReport {

  public void reportMarker(IJavaElement javaElement, String message, int line,
      WarnLevel level, String markerType, String problem) throws CoreException {
    IMarker marker = javaElement.getResource().createMarker(markerType);
    marker.setAttribute(IMarker.MESSAGE, message);
    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
    switch (level) {
      case ERROR:
        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        break;
      case WARNING:
        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
        break;
      case IGNORE:
        // marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
        break;
    }
    marker.setAttribute(IMarker.LINE_NUMBER, line);
    // additional info, not used at the moment
    marker.setAttribute(MarkerFactory.PROBLEM, problem);
    String elementString = getJavaElementString(javaElement);
    marker.setAttribute(MarkerFactory.JAVA_ELEMENT_ATTRIBUTE, elementString);
  }

  /**
   * @return a nice String for a class, method or field like:
   * MyClass.myMethod(String, int)
   */
  private static String getJavaElementString(IJavaElement javaElement) {
    StringBuilder sb = new StringBuilder();
    if (javaElement instanceof IType) {
      sb.append(MarkerFactory.JAVA_ELEMENT_TYPE);
    }
    else if (javaElement instanceof IMethod) {
      sb.append(MarkerFactory.JAVA_ELEMENT_METHOD);
    }
    else if (javaElement instanceof IField) {
      sb.append(MarkerFactory.JAVA_ELEMENT_FIELD);
    }
    sb.append(',').append(javaElement.getElementName());
    if (javaElement instanceof IMethod) {
      IMethod method = (IMethod) javaElement;
      String[] parameterTypes = method.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++) {
        sb.append(',');
        sb.append(parameterTypes[i]);
      }
    }
    return sb.toString();
  }

  public void endReport(Object[] selected, long start) {

  }
}
