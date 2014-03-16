/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
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
 * <p>
 * @author Joerg Spieler
 * @since 2008-09-17
 */
public class UCDProgressMonitor implements IProgressMonitor {
  static final String CANCEL_MESSAGE = "Cancel requested by user"; //$NON-NLS-1$
  private String taskName = ""; //$NON-NLS-1$
  private final IProgressMonitor delegate;
  private String lastWork;
  private static final DecimalFormat FORMAT_DOUBLE = new DecimalFormat("0.0000"); //$NON-NLS-1$
  private IMember activeSearchElement = null;
  private boolean isFinished = false;
  private boolean isSleep = false;
  private final Object lock = new Object();

  public boolean isFinished() {
    return isFinished;
  }

  public UCDProgressMonitor(IProgressMonitor delegate) {
    this.delegate = delegate;
  }

  public UCDProgressMonitor() {
    this(new NullProgressMonitor());
  }

  public void beginTask(String name, int totalWork) {
    Log.info("Task.beginTask '" + name + "'"); //$NON-NLS-1$ //$NON-NLS-2$
    this.taskName = name;
    delegate.beginTask(taskName, totalWork);
  }

  public void done() {
    Log.info("Task.done '" + taskName + "'"); //$NON-NLS-1$ //$NON-NLS-2$
    delegate.done();
    isFinished = true;
  }

  public void internalWorked(double work) {
    String sWork = FORMAT_DOUBLE.format(work);
    if (UCDetectorPlugin.isHeadlessMode() && !sWork.equals(lastWork)) {
      Log.info("Task.internalWorked " + sWork); //$NON-NLS-1$
    }
    lastWork = sWork;
    delegate.internalWorked(work);
  }

  public boolean isCanceled() {
    boolean isCanceled = delegate.isCanceled();
    if (isCanceled) {
      Log.info(CANCEL_MESSAGE);
    }
    return isCanceled;
  }

  public boolean isSleep() {
    return isSleep;
  }

  public void setSleep(boolean isSleep) {
    this.isSleep = isSleep;
    if (!isSleep) {
      synchronized (lock) {
        lock.notify();
      }
    }
  }

  // Visibiliy needed for plugins extending ucdetector
  public void throwIfIsCanceled() {
    if (isCanceled()) {
      throw new OperationCanceledException();
    }
    if (isSleep) {
      try {
        synchronized (lock) {
          lock.wait();
        }
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void setCanceled(boolean value) {
    Log.warn("Task.setCanceled: " + value); //$NON-NLS-1$
    delegate.setCanceled(value);
  }

  public void setTaskName(String name) {
    Log.info("Task.setTaskName " + name); //$NON-NLS-1$
    this.taskName = name;
    delegate.setTaskName(name);
  }

  public void subTask(String name) {
    if (Log.isDebug()) {
      Log.debug(/*"Task.subTask: " + */name);
    }
    else if (UCDetectorPlugin.isHeadlessMode()) {
      Log.info(/*"Task.subTask: " + */name);
    }
    delegate.subTask(name);
  }

  public void worked(int work) {
    // not useful!
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