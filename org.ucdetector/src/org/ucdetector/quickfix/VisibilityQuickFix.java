/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.MarkerFactory;

/**
 * // http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html
 * @see http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html
 */
class VisibilityQuickFix extends AbstractUCDQuickFix {
  private String markerType = null;

  // TODO 22.09.2008: clean up
  VisibilityQuickFix(IMarker marker) {
    try {
      this.markerType = marker.getType();
    }
    catch (CoreException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void runImpl(IMarker marker, ELEMENT element,
      BodyDeclaration nodeToChange) throws Exception {
    ListRewrite listRewrite = getListRewrite(element, nodeToChange);
    Modifier modifierFound = getModifierVisibility(nodeToChange);
    Modifier modifierNew = null;
    if (MarkerFactory.UCD_MARKER_USE_PRIVATE.equals(markerType)) {
      modifierNew = nodeToChange.getAST().newModifier(
          Modifier.ModifierKeyword.PRIVATE_KEYWORD);
    }
    else if (MarkerFactory.UCD_MARKER_USE_PROETECTED.equals(markerType)) {
      modifierNew = nodeToChange.getAST().newModifier(
          Modifier.ModifierKeyword.PROTECTED_KEYWORD);
    }
    // default -> default
    if (modifierFound == null && modifierNew == null) {
      return; // nothing
    }
    // default -> private
    else if (modifierFound == null && modifierNew != null) {
      listRewrite.insertFirst(modifierNew, null);
    }
    // public -> default
    else if (modifierFound != null && modifierNew == null) {
      listRewrite.remove(modifierFound, null);
    }
    // public -> private
    else if (modifierFound != null && modifierNew != null) {
      listRewrite.replace(modifierFound, modifierNew, null);
    }
    commit(marker);
  }

  public String getLabel() {
    String keyword = null;
    if (MarkerFactory.UCD_MARKER_USE_PROETECTED.equals(markerType)) {
      keyword = "protected"; //$NON-NLS-1$
    }
    else if (MarkerFactory.UCD_MARKER_USE_DEFAULT.equals(markerType)) {
      keyword = "default"; //$NON-NLS-1$
    }
    else if (MarkerFactory.UCD_MARKER_USE_PRIVATE.equals(markerType)) {
      keyword = "private"; //$NON-NLS-1$
    }
    return NLS.bind(Messages.VisibilityQuickFix_label, keyword);
  }

  @Override
  public Image getImage() {
    if (MarkerFactory.UCD_MARKER_USE_PROETECTED.equals(markerType)) {
      return UCDetectorPlugin
          .getJavaPluginImage(JavaPluginImages.IMG_MISC_PROTECTED);
    }
    else if (MarkerFactory.UCD_MARKER_USE_DEFAULT.equals(markerType)) {
      return UCDetectorPlugin
          .getJavaPluginImage(JavaPluginImages.IMG_MISC_DEFAULT);
    }
    else if (MarkerFactory.UCD_MARKER_USE_PRIVATE.equals(markerType)) {
      return UCDetectorPlugin
          .getJavaPluginImage(JavaPluginImages.IMG_MISC_PRIVATE);
    }
    return null;
  }
}
