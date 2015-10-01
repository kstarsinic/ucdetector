/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.preferences.Prefs;

/**
 * Handle reports dir and file name
 * <p>
 * @author Joerg Spieler
 * @since 2011-04-11
 */
@SuppressWarnings("nls")
public class ReportNameManager {
  private static final String FILE_NAME_REPLACE_NUMBER = "${number}";
  private static final String REPORT_DEFAULT_DIR = ".ucdetector_reports";
  private static final DecimalFormat FORMAT_REPORT_NUMBER = new DecimalFormat("000");

  /**
   * @param create create directory (only use <code>true</code>, when directory is needed)
   * @return report directory
   */
  public static String getReportDir(boolean create) {
    String dir = Prefs.getReportsDir();
    if (dir.length() == 0) {
      dir = ReportNameManager.getReportDirDefault();
    }
    File reportDir = new File(dir);
    //    if (create) {
    reportDir.mkdirs();
    //    }
    return reportDir.getAbsolutePath();
  }

  public static String getReportDirDefault() {
    String reportDir;
    try {
      File workspaceDir = Platform.getLocation().toFile();
      reportDir = UCDetectorPlugin.getCanonicalPath(new File(workspaceDir, REPORT_DEFAULT_DIR));
    }
    catch (Exception e) {
      reportDir = REPORT_DEFAULT_DIR;
      Log.error("Can't get report file name", e);
    }
    // Needed to avoid message: Value must be an existing directory
    //    new File(reportDir).mkdirs();
    return reportDir;
  }

  /**
   * @param fileName file name to replace date, time...
   * @param objectsToIterate needed to get project name
   * @return File name, with does not exist, containing a number.
   * eg: UCDetectorReport_001
   */
  // Fix [2811049]  Html report is overridden each run
  public static String getReportFileName(String fileName, IJavaElement[] objectsToIterate) {// NO_UCD - Needed in other plugin
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat timeFormat = new SimpleDateFormat("HHmmss");
    String result = fileName;
    result = result.replace("${reportName}", Prefs.getReportFile());
    result = result.replace("${project}", ReportNameManager.getProjectName(objectsToIterate));
    result = result.replace("${date}", dateFormat.format(new Date()));
    result = result.replace("${time}", timeFormat.format(new Date()));
    result = result.replace(FILE_NAME_REPLACE_NUMBER, freeFileNumber);
    return result;
  }

  private static String freeFileNumber = "";

  public static void setNextFreeFileNumberString() {
    File reportDir = new File(getReportDir(true));
    String[] files = reportDir.list();
    files = (files == null) ? new String[0] : files;
    for (int i = 1; i < 1000; i++) {
      String number = FORMAT_REPORT_NUMBER.format(i);
      boolean fileNumberFound = false;
      for (String file : files) {
        if (file.contains(number)) {
          fileNumberFound = true;
          break;
        }
      }
      if (!fileNumberFound) {
        freeFileNumber = number;
        return;
      }
    }
    freeFileNumber = "";
  }

  private static String getProjectName(IJavaElement[] objectsToIterate) {
    Set<String> projects = new HashSet<>();
    for (IJavaElement element : objectsToIterate) {
      if (element.getJavaProject() != null) {
        projects.add(element.getJavaProject().getElementName());
      }
    }
    switch (projects.size()) {
      case 0:
        return "unknown_project";
      case 1:
        return projects.iterator().next();
      default:
        return "several_projects";
    }
  }
}
