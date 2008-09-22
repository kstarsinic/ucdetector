package org.ucdetector.quickfix;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.ucdetector.Messages;

/**
 *
 */
class LineCommentQuickFix extends AbstractUCDQuickFix {

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
      for (int lineNr = lineStart; lineNr <= lineEnd; lineNr++) {
        IRegion region = doc.getLineInformation(lineNr);
        int offsetLine = region.getOffset();
        int lengthLine = region.getLength();
        String strLine = doc.get(offsetLine, lengthLine);
        doc.replace(offsetLine, lengthLine, "// " + strLine); //$NON-NLS-1$
      }
      textFileBuffer.commit(null, true);
    }
    finally {
      bufferManager.disconnect(path, LocationKind.NORMALIZE, null);
    }
  }

  public String getLabel() {
    return Messages.LineCommentQuickFix_label;
  }
}
