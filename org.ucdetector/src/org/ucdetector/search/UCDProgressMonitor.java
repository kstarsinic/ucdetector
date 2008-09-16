package org.ucdetector.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;

/**
 *
 */
public class UCDProgressMonitor implements IProgressMonitor {
  private final IProgressMonitor delegate;
  private String taskName = "?";

  public UCDProgressMonitor(IProgressMonitor delegate) {
    this.delegate = delegate;
  }

  public void beginTask(String taskName, int totalWork) {
    this.taskName = taskName;
    Log.logInfo("Start task: " + taskName);
    delegate.beginTask(taskName, totalWork);
  }

  public void done() {
    Log.logInfo("End task: " + taskName);
    delegate.done();
  }

  public void internalWorked(double work) {
    delegate.internalWorked(work);
  }

  public boolean isCanceled() {
    return delegate.isCanceled();
  }

  public void setCanceled(boolean value) {
    Log.logInfo("Task canceled: " + taskName);
    delegate.setCanceled(value);
  }

  public void setTaskName(String name) {
    delegate.setTaskName(name);
  }

  public void subTask(String name) {
    if (UCDetectorPlugin.DEBUG) {
      Log.logDebug(name);
    }
    delegate.subTask(name);
  }

  public void worked(int work) {
    delegate.worked(work);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public boolean equals(Object obj) {
    return delegate.equals(obj);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }
}