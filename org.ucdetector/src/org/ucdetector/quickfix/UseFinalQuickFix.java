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
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.ucdetector.Messages;

/**
 * // http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html
 * @see http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html
 */
class UseFinalQuickFix extends AbstractUCDQuickFix {

  @Override
  public final void runImpl(IMarker marker, ELEMENT element,
      BodyDeclaration nodeToChange) throws Exception {
    ListRewrite listRewrite = getListRewrite(element, nodeToChange);
    Modifier modifierFound = getModifierVisibility(nodeToChange);
    Modifier modifierFinal = nodeToChange.getAST().newModifier(
        Modifier.ModifierKeyword.FINAL_KEYWORD);
    // default -> final
    if (modifierFound == null) {
      listRewrite.insertFirst(modifierFinal, null);
    }
    // public -> public final
    else {
      listRewrite.insertAfter(modifierFinal, modifierFound, null);
    }
    commit(marker);
  }

  public String getLabel() {
    return Messages.UseFinalQuickFix_label;
  }
}
