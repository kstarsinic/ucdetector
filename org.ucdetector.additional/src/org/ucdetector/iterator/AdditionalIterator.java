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
    getMarkerFactory().createMarker(reportParam);
  }
}
