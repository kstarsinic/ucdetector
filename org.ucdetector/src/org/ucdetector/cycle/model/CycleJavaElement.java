/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.model;

import org.eclipse.jdt.core.IJavaElement;

public abstract class CycleJavaElement extends CycleBaseElement {

  /**
   * @return the java element associated with the  CycleBaseElement, may be <code>null</code>
   */
  public abstract IJavaElement getJavaElement();
}
