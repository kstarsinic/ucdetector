/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.ucdetector.Messages;
import org.ucdetector.preferences.Prefs;

/**
 * Count non private <code>IJavaElement</code>'s like:
 * <ul>
 * <li>projects</li>
 * <li>packages</li>
 * <li>classes</li>
 * <li>methods</li>
 * <li>fields</li>
 * </ul>
 */
public class CountIterator extends AbstractUCDetectorIterator {
  private int projects = 0;
  private int packages = 0;
  private int classes = 0;
  private int methods = 0;
  private int fields = 0;

  /**
   * Force detection for the first elements.
   * So it is possible to count elements in jars
   */
  private boolean isFirst = true;

  private String selectedAsString;

  @Override
  public void handleStartGlobal(IJavaElement[] objects) throws CoreException {
    getMonitor().beginTask(getJobName(), 10);
    selectedAsString = getSelectedString(objects);
    super.handleStartGlobal(objects);
  }

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement) {
    isFirst = true;
  }

  @Override
  public void handleEndElement(IJavaElement javaElement) throws CoreException {
    isFirst = false;
  }

  @Override
  protected void handleJavaProject(IJavaProject project) {
    projects++;
  }

  /**
   * If only jar is selected, the number of packages, classes will be counted
   */
  @Override
  protected boolean doPackageFragmentRootChildren(IPackageFragmentRoot root) {
    return isFirst ? true : super.doPackageFragmentRootChildren(root);
  }

  @Override
  protected void handlePackageFragment(IPackageFragment packageFragment) {
    packages++;
  }

  @Override
  protected void handleType(IType type) throws CoreException {
    if (isPrivate(type) || Prefs.filterType(type)) {
      debugNotHandle("type", type, "isPrivate || filterType"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    debugHandle("type", type); //$NON-NLS-1$
    classes++;
  }

  @Override
  protected void handleMethod(IMethod method) throws CoreException {
    if (isPrivate(method) || Prefs.filterMethod(method)) {
      debugNotHandle("method", method, "isPrivate || filterMethod"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    debugHandle("method", method); //$NON-NLS-1$
    methods++;
  }

  @Override
  protected void handleField(IField field) throws CoreException {
    if (isPrivate(field) || Prefs.filterField(field)) {
      debugNotHandle("field", field, "isPrivate || filterField"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    debugHandle("field", field); //$NON-NLS-1$
    fields++;
  }

  /**
   * Create a "report"
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(Messages.CountIterator_NotPrivate).append(' ');
    // sb.append("\r\n");
    int totalFound = getElelementsToDetectCount();
    if (totalFound == 0) {
      sb.append(Messages.CountIterator_NothingFound);
      return sb.toString();
    }
    // ---------------------------------------------------------------------
    if (projects > 0) {
      sb.append(Messages.CountIterator_Projects);
      sb.append('=').append(projects).append(SEP);
    }
    if (packages > 0) {
      sb.append(Messages.CountIterator_Packages);
      sb.append('=').append(packages).append(SEP);
    }
    if (classes > 0) {
      sb.append(Messages.CountIterator_Classes);
      sb.append('=').append(classes).append(SEP);
    }
    if (methods > 0) {
      sb.append(Messages.CountIterator_Methods);
      sb.append('=').append(methods).append(SEP);
    }
    if (fields > 0) {
      sb.append(Messages.CountIterator_Fields);
      sb.append('=').append(fields).append(SEP);
    }
    sb.setLength(sb.length() - SEP.length());
    sb.append(" (in ").append(selectedAsString).append(')');//$NON-NLS-1$
    return sb.toString();
  }

  @Override
  public int getElelementsToDetectCount() {
    return projects + packages + classes + methods + fields;
  }

  @Override
  public String getJobName() {
    return Messages.CountIterator_JobName;
  }
}
