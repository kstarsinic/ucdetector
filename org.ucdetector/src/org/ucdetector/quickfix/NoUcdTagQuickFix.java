package org.ucdetector.quickfix;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.MalformedTreeException;

/**
 *
 */
class NoUcdTagQuickFix extends AbstractUCDQuickFix {
  public String getLabel() {
    return "Use comment: // NO_UCD";
  }

  @Override
  public void runImpl(IMarker marker, ELEMENT element, BodyDeclaration nodeToChange) throws Exception {
    addUCDComment(marker);
  }

  private void addUCDComment(IMarker marker) throws CoreException,
      MalformedTreeException, BadLocationException {
    ITextFileBufferManager bufferManager = FileBuffers
        .getTextFileBufferManager();
    IPath path = marker.getResource().getLocation();
    try {
      bufferManager.connect(path, LocationKind.NORMALIZE, null);
      ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path,
          LocationKind.NORMALIZE);
      IDocument doc = textFileBuffer.getDocument();
      int lineNr = marker.getAttribute(IMarker.LINE_NUMBER, -1);
      if (lineNr < 1) {
        return;
      }
      IRegion region = doc.getLineInformation(lineNr - 1);
      int offset = region.getOffset();
      int length = region.getLength();
      String strLine = doc.get(offset, length);
      doc.replace(offset, length, strLine + " // NO_UCD");
      textFileBuffer.commit(null, true);
      marker.delete();
    }
    finally {
      bufferManager.disconnect(path, LocationKind.NORMALIZE, null);
    }
  }
}
