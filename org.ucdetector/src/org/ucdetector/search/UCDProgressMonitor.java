package org.ucdetector.search;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 *
 */
public class UCDProgressMonitor implements IProgressMonitor {
  private final IProgressMonitor delegate;

  public UCDProgressMonitor(IProgressMonitor delegate) {
    this.delegate = delegate;
  }

  public void beginTask(String name, int totalWork) {
    delegate.beginTask(name, totalWork);
  }

  public void done() {
    delegate.done();
  }

  public void internalWorked(double work) {
    delegate.internalWorked(work);
  }

  public boolean isCanceled() {
    return delegate.isCanceled();
  }

  public void setCanceled(boolean value) {
    delegate.setCanceled(value);
  }

  public void setTaskName(String name) {
    delegate.setTaskName(name);
  }

  public void subTask(String name) {
    // TODO 16.09.2008: logging
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
