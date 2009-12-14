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
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.MarkerFactory.ElementType;

/**
 * Fixes code by adding keyword <code>final</code>
 */
class UseFinalQuickFix extends AbstractUCDQuickFix {

  protected UseFinalQuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public final int runImpl(IMarker marker, ElementType elementType,
      BodyDeclaration nodeToChange) throws BadLocationException {
    ListRewrite listRewrite = getModifierListRewrite(elementType, nodeToChange);
    Modifier modifierFound = getModifierVisibility(nodeToChange);
    Modifier modifierFinal = nodeToChange.getAST().newModifier(
        Modifier.ModifierKeyword.FINAL_KEYWORD);
    int startPosition;
    if (modifierFound == null) {
      // default -> final
      listRewrite.insertFirst(modifierFinal, null);
      startPosition = nodeToChange.getStartPosition();
    }
    else {
      // public -> public final
      listRewrite.insertAfter(modifierFinal, modifierFound, null);
      startPosition = modifierFound.getStartPosition()
          + modifierFound.getLength() + 1;
    }
    //    commitChanges();
    return startPosition;
  }

  public String getLabel() {
    return Messages.UseFinalQuickFix_label;
  }

  public Image getImage() {
    return UCDetectorPlugin.getImage(UCDetectorPlugin.IMAGE_FINAL);
  }
}
