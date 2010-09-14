/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class UCDTask extends Task {

  public UCDTask() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void execute() throws BuildException {
    System.out.println("Running: UCDTask");
  }
}
