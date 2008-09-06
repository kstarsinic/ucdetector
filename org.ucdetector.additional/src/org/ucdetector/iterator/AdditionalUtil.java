package org.ucdetector.iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMember;
import org.ucdetector.preferences.WarnLevel;
import org.ucdetector.search.LineManger;
import org.ucdetector.util.MarkerFactory;

/**
 * Helper for this plugin
 */
public class AdditionalUtil {
  private final MarkerFactory markerFactory = MarkerFactory.createInstance();

  private static final String ANALYZE_MARKER_EXAMPLE //
  = "org.ucdetector.analyzeMarkerExample"; //$NON-NLS-1$
  private static final String PROBLEM_EXAMPLE //
  = "PROBLEM_EXAMPLE";//$NON-NLS-1$
  private static final LineManger lineManger = new LineManger();

  void createMarker(IMember element, String message) throws CoreException {
    int line = lineManger.getLine(element);
    markerFactory.createMarker(element, message, line, WarnLevel.WARNING,
        AdditionalUtil.ANALYZE_MARKER_EXAMPLE, AdditionalUtil.PROBLEM_EXAMPLE);
  }
}
