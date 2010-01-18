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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
  private String resultMessage = ""; //$NON-NLS-1$
  private static final LineManger lineManger = new LineManger();

  @Override
  public void handleStartGlobal(IJavaElement[] javaElements) throws CoreException {
    if (javaElements == null) {
      return;
    }

    getMonitor().beginTask(getJobName(), IProgressMonitor.UNKNOWN);

    List<IMember> members = new ArrayList<IMember>();
    for (IJavaElement iJavaElement : javaElements) {
      MarkerFactory.deleteMarkers(iJavaElement.getJavaProject());
      if (iJavaElement instanceof IMember) {
        members.add((IMember) iJavaElement);
      }
    }
    IMember[] membersArray = members.toArray(new IMember[members.size()]);
    MethodWrapper[] calleeRoots = CallHierarchy.getDefault().getCalleeRoots(membersArray);
    for (MethodWrapper calleeRoot : calleeRoots) {
      iterateCalls(calleeRoot);
    }
  }

  private void iterateCalls(MethodWrapper startWrapper) {
    if (getMonitor().isCanceled()) {
      return;
    }
    MethodWrapper[] wrappers = startWrapper.getCalls(new NullProgressMonitor());
    for (MethodWrapper wrapper : wrappers) {
      IMember member = wrapper.getMember();
      if (member.isBinary()) {
        continue;
      }
      if (visitedMethods.contains(member)) {
        continue;
      }
      if (member instanceof IMethod) {
        IMethod method = (IMethod) member;
        getMonitor().worked(1);
        getMonitor().subTask(String.format("Level %s - %s",// //$NON-NLS-1$
            Integer.valueOf(wrapper.getLevel()), JavaElementUtil.getElementName(method)));
        getMonitor().setActiveSearchElement(method);
        visitedMethods.add(method);
        javaProjects.add(method.getJavaProject());
      }
      iterateCalls(wrapper);
    }
  }

  private List<IMethod> getAllMethodsOfProjects() throws CoreException {
    List<IMethod> result = new ArrayList<IMethod>();
    for (IJavaProject javaProject : javaProjects) {
      getMonitor().subTask("Collect all methods in: " + JavaElementUtil.getElementName(javaProject)); //$NON-NLS-1$
      MethodCollectIterator methodCollector = new MethodCollectIterator();
      methodCollector.setMonitor(getMonitor());
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
    getMonitor().subTask("Clear markers"); //$NON-NLS-1$
    for (IJavaProject javaProject : javaProjects) {
      MarkerFactory.deleteMarkers(javaProject);
    }
    getMonitor().subTask("Create markers: " + unusedMethods.size()); //$NON-NLS-1$
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
