/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * See: MethodWrapperWorkbenchAdapter
 *
 */
public class CycleWorkbenchAdapter implements IWorkbenchAdapter {
  private final CycleBaseElement delegate;

  public CycleWorkbenchAdapter(CycleBaseElement delegate) {
    this.delegate = delegate;
  }

  public CycleBaseElement getCycleBaseElement() {
    return delegate;
  }

  public Object[] getChildren(Object o) { //should not be called
    return new Object[0];
  }

  public ImageDescriptor getImageDescriptor(Object object) {
    return null;
  }

  public String getLabel(Object o) {
    return delegate.getText();
  }

  public Object getParent(Object o) {
    return delegate.getParent();
  }
}
