/**
 * Copyright (c) 2009 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;

public class MoveHandler {
  private final MarkerFactory markerFactory;
  private final IMember startElement;
  private final Map<IPackageFragment, Integer> matchPerPackage = new HashMap<IPackageFragment, Integer>();
  private final IPackageFragment startPackage;

  MoveHandler(MarkerFactory markerFactory, IMember startElement) {
    this.markerFactory = markerFactory;
    this.startElement = startElement;
    startPackage = JavaElementUtil.getPackageFor(startElement);

  }

  void addMatch(IJavaElement match) {
    IPackageFragment pakage = JavaElementUtil.getPackageFor(match);
    Integer matchCount = matchPerPackage.get(pakage);
    int iMatchCount = (matchCount == null ? 0 : matchCount.intValue()) + 1;
    matchPerPackage.put(pakage, Integer.valueOf(iMatchCount));
  }

  boolean createMarker(IMember member, int line) throws CoreException {
    Collection<Integer> matchCounts = matchPerPackage.values();
    int maxMatchCount = 0;
    for (Integer matchCount : matchCounts) {
      maxMatchCount = Math.max(maxMatchCount, matchCount.intValue());
    }
    List<IPackageFragment> mostMatchedPackages = new ArrayList<IPackageFragment>();
    Set<Entry<IPackageFragment, Integer>> entrySet = matchPerPackage.entrySet();
    for (Entry<IPackageFragment, Integer> packageAndMatch : entrySet) {
      if (packageAndMatch.getValue().intValue() == maxMatchCount) {
        mostMatchedPackages.add(packageAndMatch.getKey());
      }
    }
    if (mostMatchedPackages.contains(startPackage)) {
      return false;
    }
    // TODO: new type
    return markerFactory.createVisibilityMarker(member,
        MarkerFactory.UCD_MARKER_USE_DEFAULT, line);
  }
}
