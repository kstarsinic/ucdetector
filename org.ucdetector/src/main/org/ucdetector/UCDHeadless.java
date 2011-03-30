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
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.ITargetHandle;
import org.eclipse.pde.internal.core.target.provisional.ITargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.LoadTargetDefinitionJob;
import org.osgi.framework.Bundle;
import org.ucdetector.iterator.UCDetectorIterator;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.search.UCDProgressMonitor;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.StopWatch;

/**
 * Run UCDetector in headless mode (no UI). Entry point is an eclipse application.
 * <p>
 * @author Joerg Spieler
 * @since 2010-09-15
 */
@SuppressWarnings("nls")
public class UCDHeadless implements IApplication {
  private final UCDProgressMonitor ucdMonitor = new UCDProgressMonitor();
  private final int buildType;
  private File targetPlatformFile;
  private final Report report;
  private final List<String> resourcesToIterate;
  private File optionsFile;

  public enum Report {
    single, eachproject
  }

  public UCDHeadless() {
    this(-1, null, null, null, null);
  }

  public UCDHeadless(int buildType, File optionsFile, File targetPlatformFile, Report report,
      List<String> resourcesToIterate) {
    UCDetectorPlugin.setHeadlessMode(true);// MUST BE BEFORE LOGGING!
    this.buildType = (buildType == -1) ? IncrementalProjectBuilder.AUTO_BUILD : buildType;
    this.optionsFile = optionsFile;
    loadOptions(optionsFile);
    this.targetPlatformFile = targetPlatformFile;
    this.report = report;
    this.resourcesToIterate = resourcesToIterate;
  }

  public Object start(IApplicationContext context) throws Exception {
    Log.info("Starting UCDHeadless as an application");
    String userDir = System.getProperty("user.dir");
    this.optionsFile = new File(userDir, "ucdetector.options");
    this.targetPlatformFile = new File(userDir, "ucdetector.target");
    Log.info("To change detection, use: " + optionsFile.getAbsolutePath());
    Log.info("To change detection, use: " + targetPlatformFile.getAbsolutePath());
    run();
    return IApplication.EXIT_OK;
  }

  public void stop() {
    Log.info("Stopping UCDHeadless (application mode)");
  }

  private void loadOptions(File optionFile) {
    if (optionsFile != null) {
      Log.info("\toptionFile: %s exists: %s", Log.getCanonicalPath(optionFile), "" + optionFile.exists());
      if (optionFile.exists()) {
        Map<String, String> ucdOptions = UCDetectorPlugin.loadModeFile(true, optionFile.getAbsolutePath());
        for (Entry<String, String> option : ucdOptions.entrySet()) {
          Prefs.setValue(option.getKey(), option.getValue());
        }
        Log.info(UCDetectorPlugin.getPreferencesAsString().replace(", ", "\n\t"));
      }
    }
  }

  public void run() throws FileNotFoundException, CoreException {
    long start = System.currentTimeMillis();
    try {
      Log.info("Starting UCDetector Headless");
      try {
        // Log.info("TRY TO START DS  - eclipse bug 314814");
        Bundle fwAdminBundle = Platform.getBundle("org.eclipse.equinox.ds");
        fwAdminBundle.start();
      }
      catch (Exception e) {
        Log.error("PROBLEMS STARTING DS", e);
      }
      loadTargetPlatform();
      // Run it twice because of Exception, when running it with a complete workspace: See end of file
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=314814
      // Log.logInfo("Load target platform again, because of Exception - eclipse bug 314814");
      //loadTargetPlatform();
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      IWorkspaceRoot workspaceRoot = workspace.getRoot();
      //
      StopWatch stopWatch = new StopWatch();
      List<IJavaProject> allProjects = createProjects(ucdMonitor, workspaceRoot);
      Log.info(stopWatch.end("createProjects", false));
      //
      IProject[] projects = workspaceRoot.getProjects();
      Log.info("\tprojects found in workspace: " + projects.length);
      Log.info("\tWorkspace: " + workspaceRoot.getLocation());
      Log.info("Refresh workspace... Please wait...!");
      workspaceRoot.refreshLocal(IResource.DEPTH_INFINITE, ucdMonitor);
      Log.info(stopWatch.end("Refresh workspace", false));
      //
      Log.info("Build workspace... Please wait...!");
      workspace.build(buildType, ucdMonitor);
      Log.info(stopWatch.end("Build workspace", false));
      //
      if (projects.length == 0) {
        Log.warn("NO PROJECTS FOUND - NOTHING TODO");
      }
      iterate(workspaceRoot, allProjects);
    }
    finally {
      Log.info("Time to run UCDetector Headless: " + StopWatch.timeAsString(System.currentTimeMillis() - start));
    }
  }

