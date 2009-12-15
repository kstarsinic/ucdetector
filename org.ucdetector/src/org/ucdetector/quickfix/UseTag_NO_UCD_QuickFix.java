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
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;

/**
 * 'Fixes' code by adding line comment at end of line: "// NO_UCD"
 */
class UseTag_NO_UCD_QuickFix extends AbstractUCDQuickFix {
  private static final String COMMENT_SPACE = " "; //$NON-NLS-1$

  protected UseTag_NO_UCD_QuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public int runImpl(BodyDeclaration nodeToChange) throws BadLocationException {
    int lineNr = marker.getAttribute(IMarker.LINE_NUMBER, -1);
    if (lineNr < 1) {
      return nodeToChange.getStartPosition();//
    }
    IRegion region = doc.getLineInformation(lineNr - 1);
    int offset = region.getOffset();
    int length = region.getLength();
    String strLine = doc.get(offset, length);
    doc.replace(offset, length, strLine + COMMENT_SPACE + "// NO_UCD"); //$NON-NLS-1$
    return offset + length + COMMENT_SPACE.length();
  }

  public Image getImage() {
    // IMG_OBJS_HTMLTAG
    return JavaUI.getSharedImages()
        .getImage(JavaPluginImages.IMG_OBJS_NLS_SKIP);
  }

  public String getLabel() {
    return Messages.UseTag_NO_UCD_QuickFix_label;
  }

  public String getDescription() {
    return null;
    //    try {
    //      int lineNr = getMarker().getAttribute(IMarker.LINE_NUMBER, -1);
    //      IRegion region = doc.getLineInformation(lineNr - 1);
    //      int offset = region.getOffset();
    //      int length = region.getLength();
    //      String strLine = doc.get(offset, length);
    //      return strLine + "<b>" + COMMENT_SPACE + "// NO_UCD</b>"; //$NON-NLS-1$
    //    }
    //    catch (Exception e) {
    //      e.printStackTrace();
    //      return null;
    //    }
  }
}
