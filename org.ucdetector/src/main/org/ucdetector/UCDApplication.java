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
 * Run UCDetector from command line as an application in headless mode.
 * <p>
 * See files:
 * <pre>/org.ucdetector/ant/detect.sh</pre>
 * <pre>/org.ucdetector/ant/detect.bat</pre>
 * <pre>/org.ucdetector/ant/build.xml</pre>
 * <p>
 * @author Joerg Spieler
 * @since 31.03.2011
 */
@SuppressWarnings("nls")
public class UCDApplication implements IApplication {
  private SystemInReader systemInReader = null;

  public Object start(IApplicationContext context) throws Exception {
    Log.info("Starting UCDHeadless as an application");
    UCDHeadless ucdHeadless = new UCDHeadless(getOptionsFileName());
    systemInReader = new SystemInReader(ucdHeadless);
    systemInReader.start();
    ucdHeadless.iterate();
    return IApplication.EXIT_OK;
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
    if (systemInReader != null) {
      systemInReader.interrupt();
    }
  }

  // SystemInReader -----------------------------------------------------------

  private final class SystemInReader extends Thread {
    private final UCDHeadless ucdHeadless;

    public SystemInReader(UCDHeadless ucdHeadless) {
      this.ucdHeadless = ucdHeadless;
    }

    @Override
    public void run() {
      System.out.println("SystemInReader: Start");
      Log.info("------------------------------------");
      Log.info("Type 'exit' to to cancel UCDHeadless");
      Log.info("------------------------------------");
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      String line;
      try {
        while ((line = reader.readLine()) != null) {
          Log.debug("SystemInReader LINE: " + line);
          if ("exit".equals(line)) {
            Log.debug("SystemInReader: exit called!");
            if (ucdHeadless.ucdMonitor != null) {
              ucdHeadless.ucdMonitor.setCanceled(true);
            }
            break;
          }
          if (isInterrupted()) {
            Log.debug("SystemInReader: Interrupted");
            break;
          }
        }
      }
      catch (Exception ex) {
        Log.error("Exception reading System.in", ex);
      }
      Log.debug("SystemInReader: End");
    }
  }
}