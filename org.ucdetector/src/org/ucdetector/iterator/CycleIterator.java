/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.ucdetector.Messages;
import org.ucdetector.cycle.search.CycleSearchManager;
import org.ucdetector.preferences.Prefs;

/**
 * search for class cycles
 */
public class CycleIterator extends AbstractUCDetectorIterator {
  private final List<IType> types = new ArrayList<IType>();

  @Override
  protected void handleType(IType type) throws CoreException {
    if (isPrivate(type) || !Prefs.isUCDetectionInClasses()
        || Prefs.filterType(type)) {
      return;
    }
    if (type.isLocal() || type.isAnonymous()) {
      return;
    }
    // ignore classes declared inside a class
    if (!type.getCompilationUnit().findPrimaryType().equals(type)) {
      return;
    }
    this.types.add(type);
  }

  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    getMonitor().beginTask(Messages.CycleIterator_MONITOR_INFO,
        types.size() * 2);
    CycleSearchManager cycleSearchManager = new CycleSearchManager(
        getMonitor(), types, selections);
    cycleSearchManager.search();
  }

  @Override
  public String getJobName() {
    return Messages.CycleIterator_JobName;
  }

}
