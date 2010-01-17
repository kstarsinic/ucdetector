/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.ucdetector.preferences.Prefs;

/**
 * Callback methods for classes extending {@link AbstractUCDetectorIterator}
 */
public class UCDetectorCallBack { // NO_UCD
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
    return !packageFragmentRoot.isArchive() && !Prefs.filterPackageFragmentRoot(packageFragmentRoot);
  }

  /**
   * Override and return <code>true</code>, if you don't want to iterate
   * children of packages, which are classes
   */
  protected boolean doPackageChildren(IPackageFragment packageFragment) { //
    return !Prefs.filterPackage(packageFragment);
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
