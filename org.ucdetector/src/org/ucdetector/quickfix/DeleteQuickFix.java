/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 * Fixes code by deleting all affected lines
 */
class DeleteQuickFix extends AbstractUCDQuickFix {
  @Override
  public void runImpl(IMarker marker, ELEMENT element,
      BodyDeclaration nodeToChange) throws Exception {
    rewrite.remove(nodeToChange, null);
    commitChanges();
  }

  public String getLabel() {
    return Messages.DeleteQuickFix_label;
  }

  public Image getImage() {
    //    return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_REMOVE);
    return UCDetectorPlugin.getSharedImage(ISharedImages.IMG_TOOL_DELETE);
  }
}
