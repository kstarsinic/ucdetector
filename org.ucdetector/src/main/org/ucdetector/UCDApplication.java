/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.Platform;
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
    Log.info("Starting UCDHeadless as an application");
    UCDHeadless ucdHeadless = new UCDHeadless(getOptionsFileName());
    new SystemInReader(ucdHeadless).start();
    ucdHeadless.iterate();
    return IApplication.EXIT_OK;
  }

  private final class SystemInReader extends Thread {
    private final UCDHeadless ucdHeadless;

    public SystemInReader(UCDHeadless ucdHeadless) {
      this.ucdHeadless = ucdHeadless;
    }

    @Override
    public void run() {
      // System.out.println("SystemInReader: Start");
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      String line;
      try {
        while ((line = reader.readLine()) != null) {
          // System.out.println("SystemInReader LINE: " + line);
          if ("exit".equals(line)) {
            // System.out.println("SystemInReader: exit called!");
            if (ucdHeadless.ucdMonitor != null) {
              ucdHeadless.ucdMonitor.setCanceled(true);
            }
            break;
          }
          if (isInterrupted()) {
            // System.out.println("SystemInReader: Interrupted");
            break;
          }
        }
      }
      catch (Exception ex) {
        Log.error("Exception reading System.in", ex);
      }
      // System.out.println("SystemInReader: End");
    }
  }

  private static String getOptionsFileName() {
    String[] args = Platform.getCommandLineArgs();
    for (int i = 0; i < args.length; i++) {
      if ("-ucd.options".equals(args[i]) && i < args.length - 1) {
        return args[i + 1];
      }
    }
    return null;
  }

  public void stop() {
    Log.info("Stopping UCDHeadless as an application");
  }
}