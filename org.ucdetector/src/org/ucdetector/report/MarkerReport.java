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
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.MarkerFactory;

/**
 *
 */
public class MarkerReport implements IUCDetectorReport {
  /**
   * Don't create one marker each, create number of markers declared here
   * each time!
   */
  private static final int MARKERS_TO_CREATE_COUNT = 10;
  private final List<ReportParam> markersToCreate = new ArrayList<ReportParam>();

  public void reportMarker(ReportParam reportParam) throws CoreException {
    if (UCDetectorPlugin.DEBUG) {
      Log.logDebug("    Add to queue: " + reportParam); //$NON-NLS-1$
    }
    markersToCreate.add(reportParam);
    if (markersToCreate.size() >= MARKERS_TO_CREATE_COUNT) {
      flushReport();
    }
  }

  /**
   * Create markers and clean cache;
   */
  private void flushReport() throws CoreException {
    if (UCDetectorPlugin.DEBUG) {
      Log.logDebug(" FlushMarkers: Create : " + markersToCreate.size() //$NON-NLS-1$
          + " markers"); //$NON-NLS-1$
    }
    for (ReportParam reportParamToCreate : markersToCreate) {
      createMarker(reportParamToCreate);
    }
    markersToCreate.clear();
  }

  private void createMarker(ReportParam reportParam) throws CoreException {
    int severity;
    switch (reportParam.level) {
      case ERROR:
        severity = IMarker.SEVERITY_ERROR;
        break;
      case WARNING:
        severity = IMarker.SEVERITY_WARNING;
        break;
      default:
        return;
    }
    IMarker marker = reportParam.javaElement.getResource().createMarker(
        reportParam.markerType);
    marker.setAttribute(IMarker.SEVERITY, severity);
    marker.setAttribute(IMarker.MESSAGE, reportParam.message);
    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
    marker.setAttribute(IMarker.LINE_NUMBER, reportParam.line);
    String elementString = getJavaElementString(reportParam.javaElement);
    marker.setAttribute(MarkerFactory.JAVA_ELEMENT_ATTRIBUTE, elementString);
  }

  /**
   * @return a nice String for a class, method or field like:
   * type,MyClass
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
    return sb.toString();
  }

  public void endReport(Object[] selected, long start) throws CoreException {
    flushReport();
  }
}
