package org.ucdetector.quickfix;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;

/**
 * 'Fixes' code by adding line comment at end of line: "// NO_UCD"
 */
class UseTag_NO_UCD_QuickFix extends AbstractUCDQuickFix {

  @Override
  public void runImpl(IMarker marker, ELEMENT element,
      BodyDeclaration nodeToChange) throws Exception {
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
      doc.replace(offset, length, strLine + " // NO_UCD"); //$NON-NLS-1$
      textFileBuffer.commit(null, true);
    }
    finally {
      bufferManager.disconnect(path, LocationKind.NORMALIZE, null);
    }
  }

  public Image getImage() {
    // IMG_OBJS_HTMLTAG
    return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_NLS_SKIP);
  }

  public String getLabel() {
    return Messages.NoUcdTagQuickFix_label;
  }
}
