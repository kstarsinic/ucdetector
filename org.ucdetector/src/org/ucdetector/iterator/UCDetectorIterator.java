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
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
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
  
  private final StopWatch stopWatch = new StopWatch("UCDetectorIterator");

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement)
      throws CoreException {
    MarkerFactory.deleteMarkers(javaElement);
  }

  @Override
  protected void handleType(IType type) throws CoreException {
    if (isPrivate(type) || !Prefs.isUCDetectionInClasses()
        || Prefs.filterType(type)) {
      return;
    }
    // TODO 31.08.2008: Check local types?
    if (type.isLocal() || type.isAnonymous()/* || type.isInterface() */) {
      return;
    }
    types.add(type);
  }

  @Override
  protected void handleMethod(IMethod method) throws CoreException {
    if (isPrivate(method) || !Prefs.isUCDetectionInMethods()
        || Prefs.filterMethod(method)) {
      return;
    }
    if (method.isMainMethod()) {
      return;
    }
    // ignore default constructors
    if (method.isConstructor() && method.getNumberOfParameters() == 0) {
      return;
    }
    if (Flags.isAbstract(method.getFlags())) {
      return;
    }
    if (Prefs.isFilterBeanMethod() && JavaElementUtil.isBeanMethod(method)) {
      return;
    }
    methods.add(method);
  }

  @Override
  protected void handleField(IField field) throws CoreException {
    if (Prefs.filterField(field)) {
      return;
    }
    if (Prefs.isCheckUseFinalField()) {
      // we need even private fields here!
      fields.add(field);
    }
    else if (Prefs.isUCDetectionInFields() && !isPrivate(field)) {
      fields.add(field);
    }
  }

  /**
   * Call searchManager for collected elements
   * @throws CoreException
   */
  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    int totalSize = types.size() + methods.size() + fields.size();
    getMonitor().beginTask(Messages.UCDetectorIterator_MONITOR_INFO, totalSize);
    getMonitor().worked(1);
    int total = types.size() + methods.size() + fields.size();
    SearchManager searchManager = new SearchManager(getMonitor(), total);
    searchManager.search(types, methods, fields, objects);
    stopWatch.end("Run");
  }

  @Override
  public String getJobName() {
    return Messages.UCDetectorIterator_JobName;
  }
}
