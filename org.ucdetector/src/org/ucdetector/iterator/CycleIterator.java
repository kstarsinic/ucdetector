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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.ucdetector.Messages;
import org.ucdetector.cycle.CycleSearchManager;
import org.ucdetector.preferences.Prefs;

/**
 * search for class cycles
 */
public class CycleIterator extends AbstractUCDetectorIterator {
  private final List<IType> types = new ArrayList<IType>();

  @Override
  protected boolean handleType(IType type) throws CoreException {
    // Fix [ 2103655 ] Detect cycles does not show anything
    // Don't use "isUCDetectionInClasses()" here!
    if (isPrivate(type) || type.isLocal() || type.isAnonymous()) {
      debugNotHandle("type", type, "isPrivate || isLocal || isAnonymous"); //$NON-NLS-1$ //$NON-NLS-2$
      return false;
    }
    if (Prefs.filterType(type)) {
      debugNotHandle("type", type, "filterType"); //$NON-NLS-1$ //$NON-NLS-2$
      return false;
    }
    ICompilationUnit compilationUnit = type.getCompilationUnit();
    IType primaryType = compilationUnit.findPrimaryType();
    if (!primaryType.equals(type)) {
      debugNotHandle("type", type, "!primary type"); //$NON-NLS-1$ //$NON-NLS-2$
      return false;
    }
    debugHandle("type", type); //$NON-NLS-1$
    this.types.add(type);
    return false;
  }

  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    getMonitor().beginTask(Messages.CycleIterator_MONITOR_INFO, types.size() * 2);
    CycleSearchManager cycleSearchManager = new CycleSearchManager(getMonitor(), types, selections);
    cycleSearchManager.search();
  }

  @Override
  public int getElelementsToDetectCount() {
    return types.size();
  }

  @Override
  public String getJobName() {
    return Messages.CycleIterator_JobName;
  }
}
