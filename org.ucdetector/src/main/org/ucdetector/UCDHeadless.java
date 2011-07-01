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
import java.util.Collections;
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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.ITargetHandle;
import org.eclipse.pde.internal.core.target.provisional.ITargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.LoadTargetDefinitionJob;
import org.osgi.framework.Bundle;
import org.ucdetector.iterator.AbstractUCDetectorIterator;
import org.ucdetector.iterator.HeadlessExtension;
import org.ucdetector.iterator.UCDetectorIterator;
import org.ucdetector.preferences.ModesReader;
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
public class UCDHeadless {
  /**  "org.ucdetector.internal.headless." */
  private static final String HEADLESS_KEY = Prefs.INTERNAL + ".headless.";

  private static final String INCREMENTAL_BUILD = "INCREMENTAL_BUILD";
  private final UCDProgressMonitor ucdMonitor = new UCDProgressMonitor();
  private final int buildType;
  private final File targetPlatformFile;
  private final Report report;
  private final List<String> resourcesToIterate;
  private final IWorkspace workspace;

  public enum Report {
    single, eachproject
  }

  {
    UCDetectorPlugin.setHeadlessMode(true);// CALL BEFORE LOGGING!
  }

  public UCDHeadless(String optionsFileName) throws FileNotFoundException {
    Log.info("Options file name: %s", optionsFileName);
    this.workspace = ResourcesPlugin.getWorkspace();
    File optionsFile = getFile(optionsFileName, "ucdetector.options");
    Map<String, String> options = loadOptions(optionsFile);
    this.targetPlatformFile = getFile(options.get(HEADLESS_KEY + "targetPlatformFile"), "ucdetector.target");
    String sBuildType = options.get(HEADLESS_KEY + "buildType");
    this.buildType = parseBuildType(sBuildType);
    this.report = parseReport(options.get(HEADLESS_KEY + "report"));
    this.resourcesToIterate = getResourcesToIterate(options);
    String iterateInfo = resourcesToIterate.isEmpty() ? "ALL" : //
        resourcesToIterate.size() + " elements: " + resourcesToIterate;
    Log.info("----------------------------------------------------------------------");
    logExists(optionsFile);
    logExists(targetPlatformFile);
    Log.info("    iterate           : " + iterateInfo);
    Log.info("    buildType         : " + (sBuildType == null ? INCREMENTAL_BUILD : sBuildType));
    Log.info("    report            : " + report);
    Log.info("----------------------------------------------------------------------");
    //    new SystemInReader().run();
  }

  //  private final class SystemInReader extends Thread {
  //    @Override
  //    public void run() {
  //      Log.info("SystemInReader: Start");
  //      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  //      String line;
  //      try {
  //        while ((line = reader.readLine()) != null) {
  //          Log.info("SystemInReader LINE: " + line);
  //          if ("exit".equals(line)) {
  //            Log.info("SystemInReader: exit called!");
  //            ucdMonitor.setCanceled(true);
  //            // closeWorkspace();
  //          }
  //          if (isInterrupted()) {
  //            Log.info("SystemInReader: Interrupted");
  //            break;
  //          }
  //        }
  //        Log.info("SystemInReader: End");
  //      }
  //      catch (Exception ex) {
  //        Log.error("SystemInReader: Error", ex);
  //      }
  //    }
  //  }

  //  public static void main(String[] args) {
  //    new SystemInReader2().run();
  //  }
  //
  //  private static final class SystemInReader2 extends Thread {
  //    @Override
  //    public void run() {
  //      System.out.println("SystemInReader: Start");
  //      InputStreamReader inStream = new InputStreamReader(System.in);
  //      try {
  //        while (true) {
  //          int read = inStream.read();
  //          System.out.println("SystemInReader read: " + read);
  //          if (isInterrupted()) {
  //            System.out.println("SystemInReader: Interrupted");
  //            break;
  //          }
  //          if (read == -1) {
  //            System.out.println("SystemInReader: read == -1");
  //            break;
  //          }
  //        }
  //        System.out.println("SystemInReader: End");
  //      }
  //      catch (Exception ex) {
  //        ex.printStackTrace();
  //      }
  //    }
  //  }

