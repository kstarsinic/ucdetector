/**
 * Copyright (c) 2010 Joerg Spieler
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
 * 'Fixes' code by adding a todo comment and a NO_UCD comment
 */
class TodoQuickFix extends AbstractUCDQuickFix {
  private static final String TODO_UCD = "// TODO from UCDetector: "; //$NON-NLS-1$

  protected TodoQuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public int runImpl(BodyDeclaration nodeToChange) throws BadLocationException {
    int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
    NoUcdTagQuickFix.appendNoUcd(doc, charStart);
    int offset = doc.getLineInformationOfOffset(charStart).getOffset();
    String todo = TODO_UCD + marker.getAttribute(IMarker.MESSAGE, "?") + getLineDelimitter(); //$NON-NLS-1$ 
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
    return String
        .format("Add comments:<br><b>%s...</b><br>And<br><b>%s</b>", TODO_UCD, NoUcdTagQuickFix.NO_UCD_COMMENT); //$NON-NLS-1$
  }
}
