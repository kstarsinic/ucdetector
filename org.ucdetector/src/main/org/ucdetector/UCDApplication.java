/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * 
 * Run UCDetector from command line as an application in headless mode (experimental):
 * <pre>$ ECLIPSE_HOME/eclipse -data WORKSPACE -application org.ucdetector.detect -noSplash</pre>
 * <p>
 * This class searches for options files in user dir.
 * <p>
 * @author Joerg Spieler
 * @since 31.03.2011
 */
@SuppressWarnings("nls")
public class UCDApplication implements IApplication {

  public Object start(IApplicationContext context) throws Exception {
    Log.info("Starting UCDHeadless  (application mode)");
    new UCDHeadless(null).run();
    return IApplication.EXIT_OK;
  }

  public void stop() {
    Log.info("Stopping UCDHeadless (application mode)");
  }
}