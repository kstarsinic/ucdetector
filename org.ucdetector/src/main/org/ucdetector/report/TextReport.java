/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;

/**
 * TODO: Describe class
 * <p>
 * @author Joerg Spieler
 * @since 31.03.2011
 */
@SuppressWarnings("nls")
public class TextReport implements IUCDetectorReport {

  public void startReport(IJavaElement[] objectsToIterate, long startTime) throws CoreException {
    //
  }

  public boolean reportMarker(ReportParam reportParam) throws CoreException {
    System.out.println("TextReport.reportMarker: " + reportParam);
    return false;
  }

  public void reportDetectionProblem(IStatus status) {
    System.out.println("TextReport.reportDetectionProblem: " + status);
    //
  }

  public void endReport() throws CoreException {
    System.out.println("TextReport.endReport");
    //
  }
}
