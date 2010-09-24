/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * Count number of matches
 * <p>
 * @author Joerg Spieler
 * @since 2009-12-31
 */
public class CountSearchRequestor extends SearchRequestor {
  private int found = 0;

  @Override
  public void acceptSearchMatch(SearchMatch match) {
    //    System.out.println("~~~~~~~~acceptSearchMatch=" + match.getElement());
    if (match.getElement() instanceof IJavaElement) {
      this.found++;
    }
  }

  @Override
  public String toString() {
    return "found: " + found; //$NON-NLS-1$
  }

  public boolean isFound() {
    return found > 0;
  }

  public int getFoundCount() {
    return found;
  }
}