/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * Visit all BodyDeclaration's which UCDetector handles:
 * <ul>
 * <li>classes (types, enums, annotations)</li>
 * <li>methods</li>
 * <li>fields</li>
 * </ul>
 * See concrete classes of {@link org.eclipse.jdt.core.dom.BodyDeclaration}
 */
public abstract class ASTMemberVisitor extends ASTVisitor {

  protected abstract boolean visitImpl(BodyDeclaration declaration, SimpleName name);

  //----------------------------------------------------------------------------
  // TYPES
  //----------------------------------------------------------------------------
  @Override
  public boolean visit(TypeDeclaration declaration) {
    return visitImpl(declaration, declaration.getName());
  }

  @Override
  public boolean visit(EnumDeclaration declaration) {
    return visitImpl(declaration, declaration.getName());
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration declaration) {
    return visitImpl(declaration, declaration.getName());
  }

  //----------------------------------------------------------------------------
  // METHODS
  //----------------------------------------------------------------------------
  @Override
  public boolean visit(MethodDeclaration declaration) {
    return visitImpl(declaration, declaration.getName());
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration declaration) {
    return visitImpl(declaration, declaration.getName());
  }

  // NOTHING TO DO FOR:
  // public boolean visit(Initializer declaration) 

  //----------------------------------------------------------------------------
  // FIELDS
  //----------------------------------------------------------------------------
  /**
   * Use name of last VariableDeclarationFragment for SimpleName
   */
  @Override
  public boolean visit(FieldDeclaration declaration) {
    List<?> fragments = declaration.fragments();
    if (fragments.size() > 0) {
      Object last = fragments.get(fragments.size() - 1);
      VariableDeclarationFragment fragment = (VariableDeclarationFragment) last;
      SimpleName name = fragment.getName();
      visitImpl(declaration, name);
    }
    return false;
  }

  @Override
  public boolean visit(EnumConstantDeclaration declaration) {
    visitImpl(declaration, declaration.getName());
    return false;
  }
}
