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
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IMarkerResolution2;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.MarkerFactory;

/**
 * Base class for all UCDetector QuickFixes
 * 
 * http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html
 * @see http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html
 */
abstract class AbstractUCDQuickFix implements IMarkerResolution2 {
  static enum ELEMENT {
    TYPE, METHOD, FIELD;
  }

  private String markerType;
  /** Parameters of the methods */
  //  private String elementName;
  // ---------------------------------------------------------------------------
  protected ASTRewrite rewrite;
  protected IDocument doc;
  protected ITextFileBuffer textFileBuffer;
  private int lineNrMarker;

  @SuppressWarnings("unchecked")
  public void run(IMarker marker) {
    ITextFileBufferManager bufferManager = FileBuffers
        .getTextFileBufferManager();
    IPath path = marker.getResource().getLocation();
    try {
      if (UCDetectorPlugin.DEBUG) {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("run().Marker="); //$NON-NLS-1$
        sb.append(new HashMap(marker.getAttributes()));
        Log.logDebug(sb.toString());
      }
      // -----------------------------------------------------------------------
      markerType = marker.getType();
      lineNrMarker = marker.getAttribute(IMarker.LINE_NUMBER, -1);
      // -----------------------------------------------------------------------
      bufferManager.connect(path, LocationKind.NORMALIZE, null);
      textFileBuffer = bufferManager.getTextFileBuffer(path,
          LocationKind.NORMALIZE);
      doc = textFileBuffer.getDocument();
      // -----------------------------------------------------------------------
      String[] elementInfos = marker.getAttribute(
          MarkerFactory.JAVA_ELEMENT_ATTRIBUTE, "?").split(","); //$NON-NLS-1$ //$NON-NLS-2$
      ELEMENT element = getElement(elementInfos[0]);
      //      elementName = elementInfos[1];
      // -----------------------------------------------------------------------
      ICompilationUnit originalUnit = getCompilationUnit(marker);
      CompilationUnit copyUnit = createCopy(originalUnit);
      rewrite = ASTRewrite.create(copyUnit.getAST());

      AbstractTypeDeclaration firstType = (AbstractTypeDeclaration) copyUnit
          .types().get(0);
      FindNodeToChangeVisitor visitor = new FindNodeToChangeVisitor();
      firstType.accept(visitor);
      BodyDeclaration nodeToChange = visitor.nodeToChange;
      if (UCDetectorPlugin.DEBUG) {
        Log.logDebug("Node to change:\r\n" + nodeToChange); //$NON-NLS-1$
      }
      if (nodeToChange == null) {
        return;
      }
      runImpl(marker, element, nodeToChange);
      marker.delete();
    }
    catch (Exception e) {
      Log.logErrorAndStatus("Quick Fix Problems", e); //$NON-NLS-1$
    }
    finally {
      try {
        bufferManager.disconnect(path, LocationKind.NORMALIZE, null);
      }
      catch (CoreException e) {
        Log.logErrorAndStatus("Quick Fix Problems", e); //$NON-NLS-1$
      }
    }
  }

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

  private ICompilationUnit getCompilationUnit(IMarker marker) {
    IResource resource = marker.getResource();
    if (resource instanceof IFile && resource.isAccessible()) {
      IFile file = (IFile) resource;
      IJavaElement javaElement = JavaCore.create(file);
      if (javaElement instanceof ICompilationUnit) {
        return (ICompilationUnit) javaElement;
      }
    }
    Log.logWarn("Can't find CompilationUnit: " //$NON-NLS-1$
        + this.markerType); 
    return null;
  }

  private CompilationUnit createCopy(ICompilationUnit unit)
      throws JavaModelException {
    unit.becomeWorkingCopy(null);
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setSource(unit);
    parser.setResolveBindings(true);
    return (CompilationUnit) parser.createAST(null);
  }

  protected final void commit(IMarker marker) throws CoreException,
      BadLocationException {
    TextEdit edits = rewrite.rewriteAST(doc, null);
    edits.apply(doc);
    textFileBuffer.commit(null, true);
  }

  protected final ListRewrite getListRewrite(ELEMENT element,
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
   * Finds a ASTNode by line number
   */
  private final class FindNodeToChangeVisitor extends ASTVisitor {
    private BodyDeclaration nodeToChange = null;

    private boolean visitImpl(BodyDeclaration declaration) {
      try {
        int start = declaration.getStartPosition();
        int lineNrDeclaration = doc.getLineOfOffset(start) + 1;
        if (lineNrDeclaration == lineNrMarker) {
          nodeToChange = declaration;
        }
      }
      catch (BadLocationException e) {
        Log.logError("Can't get line", e); //$NON-NLS-1$
        return false;
      }
      return nodeToChange == null;
    }

    @Override
    public boolean visit(TypeDeclaration declaration) {
      return visitImpl(declaration);
    }

    @Override
    public boolean visit(EnumDeclaration declaration) {
      return visitImpl(declaration);
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration declaration) {
      return visitImpl(declaration);
    }

    @Override
    public boolean visit(MethodDeclaration declaration) {
      return visitImpl(declaration);
    }

    @Override
    public boolean visit(FieldDeclaration declaration) {
      visitImpl(declaration);
      return false;
    }

    @Override
    public boolean visit(EnumConstantDeclaration declaration) {
      visitImpl(declaration);
      return false;
    }
  }
}
