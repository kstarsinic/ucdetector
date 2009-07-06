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
 * must be searched, run detection afterward 
 * */
public class UCDetectorIterator extends AbstractUCDetectorIterator {
  private static final String FIELD = "field"; //$NON-NLS-1$
  private static final String METHOD = "method"; //$NON-NLS-1$
  private static final String TYPE = "type"; //$NON-NLS-1$
  //
  private final List<IType> types = new ArrayList<IType>();
  private final List<IMethod> methods = new ArrayList<IMethod>();
  private final List<IField> fields = new ArrayList<IField>();

  private final StopWatch stopWatch = new StopWatch();
  private int markerCreated;

  public int getMarkerCreated() {
    return markerCreated;
  }

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement)
      throws CoreException {
    MarkerFactory.deleteMarkers(javaElement);
  }

  @Override
  protected void handleType(IType type) throws CoreException {
    if (isPrivate(type)) {
      debugNotHandle(TYPE, type, "isPrivate"); //$NON-NLS-1$ 
      return;
    }
    if (type.isAnonymous()) {
      debugNotHandle(TYPE, type, "isAnonymous"); //$NON-NLS-1$ 
      return;
    }
    if (!Prefs.isUCDetectionInClasses()) {
      debugNotHandle(TYPE, type, "!isUCDetectionInClasses"); //$NON-NLS-1$ 
      return;
    }
    if (Prefs.filterType(type)) {
      debugNotHandle(TYPE, type, "filterType"); //$NON-NLS-1$ 
      return;
    }
    debugHandle(TYPE, type);
    types.add(type);
  }

  @Override
  protected void handleMethod(IMethod method) throws CoreException {
    // Fix Bug [ 2153699 ] Find unused abstract methods
    if (isPrivate(method)) {
      debugNotHandle(METHOD, method, "isPrivate");//$NON-NLS-1$ 
      return;
    }
    if (method.isMainMethod()) {
      debugNotHandle(METHOD, method, "isMainMethod"); //$NON-NLS-1$ 
      return;
    }
    if (!Prefs.isUCDetectionInMethods()) {
      debugNotHandle(METHOD, method, "!isUCDetectionInMethods"); //$NON-NLS-1$ 
      return;
    }
    if (Prefs.filterMethod(method)) {
      debugNotHandle(METHOD, method, "filterMethod"); //$NON-NLS-1$ 
      return;
    }
    // ignore default constructors
    if (method.isConstructor() && method.getNumberOfParameters() == 0) {
      debugNotHandle(METHOD, method, "default constructor"); //$NON-NLS-1$ 
      return;
    }
    if (Prefs.isFilterBeanMethod() && JavaElementUtil.isBeanMethod(method)) {
      debugNotHandle(METHOD, method, "bean method"); //$NON-NLS-1$
      return;
    }
    debugHandle(METHOD, method);
    methods.add(method);
  }

  @Override
  protected void handleField(IField field) throws CoreException {
    if (Prefs.filterField(field)) {
      debugNotHandle(FIELD, field, "filterField"); //$NON-NLS-1$ 
    }
    else if (Prefs.isCheckUseFinalField()) {
      // we need even private fields here!
      debugHandle(FIELD, field);
      fields.add(field);
    }
    else if (Prefs.isUCDetectionInFields() && !isPrivate(field)) {
      debugHandle(FIELD, field);
      fields.add(field);
    }
    else {
      debugNotHandle(FIELD, field,
          "!isCheckUseFinalField || isUCDetectionInFields"); //$NON-NLS-1$
    }
  }

  /**
   * Call searchManager for collected elements
   * @throws CoreException if the search failed 
   */
  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    int totalSize = getElelementsToDetectCount();
    getMonitor().beginTask(Messages.UCDetectorIterator_MONITOR_INFO, totalSize);
    getMonitor().worked(1);
    SearchManager searchManager = new SearchManager(getMonitor(), totalSize,
        getMarkerFactory());
    try {
      searchManager.search(types, methods, fields);
    }
    finally {
      stopWatch.end("Time to run UCDetector"); //$NON-NLS-1$
      markerCreated = searchManager.getMarkerCreated();
    }
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
