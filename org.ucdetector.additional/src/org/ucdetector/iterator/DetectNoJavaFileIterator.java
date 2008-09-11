/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Detect no java files
 */
public class DetectNoJavaFileIterator extends AdditionalIterator {
  @Override
  public void handleResource(IResource resource) throws CoreException {
    if (resource instanceof IContainer) {
      IContainer container = (IContainer) resource;
      IResource[] members = container.members();
      for (IResource member : members) {
        if (member instanceof IFile) {
          IFile file = (IFile) member;
          if (!file.getName().endsWith(".java")) { //$NON-NLS-1$
            System.out.println("No java: " + file.getName());//$NON-NLS-1$
            // file.delete(false, null);
          }
        }
      }
    }

  }

  @Override
  public String getJobName() {
    return "Detect mo java file job";
  }
}
