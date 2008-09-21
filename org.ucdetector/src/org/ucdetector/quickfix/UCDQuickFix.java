/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.MarkerFactory;

/**
 * // http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html
 * @see http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html
 */
public class UCDQuickFix extends AbstractUCDQuickFix { // NO_UCD
  public UCDQuickFix(IMarker marker) throws CoreException {
    super(marker);
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

  @Override
  public void runImpl(IMarker marker) throws Exception {
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
}
