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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.iterator.AbstractUCDetectorIterator;
import org.ucdetector.iterator.AdditionalIterator;
import org.ucdetector.iterator.CheckNameConventionIterator;
import org.ucdetector.iterator.CommentIterator;
import org.ucdetector.iterator.DetectDoubleClassNameIterator;
import org.ucdetector.iterator.DetectNoJavaFileIterator;
import org.ucdetector.iterator.MakeMethodsStaticIterator;

/**
 * Run Example Action
 */
public class ExampleAction extends AbstractUCDetectorAction {// NO_UCD
  private static final AdditionalIterator ITERATORS[] = {//
  /**/new MakeMethodsStaticIterator(), //
      new CheckNameConventionIterator(), //
      new CommentIterator(), //
      new DetectDoubleClassNameIterator(), //
      new DetectNoJavaFileIterator(), //
  };

  private AdditionalIterator iterator;

  @Override
  protected AbstractUCDetectorIterator createIterator() {
    String[] options = new String[ITERATORS.length];
    for (int i = 0; i < ITERATORS.length; i++) {
      String className = ITERATORS[i].getClass().getSimpleName();
      int index = className.indexOf("Iterator");
      options[i] = (index == -1) ? className : className.substring(0, index);
    }
    Image image = UCDetectorPlugin.getImage(UCDetectorPlugin.IMAGE_UCD);
    MessageDialog msg = new MessageDialog(UCDetectorPlugin.getShell(), "Select iterator", image,
        "Select an iterator to run", MessageDialog.QUESTION, options, 0);
    int open = msg.open();
    if (open == -1) {
      return null;
    }
    iterator = ITERATORS[open];
    try {
      iterator = iterator.getClass().newInstance();
    }
    catch (InstantiationException e) {
      e.printStackTrace();
    }
    catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Log.info("Selected iterator: " + iterator);
    return iterator;
  }

  private IStatus status = null;

  @Override
  protected IStatus postIteration() {
    final String message = iterator.getMessage();
    if (message != null) {
      // show message dialog
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          status = new Status(IStatus.INFO, UCDetectorPlugin.ID, IStatus.INFO, message, null);
          MessageDialog.openInformation(UCDetectorPlugin.getShell(), iterator.getJobName(), message);
        }
      });
    }
    return status;
  }
}
