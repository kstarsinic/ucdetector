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
import org.eclipse.jdt.core.JavaModelException;
import org.ucdetector.Log;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;
import org.ucdetector.util.MarkerFactory.ElementType;

/**
 * Create a marker
 */
public class MarkerReport implements IUCDetectorReport {
  private static final String JAVA_ELEMENT_SEPARATOR_MARKER = ",";
  /**
   * Don't create each marker. Do a batch creation instead
   */
  private static final int MARKERS_TO_CREATE_COUNT = 10;
  private final List<ReportParam> markersToCreate = new ArrayList<ReportParam>();

  public void reportMarker(ReportParam reportParam) throws CoreException {
    if (Log.DEBUG) {
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
    if (Log.DEBUG) {
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
    String elementString = createJavaElementString(reportParam.javaElement);
    marker.setAttribute(MarkerFactory.ELEMENT_TYPE_ATTRIBUTE, elementString);
  }

  public static ElementTypeAndName getElementTypeAndName(IMarker marker) {
    String attribute = marker.getAttribute(
        MarkerFactory.ELEMENT_TYPE_ATTRIBUTE, "?");
    String[] resultArray = attribute
        .split(MarkerReport.JAVA_ELEMENT_SEPARATOR_MARKER); //$NON-NLS-1$
    ElementTypeAndName result = new ElementTypeAndName();
    result.elementType = (resultArray.length > 0 ? ElementType
        .valueOf(resultArray[0]) : null);
    result.name = (resultArray.length > 1 ? resultArray[1] : null);
    return result;
  }

  /**
   * Data container, containing a elementType
   * @see MarkerReport#createJavaElementString()
   */
  // TODO 2009-02-20: UCD tells to use default visibility. But compile error
  public static class ElementTypeAndName {
    public ElementType elementType;
    protected String name;
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
  }
}
