/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

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
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IMarkerResolution;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;

/**
 * // http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html
 * @see http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html
 */
public class UCDQuickFix implements IMarkerResolution { // NO_UCD
  private final String problem;
  /** The marker marks a type */
  private final boolean isType;
  /** The marker marks a method */
  private final boolean isMethod;
  /** The marker marks a field */
  private final boolean isField;
  //
  /** Name of the type, method or field */
  private final String elementName;
  /** Parameters of the methods */
  private final String[] methodParameters;
  private ICompilationUnit originalUnit;
  private CompilationUnit copyUnit;
  private ASTRewrite rewrite;
  private TypeDeclaration type;
  private BodyDeclaration bodyDeclaration;

  UCDQuickFix(String problem, String javaElementString) {
    if (UCDetectorPlugin.DEBUG) {
      Log.logDebug("UCDQuickFix(): problem=" + problem + //$NON-NLS-1$
          ",javaElementString=" + javaElementString); //$NON-NLS-1$
    }
    if (problem == null || javaElementString == null) {
      throw new IllegalArgumentException("problem=" + problem //$NON-NLS-1$
          + ", javaElement=" + javaElementString); //$NON-NLS-1$
    }
    this.problem = problem;
    String[] split = javaElementString.split(","); //$NON-NLS-1$
    isType = split[0].equals(MarkerFactory.JAVA_ELEMENT_TYPE);
    isMethod = split[0].equals(MarkerFactory.JAVA_ELEMENT_METHOD);
    isField = split[0].equals(MarkerFactory.JAVA_ELEMENT_FIELD);
    elementName = split[1];
    methodParameters = new String[split.length - 2];
    System.arraycopy(split, 2, methodParameters, 0, methodParameters.length);
  }

  public String getLabel() {
    // ----------------------------------------------------------------------
    if (MarkerFactory.UCD_MARKER_UNUSED.equals(problem)) {
      return "Delete code"; //$NON-NLS-1$
    }
    if (MarkerFactory.UCD_MARKER_USE_PRIVATE.equals(problem)) {
      return "Change visibility to private"; //$NON-NLS-1$
    }
    if (MarkerFactory.UCD_MARKER_USE_PROETECTED.equals(problem)) {
      return "Change visibility to protected"; //$NON-NLS-1$
    }
    if (MarkerFactory.UCD_MARKER_USE_DEFAULT.equals(problem)) {
      return "Change visibility to default"; //$NON-NLS-1$
    }
    if (MarkerFactory.UCD_MARKER_USE_FINAL.equals(problem)) {
      return "Add keyword 'final'"; //$NON-NLS-1$
    }
    return null;
  }

  public void run(IMarker marker) {
    try {
      if (UCDetectorPlugin.DEBUG) {
        Log.logDebug("  QuickFix.run(): " + marker.getAttributes().values()); //$NON-NLS-1$
      }
      originalUnit = getCompilationUnit(marker);
      copyUnit = createCopy(originalUnit);
      rewrite = ASTRewrite.create(copyUnit.getAST());
      type = (TypeDeclaration) copyUnit.types().get(0);
      bodyDeclaration = getBodyDeclaration();
      if (bodyDeclaration == null) {
        return;
      }
      if (MarkerFactory.UCD_MARKER_UNUSED.equals(problem)) {
        runDeleteElement(marker);
      }
      // ------------------------------------------------------------------
      else if (MarkerFactory.UCD_MARKER_USE_PRIVATE.equals(problem) || //
          MarkerFactory.UCD_MARKER_USE_PROETECTED.equals(problem) || //
          MarkerFactory.UCD_MARKER_USE_DEFAULT.equals(problem)) {
        runSetVisibilityKeyword(marker);
      }
      // ------------------------------------------------------------------
      else if (MarkerFactory.UCD_MARKER_USE_FINAL.equals(problem)) {
        runAddKeywordFinal(marker);
      }
    }
    catch (Exception e) {
      Log.logErrorAndStatus("Quick Fix Problems", e); //$NON-NLS-1$
    }
  }

  // ---------------------------------------------------------------------------
  // RUN IMPL
  // ---------------------------------------------------------------------------
  private void runSetVisibilityKeyword(IMarker marker) throws CoreException,
      BadLocationException {
    if (UCDetectorPlugin.DEBUG) {
      Log.logDebug("  QuickFix visibility"); //$NON-NLS-1$
    }
    ListRewrite listRewrite = getListRewrite();
    Modifier modifierFound = getModifierVisibility(bodyDeclaration);
    Modifier modifierNew = null;
    if (MarkerFactory.UCD_MARKER_USE_PRIVATE.equals(problem)) {
      modifierNew = bodyDeclaration.getAST().newModifier(
          Modifier.ModifierKeyword.PRIVATE_KEYWORD);
    }
    else if (MarkerFactory.UCD_MARKER_USE_PROETECTED.equals(problem)) {
      modifierNew = bodyDeclaration.getAST().newModifier(
          Modifier.ModifierKeyword.PROTECTED_KEYWORD);
    }
    // default -> default
    if (modifierFound == null && modifierNew == null) {
      return; // nothing
    }
    // default -> private
    else if (modifierFound == null && modifierNew != null) {
      listRewrite.insertFirst(modifierNew, null);
    }
    // public -> default
    else if (modifierFound != null && modifierNew == null) {
      listRewrite.remove(modifierFound, null);
    }
    // public -> private
    else if (modifierFound != null && modifierNew != null) {
      listRewrite.replace(modifierFound, modifierNew, null);
    }
    commit(marker);
    marker.delete();
  }

