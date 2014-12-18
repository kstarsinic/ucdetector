/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.ucdetector.util.JavaElementUtil;

/**
 * Container for a javaElement (for example a method, field, import declaration)
 * and the regions, where a reference is found
 * 
 * <pre>
 * SearchResultRoot
 *   |- SearchResult
 *     |- Cycle
 *       |- CycleType
 *         |- <font color="red">CycleMember</font>
 *           |- CycleRegion
 */
public class CycleMember extends CycleJavaElement {
  private final IJavaElement match;
  private final List<CycleRegion> cycleRegions = new ArrayList<>();
  private CycleType parent;

  public CycleMember(IJavaElement match) {
    if (match == null) {
      throw new IllegalArgumentException("match may not be null"); //$NON-NLS-1$
    }
    this.match = match;
  }

  public IJavaElement getMatch() {
    return match;
  }

  @Override
  public List<CycleRegion> getChildren() {
    return cycleRegions;
  }

  @Override
  public IJavaElement getJavaElement() {
    return match;
  }

  @Override
  public CycleType getParent() {
    return parent;
  }

  @Override
  public String getText() {
    IType typeFor = JavaElementUtil.getTypeFor(match, false);
    StringBuilder sb = new StringBuilder();
    sb.append(super.getDefaultText(typeFor));
    if (match instanceof IImportDeclaration) {
      sb.append(" (import declaration)"); //$NON-NLS-1$
      return sb.toString();
    }
    sb.append('.').append(getDefaultText(match));
    return sb.toString();
  }

  void setParent(CycleType parent) {
    this.parent = parent;
  }
}
