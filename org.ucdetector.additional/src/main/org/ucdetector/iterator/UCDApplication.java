/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.service.prefs.BackingStoreException;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.preferences.PreferenceInitializer;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.search.UCDProgressMonitor;

/**
 * Run UCDetector in headless mode. Entry point is an eclipse application.
 * <p>
 * See also files:
 * <ul>
 * <li>ant/detect.sh</li>
 * <li>ant/detect.bat</li>
 * <li>ant/build.xml</li>
 * </ul>
 * <p>
 * See feature request: [ 2653112 ] UCDetector should run as a ant task in
 * headless mode
 */
@SuppressWarnings("nls")
public class UCDApplication implements IApplication {
  /**
   * @see org.eclipse.core.resources.IncrementalProjectBuilder
   */
  private static final Map<String, Integer> BUILD_TYPES = new HashMap<String, Integer>();
  private final Map<String, String> ucdOptions = new HashMap<String, String>();
  private List<String> projectsToIterate = null;
  private int buildType = 0;

  static {
    BUILD_TYPES.put("FULL_BUILD", Integer.valueOf(IncrementalProjectBuilder.FULL_BUILD));
    BUILD_TYPES.put("AUTO_BUILD", Integer.valueOf(IncrementalProjectBuilder.AUTO_BUILD));
    BUILD_TYPES.put("CLEAN_BUILD", Integer.valueOf(IncrementalProjectBuilder.CLEAN_BUILD));
    BUILD_TYPES.put("INCREMENTAL_BUILD", Integer.valueOf(IncrementalProjectBuilder.INCREMENTAL_BUILD));
  }

  public Object start(IApplicationContext context) throws Exception {
    parseCommandLine((String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS));
    startImpl();
    return IApplication.EXIT_OK;
  }

  private void parseCommandLine(String[] sArgs) {
    String sBuildType = null;
    for (int i = 0; i < sArgs.length; i++) {
      boolean hasOptionValue = hasOptionValue(sArgs, i);
      Log.debug("sArgs[%s]=%-15s, hasOptionValue=%s", "" + i, sArgs[i], "" + hasOptionValue);
      if (sArgs[i].equals("-ucd.projects")) {
        if (hasOptionValue) {
          projectsToIterate = Arrays.asList(sArgs[i + 1].split(","));
        }
      }
      if (sArgs[i].equals("-ucd.buildtype")) {
        if (hasOptionValue) {
          sBuildType = sArgs[i + 1];
        }
      }
      if (sArgs[i].equals("-ucd.options")) {
        if (hasOptionValue) {
          List<String> keyValues = Arrays.asList(sArgs[i + 1].split(","));
          Log.debug("\tucd.options.keyValues=" + keyValues);
          for (String keyValue : keyValues) {
            int index = keyValue.indexOf("=");
            if (index != -1) {
              String key = keyValue.substring(0, index).trim();
              String value = keyValue.substring(index + 1).trim();
              ucdOptions.put(key, value);
            }
          }
        }
      }
      if (hasOptionValue) {
        i++;
      }
    }
    //
    String info = (projectsToIterate == null) ? "ALL" : projectsToIterate.toString();
    Log.info("\tprojects to detect: " + (info));
    //
    sBuildType = (sBuildType == null) ? "AUTO_BUILD" : sBuildType;
    Log.info("\tBuildType         : " + sBuildType);
    if (BUILD_TYPES.containsKey(sBuildType)) {
      buildType = BUILD_TYPES.get(sBuildType).intValue();
    }
    else {
      buildType = IncrementalProjectBuilder.AUTO_BUILD;
    }
    Log.info("\tInput ucd options : " + ucdOptions);
    Set<Entry<String, String>> optionSet = ucdOptions.entrySet();
    for (Entry<String, String> option : optionSet) {
      String key = option.getKey();
      String value = option.getValue();
      Log.info("\tSet ucd option    : %s=%s", key, value);
      Prefs.setValue(key, value);
    }
    String prefs = UCDetectorPlugin.getPreferencesAsString();
    Log.info(prefs.replace(", ", "\n\t"));
    IEclipsePreferences node = new DefaultScope().getNode(UCDetectorPlugin.ID);
    try {
      String[] keys = node.keys();
      Arrays.sort(keys);
      Log.info("Avaiable Options (to modify chang key 'ucd.options' in build.properties): ");
      for (String key : keys) {
        int startIndex = (UCDetectorPlugin.ID + ".").length();
        Log.info("\t" + key.substring(startIndex));
      }
    }
    catch (BackingStoreException ex) {
      Log.error("Can't get preferences for node: " + node, ex);
    }
    Log.info("Report directory is: " + PreferenceInitializer.getReportDir());
  }

  private boolean hasOptionValue(String[] sArgs, int i) {
    return i < (sArgs.length - 1) && !sArgs[i + 1].startsWith("-");
  }

  /**
   * @throws CoreException if an error occurs accessing the contents
   *    of its underlying resource
   */
  public void startImpl() throws CoreException {
    Log.info("Run UCDetector");
    UCDetectorPlugin.setHeadlessMode(true);
    UCDProgressMonitor ucdMonitor = new UCDProgressMonitor();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();

    IProject[] projects = root.getProjects();
    Log.info("\tprojects found in workspace (before create): " + projects.length);
    File rootDir = root.getLocation().toFile();
    File[] rootFiles = rootDir.listFiles();
    for (File rootFile : rootFiles) {
      File dotProject = new File(rootFile, ".project");
      if (dotProject.exists()) {
        IProject project = root.getProject(rootFile.getName());
        if (!project.exists()) {
          Log.info("\tCreate project for: " + rootFile.getAbsolutePath());
          project.create(ucdMonitor);
          project.open(ucdMonitor);
        }
      }
    }
    projects = root.getProjects();
    Log.info("\tprojects found in workspace (after  create): " + projects.length);
    List<IJavaProject> openProjects = new ArrayList<IJavaProject>();
    Log.info("\tWorkspace: " + root.getLocation());

    for (IProject project : projects) {
      IJavaProject javaProject = JavaCore.create(project);
      String projectName = javaProject.getElementName();
      boolean ignore = projectsToIterate != null && !projectsToIterate.contains(projectName);
      if (ignore) {
        Log.info("\tIgnore        : " + projectName);
        continue;
      }
      if (!javaProject.exists()) {
        Log.info("\tDoes not exist: %s\t\t if this is a problem, open project in eclipse IDE, restart ant", projectName);
        continue;
      }
      if (!javaProject.isOpen()) {
        Log.info("\tTry to open   : " + projectName);
        javaProject.open(ucdMonitor);
      }
      if (javaProject.isOpen()) {
        Log.info("\tIs open       : " + projectName);
        openProjects.add(javaProject);
      }
      else {
        Log.info("\tIs closed     : " + projectName);
      }
    }

    Log.info("Refresh workspace...Please wait...!");
    root.refreshLocal(IResource.DEPTH_INFINITE, ucdMonitor);

    Log.info("Build workspace... Please wait...!");
    workspace.build(buildType, ucdMonitor);

    UCDetectorIterator iterator = new UCDetectorIterator();
    iterator.setMonitor(ucdMonitor);
    Log.info("Number of projects to iterate: " + openProjects.size());
    for (IJavaProject openProject : openProjects) {
      Log.info("\tProject to iterate   : " + openProject.getElementName());
    }
    iterator.iterate(openProjects.toArray(new IJavaProject[openProjects.size()]));
  }

  public void stop() {
    Log.info("Finished UCDetector");
  }
}
