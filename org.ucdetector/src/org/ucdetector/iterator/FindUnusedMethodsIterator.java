/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.ucdetector.Log;
import org.ucdetector.preferences.WarnLevel;
import org.ucdetector.report.ReportParam;
import org.ucdetector.search.LineManger;
import org.ucdetector.search.UCDProgressMonitor;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;

/**
 * CallHierarchyViewPart
 * CallHierarchy
 * CalleeMethodWrapper.findChildren()
 */
public class FindUnusedMethodsIterator extends AbstractUCDetectorIterator {
  private final Set<IMethod> visitedMethods = new LinkedHashSet<IMethod>();
  private final Set<IJavaProject> javaProjects = new LinkedHashSet<IJavaProject>();
  private final UCDProgressMonitor monitor = new UCDProgressMonitor();
  private String resultMessage = ""; //$NON-NLS-1$
  private static final LineManger lineManger = new LineManger();

  @Override
  public void handleStartGlobal(IJavaElement[] javaElements) throws CoreException {
    if (javaElements == null) {
      return;
    }
    List<IMember> members = new ArrayList<IMember>();
    for (IJavaElement iJavaElement : javaElements) {
      MarkerFactory.deleteMarkers(iJavaElement.getJavaProject());
      if (iJavaElement instanceof IMember) {
        members.add((IMember) iJavaElement);
        javaProjects.add(iJavaElement.getJavaProject());
      }
    }
    IMember[] membersArray = members.toArray(new IMember[members.size()]);
    MethodWrapper[] calleeRoots = CallHierarchy.getDefault().getCalleeRoots(membersArray);
    for (MethodWrapper calleeRoot : calleeRoots) {
      iterateCalls(calleeRoot);
    }
  }

  private void iterateCalls(MethodWrapper startWrapper) {
    MethodWrapper[] wrappers = startWrapper.getCalls(monitor);
    for (MethodWrapper wrapper : wrappers) {
      IMember member = wrapper.getMember();
      if (member.isBinary()) {
        continue;
      }
      if (visitedMethods.contains(member)) {
        continue;
      }
      if (member instanceof IMethod) {
        visitedMethods.add((IMethod) member);
        // System.err.println("### " + JavaElementUtil.getElementNameAndClassName(member) + " -> " + wrapper.getCalls(monitor).length);
      }
      iterateCalls(wrapper);
    }
  }

  private List<IMethod> getAllMethodsOfProjects() throws CoreException {
    List<IMethod> result = new ArrayList<IMethod>();
    for (IJavaProject javaProject : javaProjects) {
      MethodCollectIterator methodCollector = new MethodCollectIterator();
      methodCollector.setMonitor(new UCDProgressMonitor());
      methodCollector.iterate(javaProject);
      result.addAll(methodCollector.getAllmethods());
    }
    return result;
  }

  private static class MethodCollectIterator extends AbstractUCDetectorIterator {
    private final List<IMethod> methods = new ArrayList<IMethod>();

    public List<IMethod> getAllmethods() {
      return methods;
    }

    @Override
    protected void handleMethod(IMethod method) throws CoreException {
      methods.add(method);
    }

    @Override
    public String getJobName() {
      return "Collect all methods"; //$NON-NLS-1$
    }
  }

  @Override
  public void handleEndGlobal(IJavaElement[] javaElements) throws CoreException {
    List<IMethod> allMethods = getAllMethodsOfProjects();
    List<IMethod> unusedMethods = new ArrayList<IMethod>(allMethods);
    unusedMethods.removeAll(visitedMethods);
    if (Log.DEBUG) {
      Log.logDebug("VisitedMethods:\n" + JavaElementUtil.getElementNames(visitedMethods)); //$NON-NLS-1$
      Log.logDebug("UnusedMethods:\n" + JavaElementUtil.getElementNames(unusedMethods)); //$NON-NLS-1$
    }
    StringBuilder sb = new StringBuilder();
    sb.append("Methods all   : " + allMethods.size()).append(NL); //$NON-NLS-1$
    sb.append("Methods visited: " + visitedMethods.size()).append(NL); //$NON-NLS-1$
    sb.append("Methods unused : " + unusedMethods.size()).append(NL); //$NON-NLS-1$
    resultMessage = sb.toString();
    Log.logInfo(resultMessage);
    for (IMethod unusedMethod : unusedMethods) {
      createMarker(unusedMethod, "Method not called", MarkerFactory.UCD_MARKER_UNUSED); //$NON-NLS-1$
    }
  }

  private void createMarker(IMember element, String message, String markerType) throws CoreException {
    int line = lineManger.getLine(element);
    if (line != -1) {
      ReportParam reportParam = new ReportParam(element, message, line, markerType, WarnLevel.WARNING);
      getMarkerFactory().reportMarker(reportParam);
    }
  }

  @Override
  public String toString() {
    return "Finished parsing methods:\n" + resultMessage; //$NON-NLS-1$
  }

  @Override
  public String getJobName() {
    return "Find unused methods"; //$NON-NLS-1$
  }
}
