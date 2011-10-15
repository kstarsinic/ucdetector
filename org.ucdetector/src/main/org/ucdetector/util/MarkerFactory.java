/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.osgi.util.NLS;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.report.IUCDetectorReport;
import org.ucdetector.report.MarkerReport;
import org.ucdetector.report.ReportExtension;
import org.ucdetector.report.ReportNameManager;
import org.ucdetector.report.ReportParam;
import org.ucdetector.report.XmlReport;
import org.ucdetector.search.LineManger;

/**
 * Create ERROR, WARN markers. Delete markers<p>
 * 
 * CALL endReport() TO FLUSH MARKERS!!!
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
 */
public final class MarkerFactory implements IUCDetectorReport {
  /**
   * Marker prefix for all UCDetector markers
   * See extension point="org.eclipse.core.resources.markers" in plugin.xml
   */
  public static final String UCD_MARKER = UCDetectorPlugin.ID + ".analyzeMarker"; //$NON-NLS-1$
  public static final String UCD_MARKER_UNUSED = UCD_MARKER + "Reference"; //$NON-NLS-1$
  public static final String UCD_MARKER_USED_FEW = UCD_MARKER + "FewReference"; //$NON-NLS-1$
  public static final String UCD_MARKER_USE_PRIVATE = UCD_MARKER + "VisibilityPrivate"; //$NON-NLS-1$
  // TODO remove? [ 3191417 ] Change to "protected" should be default
  public static final String UCD_MARKER_USE_PROTECTED = UCD_MARKER + "VisibilityProtected"; //$NON-NLS-1$
  public static final String UCD_MARKER_USE_DEFAULT = UCD_MARKER + "VisibilityDefault"; //$NON-NLS-1$
  public static final String UCD_MARKER_USE_FINAL = UCD_MARKER + "Final"; //$NON-NLS-1$
  public static final String UCD_MARKER_TEST_ONLY = UCD_MARKER + "TestOnly"; //$NON-NLS-1$ // NO_UCD
  // ADDING NEW MARKER? ADD ALSO TO plugin.xml!

  private final List<IUCDetectorReport> reports;

  /** Helper attribute to transfer java element name (e.g. method name) from a marker to QuickFix  **/
  public static final String JAVA_NAME = "JAVA_NAME";//$NON-NLS-1$
  /**
   * Helper attribute to transfer java type information from a marker to QuickFix
   * See: {@link org.ucdetector.report.MarkerReport.ElementType}
   */
  public static final String JAVA_TYPE = "JAVA_TYPE";//$NON-NLS-1$

  private MarkerFactory(List<IUCDetectorReport> reports) {
    this.reports = reports;
  }

  public static MarkerFactory createInstance() {
    ReportNameManager.setNextFreeFileNumberString();
    ArrayList<IUCDetectorReport> reportsList = new ArrayList<IUCDetectorReport>();
    reportsList.add(new MarkerReport());
    if (Prefs.isWriteReportFile()) {
      reportsList.add(new XmlReport());
    }
    List<ReportExtension> classExtensions = ReportExtension.getClassExtensions();
    for (ReportExtension reportExtension : classExtensions) {
      IUCDetectorReport report = reportExtension.getReport();
      report.setExtension(reportExtension);
      reportsList.add(report);
    }
    return new MarkerFactory(reportsList);
  }

  public void startReport(IJavaElement[] objectsToIterate, long startTime) throws CoreException {
    for (IUCDetectorReport report : reports) {
      report.startReport(objectsToIterate, startTime);
    }
  }

  /**
   * This method does the work and creates an marker
   * @return <code>true</code>, if a marker was created
   */
  public boolean reportMarker(ReportParam reportParam) throws CoreException {
    if (reportParam.getLine() == LineManger.LINE_NOT_FOUND) {
      String elementName = JavaElementUtil.getElementName(reportParam.getJavaElement());
      Log.error("reportMarker: Line not found for: " + elementName); //$NON-NLS-1$
      return false;
    }
    if (reportParam.getJavaElement().getResource() == null) {
      Log.error("reportMarker: Resource is null"); //$NON-NLS-1$
      return false;
    }
    for (IUCDetectorReport report : reports) {
      report.reportMarker(reportParam);
    }
    return true;
  }

  public void reportDetectionProblem(IStatus status) {
    for (IUCDetectorReport report : reports) {
      report.reportDetectionProblem(status);
    }
  }

  public void endReport() throws CoreException {
    if (!Prefs.isWriteReportFile()) {
      Log.info("Do not write reports, because no write report option is selected"); //$NON-NLS-1$
    }
    for (IUCDetectorReport report : reports) {
      report.endReport();
    }
  }

