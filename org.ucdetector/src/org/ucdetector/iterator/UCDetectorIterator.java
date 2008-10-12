/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.ucdetector.Messages;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.search.SearchManager;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;
import org.ucdetector.util.StopWatch;

/**
 * Unnecessary Code Detector Iterator Collect types, methods an fields which
 * must be searched, run detection afterward */
public class UCDetectorIterator extends AbstractUCDetectorIterator {
  private final List<IType> types = new ArrayList<IType>();
  private final List<IMethod> methods = new ArrayList<IMethod>();
  private final List<IField> fields = new ArrayList<IField>();

  private final StopWatch stopWatch = new StopWatch();

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement)
      throws CoreException {
    MarkerFactory.deleteMarkers(javaElement);
  }

  @Override
  protected void handleType(IType type) throws CoreException {
    if (isPrivate(type) || type.isAnonymous()) {
      debugNotHandle("type", type, "isPrivate || isAnonymous"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    if (!Prefs.isUCDetectionInClasses()) {
      debugNotHandle("type", type, "!isUCDetectionInClasses"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    if (Prefs.filterType(type)) {
      debugNotHandle("type", type, "filterType"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    debugHandle("type", type); //$NON-NLS-1$
    types.add(type);
  }

  @Override
  protected void handleMethod(IMethod method) throws CoreException {
    // Fix Bug [ 2153699 ] Find unused abstract methods
    if (isPrivate(method) || method.isMainMethod()) {
      debugNotHandle("method", method, //$NON-NLS-1$
          "isPrivate || isMainMethod"); //$NON-NLS-1$
      return;
    }
    if (!Prefs.isUCDetectionInMethods()) {
      debugNotHandle("method", method, "!isUCDetectionInMethods"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    if (Prefs.filterMethod(method)) {
      debugNotHandle("method", method, "filterMethod"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    // ignore default constructors
    if (method.isConstructor() && method.getNumberOfParameters() == 0) {
      debugNotHandle("method", method, "default constructor"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    if (Prefs.isFilterBeanMethod() && JavaElementUtil.isBeanMethod(method)) {
      debugNotHandle("method", method, "bean method"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    debugHandle("method", method); //$NON-NLS-1$
    methods.add(method);
  }

  @Override
  protected void handleField(IField field) throws CoreException {
    if (Prefs.filterField(field)) {
      debugNotHandle("field", field, "filterField"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else if (Prefs.isCheckUseFinalField()) {
      // we need even private fields here!
      debugHandle("field", field); //$NON-NLS-1$
      fields.add(field);
    }
    else if (Prefs.isUCDetectionInFields() && !isPrivate(field)) {
      debugHandle("field", field); //$NON-NLS-1$
      fields.add(field);
    }
    else {
      debugNotHandle("field", field, //$NON-NLS-1$
          "!isCheckUseFinalField || isUCDetectionInFields"); //$NON-NLS-1$
    }
  }

  /**
   * Call searchManager for collected elements
   * @throws CoreException
   */
  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    int totalSize = getElelementsToDetectCount();
    getMonitor().beginTask(Messages.UCDetectorIterator_MONITOR_INFO, totalSize);
    getMonitor().worked(1);
    SearchManager searchManager = new SearchManager(getMonitor(), totalSize,
        getMarkerFactory());
    searchManager.search(types, methods, fields, objects);
    stopWatch.end("Time to run UCDetector"); //$NON-NLS-1$
  }

  @Override
  public int getElelementsToDetectCount() {
    return types.size() + methods.size() + fields.size();
  }

  @Override
  public String getJobName() {
    return Messages.UCDetectorIterator_JobName;
  }
}
