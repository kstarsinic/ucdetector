/**
 * Copyright (c) 2010 Joerg Spieler
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
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
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
  protected boolean handleType(IType type) throws CoreException {
    if (isPrivate(type) || Prefs.isFilterType(type)) {
      debugNotHandle(type, "isPrivate or isFilterType"); //$NON-NLS-1$ 
      return true;
    }
    debugHandle(type);
    classes++;
    return true;
  }

  @Override
  protected void handleMethod(IMethod method) throws CoreException {
    if (isPrivate(method) || Prefs.isFilterMethod(method)) {
      debugNotHandle(method, "isPrivate or isFilterMethod"); //$NON-NLS-1$ 
      return;
    }
    debugHandle(method);
    methods++;
  }

  @Override
  protected void handleField(IField field) throws CoreException {
    if (isPrivate(field) || Prefs.isFilterField(field)) {
      return;
    }
    debugHandle(field);
    fields++;
  }

  /**
   * Create a "report"
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(Messages.CountIterator_NotPrivate).append(' ');
    appendLine(sb, Messages.CountIterator_Projects, projects);
    appendLine(sb, Messages.CountIterator_Packages, packages);
    appendLine(sb, Messages.CountIterator_Classes, classes);
    appendLine(sb, Messages.CountIterator_Methods, methods);
    appendLine(sb, Messages.CountIterator_Fields, fields);
    sb.append(NL).append("In: ").append(selectedAsString);//$NON-NLS-1$
    return sb.toString();
  }

  private static void appendLine(StringBuilder sb, String info, int count) {
    sb.append(String.format("%n\t%s\t=\t%s", info, Integer.valueOf(count))); //$NON-NLS-1$
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
