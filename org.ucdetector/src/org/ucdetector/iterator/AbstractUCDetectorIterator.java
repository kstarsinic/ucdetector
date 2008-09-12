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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.ucdetector.Log;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;

/**
 * Base Class to iterate over projects, packages, classes, methods, fields...
 */
public abstract class AbstractUCDetectorIterator extends UCDetectorHandler {
  static final boolean DEBUG = "true".equalsIgnoreCase(Platform //$NON-NLS-1$
      .getDebugOption("org.ucdetector/debug/iterator")); //$NON-NLS-1$
  static final String SEP = ", "; //$NON-NLS-1$
  private IProgressMonitor monitor;
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
      Log.logDebug("Selection to iterate: " + getSelectedString(selectionsUI)); //$NON-NLS-1$
    }
    this.selections = selectionsUI;
    handleStartGlobal(selections);
    for (IJavaElement selection : selections) {
      activePackage = null;
      if (selection instanceof IPackageFragment) {
        activePackage = (IPackageFragment) selection;
      }
      handleStartSelectedElement(selection);
      iterate(selection);
      handleEndSelectedElement(selection);
    }
    handleEndGlobal(selections);
    if (markerFactory != null) {
      markerFactory.endReport(selections, start);
    }
  }

  /**
   * @return number of elemnts to detect. If return 0, a warning message will
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
      Log.logDebug("iterate java element '" // //$NON-NLS-1$
          + JavaElementUtil.asString(javaElement) + "' " //$NON-NLS-1$
          + Log.getClassName(javaElement));
    }
    handleStartElement(javaElement);

    // TODO 12.09.2008: Resource iteration NOT OK!
    if (javaElement.getCorrespondingResource() != null) {
      IResource resource = javaElement.getCorrespondingResource();
      if (DEBUG && resource != null) {
        Log.logDebug("iterate resource     '" // //$NON-NLS-1$
            + resource.getName() + "' " + Log.getClassName(resource)); //$NON-NLS-1$
      }
      handleResource(resource);
    }
    //
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
          + javaElement.getElementName() + ":" //$NON-NLS-1$
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

  public final void setMonitor(IProgressMonitor monitor) {
    this.monitor = monitor;
  }

  protected final IProgressMonitor getMonitor() {
    return monitor;
  }

  /**
   * @return text for the progress bar
   */
  public abstract String getJobName();

  /**
   * @return creates a String for all selected javaElents
   */
  protected final String getSelectedString(IJavaElement[] objects) {
    StringBuffer selectedAsString = new StringBuffer();
    for (Object selection : objects) {
      if (selection instanceof IJavaElement) {
        if (selectedAsString.length() > 0) {
          selectedAsString.append(SEP);
        }
        IJavaElement javaElement = (IJavaElement) selection;
        selectedAsString.append(javaElement.getElementName());
      }
    }
    return selectedAsString.toString();
  }

  protected boolean isDefaultFilter(IType type) throws JavaModelException {
    return isPrivate(type) || !Prefs.isUCDetectionInClasses()
        || Prefs.filterType(type);
  }

  protected boolean isDefaultFilter(IMethod method) throws JavaModelException {
    return isPrivate(method) || !Prefs.isUCDetectionInMethods()
        || Prefs.filterMethod(method);
  }

  public MarkerFactory getMarkerFactory() {
    if (markerFactory == null) {
      markerFactory = MarkerFactory.createInstance();
    }
    return markerFactory;
  }
}
