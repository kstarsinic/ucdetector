/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.search.SearchManager;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;

/**
 * Suggest to move a class to another package
 */
public class MoveIterator extends AdditionalIterator {
  private final List<IType> types = new ArrayList<IType>();

  @Override
  protected void handleType(IType type) throws CoreException {
    // Only public types can be used in other packages
    if (isPublic(type) && JavaElementUtil.isPrimary(type)) {
      types.add(type);
    }
  }

  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
    for (IType type : types) {
      SearchPattern pattern = SearchPattern.createPattern(type,
          IJavaSearchConstants.REFERENCES);
      MatchPerPackageRequestor requestor = new MatchPerPackageRequestor(type);
      JavaElementUtil.runSearch(pattern, requestor, scope);
      System.out.println("---------------------------------------------------");
      createMarker(requestor.matchPerPackage, type);
    }
  }

  private boolean createMarker(MatchPerPackageList matchPerPackage, IType type) {
    if (matchPerPackage.isEmpty()) {
      return false;
    }
    String packageName = JavaElementUtil.getPackageFor(type).getElementName();
    System.out.print(packageName + "." + type.getElementName());
    UCDetectorPlugin.dumpList(matchPerPackage.toString(), "\n\t");
    Set<String> mostMatchedPackages = matchPerPackage.getMostMatchedPackages();
    if (mostMatchedPackages.contains(packageName)) {
      return false;
    }
    System.out.println("\nMove class " //
        + JavaElementUtil.getTypeName(type) //
        + " from " + packageName//
        + " to " + mostMatchedPackages.toString());
    return true;
  }

  /**
   * Count matches per package
   */
  private static final class MatchPerPackageRequestor extends SearchRequestor {
    private final MatchPerPackageList matchPerPackage = new MatchPerPackageList();
    private final IType startPrimaryType;

    public MatchPerPackageRequestor(IType type) {
      this.startPrimaryType = JavaElementUtil.getTypeFor(type, true);
    }

    @Override
    public void acceptSearchMatch(SearchMatch match) {
      IJavaElement javaMatch = SearchManager.defaultIgnoreMatch(match);
      if (javaMatch == null) {
        return;
      }
      if (JavaElementUtil.isTestCode(javaMatch)) {
        return;
      }
      IType matchPrimaryType = JavaElementUtil.getTypeFor(javaMatch, true);
      // Ignore matches in same class
      if (matchPrimaryType.equals(startPrimaryType)) {
        return;
      }
      String pakage = JavaElementUtil.getPackageFor(javaMatch).getElementName();
      matchPerPackage.add(pakage);
      //      System.out.println("matchPerPackage=" + matchPerPackage); //$NON-NLS-1$
    }
  }

  /**
   * Contains a sorted list of MatchPerPackage and offers 
   * statistic methods for the contained data
   */
  private static final class MatchPerPackageList {
    private final TreeSet<MatchPerPackage> delegate = new TreeSet<MatchPerPackage>();

    private void add(String pakage) {
      MatchPerPackage found = null;
      for (MatchPerPackage matchPerPackage : delegate) {
        if (matchPerPackage.pakage.equals(pakage)) {
          found = matchPerPackage;
          break;
        }
      }
      if (found != null) {
        delegate.remove(found);
        found.incrementMatch();
        delegate.add(found);
        return;
      }
      delegate.add(new MatchPerPackage(pakage));
    }

    private int getMaxMatchCount() {
      int result = 0;
      for (MatchPerPackage matchPerPackage : delegate) {
        result = Math.max(result, matchPerPackage.match);
      }
      return result;
    }

    private Set<String> getMostMatchedPackages() {
      int maxMatchCount = getMaxMatchCount();
      Set<String> result = new HashSet<String>();
      for (MatchPerPackage matchPerPackage : delegate) {
        if (matchPerPackage.match == maxMatchCount) {
          result.add(matchPerPackage.pakage);
        }
      }
      return result;
    }

    public boolean isEmpty() {
      return delegate.isEmpty();
    }

    @Override
    public String toString() {
      return delegate.toString();
    }
  }

  /**
   * Data container class, containing the package name and the number of matches
   */
  private static final class MatchPerPackage implements
      Comparable<MatchPerPackage> {
    private final String pakage;
    private int match = 1;

    private MatchPerPackage(String pakage) {
      if (pakage == null) {
        throw new IllegalArgumentException("package may not be null");
      }
      this.pakage = pakage;
    }

    @Override
    public String toString() {
      return pakage + ": " + match;
    }

    private void incrementMatch() {
      match++;
    }

    public int compareTo(MatchPerPackage other) {
      if (match < other.match) {
        return 1;
      }
      if (match == other.match) {
        return pakage.compareTo(other.pakage);
      }
      return -1;
    }

    @Override
    public int hashCode() {
      return 31 * (31 + match) + pakage.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof MatchPerPackage)) {
        return false;
      }
      MatchPerPackage other = (MatchPerPackage) obj;
      if (match != other.match) {
        return false;
      }
      return pakage.equals(other.pakage);
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
