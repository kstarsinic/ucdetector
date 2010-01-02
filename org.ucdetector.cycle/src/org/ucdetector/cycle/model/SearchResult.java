/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.model;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.ucdetector.cycle.Messages;

/**
 * Contains cycles found by a search
 *
 * <pre>
 * SearchResultRoot
 *   |- <font color="red">SearchResult</font>
 *     |- Cycle
 *       |- CycleType
 *         |- CycleMember
 *           |- CycleRegion
 * </pre>
 */
public class SearchResult extends CycleBaseElement {
  // private final Date created = new Date();
  private List<Cycle> cycles = Collections.emptyList();
  private final CycleBaseElement parent;
  private final IJavaProject javaProject;
  private final IJavaElement[] selections;

  public SearchResult(SearchResultRoot parent, IJavaElement[] selections, IJavaProject javaProject) {
    this.parent = parent;
    this.selections = selections;
    this.javaProject = javaProject;
  }

  public List<Cycle> getChildren() {
    return cycles;
  }

  public Image getImage() {
    return JavaUI.getSharedImages().getImage(JavaPluginImages.IMG_OBJS_SEARCH_REF); // IMG_OBJS_JSEARCH
  }

  public CycleBaseElement getParent() {
    return parent;
  }

  // Search in "org.ucdetector.cycle" - org.ucdetector.example [found 4 cycles]
  // Search in "aaa,bbb,ccc" - org.ucdetector.example [found 4 cycles]
  public String getText() {
    StringBuilder search = new StringBuilder();
    for (IJavaElement selection : selections) {
      if (selection.equals(javaProject)) {
        search.setLength(0);
        search.append(javaProject.getElementName());
        break;
      }
      else if (selection.getJavaProject().equals(javaProject)) {
        search.append(search.length() > 0 ? ", " : ""); //$NON-NLS-1$ //$NON-NLS-2$
        search.append(selection.getElementName());
      }
    }
    Object[] bindings = new Object[] {//
    /*    */search, //
        javaProject.getElementName(), //
        Integer.valueOf(getChildren().size()) };
    return NLS.bind(Messages.SearchResult_get_text, bindings);
  }

  public void setCycles(List<Cycle> cycles) {
    this.cycles = cycles;
  }
}
