package org.ucdetector.quickfix;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 *
 */
public class LineCommentQuickFix extends AbstractUCDQuickFix {

  public LineCommentQuickFix(IMarker marker) throws CoreException {
    super(marker);
  }

  public String getLabel() {
    return "Comment all lines";
  }

  @Override
  public void runImpl(IMarker marker) throws Exception {
    int offsetBody = bodyDeclaration.getStartPosition();
    int lengthBody = bodyDeclaration.getLength();
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
        doc.replace(offsetLine, lengthLine, "// " + strLine);
      }
      textFileBuffer.commit(null, true);
      marker.delete();
    }
    finally {
      bufferManager.disconnect(path, LocationKind.NORMALIZE, null);
    }
  }
}
