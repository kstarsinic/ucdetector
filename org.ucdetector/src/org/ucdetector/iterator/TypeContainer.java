/**
 * Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

/**
 * Contains a type and its methods/fields 
 */
public class TypeContainer {
  private final IType type;
  private final List<IField> fields = new ArrayList<IField>();
  private final List<IMethod> methods = new ArrayList<IMethod>();

  @SuppressWarnings("ucd")
  public TypeContainer(IType type) {
    this.type = type;
  }

  public IType getType() {
    return type;
  }

  public List<IField> getFields() {
    return fields;
  }

  public List<IMethod> getMethods() {
    return methods;
  }

  @SuppressWarnings("ucd")
  public int size() {
    return 1 + getFields().size() + getMethods().size();
  }
}
