/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.ucdetector.Log;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;
import org.ucdetector.util.MarkerFactory.ElementType;

/**
 * Create a marker
 */
public class MarkerReport implements IUCDetectorReport {
  private static final String JAVA_ELEMENT_SEPARATOR_MARKER = ","; //$NON-NLS-1$
  /**
   * Don't create each marker. Do a batch creation instead
   */
  private static final int MARKERS_FLASH_LIMIT = 10;
  private final List<ReportParam> markersToFlash = new ArrayList<ReportParam>();
  private int totalMarkerCount = 0;

  public boolean reportMarker(ReportParam reportParam) throws CoreException {
    if (Log.DEBUG) {
      Log.logDebug("    Add to queue: " + reportParam); //$NON-NLS-1$
    }
    markersToFlash.add(reportParam);
    totalMarkerCount++;
    // Waiting for first 10 markers - ID: 2787576
    // Flush all markers at the begin, so users can start using UCDetector results
    if (totalMarkerCount < MARKERS_FLASH_LIMIT
        || markersToFlash.size() >= MARKERS_FLASH_LIMIT) {
      flushReport();
    }
    return true;
  }

  /**
   * Create markers and clean cache;
   */
  private void flushReport() throws CoreException {
    if (Log.DEBUG) {
      Log.logDebug(String.format(" FlushMarkers created %s markers", //$NON-NLS-1$
          Integer.valueOf(markersToFlash.size())));
    }
    for (ReportParam reportParamToCreate : markersToFlash) {
      createMarker(reportParamToCreate);
    }
    markersToFlash.clear();
  }

  private void createMarker(ReportParam reportParam) throws CoreException {
    int severity;
    switch (reportParam.getLevel()) {
      case ERROR:
        severity = IMarker.SEVERITY_ERROR;
        break;
      case WARNING:
        severity = IMarker.SEVERITY_WARNING;
        break;
      default:
        return;
    }
    IMarker marker = reportParam.getJavaElement().getResource().createMarker(
        reportParam.getMarkerType());
    marker.setAttribute(IMarker.SEVERITY, severity);
    marker.setAttribute(IMarker.MESSAGE, reportParam.getMessage());
    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
    marker.setAttribute(IMarker.LINE_NUMBER, reportParam.getLine());
    String elementString = createJavaElementString(reportParam.getJavaElement());
    marker.setAttribute(MarkerFactory.ELEMENT_TYPE_ATTRIBUTE, elementString);
  }

  public static ElementTypeAndName getElementTypeAndName(IMarker marker) {
    String attribute = marker.getAttribute(
        MarkerFactory.ELEMENT_TYPE_ATTRIBUTE, "?"); //$NON-NLS-1$
    String[] resultArray = attribute
        .split(MarkerReport.JAVA_ELEMENT_SEPARATOR_MARKER);
    ElementTypeAndName result = new ElementTypeAndName();
    result.elementType = (resultArray.length > 0 ? ElementType
        .valueOf(resultArray[0]) : null);
    result.elementName = (resultArray.length > 1 ? resultArray[1] : null);
    return result;
  }

  /**
   * Data container, containing a elementType
   * See:  {@link MarkerReport#createJavaElementString(IJavaElement)}
   */
  // TODO 2009-02-20: UCD tells to use default visibility. But compile error
  @SuppressWarnings("ucd")
  public static class ElementTypeAndName {
    public ElementType elementType;
    public String elementName;
  }

  /**
   * @return a String for a class, method or field like:
   * <ul>
   * <li>"type,MyClass"</li>
   * <li>"method,calculate"</li>
   * <li>"field,value"</li>
   * <li>"constant,MAX_VALUE"</li>
   * </ul>
   *  NOTE: This string is used in other classes!
   */
  private static String createJavaElementString(IJavaElement javaElement)
      throws JavaModelException {
    StringBuilder sb = new StringBuilder();
    if (javaElement instanceof IType) {
      sb.append(MarkerFactory.ElementType.TYPE);
    }
    else if (javaElement instanceof IMethod) {
      sb.append(MarkerFactory.ElementType.METHOD);
    }
    else if (javaElement instanceof IField) {
      boolean isConstant = JavaElementUtil.isConstant((IField) javaElement);
      sb.append(isConstant ? MarkerFactory.ElementType.CONSTANT
          : MarkerFactory.ElementType.FIELD);
    }
    sb.append(JAVA_ELEMENT_SEPARATOR_MARKER).append(
        javaElement.getElementName());
    return sb.toString();
  }

  public void endReport(Object[] selected, long start) throws CoreException {
    flushReport();
    Log.logInfo(totalMarkerCount + " markers created"); //$NON-NLS-1$
  }

  public void reportDetectionProblem(IStatus status) {
    // 
  }
}
