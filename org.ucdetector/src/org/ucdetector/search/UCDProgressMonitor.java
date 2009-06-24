/**
 * Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import java.text.DecimalFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IMember;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;

/**
 * Wrap an IProgressMonitor and do logging, when IProgressMonitor methods
 * are called
 */
public class UCDProgressMonitor implements IProgressMonitor {
  private String taskName = ""; //$NON-NLS-1$
  private final IProgressMonitor delegate;
  private String lastWork;
  private static final DecimalFormat FORMAT_DOUBLE = new DecimalFormat("0.0000"); //$NON-NLS-1$
  private IMember activeSearchElement = null;

  public UCDProgressMonitor(IProgressMonitor delegate) {
    this.delegate = delegate;
  }

  public UCDProgressMonitor() {
    this(new NullProgressMonitor());
  }

  public void beginTask(String name, int totalWork) {
    Log.logInfo("Task.beginTask " + name); //$NON-NLS-1$
    this.taskName = name;
    delegate.beginTask(taskName, totalWork);
  }

  public void done() {
    Log.logInfo("Task.done " + taskName); //$NON-NLS-1$
    delegate.done();
  }

  public void internalWorked(double work) {
    String sWork = FORMAT_DOUBLE.format(work);
    if (UCDetectorPlugin.isHeadlessMode() && !sWork.equals(lastWork)) {
      Log.logInfo("Task.internalWorked " + sWork); //$NON-NLS-1$
    }
    lastWork = sWork;
    delegate.internalWorked(work);
  }

  public boolean isCanceled() {
    return delegate.isCanceled();
  }

  public void throwIfIsCanceled() {
    if (isCanceled()) {
      throw new OperationCanceledException();
    }
  }

  public void setCanceled(boolean value) {
    Log.logInfo("Task.setCanceled: " + value); //$NON-NLS-1$
    delegate.setCanceled(value);
  }

  public void setTaskName(String name) {
    Log.logInfo("Task.setTaskName " + name); //$NON-NLS-1$
    this.taskName = name;
    delegate.setTaskName(name);
  }

  public void subTask(String name) {
    if (Log.DEBUG) {
      Log.logDebug(/*"Task.subTask: " + */name);
    }
    else if (UCDetectorPlugin.isHeadlessMode()) {
      Log.logInfo(/*"Task.subTask: " + */name);
    }
    delegate.subTask(name);
  }

  public void worked(int work) {
    // nothing usefull!
    // Log.logInfo("Task.worked: " + work);//$NON-NLS-1$
    delegate.worked(work);
  }

  public void setActiveSearchElement(IMember activeSearchElement) {
    this.activeSearchElement = activeSearchElement;
  }

  public IMember getActiveSearchElement() {
    return activeSearchElement;
  }
}