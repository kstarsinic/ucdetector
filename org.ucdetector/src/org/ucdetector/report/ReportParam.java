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
 *
 */
public class ReportParam {
  public final IJavaElement javaElement;
  protected final String message;
  public final int line;
  protected final String markerType;
  protected final WarnLevel level;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MARKER{").append(level).append(';').append(message); //$NON-NLS-1$
    IResource resource = javaElement.getResource();
    if (resource != null) {
      sb.append(';').append(resource.getFullPath()).append(':').append(line);
    }
    else {
      sb.append(";line=").append(line); //$NON-NLS-1$
    }
    sb.append(';').append(markerType).append('}');
    return sb.toString();
  }

  public ReportParam(IJavaElement javaElement, String message, int line,
      String markerType) {
    this(javaElement, message, line, markerType, null);
  }

  public ReportParam(IJavaElement javaElement, String message, int line,
      String markerType, WarnLevel warnLevel) {
    this.javaElement = javaElement;
    this.message = message;
    this.line = line;
    this.markerType = markerType;
    this.level = warnLevel == null ? calculateWarnLevel() : warnLevel;
  }

  private WarnLevel calculateWarnLevel() {
    WarnLevel warnLevel = null;
    if (MarkerFactory.UCD_MARKER_UNUSED.equals(markerType)
        || MarkerFactory.UCD_MARKER_USED_FEW.equals(markerType)) {
      if (javaElement instanceof IType) {
        warnLevel = Prefs.getUCDetectionInClasses();
      }
      else if (javaElement instanceof IMethod) {
        warnLevel = Prefs.getUCDetectionInMethods();
      }
      else if (javaElement instanceof IField) {
        warnLevel = Prefs.getUCDetectionInFields();
      }
    }
    else if (MarkerFactory.UCD_MARKER_USE_PROTECTED.equals(markerType)
        || MarkerFactory.UCD_MARKER_USE_DEFAULT.equals(markerType)) {
      warnLevel = Prefs.getCheckReduceVisibilityProtected(javaElement);
    }
    else if (MarkerFactory.UCD_MARKER_USE_PRIVATE.equals(markerType)) {
      warnLevel = Prefs.getCheckReduceVisibilityToPrivate(javaElement);
    }
    else if (MarkerFactory.UCD_MARKER_USE_FINAL.equals(markerType)) {
      if (javaElement instanceof IMethod) {
        warnLevel = Prefs.getCheckUseFinalMethod();
      }
      else if (javaElement instanceof IField) {
        warnLevel = Prefs.getCheckUseFinalField();
      }
    }
    return warnLevel == null ? WarnLevel.WARNING : warnLevel;
  }
}