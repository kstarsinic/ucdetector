/**
 * Copyright (c) 2011 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.quickfix.MakeStaticQuickFix;
import org.ucdetector.search.UCDProgressMonitor;
import org.ucdetector.util.MarkerFactory;

/**
 * Fix warnings: The method x can be declared as static
 * <p>
 * {@link IProblem#MethodCanBeStatic}<br>
 * /org/eclipse/jdt/internal/compiler/problem/messages.properties<br>
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682<br>
 * AddStaticQualifierOperation<br>
 * 
 * ModifierCorrectionSubProcessor.addStaticMethodProposal()<br>
 */
public class MakeMethodsStaticIterator extends AdditionalIterator {
  /** {@link org.eclipse.jdt.core.compiler.IProblem} */
  private static final String PROBLEM_ID = "id";
  private final List<IMarker> markersToFix = new ArrayList<IMarker>();

  @Override
  protected void handleCompilationUnit(ICompilationUnit unit) throws CoreException {
    IResource resource = unit.getCorrespondingResource();
    IMarker[] markers = resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
    for (IMarker marker : markers) {
      System.out.println("   Maker:  " + MarkerFactory.dumpMarker(marker));
      int problemId = marker.getAttribute(PROBLEM_ID, -1);
      if (problemId == IProblem.MethodCanBeStatic) {
        markersToFix.add(marker);
      }
    }
  }

  private boolean doContinue = false;

  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        String message = "Do your really want to fix " + markersToFix.size() + " static warnings?";
        doContinue = MessageDialog.openQuestion(UCDetectorPlugin.getShell(), "Multi QuickFix", message);
      }
    });
    if (!doContinue) {
      markersToFix.clear();
      return;
    }
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        doQuickFix();
      }
    });
  }

  private void doQuickFix() {
    UCDProgressMonitor monitor = getMonitor();
    monitor.beginTask("Apply static QuickFix", markersToFix.size());
    for (IMarker marker : markersToFix) {
      monitor.internalWorked(1);
      new MakeStaticQuickFix(marker).run(marker);
      if (monitor.isCanceled()) {
        return;
      }
    }
  }

  @Override
  public String getJobName() {
    return "Fix warnings";
  }

  @Override
  public String getMessage() {
    return doContinue ? "Fixed warnings: " + markersToFix.size() : null;
  }
}