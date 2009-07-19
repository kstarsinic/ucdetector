/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.action;

import org.ucdetector.iterator.AbstractUCDetectorIterator;
import org.ucdetector.iterator.MoveIterator;

/**
 * Run Example Action
 */
public class MoveAction extends AbstractUCDetectorAction {// NO_UCD

  @Override
  protected AbstractUCDetectorIterator createIterator() {
    return new MoveIterator();
  }
}
