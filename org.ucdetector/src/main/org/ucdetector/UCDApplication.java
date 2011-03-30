/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.ucdetector.preferences.Prefs;

/**
 * 
 * Run UCDetector from command line as an application in headless mode (experimental):
 * <pre>$ eclipse -application org.ucdetector.detect</pre>
 * <p>
 * This class seaches for options files in user dir.
 * <p>
 * @author Joerg Spieler
 * @since 31.03.2011
 */
@SuppressWarnings("nls")
public class UCDApplication implements IApplication {
  private static final String APPLICATION_KEY = Prefs.INTERNAL + ".application.";

  public Object start(IApplicationContext context) throws Exception {
    Log.info("Starting UCDHeadless as an application");
    String userDir = System.getProperty("user.dir");
    File optionsFile = new File(userDir, "ucdetector.options");
    File targetFile = new File(userDir, "ucdetector.target");
    Log.info("To change detection, %s: %s", optionsFile.exists() ? "edit" : "create", optionsFile.getAbsolutePath());
    Log.info("To change detection, %s: %s", targetFile.exists() ? "edit" : "create", targetFile.getAbsolutePath());

    Map<String, String> options = UCDHeadless.loadOptions(optionsFile);
    String buildType = options.get(APPLICATION_KEY + "buildType");
    String report = options.get(APPLICATION_KEY + "report");
    String resources = options.get(APPLICATION_KEY + "resourcesToIterate");
    List<String> resourcesToIterate = resources == null ? null : Arrays.asList(resources.split(","));
    UCDHeadless headless = new UCDHeadless(buildType, optionsFile, targetFile, report, resourcesToIterate);
    headless.run();
    return IApplication.EXIT_OK;
  }

  public void stop() {
    Log.info("Stopping UCDHeadless (application mode)");
  }
}