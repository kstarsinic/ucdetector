/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 * Fixes code by deleting a file (= a java class)
 * <p>
 * @author Joerg Spieler
 * @since 2010-11-19
 */
class DeleteFileQuickFix extends AbstractUCDQuickFix {

  protected DeleteFileQuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public void run(IMarker marker2) {
    IResource resource = marker2.getResource();
    try {
      resource.delete(false, null);
    }
    catch (CoreException ex) {
      UCDetectorPlugin.logToEclipseLog("Can't delete file: " + resource, ex); //$NON-NLS-1$
    }
  }

  @Override
  public String getLabel() {
    return Messages.DeleteFileQuickFix_label;
  }

  @Override
  public Image getImage() {
    return UCDetectorPlugin.getSharedImage(ISharedImages.IMG_TOOL_DELETE);
  }

  @Override
  public String getDescription() {
    return null;
  }
}
