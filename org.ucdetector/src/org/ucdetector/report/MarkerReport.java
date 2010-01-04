/**
 * Copyright (c) 2010 Joerg Spieler
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
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
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
    if (totalMarkerCount < MARKERS_FLASH_LIMIT || markersToFlash.size() >= MARKERS_FLASH_LIMIT) {
      flushReport();
    }
    return true;
  }

  /**
   * Create markers and clean cache;
   */
  private void flushReport() throws CoreException {
    if (Log.DEBUG) {
      Log.logDebug(String.format("flushReport will create %s markers", Integer.valueOf(markersToFlash.size())));//$NON-NLS-1$
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
    IMember javaElement = reportParam.getJavaElement();
    ISourceRange range = javaElement.getNameRange();
    IMarker marker = javaElement.getResource().createMarker(reportParam.getMarkerType());
    marker.setAttribute(IMarker.SEVERITY, severity);
    marker.setAttribute(IMarker.MESSAGE, reportParam.getMessage());
    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
    //    marker.setAttribute(IMarker.LINE_NUMBER, reportParam.getLine());
    marker.setAttribute(IMarker.CHAR_START, range.getOffset());
    marker.setAttribute(IMarker.CHAR_END, range.getOffset() + range.getLength());
    marker.setAttribute(MarkerFactory.JAVA_NAME, javaElement.getElementName());
    marker.setAttribute(MarkerFactory.JAVA_TYPE, String.valueOf(getElementType(javaElement)));
  }

  /**
   * @return a ElementType based on javaElement
   */
  private static ElementType getElementType(IJavaElement javaElement) throws JavaModelException {
    if (javaElement instanceof IType) {
      IType type = (IType) javaElement;
      return (type.isEnum() ? ElementType.ENUM : type.isAnnotation() ? ElementType.ANNOTATION : ElementType.TYPE);
    }
    else if (javaElement instanceof IMethod) {
      IType type = JavaElementUtil.getTypeFor(javaElement, false);
      return (type.isAnnotation() ? ElementType.ANNOTATION_TYPE_MEMBER : ElementType.METHOD);
    }
    else if (javaElement instanceof IField) {
      IField field = (IField) javaElement;
      return (field.isEnumConstant() ? ElementType.ENUM_CONSTANT : ElementType.FIELD);
    }
    return null;
  }

  public void endReport(Object[] selected, long start) throws CoreException {
    flushReport();
    Log.logInfo(totalMarkerCount + " markers created"); //$NON-NLS-1$
  }

  public void reportDetectionProblem(IStatus status) {
    // 
  }
}
