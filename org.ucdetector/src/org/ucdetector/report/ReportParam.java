/**
 * Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.preferences.WarnLevel;
import org.ucdetector.util.MarkerFactory;

/**
 * This class contains parameters for a report (for a marker)
 */
public class ReportParam {
  private final IJavaElement javaElement;
  private final String message;
  private final int line;
  private final String markerType;
  private final WarnLevel level;
  /**  2803618  Add number of references to report */
  private final int referenceCount;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MARKER{").append(getLevel()).append(';').append(getMessage()); //$NON-NLS-1$
    IResource resource = getJavaElement().getResource();
    if (resource != null) {
      sb.append(';').append(resource.getFullPath()).append(':').append(getLine());
    }
    else {
      sb.append(";line=").append(getLine()); //$NON-NLS-1$
    }
    sb.append(';').append(getMarkerType()).append('}');
    return sb.toString();
  }

  public ReportParam(IJavaElement javaElement, String message, int line,
      String markerType) {
    this(javaElement, message, line, markerType, null, -1);
  }

  public ReportParam(IJavaElement javaElement, String message, int line,
      String markerType, int referenceCount) {
    this(javaElement, message, line, markerType, null, referenceCount);
  }

  public ReportParam(IJavaElement javaElement, String message, int line,
      String markerType, WarnLevel warnLevel) {
    this(javaElement, message, line, markerType, warnLevel, -1);
  }

  private ReportParam(IJavaElement javaElement, String message, int line,
      String markerType, WarnLevel warnLevel, int referenceCount) {
    this.javaElement = javaElement;
    this.message = message;
    this.line = line;
    this.markerType = markerType;
    this.level = warnLevel == null ? calculateWarnLevel() : warnLevel;
    this.referenceCount = referenceCount;
  }

  private WarnLevel calculateWarnLevel() {
    WarnLevel warnLevel = null;
    if (MarkerFactory.UCD_MARKER_UNUSED.equals(getMarkerType())
        || MarkerFactory.UCD_MARKER_USED_FEW.equals(getMarkerType())) {
      if (getJavaElement() instanceof IType) {
        warnLevel = Prefs.getUCDetectionInClasses();
      }
      else if (getJavaElement() instanceof IMethod) {
        warnLevel = Prefs.getUCDetectionInMethods();
      }
      else if (getJavaElement() instanceof IField) {
        warnLevel = Prefs.getUCDetectionInFields();
      }
    }
    else if (MarkerFactory.UCD_MARKER_USE_PROTECTED.equals(getMarkerType())
        || MarkerFactory.UCD_MARKER_USE_DEFAULT.equals(getMarkerType())) {
      warnLevel = Prefs.getCheckReduceVisibilityProtected(getJavaElement());
    }
    else if (MarkerFactory.UCD_MARKER_USE_PRIVATE.equals(getMarkerType())) {
      warnLevel = Prefs.getCheckReduceVisibilityToPrivate(getJavaElement());
    }
    else if (MarkerFactory.UCD_MARKER_USE_FINAL.equals(getMarkerType())) {
      if (getJavaElement() instanceof IMethod) {
        warnLevel = Prefs.getCheckUseFinalMethod();
      }
      else if (getJavaElement() instanceof IField) {
        warnLevel = Prefs.getCheckUseFinalField();
      }
    }
    return warnLevel == null ? WarnLevel.WARNING : warnLevel;
  }

  public IJavaElement getJavaElement() {
    return javaElement;
  }

  protected String getMessage() {
    return message;
  }

  public int getLine() {
    return line;
  }

  protected String getMarkerType() {
    return markerType;
  }

  protected WarnLevel getLevel() {
    return level;
  }

  protected int getReferenceCount() {
    return referenceCount;
  }
}