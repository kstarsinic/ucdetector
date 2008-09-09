package org.ucdetector.report;

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
  public final String message;
  public final int line;
  public final String markerType;
  public final WarnLevel level;

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
    if (MarkerFactory.UCD_MARKER_UNUSED.equals(markerType)) {
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
    else if (MarkerFactory.UCD_MARKER_USE_PRIVATE.equals(markerType)
        || MarkerFactory.UCD_MARKER_USE_PROETECTED.equals(markerType)
        || MarkerFactory.UCD_MARKER_USE_DEFAULT.equals(markerType)) {
      warnLevel = Prefs.getCheckIncreaseVisibility();
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