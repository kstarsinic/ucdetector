/**
 * Copyright (c) 2009 Joerg Spieler
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
   * @param objects to handle
   * @throws CoreException in classes overriding this method */
  public void handleStartGlobal(IJavaElement[] objects) throws CoreException {
    //
  }

  /** 
   * @param objects to handle
   * @throws CoreException in classes overriding this method */
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    //
  }

  /**  @param javaElement to handle
   * @throws CoreException in classes overriding this method */
  public void handleStartSelectedElement(IJavaElement javaElement)
      throws CoreException {
    //
  }

  /**  @param javaElement to handle
   * @throws CoreException in classes overriding this method */
  public void handleEndSelectedElement(IJavaElement javaElement)
      throws CoreException {
    //
  }

  /** 
   * @param javaElement to handle
   * @throws CoreException in classes overriding this method */
  public void handleStartElement(IJavaElement javaElement) throws CoreException {
    // Dumping all javaElements
    // System.out.println(JavaElementUtil.asString(javaElement));
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
   * @param model  
   */
  protected void handleJavaModel(IJavaModel model) {
    //
  }

  /**
   * @param project  
   */
  protected void handleJavaProject(IJavaProject project) {
    //

  }

  /**
   * @param root  
   */
  protected void handlePackageFragmentRoot(IPackageFragmentRoot root) {
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

  /** @param unit 
   * @throws CoreException in classes overriding this method */
  protected void handleCompilationUnit(ICompilationUnit unit)
      throws CoreException {
    //
  }

  /** @param type 
   * @throws CoreException in classes overriding this method */
  protected void handleType(IType type) throws CoreException {
    //
  }

  // SUP CLASS -----------------------------------------------------------------
  /** @param packageDeclaration 
   * @throws CoreException */
  protected void handlePackageDeclaration(IPackageDeclaration packageDeclaration)
      throws CoreException {
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
  protected void handleResourceWorkspaceRoot(IWorkspaceRoot workspaceRoot)
      throws CoreException {
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
  protected boolean doPackageFragmentRootChildren(IPackageFragmentRoot root) {
    return !root.isArchive() && !Prefs.filterPackageFragmentRoot(root);
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
   * children of types, which are methods, classes, fields, imports
   */
  protected boolean doTypeChildren(IType type) {
    return !Prefs.filterType(type);
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
  protected static final boolean isPrivate(IMember member)
      throws JavaModelException {
    return Flags.isPrivate(member.getFlags());
  }
}
