/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchMatch;
import org.ucdetector.Log;
import org.ucdetector.cycle.model.CycleMember;
import org.ucdetector.cycle.model.CycleRegion;
import org.ucdetector.cycle.model.CycleType;
import org.ucdetector.search.LineManger;
import org.ucdetector.util.JavaElementUtil;

/**
 * Collect the search results for a class
 * 
 * <pre>
 * Type
 *  |- CycleMember
 *    |- JavaElement
 *     |- Match
 *       |- CycleRegion
 * </pre>
 */
class TypeAndMatches {
  private final IType root;
  private final LineManger lineManger = new LineManger();

  private final Map<IType, List<CycleMember>> referencesMap //
  = new HashMap<IType, List<CycleMember>>();

  TypeAndMatches(IType root) {
    if (root == null) {
      throw new IllegalArgumentException("root may not be null"); //$NON-NLS-1$
    }
    this.root = root;
  }

  void addMatch(SearchMatch match) {
    IJavaElement matchElement = (IJavaElement) match.getElement();

    IType matchtedType = JavaElementUtil.getTypeFor(matchElement, false);
    if (matchtedType.equals(this.root)) {
      return; // ignore matches in same class
    }
    // A type has matches in other classes
    List<CycleMember> cycleElementList = getOrCreateCycleElementList(matchtedType);
    CycleMember cycleMember = getOrCreateCycleElement(matchElement, cycleElementList);
    // A match consists of one or more regions (=source ranges)
    int lineNr;
    try {
      lineNr = lineManger.getLine(matchtedType, match.getOffset());
    }
    catch (CoreException e) {
      Log.logError("Can't get line", e); //$NON-NLS-1$
      return;
    }
    int offset = match.getOffset();
    int length = match.getLength();
    String codeLine = lineManger.getPieceOfCode(matchElement, offset);
    List<CycleRegion> cycleRegions = cycleMember.getChildren();
    CycleRegion cycleRegion = new CycleRegion(cycleMember, offset, length, lineNr, codeLine);
    cycleRegions.add(cycleRegion);
  }

  private List<CycleMember> getOrCreateCycleElementList(IType matchtedType) {
    List<CycleMember> cycleElementList = referencesMap.get(matchtedType);
    if (cycleElementList == null) {
      cycleElementList = new ArrayList<CycleMember>();
      this.referencesMap.put(matchtedType, cycleElementList);
    }
    return cycleElementList;
  }

  private static CycleMember getOrCreateCycleElement(IJavaElement matchElement, List<CycleMember> cycleElementList) {
    CycleMember cycleMember = null;
    for (CycleMember searchCycleElement : cycleElementList) {
      if (searchCycleElement.getMatch().equals(matchElement)) {
        cycleMember = searchCycleElement;
        break;
      }
    }
    if (cycleMember == null) {
      cycleMember = new CycleMember(matchElement);
      cycleElementList.add(cycleMember);
    }
    return cycleMember;
  }

  CycleType createCycleClass(IType forMatchTarget) {
    List<CycleMember> cycleMembers = this.referencesMap.get(forMatchTarget);
    CycleType cycleType = new CycleType(root, cycleMembers);
    return cycleType;
  }

  IType getRoot() {
    return root;
  }

  Set<IType> getTypeSearchMatches() {
    return this.referencesMap.keySet();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(root.getElementName()).append(" (root)").append('\n'); //$NON-NLS-1$
    Set<Entry<IType, List<CycleMember>>> entrySet//
    = referencesMap.entrySet();
    for (Entry<IType, List<CycleMember>> entry : entrySet) {
      sb.append("  |-").append(entry.getKey().getElementName()); //$NON-NLS-1$
      sb.append(" (ref found)").append('\n'); //$NON-NLS-1$
      List<CycleMember> value = entry.getValue();
      for (CycleMember cycleMember : value) {
        sb.append("    |-").append(cycleMember).append('\n'); //$NON-NLS-1$
      }
    }
    return sb.toString();
  }
}
