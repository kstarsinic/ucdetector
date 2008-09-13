/**
 * Copyright (c) 2008 Joerg Spieler
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.action;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.iterator.AbstractUCDetectorIterator;
import org.ucdetector.iterator.AdditionalIterator;

/**
 * Run Example Action
 */
public class ExampleAction extends AbstractUCDetectorAction {// NO_UCD
  private AdditionalIterator iterator;

  @Override
  protected AbstractUCDetectorIterator createIterator() {
    iterator = new org.ucdetector.iterator.CheckNameConventionIterator();
    // iterator = new org.ucdetector.iterator.DetectDoubleClassNameIterator();
    // iterator = new org.ucdetector.iterator.DetectNoJavaFileIterator();
    // iterator = new org.ucdetector.iterator.CommentIterator();
    return iterator;
  }

  @Override
  protected IStatus postIteration() {
    // show message dialog
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        MessageDialog.openInformation(UCDetectorPlugin.getShell(), iterator
            .getJobName(), iterator.getMessage());
      }
    });
    return null;
  }
}
