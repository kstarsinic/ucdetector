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
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;
import org.ucdetector.util.MarkerFactory;

/**
 * Fixes code by changing visibility to <code>protected</code>
 * default or <code>private</code>
 * <p>
 * @author Joerg Spieler
 * @since 2008-09-18
 */
class VisibilityQuickFix extends AbstractUCDQuickFix {

  protected VisibilityQuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public int runImpl(BodyDeclaration nodeToChange) throws BadLocationException {
    ListRewrite listRewrite = getListRewrite(nodeToChange);
    Modifier modifierFound = getModifierVisibility(nodeToChange);
    Modifier modifierNew = getModifierNew(nodeToChange);
    int startPosition = -1;
    // default -> default: if (modifierFound == null && modifierNew == null) {// nothing }
    // default -> private
    if (modifierFound == null && modifierNew != null) {
      listRewrite.insertFirst(modifierNew, null);
      // Fix bug 2996487: modifierNew has no start position!
      // startPosition = modifierNew.getStartPosition();
    }
    // public -> default
    else if (modifierFound != null && modifierNew == null) {
      listRewrite.remove(modifierFound, null);
      startPosition = modifierFound.getStartPosition();
    }
    // public -> private
    else if (modifierFound != null && modifierNew != null) {
      listRewrite.replace(modifierFound, modifierNew, null);
      startPosition = modifierFound.getStartPosition();
    }
    //    commitChanges();
    return startPosition;
  }

  private Modifier getModifierNew(BodyDeclaration nodeToChange) {
    String markerType = getMarkerType();
    if (MarkerFactory.UCD_MARKER_TYPE_USE_PRIVATE.equals(markerType)) {
      return nodeToChange.getAST().newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
    }
    else if (MarkerFactory.UCD_MARKER_TYPE_USE_PROTECTED.equals(markerType)) {
      return nodeToChange.getAST().newModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
    }
    return null;
  }

  @Override
  public String getLabel() {
    String visibility = MarkerFactory.getVisibilityForMarkerType(getMarkerType());
    return NLS.bind(Messages.VisibilityQuickFix_label, visibility);
  }

  @Override
  public Image getImage() {
    String markerType = getMarkerType();
    if (MarkerFactory.UCD_MARKER_TYPE_USE_PROTECTED.equals(markerType)) {
      return JavaUI.getSharedImages().getImage(JavaPluginImages.IMG_MISC_PROTECTED);
    }
    else if (MarkerFactory.UCD_MARKER_TYPE_USE_DEFAULT.equals(markerType)) {
      return JavaUI.getSharedImages().getImage(JavaPluginImages.IMG_MISC_DEFAULT);
    }
    else if (MarkerFactory.UCD_MARKER_TYPE_USE_PRIVATE.equals(markerType)) {
      return JavaUI.getSharedImages().getImage(JavaPluginImages.IMG_MISC_PRIVATE);
    }
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }
}
