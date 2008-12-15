package org.ucdetector.quickfix;

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
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution2;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.MarkerFactory;

/**
 * Base class for all UCDetector QuickFixes. This class does all the stuff to
 * change a java file.
 * 
 * http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html
 * @see http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html
 */
abstract class AbstractUCDQuickFix 
    // TODO 2008.12.15. [ 2417657 ] QuickFix should behave as for Java problems
    // extends WorkbenchMarkerResolution
    implements IMarkerResolution2
 { 
  static enum ELEMENT {
    TYPE, METHOD, FIELD;
  }

  protected ASTRewrite rewrite;
  protected IDocument doc;

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
      String[] elementInfos = marker.getAttribute(
          MarkerFactory.JAVA_ELEMENT_ATTRIBUTE, "?").split(","); //$NON-NLS-1$ //$NON-NLS-2$
      ELEMENT element = getElement(elementInfos[0]);
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
        Log.logDebug("Node to change:\r\n" + nodeToChange); //$NON-NLS-1$
      }
      if (nodeToChange == null) {
        return;
      }
      runImpl(marker, element, nodeToChange);
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
   * @return type of java element: type, method or field
   */
  private ELEMENT getElement(String elementType) {
    if (elementType.equals(MarkerFactory.JAVA_ELEMENT_TYPE)) {
      return ELEMENT.TYPE;
    }
    else if (elementType.equals(MarkerFactory.JAVA_ELEMENT_METHOD)) {
      return ELEMENT.METHOD;
    }
    else if (elementType.equals(MarkerFactory.JAVA_ELEMENT_FIELD)) {
      return ELEMENT.FIELD;
    }
    return null;
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
  protected final ListRewrite getModifierListRewrite(ELEMENT element,
      BodyDeclaration nodeToChange) {
    switch (element) {
      case TYPE:
        return rewrite.getListRewrite(nodeToChange,
            TypeDeclaration.MODIFIERS2_PROPERTY);
      case METHOD:
        return rewrite.getListRewrite(nodeToChange,
            MethodDeclaration.MODIFIERS2_PROPERTY);
      case FIELD:
        return rewrite.getListRewrite(nodeToChange,
            FieldDeclaration.MODIFIERS2_PROPERTY);
    }
    return null;
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

  public abstract void runImpl(IMarker marker, ELEMENT element,
      BodyDeclaration nodeToChange) throws Exception;

  public String getDescription() {
    return null;
  }

  /**
   * Find a ASTNode by line number
   */
  private static final class FindNodeToChangeVisitor extends ASTVisitor {
    private BodyDeclaration nodeFound = null;
    private final IDocument doc;
    private final int lineMarker;

    protected FindNodeToChangeVisitor(IDocument doc, int lineNrMarker) {
      this.doc = doc;
      this.lineMarker = lineNrMarker;
    }

    private boolean visitImpl(BodyDeclaration declaration, SimpleName name) {
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
    public boolean visit(TypeDeclaration declaration) {
      return visitImpl(declaration, declaration.getName());
    }

    @Override
    public boolean visit(MethodDeclaration declaration) {
      return visitImpl(declaration, declaration.getName());
    }

    /**
     * Use name of last VariableDeclarationFragment for SimpleName
     */
    @Override
    public boolean visit(FieldDeclaration declaration) {
      List<?> fragments = declaration.fragments();
      if (fragments.size() > 0) {
        Object last = fragments.get(fragments.size() - 1);
        SimpleName name = ((VariableDeclarationFragment) last).getName();
        visitImpl(declaration, name);
      }
      return false;
    }

    @Override
    public boolean visit(EnumDeclaration declaration) {
      return visitImpl(declaration, declaration.getName());
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration declaration) {
      return visitImpl(declaration, declaration.getName());
    }

    @Override
    public boolean visit(EnumConstantDeclaration declaration) {
      visitImpl(declaration, declaration.getName());
      return false;
    }
  }
}