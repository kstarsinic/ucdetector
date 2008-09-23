package org.ucdetector.quickfix;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
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
  = "TODO UCdetector: Remove unused code"; //$NON-NLS-1$

  @Override
  public void runImpl(IMarker marker, ELEMENT element,
      BodyDeclaration nodeToChange) throws Exception {
    int offsetBody = nodeToChange.getStartPosition();
    int lengthBody = nodeToChange.getLength();
    ITextFileBufferManager bufferManager = FileBuffers
        .getTextFileBufferManager();
    IPath path = marker.getResource().getLocation();
    try {
      bufferManager.connect(path, LocationKind.NORMALIZE, null);
      ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path,
          LocationKind.NORMALIZE);
      IDocument doc = textFileBuffer.getDocument();
      int lineStart = doc.getLineOfOffset(offsetBody);
      int lineEnd = doc.getLineOfOffset(offsetBody + lengthBody);
      boolean useLineComments = containsBlockComment(doc, lineStart, lineEnd);
      if (useLineComments) {
        createLineComments(doc, lineStart, lineEnd);
      }
      else {
        createBlockComment(doc, lineStart, lineEnd);
      }
      textFileBuffer.commit(null, true);
    }
    finally {
      bufferManager.disconnect(path, LocationKind.NORMALIZE, null);
    }
  }

  /**
   * comment effected lines using <code>/* * /</code>
   */
  private void createBlockComment(IDocument doc, int lineStart, int lineEnd)
      throws BadLocationException {
    // end -----------------------------------------------------------------
    IRegion region = doc.getLineInformation(lineEnd);
    int offsetLine = region.getOffset();
    int lengthLine = region.getLength();
    String strLine = doc.get(offsetLine, lengthLine);
    String newLine = getLineDelimitter(doc, lineEnd);
    StringBuilder replaceEnd = new StringBuilder();
    replaceEnd.append(strLine).append(newLine).append("*/"); //$NON-NLS-1$
    doc.replace(offsetLine, lengthLine, replaceEnd.toString());
    // start ---------------------------------------------------------------
    newLine = getLineDelimitter(doc, lineEnd);
    region = doc.getLineInformation(lineStart);
    offsetLine = region.getOffset();
    lengthLine = region.getLength();
    strLine = doc.get(offsetLine, lengthLine);
    StringBuilder replaceStart = new StringBuilder();
    replaceStart.append(newLine).append("/* ").append(TODO_COMMENT); //$NON-NLS-1$
    replaceStart.append(newLine).append(strLine);
    doc.replace(offsetLine, lengthLine, replaceStart.toString());
  }

  /**
   * comment effected lines using <code>//</code> 
   */
  private void createLineComments(IDocument doc, int lineStart, int lineEnd)
      throws BadLocationException {
    // start at the end, because inserting new lines shifts following lines
    for (int lineNr = lineEnd; lineNr >= lineStart; lineNr--) {
      String newLine = getLineDelimitter(doc, lineNr);
      IRegion region = doc.getLineInformation(lineNr);
      int offsetLine = region.getOffset();
      int lengthLine = region.getLength();
      String strLine = doc.get(offsetLine, lengthLine);
      StringBuilder replace = new StringBuilder();
      if (lineNr == lineStart) {
        replace.append(newLine).append(LINE_COMMENT);
        replace.append(TODO_COMMENT).append(newLine);
      }
      replace.append(LINE_COMMENT).append(strLine);
      doc.replace(offsetLine, lengthLine, replace.toString());
    }
  }

  /**
   * @return lineDelimitter at lineNr or line separator from system
   */
  private String getLineDelimitter(IDocument doc, int lineNr)
      throws BadLocationException {
    String delimiter = doc.getLineDelimiter(lineNr);
    return delimiter == null ? System.getProperty("line.separator") : delimiter; //$NON-NLS-1$
  }

  /**
   * @return <code>true</code>, if "/*" is found in one of the lines
   */
  private boolean containsBlockComment(IDocument doc, int lineStart, int lineEnd)
      throws BadLocationException {
    for (int lineNr = lineStart; lineNr <= lineEnd; lineNr++) {
      String line = getLine(doc, lineNr);
      if (line.indexOf("/*") != -1) { //$NON-NLS-1$
        return true;
      }
    }
    return false;
  }

  /**
   * @return line as String at lineNr
   */
  private String getLine(IDocument doc, int lineNr) throws BadLocationException {
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
}
