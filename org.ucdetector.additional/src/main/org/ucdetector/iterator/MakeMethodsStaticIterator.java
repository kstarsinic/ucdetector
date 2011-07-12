/**
 * Copyright (c) 2011 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.compiler.IProblem;
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
  private int fixedMarkerCount = 0;

  @Override
  protected void handleCompilationUnit(ICompilationUnit unit) throws CoreException {
    IResource resource = unit.getCorrespondingResource();
    IMarker[] markers = resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
    for (IMarker marker : markers) {
      int problemId = marker.getAttribute(PROBLEM_ID, -1);
      if (problemId == IProblem.MethodCanBeStatic) {
        fixedMarkerCount++;
        System.out.println("   Fix Maker:  " + MarkerFactory.dumpMarker(marker));
        // new MakeStaticQuickFix(marker).run(marker);
      }
    }
  }

  @Override
  public String getJobName() {
    return "Fix warnings";
  }

  @Override
  public String getMessage() {
    return "Fixed warnings: " + fixedMarkerCount;
  }
}