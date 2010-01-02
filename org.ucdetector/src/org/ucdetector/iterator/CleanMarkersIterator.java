/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.ucdetector.Messages;
import org.ucdetector.util.MarkerFactory;

/**
 * Delete Markers of selected <code>IJavaElement</code> and its children
 */
public class CleanMarkersIterator extends AbstractUCDetectorIterator {

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement) throws CoreException {
    MarkerFactory.deleteMarkers(javaElement);
  }

  @Override
  protected boolean doSelectedElement() {
    return false;
  }

  @Override
  public String getJobName() {
    return Messages.CleanMarkersIterator_JobName;
  }
}
