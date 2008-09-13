/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.osgi.util.NLS;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.cycle.model.Cycle;
import org.ucdetector.cycle.model.SearchResult;
import org.ucdetector.cycle.model.SearchResultRoot;
import org.ucdetector.search.SearchManager;
import org.ucdetector.util.JavaElementUtil;

/**
 * Use eclipse search to find references of classes
 */
public class CycleSearchManager {
  private final IProgressMonitor monitor;
  /**
   * We search cycles for each project, because there should be no
   * cycles between project A and project B!
   */
  private final Map<IJavaProject, List<IType>> typesMap = new HashMap<IJavaProject, List<IType>>();
  private final IJavaElement[] selections;

  public CycleSearchManager(IProgressMonitor monitor, List<IType> types,
      IJavaElement[] selections) {
    this.monitor = monitor;
    this.selections = selections;
    // Group types by project
    for (IType type : types) {
      IJavaProject javaProject = type.getJavaProject();
      List<IType> typesList = typesMap.get(javaProject);
      if (typesList == null) {
        typesList = new ArrayList<IType>();
        typesMap.put(javaProject, typesList);
      }
      typesList.add(type);
    }
  }

  public void search() throws CoreException {
    SearchResultRoot root = SearchResultRoot.getInstance();
    monitor.subTask("Search cycles"); //$NON-NLS-1$
    monitor.worked(1);
    //
    Set<Entry<IJavaProject, List<IType>>> entrySet = typesMap.entrySet();
    int projectNr = 1;
    for (Entry<IJavaProject, List<IType>> entry : entrySet) {
      IJavaProject javaProject = entry.getKey();
      List<IType> types = entry.getValue();
      List<TypeAndMatches> typeAndRefsList = searchAllTypes(types, projectNr);
      SearchResult searchResult = new SearchResult(root, selections,
          javaProject);
      CycleCalculator cycleCalculator = new CycleCalculator(searchResult,
          typeAndRefsList, monitor);
      monitor.subTask(Messages.CycleSearchManager_Project_Info
          + javaProject.getElementName());
      monitor.worked(1);
      List<Cycle> cycleList = cycleCalculator.calculate();
      //
      searchResult.setCycles(cycleList);
      if (UCDetectorPlugin.DEBUG) {
        Log.logDebug("Found cycles:\r\n" + searchResult);
      }
      root.getChildren().add(searchResult);
      projectNr++;
    }
  }

  private List<TypeAndMatches> searchAllTypes(List<IType> types, int projectNr)
      throws CoreException {
    List<TypeAndMatches> result = new ArrayList<TypeAndMatches>();
    int search = 0;
    for (IType type : types) {
      if (monitor.isCanceled()) {
        break;
      }
      search++;
      String mes = getMonitorMessage(types, projectNr, search, type);
      monitor.subTask(mes);
      monitor.worked(1);
      TypeAndMatches typeAndMatches = new TypeAndMatches(type);
      SearchPattern pattern = SearchPattern.createPattern(type,
          IJavaSearchConstants.REFERENCES, SearchPattern.R_EXACT_MATCH);
      // IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
      IJavaProject javaProject = type.getJavaProject();
      IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
          new IJavaProject[] { javaProject }, false);
      ReferenceSearchRequestor requestor = new ReferenceSearchRequestor(
          typeAndMatches);
      JavaElementUtil.runSearch(pattern, requestor, scope);
      result.add(typeAndMatches);
      if (UCDetectorPlugin.DEBUG) {
        int found = typeAndMatches.getTypeSearchMatches().size();
        Log.logDebug("found " + found + " references for "
            + type.getElementName());
      }
    }
    return result;
  }

  private String getMonitorMessage(List<IType> types, int projectNr,
      int search, IType type) {
    List<Object> bindingList = new ArrayList<Object>();
    if (typesMap.size() > 1) {
      bindingList.add(Integer.valueOf(projectNr));
      bindingList.add(Integer.valueOf(typesMap.size()));
    }
    bindingList.add(Integer.valueOf(search));
    bindingList.add(Integer.valueOf(types.size()));
    bindingList.add(type.getElementName());
    Object[] bindings = bindingList.toArray();
    String message = typesMap.size() > 1 ? //
    NLS.bind(Messages.CycleSearchManager_MonitorProject, bindings)
        : NLS.bind(Messages.CycleSearchManager_Monitor, bindings);
    if (UCDetectorPlugin.DEBUG) {
      Log.logDebug(message);
    }
    return message;
  }

  /**
   * Collect search matches
   */
  private static final class ReferenceSearchRequestor extends SearchRequestor {
    private final TypeAndMatches typeAndMatches;

    ReferenceSearchRequestor(TypeAndMatches typeAndMatches) {
      this.typeAndMatches = typeAndMatches;
    }

    @Override
    public void acceptSearchMatch(SearchMatch match) {
      IJavaElement javaElement = SearchManager.defaultIgnoreMatch(match);
      if (javaElement == null) {
        return;
      }
      this.typeAndMatches.addMatch(match);
    }
  }
}