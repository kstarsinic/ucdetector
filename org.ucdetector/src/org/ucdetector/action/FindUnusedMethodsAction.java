/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.action;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.iterator.AbstractUCDetectorIterator;
import org.ucdetector.iterator.FindUnusedMethodsIterator;

/**
 * Run "count", show a dialog about counted classes, methods, fields...
 */
public class FindUnusedMethodsAction extends AbstractUCDetectorAction {
  private FindUnusedMethodsIterator iterator;

  @Override
  protected AbstractUCDetectorIterator createIterator() {
    iterator = new FindUnusedMethodsIterator();
    return iterator;
  }

  // org.eclipse.jdt.ui/icons/full/elcl16/ch_callees.gif
  @Override
  protected IStatus postIteration() {
    // show message for count dialog, create status
    final IStatus status = new Status(IStatus.INFO, UCDetectorPlugin.ID, IStatus.INFO, iterator.toString(), null);
    UCDetectorPlugin.logStatus(status);
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        MessageDialog.openInformation(UCDetectorPlugin.getShell(), "Unused methods", iterator.toString());
      }
    });
    return status;
  }

  /**
   * this action is enabled for all JavaElement. Includes JavaElement in jars
   */
  @Override
  protected void handleJavaElementSelections(IAction action) {
    action.setEnabled(true);
  }
}
