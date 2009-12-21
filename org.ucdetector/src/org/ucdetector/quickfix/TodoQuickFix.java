/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 * 'Fixes' code by adding line comment at end of line: "// NO_UCD"
 */
class TodoQuickFix extends AbstractUCDQuickFix {

  protected TodoQuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public int runImpl(BodyDeclaration nodeToChange) throws BadLocationException {
    int lineNr = marker.getAttribute(IMarker.LINE_NUMBER, -1);
    if (lineNr < 1) {
      return nodeToChange.getStartPosition();//
    }
    int offset = doc.getLineInformation(lineNr - 1).getOffset();
    String todo = "// TODO from UCDetector: " + marker.getAttribute(IMarker.MESSAGE, "?") + getLineDelimitter(); //$NON-NLS-1$
    doc.replace(offset, 0, todo);
    return offset;
  }

  public Image getImage() {
    return UCDetectorPlugin.getImage(UCDetectorPlugin.IMAGE_TODO);
  }

  public String getLabel() {
    return Messages.TodoQuickFix_label;
  }

  public String getDescription() {
    return null;
  }
}
