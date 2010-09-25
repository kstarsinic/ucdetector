/**
 * Copyright (c) 2010 Joerg Spieler
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
import org.eclipse.jdt.core.Flags;
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
import org.eclipse.jdt.core.JavaModelException;
import org.ucdetector.Log;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.report.IUCDetectorReport;
import org.ucdetector.report.MarkerReport;
import org.ucdetector.report.XmlReport;
import org.ucdetector.search.UCDProgressMonitor;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;
import org.ucdetector.util.StopWatch;

/**
 * Base Class to iterate over projects, packages, classes, methods, fields...
 * Extend this class, to create your custom detector. Override methods handleX()
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
 */
public abstract class AbstractUCDetectorIterator {
  static final boolean DEBUG = "true".equalsIgnoreCase(Platform //$NON-NLS-1$ // NO_UCD
      .getDebugOption("org.ucdetector/debug/iterator")); //$NON-NLS-1$
  static final String SEP = ", "; //$NON-NLS-1$ // NO_UCD
  static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$
  private UCDProgressMonitor monitor;
  /** Elements selected in the UI */
  protected IJavaElement[] objectsToIterate;

  private IPackageFragment activePackage;
  private final List<IPackageFragment> visitedPackages = new ArrayList<IPackageFragment>();

  private final long timeStart = System.currentTimeMillis();
  private long timeEnd = 0;
  private MarkerFactory markerFactory = null;