  private static File getFile(String fileName, String defaultFileName) throws FileNotFoundException {
    if (fileName == null || fileName.trim().length() == 0) {
      return new File(defaultFileName);
    }
    String resultName = fileName;
    resultName = resultName.replace("${WORKSPACE}", UCDetectorPlugin.getAboutWorkspace());
    resultName = resultName.replace("${ECLIPSE_HOME}", UCDetectorPlugin.getAboutEclipseHome());
    File resultFile = new File(UCDetectorPlugin.getCanonicalPath(resultName));
    if (!resultFile.exists()) {
      throw new FileNotFoundException("Missing file: " + resultFile.getAbsolutePath());
    }
    return resultFile;
  }

  private static List<String> getResourcesToIterate(Map<String, String> options) {
    List<String> result = new ArrayList<String>();
    String resourcesToIterateString = options.get(HEADLESS_KEY + "resourcesToIterate");
    if (resourcesToIterateString != null) {
      String[] resourcesList = resourcesToIterateString.split(",");
      for (String resourceName : resourcesList) {
        resourceName = resourceName.trim();
        if (resourceName.length() > 0) {
          result.add(resourceName);
        }
      }
    }
    return result;
  }

  private static void logExists(File file) {
    Log.info("To change detection   : %-6s %s", file.exists() ? "edit" : "create",
        UCDetectorPlugin.getCanonicalPath(file));
  }

  static Map<String, String> loadOptions(File optionFile) {
    Map<String, String> ucdOptions = Collections.emptyMap();
    Log.info("   optionFile: %s exists: %s", UCDetectorPlugin.getCanonicalPath(optionFile), "" + optionFile.exists());
    if (optionFile.exists()) {
      ucdOptions = ModesReader.loadModeFile(true, optionFile.getAbsolutePath());
      for (Entry<String, String> option : ucdOptions.entrySet()) {
        Prefs.setValue(option.getKey(), option.getValue());
      }
      Log.info(UCDetectorPlugin.getPreferencesAsString().replace(", ", "\n\t"));
    }
    return ucdOptions;
  }

  public void run() throws CoreException {
    long start = System.currentTimeMillis();
    try {
      Log.info("Starting UCDetector Headless");
      tryToStartDsPlugin();
      loadTargetPlatform();
      // Run it twice because of Exception, when running it with a complete workspace: See end of file
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=314814
      // Log.logInfo("Load target platform again, because of Exception - eclipse bug 314814");
      //loadTargetPlatform();
      IWorkspaceRoot workspaceRoot = workspace.getRoot();
      List<IJavaProject> allProjects = createProjects(ucdMonitor, workspaceRoot);
      prepareWorkspace();
      //
      List<IJavaElement> javaElementsToIterate = getJavaElementsToIterate(workspaceRoot, allProjects);
      iterateImpl(javaElementsToIterate);
      postIterate(javaElementsToIterate);
    }
    finally {
      closeWorkspace();
      Log.info("Time to run UCDetector Headless: " + StopWatch.timeAsString(System.currentTimeMillis() - start));
    }
  }

  private void closeWorkspace() {
    StopWatch stopWatch = new StopWatch();
    try {
      workspace.save(true, ucdMonitor);
      // causes npe: at org.eclipse.core.internal.resources.Workspace.removeResourceChangeListener(Workspace.java:2302)
      // if (workspace instanceof Workspace) {
      //   ((Workspace) workspace).close(ucdMonitor);
      // }
    }
    catch (Exception ex) {
      Log.error("Can't close workspace", ex);
    }
    Log.info(stopWatch.end("Time to close workspace")); //$NON-NLS-1$
  }

  /** See eclipse bug 314814 */
  private static void tryToStartDsPlugin() {
    try {
      Bundle fwAdminBundle = Platform.getBundle("org.eclipse.equinox.ds");
      fwAdminBundle.start();
    }
    catch (Exception e) {
      Log.error("PROBLEMS STARTING DS", e);
    }
  }

