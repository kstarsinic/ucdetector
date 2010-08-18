/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.ucdetector.iterator.UCDetectorIterator;
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
  /** @see org.eclipse.core.resources.IncrementalProjectBuilder */
  private static final Map<String, Integer> BUILD_TYPES = new HashMap<String, Integer>();
  private List<String> resourcesToIterate = Collections.emptyList();
  private int buildType = 0;

  private static final String AUTO_BUILD = "AUTO_BUILD";
  static {
    BUILD_TYPES.put("FULL_BUILD", Integer.valueOf(IncrementalProjectBuilder.FULL_BUILD));
    BUILD_TYPES.put(AUTO_BUILD, Integer.valueOf(IncrementalProjectBuilder.AUTO_BUILD));
    BUILD_TYPES.put("INCREMENTAL_BUILD", Integer.valueOf(IncrementalProjectBuilder.INCREMENTAL_BUILD));
    BUILD_TYPES.put("CLEAN_BUILD", Integer.valueOf(IncrementalProjectBuilder.CLEAN_BUILD));
  }

  public Object start(IApplicationContext context) throws Exception {
    UCDetectorPlugin.setHeadlessMode(true);// MUST BE BEFORE LOGGING!
    Log.logInfo("Starting UCDetector Application");
    parseCommandLine((String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS));
    startImpl();
    return IApplication.EXIT_OK;
  }

  private void parseCommandLine(String[] sArgs) {
    String sBuildType = AUTO_BUILD;
    Map<String, String> ucdOptions = Collections.emptyMap();
    for (int i = 0; i < sArgs.length; i++) {
      boolean hasOptionValue = hasOptionValue(sArgs, i);
      Log.logDebug("sArgs[%s]=%-15s, hasOptionValue=%s", "" + i, sArgs[i], "" + hasOptionValue);
      if (sArgs[i].equals("-ucd.projects")) {
        if (hasOptionValue) {
          resourcesToIterate = Arrays.asList(sArgs[i + 1].split(","));
        }
      }
      if (sArgs[i].equals("-ucd.buildtype")) {
        if (hasOptionValue) {
          sBuildType = sArgs[i + 1];
        }
      }
      if (sArgs[i].equals("-ucd.options")) {
        if (hasOptionValue) {
          String optionFileName = sArgs[i + 1];
          File optionFile = new File(".", optionFileName);
          Log.logInfo("\toptionFile: %s exists: %s", Log.getCanonicalPath(optionFile), "" + optionFile.exists());
          if (optionFile.exists()) {
            ucdOptions = UCDetectorPlugin.loadModeFile(true, optionFile.getAbsolutePath());
          }
        }
      }
      if (hasOptionValue) {
        i++;
      }
    }
    Log.logInfo("\tresources to iterate: " + (resourcesToIterate == null ? "ALL" : resourcesToIterate.toString()));
    Log.logInfo("\tBuildType         : " + sBuildType);
    buildType = BUILD_TYPES.containsKey(sBuildType) ? BUILD_TYPES.get(sBuildType).intValue()
        : IncrementalProjectBuilder.AUTO_BUILD;
    Set<Entry<String, String>> optionSet = ucdOptions.entrySet();
    for (Entry<String, String> option : optionSet) {
      //Log.logInfo("\tSet ucd option    : %s=%s", option.getKey(), option.getValue());
      Prefs.setValue(option.getKey(), option.getValue());
    }
    String prefs = UCDetectorPlugin.getPreferencesAsString();
    Log.logInfo(prefs.replace(", ", "\n\t"));
    Log.logInfo("Report directory is: " + PreferenceInitializer.getReportDir());
  }

  private boolean hasOptionValue(String[] sArgs, int i) {
    return i < (sArgs.length - 1) && !sArgs[i + 1].startsWith("-");
  }

  /** @throws CoreException if an error occurs accessing the contents of its underlying resource */
  public void startImpl() throws CoreException {
    Log.logInfo("Run UCDetector");
    UCDProgressMonitor ucdMonitor = new UCDProgressMonitor();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot workspaceRoot = workspace.getRoot();
    List<IJavaProject> allProjects = createProjects(ucdMonitor, workspaceRoot);
    IProject[] projects = workspaceRoot.getProjects();
    Log.logInfo("\tprojects found in workspace: " + projects.length);
    Log.logInfo("\tWorkspace: " + workspaceRoot.getLocation());
    Log.logInfo("Refresh workspace... Please wait...!");
    workspaceRoot.refreshLocal(IResource.DEPTH_INFINITE, ucdMonitor);
    Log.logInfo("Build workspace... Please wait...!");
    workspace.build(buildType, ucdMonitor);

    UCDetectorIterator iterator = new UCDetectorIterator();
    iterator.setMonitor(ucdMonitor);
    List<IJavaElement> javaElementsToIterate = new ArrayList<IJavaElement>();
    if (resourcesToIterate.isEmpty()) {
      javaElementsToIterate.addAll(allProjects);
    }
    else {
      for (String resourceToIterate : resourcesToIterate) {
        Path path = new Path(resourceToIterate);
        System.out.println("path: " + path);
        if (path.segmentCount() == 1) {
          IProject project = workspaceRoot.getProject(resourceToIterate);
          IJavaProject javaProject = JavaCore.create(project);
          javaProject.open(ucdMonitor);
          Log.logInfo("resource=%s, javaProject=%s", resourceToIterate, javaProject);
          javaElementsToIterate.add(javaProject);
        }
        else {
          IFolder folder = workspaceRoot.getFolder(path);
          IJavaElement javaElement = JavaCore.create(folder);
          Log.logInfo("resource=%s, folder=%s, javaElement=%s", resourceToIterate, folder, javaElement);
          javaElementsToIterate.add(javaElement);
        }
      }
    }
    iterator.iterate(javaElementsToIterate.toArray(new IJavaElement[javaElementsToIterate.size()]));
  }

  private List<IJavaProject> createProjects(IProgressMonitor monitor, IWorkspaceRoot workspaceRoot)
      throws CoreException {
    List<IJavaProject> projects = new ArrayList<IJavaProject>();
    File rootDir = workspaceRoot.getLocation().toFile();
    File[] rootFiles = rootDir.listFiles();
    for (File rootFile : rootFiles) {
      File dotProject = new File(rootFile, ".project");
      if (!dotProject.exists()) {
        continue;
      }
      IProject project = workspaceRoot.getProject(rootFile.getName());
      if (!project.exists()) {
        Log.logInfo("\tCreate project for: " + rootFile.getAbsolutePath());
        project.create(monitor);
      }
      project.open(monitor);
      projects.add(JavaCore.create(project));
    }
    return projects;
  }

  public void stop() {
    Log.logInfo("Stopping UCDetector Application");
  }
}