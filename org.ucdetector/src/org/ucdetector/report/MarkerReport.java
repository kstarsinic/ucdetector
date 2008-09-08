/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.ucdetector.util.MarkerFactory;

/**
 *
 */
public class MarkerReport implements IUCDetctorReport {
  /**
   * Don't create one marker each, create number of markers declared here
   * each time!
   */
  private static final int MARKERS_TO_CREATE_COUNT = 10;
  private final List<ReportParam> markersToCreate = new ArrayList<ReportParam>();

  public void reportMarker(ReportParam reportParam) throws CoreException {
    markersToCreate.add(reportParam);
    if (markersToCreate.size() >= MARKERS_TO_CREATE_COUNT) {
      flushReport();
    }
  }

  /**
   * Create markers and clean cache;
   */
  private void flushReport() throws CoreException {
    for (ReportParam reportParamToCreate : markersToCreate) {
      createMarker(reportParamToCreate);
    }
    markersToCreate.clear();
  }

  private void createMarker(ReportParam reportParam) throws CoreException {
    IMarker marker = reportParam.javaElement.getResource().createMarker(
        reportParam.markerType);
    marker.setAttribute(IMarker.MESSAGE, reportParam.message);
    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
    switch (reportParam.level) {
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
    marker.setAttribute(IMarker.LINE_NUMBER, reportParam.line);
    // additional info, not used at the moment
    marker.setAttribute(MarkerFactory.PROBLEM, reportParam.problem);
    String elementString = getJavaElementString(reportParam.javaElement);
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

  public void endReport(Object[] selected, long start) throws CoreException {
    flushReport();
  }
}
