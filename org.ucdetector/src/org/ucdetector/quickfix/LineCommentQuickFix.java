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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 * Fixes code by commenting all affected lines
 */
class LineCommentQuickFix extends AbstractUCDQuickFix {
  private static final String LINE_COMMENT = "// "; //$NON-NLS-1$
  private static final String TODO_COMMENT //
  = "TODO UCdetector: Remove unused code: "; //$NON-NLS-1$

  protected LineCommentQuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public int runImpl(BodyDeclaration nodeToChange) throws BadLocationException {
    int offsetBody = nodeToChange.getStartPosition();
    int lengthBody = nodeToChange.getLength();
    int lineStart = doc.getLineOfOffset(offsetBody);
    int startPositionLine = doc.getLineInformation(lineStart).getOffset();
    int lineEnd = doc.getLineOfOffset(offsetBody + lengthBody);
    boolean useLineComments = containsBlockComment(lineStart, lineEnd);
    String indent;
    if (useLineComments) {
      indent = createLineComments(lineStart, lineEnd);
    }
    else {
      indent = createBlockComment(lineStart, lineEnd);
    }
    return startPositionLine + indent.length();
  }

  /**
   * comment effected lines using <code>/* * /</code>
   */
  private String createBlockComment(int lineStart, int lineEnd)
      throws BadLocationException {
    String newLine = getLineDelimitter();
    // end -----------------------------------------------------------------
    IRegion region = doc.getLineInformation(lineEnd);
    int offsetLine = region.getOffset();
    int lengthLine = region.getLength();
    String strLine = doc.get(offsetLine, lengthLine);
    StringBuilder replaceEnd = new StringBuilder();
    String indent = guessIndent(region);
    replaceEnd.append(strLine).append(newLine);
    replaceEnd.append(indent).append("*/"); //$NON-NLS-1$
    doc.replace(offsetLine, lengthLine, replaceEnd.toString());
    // start ---------------------------------------------------------------
    region = doc.getLineInformation(lineStart);
    offsetLine = region.getOffset();
    lengthLine = region.getLength();
    strLine = doc.get(offsetLine, lengthLine);
    StringBuilder replaceStart = new StringBuilder();
    replaceStart.append(indent).append("/* ").append(TODO_COMMENT); //$NON-NLS-1$
    replaceStart.append(indent).append(newLine).append(strLine);
    doc.replace(offsetLine, lengthLine, replaceStart.toString());
    return indent;
  }

  /**
   * comment effected lines using <code>//</code> 
   */
  private String createLineComments(int lineStart, int lineEnd)
      throws BadLocationException {
    String newLine = getLineDelimitter();
    // start at the end, because inserting new lines shifts following lines
    for (int lineNr = lineEnd; lineNr >= lineStart; lineNr--) {
      IRegion region = doc.getLineInformation(lineNr);
      int offsetLine = region.getOffset();
      int lengthLine = region.getLength();
      String strLine = doc.get(offsetLine, lengthLine);
      StringBuilder replace = new StringBuilder();
      if (lineNr == lineStart) {
        replace/*.append(newLine)*/.append(LINE_COMMENT);
        replace.append(TODO_COMMENT).append(newLine);
      }
      replace.append(LINE_COMMENT).append(strLine);
      doc.replace(offsetLine, lengthLine, replace.toString());
    }
    return ""; //$NON-NLS-1$
  }

  /**
   * @return <code>true</code>, if "/*" or "*\" is found in one of the lines
   */
  private boolean containsBlockComment(int lineStart, int lineEnd)
      throws BadLocationException {
    for (int lineNr = lineStart; lineNr <= lineEnd; lineNr++) {
      String line = getLine(lineNr);
      if (line.indexOf("/*") != -1 || line.indexOf("*/") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
        return true;
      }
    }
    return false;
  }

  /**
   * @return line as String at lineNr
   */
  private String getLine(int lineNr) throws BadLocationException {
    IRegion region = doc.getLineInformation(lineNr);
    return doc.get(region.getOffset(), region.getLength());
  }

  public String getLabel() {
    return Messages.LineCommentQuickFix_label;
  }

  public Image getImage() {
    return UCDetectorPlugin.getImage(UCDetectorPlugin.IMAGE_COMMENT);
    //    return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_REMOVE); // IMG_OBJS_JSEARCH
  }

  public String getDescription() {
    return null;
  }
}
