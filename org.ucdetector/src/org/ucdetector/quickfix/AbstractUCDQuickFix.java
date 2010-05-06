/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringFileBuffers;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.ASTMemberVisitor;
import org.ucdetector.util.MarkerFactory;

/**
 * Base class for all UCDetector QuickFixes. This class does all the stuff to
 * change a java file.
 * <p>
 * Useful code from org.eclipse.jdt.ui:
 * <ul>
 * <li>{@link org.eclipse.jdt.internal.corext.fix.UnusedCodeFix}</li>
 * <li>{@link org.eclipse.jdt.internal.corext.refactoring.changes.AbstractDeleteChange}</li>
 * <li>{@link org.eclipse.jdt.internal.ui.text.correction.CorrectionMarkerResolutionGenerator}</li>
 * </ul>
 * 
 * @see <a href="http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html" >javadoc ASTRewrite</a>
 * @see <a href="http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html">Abstract Syntax Tree</a>
 */
@SuppressWarnings("nls")
abstract class AbstractUCDQuickFix extends WorkbenchMarkerResolution {
  IMarker marker;
  ASTRewrite rewrite;
  IDocument doc;

  protected AbstractUCDQuickFix(IMarker marker) {
    this.marker = marker;
  }

  public void run(IMarker marker2) {
    // Hack: For run(markers[]), marker2 != marker from constructor
    this.marker = marker2;
    ICompilationUnit originalUnit = null;
    try {
      if (Log.DEBUG) {
        Log.logDebug("%s.run(). Marker=%s", getClass().getSimpleName(), dumpMarker(marker));
      }
      int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
      if (charStart == -1) {
        Log.logWarn("CHAR_START missing for marker: '%s'", dumpMarker(marker));
        return;
      }
      originalUnit = getCompilationUnit();
      ITextFileBuffer textFileBuffer = RefactoringFileBuffers.acquire(originalUnit);
      doc = textFileBuffer.getDocument();
      CompilationUnit copyUnit = createCopy(originalUnit);
      rewrite = ASTRewrite.create(copyUnit.getAST());
      AbstractTypeDeclaration firstType = (AbstractTypeDeclaration) copyUnit.types().get(0);
      FindNodeToChangeVisitor visitor = new FindNodeToChangeVisitor(charStart);
      firstType.accept(visitor);
      if (visitor.nodeToChange == null) {
        Log.logWarn("Node to change not found for marker: '%s'", dumpMarker(marker));
        return;
      }
      int startPosition = runImpl(visitor.nodeToChange);
      marker.delete();
      commitChanges();
      // -----------------------------------------------------------------------
      IEditorPart part = EditorUtility.isOpenInEditor(originalUnit);
      if (part == null) { // closed
        part = EditorUtility.openInEditor(originalUnit, true);
      }
      if (part != null) {
        // textFileBuffer.commit(null, false);
        // Eclipse does not save buffer, when applying quickfixes. HACK we do:
        part.doSave(null);
      }
      originalUnit.getResource().refreshLocal(IResource.DEPTH_ONE, null);
      if (part instanceof ITextEditor) {// needs org.eclipse.ui.editors
        // Fix bug 2996487:Applying quick fix scrolls page
        if (startPosition > 0) {
          ITextEditor textEditor = (ITextEditor) part;
          textEditor.selectAndReveal(startPosition, 0);//set cursor at edit position
        }
      }
    }
    catch (Exception e) {
      UCDetectorPlugin.logErrorAndStatus("Quick Fix Problems", e);
    }
    finally {
      try {
        if (originalUnit != null) {
          RefactoringFileBuffers.release(originalUnit);
        }
      }
      catch (CoreException e) {
        UCDetectorPlugin.logErrorAndStatus("Quick Fix Problems", e);
      }
    }
  }

  /**
   * Find a ASTNode by line number
   */
  private static class FindNodeToChangeVisitor extends ASTMemberVisitor {
    BodyDeclaration nodeToChange = null;
    private final int charStart;

    protected FindNodeToChangeVisitor(int charStart) {
      this.charStart = charStart;
    }

