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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.iterator.AbstractUCDetectorIterator;
import org.ucdetector.iterator.CountIterator;

/**
 * Run "count", show a dialog about counted classes, methods, fields...
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
 */
public class CountAction extends AbstractUCDetectorAction {
  CountIterator iterator;

  @Override
  protected AbstractUCDetectorIterator createIterator() {
    iterator = new CountIterator();
    return iterator;
  }

  @Override
  protected IStatus postIteration() {
    // show message for count dialog, create status
    final String message = iterator.toString();
    final IStatus status = new Status(IStatus.INFO, UCDetectorPlugin.ID, IStatus.INFO, message, null);
    UCDetectorPlugin.logToEclipseLog(status);
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        Shell shell = UCDetectorPlugin.getShell();
        Log.info(message);
        MessageDialog.openInformation(shell, Messages.CountAction_ResultTitle, message);
      }
    });
    return status;
  }
}
