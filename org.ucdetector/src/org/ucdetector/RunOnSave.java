package org.ucdetector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.ucdetector.preferences.Prefs;

/**
 * Feature request: Eclipse Save Action - ID: 2993738
 * <p>
 * @author Joerg Spieler
 * @since 28.10.2010
 */
// http://www.eclipse.org/articles/Article-Resource-deltas/resource-deltas.html
@SuppressWarnings("nls")
final class RunOnSave implements IResourceChangeListener, IResourceDeltaVisitor {

  public void resourceChanged(IResourceChangeEvent event) {
    //    System.out.println("IResourceChangeEvent" + event);
    //we are only interested in POST_CHANGE events
    if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
      return;
    }
    IResourceDelta rootDelta = event.getDelta();
    try {
      rootDelta.accept(this);
    }
    catch (CoreException e) {
      e.printStackTrace();
    }
  }

  public boolean visit(IResourceDelta delta) {
    // System.out.println("IResourceDelta" + delta);
    //only interested in changed resources (not added or removed)
    if (delta.getKind() != IResourceDelta.CHANGED) {
      return true;
    }
    //only interested in content changes
    if ((delta.getFlags() & IResourceDelta.CONTENT) == 0) {
      return true;
    }
    IResource resource = delta.getResource();
    // System.out.println("resource" + resource);
    //only interested in files with the "txt" extension
    if (resource.getType() != IResource.FILE || !"java".equalsIgnoreCase(resource.getFileExtension())) {
      return true;
    }
    IFile file = (IFile) resource;
    IJavaElement javaElement = JavaCore.create(file);
    //    System.out.println("javaElement: " + resource);
    if (!(javaElement instanceof ICompilationUnit)) {
      return true;
    }
    ICompilationUnit unit = (ICompilationUnit) javaElement;
    IType type;
    try {
      type = unit.getTypes()[0];
    }
    catch (JavaModelException e1) {
      e1.printStackTrace();
      return true;
    }
    typeChanged(type);
    return true;
  }

  private void typeChanged(IType type) {
    System.out.println("######### typeChanged: " + type.getElementName());

  }

  void setActive(boolean runOnSave) {
    System.out.println(Prefs.RUN_ON_SAVE + ": " + runOnSave);
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    if (runOnSave) {
      workspace.addResourceChangeListener(this);
    }
    else {
      workspace.removeResourceChangeListener(this);
    }
  }
}