  // -------------------------------------------------------------------------
  // ITERATOR
  // -------------------------------------------------------------------------
  /**
   * If there is nothing to iterate, iterate all projects
   * @throws CoreException when a problems happened during iteration
   */
  public final void iterateAll() throws CoreException { // NO_UCD
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
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
   * @param javaElements elements selected in user interface (projects, packages, classes, methods, fields)
   * @throws CoreException when a problems happened during iteration
   */
  public final void iterate(IJavaElement[] javaElements) throws CoreException {
    this.objectsToIterate = javaElements;
    if (DEBUG) {
      Log.debug("%s selections to iterate: %s", String.valueOf(javaElements.length), getSelectedString(javaElements)); //$NON-NLS-1$
    }
    try {
      handleStartGlobal(javaElements);
      for (IJavaElement selection : javaElements) {
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
      handleEndGlobal(javaElements);
    }
    finally {
      if (markerFactory != null) {
        markerFactory.endReport();
        timeEnd = System.currentTimeMillis();
        Log.info("Detection time: " + StopWatch.timeAsString(timeEnd - timeStart)); //$NON-NLS-1$
      }
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
      Log.debug("Iterate Resource '%s' %s", resource.getName(), JavaElementUtil.getClassName(resource));//$NON-NLS-1$
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
      Log.warn("UNHANDLED RESOURCE %s:%s", resource.getName(), resource.getClass().getSimpleName());//$NON-NLS-1$
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
   * Override, to show a message, when there are 0 elements to detect
   * @return number of elements to detect. If return 0, a warning message will
   * be shown "nothing to detect"
   */
  public int getElelementsToDetectCount() {
    return -1;
  }

  /**
   * Concrete iteration method, called recursively
   * @param javaElement to iterate
   * @throws CoreException if an error occurs during iterations
   */
  protected final void iterate(IJavaElement javaElement) throws CoreException {
    if (getMonitor().isCanceled()) {
      return;
    }
    if (DEBUG) {
      Log.debug("Iterate JavaElement '%s' %s", JavaElementUtil.getElementName(javaElement), //$NON-NLS-1$
          JavaElementUtil.getClassName(javaElement));
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
          List<IPackageFragment> subPackages = JavaElementUtil.getSubPackages(packageFragment);
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
      doChildren = handleType(type);
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
      Log.warn("UNHANDLED TYPE %s:%s", //$NON-NLS-1$
          JavaElementUtil.getElementName(javaElement), javaElement.getClass().getSimpleName());
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

  public final UCDProgressMonitor getMonitor() {
    return monitor;
  }

  /**
   * @return text for the progress bar
   */
  public abstract String getJobName();

  /**
   * @return creates a String for all selected javaElements
   */
  protected final String getSelectedString(IJavaElement[] javaElements) {
    StringBuilder selectedAsString = new StringBuilder();
    for (IJavaElement javaElement : javaElements) {
      if (selectedAsString.length() > 0) {
        selectedAsString.append(SEP);
      }
      selectedAsString.append(JavaElementUtil.getElementName(javaElement));
    }
    return selectedAsString.toString();
  }

  protected final MarkerFactory getMarkerFactory() {
    if (markerFactory == null) {
      List<IUCDetectorReport> reports = new ArrayList<IUCDetectorReport>();
      reports.add(new MarkerReport());
      if (Prefs.isWriteReportFile()) {
        reports.add(new XmlReport(objectsToIterate, timeStart));
      }
      markerFactory = MarkerFactory.createInstance(reports);
    }
    return markerFactory;
  }

  /**
   * Debug, that a member will be handled!
   */
  protected final void debugHandle(IMember member) {
    if (DEBUG) {
      Log.debug("    Handle %s '%s'", // //$NON-NLS-1$
          JavaElementUtil.getMemberTypeString(member), JavaElementUtil.getElementName(member));
    }
  }

  /**
   * Debug, that a member will not be handled, because it is
   * filtered, private...
   */
  protected final void debugNotHandle(IMember member, String reason) {
    if (DEBUG) {
      Log.debug("    Ignore %s '%s' because: %s",// //$NON-NLS-1$
          JavaElementUtil.getMemberTypeString(member), JavaElementUtil.getElementName(member), reason);
    }
  }

  // ---------------------------------------------------------------------------
  // GENERIC HANDLERS
  // ---------------------------------------------------------------------------
  /**
   * @param javaElements to handle
   * @throws CoreException in classes overriding this method */
  public void handleStartGlobal(IJavaElement[] javaElements) throws CoreException {
    //
  }

  /**
   * @param javaElements to handle
   * @throws CoreException in classes overriding this method */
  public void handleEndGlobal(IJavaElement[] javaElements) throws CoreException {
    //
  }

  /**  @param javaElement to handle
   * @throws CoreException in classes overriding this method */
  public void handleStartSelectedElement(IJavaElement javaElement) throws CoreException {
    //
  }

  /**  @param javaElement to handle
   * @throws CoreException in classes overriding this method */
  public void handleEndSelectedElement(IJavaElement javaElement) throws CoreException {
    //
  }

  /**
   * #########################################################
   * OVERRIDE THIS METHOD, TO DUMP ALL ITERATED IJavaElement's
   * #########################################################
   *
   * @param javaElement to handle
   * @throws CoreException in classes overriding this method */
  public void handleStartElement(IJavaElement javaElement) throws CoreException {
    // Dumping all javaElements
    // System.out.println(JavaElementUtil.getElementName(javaElement) + " - "
    //    + JavaElementUtil.getClassName(javaElement));
  }

  /**
   * @param javaElement to handle
   * @throws CoreException in classes overriding this method */
  public void handleEndElement(IJavaElement javaElement) throws CoreException {
    //
  }

  // ---------------------------------------------------------------------------
  // JAVA ELEMENT HANDLERS
  // ---------------------------------------------------------------------------

  /**
   * @param javaModel
   */
  protected void handleJavaModel(IJavaModel javaModel) {
    //
  }

  /**
   * @param javaProject
   */
  protected void handleJavaProject(IJavaProject javaProject) {
    //

  }

  /**
   * @param packageFragmentRoot
   */
  protected void handlePackageFragmentRoot(IPackageFragmentRoot packageFragmentRoot) {
    //
  }

  /**
   * @param packageFragment
   */
  protected void handlePackageFragment(IPackageFragment packageFragment) {
    //
  }

  /** @param classFile
   * @throws CoreException in classes overriding this method */
  // CLASS ---------------------------------------------------------------------
  protected void handleClassFile(IClassFile classFile) throws CoreException {
    //

  }

  /** @param compilationUnit
   * @throws CoreException in classes overriding this method */
  protected void handleCompilationUnit(ICompilationUnit compilationUnit) throws CoreException {
    //
  }

  /** @param type
   * @return <code>true</code> when children of this class should be iterated
   * @throws CoreException in classes overriding this method
   * */
  protected boolean handleType(IType type) throws CoreException {
    return true;
  }

  // SUP CLASS -----------------------------------------------------------------
  /** @param packageDeclaration
   * @throws CoreException */
  protected void handlePackageDeclaration(IPackageDeclaration packageDeclaration) throws CoreException {
    //
  }

  /** @param importContainer
   * @throws CoreException */
  protected void handleImportContainer(IImportContainer importContainer)//
      throws CoreException {
    //
  }

  /** @param importDeclaration
   * @throws CoreException */
  protected void handleImportDeclaration(IImportDeclaration importDeclaration)//
      throws CoreException {
    //
  }

  /** @param field
   * @throws CoreException */
  protected void handleField(IField field) throws CoreException {
    //
  }

  /** @param initializer
   * @throws CoreException */
  protected void handleInitializer(IInitializer initializer)//
      throws CoreException {
    //
  }

  /** @param method
   * @throws CoreException */
  protected void handleMethod(IMethod method) throws CoreException {
    //
  }

  // ---------------------------------------------------------------------------
  // RESOURCE HANDLERS
  // ---------------------------------------------------------------------------
  /** @param file
   * @throws CoreException */
  protected void handleResourceFile(IFile file) throws CoreException {
    //
  }

  /** @param folder
   * @throws CoreException */
  protected void handleResourceFolder(IFolder folder) throws CoreException {
    //
  }

  /** @param project
   * @throws CoreException */
  protected void handleResourceProject(IProject project) throws CoreException {
    //
  }

  /** @param workspaceRoot
   * @throws CoreException */
  protected void handleResourceWorkspaceRoot(IWorkspaceRoot workspaceRoot) throws CoreException {
    //
  }

  // ---------------------------------------------------------------------------
  // DO
  // ---------------------------------------------------------------------------
  /**
   * Override and return <code>false</code>, if you don't want
   * to iterate a selected element. In this case, nothing will be iterated!
   */
  protected boolean doSelectedElement() {
    return true;
  }

  /**
   * Override and return <code>true</code>, if you don't want to iterate
   *  resources like files and folders.
   */
  protected boolean doResources() {
    return false;
  }

  /**
   * Override and return <code>true</code>, if you don't want to iterate
   * PackageFragmentRoot children (children of jars...)
   * This could be a lot of stuff in case of big jars!
   */
  protected boolean doPackageFragmentRootChildren(IPackageFragmentRoot packageFragmentRoot) {
    return !packageFragmentRoot.isArchive() && !Prefs.isFilterPackageFragmentRoot(packageFragmentRoot);
  }

  /**
   * Override and return <code>true</code>, if you don't want to iterate
   * children of packages, which are classes
   */
  protected boolean doPackageChildren(IPackageFragment packageFragment) { //
    return !Prefs.isFilterPackage(packageFragment);
  }

  /**
   * Override and return <code>true</code>, if you don't want to iterate
   * children of importContainer, which are all imports.
   * @param importContainer
   */
  protected boolean doImportContainerChildren(IImportContainer importContainer) {
    return false;
  }

  // ---------------------------------------------------------------------------
  // HELPER
  // ---------------------------------------------------------------------------
  protected static final boolean isPrivate(IMember member) throws JavaModelException {
    return Flags.isPrivate(member.getFlags());
  }

  protected static final boolean isPublic(IMember member) throws JavaModelException {
    return Flags.isPublic(member.getFlags());
  }
}
