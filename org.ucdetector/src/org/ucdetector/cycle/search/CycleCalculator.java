/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.IType;
import org.eclipse.osgi.util.NLS;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.cycle.model.Cycle;
import org.ucdetector.cycle.model.CycleType;
import org.ucdetector.cycle.model.SearchResult;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.search.UCDProgressMonitor;

/**
 * Calculate Cycles based on typeAndRefs List
 */
class CycleCalculator {
  private final List<TypeAndMatches> typeAndMatchesList;
  private final SearchResult searchResult;
  private final UCDProgressMonitor monitor;

  CycleCalculator(SearchResult searchResult,
      List<TypeAndMatches> typeAndRefsList, UCDProgressMonitor monitor) {
    this.searchResult = searchResult;
    this.typeAndMatchesList = typeAndRefsList;
    this.monitor = monitor;
  }

  /**
   * calculate the cycles from the search result
   */
  List<Cycle> calculate() {
    List<Cycle> allCycles = getAllCycles();
    List<Cycle> cycleList = removeDoubleCycles(allCycles);
    return cycleList;
  }

  /**
   * get all cycles, including double cycles
   */
  private List<Cycle> getAllCycles() {
    List<Cycle> allCycles = new ArrayList<Cycle>();
    int count = 0;
    int prevSize = 0;
    for (TypeAndMatches typeAndMatches : typeAndMatchesList) {
      Object[] bindings = new Object[] {//
      Integer.valueOf(count++), //
          Integer.valueOf(typeAndMatchesList.size()), //
          typeAndMatches.getRoot().getElementName() };
      String mes = NLS.bind(Messages.CycleCalculator_Monitor, bindings);
      monitor.subTask(mes);
      monitor.worked(1);
      Stack<TypeAndMatches> path = new Stack<TypeAndMatches>();
      IType startType = typeAndMatches.getRoot();
      searchCycles(startType, path, allCycles);
      if (UCDetectorPlugin.DEBUG) {
        int found = allCycles.size() - prevSize;
        Log.logDebug(found
            + " cycles found for " //$NON-NLS-1$
            + typeAndMatches.getRoot().getElementName()
            + " (including double cycles)"); //$NON-NLS-1$
      }
      prevSize = allCycles.size();
    }
    return allCycles;
  }

  /**
   * search the TypeAndMatches for the given type
   */
  private TypeAndMatches getTypeAndMatchesFor(IType startType) {
    for (TypeAndMatches typeAndMatches : typeAndMatchesList) {
      if (startType.equals(typeAndMatches.getRoot())) {
        return typeAndMatches;
      }
    }
    return null;
  }

  // -------------------------------------------------------------------------
  // HELPER
  // -------------------------------------------------------------------------

  private List<Cycle> removeDoubleCycles(List<Cycle> cyclesFound) {
    monitor.subTask(Messages.CycleCalculator_removeDoubleCycles);
    monitor.worked(1);
    List<Cycle> result = new ArrayList<Cycle>();
    if (cyclesFound.isEmpty()) {
      return result;
    }
    // small cycles first
    Collections.sort(cyclesFound, new Comparator<Cycle>() {
      // TODO UCD marker here!
      public int compare(Cycle o1, Cycle o2) {
        return o1.getChildrenSize() - o2.getChildrenSize();
      }
    });
    result.add(cyclesFound.get(0));
    for (Cycle cycleToAdd : cyclesFound) {
      boolean isContained = false;
      for (int i = 0; i < result.size(); i++) {
        Cycle cycleAlreadyAdded = result.get(i);
        if (cycleToAdd.contains(cycleAlreadyAdded)) {
          // because big cycles could be build by small cycles
          isContained = true;
          break;
        }
      }
      if (!isContained) {
        result.add(cycleToAdd);
      }
    }
    if (UCDetectorPlugin.DEBUG) {
      int removed = cyclesFound.size() - result.size();
      Log.logDebug("Removed double cycle: " + removed); //$NON-NLS-1$
    }
    return result;
  }

  /**
   * This method is called recursively to search cycles
   * @param startType we iterate all references of this type
   * @param path actual search Path: A-B-C
   * @param result contains all found cycles, new cycles are added
   */
  private void searchCycles(IType startType, Stack<TypeAndMatches> path,
      List<Cycle> result) {
    TypeAndMatches typeAndMatches = getTypeAndMatchesFor(startType);
    if (typeAndMatches == null) {
      // there is no information about the class
      return;
    }
    if (monitor.isCanceled()) {
      return;
    }
    path.push(typeAndMatches);
    Set<IType> references = typeAndMatches.getTypeSearchMatches();
    for (IType reference : references) {
      if (path.size() > Prefs.getCycleDepth()) {
        continue; // stop recursion
      }
      TypeAndMatches first = path.firstElement();
      if (first.getRoot().equals(reference)) {
        // We are back to the first element. cycle found!
        LinkedList<CycleType> cycleTypeList = createCycleList(path);
        Cycle cycle = new Cycle(searchResult, cycleTypeList);
        result.add(cycle);
        continue;// stop recursion
      }
      searchCycles(reference, path, result);
    }
    path.pop();
  }

  private static LinkedList<CycleType> createCycleList(
      List<TypeAndMatches> matches) {
    LinkedList<CycleType> result = new LinkedList<CycleType>();
    for (int i = 0; i < matches.size(); i++) {
      int next = (i + 1) % matches.size();
      IType matchTarget = matches.get(next).getRoot();
      CycleType cycleClass = matches.get(i).createCycleClass(matchTarget);
      result.add(cycleClass);
    }
    return result;
  }
}
