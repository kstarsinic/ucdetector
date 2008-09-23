/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.ucdetector.Log;
import org.ucdetector.search.UCDProgressMonitor;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;

/**
 * Base Class to iterate over projects, packages, classes, methods, fields...
 */
public abstract class AbstractUCDetectorIterator extends UCDetectorHandler {
  static final boolean DEBUG = "true".equalsIgnoreCase(Platform //$NON-NLS-1$ NO_UCD
      .getDebugOption("org.ucdetector/debug/iterator")); //$NON-NLS-1$
  static final String SEP = ", "; //$NON-NLS-1$
  private UCDProgressMonitor monitor;
  /** Elements selected in the UI */
  protected IJavaElement[] selections;

  private IPackageFragment activePackage;
  private final List<IPackageFragment> visitedPackages //
  = new ArrayList<IPackageFragment>();

  private final long start = System.currentTimeMillis();
  private MarkerFactory markerFactory = null;

  // -------------------------------------------------------------------------
  // ITERATOR
  // -------------------------------------------------------------------------
  /**
   * If there is nothing to iterate, iterate all projects
   */
  public final void iterateAll() throws CoreException { // NO_UCD
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
        .getProjects();
    List<IJavaProject> openProjects = new ArrayList<IJavaProject>();
    for (IProject tempProject : projects) {
      IJavaProject project = JavaCore.create(tempProject);
      if (project.isOpen()) {
        openProjects.add(project);
      }
    }
    iterate(openProjects.toArray(new IJavaProject[openProjects.size()]));
  }

  /**
   * Start point for all for all iterations
   */
  public final void iterate(IJavaElement[] selectionsUI) throws CoreException {
    if (DEBUG) {
      StringBuilder sb = new StringBuilder();
      sb.append(selectionsUI.length).append(" selections to iterate: ");//$NON-NLS-1$
      sb.append(getSelectedString(selectionsUI));
      Log.logInfo(sb.toString());
    }
    this.selections = selectionsUI;
    handleStartGlobal(selections);
    for (IJavaElement selection : selections) {
      if (selection instanceof IPackageFragment) {
        activePackage = (IPackageFragment) selection;
      }
      else {
        activePackage = null;
      }
      handleStartSelectedElement(selection);
      if (doSelectedElement()) {
        iterate(selection);
      }
      IResource resource = selection.getCorrespondingResource();
      if (doResources() && resource != null) {
        iterateResource(resource);
      }
      handleEndSelectedElement(selection);
    }
    handleEndGlobal(selections);
    if (markerFactory != null) {
      markerFactory.endReport(selections, start);
    }
  }

  /**
   * Concrete iteration method for resources, called recursively
   */
  private void iterateResource(IResource resource) throws CoreException {
    if (getMonitor().isCanceled()) {
      return;
    }
    if (DEBUG) {
      StringBuilder sb = new StringBuilder();
      sb.append("Iterate Resource '").append(resource.getName()); //$NON-NLS-1$
      sb.append("' ").append(Log.getClassName(resource)); //$NON-NLS-1$
      Log.logDebug(sb.toString());
    }
    if (resource instanceof IFile) {
      IFile file = (IFile) resource;
      handleResourceFile(file);
    }
    else if (resource instanceof IFolder) {
      IFolder folder = (IFolder) resource;
      handleResourceFolder(folder);
    }
    else if (resource instanceof IProject) {
      IProject project = (IProject) resource;
      handleResourceProject(project);
    }
    else if (resource instanceof IWorkspaceRoot) {
      IWorkspaceRoot workspaceRoot = (IWorkspaceRoot) resource;
      handleResourceWorkspaceRoot(workspaceRoot);
    }
    else {
      Log.logWarn("UNHANDLED RESOURCE" //$NON-NLS-1$
          + resource.getName() + ":" //$NON-NLS-1$
          + resource.getClass().getSimpleName());
    }
    if (resource instanceof IContainer) {
      IContainer container = (IContainer) resource;
      IResource[] members = container.members();
      for (IResource member : members) {
        iterateResource(member);
      }
    }
  }

  /**
   * @return number of elements to detect. If return 0, a warning message will
   * be shown "nothing to detect"
   */
  public int getElelementsToDetectCount() {
    return -1;
  }

