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

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.ucdetector.Messages;

/**
 * Contains all search results
 * 
 * <pre>
 * <font color="red">SearchResultRoot</font> 
 *   |- SearchResult
 *     |- Cycle
 *       |- CycleType
 *         |- CycleMember
 *           |- CycleRegion
 * </pre>
 */
public class SearchResultRoot extends CycleBaseElement {
  private static final SearchResultRoot INSTANCE = new SearchResultRoot();

  private static final List<SearchResult> searchResults = new ArrayList<SearchResult>();

  public static final SearchResultRoot getInstance() {
    return INSTANCE;
  }

  public List<SearchResult> getChildren() {
    return searchResults;
  }

  public Image getImage() {
    return getImage(ISharedImages.IMG_TOOL_BACK);
  }

  /**
   * @return always <code>null</code>
   */
  public ICycleBaseElement getParent() {
    return null;
  }

  public String getText() {
    return Messages.SearchResultRoot_Name;
  }

  @Override
  public ICycleBaseElement getNextMatch() {
    return getNextMatchFromChildren();
  }
}