  /**
   * ADD FINAL (methods, fields)
   */
  private void runAddKeywordFinal(IMarker marker) throws BadLocationException,
      CoreException {
    if (UCDetectorPlugin.DEBUG) {
      Log.logDebug("  QuickFix add final"); //$NON-NLS-1$
    }
    ListRewrite listRewrite = getListRewrite();
    Modifier modifierFound = getModifierVisibility(bodyDeclaration);
    Modifier modifierFinal = bodyDeclaration.getAST().newModifier(
        Modifier.ModifierKeyword.FINAL_KEYWORD);

    // default -> default
    if (modifierFound == null) {
      listRewrite.insertFirst(modifierFinal, null);
    }
    else {
      listRewrite.insertAfter(modifierFinal, modifierFound, null);
    }
    commit(marker);
    marker.delete();
  }

  /**
   * Delete a class, a method or a field
   */
  private void runDeleteElement(IMarker marker) throws CoreException,
      BadLocationException {
    if (UCDetectorPlugin.DEBUG) {
      Log.logDebug("  QuickFix delete: " + bodyDeclaration); //$NON-NLS-1$
    }
    rewrite.remove(bodyDeclaration, null);
    commit(marker);
    marker.delete();
  }

  private void commit(IMarker marker) throws CoreException,
      BadLocationException {
    ITextFileBufferManager bufferManager = FileBuffers
        .getTextFileBufferManager();
    IPath path = copyUnit.getJavaElement().getPath();
    try {
      bufferManager.connect(path, LocationKind.NORMALIZE, null);
      ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path,
          LocationKind.NORMALIZE);
      IDocument doc = textFileBuffer.getDocument();
      TextEdit edits = rewrite.rewriteAST(doc, originalUnit.getJavaProject()
          .getOptions(true));
      edits.apply(doc);
      textFileBuffer.commit(null, true);
    }
    finally {
      bufferManager.disconnect(path, LocationKind.NORMALIZE, null);
    }
  }

  private ListRewrite getListRewrite() {
    if (isField) {
      return rewrite.getListRewrite(bodyDeclaration,
          FieldDeclaration.MODIFIERS2_PROPERTY);
    }
    if (isMethod) {
      return rewrite.getListRewrite(bodyDeclaration,
          MethodDeclaration.MODIFIERS2_PROPERTY);
    }
    if (isType) {
      return rewrite.getListRewrite(bodyDeclaration,
          TypeDeclaration.MODIFIERS2_PROPERTY);
    }
    return null;
  }

  private BodyDeclaration getBodyDeclaration() {
    if (isField) {
      return findFieldDeclaration(type.getFields());
    }
    if (isMethod) {
      return findMethodDeclaration(type.getMethods());
    }
    if (isType) {
      return findTypeDeclaration(type);
    }
    return null;
  }

  // ---------------------------------------------------------------------------
  // HELPER
  // ---------------------------------------------------------------------------

  private TypeDeclaration findTypeDeclaration(TypeDeclaration td) {
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

  private static Modifier getModifierVisibility(BodyDeclaration declaration) {
    List<?> list = declaration.modifiers();
    for (Object o : list) {
      if (o.getClass().equals(Modifier.class)) {
        Modifier mdf = (Modifier) o;
        if (mdf.getKeyword().equals(ModifierKeyword.PUBLIC_KEYWORD)
            || mdf.getKeyword().equals(ModifierKeyword.PROTECTED_KEYWORD)) {
          return mdf;
        }
      }
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
        + this.problem + ", " + this.elementName); //$NON-NLS-1$
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

  // TODO 21.09.2008: Use IMarkerResolution2?
  //  public String getDescription() {
  //    return "Test Description";
  //  }
  //
  //  public Image getImage() {
  //    IPath path = new Path("icons").append("/cycle.gif"); //$NON-NLS-1$ //$NON-NLS-2$
  //    ImageDescriptor id = JavaPluginImages.createImageDescriptor(
  //        UCDetectorPlugin.getDefault().getBundle(), path, false);
  //    return id.createImage();
  //  }
}
