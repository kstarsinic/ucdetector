/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.preferences.PreferenceInitializer;
import org.ucdetector.util.JavaElementUtil;

/**
 * Write a text report
 * <p>
 * @author Joerg Spieler
 * @since 31.03.2011
 */
@SuppressWarnings("nls")
public class TextReport implements IUCDetectorReport {
  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String TAB = "\t";
  //
  private final StringBuilder report = new StringBuilder();
  private ReportExtension reportExtension;
  private IJavaElement[] objectsToIterate;

  public void startReport(IJavaElement[] objectsToIterate, long startTime) throws CoreException {
    this.objectsToIterate = objectsToIterate;
    report.append("UCDetector Report").append(TAB).append(UCDetectorPlugin.getNow()).append(NEW_LINE);
    appendHeader();
  }

  private void appendHeader() {
    report.append("Location").append(TAB);
    report.append("Description").append(TAB);
    report.append("Java").append(TAB);
    report.append("Marker").append(TAB);
    report.append("Author");
    report.append(NEW_LINE);
  }

  public boolean reportMarker(ReportParam reportParam) throws CoreException {
    IMember javaElement = reportParam.getJavaElement();
    IType type = JavaElementUtil.getTypeFor(javaElement, true);
    String typeNameFull = JavaElementUtil.getTypeNameFull(type);
    String member;
    if (javaElement instanceof IType) {
      member = "<init>";
    }
    else {
      member = javaElement.getElementName();
    }
    String typeName = JavaElementUtil.getElementName(type);
    String location = String.format("%s.%s(%s.java:%s)", typeNameFull, member, typeName, "" + reportParam.getLine());
    report.append(location).append(TAB);// Location
    report.append(reportParam.getMessage()).append(TAB); // Description
    report.append(JavaElementUtil.getElementName(javaElement)).append(TAB);// Java
    report.append(reportParam.getMarkerType()).append(TAB);// Marker
    report.append(reportParam.getAuthor());// Author
    report.append(NEW_LINE);
    return true;
  }

  public void reportDetectionProblem(IStatus status) {
    //
  }

  public void endReport() throws CoreException {
    // TODO: use report name from reportExtension, fix dir name
    String reportName = PreferenceInitializer.getReportName(objectsToIterate);

    String reportDir = PreferenceInitializer.getReportDir(true);
    String reportFile = reportExtension.getResultFile();
    File resultFile = new File(reportDir, reportFile);
    FileWriter writer = null;
    try {
      writer = new FileWriter(resultFile);
      writer.append(report);
      Log.info("Created file: " + resultFile);
    }
    catch (IOException ex) {
      UCDetectorPlugin.logToEclipseLog("Can't write report", ex);
    }
    UCDetectorPlugin.closeSave(writer);
  }

  public void setExtension(ReportExtension reportExtension) {
    this.reportExtension = reportExtension;
  }
}
