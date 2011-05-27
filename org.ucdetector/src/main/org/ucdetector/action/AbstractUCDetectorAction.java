/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.progress.IProgressConstants;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.iterator.AbstractUCDetectorIterator;
import org.ucdetector.search.UCDProgressMonitor;

/**
 * Base class for actions in this plugin
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
 */
// Don't change visibility to default!
public abstract class AbstractUCDetectorAction extends ActionDelegate { // NO_UCD
  private final List<Object> selections = new ArrayList<Object>();

  @Override
  public void runWithEvent(IAction action, Event event) {
    final AbstractUCDetectorIterator iterator = createIterator();
    final Job job = new Job(iterator.getJobName()) {
      @Override
      public IStatus run(IProgressMonitor monitor) {
        UCDProgressMonitor ucdMonitor = new UCDProgressMonitor(monitor);
        iterator.setMonitor(ucdMonitor);
        try {
          iterator.iterate(getSelections());
          if (iterator.getElelementsToDetectCount() == 0) {
            showNothingToDetectMessage();
          }
        }
        catch (CoreException e) {
          UCDetectorPlugin.logToEclipseLog(e.getStatus());
          //          return e.getStatus();
        }
        catch (Throwable e) {
          UCDetectorPlugin.logToEclipseLog(Messages.AbstractUCDetectorAction_AnalyzeFailedText, e);
        }
        IStatus status = null;
        try {
          status = postIteration();
        }
        finally {
          ucdMonitor.done();
        }
        return status != null ? status : ucdMonitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
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
    setJobProperty(job);
    job.schedule();
  }

  /** Override, to set custom job properties!   */
  protected void setJobProperty(Job job) {
    ImageDescriptor ucdIcon = UCDetectorPlugin.getImageDescriptor(UCDetectorPlugin.IMAGE_UCD);
    job.setProperty(IProgressConstants.ICON_PROPERTY, ucdIcon);
    job.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
  }

  /** Get iterator for the action   */
  protected abstract AbstractUCDetectorIterator createIterator();

  /** Override to do something after iteration, for example show a dialog */
  protected IStatus postIteration() {
    return null;
  }

  /** Disable / enable action */
  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    // System.out.println("action=" + action.getText());
    if (action != null) {
      getSelectedJavaElements(selection);
      PlatformUI.getWorkbench().getHelpSystem().setHelp(action, UCDetectorPlugin.HELP_ID);
      handleJavaElementSelections(action);
    }
  }

  private void getSelectedJavaElements(ISelection selection) {
    selections.clear();
    if (selection instanceof IStructuredSelection) {
      List<?> structered = ((IStructuredSelection) selection).toList();
      for (Object selectedObject : structered) {
        // Log.debug("selectedObject: " + selectedObject.getClass().getName()); //$NON-NLS-1$
        if (selectedObject instanceof IJavaElement) {
          selections.add(selectedObject);
        }
        else if (selectedObject instanceof IWorkingSet) {
          IAdaptable[] workingSetProjects = ((IWorkingSet) selectedObject).getElements();
          for (IAdaptable workingSetProject : workingSetProjects) {
            if (workingSetProject instanceof IProject) {
              selections.add(workingSetProject);
            }
          }
        }
      }
    }
  }

  /** Enable/disable action. Default Behavior: enabled for all java elements   */
  protected void handleJavaElementSelections(IAction action) {// 
    action.setEnabled(true/*allAccessible()*/);
  }

  /** @return never <code>null</code> */
  // Create javaProject lazy, because selectionChanged() is called frequently
  protected IJavaElement[] getSelections() {
    List<IJavaElement> result = new ArrayList<IJavaElement>();
    for (Object selection : selections) {
      if (selection instanceof IJavaElement) {
        result.add((IJavaElement) selection);
      }
      else if (selection instanceof IProject) {
        IProject project = (IProject) selection;
        IJavaProject javaProject = JavaCore.create(project);
        if (javaProject.exists()) {
          result.add(javaProject);
        }
      }
    }
    return result.toArray(new IJavaElement[result.size()]);
  }
}
//  /**
//   * @return <code>true</code> when all javaElements are accessible.
//   *         Accessibility is necessary to create markers.
//   */
//  private final boolean allAccessible() { // 
//    for (IJavaElement javaElement : selections) {
//      try {
//        if (!javaElement.exists()) {
//          return true;
//        }
//        if (javaElement instanceof IMember) {
//          return !((IMember) javaElement).isBinary();
//        }
//        if (javaElement instanceof IPackageFragmentRoot) {
//          IPackageFragmentRoot pfr = (IPackageFragmentRoot) javaElement;
//          return pfr.getKind() != IPackageFragmentRoot.K_BINARY;
//        }
//        IResource resource = javaElement.getCorrespondingResource();
//        if (resource == null || !resource.isAccessible()) {
//          return false;
//        }
//      }
//      catch (JavaModelException e) {
//        Log.error("Can't get allAccessible for javaElement: " + javaElement, e); //$NON-NLS-1$
//        return false;
//      }
//    }
//    return true;
//  }
