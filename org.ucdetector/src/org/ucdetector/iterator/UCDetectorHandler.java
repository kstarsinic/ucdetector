/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import org.eclipse.core.resources.IResource;
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

class UCDetectorHandler implements IUCDetectorHandler {

  // -------------------------------------------------------------------------
  // GENERIC HANDLERS
  // -------------------------------------------------------------------------
  public void handleStartGlobal(IJavaElement[] objects) throws CoreException {
  }

  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
  }

  public void handleStartSelectedElement(IJavaElement javaElement)
      throws CoreException {
  }

  public void handleEndSelectedElement(IJavaElement javaElement)
      throws CoreException {
  }

  public void handleStartElement(IJavaElement javaElement) throws CoreException {
    // Dumping all javaElements
    // System.out.println(JavaElementUtil.asString(javaElement));
  }

  public void handleEndElement(IJavaElement javaElement) throws CoreException {
  }

  public void handleResource(IResource resource) throws CoreException {
  }

  // -------------------------------------------------------------------------
  // SPECIFIC HANDLERS
  // -------------------------------------------------------------------------

  protected void handleJavaModel(IJavaModel model) {// NO_UCD
  }

  protected void handleJavaProject(IJavaProject project) {
  }

  protected void handlePackageFragmentRoot(IPackageFragmentRoot root) {// NO_UCD
  }

  protected void handlePackageFragment(IPackageFragment packageFragment) {
  }

  /** @throws CoreException in classes overriding this method */
  // CLASS -------------------------------------------------------------------
  protected void handleClassFile(IClassFile classFile) throws CoreException {// NO_UCD
  }

  /** @throws CoreException in classes overriding this method */
  protected void handleCompilationUnit(ICompilationUnit unit)
      throws CoreException {// NO_UCD
  }

  /** @throws CoreException in classes overriding this method */
  protected void handleType(IType type) throws CoreException {
  }

  // SUP CLASS ---------------------------------------------------------------
  /** @throws CoreException */
  protected void handlePackageDeclaration(IPackageDeclaration packageDeclaration)// NO_UCD
      throws CoreException {
  }

  /** @throws CoreException */
  protected void handleImportContainer(IImportContainer importContainer)// NO_UCD
      throws CoreException {
  }

  /** @throws CoreException */
  protected void handleImportDeclaration(IImportDeclaration importDeclaration)// NO_UCD
      throws CoreException {
  }

  /** @throws CoreException */
  protected void handleField(IField field) throws CoreException {
  }

  /** @throws CoreException */
  protected void handleInitializer(IInitializer initializer)// NO_UCD
      throws CoreException {
  }

  /** @throws CoreException */
  protected void handleMethod(IMethod method) throws CoreException {
  }

  // -------------------------------------------------------------------------
  // DO
  // -------------------------------------------------------------------------
  protected boolean doPackageFragmentRootChildren(IPackageFragmentRoot root) {
    return !root.isArchive() && !Prefs.filterPackageFragmentRoot(root);
  }

  protected boolean doPackageChildren(IPackageFragment packageFragment) { // NO_UCD
    return !Prefs.filterPackage(packageFragment);
  }

  protected boolean doTypeChildren(IType type) {// NO_UCD
    return !Prefs.filterType(type);
  }

  // -------------------------------------------------------------------------
  // HELPER
  // -------------------------------------------------------------------------
  static final boolean isPrivate(IMember member) throws JavaModelException {
    return Flags.isPrivate(member.getFlags());
  }
}
