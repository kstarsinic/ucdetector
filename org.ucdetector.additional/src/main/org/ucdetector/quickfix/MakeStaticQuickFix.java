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
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.graphics.Image;

/**
 * Fixes code by adding keyword <code>final</code>
 * <p>
 * @author Joerg Spieler
 * @since 2008-09-22
 */
public class MakeStaticQuickFix extends AbstractUCDQuickFix {

  public MakeStaticQuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public final int runImpl(BodyDeclaration nodeToChange) throws BadLocationException {
    ListRewrite listRewrite = getListRewrite(nodeToChange);
    Modifier modifierFound = getModifierVisibility(nodeToChange);
    Modifier modifierStatic = nodeToChange.getAST().newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
    int startPosition = -1;
    // default -> static
    if (modifierFound == null) {
      listRewrite.insertFirst(modifierStatic, null);
    }
    // public -> public static
    else {
      listRewrite.insertAfter(modifierStatic, modifierFound, null);
      startPosition = modifierFound.getStartPosition() + modifierFound.getLength() + 1;
    }
    return startPosition;
  }

  public String getLabel() {
    return "Add keyword 'static'";// Messages.UseFinalQuickFix_label;
  }

  public Image getImage() {
    return null;// UCDetectorPlugin.getImage(UCDetectorPlugin.IMAGE_FINAL);
  }

  public String getDescription() {
    return null;
  }
}
