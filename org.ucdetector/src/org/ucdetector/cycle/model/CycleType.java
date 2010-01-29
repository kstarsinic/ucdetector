/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.model;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;

/**
 * This is a class, making part of a cycle
 * 
 * <pre>
 * SearchResultRoot
 *   |- SearchResult
 *     |- Cycle
 *       |- <font color="red">CycleType</font>
 *         |- CycleMember
 *           |- CycleRegion
 */
public class CycleType extends CycleJavaElement {
  private final List<CycleMember> cycleMembers;
  private final IType type;
  private Cycle parent;

  public CycleType(IType type, List<CycleMember> cycleMembers) {
    if (type == null) {
      throw new IllegalArgumentException("root may not be null"); //$NON-NLS-1$
    }
    if (cycleMembers == null || cycleMembers.size() == 0) {
      throw new IllegalArgumentException("cycleMembers must exist"); //$NON-NLS-1$
    }
    this.type = type;
    this.cycleMembers = cycleMembers;
    for (CycleMember cycleMember : cycleMembers) {
      cycleMember.setParent(this);
    }
  }

  public List<CycleMember> getChildren() {
    return cycleMembers;
  }

  public Image getImage() {
    return getDefaultImage(type);
  }

  @Override
  public IType getJavaElement() {
    return type;
  }

  public Cycle getParent() {
    return parent;
  }

  int getMatchCount() {
    int result = 0;
    for (CycleMember cycleMember : cycleMembers) {
      result += cycleMember.getChildrenSize();
    }
    return result;
  }

  public String getText() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.getDefaultText(type)).append(' ');
    int size = getMatchCount();
    sb.append('(').append(size);
    sb.append(size < 2 ? Messages.CycleType_match : Messages.CycleType_matches);
    sb.append(')');
    return sb.toString();
  }

  void setParent(Cycle parent) {
    this.parent = parent;
  }
}
