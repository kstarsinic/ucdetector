/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import static org.ucdetector.util.MarkerFactory.UCD_MARKER_TYPE_UNUSED;
import static org.ucdetector.util.MarkerFactory.UCD_MARKER_TYPE_USED_FEW;
import static org.ucdetector.util.MarkerFactory.UCD_MARKER_TYPE_USE_DEFAULT;
import static org.ucdetector.util.MarkerFactory.UCD_MARKER_TYPE_USE_FINAL;
import static org.ucdetector.util.MarkerFactory.UCD_MARKER_TYPE_USE_PRIVATE;
import static org.ucdetector.util.MarkerFactory.UCD_MARKER_TYPE_USE_PROTECTED;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.preferences.WarnLevel;
import org.ucdetector.search.LineManger;

/**
 * This class contains parameters for a report (for a marker)
 * <p>
 * @author Joerg Spieler
 * @since 2008-098-09
 */
public class ReportParam {
  private final IMember javaElement;
  private final String message;
  private final int line;
  private final String markerType;
  private final WarnLevel level;
  /**  2803618  Add number of references to report */
  private final int referenceCount;
  private final String author;
  //
  public static LineManger lineManager = null; // Hack :-(
  private final int lineEnd;
  private final int lineStart;

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
    sb.append(';').append(markerType).append('}');
    return sb.toString();
  }

  public ReportParam(IMember javaElement, String message, int line, String markerType) {
    this(javaElement, message, line, markerType, null, -1);
  }

  public ReportParam(IMember javaElement, String message, int line, String markerType, int referenceCount) {
    this(javaElement, message, line, markerType, null, referenceCount);
  }

  public ReportParam(IMember javaElement, String message, int line, String markerType, WarnLevel warnLevel) {
    this(javaElement, message, line, markerType, warnLevel, -1);
  }

  private ReportParam(IMember javaElement, String message, int line, String markerType, WarnLevel warnLevel,
      int referenceCount) {
    this.javaElement = javaElement;
    this.message = message;
    this.line = line;
    this.markerType = markerType;
    this.level = warnLevel == null ? calculateWarnLevel() : warnLevel;
    this.referenceCount = referenceCount;
    this.author = LineManger.getAuthor(javaElement);
    this.lineStart = lineManager == null ? LineManger.LINE_NOT_FOUND : lineManager.getLineStart(javaElement);
    this.lineEnd = lineManager == null ? LineManger.LINE_NOT_FOUND : lineManager.getLineEnd(javaElement);
  }

  private WarnLevel calculateWarnLevel() {
    WarnLevel warnLevel = null;
    if (UCD_MARKER_TYPE_UNUSED.equals(markerType) || UCD_MARKER_TYPE_USED_FEW.equals(markerType)) {
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
    else if (UCD_MARKER_TYPE_USE_PROTECTED.equals(markerType) || UCD_MARKER_TYPE_USE_DEFAULT.equals(markerType)) {
      warnLevel = Prefs.getVisibilityProtectedCheck(getJavaElement());
    }
    else if (UCD_MARKER_TYPE_USE_PRIVATE.equals(markerType)) {
      warnLevel = Prefs.getVisibilityPrivateCheck(getJavaElement());
    }
    else if (UCD_MARKER_TYPE_USE_FINAL.equals(markerType)) {
      if (getJavaElement() instanceof IMethod) {
        warnLevel = Prefs.getCheckUseFinalMethod();
      }
      else if (getJavaElement() instanceof IField) {
        warnLevel = Prefs.getCheckUseFinalField();
      }
    }
    return warnLevel == null ? WarnLevel.WARNING : warnLevel;
  }

  public IMember getJavaElement() {
    return javaElement;
  }

  public String getMessage() { // NO_UCD - Needed in other plugin
    return message;
  }

  public int getLine() {
    return line;
  }

  public String getMarkerType() {// NO_UCD - Needed in other plugin
    return markerType;
  }

  public WarnLevel getLevel() {// NO_UCD - Needed in other plugin
    return level;
  }

  public int getReferenceCount() {// NO_UCD - Needed in other plugin
    return referenceCount;
  }

  public String getAuthor() {
    return author;
  }

  /** @return never <code>null</code>, trimms the author name */
  public String getAuthorTrimmed() {
    return author == null ? "" : author.length() > 70 ? author.substring(0, 70) + "..." : author; //$NON-NLS-1$//$NON-NLS-2$
  }

  public int getLineStart() {
    return lineStart;
  }

  public int getLineEnd() {
    return lineEnd;
  }
}