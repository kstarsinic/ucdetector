/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.model;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.Messages;

/**
 * A region in a java file, for example the name of the method starts at offset
 * and has the lengths of lengths.
 * 
 * <pre>
 * SearchResultRoot
 *   |- SearchResult
 *     |- Cycle
 *       |- CycleType
 *         |- CycleMember
 *           |-  <font color="red">CycleRegion</font>
 */
public class CycleRegion extends CycleBaseElement {
  private final CycleMember cycleMember;
  private final int offset;
  private final int length;
  private final int line;
  private final String strLine;

  public CycleRegion(CycleMember cycleMember, int offset, int length, int line, String strLine) {
    this.cycleMember = cycleMember;
    this.offset = offset;
    this.length = length;
    this.line = line;
    this.strLine = strLine;
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }

  /**
   * @return always an empty list
   */
  @Override
  public List<CycleBaseElement> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public Image getImage() {
    //    return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_SEARCH_OCCURRENCE);
    return JavaUI.getSharedImages().getImage(JavaPluginImages.IMG_OBJS_SEARCH_OCCURRENCE);
  }

  @Override
  public CycleMember getParent() {
    return cycleMember;
  }

  @Override
  public String getText() {
    StringBuilder sb = new StringBuilder();
    sb.append(Messages.CycleRegion_Line).append(line);
    sb.append(": ").append(strLine); //$NON-NLS-1$
    return sb.toString();
  }
}