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
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
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
  // TODO 22.09.2008: Use this field for: findMethodDeclaration()
  private String[] methodParams;
  private String elementName;
  // ---------------------------------------------------------------------------
  protected ASTRewrite rewrite;

  @SuppressWarnings("unchecked")
  public void run(IMarker marker) {
    try {
      // -----------------------------------------------------------------------
      if (UCDetectorPlugin.DEBUG) {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("run().Marker="); //$NON-NLS-1$
        sb.append(new HashMap(marker.getAttributes()));
        Log.logDebug(sb.toString());
      }
      markerType = marker.getType();
      String[] elementInfos = marker.getAttribute(
          MarkerFactory.JAVA_ELEMENT_ATTRIBUTE, "?").split(","); //$NON-NLS-1$ //$NON-NLS-2$
      ELEMENT element = getElement(elementInfos[0]);
      elementName = elementInfos[1];
      methodParams = new String[elementInfos.length - 2];
      System.arraycopy(elementInfos, 2, methodParams, 0, methodParams.length);
      // -----------------------------------------------------------------------
      ICompilationUnit originalUnit = getCompilationUnit(marker);
      CompilationUnit copyUnit = createCopy(originalUnit);
      rewrite = ASTRewrite.create(copyUnit.getAST());
      // -----------------------------------------------------------------------
      // TODO 23.09.2008: handle AnnotationTypeDeclaration and EnumDeclaration
      //  use lines to get BodyDeclaration!
      // -----------------------------------------------------------------------
      Object firstType = copyUnit.types().get(0);
      BodyDeclaration nodeToChange = null;
      if (firstType instanceof TypeDeclaration) {
        TypeDeclaration typeDeclaration = (TypeDeclaration) firstType;
        nodeToChange = getBodyDeclaration(element, typeDeclaration);
      }
      else if (firstType instanceof AnnotationTypeDeclaration) {
        AnnotationTypeDeclaration atd = (AnnotationTypeDeclaration) firstType;
        atd.bodyDeclarations();
      }
      else if (firstType instanceof EnumDeclaration) {
        EnumDeclaration ed = (EnumDeclaration) firstType;
        ed.bodyDeclarations();
      }
      if (UCDetectorPlugin.DEBUG) {
        StringBuilder sb = new StringBuilder();
        sb.append("    Node to change='"); //$NON-NLS-1$
        sb.append(nodeToChange).append('\'');
        Log.logDebug(sb.toString());
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
  }

  private BodyDeclaration getBodyDeclaration(ELEMENT element,
      TypeDeclaration typeDeclaration) {
    switch (element) {
      case TYPE:
        return findTypeDeclaration(typeDeclaration);
      case METHOD:
        return findMethodDeclaration(typeDeclaration.getMethods());
      case FIELD:
        return findFieldDeclaration(typeDeclaration.getFields());
    }
    return null;
  }

  private ELEMENT getElement(String first) {
    ELEMENT element = null;
    if (first.equals(MarkerFactory.JAVA_ELEMENT_TYPE)) {
      element = ELEMENT.TYPE;
    }
    else if (first.equals(MarkerFactory.JAVA_ELEMENT_METHOD)) {
      element = ELEMENT.METHOD;
    }
    else if (first.equals(MarkerFactory.JAVA_ELEMENT_FIELD)) {
      element = ELEMENT.FIELD;
    }
    return element;
  }

  private final TypeDeclaration findTypeDeclaration(TypeDeclaration td) {
    String typeName = td.getName().getIdentifier();
    if (typeName.equals(this.elementName)) {
      return td;
    }
    TypeDeclaration[] types = td.getTypes();
    for (TypeDeclaration childTd : types) {
      typeName = childTd.getName().getIdentifier();
      if (typeName.equals(this.elementName)) {
        return childTd;
      }
    }
    Log.logWarn("Can't find type " + this.elementName); //$NON-NLS-1$
    return null;
  }

  private MethodDeclaration findMethodDeclaration(MethodDeclaration[] methods) {
    for (MethodDeclaration td : methods) {
      String methodName = td.getName().getIdentifier();
      if (methodName.equals(this.elementName)) {
        return td;
      }
    }
    Log.logWarn("Can't find method " + this.elementName); //$NON-NLS-1$
    return null;
  }

  private FieldDeclaration findFieldDeclaration(FieldDeclaration[] fields) {
    for (FieldDeclaration field : fields) {
      List<?> fragments = field.fragments();
      for (Object object : fragments) {
        if (object instanceof VariableDeclarationFragment) {
          VariableDeclarationFragment fragment = (VariableDeclarationFragment) object;
          String identifier = fragment.getName().getIdentifier();
          if (identifier.equals(this.elementName)) {
            return field;
          }
        }
      }
    }
    Log.logWarn("Can't find field " + this.elementName); //$NON-NLS-1$
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
        + this.markerType + ", " + this.elementName); //$NON-NLS-1$
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
    ITextFileBufferManager bufferManager = FileBuffers
        .getTextFileBufferManager();
    IPath path = marker.getResource().getLocation();// copyUnit.getJavaElement().getPath();
    try {
      bufferManager.connect(path, LocationKind.NORMALIZE, null);
      ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path,
          LocationKind.NORMALIZE);
      IDocument doc = textFileBuffer.getDocument();

      TextEdit edits = rewrite.rewriteAST(doc, null);
      edits.apply(doc);
      textFileBuffer.commit(null, true);
    }
    finally {
      bufferManager.disconnect(path, LocationKind.NORMALIZE, null);
    }
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
    for (Object o : list) {
      if (o.getClass().equals(Modifier.class)) {
        Modifier mdf = (Modifier) o;
        if (mdf.getKeyword().equals(ModifierKeyword.PUBLIC_KEYWORD)
            || mdf.getKeyword().equals(ModifierKeyword.PROTECTED_KEYWORD)
            || mdf.getKeyword().equals(ModifierKeyword.PRIVATE_KEYWORD)) {
          return mdf;
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

  // TODO 21.09.2008: Use IMarkerResolution2?
  public String getDescription() {
    return null;//"Test Description";
  }
}
