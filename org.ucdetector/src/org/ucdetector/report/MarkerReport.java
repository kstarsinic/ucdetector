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
import org.ucdetector.preferences.WarnLevel;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;

/**
 * Create a marker
 */
public class MarkerReport implements IUCDetectorReport {
  /** Don't create each marker. Do a batch creation instead */
  private static final int MARKERS_FLASH_LIMIT = 10;
  private final List<ReportParam> markersToFlash = new ArrayList<ReportParam>();
  private int totalMarkerCount = 0;

  private void createMarker(ReportParam reportParam) throws CoreException {
    WarnLevel level = reportParam.getLevel();
    if (level == WarnLevel.IGNORE) {
      return;
    }
    int severity = (level == WarnLevel.ERROR) ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING;
    IMember javaElement = reportParam.getJavaElement();
    ISourceRange range = javaElement.getNameRange();
    IMarker marker = javaElement.getResource().createMarker(reportParam.getMarkerType());
    marker.setAttribute(IMarker.SEVERITY, severity);
    marker.setAttribute(IMarker.MESSAGE, reportParam.getMessage());
    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
    // LINE_NUMBER Still needed for MultiQuickFix dialog
    marker.setAttribute(IMarker.LINE_NUMBER, reportParam.getLine());
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

  /**
   * See also concrete classes of: org.eclipse.jdt.core.dom.BodyDeclaration<br>
   * See also org.ucdetector.quickfix.AbstractUCDQuickFix.getModifierListRewrite()<br>
   */
  private static enum ElementType {
    TYPE, ANNOTATION, ENUM, // types
    METHOD, ANNOTATION_TYPE_MEMBER, // methods
    FIELD, ENUM_CONSTANT, /*ANNOTATION_TYPE_MEMBER*/// fields
  }

  public boolean reportMarker(ReportParam reportParam) throws CoreException {
    if (Log.isDebug()) {
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
    if (Log.isDebug()) {
      Log.logDebug("flushReport will create %s markers", Integer.valueOf(markersToFlash.size()));//$NON-NLS-1$
    }
    for (ReportParam reportParamToCreate : markersToFlash) {
      createMarker(reportParamToCreate);
    }
    markersToFlash.clear();
  }

  public void endReport(Object[] selected, long start) throws CoreException {
    flushReport();
    Log.logInfo(totalMarkerCount + " markers created"); //$NON-NLS-1$
  }

  public void reportDetectionProblem(IStatus status) {
    // 
  }
}
