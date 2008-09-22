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
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.report.IUCDetectorReport;
import org.ucdetector.report.MarkerReport;
import org.ucdetector.report.ReportParam;
import org.ucdetector.report.TextReport;
import org.ucdetector.search.LineManger;

/**
 * Create ERROR, WARN markers. Delete markers<p>
 * 
 * CALL endReport() TO FLUSH MARKERS!!!
 */
public class MarkerFactory {
  private final List<IUCDetectorReport> reports = new ArrayList<IUCDetectorReport>();

  /**
   * See extension point="org.eclipse.core.resources.markers" in plugin.xml
   */
  public static final String UCD_MARKER //
  = "org.ucdetector.analyzeMarker"; //$NON-NLS-1$
  public static final String UCD_MARKER_UNUSED //
  = "org.ucdetector.analyzeMarkerReference"; //$NON-NLS-1$
  public static final String UCD_MARKER_USED_FEW //
  = "org.ucdetector.analyzeMarkerFewReference"; //$NON-NLS-1$
  public static final String UCD_MARKER_USE_PRIVATE //
  = "org.ucdetector.analyzeMarkerVisibilityPrivate"; //$NON-NLS-1$
  public static final String UCD_MARKER_USE_PROETECTED //
  = "org.ucdetector.analyzeMarkerVisibilityProtected"; //$NON-NLS-1$
  public static final String UCD_MARKER_USE_DEFAULT //
  = "org.ucdetector.analyzeMarkerVisibilityDefault"; //$NON-NLS-1$
  public static final String UCD_MARKER_USE_FINAL //
  = "org.ucdetector.analyzeMarkerFinal"; //$NON-NLS-1$
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

  public void endReport(Object[] selected, long start) throws CoreException {
    for (IUCDetectorReport report : reports) {
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
    return createMarkerImpl(new ReportParam(method, message, line,
        UCD_MARKER_USE_FINAL));
  }

  /**
   * Create a marker: "Use final for method myMethod()"
   * @return <code>true</code>, if a marker was created
   */
  public boolean createFinalMarker(IField field, int line) throws CoreException {
    String message = NLS.bind(Messages.SearchManager_MarkerFinalField,
        new Object[] { field.getElementName() });
    return createMarkerImpl(new ReportParam(field, message, line,
        UCD_MARKER_USE_FINAL));
  }

  /**
   * Create an eclipse marker: "Class MyClass has {0} references"
   * @param found TODO
   */
  public boolean createReferenceMarker(IJavaElement javaElement,
      String message, int line, int found) throws CoreException {
    String type = found == 0 ? UCD_MARKER_UNUSED : UCD_MARKER_USED_FEW;
    return createMarkerImpl(new ReportParam(javaElement, message, line, type));
  }

  /**
   * Create an eclipse marker: "Change visibility to protected"
   */
  public boolean createVisibilityMarker(IJavaElement javaElement, String type,
      int line) throws CoreException {
    String visibilityString = null;
    if (UCD_MARKER_USE_PRIVATE.equals(type)) {
      visibilityString = "private"; //$NON-NLS-1$
    }
    else if (UCD_MARKER_USE_PROETECTED.equals(type)) {
      visibilityString = "protected"; //$NON-NLS-1$
    }
    else if (UCD_MARKER_USE_DEFAULT.equals(type)) {
      visibilityString = "default"; //$NON-NLS-1$
    }
    Object[] bindings = new Object[] { javaElement.getElementName(),
        visibilityString };
    String message = NLS
        .bind(Messages.SearchManager_MarkerVisibility, bindings);
    return createMarkerImpl(new ReportParam(javaElement, message, line, type));
  }

  /**
   * Create any eclipse marker
   */
  public boolean createMarker(ReportParam reportParam) throws CoreException {
    return createMarkerImpl(reportParam);
  }

  /**
   * This method does the work and creates an marker
   * @return <code>true</code>, if a marker was created
   */
  private boolean createMarkerImpl(ReportParam reportParam)
      throws CoreException {
    if (reportParam.line == LineManger.LINE_NOT_FOUND) {
      Log.logError("createMarkerImpl: Line not found"); //$NON-NLS-1$
      return false;
    }
    if (reportParam.javaElement.getResource() == null) {
      Log.logError("createMarkerImpl: Resource is null"); //$NON-NLS-1$
      return false;
    }
    for (IUCDetectorReport report : reports) {
      report.reportMarker(reportParam);
    }
    return true;
  }

  /**
   * Delete markers of the javaElement and all of its children
   */
  public static void deleteMarkers(IJavaElement javaElement)
      throws CoreException {
    if (javaElement.getResource() != null) {
      javaElement.getResource().deleteMarkers(UCD_MARKER, true,
          IResource.DEPTH_INFINITE);
    }
  }
}
