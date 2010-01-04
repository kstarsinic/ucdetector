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
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
import org.ucdetector.report.MarkerReport;
import org.ucdetector.util.MarkerFactory.ElementType;

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
abstract class AbstractUCDQuickFix extends WorkbenchMarkerResolution {
  IMarker marker;
  String markerType;
  ASTRewrite rewrite;
  IDocument doc;
  private ElementType elementType;

  //  private final String elementName;

  protected AbstractUCDQuickFix(IMarker marker) {
    this.marker = marker;
    markerType = getMarkerType();
    elementType = MarkerReport.getElementTypeAndName(marker).elementType;
    //    elementName = MarkerReport.getElementTypeAndName(marker).elementName;
  }

  private String getMarkerType() {
    try {
      return marker.getType();
    }
    catch (CoreException e) {
      Log.logError("Can't get marker type", e); //$NON-NLS-1$
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public void run(IMarker marker2) {
    // Hack: For run(markers[]), marker2 != marker from constructor
    this.marker = marker2;
    markerType = getMarkerType();
    elementType = MarkerReport.getElementTypeAndName(marker2).elementType;
    ICompilationUnit originalUnit = null;
    try {
      if (Log.DEBUG) {
        Log.logDebug(String.format("%s.run(). Marker=%s", //$NON-NLS-1$
            getClass().getSimpleName(), new HashMap(marker.getAttributes())));
      }
      int lineNrMarker = marker.getAttribute(IMarker.LINE_NUMBER, -1);
      originalUnit = getCompilationUnit();
      ITextFileBuffer textFileBuffer = RefactoringFileBuffers.acquire(originalUnit);
      doc = textFileBuffer.getDocument();
      CompilationUnit copyUnit = createCopy(originalUnit);
      rewrite = ASTRewrite.create(copyUnit.getAST());
      AbstractTypeDeclaration firstType = (AbstractTypeDeclaration) copyUnit.types().get(0);
      FindNodeToChangeVisitor visitor = new FindNodeToChangeVisitor(doc, lineNrMarker);
      firstType.accept(visitor);
      BodyDeclaration nodeToChange = visitor.nodeFound;
      if (Log.DEBUG) {
        //        Log.logDebug(String.format("Node to change:'%n%s'", nodeToChange)); //$NON-NLS-1$ 
      }
      if (nodeToChange == null) {
        HashMap attributes = new HashMap(marker.getAttributes());
        Log.logWarn(String.format("Node to change not found for marker: '%s'", attributes)); //$NON-NLS-1$ 
        return;
      }
      int startPosition = runImpl(nodeToChange);
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
      // needs org.eclipse.ui.editors
      if (part instanceof ITextEditor) {
        ITextEditor textEditor = (ITextEditor) part;
        //set cursor at edit position
        textEditor.selectAndReveal(startPosition, 0);
      }
    }
    catch (Exception e) {
      UCDetectorPlugin.logErrorAndStatus("Quick Fix Problems", e); //$NON-NLS-1$
    }
    finally {
      try {
        if (originalUnit != null) {
          RefactoringFileBuffers.release(originalUnit);
        }
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
        // To many logging
        // if (Log.DEBUG) {
        // Log.logDebug(String.format("%n%s. Lines: %s<=%s<=%s. Found node: %s", //$NON-NLS-1$
        //   declaration, lineStart, lineMarker, lineEnd, found));
        // if (found) {
        //   Log.logDebug("NODE FOUND: \r\n" + name.getIdentifier()); //$NON-NLS-1$
        // }
        // }
        if (found) {
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
  private ICompilationUnit getCompilationUnit() throws CoreException {
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
  protected final void commitChanges() throws BadLocationException {
    TextEdit edits = rewrite.rewriteAST(doc, null);
    edits.apply(doc);
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
  protected final ListRewrite getModifierListRewrite(BodyDeclaration nodeToChange) {
    ChildListPropertyDescriptor property = null;
    switch (elementType) {
      case TYPE:
        property = TypeDeclaration.MODIFIERS2_PROPERTY;
        break;
      case ANNOTATION:
        property = AnnotationTypeDeclaration.MODIFIERS2_PROPERTY;
        break;
      case ENUM:
        // Fix bug 2922801: Quick fix exception on enum declaration 
        property = EnumDeclaration.MODIFIERS2_PROPERTY;
        break;
      case METHOD:
        property = MethodDeclaration.MODIFIERS2_PROPERTY;
        break;
      case ANNOTATION_TYPE_MEMBER:
        // Fix bug 2906950: IllegalArgumentException modifiers is not a property of type
        property = AnnotationTypeMemberDeclaration.MODIFIERS2_PROPERTY;
        break;
      case FIELD:
        property = FieldDeclaration.MODIFIERS2_PROPERTY;
        break;
      case ENUM_CONSTANT:
        property = EnumConstantDeclaration.MODIFIERS2_PROPERTY;
        break;
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

  public abstract int runImpl(BodyDeclaration nodeToChange) throws BadLocationException;

  @Override
  public IMarker[] findOtherMarkers(IMarker[] markers) {
    final List<IMarker> result = new ArrayList<IMarker>();
    for (IMarker markerToCheck : markers) {
      try {
        if (this.marker != markerToCheck && this.markerType.equals(markerToCheck.getType())) {
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
    // sorting does not resolve quick fix problem
    //    Collections.sort(result, new Comparator<IMarker>() {
    //      public int compare(IMarker m1, IMarker m2) {
    //        int nr1 = m1.getAttribute(IMarker.LINE_NUMBER, -1);
    //        int nr2 = m2.getAttribute(IMarker.LINE_NUMBER, -1);
    //        return nr2 < nr1 ? -1 : (nr2 == nr1 ? 0 : 1);
    //      }
    //    });
    return result.toArray(new IMarker[result.size()]);
  }

  /**
   * @return <code>true</code> if FIELD and FIELD
   */
  private final boolean isElementTypeEqual(IMarker markerToCheck) {
    return getElementType(marker).equals(getElementType(markerToCheck));
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

  private static final ElementType getElementType(IMarker marker) {
    return MarkerReport.getElementTypeAndName(marker).elementType;
  }
}