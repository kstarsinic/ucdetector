/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.iterator.AbstractUCDetectorIterator;
import org.ucdetector.search.UCDProgressMonitor;

/**
 * Base class for actions in this plugin
 */
// Don't change visibility to default!
public abstract class AbstractUCDetectorAction extends ActionDelegate { // NO_UCD
  protected IJavaElement[] selections;

  @Override
  public void runWithEvent(IAction action, Event event) {
    if (selections == null) {
      return;
    }
    final AbstractUCDetectorIterator iterator = createIterator();
    Job job = new Job(iterator.getJobName()) {
      @Override
      public IStatus run(IProgressMonitor monitor) {
        UCDProgressMonitor ucdMonitor = new UCDProgressMonitor(monitor);
        iterator.setMonitor(ucdMonitor);
        try {
          iterator.iterate(selections);
          if (ucdMonitor.isCanceled()) {
            return Status.CANCEL_STATUS;
          }
          if (iterator.getElelementsToDetectCount() == 0) {
            showNothingToDetectMessage();
          }
          IStatus status = postIteration();
          if (status != null) {
            return status;
          }
        }
        catch (CoreException e) {
          return e.getStatus();
        }
        catch (Throwable e) {
          return UCDetectorPlugin.logErrorAndStatus(
              Messages.AbstractUCDetectorAction_AnalyzeFailedText, e);
        }
        finally {
          ucdMonitor.done();
        }
        return Status.OK_STATUS;
      }

      private void showNothingToDetectMessage() {
        Display.getDefault().asyncExec(new Runnable() {
          public void run() {
            MessageDialog.openWarning(UCDetectorPlugin.getShell(),
                Messages.AbstractUCDetectorIterator_NothingToDetectTitle,
                Messages.AbstractUCDetectorIterator_NothingToDetect);
          }
        });
      }
    };
    // http://www.eclipse.org/articles/Article-Concurrency/jobs-api.html
    //    job.setRule(ResourcesPlugin.getWorkspace().getRoot());
    job.setUser(true);
    job.schedule();
  }

  /**
   * get iterator for the action
   */
  protected abstract AbstractUCDetectorIterator createIterator();

  /**
   * override to do something after iteration, for example show a dialog
   */
  protected IStatus postIteration() {
    return null;
  }

  /**
   * Disable / enable action
   */
  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    // System.out.println("action=" + action.getText());
    if (action == null) {
      return;
    }
    PlatformUI.getWorkbench().getHelpSystem().setHelp(action,
        UCDetectorPlugin.HELP_ID);
    action.setEnabled(true);
    // Collect selections
    collectJavaElementSelections(selection);

    if (selections == null || selections.length == 0) {
      action.setEnabled(false);
      return;
    }
    handleJavaElementSelections(action);
  }

  /**
   * @return <code>true</code> when all javaElements are accessible.
   *         Accessibility is necessary to create markers.
   */
  protected final boolean allAccessible() { // 
    for (IJavaElement javaElement : selections) {
      try {
        if (javaElement instanceof IMember) {
          return !((IMember) javaElement).isBinary();
        }
        if (javaElement instanceof IPackageFragmentRoot) {
          IPackageFragmentRoot pfr = (IPackageFragmentRoot) javaElement;
          return pfr.getKind() != IPackageFragmentRoot.K_BINARY;
        }
        IResource resource = javaElement.getCorrespondingResource();
        if (resource == null || !resource.isAccessible()) {
          return false;
        }
      }
      catch (JavaModelException e) {
        Log.logError("allAccessible", e); //$NON-NLS-1$
        return false;
      }
    }
    return true;
  }

  /**
   * enable/disable action. Default Behavior: enabled for accessible java
   * elements
   */
  protected void handleJavaElementSelections(IAction action) {// 
    action.setEnabled(allAccessible());
  }

  private void collectJavaElementSelections(ISelection selection) {
    selections = null;
    if (selection instanceof IStructuredSelection) {
      List<?> structered = ((IStructuredSelection) selection).toList();
      List<IJavaElement> result = new ArrayList<IJavaElement>();
      for (int i = 0; i < structered.size(); i++) {
        Object object = structered.get(i);
        if (object instanceof IJavaElement) {
          result.add((IJavaElement) object);
        }
      }
      selections = result.toArray(new IJavaElement[result.size()]);
    }
  }
}
