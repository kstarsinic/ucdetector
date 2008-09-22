package org.ucdetector.quickfix;

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
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IMarkerResolution2;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.MarkerFactory;

/**
 *
 */
abstract class AbstractUCDQuickFix implements IMarkerResolution2 {
  static enum ELEMENT {
    TYPE, METHOD, FIELD;
  }

  protected String markerType;
  /** Parameters of the methods */
  // TODO 22.09.2008: Use this field for: findMethodDeclaration()
  private String[] methodParameters;
  private String elementName;
  // ---------------------------------------------------------------------------
  protected ASTRewrite rewrite;

  public void run(IMarker marker) {
    try {
      // -----------------------------------------------------------------------
      String javaElement = (String) marker
          .getAttribute(MarkerFactory.JAVA_ELEMENT_ATTRIBUTE);
      markerType = marker.getType();
      if (UCDetectorPlugin.DEBUG) {
        Log.logDebug("UCDQuickFix(): problem=" + markerType + //$NON-NLS-1$
            ",javaElementString=" + javaElement); //$NON-NLS-1$
      }
      if (markerType == null || javaElement == null) {
        throw new IllegalArgumentException("problem=" + markerType //$NON-NLS-1$
            + ", javaElement=" + javaElement); //$NON-NLS-1$
      }
      String[] split = javaElement.split(","); //$NON-NLS-1$
      ELEMENT element = getElement(split[0]);
      elementName = split[1];
      methodParameters = new String[split.length - 2];
      System.arraycopy(split, 2, methodParameters, 0, methodParameters.length);
      // -----------------------------------------------------------------------
      ICompilationUnit originalUnit = getCompilationUnit(marker);
      CompilationUnit copyUnit = createCopy(originalUnit);
      rewrite = ASTRewrite.create(copyUnit.getAST());
      TypeDeclaration typeDeclaration = (TypeDeclaration) copyUnit.types().get(
          0);
      BodyDeclaration nodeToChange = getBodyDeclaration(element,
          typeDeclaration);
      if (nodeToChange == null) {
        return;
      }
      runImpl(marker, element, nodeToChange);
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

  public Image getImage() {
    //      IPath path = new Path("icons").append("/cycle.gif"); //$NON-NLS-1$ //$NON-NLS-2$
    //      ImageDescriptor id = JavaPluginImages.createImageDescriptor(
    //          UCDetectorPlugin.getDefault().getBundle(), path, false);
    //      return id.createImage();
    return null;
  }
}
