package org.ucdetector.search;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * Count number of matches
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