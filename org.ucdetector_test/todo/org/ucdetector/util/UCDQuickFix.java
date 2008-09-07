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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IMarkerResolution;
import org.ucdetector.UCDetectorPlugin;

/**
 * // http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.jdt.doc.isv/
 * // reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html
 * @see http://www.eclipse.org/articles/article.php?file=Article-
 *      JavaCodeManipulation_AST/index.html
 */
public class UCDQuickFix implements IMarkerResolution { // NO_UCD
  // TODO 331.08.2008: monitor is not set
  private IProgressMonitor monitor; // NO_UCD
  //
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

  UCDQuickFix(String problem, String javaElementString) {
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
    // DELETE (classes, methods, fields)
    if (MarkerFactory.PROBLEM_UNUSED.equals(problem)) {
      return "Delete code (EXPERIMENTAL)"; //$NON-NLS-1$
    }
    // ----------------------------------------------------------------------
    // CHANGE KEYWORD (methods, fields), REMOVE KEYWORD (classes)
    if (MarkerFactory.PROBLEM_USE_PRIVATE.equals(problem)) {
      return "Change visibility to private (EXPERIMENTAL)"; //$NON-NLS-1$
    }
    if (MarkerFactory.PROBLEM_USE_PROTECTED.equals(problem)) {
      return "Change visibility to protected (EXPERIMENTAL)"; //$NON-NLS-1$
    }
    if (MarkerFactory.PROBLEM_USE_DEFAULT.equals(problem)) {
      return "Change visibility to default (EXPERIMENTAL)"; //$NON-NLS-1$
    }
    // ----------------------------------------------------------------------
    // ADD FINAL (methods, fields)
    if (MarkerFactory.PROBLEM_USE_FINAL.equals(problem)) {
      return "Add keyword 'final' (EXPERIMENTAL)"; //$NON-NLS-1$
    }
    // ----------------------------------------------------------------------
    return "todo jsp: fixme"; //$NON-NLS-1$
  }

  public void run(IMarker marker) {
    try {
      if (MarkerFactory.PROBLEM_UNUSED.equals(problem)) {
        runDeleteElement(marker);
      }
      // ------------------------------------------------------------------
      else if (MarkerFactory.PROBLEM_USE_PRIVATE.equals(problem) || //
          MarkerFactory.PROBLEM_USE_PROTECTED.equals(problem) || //
          MarkerFactory.PROBLEM_USE_DEFAULT.equals(problem)) {
        runSetVisibilityKeyword(marker);
      }
      // ------------------------------------------------------------------
      else if (MarkerFactory.PROBLEM_USE_FINAL.equals(problem)) {
        runAddKeywordFinal(marker);
      }
      else {
        MessageDialog.openInformation(null, "QuickFix Demo", //$NON-NLS-1$
            "This quick-fix is not yet implemented"); //$NON-NLS-1$
      }
    }
    catch (Exception e) {
      UCDetectorPlugin.logError("Quick Fix Problems", e); //$NON-NLS-1$
    }
  }

  // ---------------------------------------------------------------------------
  // RUN IMPL
  // ---------------------------------------------------------------------------

  /**
   * ADD FINAL (methods, fields)
   * @throws BadLocationException 
   * @throws CoreException 
   */
  private void runAddKeywordFinal(IMarker marker) throws BadLocationException,
      CoreException {
    ICompilationUnit originalUnit = getCompilationUnit(marker);
    Document doc = new Document(originalUnit.getBuffer().getContents());
    CompilationUnit copyUnit = createCopy(originalUnit);
    ASTRewrite rewrite = ASTRewrite.create(copyUnit.getAST());
    // TODO JSPIELER 20.07.2008: find class, method, field
    TypeDeclaration td = (TypeDeclaration) copyUnit.types().get(0);
    //    ASTNode nodeToRewrite = null;
    Modifier finalModifier = copyUnit.getAST().newModifier(
        Modifier.ModifierKeyword.FINAL_KEYWORD);
    ListRewrite modRewrite = null;
    if (isField) {
      ASTNode nodeToRewrite = findFieldDeclaration(td.getFields());
      modRewrite = rewrite.getListRewrite(nodeToRewrite,
          FieldDeclaration.MODIFIERS2_PROPERTY);
    }
    if (isMethod) {
      ASTNode nodeToRewrite = findMethodDeclaration(td.getMethods());
      modRewrite = rewrite.getListRewrite(nodeToRewrite,
          MethodDeclaration.MODIFIERS2_PROPERTY);
    }
    if (modRewrite != null) {
      modRewrite.insertLast(finalModifier, null);
    }
    rewriteCompilationUnit(rewrite, doc, originalUnit);
    // TODO JSPIELER 20.07.2008: refresh markers
    marker.delete();
  }