  private void loadTargetPlatform() throws CoreException {
    if (targetPlatformFile == null || !targetPlatformFile.exists()) {
      Log.info("Target platform file missing: Use eclipse as target platform");
      return;
    }
    StopWatch stopWatch = new StopWatch();
    Log.info("Use target platform declared in: " + targetPlatformFile.getAbsolutePath());
    Log.info("START: loadTargetPlatform");
    ITargetPlatformService tps = (ITargetPlatformService) PDECore.getDefault().acquireService(
        ITargetPlatformService.class.getName());
    ITargetHandle targetHandle = tps.getTarget(targetPlatformFile.toURI());
    ITargetDefinition targetDefinition = targetHandle.getTargetDefinition();
    new LoadTargetDefinitionJob(targetDefinition).run(ucdMonitor);
    //    LoadTargetDefinitionJob.load(targetDefinition);
    Log.info(stopWatch.end("END: loadTargetPlatform", false));
  }

  private void prepareWorkspace() throws CoreException {
    StopWatch stopWatch = new StopWatch();
    IWorkspaceRoot workspaceRoot = workspace.getRoot();
    Log.info(stopWatch.end("createProjects", false));
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
  }

  private void iterateImpl(List<IJavaElement> javaElementsToIterate) throws CoreException {
    if (report == null || Report.eachproject == report) {
      for (IJavaElement javaElement : javaElementsToIterate) {
        AbstractUCDetectorIterator iterator = new UCDetectorIterator();
        iterator.setMonitor(ucdMonitor);
        iterator.iterate(new IJavaElement[] { javaElement });
      }
    }
    else {
      AbstractUCDetectorIterator iterator = new UCDetectorIterator();
      iterator.setMonitor(ucdMonitor);
      iterator.iterate(javaElementsToIterate);
    }
  }

  private void postIterate(List<IJavaElement> javaElementsToIterate) throws CoreException {
    ArrayList<AbstractUCDetectorIterator> postIterators = HeadlessExtension.getPostIterators();
    for (AbstractUCDetectorIterator postIterator : postIterators) {
      Log.info("Run Post iterator: %s, for: %s",//
          postIterator.getJobName(), JavaElementUtil.getElementNames(javaElementsToIterate));
      postIterator.setMonitor(ucdMonitor);
      postIterator.iterate(javaElementsToIterate);
    }
  }

  private List<IJavaElement> getJavaElementsToIterate(IWorkspaceRoot workspaceRoot, List<IJavaProject> allProjects) {
    List<IJavaElement> javaElementsToIterate = new ArrayList<IJavaElement>();
    if (resourcesToIterate == null || resourcesToIterate.isEmpty()) {
      javaElementsToIterate.addAll(allProjects);
    }
    else {
      for (String resourceToIterate : resourcesToIterate) {
        Log.info("resourceToIterate: " + resourceToIterate);
        try {
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
            Log.info("resource=%s, folder=%s, javaElement=%s", resourceToIterate, folder,
                JavaElementUtil.getElementName(javaElement));
          }
          if (javaElement == null || !javaElement.exists()) {
            Log.warn("Ignore resource: '%s'. Possible reasons: It is not a java element, it does not exists",
                resourceToIterate);
            continue;
          }
          javaElementsToIterate.add(javaElement);
        }
        catch (Exception ex) {
          Log.warn("Ignore resource: '%s' because %s", resourceToIterate, ex);
        }
      }
    }
    // Logging
    Log.info("There are %s java elements to iterate", String.valueOf(javaElementsToIterate.size()));//$NON-NLS-1$
    for (IJavaElement javaElement : javaElementsToIterate) {
      Log.info("    " + JavaElementUtil.getElementName(javaElement));//$NON-NLS-1$
    }
    return javaElementsToIterate;
  }

  private static List<IJavaProject> createProjects(IProgressMonitor monitor, IWorkspaceRoot workspaceRoot)
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

  private static Report parseReport(String reportString) {
    if (reportString == null || reportString.length() == 0) {
      return Report.eachproject;
    }
    for (Report rep : Report.values()) {
      if (rep.name().equals(reportString)) {
        return rep;
      }
    }
    Log.warn("Unknown report: '%s'. Using: %s", reportString, Report.eachproject);
    return Report.eachproject;
  }

  /** @see org.eclipse.core.resources.IncrementalProjectBuilder */
  private static int parseBuildType(String buildType) {
    return "FULL_BUILD".equals(buildType) ? IncrementalProjectBuilder.FULL_BUILD
        : IncrementalProjectBuilder.INCREMENTAL_BUILD;
  }
}
