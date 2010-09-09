/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.report;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 *
 */
public interface IUCDetectorReport {
  boolean reportMarker(ReportParam reportParam) throws CoreException;

  void reportDetectionProblem(IStatus status);

  void endReport() throws CoreException;
}
