/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.osgi.util.NLS;
import org.ucdetector.Messages;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.preferences.WarnLevel;
import org.ucdetector.report.IUCDetctorReport;
import org.ucdetector.report.MarkerReport;
import org.ucdetector.report.TextReport;
import org.ucdetector.search.LineManger;

/**
 * Create ERROR, WARN markers. Delete markers
 */
public class MarkerFactory {
  private final List<IUCDetctorReport> reports = new ArrayList<IUCDetctorReport>();

  /**
   * See extension point="org.eclipse.core.resources.markers" in plugin.xml
   */
  public static final String ANALYZE_MARKER //
  = "org.ucdetector.analyzeMarker"; //$NON-NLS-1$
  public static final String ANALYZE_MARKER_REFERENCE //
  = "org.ucdetector.analyzeMarkerReference"; //$NON-NLS-1$
  //
  public static final String ANALYZE_MARKER_VISIBILITY_PRIVATE //
  = "org.ucdetector.analyzeMarkerVisibilityPrivate"; //$NON-NLS-1$
  public static final String ANALYZE_MARKER_VISIBILITY_PROETECTED //
  = "org.ucdetector.analyzeMarkerVisibilityProtected"; //$NON-NLS-1$
  public static final String ANALYZE_MARKER_VISIBILITY_DEFAULT //
  = "org.ucdetector.analyzeMarkerVisibilityDefault"; //$NON-NLS-1$
  //
  public static final String ANALYZE_MARKER_FINAL //
  = "org.ucdetector.analyzeMarkerFinal"; //$NON-NLS-1$
  // -------------------------------------------------------------------------
  public static final String PROBLEM = "UCD_PROBLEM";//$NON-NLS-1$
  public static final String PROBLEM_UNUSED_CLASS = "PROBLEM_UNUSED_CLASS";//$NON-NLS-1$
  public static final String PROBLEM_UNUSED_METHOD = "PROBLEM_UNUSED_METHOD";//$NON-NLS-1$
  public static final String PROBLEM_UNUSED_FIELD = "PROBLEM_UNUSED_FIELD";//$NON-NLS-1$
  //
  static final String PROBLEM_USE_PRIVATE = "PROBLEM_USE_PRIVATE";//$NON-NLS-1$
  static final String PROBLEM_USE_PROTECTED = "PROBLEM_USE_PROTECTED";//$NON-NLS-1$
  static final String PROBLEM_USE_DEFAULT = "PROBLEM_USE_DEFAULT";//$NON-NLS-1$
  //
  static final String PROBLEM_USE_FINAL = "PROBLEM_USE_FINAL";//$NON-NLS-1$
  /**
   * Helper attribute to transfer java element information
   * of a marker to QuickFix. Only String, Integer... are permitted
   * as iMarker.setAttribute()
   **/
  public static final String JAVA_ELEMENT_ATTRIBUTE = "JAVA_ELEMENT_ATTRIBUTE";//$NON-NLS-1$
  /** the java element of the marker is an type */
  public static final String JAVA_ELEMENT_TYPE = "type";//$NON-NLS-1$
  /** the java element of the marker is an method */
  public static final String JAVA_ELEMENT_METHOD = "method";//$NON-NLS-1$
  /** the java element of the marker is an field */
  public static final String JAVA_ELEMENT_FIELD = "field";//$NON-NLS-1$

  private MarkerFactory() {
    reports.add(new MarkerReport());
    if (Prefs.isWriteReportFile()) {
      reports.add(new TextReport());
    }
  }

  public static MarkerFactory createInstance() {
    return new MarkerFactory();
  }

  public void endReport(Object[] selected, long start) {
    for (IUCDetctorReport report : reports) {
      report.endReport(selected, start);
    }
  }

  /**
   * Create a marker: "Use final for method myMethod()"
   * @return <code>true</code>, if a marker was created
   */
  public boolean createFinalMarker(IMethod method, int line)
      throws CoreException {
    String message = NLS.bind(Messages.SearchManager_MarkerFinalMethod,
        new Object[] { method.getElementName() });
    WarnLevel level = Prefs.getCheckUseFinalMethod();
    return createMarkerImpl(method, message, line, level, ANALYZE_MARKER_FINAL,
        PROBLEM_USE_FINAL);
  }

  /**
   * Create a marker: "Use final for method myMethod()"
   * @return <code>true</code>, if a marker was created
   */
  public boolean createFinalMarker(IField field, int line) throws CoreException {
    String message = NLS.bind(Messages.SearchManager_MarkerFinalField,
        new Object[] { field.getElementName() });
    WarnLevel level = Prefs.getCheckUseFinalField();
    return createMarkerImpl(field, message, line, level, ANALYZE_MARKER_FINAL,
        PROBLEM_USE_FINAL);
  }

  /**
   * Create an eclipse marker: "Class MyClass has 0 references"
   */
  public boolean createReferenceMarker(IJavaElement javaElement,
      String message, int line, WarnLevel level, String problem)
      throws CoreException {
    String type = ANALYZE_MARKER_REFERENCE;
    return createMarkerImpl(javaElement, message, line, level, type, problem);
  }

  /**
   * Create an eclipse marker: "Change visibility to protected"
   */
  public boolean createVisibilityMarker(IJavaElement javaElement, String type,
      int line) throws CoreException {
    String visibilityString = null;
    String problem = null;
    if (ANALYZE_MARKER_VISIBILITY_PRIVATE.equals(type)) {
      visibilityString = "private"; //$NON-NLS-1$
      problem = MarkerFactory.PROBLEM_USE_PRIVATE;
    }
    else if (ANALYZE_MARKER_VISIBILITY_PROETECTED.equals(type)) {
      visibilityString = "protected"; //$NON-NLS-1$
      problem = MarkerFactory.PROBLEM_USE_PROTECTED;
    }
    else if (ANALYZE_MARKER_VISIBILITY_DEFAULT.equals(type)) {
      visibilityString = "default"; //$NON-NLS-1$
      problem = MarkerFactory.PROBLEM_USE_DEFAULT;
    }
    Object[] bindings = new Object[] { javaElement.getElementName(),
        visibilityString };
    String message = NLS
        .bind(Messages.SearchManager_MarkerVisibility, bindings);
    WarnLevel level = Prefs.getCheckIncreaseVisibility();
    return createMarkerImpl(javaElement, message, line, level, type, problem);
  }

  /**
   * Create any eclipse marker
   */
  public boolean createMarker(IJavaElement javaElement, String message, // NO_UCD
      int line, WarnLevel level, String markerType, String problem)
      throws CoreException {
    return createMarkerImpl(javaElement, message, line, level, markerType,
        problem);
  }

  /**
   * This method does the work and creates an marker
   * @return <code>true</code>, if a marker was created
   */
  private boolean createMarkerImpl(IJavaElement javaElement, String message,
      int line, WarnLevel level, String markerType, String problem)
      throws CoreException {
    if (line == LineManger.LINE_NOT_FOUND //
        || javaElement.getResource() == null //
        || WarnLevel.IGNORE.equals(level) //
    ) {
      return false;
    }
    for (IUCDetctorReport report : reports) {
      report.reportMarker(javaElement, message, line, level, markerType,
          problem);
    }
    return true;
  }

  /**
   * Delete markers of the javaElement and all of its children
   */
  public static void deleteMarkers(IJavaElement javaElement)
      throws CoreException {
    if (javaElement.getResource() != null) {
      javaElement.getResource().deleteMarkers(ANALYZE_MARKER, true,
          IResource.DEPTH_INFINITE);
    }
  }
}
