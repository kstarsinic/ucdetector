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
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.MarkerFactory.ElementType;

/**
 * Fixes code by deleting all affected lines
 */
class DeleteQuickFix extends AbstractUCDQuickFix {
  protected DeleteQuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public void runImpl(IMarker marker, ElementType elementType,
      BodyDeclaration nodeToChange) throws BadLocationException {
    // [ 2721955 ] On QuickFix the direct sibling marker gets deleted too
    // rewrite.remove(nodeToChange, null); // This line did not work
    // Hack: replace deleted note by a comment
    LineComment lineComment = nodeToChange.getAST().newLineComment();
    rewrite.replace(nodeToChange, lineComment, null);
    //    commitChanges();
  }

  public String getLabel() {
    return Messages.DeleteQuickFix_label;
  }

  public Image getImage() {
    //    return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_REMOVE);
    return UCDetectorPlugin.getSharedImage(ISharedImages.IMG_TOOL_DELETE);
  }
}
