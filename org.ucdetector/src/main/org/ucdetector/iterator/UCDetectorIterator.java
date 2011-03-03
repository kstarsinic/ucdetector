/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
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
 * must be searched, run detection afterward
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
 * */
public class UCDetectorIterator extends AbstractUCDetectorIterator {
  private final Set<TypeContainer> typeContainers = new LinkedHashSet<TypeContainer>();
  private TypeContainer iteratedTypeContainer = null;

  /**
   * Avoid NPE for missing type, eg. detection for only one method
  */
  private TypeContainer getIteratedTypeContainer() {
    if (iteratedTypeContainer != null) {
      return iteratedTypeContainer;
    }
    return addType(null);
  }

  private final StopWatch stopWatch = new StopWatch();
  private int markerCreated;

  public int getMarkerCreated() {
    return markerCreated;
  }

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement) throws CoreException {
    MarkerFactory.deleteMarkers(javaElement);
  }

  @Override
  protected boolean handleType(IType type) throws CoreException {
    if (isPrivate(type)) {
      debugNotHandle(type, "isPrivate"); //$NON-NLS-1$
      return false;
    }
    if (type.isAnonymous()) {
      debugNotHandle(type, "isAnonymous"); //$NON-NLS-1$
      return false;
    }
    if (!Prefs.isUCDetectionInClasses()) {
      debugNotHandle(type, "!isUCDetectionInClasses"); //$NON-NLS-1$
      return false;
    }
    if (Prefs.isFilterType(type)) {
      debugNotHandle(type, "isFilterType"); //$NON-NLS-1$
      return false;
    }
    if (Prefs.isFilterClassContainingString() && type.getCompilationUnit() != null) {
      String classAsString = type.getCompilationUnit().getSource();
      if (Prefs.isFilterClassContainingString(classAsString)) {
        debugNotHandle(type, "isFilterClassContainingString"); //$NON-NLS-1$
        return false;
      }
    }
    if (Prefs.isIgnoreDerived()) {
      if (type.getResource() != null && type.getResource().isDerived()) {
        debugNotHandle(type, "isIgnoreDerived"); //$NON-NLS-1$
        return false;
      }
    }
    // [ 2929828 ] Filter to exclude classes extending/implementing
    if (Prefs.isFilterImplements()) {
      IType[] superTypes = JavaElementUtil.getAllSupertypes(type);
      for (IType superType : superTypes) {
        String simple = superType.getElementName();
        String full = superType.getFullyQualifiedName('.');
        if (Prefs.isFilterImplements(simple) || Prefs.isFilterImplements(full)) {
          debugNotHandle(type, "isFilterImplements"); //$NON-NLS-1$
          return false;
        }
      }
    }
    debugHandle(type);
    addType(type);
    return true;
  }

  private TypeContainer addType(IType type) {
    iteratedTypeContainer = new TypeContainer(type);
    typeContainers.add(iteratedTypeContainer);
    return iteratedTypeContainer;
  }

  @Override
  protected void handleMethod(IMethod method) throws CoreException {
    // Fix Bug [ 2153699 ] Find unused abstract methods
    if (isPrivate(method)) {
      debugNotHandle(method, "isPrivate");//$NON-NLS-1$
      return;
    }
    if (method.isMainMethod()) {
      debugNotHandle(method, "isMainMethod"); //$NON-NLS-1$
      return;
    }
    if (!Prefs.isUCDetectionInMethods()) {
      debugNotHandle(method, "!isUCDetectionInMethods"); //$NON-NLS-1$
      return;
    }
    if (Prefs.isFilterMethod(method)) {
      debugNotHandle(method, "isFilterMethod"); //$NON-NLS-1$
      return;
    }
    // ignore default constructors
    if (method.isConstructor() && method.getNumberOfParameters() == 0) {
      debugNotHandle(method, "default constructor"); //$NON-NLS-1$
      return;
    }
    if (Prefs.isFilterBeanMethod() && JavaElementUtil.isBeanMethod(method)) {
      debugNotHandle(method, "isFilterBeanMethod"); //$NON-NLS-1$
      return;
    }
    debugHandle(method);
    getIteratedTypeContainer().getMethods().add(method);
  }

  @Override
  protected void handleField(IField field) throws CoreException {
    if (Prefs.isFilterField(field)) {
      debugNotHandle(field, "isFilterField"); //$NON-NLS-1$
    }
    else if (!Prefs.isUCDetectionInFields()) {
      debugNotHandle(field, "!isUCDetectionInFields"); //$NON-NLS-1$
    }
    else if (Prefs.isCheckUseFinalField()) {
      // we need even private fields here!
      debugHandle(field);
      getIteratedTypeContainer().getFields().add(field);
    }
    else if (!isPrivate(field)) {
      debugHandle(field);
      getIteratedTypeContainer().getFields().add(field);
    }
    else {
      debugNotHandle(field, "!isCheckUseFinalField || isUCDetectionInFields"); //$NON-NLS-1$
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
    SearchManager searchManager = new SearchManager(getMonitor(), totalSize, getMarkerFactory());
    try {
      UCDetectorPlugin.logMemoryInfo();
      searchManager.search(typeContainers);
      UCDetectorPlugin.logMemoryInfo();
    }
    finally {
      stopWatch.end("Time to run UCDetector"); //$NON-NLS-1$
      markerCreated = searchManager.getMarkerCreated();
    }
  }

  @Override
  public int getElelementsToDetectCount() {
    int result = 0;
    for (TypeContainer typeContainer : typeContainers) {
      result += typeContainer.size();
    }
    return result;
  }

  @Override
  public String getJobName() {
    return Messages.UCDetectorIterator_JobName;
  }
}
