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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 * Fixes code by commenting all affected lines
 * <p>
 * @author Joerg Spieler
 * @since 2008-09-22
 */
class LineCommentQuickFix extends AbstractUCDQuickFix {

  protected LineCommentQuickFix(IMarker marker) {
    super(marker);
  }

  @Override
  public int runImpl(BodyDeclaration nodeToChange) throws BadLocationException {
    int offsetBody = nodeToChange.getStartPosition();
    int lengthBody = nodeToChange.getLength();
    int lineStart = doc.getLineOfOffset(offsetBody);
    int lineEnd = doc.getLineOfOffset(offsetBody + lengthBody);
    createLineComments(lineStart, lineEnd);
    return doc.getLineInformation(lineStart).getOffset();
  }

  /**
   * comment effected lines using <code>// </code>
   * See also ToggleCommentAction, TextViewer.shift(), ITextOperationTarget.PREFIX, ITextOperationTarget.STRIP_PREFIX<br>
   */
  private void createLineComments(int lineStart, int lineEnd) throws BadLocationException {
    String newLine = getLineDelimitter();
    // start at the end, because inserting new lines shifts following lines
    for (int lineNr = lineEnd; lineNr >= lineStart; lineNr--) {
      IRegion region = doc.getLineInformation(lineNr);
      int offsetLine = region.getOffset();
      int lengthLine = region.getLength();
      String strLine = doc.get(offsetLine, lengthLine);
      StringBuilder replace = new StringBuilder();
      if (lineNr == lineStart) {
        replace.append("// TODO UCdetector: Remove unused code: "); //$NON-NLS-1$;
        replace.append(newLine);
      }
      replace.append("// ").append(strLine); //$NON-NLS-1$;
      doc.replace(offsetLine, lengthLine, replace.toString());
    }
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