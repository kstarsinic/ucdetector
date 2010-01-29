/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

public abstract class CycleJavaElement extends CycleBaseElement implements IAdaptable {

  /**
   * @return the java element associated with the  CycleBaseElement, may be <code>null</code>
   */
  public abstract IJavaElement getJavaElement();

  @SuppressWarnings("unchecked")
  public final Object getAdapter(Class adapter) {
    if (adapter == IJavaElement.class) {
      return getJavaElement();
    }
    else if (adapter == IWorkbenchAdapter.class) {
      return new CycleWorkbenchAdapter(this);
    }
    else {
      return null;
    }
    //    //    System.out.println("getAdapter: " + adapter.getName());
    //    if (IContributorResourceAdapter.class == adapter) {
    //      return CycleAdapterFactory.getInstance();
    //    }
    //    return CycleAdapterFactory.getInstance().getAdapter(getJavaElement(), adapter);
  }

  private static final int LABLEL_FLAGS = JavaElementLabelProvider.SHOW_PARAMETERS;
  //
  private static final ILabelProvider LABEL_PROVIDER_DELEGAT = new JavaUILabelProvider();

  //
  protected final Image getDefaultImage(IJavaElement javaElement) {
    return LABEL_PROVIDER_DELEGAT.getImage(javaElement);
  }

  //
  protected final String getDefaultText(IJavaElement javaElement) {
    return LABEL_PROVIDER_DELEGAT.getText(javaElement);
  }
}
