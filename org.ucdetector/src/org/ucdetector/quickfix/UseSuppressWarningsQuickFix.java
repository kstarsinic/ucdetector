/**
 * Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;
import org.ucdetector.util.MarkerFactory.ElementType;

/**
 * 'Fixes' code by adding @SuppressWarnings("UCD")
 */
class UseSuppressWarningsQuickFix extends AbstractUCDQuickFix {

  protected UseSuppressWarningsQuickFix(IMarker marker) {
    super(marker);
  }

  /**
   * We should do here complicate stuff, like creating and inserting nodes,
   * search for existing @SuppressWarnings. Like in
   * {@link org.eclipse.jdt.internal.ui.text.correction.SuppressWarningsSubProcessor}
   * <p>
   * This code inserts the annotation at the beginning of the line and then
   * adds a new line, which should work in 95%!
   */
  @Override
  public void runImpl(IMarker marker, ElementType elementType,
      BodyDeclaration nodeToChange) throws BadLocationException {
    int lineNr = marker.getAttribute(IMarker.LINE_NUMBER, -1);
    if (lineNr < 1) {
      return;
    }
    IRegion region = doc.getLineInformation(lineNr - 1);
    String indent = guessIndent(region);
    String declarationLine = doc.get(region.getOffset(), 0);
    String twoLines = indent + "@SuppressWarnings(\"UCD\")" //$NON-NLS-1$
        + getLineDelimitter() + declarationLine;
    doc.replace(region.getOffset(), 0, twoLines);
  }

  private String guessIndent(IRegion region) throws BadLocationException {
    String strLine = doc.get(region.getOffset(), region.getLength());
    int index = 0;
    for (index = 0; index < strLine.length(); index++) {
      if (!Character.isWhitespace(strLine.charAt(index))) {
        break;
      }
    }
    return strLine.substring(0, index);
  }

  public Image getImage() {
    return JavaUI.getSharedImages().getImage(
        JavaPluginImages.IMG_OBJS_ANNOTATION);
  }

  public String getLabel() {
    return Messages.UseAnnotation_UCD_QuickFix_label;
  }
}