  /**
   * Concrete iteration method, called recursively
   */
  private void iterate(IJavaElement javaElement) throws CoreException {
    if (getMonitor().isCanceled()) {
      return;
    }
    if (DEBUG) {
      StringBuilder sb = new StringBuilder();
      sb.append("Iterate JavaElement '");
      sb.append(JavaElementUtil.getElementName(javaElement)).append("' ");
      sb.append(Log.getClassName(javaElement)); 
      Log.logDebug(sb.toString());
    }
    handleStartElement(javaElement);
    boolean doChildren = true;
    if (javaElement instanceof IJavaModel) {
      IJavaModel model = (IJavaModel) javaElement;
      handleJavaModel(model);
    }
    else if (javaElement instanceof IJavaProject) {
      IJavaProject project = (IJavaProject) javaElement;
      handleJavaProject(project);
    }

    else if (javaElement instanceof IPackageFragmentRoot) {
      IPackageFragmentRoot pfRoot = (IPackageFragmentRoot) javaElement;
      doChildren = doPackageFragmentRootChildren(pfRoot);
      handlePackageFragmentRoot(pfRoot);
    }
    else if (javaElement instanceof IPackageFragment) {
      doChildren = false;
      IPackageFragment packageFragment = (IPackageFragment) javaElement;
      // fix for [ 2103678 ] Unnecessary code doesn't recurse in sub packages
      if (!visitedPackages.contains(packageFragment)) {
        visitedPackages.add(packageFragment);
        if (activePackage == packageFragment) {
          List<IPackageFragment> subPackages = JavaElementUtil
              .getSubPackages(packageFragment);
          for (IPackageFragment subPackage : subPackages) {
            iterate(subPackage);
          }
        }
        doChildren = doPackageChildren(packageFragment);
        handlePackageFragment(packageFragment);
      }
    }
    // CLASS ---------------------------------------------------------------
    else if (javaElement instanceof IClassFile) {
      IClassFile classFile = (IClassFile) javaElement;
      handleClassFile(classFile);
    }
    else if (javaElement instanceof ICompilationUnit) {
      ICompilationUnit unit = (ICompilationUnit) javaElement;
      handleCompilationUnit(unit);
    }
    else if (javaElement instanceof IType) {
      IType type = (IType) javaElement;
      doChildren = doTypeChildren(type);
      handleType(type);
    }
    // SUB CLASS -----------------------------------------------------------
    else if (javaElement instanceof IImportContainer) {
      IImportContainer importContainer = (IImportContainer) javaElement;
      doChildren = doImportContainerChildren(importContainer);
      handleImportContainer(importContainer);
    }
    else if (javaElement instanceof IPackageDeclaration) {
      IPackageDeclaration packageDeclaration = (IPackageDeclaration) javaElement;
      handlePackageDeclaration(packageDeclaration);
    }
    else if (javaElement instanceof IImportDeclaration) {
      IImportDeclaration importDeclaration = (IImportDeclaration) javaElement;
      handleImportDeclaration(importDeclaration);
    }
    else if (javaElement instanceof IInitializer) {
      IInitializer initializer = (IInitializer) javaElement;
      handleInitializer(initializer);
    }
    else if (javaElement instanceof IMethod) {
      IMethod method = (IMethod) javaElement;
      handleMethod(method);
    }
    else if (javaElement instanceof IField) {
      IField field = (IField) javaElement;
      handleField(field);
    }
    else {
      // ILocalVariable
      // ITypeParameter
      Log.logWarn("UNHANDLED TYPE" //$NON-NLS-1$
          + JavaElementUtil.getElementName(javaElement) + ":" //$NON-NLS-1$
          + javaElement.getClass().getSimpleName());
    }
    // CHILDREN
    if (doChildren) {
      if (javaElement instanceof IParent) {
        IJavaElement[] children = ((IParent) javaElement).getChildren();
        for (IJavaElement child : children) {
          iterate(child);
        }
      }
    }
    handleEndElement(javaElement);
  }

  public final void setMonitor(UCDProgressMonitor monitor) {
    this.monitor = monitor;
  }

  protected final UCDProgressMonitor getMonitor() {
    return monitor;
  }

  /**
   * @return text for the progress bar
   */
  public abstract String getJobName();

  /**
   * @return creates a String for all selected javaElents
   */
  protected final String getSelectedString(IJavaElement[] javaElements) {
    StringBuffer selectedAsString = new StringBuffer();
    for (IJavaElement javaElement : javaElements) {
      if (selectedAsString.length() > 0) {
        selectedAsString.append(SEP);
      }
      selectedAsString.append(JavaElementUtil.getElementName(javaElement));
    }
    return selectedAsString.toString();
  }

  public MarkerFactory getMarkerFactory() {
    if (markerFactory == null) {
      markerFactory = MarkerFactory.createInstance();
    }
    return markerFactory;
  }

  /**
   * Debug, that a member will be handled!
   */
  protected final void debugHandle(String what, IMember member) {
    if (DEBUG) {
      StringBuilder sb = new StringBuilder();
      sb.append("    Handle ").append(what).append(" '");
      sb.append(JavaElementUtil.getElementName(member)).append('\'');
      Log.logDebug(sb.toString());
    }
  }

  /**
   * Debug, that a member will not be handled, because it is
   * filtered, private...
   */
  protected final void debugNotHandle(String what, IMember member, String reason) {
    if (DEBUG) {
      StringBuilder sb = new StringBuilder();
      sb.append("    Ignore ").append(what).append(" '");
      sb.append(JavaElementUtil.getElementName(member));
      sb.append("' because: ").append(reason);
      Log.logDebug(sb.toString());
    }
  }
}
