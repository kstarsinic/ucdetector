/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.search.SearchManager;
import org.ucdetector.search.UCDProgressMonitor;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;

/**
 * Suggest to move a class to another package
 */
public class MoveIterator extends AdditionalIterator {
  private static final String ANALYZE_MARKER_MOVE_CLASS = MarkerFactory.MARKER_PREFIX + "analyzeMarkerMoveClass"; //$NON-NLS-1$
  private final List<IType> types = new ArrayList<IType>();

  @Override
  protected boolean handleType(IType type) throws CoreException {
    // Only public types can be used in other packages
    if (isPublic(type) && JavaElementUtil.isPrimary(type)) {
      types.add(type);
    }
    return true;
  }

  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    List<MatchPerPackageList> allMatchPerPackages = new ArrayList<MatchPerPackageList>();
    UCDProgressMonitor monitor = getMonitor();
    monitor.beginTask(getJobName(), types.size());
    for (IType type : types) {
      monitor.worked(1);
      monitor.subTask(JavaElementUtil.getTypeName(type));
      SearchPattern pattern = SearchPattern.createPattern(type, IJavaSearchConstants.REFERENCES);
      MatchPerPackageRequestor requestor = new MatchPerPackageRequestor(type);
      JavaElementUtil.runSearch(pattern, requestor);
      createMarker(requestor.matchPerPackage, type);
      allMatchPerPackages.add(requestor.matchPerPackage);
    }
    for (MatchPerPackageList matchPerPackageList : allMatchPerPackages) {
      UCDetectorPlugin.dumpList(matchPerPackageList.toString(), "\n\t");
    }
  }

  private boolean createMarker(MatchPerPackageList matchPerPackage, IType type) throws CoreException {
    IResource resource = type.getResource();
    if (resource == null) {
      return false;
    }
    if (!matchPerPackage.matchInOneOtherPackage()) {
      return false;
    }
    MatchPerPackage targetPackageMatch = matchPerPackage.iterator().next();
    String message = "Move class " + JavaElementUtil.getTypeNameFull(type) //
        + " to " + targetPackageMatch.pakage;
    System.out.println(message);
    createMarker(type, message, ANALYZE_MARKER_MOVE_CLASS);
    return false;
  }

  /**
   * Count matches per package
   */
  private static final class MatchPerPackageRequestor extends SearchRequestor {
    private final MatchPerPackageList matchPerPackage;

    protected MatchPerPackageRequestor(IType type) {
      this.matchPerPackage = new MatchPerPackageList(JavaElementUtil.getTypeFor(type, true));
    }

    @Override
    public void acceptSearchMatch(SearchMatch match) {
      IJavaElement javaMatch = SearchManager.defaultIgnoreMatch(match);
      if (javaMatch == null || JavaElementUtil.isTestCode(javaMatch)) {
        return;
      }
      IType matchPrimaryType = JavaElementUtil.getTypeFor(javaMatch, true);
      if (matchPrimaryType.equals(matchPerPackage.getBaseType())) {
        return;// Ignore matches in same class
      }
      String pakage = JavaElementUtil.getPackageFor(javaMatch).getElementName();
      matchPerPackage.add(pakage);
      if (matchPerPackage.getForeignPackageCount() > 1) {
        //        throw new OperationCanceledException(
        //            "Type found in more then 1 other package");
      }
    }
  }

  /**
   * Contains a sorted list of MatchPerPackage and offers
   * statistic methods for the contained data
   */
  private static final class MatchPerPackageList {
    private final TreeSet<MatchPerPackage> delegate = new TreeSet<MatchPerPackage>();
    private final IType baseType;
    private final String basePackage;

    protected MatchPerPackageList(IType forType) {
      this.baseType = forType;
      basePackage = JavaElementUtil.getPackageFor(forType).getElementName();
    }

    public IType getBaseType() {
      return baseType;
    }

    private void add(String pakage) {
      for (MatchPerPackage matchPerPackage : delegate) {
        if (matchPerPackage.pakage.equals(pakage)) {
          delegate.remove(matchPerPackage);
          matchPerPackage.incrementMatch();
          delegate.add(matchPerPackage);
          return; // found!
        }
      }
      // not found
      delegate.add(new MatchPerPackage(pakage));
    }

    public Iterator<MatchPerPackage> iterator() {
      return delegate.iterator();
    }

    public int getPackageMatchCount() {
      return delegate.size();
    }

    public boolean matchInOneOtherPackage() {
      return !containsPackageForType() && getPackageMatchCount() == 1;
    }

    protected boolean containsPackage(String packageName) {
      for (MatchPerPackage matchPerPackage : delegate) {
        if (matchPerPackage.pakage.equals(packageName)) {
          return true;
        }
      }
      return false;
    }

    protected boolean containsPackageForType() {
      return containsPackage(basePackage);
    }

    public int getForeignPackageCount() {
      int size = delegate.size();
      return size == 0 ? 0 : containsPackage(basePackage) ? size - 1 : size;
    }

    @Override
    public String toString() {
      return JavaElementUtil.getTypeNameFull(baseType) + delegate.toString();
    }
  }

  /**
   * Data container class, containing the package name and the number of matches
   */
  private static final class MatchPerPackage implements Comparable<MatchPerPackage> {
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
      return match + "\t" + pakage;
    }

    private void incrementMatch() {
      match++;
    }

    public int compareTo(MatchPerPackage other) {
      if (match < other.match) {
        return 1;
      }
      return (match == other.match) ? pakage.compareTo(other.pakage) : -1;
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
      return match == other.match ? pakage.equals(other.pakage) : false;
    }
  }

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement) throws CoreException {
    if (javaElement.getResource() != null) {
      javaElement.getResource().deleteMarkers(ANALYZE_MARKER_MOVE_CLASS, true, IResource.DEPTH_INFINITE);
    }
  }

  @Override
  public String getMessage() {
    return "Found TODO";
  }

  @Override
  public String getJobName() {
    return "Detect classes possible to move";
  }
}
// 280
/*
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
*/