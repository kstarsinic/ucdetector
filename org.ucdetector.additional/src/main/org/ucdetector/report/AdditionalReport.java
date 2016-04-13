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
 * Write a custom report - based on: org.ucdetector.report.TextReport
 * <p>
 * See also "org.ucdetector.reports" section in /org.ucdetector.additional/plugin.xml
 * <p>
 * @author Joerg Spieler
 * @since 2011-06-22
 */
@SuppressWarnings("nls")
public class AdditionalReport implements IUCDetectorReport {
  private static final String NEW_LINE = System.getProperty("line.separator");
  private static final String TAB = "\t";
  //
  private StringBuilder report;
  private ReportExtension extension;
  private IJavaElement[] objectsToIterate;
  private int markerCount = 0;

  @Override
  public void startReport(IJavaElement[] objectsToIterateIn, long startTime) throws CoreException {
    this.objectsToIterate = objectsToIterateIn;
    reset();
    appendTitle();
    appendHeader();
  }

  private void reset() {
    this.report = new StringBuilder();
    this.markerCount = 0;
  }

  private void appendTitle() {
    report.append("Created with UCDetector Addtional: ").append(UCDInfo.getUCDVersion()).append(TAB);
    report.append(UCDInfo.getNow(false)).append(TAB);
    report.append("http://www.ucdetector.org/").append(TAB);
    report.append(NEW_LINE);
  }

  private void appendHeader() {
    report.append("Location").append(TAB);
    report.append("Description").append(TAB);
    report.append("Java").append(TAB);
    report.append("Marker").append(TAB);
    report.append("LineStart").append(TAB);
    report.append("LineEnd");
    report.append(NEW_LINE);
  }

  @Override
  public boolean reportMarker(ReportParam reportParam) throws CoreException {
    markerCount++;
    IMember javaElement = reportParam.getJavaElement();
    String location = JavaElementUtil.createJavaLink(javaElement, reportParam.getLine());
    report.append(location).append(TAB);/*                                   */// Location
    report.append(reportParam.getMessage()).append(TAB); /*                  */// Description
    report.append(JavaElementUtil.getElementName(javaElement)).append(TAB);/**/// Java
    report.append(reportParam.getMarkerType()).append(TAB);/*                */// Marker
    report.append(reportParam.getAuthorTrimmed());/*                         */// Author
    //  [ 3323078 ] Add line number start/end to markers
    report.append(reportParam.getLineStart());/*                             */// LineStart
    report.append(reportParam.getLineEnd());/*                               */// LineEnd
    report.append(NEW_LINE);
    return true;
  }

  @Override
  public void endReport() throws CoreException {
    writeReportFile();
  }

  private void writeReportFile() {
    if (markerCount == 0 || !Prefs.isCreateReport(extension)) {
      return;
    }
    String reportDir = ReportNameManager.getReportDir(true);
    String reportName = ReportNameManager.getReportFileName(extension.getResultFile(), objectsToIterate);
    File reportFile = new File(reportDir, reportName);
    OutputStreamWriter writer = null;
    try {
      writer = new OutputStreamWriter(new FileOutputStream(reportFile), UCDetectorPlugin.UTF_8);
      writer.append(report.toString());
      Log.info("Created file: " + reportFile);
    }
    catch (IOException ex) {
      UCDetectorPlugin.logToEclipseLog("Can't write report", ex);
    }
    finally {
      UCDetectorPlugin.closeSave(writer);
    }
  }

  @Override
  public void setExtension(ReportExtension reportExtension) {
    this.extension = reportExtension;
  }

  @Override
  public void reportDetectionProblem(IStatus status) {
    // 
  }
}
