/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.CoreException;
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
    systemInReader = new SystemInReader(ucdHeadless);
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
      InputStreamReader inStream = new InputStreamReader(System.in);
      BufferedReader reader = new BufferedReader(inStream);
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
      finally {
        try {
          inStream.close();
        }
        catch (Exception e) {
          // ignore
        }
      }
      Log.debug("SystemInReader: End");
    }
  }
}