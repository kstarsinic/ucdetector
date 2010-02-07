/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.model;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

public abstract class CycleJavaElement extends CycleBaseElement /*implements IAdaptable*/{
  private static final ILabelProvider LABEL_PROVIDER_DELEGAT = new JavaElementLabelProvider(
      JavaElementLabelProvider.SHOW_SMALL_ICONS | //
          JavaElementLabelProvider.SHOW_OVERLAY_ICONS | // 
          JavaElementLabelProvider.SHOW_POST_QUALIFIED | //
          JavaElementLabelProvider.SHOW_TYPE | //
          JavaElementLabelProvider.SHOW_PARAMETERS//
  );

  //  JavaElementImageProvider ip = new JavaElementImageProvider();

  /** @return the java element associated with the  CycleBaseElement, may be <code>null</code>*/
  public abstract IJavaElement getJavaElement();

  @Override
  public Image getImage() {
    return LABEL_PROVIDER_DELEGAT.getImage(getJavaElement());
  }

  public static String getDefaultText(IJavaElement javaElement) {
    return LABEL_PROVIDER_DELEGAT.getText(javaElement);
  }

  //  @SuppressWarnings("unchecked")
  //  public final Object getAdapter(Class adapter) {
  //    if (adapter == IJavaElement.class) {
  //      return getJavaElement();
  //    }
  //    else if (adapter == IWorkbenchAdapter.class) {
  //      return new CycleWorkbenchAdapter(this);
  //    }
  //    else {
  //      return null;
  //    }
  //    //    //    System.out.println("getAdapter: " + adapter.getName());
  //    //    if (IContributorResourceAdapter.class == adapter) {
  //    //      return CycleAdapterFactory.getInstance();
  //    //    }
  //    //    return CycleAdapterFactory.getInstance().getAdapter(getJavaElement(), adapter);
  //  }
}
