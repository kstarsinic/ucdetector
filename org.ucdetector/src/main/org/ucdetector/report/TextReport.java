/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.ucdetector.Log;
import org.ucdetector.UCDInfo;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.preferences.Prefs;
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
  private StringBuilder report;
  private ReportExtension extension;
  private IJavaElement[] objectsToIterate;
  private int markerCount = 0;
  private final List<IStatus> detectionProblems = new ArrayList<IStatus>();

  public void startReport(IJavaElement[] objectsToIterateIn, long startTime) throws CoreException {
    this.objectsToIterate = objectsToIterateIn;
    reset();
    appendTitle();
    appendHeader();
  }

  private void reset() {
    this.report = new StringBuilder();
    this.detectionProblems.clear();
    this.markerCount = 0;
  }

  private void appendTitle() {
    report.append("Created with UCDetector ").append(UCDInfo.getUCDVersion()).append(TAB);
    report.append(UCDInfo.getNow(false)).append(TAB);
    report.append("http://www.ucdetector.org/").append(TAB);
    report.append("Created by class: " + getClass().getName()).append(TAB);
    report.append(NEW_LINE);
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
    markerCount++;
    IMember javaElement = reportParam.getJavaElement();
    String location = JavaElementUtil.createJavaLink(javaElement, reportParam.getLine());
    report.append(location).append(TAB);/*                                   */// Location
    report.append(reportParam.getMessage()).append(TAB); /*                  */// Description
    report.append(JavaElementUtil.getElementName(javaElement)).append(TAB);/**/// Java
    report.append(reportParam.getMarkerType()).append(TAB);/*                */// Marker
    report.append(reportParam.getAuthorTrimmed());/*                         */// Author
    report.append(NEW_LINE);
    return true;
  }

  public void reportDetectionProblem(IStatus status) {
    detectionProblems.add(status);
  }

  public void endReport() throws CoreException {
    if (!detectionProblems.isEmpty()) {
      report.append(NEW_LINE).append(detectionProblems.size()).append(" Exceptions found during detection");
    }
    for (IStatus status : detectionProblems) {
      report.append(NEW_LINE);
      report.append(UCDetectorPlugin.exceptionToString(status.getException()));
    }
    writeReportFile();
  }

  private void writeReportFile() {
    if (markerCount == 0 || !Prefs.isCreateReport(extension)) {
      return;
    }
    String reportName = ReportNameManager.getReportFileName(extension.getResultFile(), objectsToIterate);
    String reportDir = ReportNameManager.getReportDir(true);
    File resultFile = new File(reportDir, reportName);
    OutputStreamWriter writer = null;
    try {
      writer = new OutputStreamWriter(new FileOutputStream(resultFile), UCDetectorPlugin.UTF_8);
      writer.append(report.toString());
      Log.info("Created file: " + resultFile);
    }
    catch (IOException ex) {
      UCDetectorPlugin.logToEclipseLog("Can't write report", ex);
    }
    finally {
      UCDetectorPlugin.closeSave(writer);
    }
  }

  public void setExtension(ReportExtension reportExtension) {
    this.extension = reportExtension;
  }
}
