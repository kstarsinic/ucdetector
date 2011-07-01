/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.ant;

import java.io.IOException;

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
    Log.info("UCDetector ANT: 3 - Start java code of ant task '<ucdetector>' inside headless eclipse");
    try {
      String optionsFileName = System.getProperty("ucd.options.file");
      new UCDHeadless(optionsFileName).run();
    }
    catch (Exception e) {
      throw new BuildException(e);
    }
    Log.info("UCDetector ANT: 4 - End java code of ant task '<ucdetector>' inside headless eclipse");
  }

  //  @Override
  //  protected int handleInput(byte[] buffer, int offset, int length) throws IOException {
  //    System.out.printf("########## JOSP handleInput:  buffer.length: %s, offset: %s, length: %s : '%s'%n",
  //        buffer.length, offset, length, new String(buffer, offset, length));
  //    return super.handleInput(buffer, offset, length);
  //  }

  //  private String optionsFile;
  // ANT Attributes
  //  public void setOptionsFile(String optionsFile) {
  //    this.optionsFile = optionsFile;
  //  }
}
