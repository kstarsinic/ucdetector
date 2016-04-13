/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
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
import org.ucdetector.report.MarkerReport.ElementType;
import org.ucdetector.util.ASTMemberVisitor;
import org.ucdetector.util.JavaElementUtil;
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
 * <p>
 * @author Joerg Spieler
 * @since 2008-09-22
 */
@SuppressWarnings("nls")
public abstract class AbstractUCDQuickFix extends WorkbenchMarkerResolution {
  private static final String QUICK_FIX_PROBLEMS = "Quick Fix Problems";
  int charStart = -1;
  IMarker marker;
  ASTRewrite rewrite;
  IDocument doc;

  protected AbstractUCDQuickFix(IMarker marker) {
    this.marker = marker;
    // From Bug [ 3474851 ]: Do not use field 'marker' here, because it changes in run() to 'marker2'!!!
  }

  @Override
  public void run(IMarker marker2) {
    this.marker = marker2; //  marker2 != marker in case of multi-selection
    // [ 3474851 ] Bad handling of quickfixes on multi-selection
    // Solution: Set field charStart here using marker2 not marker
    charStart = marker2.getAttribute(IMarker.CHAR_START, -1);
    ICompilationUnit originalUnit = null;
    try {
      if (Log.isDebug()) {
        Log.debug("%s.run(). Marker=%s", getClass().getSimpleName(), MarkerFactory.dumpMarker(marker));
      }
      if (charStart == -1) {
        Log.warn("CHAR_START missing for marker: '%s'", MarkerFactory.dumpMarker(marker));
        return;
      }
      originalUnit = JavaElementUtil.getCompilationUnitFor(marker.getResource());
      if (originalUnit == null) {
        Log.warn("Can't find CompilationUnit: " + marker.getType());
        return;
      }
      ITextFileBuffer textFileBuffer = RefactoringFileBuffers.acquire(originalUnit);
      doc = textFileBuffer.getDocument();
      CompilationUnit copyUnit = createCopy(originalUnit);
      rewrite = ASTRewrite.create(copyUnit.getAST());
      // Quick fix broken, when there a several types
      // AbstractTypeDeclaration firstType = (AbstractTypeDeclaration) copyUnit.types().get(0);
      FindNodeToChangeVisitor visitor = new FindNodeToChangeVisitor(charStart);
      copyUnit.accept(visitor);
      if (visitor.nodeToChange == null) {
        Log.warn("Node to change not found for marker: '%s'", MarkerFactory.dumpMarker(marker));
        return;
      }
      int startPosition = runImpl(visitor.nodeToChange);
      if (startPosition == -1) {
        // When start position is unknown, use line of marker
        startPosition = doc.getLineInformationOfOffset(charStart).getOffset();
      }
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
      UCDetectorPlugin.logToEclipseLog(QUICK_FIX_PROBLEMS, e);
    }
    finally {
      try {
        if (originalUnit != null) {
          RefactoringFileBuffers.release(originalUnit);
        }
      }
      catch (CoreException e) {
        UCDetectorPlugin.logToEclipseLog(QUICK_FIX_PROBLEMS, e);
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
      if (Log.isDebug() && found) {
        Log.debug("NodeToChange: %s. char postion: %s<=%s<=%s.", //
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
   * @return copy of CompilationUnit, which is used for manipulation
   */
  private static CompilationUnit createCopy(ICompilationUnit unit) throws JavaModelException {
    unit.becomeWorkingCopy(null);
    ASTParser parser = UCDetectorPlugin.newASTParser();
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
  /**
   * @param nodeToChange node which is changed
   * @return  startPosition
   * @throws BadLocationException when there are problems with postion
   */
  public int runImpl(BodyDeclaration nodeToChange) throws BadLocationException {
    return -1;
  }

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
          String sJavaType1 = (String) marker.getAttribute(MarkerFactory.JAVA_TYPE);
          String sJavaType2 = (String) marker2.getAttribute(MarkerFactory.JAVA_TYPE);
          ElementType javaType1 = ElementType.valueOf(sJavaType1);
          ElementType javaType2 = ElementType.valueOf(sJavaType2);
          return javaType1 == javaType2;
        }
      }
      catch (Exception e) {
        Log.error("Can't compare markers: " + e.getMessage());
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
      Log.warn("Can't getMarkerType: %s", e);
      return null;
    }
  }
}