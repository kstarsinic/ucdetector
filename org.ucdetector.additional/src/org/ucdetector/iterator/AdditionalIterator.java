/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMember;
import org.ucdetector.preferences.WarnLevel;
import org.ucdetector.report.ReportParam;
import org.ucdetector.search.LineManger;

/**
 *
 */
public abstract class AdditionalIterator extends AbstractUCDetectorIterator {
  private static final String ANALYZE_MARKER_EXAMPLE //
  = "org.ucdetector.analyzeMarkerExample"; //$NON-NLS-1$
  private static final LineManger lineManger = new LineManger();

  void createMarker(IMember element, String message) throws CoreException {
    int line = lineManger.getLine(element);
    ReportParam reportParam = new ReportParam(element, message, line,
        ANALYZE_MARKER_EXAMPLE, WarnLevel.WARNING);
    getMarkerFactory().reportMarker(reportParam);
  }
  
  public abstract String getMessage();
}
