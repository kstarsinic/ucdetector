/**
 * Copyright (c) 2008 Joerg Spieler
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
import org.ucdetector.cycle.Messages;

/**
 * A region in a java file, for example the name of the method starts at offset
 * and has the lengths of lengths.
 * 
 * <pre>
 * SearchResultRoot
 *   |- SearchResult
 *     |- Cycle
 *       |- CycleType
 *         |- CycleMember<
 *           |-  <font color="red">CycleRegion</font>
 */
public class CycleRegion extends CycleBaseElement {
  private final CycleBaseElement parent;
  private final int offset;
  private final int length;
  private final int line;
  private final String strLine;

  public CycleRegion(CycleBaseElement parent, int offset, int length, int line,
      String strLine) {
    this.parent = parent;
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
  public List<ICycleBaseElement> getChildren() {
    return Collections.emptyList();
  }

  public Image getImage() {
    return JavaUI.getSharedImages().getImage(
        JavaPluginImages.IMG_OBJS_SEARCH_OCCURRENCE);
  }

  public CycleBaseElement getParent() {
    return parent;
  }

  public String getText() {
    StringBuilder sb = new StringBuilder();
    sb.append(Messages.CycleRegion_Line).append(line);
    sb.append(": ").append(strLine); //$NON-NLS-1$
    return sb.toString();
  }
}