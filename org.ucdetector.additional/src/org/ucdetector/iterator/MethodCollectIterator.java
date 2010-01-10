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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;

/**
 * Detect double class names
 */
@SuppressWarnings("nls")
public class MethodCollectIterator extends AbstractUCDetectorIterator {
  private final List<IMethod> allMethods = new ArrayList<IMethod>();

  public List<IMethod> getAllmethods() {
    return allMethods;
  }

  @Override
  protected void handleMethod(IMethod method) throws CoreException {
    allMethods.add(method);
  }

  @Override
  public String getJobName() {
    return "DetectDoubleClassNameIterator";
  }
}
