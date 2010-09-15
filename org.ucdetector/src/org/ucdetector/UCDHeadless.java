/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.ITargetHandle;
import org.eclipse.pde.internal.core.target.provisional.ITargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.LoadTargetDefinitionJob;
import org.ucdetector.iterator.UCDetectorIterator;
import org.ucdetector.preferences.PreferenceInitializer;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.search.UCDProgressMonitor;
import org.ucdetector.util.StopWatch;

@SuppressWarnings("nls")
public class UCDHeadless {
  private final UCDProgressMonitor ucdMonitor = new UCDProgressMonitor();
  private final List<String> resourcesToIterate;
  private final int buildType;
  private final File targetPlatformFile;

  public UCDHeadless(String buildType, String optionsFile, String targetPlatformFile, List<String> resourcesToIterate) {
    UCDetectorPlugin.setHeadlessMode(true);// MUST BE BEFORE LOGGING!
    this.buildType = getBuildType(buildType);
    this.targetPlatformFile = targetPlatformFile == null ? null : new File(targetPlatformFile);
    this.resourcesToIterate = resourcesToIterate;
    if (optionsFile != null) {
      loadOptions(optionsFile);
    }
  }

  /** @see org.eclipse.core.resources.IncrementalProjectBuilder */
  private int getBuildType(String sBuildType) {
    if ("FULL_BUILD".equals(sBuildType)) {
      return IncrementalProjectBuilder.FULL_BUILD;
    }
    if ("AUTO_BUILD".equals(sBuildType)) {
      return IncrementalProjectBuilder.AUTO_BUILD;
    }
    if ("INCREMENTAL_BUILD".equals(sBuildType)) {
      return IncrementalProjectBuilder.INCREMENTAL_BUILD;
    }
    if ("CLEAN_BUILD".equals(sBuildType)) {
      return IncrementalProjectBuilder.CLEAN_BUILD;
    }
    return IncrementalProjectBuilder.AUTO_BUILD;
  }

  private void loadOptions(String optionsFile) {
    File optionFile = new File(".", optionsFile);
    Log.logInfo("\toptionFile: %s exists: %s", Log.getCanonicalPath(optionFile), "" + optionFile.exists());
    if (optionFile.exists()) {
      Map<String, String> ucdOptions = UCDetectorPlugin.loadModeFile(true, optionFile.getAbsolutePath());
      for (Entry<String, String> option : ucdOptions.entrySet()) {
        Prefs.setValue(option.getKey(), option.getValue());
      }
      Log.logInfo(UCDetectorPlugin.getPreferencesAsString().replace(", ", "\n\t"));
      Log.logInfo("Report directory is: " + PreferenceInitializer.getReportDir());
    }
  }

  public void run() throws FileNotFoundException, CoreException {
    long start = System.currentTimeMillis();
    try {
      Log.logInfo("Starting UCDetector Headless");
      loadTargetPlatform();
      // Run it twice because of Exception, when running it with a complete workspace: See end of file
      //      Log.logInfo("Run 'load target platform' again, because of Exception, when running it with a complete workspace");
      //      loadTargetPlatform();
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
      iterate(workspaceRoot, allProjects);
    }
    finally {
      Log.logInfo("Time to run UCDetector Headless: " + StopWatch.timeAsString(System.currentTimeMillis() - start));
    }
  }

  private void loadTargetPlatform() throws CoreException, FileNotFoundException {
    if (targetPlatformFile == null) {
      Log.logInfo("Use eclipse as target platform");
      return;
    }
    Log.logInfo("Use target platform declared in: " + targetPlatformFile.getAbsolutePath());
    if (!targetPlatformFile.exists()) {
      throw new FileNotFoundException("Can't find target platform file: " + targetPlatformFile);
    }
    Log.logInfo("START: loadTargetPlatform");
    ITargetPlatformService tps = (ITargetPlatformService) PDECore.getDefault().acquireService(
        ITargetPlatformService.class.getName());
    ITargetHandle targetHandle = tps.getTarget(targetPlatformFile.toURI());
    ITargetDefinition targetDefinition = targetHandle.getTargetDefinition();
    new LoadTargetDefinitionJob(targetDefinition).run(ucdMonitor);
    //    LoadTargetDefinitionJob.load(targetDefinition);
    Log.logInfo("END: loadTargetPlatform");
  }

  private void iterate(IWorkspaceRoot workspaceRoot, List<IJavaProject> allProjects) throws CoreException {
    UCDetectorIterator iterator = new UCDetectorIterator();
    iterator.setMonitor(ucdMonitor);
    List<IJavaElement> javaElementsToIterate = new ArrayList<IJavaElement>();
    if (resourcesToIterate.isEmpty()) {
      javaElementsToIterate.addAll(allProjects);
    }
    else {
      for (String resourceToIterate : resourcesToIterate) {
        Path path = new Path(resourceToIterate);
        IJavaElement javaElement;
        if (path.segmentCount() == 1) {
          IProject project = workspaceRoot.getProject(resourceToIterate);
          javaElement = JavaCore.create(project);
          Log.logInfo("resource=%s, javaProject=%s", resourceToIterate, javaElement.getElementName());
        }
        else {
          IFolder folder = workspaceRoot.getFolder(path);
          javaElement = JavaCore.create(folder);
          Log.logInfo("resource=%s, folder=%s, javaElement=%s", resourceToIterate, folder, javaElement.getElementName());
        }
        javaElementsToIterate.add(javaElement);
      }
    }
    iterator.iterate(javaElementsToIterate.toArray(new IJavaElement[javaElementsToIterate.size()]));
  }

  private List<IJavaProject> createProjects(IProgressMonitor monitor, IWorkspaceRoot workspaceRoot)
      throws CoreException {
    List<IJavaProject> projects = new ArrayList<IJavaProject>();
    File rootDir = workspaceRoot.getLocation().toFile();
    File[] rootFiles = rootDir.listFiles();
    // ---------------------------------------------------------------------------
    // workspaceRoot.getProjects() DOES NOT WORK, when workspace is completely new 
    // We must use low level stuff here:
    // ---------------------------------------------------------------------------
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
      IJavaProject javaProject = JavaCore.create(project);
      // Running headless throws exception for no java projects!
      if (javaProject.exists()) {
        projects.add(javaProject);
        Log.logInfo("Project created: " + javaProject.getElementName());
      }
      else {
        Log.logWarn("Ignore project (maybe it is not a java project): " + javaProject.getElementName());
      }
    }
    return projects;
  }
}
//    [java] java.lang.NullPointerException
//    [java]   at org.eclipse.pde.internal.core.target.AbstractBundleContainer.getVMArguments(AbstractBundleContainer.java:525)
//    [java]   at org.eclipse.pde.internal.core.target.TargetPlatformService.newDefaultTargetDefinition(TargetPlatformService.java:525)
//    [java]   at org.eclipse.pde.internal.core.PluginModelManager.initDefaultTargetPlatformDefinition(PluginModelManager.java:575)
//    [java]   at org.eclipse.pde.internal.core.PluginModelManager.initializeTable(PluginModelManager.java:528)
//    [java]   at org.eclipse.pde.internal.core.PluginModelManager.getExternalModelManager(PluginModelManager.java:1013)
//    [java]   at org.eclipse.pde.internal.core.TargetPlatformResetJob.run(TargetPlatformResetJob.java:36)
//    [java]   at org.eclipse.core.internal.jobs.Worker.run(Worker.java:54)
