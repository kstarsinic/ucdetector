/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.ant;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.ucdetector.UCDHeadless;

public class UCDTask extends Task {
  private String buildType;
  private String optionsFile;
  private String targetPlatformFile;
  private final List<Iterate> iterateList = new ArrayList<Iterate>();

  @Override
  public void execute() throws BuildException {
    System.out.println("Running UCDetector Ant Task");
    List<String> resourcesToIterate = new ArrayList<String>();
    for (Iterate iterate : iterateList) {
      resourcesToIterate.add(iterate.getName());
    }
    try {
      new UCDHeadless(buildType, optionsFile, targetPlatformFile, resourcesToIterate).run();
    }
    catch (Exception e) {
      throw new BuildException(e);
    }
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

  public class Iterate {
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
    this.buildType = buildType;
  }

  public void setOptionsFile(String optionsFile) {
    this.optionsFile = optionsFile;
  }

  public void setTargetPlatformFile(String targetPlatformFile) {
    this.targetPlatformFile = targetPlatformFile;
  }
}
