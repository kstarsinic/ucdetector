/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.ucdetector.preferences.WarnLevel;

/**
 *
 */
public interface IUCDetctorReport {
  public static class ReportParam {
    public IJavaElement javaElement;
    public String message;
    public int line;
    public WarnLevel level;
    public String markerType;
    public String problem;

    public ReportParam(IJavaElement javaElement, String message, int line,
        WarnLevel level, String markerType, String problem) {
      this.javaElement = javaElement;
      this.message = message;
      this.line = line;
      this.level = level;
      this.markerType = markerType;
      this.problem = problem;
    }
  }

  void reportMarker(ReportParam reportParam) throws CoreException;

  void endReport(Object[] selected, long start) throws CoreException;
}
