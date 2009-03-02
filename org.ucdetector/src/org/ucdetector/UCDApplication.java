/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.util.ArrayList;
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
    startImpl();
    return IApplication.EXIT_OK;
  }

  /**
   * 
   */
  public static void startImpl() throws CoreException {
    UCDetectorPlugin.setHeadlessMode(true);
    UCDProgressMonitor ucdMonitor = new UCDProgressMonitor();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();
    root.refreshLocal(IResource.DEPTH_INFINITE, ucdMonitor);

    IProject[] projects = root.getProjects();
    List<IJavaProject> openProjects = new ArrayList<IJavaProject>();
    Log.logInfo("Number of projects: " + projects.length); //$NON-NLS-1$
    for (IProject project : projects) {
      IJavaProject javaProject = JavaCore.create(project);
      if (javaProject.exists() && !javaProject.isOpen()) {
        Log.logInfo("open project: " + javaProject.getElementName()); //$NON-NLS-1$
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
