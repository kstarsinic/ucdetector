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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.ucdetector.iterator.UCDetectorIterator;
import org.ucdetector.search.UCDProgressMonitor;

/**
 * Run UCDetector in headless mode. Entry point is an eclipse application.
 * See also files: run.sh, run.bat, plugin.xml
 */
public class UCDApplication implements IApplication {

  public Object start(IApplicationContext context) throws Exception {
    List<String> projectsToIterate = null;
    Object args = context.getArguments().get(
        IApplicationContext.APPLICATION_ARGS);
    if (args instanceof String[]) {
      String[] sArgs = (String[]) args;
      if (sArgs.length > 1 && "-projects".equals(sArgs[0])) { //$NON-NLS-1$
        projectsToIterate = Arrays.asList(sArgs[1].split(",")); //$NON-NLS-1$
      }
    }
    startImpl(projectsToIterate);
    return IApplication.EXIT_OK;
  }

  /**
   * 
   */
  public static void startImpl(List<String> projectsToIterate)
      throws CoreException {
    UCDetectorPlugin.setHeadlessMode(true);
    UCDProgressMonitor ucdMonitor = new UCDProgressMonitor();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();
    root.refreshLocal(IResource.DEPTH_INFINITE, ucdMonitor);

    IProject[] projects = root.getProjects();
    List<IJavaProject> openProjects = new ArrayList<IJavaProject>();
    Log.logInfo("Run UCDetector"); //$NON-NLS-1$
    Log.logInfo("\tWorkspace: " + root.getLocation()); //$NON-NLS-1$
    if (projectsToIterate != null && projectsToIterate.size() > 0) {
      Log.logInfo("\tprojects to detect: " + projectsToIterate); //$NON-NLS-1$
    }
    else {
      Log.logInfo("\tstart parameter '-projects' not found." //$NON-NLS-1$
          + " Run UCDetector for all projects"); //$NON-NLS-1$
    }
    Log.logInfo("\tprojects found in workspace: " + projects.length); //$NON-NLS-1$
    for (IProject project : projects) {
      IJavaProject javaProject = JavaCore.create(project);
      String projectName = javaProject.getElementName();
      if (projectsToIterate != null && !projectsToIterate.contains(projectName)) {
        Log.logInfo("\t\tIGNORE: " + projectName // //$NON-NLS-1$
            + " (found in start parameter '-projects')"); //$NON-NLS-1$
        continue;
      }
      Log.logInfo("\t\t" + projectName); //$NON-NLS-1$
      if (javaProject.exists() && !javaProject.isOpen()) {
        Log.logInfo("\t\topen project: " + projectName); //$NON-NLS-1$
        javaProject.open(ucdMonitor);
      }
      if (javaProject.isOpen()) {
        openProjects.add(javaProject);
      }
    }
    UCDetectorIterator iterator = new UCDetectorIterator();
    iterator.setMonitor(ucdMonitor);
    Log.logInfo("Number of projects to iterate: " + openProjects.size()); //$NON-NLS-1$
    iterator.iterate(openProjects
        .toArray(new IJavaProject[openProjects.size()]));
  }

  public void stop() {
  }
}