  private void loadTargetPlatform() throws CoreException, FileNotFoundException {
    if (targetPlatformFile == null) {
      Log.info("Use eclipse as target platform");
      return;
    }
    StopWatch stopWatch = new StopWatch();
    Log.info("Use target platform declared in: " + targetPlatformFile.getAbsolutePath());
    if (!targetPlatformFile.exists()) {
      throw new FileNotFoundException("Can't find target platform file: " + targetPlatformFile);
    }
    Log.info("START: loadTargetPlatform");
    ITargetPlatformService tps = (ITargetPlatformService) PDECore.getDefault().acquireService(
        ITargetPlatformService.class.getName());
    ITargetHandle targetHandle = tps.getTarget(targetPlatformFile.toURI());
    ITargetDefinition targetDefinition = targetHandle.getTargetDefinition();
    new LoadTargetDefinitionJob(targetDefinition).run(ucdMonitor);
    //    LoadTargetDefinitionJob.load(targetDefinition);
    Log.info(stopWatch.end("END: loadTargetPlatform", false));
  }

  private void iterate(IWorkspaceRoot workspaceRoot, List<IJavaProject> allProjects) throws CoreException {
    List<IJavaElement> javaElementsToIterate = new ArrayList<IJavaElement>();
    if (resourcesToIterate == null || resourcesToIterate.isEmpty()) {
      javaElementsToIterate.addAll(allProjects);
    }
    else {
      for (String resourceToIterate : resourcesToIterate) {
        Path path = new Path(resourceToIterate);
        IJavaElement javaElement;
        if (path.segmentCount() == 1) {
          IProject project = workspaceRoot.getProject(resourceToIterate);
          javaElement = JavaCore.create(project);
          Log.info("resource=%s, javaProject=%s", resourceToIterate, javaElement.getElementName());
        }
        else {
          IFolder folder = workspaceRoot.getFolder(path);
          javaElement = JavaCore.create(folder);
          Log.info("resource=%s, folder=%s, javaElement=%s", resourceToIterate, folder, javaElement.getElementName());
        }
        if (!javaElement.exists()) {
          Log.warn("Ignore resource: '%s'. Possible reasons: It is not a java element, it does not exists",
              resourceToIterate);
        }
        javaElementsToIterate.add(javaElement);
      }
    }
    // Logging
    Log.info("There are %s java elements to iterate", String.valueOf(javaElementsToIterate.size()));//$NON-NLS-1$
    for (IJavaElement javaElement : javaElementsToIterate) {
      Log.info("    " + JavaElementUtil.getElementName(javaElement));//$NON-NLS-1$
    }
    // Iterate
    if (report == null || Report.eachproject == report) {
      for (IJavaElement javaElement : javaElementsToIterate) {
        UCDetectorIterator iterator = new UCDetectorIterator();
        iterator.setMonitor(ucdMonitor);
        iterator.iterate(new IJavaElement[] { javaElement });
      }
    }
    else {
      UCDetectorIterator iterator = new UCDetectorIterator();
      iterator.setMonitor(ucdMonitor);
      iterator.iterate(javaElementsToIterate.toArray(new IJavaElement[javaElementsToIterate.size()]));
    }
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
        Log.info("\tCreate project for: " + rootFile.getAbsolutePath());
        project.create(monitor);
      }
      project.open(monitor);
      IJavaProject javaProject = JavaCore.create(project);
      // Running headless throws exception for no java projects!
      if (javaProject.exists()) {
        projects.add(javaProject);
        Log.info("Project created: " + javaProject.getElementName());
      }
      else {
        Log.warn("Ignore project '%s'. Maybe it is not a java project!", javaProject.getElementName());
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
