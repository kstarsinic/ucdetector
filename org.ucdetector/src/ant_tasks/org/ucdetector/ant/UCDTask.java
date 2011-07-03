/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.ant;

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

  @Override
  public void execute() throws BuildException {
    Log.info("UCDetector ANT: 3 - Start java code of ant task org.ucdetector.ant.UCDTask inside headless eclipse");
    try {
      String optionsFileName = System.getProperty("ucd.options.file");
      new UCDHeadless(optionsFileName).iterate();
    }
    catch (Exception e) {
      throw new BuildException(e);
    }
    Log.info("UCDetector ANT: 4 - End java code of ant task org.ucdetector.ant.UCDTask inside headless eclipse");
  }

  //  private void handleInput() {
  //    InputHandler inputHandler = getProject().getInputHandler();
  //    InputRequest request = new InputRequest("InputRequest");
  //    inputHandler.handleInput(request);
  //    String line = request.getInput();
  //    System.out.println("line:" + line);
  //  }

  //  private String optionsFile;
  // ANT Attributes
  //  public void setOptionsFile(String optionsFile) {
  //    this.optionsFile = optionsFile;
  //  }
}