    @Override
    protected boolean visitImpl(BodyDeclaration declaration, SimpleName name) {
      int startPos = declaration.getStartPosition();
      // end of class/method/field name
      int endPos = name.getStartPosition() + name.getLength();
      boolean found = startPos <= charStart && charStart <= endPos;
      if (found) {
        nodeToChange = declaration;
      }
      if (Log.DEBUG && found) {
        Log.logDebug("NodeToChange: %s. char postion: %s<=%s<=%s.", //
            name.getIdentifier(), "" + startPos, "" + charStart, "" + endPos);
      }
      return nodeToChange == null;
    }

    @Override
    public String toString() {
      return "nodeFound='" + nodeToChange + "'";
    }
  }

  /**
   * @return top java element (CompilationUnit) of the marker
   * @throws CoreException 
   */
  private ICompilationUnit getCompilationUnit() throws CoreException {
    IResource resource = marker.getResource();
    if (resource instanceof IFile && resource.isAccessible()) {
      IFile file = (IFile) resource;
      IJavaElement javaElement = JavaCore.create(file);
      if (javaElement instanceof ICompilationUnit) {
        return (ICompilationUnit) javaElement;
      }
    }
    Log.logWarn("Can't find CompilationUnit: " + marker.getType());
    return null;
  }

  /**
   * @return copy of CompilationUnit, which is used for manipulation
   */
  private CompilationUnit createCopy(ICompilationUnit unit) throws JavaModelException {
    unit.becomeWorkingCopy(null);
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setSource(unit);
    parser.setResolveBindings(true);
    return (CompilationUnit) parser.createAST(null);
  }

  /**
   * commit changes to document
   */
  private final void commitChanges() throws BadLocationException {
    TextEdit edits = rewrite.rewriteAST(doc, null);
    edits.apply(doc);
  }

  /**
   * @return ListRewrite to change <code>nodeToChange</code>
   */
  protected final ListRewrite getListRewrite(BodyDeclaration nodeToChange) {
    ChildListPropertyDescriptor property = nodeToChange.getModifiersProperty();
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

  public abstract int runImpl(BodyDeclaration nodeToChange) throws BadLocationException;

  @Override
  public IMarker[] findOtherMarkers(IMarker[] markers) {
    final List<IMarker> result = new ArrayList<IMarker>();
    for (IMarker markerToCheck : markers) {
      if (isOtherMarker(markerToCheck)) {
        result.add(markerToCheck);
      }
    }
    return result.toArray(new IMarker[result.size()]);
  }

  private boolean isOtherMarker(IMarker marker2) {
    // Don't add this marker 2 times!
    if (marker != marker2) {
      try {
        if (marker.getType().equals(marker2.getType())) {
          // Now we have a ucdetector marker!
          String javaType1 = (String) marker.getAttribute(MarkerFactory.JAVA_TYPE);
          String javaType2 = (String) marker2.getAttribute(MarkerFactory.JAVA_TYPE);
          return javaType1 != null && javaType1.equals(javaType2);
        }
      }
      catch (Exception e) {
        Log.logError("Can't compare markers: " + e.getMessage());
      }
    }
    return false;
  }

  /**
   * Inserting new lines confuses markers.line
   * We need to call IEditorPart.doSave() the buffer later, to avoid this problem
   * @return lineDelimitter at lineNr or line separator from system
   */
  protected final String getLineDelimitter() {
    return TextUtilities.getDefaultLineDelimiter(doc);
  }

  final String guessIndent(IRegion region) throws BadLocationException {
    String strLine = doc.get(region.getOffset(), region.getLength());
    int index = 0;
    for (index = 0; index < strLine.length(); index++) {
      if (!Character.isWhitespace(strLine.charAt(index))) {
        break;
      }
    }
    return strLine.substring(0, index);
  }

  final String getMarkerType() {
    try {
      return marker.getType();
    }
    catch (CoreException e) {
      return null;
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static String dumpMarker(IMarker m) {
    try {
      return new HashMap(m.getAttributes()).toString();
    }
    catch (CoreException e) {
      return e.getMessage();
    }
  }
}