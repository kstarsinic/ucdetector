/**
 * Copyright (c) 2009 Joerg Spieler
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
public class UCDApplication implements IApplication {
  /**
   * @see org.eclipse.core.resources.IncrementalProjectBuilder
   */
  private static final Map<String, Integer> buildTypes = new HashMap<String, Integer>();
  private static final Map<String, String> ucdOptions = new HashMap<String, String>();
  private List<String> projectsToIterate = null;
  private int buildType = 0;

  static {
    buildTypes.put("FULL_BUILD" //$NON-NLS-1$
        , Integer.valueOf(IncrementalProjectBuilder.FULL_BUILD));
    buildTypes.put("AUTO_BUILD"//$NON-NLS-1$
        , Integer.valueOf(IncrementalProjectBuilder.AUTO_BUILD));
    buildTypes.put("CLEAN_BUILD"//$NON-NLS-1$
        , Integer.valueOf(IncrementalProjectBuilder.CLEAN_BUILD));
    buildTypes.put("INCREMENTAL_BUILD"//$NON-NLS-1$
        , Integer.valueOf(IncrementalProjectBuilder.INCREMENTAL_BUILD));
  }

  public Object start(IApplicationContext context) throws Exception {
    Object args = context.getArguments().get(
        IApplicationContext.APPLICATION_ARGS);
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
      if (sArgs[i].equals("-projects")) { //$NON-NLS-1$
        if (hasOptionValue(sArgs, i)) {
          projectsToIterate = Arrays.asList(sArgs[i + 1].split(",")); //$NON-NLS-1$
          i++;
        }
      }
      if (sArgs[i].equals("-buildtype")) { //$NON-NLS-1$
        if (hasOptionValue(sArgs, i)) {
          sBuildType = sArgs[i + 1];
          i++;
        }
      }
      if (sArgs[i].equals("-ucdoptions")) { //$NON-NLS-1$
        if (hasOptionValue(sArgs, i)) {
          List<String> keyValues = Arrays.asList(sArgs[i + 1].split(",")); //$NON-NLS-1$
          for (String keyValue : keyValues) {
            int index = keyValue.indexOf("="); //$NON-NLS-1$
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
    String info = (projectsToIterate == null) ? "ALL" : projectsToIterate //$NON-NLS-1$
        .toString();
    Log.logInfo("\tprojects to detect: " + (info)); //$NON-NLS-1$
    // 
    sBuildType = (sBuildType == null) ? "AUTO_BUILD" : sBuildType; //$NON-NLS-1$
    Log.logInfo("\tBuildType         : " + sBuildType); //$NON-NLS-1$
    if (buildTypes.containsKey(sBuildType)) {
      buildType = buildTypes.get(sBuildType).intValue();
    }
    else {
      buildType = IncrementalProjectBuilder.AUTO_BUILD;
    }
    Log.logInfo("\tucd option        : " + ucdOptions); //$NON-NLS-1$
    Set<Entry<String, String>> optionSet = ucdOptions.entrySet();
    for (Entry<String, String> option : optionSet) {
      String key = option.getKey();
      String value = option.getValue();
      Log.logInfo("\tSet ucd option    : " + (key + "->" + value)); //$NON-NLS-1$ //$NON-NLS-2$
      Prefs.setUcdValue(key, value);
    }
    String prefs = UCDetectorPlugin.getPreferencesAsString();
    Log.logInfo(prefs.replace(", ", "\n\t")); //$NON-NLS-1$//$NON-NLS-2$
    IEclipsePreferences node = new DefaultScope().getNode(UCDetectorPlugin.ID);
    try {
      String[] avaiable = node.keys();
      Log.logInfo("\tAvaiable Options  : " + Arrays.asList(avaiable)); //$NON-NLS-1$
    }
    catch (BackingStoreException ex) {
      Log.logError("Can't get preferences", ex); //$NON-NLS-1$  
    }
  }

  private boolean hasOptionValue(String[] sArgs, int i) {
    return i < (sArgs.length - 1) && !sArgs[i + 1].startsWith("-");//$NON-NLS-1$
  }

  /**
   * @throws CoreException if an error occurs accessing the contents
   *    of its underlying resource
   */
  public void startImpl() throws CoreException {
    Log.logInfo("Run UCDetector"); //$NON-NLS-1$
    UCDetectorPlugin.setHeadlessMode(true);
    UCDProgressMonitor ucdMonitor = new UCDProgressMonitor();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();

    IProject[] projects = root.getProjects();
    List<IJavaProject> openProjects = new ArrayList<IJavaProject>();
    Log.logInfo("\tWorkspace: " + root.getLocation()); //$NON-NLS-1$

    Log.logInfo("\tprojects found in workspace: " + projects.length); //$NON-NLS-1$
    for (IProject project : projects) {
      IJavaProject javaProject = JavaCore.create(project);
      String projectName = javaProject.getElementName();
      boolean ignore = projectsToIterate != null
          && !projectsToIterate.contains(projectName);
      Log.logInfo("\t\tRun UCDetector " + !ignore + " for " + projectName); //$NON-NLS-1$ //$NON-NLS-2$
      if (ignore) {
        continue;
      }
      if (javaProject.exists() && !javaProject.isOpen()) {
        Log.logInfo("\t\topen project: " + projectName); //$NON-NLS-1$
        javaProject.open(ucdMonitor);
      }
      if (javaProject.isOpen()) {
        openProjects.add(javaProject);
      }
    }

    Log.logInfo("Refresh workspace...Please wait...!"); //$NON-NLS-1$
    root.refreshLocal(IResource.DEPTH_INFINITE, ucdMonitor);

    Log.logInfo("Build workspace... Please wait...!"); //$NON-NLS-1$
    workspace.build(buildType, ucdMonitor);

    UCDetectorIterator iterator = new UCDetectorIterator();
    iterator.setMonitor(ucdMonitor);
    Log.logInfo("Number of projects to iterate: " + openProjects.size()); //$NON-NLS-1$

    iterator.iterate(openProjects
        .toArray(new IJavaProject[openProjects.size()]));
  }

  public void stop() {
    Log.logInfo("Finished UCDetector"); //$NON-NLS-1$
  }
}
