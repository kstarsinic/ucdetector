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
import org.ucdetector.Log;
import org.ucdetector.UCDHeadless;

@SuppressWarnings("nls")
/**
 * UCDetector <a href="http://ant.apache.org/">ant<a> task. Needed to call UCDetector in headless mode from command line.
 * <p>
 * @see http://help.eclipse.org/helios/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/ant_contributing_task.htm
 * <p>
 * 
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
    File targetPlatformFile = getFile(sTargetPlatformFile, "targetPlatformFile");
    File optionsFile = getFile(sOptionsFile, "optionsFile");
    List<String> resourcesToIterate = getResourcesToIterate();
    try {
      new UCDHeadless(sBuildType, optionsFile, targetPlatformFile, sReport, resourcesToIterate).run();
    }
    catch (Exception e) {
      throw new BuildException(e);
    }
    Log.info("UCDetector ANT: 4 - End java code of ant task '<ucdetector>' inside headless eclipse");
  }

  private List<String> getResourcesToIterate() {
    List<String> result = new ArrayList<String>();
    for (Iterate iterate : iterateList) {
      Log.info("                        " + iterate);
      result.add(iterate.getName());
    }
    return result;
  }

  private static File getFile(String fileName, String about) {
    File result = null;
    if (fileName != null) {
      result = new File(fileName);
      if (!result.exists()) {
        throw new BuildException(about + " does not exist: " + result.getAbsolutePath());
      }
    }
    return result;
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

  public class Iterate { // NO_UCD - used by ant
    private String name;

    public void setName(String name) {
      this.name = name;
    }

    public String getName() {
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
