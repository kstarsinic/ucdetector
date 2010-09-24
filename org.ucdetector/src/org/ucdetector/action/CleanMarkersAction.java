/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.action;

import org.eclipse.core.runtime.IStatus;
import org.ucdetector.iterator.AbstractUCDetectorIterator;
import org.ucdetector.iterator.CleanMarkersIterator;

/**
 * Run "clean markers"
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
 */
public class CleanMarkersAction extends AbstractUCDetectorAction {

  @Override
  protected AbstractUCDetectorIterator createIterator() {
    return new CleanMarkersIterator();
  }

  @Override
  protected IStatus postIteration() {
    return null;
  }
}
