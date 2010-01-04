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

/**
 * 'Fixes' code by adding line comment at end of line: "// NO_UCD"
 */
class NoUcdTagQuickFix extends AbstractUCDQuickFix {
  static final String NO_UCD_COMMENT = " // NO_UCD";//$NON-NLS-1$

  protected NoUcdTagQuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public int runImpl(BodyDeclaration nodeToChange) throws BadLocationException {
    int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
    return appendNoUcd(doc, charStart);
  }

  /**
   * Add " // NO_UCD" to line end of class/method/field declaration
   */
  static int appendNoUcd(IDocument document, int charStart) throws BadLocationException {
    IRegion line = document.getLineInformationOfOffset(charStart);
    int offset = line.getOffset();
    int length = line.getLength();
    String newLine = document.get(offset, length) + NO_UCD_COMMENT;
    document.replace(offset, length, newLine);
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
