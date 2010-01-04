/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import java.util.List;

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
import org.ucdetector.report.IUCDetectorReport;
import org.ucdetector.report.ReportParam;
import org.ucdetector.search.LineManger;

/**
 * Create ERROR, WARN markers. Delete markers<p>
 * 
 * CALL endReport() TO FLUSH MARKERS!!!
 */
public final class MarkerFactory implements IUCDetectorReport {
  private final List<IUCDetectorReport> reports;

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
  public static final String UCD_MARKER_USE_PROTECTED //
  = "org.ucdetector.analyzeMarkerVisibilityProtected"; //$NON-NLS-1$
  public static final String UCD_MARKER_USE_DEFAULT //
  = "org.ucdetector.analyzeMarkerVisibilityDefault"; //$NON-NLS-1$
  public static final String UCD_MARKER_USE_FINAL //
  = "org.ucdetector.analyzeMarkerFinal"; //$NON-NLS-1$
  public static final String UCD_MARKER_TEST_ONLY //
  = "org.ucdetector.analyzeMarkerTestOnly"; //$NON-NLS-1$
  // ADDING NEW MARKER? ADD ALSO TO plugin.xml!

  /** Helper attribute to transfer java element name (e.g. method name) from a marker to QuickFix  **/
  public static final String JAVA_NAME = "JAVA_NAME";//$NON-NLS-1$
  /**
   * Helper attribute to transfer java type information (e.g. ElementType.TYPE.toString()) from a marker to QuickFix
   */
  public static final String JAVA_TYPE = "JAVA_TYPE";//$NON-NLS-1$

  /**
   * See also concrete classes of: org.eclipse.jdt.core.dom.BodyDeclaration<br>
   * See also org.ucdetector.quickfix.AbstractUCDQuickFix.getModifierListRewrite()<br>
   */
  public static enum ElementType {
    TYPE, ANNOTATION, ENUM, // types
    METHOD, ANNOTATION_TYPE_MEMBER, // methods
    FIELD, ENUM_CONSTANT, /*ANNOTATION_TYPE_MEMBER*/// fields
  }

  private MarkerFactory(List<IUCDetectorReport> reports) {
    this.reports = reports;
  }

  public static MarkerFactory createInstance(List<IUCDetectorReport> reports) {
    return new MarkerFactory(reports);
  }

  /**
   * This method does the work and creates an marker
   * @return <code>true</code>, if a marker was created
   */
  public boolean reportMarker(ReportParam reportParam) throws CoreException {
    if (reportParam.getLine() == LineManger.LINE_NOT_FOUND) {
      Log.logError("createMarkerImpl: Line not found"); //$NON-NLS-1$
      return false;
    }
    if (reportParam.getJavaElement().getResource() == null) {
      Log.logError("createMarkerImpl: Resource is null"); //$NON-NLS-1$
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

  public void endReport(Object[] selected, long start) throws CoreException {
    for (IUCDetectorReport report : reports) {
      report.endReport(selected, start);
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
}
