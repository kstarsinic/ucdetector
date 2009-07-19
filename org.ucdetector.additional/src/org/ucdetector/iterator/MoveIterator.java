/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.ucdetector.search.SearchManager;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;

/**
 * Suggest to move classes
 */
public class MoveIterator extends AdditionalIterator {
  private final List<IType> types = new ArrayList<IType>();

  @Override
  protected void handleType(IType type) throws CoreException {
    types.add(type);
  }

  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
    for (IType type : types) {
      SearchPattern pattern = SearchPattern.createPattern(type,
          IJavaSearchConstants.REFERENCES);
      MatchPerPackageRequestor requestor = new MatchPerPackageRequestor();
      JavaElementUtil.runSearch(pattern, requestor, scope);
      createMarker(requestor.matchPerPackage, type);
    }
  }

  boolean createMarker(Map<String, Integer> matchPerPackage, IType type) {
    if (matchPerPackage.isEmpty()) {
      return false;
    }
    String startPackage = JavaElementUtil.getPackageFor(type).getElementName();

    Collection<Integer> matchCounts = matchPerPackage.values();
    int maxMatchCount = 0;
    for (Integer matchCount : matchCounts) {
      maxMatchCount = Math.max(maxMatchCount, matchCount.intValue());
    }
    Set<String> mostMatchedPackages = new HashSet<String>();
    for (Entry<String, Integer> packageAndMatch : matchPerPackage.entrySet()) {
      if (packageAndMatch.getValue().intValue() == maxMatchCount) {
        mostMatchedPackages.add(packageAndMatch.getKey());
      }
    }
    if (mostMatchedPackages.contains(startPackage)) {
      return false;
    }
    //    System.out.println("mostMatchedPackages=" + toString(mostMatchedPackages)
    //        + "->" + maxMatchCount);
    System.out.println("Move class " + JavaElementUtil.getTypeName(type)
        + " to " + mostMatchedPackages.toString() + " (" + maxMatchCount + ")");
    System.out
        .println("\tmatchPerPackage=\n\t\t" + matchPerPackage.toString().replace(", ", "\n\t\t")); //$NON-NLS-1$
    // TODO: new type
    return false; //markerFactory.createVisibilityMarker(member,
    //        MarkerFactory.UCD_MARKER_USE_DEFAULT, line);
  }

  private static final class MatchPerPackageRequestor extends SearchRequestor {
    private final Map<String, Integer> matchPerPackage = new LinkedHashMap<String, Integer>();

    @Override
    public void acceptSearchMatch(SearchMatch match) {
      IJavaElement javaMatch = SearchManager.defaultIgnoreMatch(match);
      if (javaMatch == null) {
        return;
      }
      String pakage = JavaElementUtil.getPackageFor(javaMatch).getElementName();
      Integer matchCount = matchPerPackage.get(pakage);
      int iMatchCount = (matchCount == null ? 0 : matchCount.intValue()) + 1;
      matchPerPackage.put(pakage, Integer.valueOf(iMatchCount));
      //    System.out.println("matchPerPackage=" + toString(matchPerPackage)); //$NON-NLS-1$
    }
  }

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement)
      throws CoreException {
    MarkerFactory.deleteMarkers(javaElement);
  }

  @Override
  public String getMessage() {
    return "Found TODO";
  }

  @Override
  public String getJobName() {
    return "Try to find classes to move";
  }
}
