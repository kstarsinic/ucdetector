/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.headless;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.search.UCDProgressMonitor;

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

  @Override
  public Object start(IApplicationContext context) throws Exception {
    Log.info("Starting UCDHeadless as an application");
    try {
      startImpl();
    }
    catch (Throwable ex) {
      // Catch all to avoid no log output, when UCDApplication crashes
      Log.error("Error running UCDApplication: " + ex, ex);
    }
    finally {
      Log.info("Finished UCDHeadless as an application");
    }
    return IApplication.EXIT_OK;
  }

  private void startImpl() throws FileNotFoundException, CoreException {
    UCDHeadless ucdHeadless = new UCDHeadless(getOptionsFileName());
    systemInReader = new SystemInReader(ucdHeadless.ucdMonitor);
    systemInReader.start();
    ucdHeadless.iterate();
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

  @Override
  public void stop() {
    Log.info("Stopping UCDHeadless as an application");
    if (systemInReader != null) {
      systemInReader.interrupt();
    }
  }

  // SystemInReader -----------------------------------------------------------
  /**
   * Exit, stop, continue UCDetector by typing commands to System.in.
   * <p>
   * @author Joerg Spieler
   * @since 11.04.2013
   */
  private static final class SystemInReader extends Thread {
    private final UCDProgressMonitor ucdMonitor;

    public SystemInReader(UCDProgressMonitor ucdMonitor) {
      this.ucdMonitor = ucdMonitor;
    }

    @Override
    public void run() {
      Log.info("SystemInReader: Start");
      dumpHelp();
      InputStreamReader inStream = new InputStreamReader(System.in);
      BufferedReader reader = new BufferedReader(inStream);
      String line;
      try {
        while ((line = reader.readLine()) != null) {
          Log.info("SystemInReader LINE: '" + line + "'");
          // break ------------------------------------------------------------
          if (isInterrupted()) {
            Log.warn("SystemInReader: Interrupted");
            break;
          }
          if (line.startsWith("e")) {
            Log.warn("SystemInReader: exit called!");
            if (ucdMonitor != null) {
              setSleep(false);
              ucdMonitor.setCanceled(true);
            }
            break;
          }
          // continue ---------------------------------------------------------
          if (line.length() == 0) {
            Log.info("Type 'h' ENTER to get help");
            continue;
          }
          if (line.startsWith("h") || line.startsWith("?")) {
            setSleep(true);
            dumpHelp();
            continue;
          }
          if (line.startsWith("s")) {
            setSleep(!ucdMonitor.isSleep());// Toggle
            continue;
          }
        }
      }
      catch (Exception ex) {
        Log.error("Exception reading System.in", ex);
      }
      finally {
        UCDetectorPlugin.closeSave(inStream);
      }
      Log.info("SystemInReader: End");
    }

    void setSleep(boolean sleep) {
      ucdMonitor.setSleep(sleep);
      Log.info("SystemInReader sleep: " + sleep);
    }

    private static void dumpHelp() {
      Log.info("========================================");
      Log.info("= USAGE: Type 'h' ENTER to get help");
      Log.info("= USAGE: Type 'e' ENTER to exit");
      Log.info("= USAGE: Type 's' ENTER to stop/continue");
      Log.info("========================================");
    }
  }
}