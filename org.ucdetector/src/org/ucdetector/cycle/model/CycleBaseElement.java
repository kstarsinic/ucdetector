/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.model;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

/**
 * Base class of all CycleTreeElements
 */
// TODO 2009-02-20: UCD tells to use default visibility. But compile error
public abstract class CycleBaseElement implements ICycleBaseElement {
  private static final int LABLEL_FLAGS = JavaElementLabelProvider.SHOW_PARAMETERS;

  private static final ILabelProvider LABEL_PROVIDER_DELEGAT //
  = new JavaElementLabelProvider(LABLEL_FLAGS); // OR

  protected final Image getImage(String id) {
    return PlatformUI.getWorkbench().getSharedImages().getImage(id);
  }

  protected final Image getDefaultImage(IJavaElement javaElement) {
    return LABEL_PROVIDER_DELEGAT.getImage(javaElement);
  }

  protected final String getDefaultText(IJavaElement javaElement) {
    return LABEL_PROVIDER_DELEGAT.getText(javaElement);
  }

  public IJavaElement getJavaElement() {
    return null;
  }

  // -------------------------------------------------------------------------
  // HELPER
  // -------------------------------------------------------------------------
  public final boolean hasChildren() {
    return !getChildren().isEmpty();
  }

  public final int getChildrenSize() {
    return getChildren() == null ? 0 : getChildren().size();
  }

  @Override
  public final String toString() {
    StringBuilder sb = new StringBuilder();
    appendToString(this, sb, 0);
    return sb.toString();
  }

  private final void appendToString(ICycleBaseElement cycleBaseElement,
      StringBuilder sb, int level) {
    int nextLevel = level + 1;
    for (int i = 0; i < level; i++) {
      sb.append("  "); //$NON-NLS-1$
    }
    sb.append("|-").append(cycleBaseElement.getText()).append('\n'); //$NON-NLS-1$
    List<? extends ICycleBaseElement> children = cycleBaseElement.getChildren();
    for (ICycleBaseElement child : children) {
      appendToString(child, sb, nextLevel);
    }
  }
}
