/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.CoreException;

/**
 * Run UCDetector in headless mode. Entry point is an ant task.
 * See also files: run.sh, run.bat, plugin.xml, headless.xml
 */
public class UCDetectorTask extends Task {

  @Override
  public void execute() throws BuildException {
    try {
      Log.logInfo("Starting UCDetectorTask "); //$NON-NLS-1$
      UCDApplication.startImpl();
      Log.logInfo("Finished UCDetectorTask "); //$NON-NLS-1$
    }
    catch (CoreException e) {
      throw new BuildException(e);
    }
  }
}
