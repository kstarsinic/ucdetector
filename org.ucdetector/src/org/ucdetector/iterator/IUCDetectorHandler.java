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
import org.eclipse.jdt.core.IJavaElement;

interface IUCDetectorHandler {
  void handleStartGlobal(IJavaElement[] objects) throws CoreException;

  void handleEndGlobal(IJavaElement[] objects) throws CoreException;

  void handleStartSelectedElement(IJavaElement javaElement)
      throws CoreException;

  void handleEndSelectedElement(IJavaElement javaElement) throws CoreException;

  void handleStartElement(IJavaElement javaElement) throws CoreException;

  void handleEndElement(IJavaElement javaElement) throws CoreException;

  void handleResource(IResource resource) throws CoreException;

}
