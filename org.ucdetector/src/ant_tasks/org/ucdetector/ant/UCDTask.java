/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.ucdetector.Log;
import org.ucdetector.UCDHeadless;
import org.ucdetector.UCDHeadless.Report;

@SuppressWarnings("nls")
/**
 * UCDetector <a href="http://ant.apache.org/">ant<a> task. Needed to call UCDetector in headless mode from command line.
 * <p>
 * @author Joerg Spieler
 * @since 2008-05-08
 * 
 */
public class UCDTask extends Task {
  private String sBuildType;
  private String sOptionsFile;
  private String sTargetPlatformFile;
  private String sReport;
  private final List<Iterate> iterateList = new ArrayList<Iterate>();

  @Override
  public void execute() throws BuildException {
    Log.info("UCDetector ANT: 3 - Start java code of ant task '<ucdetector>' inside headless eclipse");
    Report report = parseReport(sReport);
    int buildType = parseBuildType(sBuildType);
    File targetPlatformFile = getFile(sTargetPlatformFile, "targetPlatformFile");
    File optionsFile = getFile(sOptionsFile, "optionsFile");
    Log.info("StartUCDetector Ant Task");
    Log.info("    buildType         : " + sBuildType);// + " (" + buildType + ")");
    Log.info("    optionsFile       : " + (optionsFile == null ? "" : optionsFile.getAbsolutePath()));
    Log.info("    targetPlatformFile: " + (targetPlatformFile == null ? "" : targetPlatformFile.getAbsolutePath()));
    Log.info("    report            : " + report);
    Log.info("    iterateList       : " + iterateList);
    List<String> resourcesToIterate = new ArrayList<String>();
    for (Iterate iterate : iterateList) {
      Log.info("                        " + iterate);
      resourcesToIterate.add(iterate.getName());
    }
    try {
      new UCDHeadless(buildType, optionsFile, targetPlatformFile, report, resourcesToIterate).run();
    }
    catch (Exception e) {
      throw new BuildException(e);
    }
  }

  private File getFile(String fileName, String about) {
    File result = null;
    if (fileName != null) {
      result = new File(fileName);
      if (!result.exists()) {
        throw new BuildException(about + " does not exist: " + result.getAbsolutePath());
      }
    }
    return result;
  }

  private Report parseReport(String reportString) {
    if (reportString == null || reportString.length() == 0) {
      return Report.eachproject;
    }
    for (Report rep : Report.values()) {
      if (rep.name().equals(reportString)) {
        return rep;
      }
    }
    throw new BuildException("Unknown report: '" + reportString + "'");
  }

  /** @see org.eclipse.core.resources.IncrementalProjectBuilder */
  private int parseBuildType(String buildType) {
    if (buildType == null || buildType.length() == 0 || "AUTO_BUILD".equals(buildType)) {
      return IncrementalProjectBuilder.AUTO_BUILD;
    }
    if ("FULL_BUILD".equals(buildType)) {
      return IncrementalProjectBuilder.FULL_BUILD;
    }
    if ("INCREMENTAL_BUILD".equals(buildType)) {
      return IncrementalProjectBuilder.INCREMENTAL_BUILD;
    }
    if ("CLEAN_BUILD".equals(buildType)) {
      return IncrementalProjectBuilder.CLEAN_BUILD;
    }
    throw new BuildException("Unknown buildType: '" + buildType + "'");
  }

  // --------------------------------------------------------------------------
  // ANT Attributes, Nested Elements
  // http://ant.apache.org/manual/tutorial-writing-tasks.html
  // --------------------------------------------------------------------------
  public Iterate createIterate() {
    Iterate i = new Iterate();
    iterateList.add(i);
    return i;
  }

  public class Iterate {// NO_UCD: used by ant
    private String name;

    public void setName(String name) { // NO_UCD: used by ant
      this.name = name;
    }

    public String getName() {// NO_UCD: used by ant
      return name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public void setBuildType(String buildType) {
    this.sBuildType = buildType;
  }

  public void setOptionsFile(String optionsFile) {
    this.sOptionsFile = optionsFile;
  }

  public void setTargetPlatformFile(String targetPlatformFile) {
    this.sTargetPlatformFile = targetPlatformFile;
  }

  public void setReport(String report) {
    this.sReport = report;
  }
}
