/**
 * Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;

class MoveHandler {
  private final MarkerFactory markerFactory;
  private final Map<String, Integer> matchPerPackage = new LinkedHashMap<String, Integer>();
  private final String startPackage;
  private final IMember startElement;

  MoveHandler(MarkerFactory markerFactory, IMember startElement) {
    this.markerFactory = markerFactory;
    this.startElement = startElement;
    this.startPackage = JavaElementUtil.getPackageFor(startElement)
        .getElementName();
  }

  void addMatch(IJavaElement match) {
    if (!(startElement instanceof IType)) {
      return;
    }
    String pakage = JavaElementUtil.getPackageFor(match).getElementName();
    Integer matchCount = matchPerPackage.get(pakage);
    int iMatchCount = (matchCount == null ? 0 : matchCount.intValue()) + 1;
    matchPerPackage.put(pakage, Integer.valueOf(iMatchCount));
    //    System.out.println("matchPerPackage=" + toString(matchPerPackage)); //$NON-NLS-1$
  }

  boolean createMarker(IMember member, int line) throws CoreException {
    if (matchPerPackage.isEmpty()) {
      return false;
    }
    Collection<Integer> matchCounts = matchPerPackage.values();
    int maxMatchCount = 0;
    for (Integer matchCount : matchCounts) {
      maxMatchCount = Math.max(maxMatchCount, matchCount.intValue());
    }
    Set<String> mostMatchedPackages = new HashSet<String>();
    Set<Entry<String, Integer>> entrySet = matchPerPackage.entrySet();
    for (Entry<String, Integer> packageAndMatch : entrySet) {
      if (packageAndMatch.getValue().intValue() == maxMatchCount) {
        mostMatchedPackages.add(packageAndMatch.getKey());
      }
    }
    if (mostMatchedPackages.contains(startPackage)) {
      return false;
    }
    //    System.out.println("mostMatchedPackages=" + toString(mostMatchedPackages)
    //        + "->" + maxMatchCount);
    System.out.println("Move class "
        + JavaElementUtil.getTypeName(startElement) + " to "
        + mostMatchedPackages.toString() + " (" + maxMatchCount + ")");
    System.out
        .println("\tmatchPerPackage=\n\t\t" + matchPerPackage.toString().replace(", ", "\n\t\t")); //$NON-NLS-1$
    // TODO: new type
    return false; //markerFactory.createVisibilityMarker(member,
    //        MarkerFactory.UCD_MARKER_USE_DEFAULT, line);
  }

  //  private String toString(Set<IPackageFragment> mostMatchedPackages) {
  //    StringBuffer buf = new StringBuffer();
  //    for (IPackageFragment pack : mostMatchedPackages) {
  //      buf.append(pack.getElementName()).append(", ");
  //    }
  //    return buf.toString();
  //  }
  //
  //  private static String toString(Map<IPackageFragment, Integer> map) {
  //    StringBuffer buf = new StringBuffer();
  //    for (Entry<IPackageFragment, Integer> entry : map.entrySet()) {
  //      buf.append(entry.getKey().getElementName());
  //      buf.append("->").append(entry.getValue()).append(", ");
  //    }
  //    return buf.toString();
  //  }

  //
  //  static class MatchPerPackageMap<IPackageFragment, Integer> extends
  //      LinkedHashMap {
  //    private static final long serialVersionUID = 1L;
  //
  //    @Override
  //  }
}