  /**
   * Create a marker: "Use final for method myMethod()"
   * @param method method to create marker for
   * @param line line to create marker for
   * @return <code>true</code>, if a marker was created
   * @throws CoreException when there are problem creating marker
   */
  public boolean createFinalMarker(IMethod method, int line) throws CoreException {
    String searchInfo = JavaElementUtil.getMemberTypeString(method);
    String elementName = JavaElementUtil.getElementName(method);
    String message = NLS.bind(Messages.MarkerFactory_MarkerFinalMethod, new Object[] { searchInfo, elementName });
    return reportMarker(new ReportParam(method, message, line, UCD_MARKER_USE_FINAL));
  }

  /**
   * Create a marker: "Use final for method myMethod()"
   * @param field to create marker for
   * @param line to create marker for
   * @return <code>true</code>, if a marker was created
   * @throws CoreException when there are problem creating marker
   */
  public boolean createFinalMarker(IField field, int line) throws CoreException {
    String searchInfo = JavaElementUtil.getMemberTypeString(field);
    String elementName = JavaElementUtil.getElementName(field);
    String message = NLS.bind(Messages.MarkerFactory_MarkerFinalField, new Object[] { searchInfo, elementName });
    return reportMarker(new ReportParam(field, message, line, UCD_MARKER_USE_FINAL));
  }

  /**
   * Create an eclipse marker: "Class MyClass has {0} references"
   * @param javaElement  to create marker for
   * @param message  to create marker for
   * @param line  to create marker for
   * @param found number of found elements
   * @return <code>true</code>, if a marker was created
   * @throws CoreException when there are problem creating marker
   */
  public boolean createReferenceMarker(IMember javaElement, String message, int line, int found) throws CoreException {
    String type = found == 0 ? UCD_MARKER_UNUSED : UCD_MARKER_USED_FEW;
    return reportMarker(new ReportParam(javaElement, message, line, type, found));
  }

  /**
   * Create an eclipse marker: Method "ClassName.myMethod()"  is only matched by test code
   * @param member  to create marker for
   * @param line  to create marker for
   * @return <code>true</code>, if a marker was created
   * @throws CoreException when there are problem creating marker
   */
  public boolean createReferenceMarkerTestOnly(IMember member, int line) throws CoreException {
    String searchInfo = JavaElementUtil.getMemberTypeString(member);
    String elementName = JavaElementUtil.getElementName(member);
    String message = NLS.bind(Messages.MarkerFactory_MarkerTestOnly, new Object[] { searchInfo, elementName });
    return reportMarker(new ReportParam(member, message, line, UCD_MARKER_TEST_ONLY));
  }

  /**
   * Create an eclipse marker: "Change visibility to protected"
   * @param member  to create marker for
   * @param type  to create marker for
   * @param line  to create marker for
   * @return <code>true</code>, if a marker was created
   * @throws CoreException when there are problem creating marker
   */
  public boolean createVisibilityMarker(IMember member, String type, int line) throws CoreException {
    String visibilityString = null;
    if (UCD_MARKER_USE_PRIVATE.equals(type)) {
      visibilityString = "private"; //$NON-NLS-1$
    }
    else if (UCD_MARKER_USE_PROTECTED.equals(type)) {
      visibilityString = "protected"; //$NON-NLS-1$
    }
    else if (UCD_MARKER_USE_DEFAULT.equals(type)) {
      visibilityString = "default"; //$NON-NLS-1$
    }
    String searchInfo = JavaElementUtil.getMemberTypeString(member);
    if (member instanceof IType) {
      // [2539795] Visibility marker for classes causes compilation error
      visibilityString += Messages.MarkerFactory_VisibilityCompileErrorForClass;
    }
    Object[] bindings = new Object[] { searchInfo, JavaElementUtil.getElementName(member), visibilityString };
    String message = NLS.bind(Messages.MarkerFactory_MarkerVisibility, bindings);
    return reportMarker(new ReportParam(member, message, line, type));
  }

  /**
   * Delete markers of the javaElement and all of its children
   * @param javaElement  to create marker for
   * @throws CoreException when there are problem creating marker
   */
  public static void deleteMarkers(IJavaElement javaElement) throws CoreException {
    if (javaElement.getResource() != null) {
      javaElement.getResource().deleteMarkers(UCD_MARKER, true, IResource.DEPTH_INFINITE);
    }
  }

  public void setExtension(ReportExtension reportExtension) {
    //
  }

  public static String dumpMarker(IMarker m) {
    return markerAsMap(m).toString();
  }

  public static Map<String, Object> markerAsMap(IMarker m) {
    Map<String, Object> result = new HashMap<String, Object>();
    try {
      Map<?, ?> attributes = m.getAttributes();
      for (Object key : attributes.keySet()) {
        result.put(String.valueOf(key), attributes.get(key));
      }
    }
    catch (CoreException e) {
      result.put("_ERROR_", e.getMessage()); //$NON-NLS-1$
    }
    return result;
  }
}
