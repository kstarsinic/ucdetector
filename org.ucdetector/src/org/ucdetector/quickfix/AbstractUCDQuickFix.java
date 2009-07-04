/**
 * Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.report.MarkerReport;
import org.ucdetector.util.MarkerFactory;
import org.ucdetector.util.MarkerFactory.ElementType;

/**
 * Base class for all UCDetector QuickFixes. This class does all the stuff to
 * change a java file.
 * 
 * @see "http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html"
 * @see "http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html"
 */
abstract class AbstractUCDQuickFix extends WorkbenchMarkerResolution {
  private final IMarker quickFixMarker;
  protected final String markerType;
  protected ASTRewrite rewrite;
  protected IDocument doc;

  protected AbstractUCDQuickFix(IMarker marker) {
    this.quickFixMarker = marker;
    markerType = getMarkerType(marker);
  }

  private String getMarkerType(IMarker marker) {
    try {
      return marker.getType();
    }
    catch (CoreException e) {
      Log.logError("Can't get marker type", e); //$NON-NLS-1$
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public void run(IMarker marker) {
    ITextFileBufferManager bufferManager = FileBuffers
        .getTextFileBufferManager();
    IPath path = marker.getResource().getLocation();
    try {
      if (Log.DEBUG) {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("run().Marker="); //$NON-NLS-1$
        sb.append(new HashMap(marker.getAttributes()));
        Log.logDebug(sb.toString());
      }
      int lineNrMarker = marker.getAttribute(IMarker.LINE_NUMBER, -1);
      bufferManager.connect(path, LocationKind.NORMALIZE, null);
      ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path,
          LocationKind.NORMALIZE);
      doc = textFileBuffer.getDocument();
      // -----------------------------------------------------------------------
      ICompilationUnit originalUnit = getCompilationUnit(marker);
      CompilationUnit copyUnit = createCopy(originalUnit);
      rewrite = ASTRewrite.create(copyUnit.getAST());
      AbstractTypeDeclaration firstType = (AbstractTypeDeclaration) copyUnit
          .types().get(0);
      FindNodeToChangeVisitor visitor = new FindNodeToChangeVisitor(doc,
          lineNrMarker);
      firstType.accept(visitor);
      BodyDeclaration nodeToChange = visitor.nodeFound;
      if (Log.DEBUG) {
        Log.logDebug("Node to change:\r\n'" + nodeToChange + "'"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      if (nodeToChange == null) {
        return;
      }
      MarkerFactory.ElementType elementType = MarkerReport
          .getElementTypeAndName(marker).elementType;
      runImpl(marker, elementType, nodeToChange);
      marker.delete();
      // -----------------------------------------------------------------------
      // see org.eclipse.jdt.internal.ui.text.correction.CorrectionMarkerResolutionGenerator
      IEditorPart part = EditorUtility.isOpenInEditor(originalUnit);
      if (part != null) { // not open
        part.doSave(null);
      }
      //      if (part instanceof ITextEditor) {
      //        ITextEditor textEditor = (ITextEditor) part;
      //        textEditor.selectAndReveal(startPosition, 0);
      //        part.setFocus();
      //      }
    }
    catch (Exception e) {
      UCDetectorPlugin.logErrorAndStatus("Quick Fix Problems", e); //$NON-NLS-1$
    }
    finally {
      try {
        bufferManager.disconnect(path, LocationKind.NORMALIZE, null);
      }
      catch (CoreException e) {
        UCDetectorPlugin.logErrorAndStatus("Quick Fix Problems", e); //$NON-NLS-1$
      }
    }
  }

  /**
   * Find a ASTNode by line number
   */
  private static class FindNodeToChangeVisitor extends ASTMemberVisitor {
    BodyDeclaration nodeFound = null;
    private final IDocument doc;
    private final int lineMarker;

    protected FindNodeToChangeVisitor(IDocument doc, int lineNrMarker) {
      this.doc = doc;
      this.lineMarker = lineNrMarker;
    }

    @Override
    protected boolean visitImpl(BodyDeclaration declaration, SimpleName name) {
      if (nodeFound != null) {
        return false;
      }
      try {
        // start of javadoc, before return type
        int startPos = declaration.getStartPosition();
        // end of class/method/field name
        int endPos = name.getStartPosition() + name.getLength();
        int lineStart = doc.getLineOfOffset(startPos) + 1;
        int lineEnd = doc.getLineOfOffset(endPos) + 1;
        boolean found = lineStart <= lineMarker && lineMarker <= lineEnd;
        if (Log.DEBUG) {
          StringBuilder sb = new StringBuilder();
          sb.append("\r\n").append(declaration); //$NON-NLS-1$
          sb.append("Lines: ").append(lineStart).append("<="); //$NON-NLS-1$ //$NON-NLS-2$
          sb.append(lineMarker).append("<=").append(lineEnd); //$NON-NLS-1$
          sb.append(", Found node=").append(found); //$NON-NLS-1$
          Log.logDebug(sb.toString());
        }
        if (found) {
          Log.logDebug("NODE FOUND: \r\n" + name.getIdentifier()); //$NON-NLS-1$
          nodeFound = declaration;
        }
      }
      catch (BadLocationException e) {
        Log.logError("Can't get line", e); //$NON-NLS-1$
        return false;
      }
      return nodeFound == null;
    }

    @Override
    public String toString() {
      return "nodeFound='" + nodeFound + "'"; //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * @return top java element (CompilationUnit) of the marker
   * @throws CoreException 
   */
  private ICompilationUnit getCompilationUnit(IMarker marker)
      throws CoreException {
    IResource resource = marker.getResource();
    if (resource instanceof IFile && resource.isAccessible()) {
      IFile file = (IFile) resource;
      IJavaElement javaElement = JavaCore.create(file);
      if (javaElement instanceof ICompilationUnit) {
        return (ICompilationUnit) javaElement;
      }
    }
    Log.logWarn("Can't find CompilationUnit: " //$NON-NLS-1$
        + marker.getType());
    return null;
  }

  /**
   * @return copy of CompilationUnit, which is used for manipulation
   */
  private CompilationUnit createCopy(ICompilationUnit unit)
      throws JavaModelException {
    unit.becomeWorkingCopy(null);
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setSource(unit);
    parser.setResolveBindings(true);
    return (CompilationUnit) parser.createAST(null);
  }

  /**
   * commit changes to document
   */
  protected final void commitChanges() throws BadLocationException {
    TextEdit edits = rewrite.rewriteAST(doc, null);
    edits.apply(doc);
    // We don't save the buffer at the moment
    // textFileBuffer.commit(null, false);
  }

  /**
   * @return ListRewrite to change modifiers like
   * <ul>
   * <li><code>final</code></li>
   * <li><code>protected</code></li>
   * <li><code>private</code></li>
   * <li>default</li>
   * </ul>
   */
  protected final ListRewrite getModifierListRewrite(
      MarkerFactory.ElementType elementType, BodyDeclaration nodeToChange) {
    ChildListPropertyDescriptor property;
    switch (elementType) {
      case TYPE:
        property = TypeDeclaration.MODIFIERS2_PROPERTY;
        break;
      case METHOD:
        property = MethodDeclaration.MODIFIERS2_PROPERTY;
        break;
      case FIELD:
      case CONSTANT:
        property = FieldDeclaration.MODIFIERS2_PROPERTY;
        break;
      default:
        property = null;
    }
    return rewrite.getListRewrite(nodeToChange, property);
  }

  /**
   * @return modifier like: public, protected, private
   */
  protected static Modifier getModifierVisibility(BodyDeclaration declaration) {
    List<?> list = declaration.modifiers();
    for (Object child : list) {
      if (child instanceof Modifier) {
        Modifier modifier = (Modifier) child;
        if (modifier.getKeyword().equals(ModifierKeyword.PUBLIC_KEYWORD)
            || modifier.getKeyword().equals(ModifierKeyword.PROTECTED_KEYWORD)
            || modifier.getKeyword().equals(ModifierKeyword.PRIVATE_KEYWORD)) {
          return modifier;
        }
      }
    }
    return null;
  }

  // ---------------------------------------------------------------------------
  // Override, implement
  // ---------------------------------------------------------------------------

  public abstract void runImpl(IMarker marker, ElementType elementType,
      BodyDeclaration nodeToChange) throws BadLocationException;

  public String getDescription() {
    return null;
  }

  @Override
  public IMarker[] findOtherMarkers(IMarker[] markers) {
    final List<IMarker> result = new ArrayList<IMarker>();
    for (IMarker markerToCheck : markers) {
      try {
        if (this.quickFixMarker != markerToCheck
            && this.markerType.equals(markerToCheck.getType())) {
          if (isElementTypeEqual(markerToCheck)) {
            result.add(markerToCheck);
          }
        }
      }
      catch (CoreException e) {
        Log.logError("Can't find other marker", e); //$NON-NLS-1$
        continue;
      }
    }
    return result.toArray(new IMarker[result.size()]);
  }

  /**
   * @return <code>true</code> if FIELD and FIELD
   */
  private final boolean isElementTypeEqual(IMarker markerToCheck) {
    return getElementType(quickFixMarker).equals(getElementType(markerToCheck));
  }

  /**
   * Inserting new lines confuses markers.line
   * We need to call IEditorPart.doSave() the buffer later, to avoid this problem
   * @return lineDelimitter at lineNr or line separator from system
   */
  protected String getLineDelimitter() {
    return TextUtilities.getDefaultLineDelimiter(doc);
  }

  private static final ElementType getElementType(IMarker marker) {
    return MarkerReport.getElementTypeAndName(marker).elementType;
  }
}