  private void runSetVisibilityKeyword(IMarker marker) throws CoreException,
      BadLocationException {
    ICompilationUnit originalUnit = getCompilationUnit(marker);
    Document doc = new Document(originalUnit.getBuffer().getContents());
    CompilationUnit copyUnit = createCopy(originalUnit);
    ASTRewrite rewrite = ASTRewrite.create(copyUnit.getAST());
    // TODO JSPIELER 20.07.2008: find class, method, field
    TypeDeclaration td = (TypeDeclaration) copyUnit.types().get(0);

    Modifier modifierPublic = getModifier(td, ModifierKeyword.PUBLIC_KEYWORD);
    ListRewrite listRewrite = rewrite.getListRewrite(td,
        TypeDeclaration.MODIFIERS2_PROPERTY);
    listRewrite.remove(modifierPublic, null);
    rewriteCompilationUnit(rewrite, doc, originalUnit);
    marker.delete();
  }

  /**
   * Delete a class, a method or a field
   */
  private void runDeleteElement(IMarker marker) throws CoreException,
      BadLocationException {
    ICompilationUnit originalUnit = getCompilationUnit(marker);
    Document doc = new Document(originalUnit.getBuffer().getContents());
    CompilationUnit workingUnit = createCopy(originalUnit);
    ASTRewrite rewrite = ASTRewrite.create(workingUnit.getAST());
    TypeDeclaration td = (TypeDeclaration) workingUnit.types().get(0);

    ASTNode nodeToRemove = null;
    if (isType) {
      nodeToRemove = td;
    }
    else if (isMethod) {
      nodeToRemove = findMethodDeclaration(td.getMethods());
    }
    else if (isField) {
      nodeToRemove = findFieldDeclaration(td.getFields());
    }
    //
    if (nodeToRemove != null) {
      rewrite.remove(nodeToRemove, null);
    }
    //
    if (true) {
      // http://www.eclipse.org/articles/article.php?file=Article-
      // JavaCodeManipulation_AST/index.html
      ITextFileBufferManager bufferManager = FileBuffers
          .getTextFileBufferManager(); // get the buffer manager
      IPath path = workingUnit.getJavaElement().getPath(); // unit: instance of
      // CompilationUnit
      try {
        bufferManager.connect(path, null); // (1)
        ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path);
        // retrieve the buffer
        IDocument document = textFileBuffer.getDocument();
        TextEdit edits = rewrite.rewriteAST(document, originalUnit
            .getJavaProject().getOptions(true));
        edits.apply(document);
        textFileBuffer.commit(null, true);
      }
      finally {
        bufferManager.disconnect(path, null);
      }
    }
    else {
      rewriteCompilationUnit(rewrite, doc, originalUnit);
    }
    marker.delete();
  }

  // ---------------------------------------------------------------------------
  // HELPER
  // ---------------------------------------------------------------------------

  /*
  private TypeDeclaration findTypeDeclaration(List list) {
    for (Object object : list) {
      TypeDeclaration td = (TypeDeclaration) object;
      if (td.getName().equals(this.elementName)) {
        return td;
      }
    }
    throw new IllegalArgumentException("Can't find type " + this.elementName);
  }
  */

  private MethodDeclaration findMethodDeclaration(MethodDeclaration[] methods) {
    for (MethodDeclaration td : methods) {
      String methodName = td.getName().getIdentifier();
      if (methodName.equals(this.elementName)) {
        return td;
      }
    }
    throw new IllegalArgumentException("Can't find method " + this.elementName); //$NON-NLS-1$
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
    throw new IllegalArgumentException("Can't find field " + this.elementName); //$NON-NLS-1$
  }

  private static Modifier getModifier(BodyDeclaration declaration,
      ModifierKeyword keyword) {
    List<?> list = declaration.modifiers();
    for (Object o : list) {
      if (o.getClass().equals(Modifier.class)) {
        Modifier mdf = (Modifier) o;
        if (mdf.getKeyword().equals(keyword)) {
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
    throw new RuntimeException("Can't find CompilationUnit for marker: " //$NON-NLS-1$
        + this.problem + ", " + this.elementName); //$NON-NLS-1$
  }

  private CompilationUnit createCopy(ICompilationUnit unit)
      throws JavaModelException {
    unit.becomeWorkingCopy(monitor);
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setSource(unit);
    parser.setResolveBindings(true);
    return (CompilationUnit) parser.createAST(monitor);
  }

  private void rewriteCompilationUnit(ASTRewrite rewrite, IDocument doc,
      ICompilationUnit originalUnit) throws JavaModelException,
      BadLocationException {
    TextEdit edits = rewrite.rewriteAST(doc, originalUnit.getJavaProject()
        .getOptions(true));
    edits.apply(doc);
    originalUnit.getBuffer().setContents(doc.get());
    originalUnit.commitWorkingCopy(true, monitor);
  }
}
