package org.ucdetector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.ucdetector.iterator.UCDetectorIterator;
import org.ucdetector.search.UCDProgressMonitor;

/**
 *
 */
public class UCDApplication implements IApplication {

  public Object start(IApplicationContext context) throws Exception {
    System.out.println("Start: Helo World UCDetector");
    UCDProgressMonitor ucdMonitor = new UCDProgressMonitor();
    System.out.println("Start: Refresh");
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();
    root.refreshLocal(IResource.DEPTH_INFINITE, ucdMonitor);

    IProject[] projects = root.getProjects();
    System.out.println("projects in workspace=" + projects.length);
    List<IJavaProject> openProjects = new ArrayList<IJavaProject>();
    for (IProject tempProject : projects) {
      IJavaProject project = JavaCore.create(tempProject);
      if (project.exists() && !project.isOpen()) {
        System.out.println("open project" + project.getElementName());
        project.open(ucdMonitor);
      }
      if (project.isOpen()) {
        openProjects.add(project);
      }
    }
    System.out.println("project to iterate=" + openProjects);
    UCDetectorIterator iterator = new UCDetectorIterator();
    iterator.setMonitor(ucdMonitor);
    iterator.iterate(openProjects
        .toArray(new IJavaProject[openProjects.size()]));
    return IApplication.EXIT_OK;
  }

  public void stop() {
    System.out.println("Stop: Helo World UCDetector");
  }
}
