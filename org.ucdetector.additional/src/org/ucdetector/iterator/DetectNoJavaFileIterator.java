/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Detect no java files
 */
public class DetectNoJavaFileIterator extends AdditionalIterator {
  private final StringBuilder report = new StringBuilder("No java files:\r\n");
  private int noJavaFileCount = 0;

  @Override
  protected void handleResourceFile(IFile file) throws CoreException {
    String name = file.getName();
    if (!name.endsWith(".java") && !name.endsWith("class")) { //$NON-NLS-1$
      report.append(file.getFullPath()).append("\r\n");//$NON-NLS-1$
      noJavaFileCount++;
    }
  }

  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    System.out.println(report);
  }

  @Override
  protected boolean doResources() {
    return true;
  }

  @Override
  protected boolean doSelectedElement() {
    return false;
  }

  /**
   * do a simple "report"
   */
  @Override
  public String getMessage() {
    return "Found " + noJavaFileCount + " no java files";
  }

  @Override
  public String getJobName() {
    return "Detect no java file job";
  }
}
