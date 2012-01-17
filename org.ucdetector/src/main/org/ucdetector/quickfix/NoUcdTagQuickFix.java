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
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;
import org.ucdetector.util.MarkerFactory;

/**
 * 'Fixes' code by adding line comment at end of line: "// NO_UCD"
 * <p>
 * @author Joerg Spieler
 * @since 2008-09-22
 */
class NoUcdTagQuickFix extends AbstractUCDQuickFix {
  static final String NO_UCD_COMMENT = " // NO_UCD";//$NON-NLS-1$

  protected NoUcdTagQuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public int runImpl(BodyDeclaration nodeToChange) throws BadLocationException {
    return appendNoUcd(doc, charStart, marker);
  }

  /**
   * Add " // NO_UCD" to line end of class/method/field declaration
   * @param marker 
   */
  static int appendNoUcd(IDocument document, int charStart, IMarker marker) throws BadLocationException {
    IRegion line = document.getLineInformationOfOffset(charStart);
    int offset = line.getOffset();
    int length = line.getLength();
    StringBuilder newLine = new StringBuilder();
    newLine.append(document.get(offset, length)).append(NO_UCD_COMMENT);
    String markerType = MarkerFactory.ucdMarkerTypeToNiceString(marker);
    if (markerType != null) {
      // [ 3474895 ] Improved "// NO_UCD" quickfix
      newLine.append(" (").append(markerType).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    document.replace(offset, length, newLine.toString());
    return offset + length + 1;
  }

  public Image getImage() {
    return JavaUI.getSharedImages().getImage(JavaPluginImages.IMG_OBJS_NLS_SKIP);// IMG_OBJS_HTMLTAG
  }

  public String getLabel() {
    return Messages.UseTag_NO_UCD_QuickFix_label;
  }

  public String getDescription() {
    return null;
  }
}
