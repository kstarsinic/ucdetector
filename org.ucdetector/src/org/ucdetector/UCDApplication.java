/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import org.ucdetector.iterator.UCDetectorIterator;
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
    Object args = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
    if (args instanceof String[]) {
      String[] sArgs = (String[]) args;
      parseCommandLine(sArgs);
    }
    startImpl();
    return IApplication.EXIT_OK;
  }

  private void parseCommandLine(String[] sArgs) {
    String sBuildType = null;

    for (int i = 0; i < sArgs.length; i++) {
      //
      if (sArgs[i].equals("-projects")) {
        if (hasOptionValue(sArgs, i)) {
          projectsToIterate = Arrays.asList(sArgs[i + 1].split(","));
          i++;
        }
      }
      if (sArgs[i].equals("-buildtype")) {
        if (hasOptionValue(sArgs, i)) {
          sBuildType = sArgs[i + 1];
          i++;
        }
      }
      if (sArgs[i].equals("-options")) {
        if (hasOptionValue(sArgs, i)) {
          List<String> keyValues = Arrays.asList(sArgs[i + 1].split(","));
          for (String keyValue : keyValues) {
            int index = keyValue.indexOf("=");
            if (index != -1) {
              String key = keyValue.substring(0, index);
              String value = keyValue.substring(index + 1);
              ucdOptions.put(key, value);
            }
          }
        }
      }
    }
    //
    String info = (projectsToIterate == null) ? "ALL" : projectsToIterate.toString();
    Log.logInfo("\tprojects to detect: " + (info));
    //
    sBuildType = (sBuildType == null) ? "AUTO_BUILD" : sBuildType;
    Log.logInfo("\tBuildType         : " + sBuildType);
    if (BUILD_TYPES.containsKey(sBuildType)) {
      buildType = BUILD_TYPES.get(sBuildType).intValue();
    }
    else {
      buildType = IncrementalProjectBuilder.AUTO_BUILD;
    }
    Log.logInfo("\tucd option        : " + ucdOptions);
    Set<Entry<String, String>> optionSet = ucdOptions.entrySet();
    for (Entry<String, String> option : optionSet) {
      String key = option.getKey();
      String value = option.getValue();
      Log.logInfo("\tSet ucd option    : " + (key + "->" + value));
      Prefs.setUcdValue(key, value);
    }
    String prefs = UCDetectorPlugin.getPreferencesAsString();
    Log.logInfo(prefs.replace(", ", "\n\t"));
    IEclipsePreferences node = new DefaultScope().getNode(UCDetectorPlugin.ID);
    try {
      String avaiable = Arrays.asList(node.keys()).toString().replace(", ", "\n\t").replace("[", "\n\t").replace("]",
          "\n\t");
      Log.logInfo("\tAvaiable Options  : " + avaiable);
    }
    catch (BackingStoreException ex) {
      Log.logError("Can't get preferences", ex);
    }
    Log.logInfo("Report directory is: " + Prefs.getReportDir());
  }

  private boolean hasOptionValue(String[] sArgs, int i) {
    return i < (sArgs.length - 1) && !sArgs[i + 1].startsWith("-");
  }

  /**
   * @throws CoreException if an error occurs accessing the contents
   *    of its underlying resource
   */
  public void startImpl() throws CoreException {
    Log.logInfo("Run UCDetector");
    UCDetectorPlugin.setHeadlessMode(true);
    UCDProgressMonitor ucdMonitor = new UCDProgressMonitor();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();

    IProject[] projects = root.getProjects();
    List<IJavaProject> openProjects = new ArrayList<IJavaProject>();
    Log.logInfo("\tWorkspace: " + root.getLocation());

    Log.logInfo("\tprojects found in workspace: " + projects.length);
    for (IProject project : projects) {
      IJavaProject javaProject = JavaCore.create(project);
      String projectName = javaProject.getElementName();
      boolean ignore = projectsToIterate != null && !projectsToIterate.contains(projectName);
      if (ignore) {
        Log.logInfo("\tIgnore        : " + projectName);
        continue;
      }
      if (!javaProject.exists()) {
        Log.logInfo("\tDoes not exist: " + projectName + "\t\t if this is a problem, "
            + "open project in eclipse IDE, restart ant");
        continue;
      }
      if (!javaProject.isOpen()) {
        Log.logInfo("\tTry to open   : " + projectName);
        javaProject.open(ucdMonitor);
      }
      if (javaProject.isOpen()) {
        Log.logInfo("\tIs open       : " + projectName);
        openProjects.add(javaProject);
      }
      else {
        Log.logInfo("\tIs closed     : " + projectName);
      }
    }

    Log.logInfo("Refresh workspace...Please wait...!");
    root.refreshLocal(IResource.DEPTH_INFINITE, ucdMonitor);

    Log.logInfo("Build workspace... Please wait...!");
    workspace.build(buildType, ucdMonitor);

    UCDetectorIterator iterator = new UCDetectorIterator();
    iterator.setMonitor(ucdMonitor);
    Log.logInfo("Number of projects to iterate: " + openProjects.size());
    for (IJavaProject openProject : openProjects) {
      Log.logInfo("\tProject to iterate   : " + openProject.getElementName());
    }
    iterator.iterate(openProjects.toArray(new IJavaProject[openProjects.size()]));
  }

  public void stop() {
    Log.logInfo("Finished UCDetector");
  }
}
