package org.ucdetector.iterator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.report.MarkerReport;
import org.ucdetector.search.LineManger;
import org.ucdetector.util.MarkerFactory;

/**
 *
 */
public class CheckUcdMarkerIterator extends AbstractUCDetectorIterator {
  private static final String ANALYZE_MARKER_CHECK_UCD_MARKERS //
  = "org.ucdetector.analyzeMarkerCheckUcdMarkers"; //$NON-NLS-1$
  //
  private int markerCount;
  private int badMarkerCount;
  //
  private static final Map<IResource, IMarker[]> fileMarkerMap//
  = new HashMap<IResource, IMarker[]>();

  //
  private static final MarkerReport markerReport = new MarkerReport();

  @Override
  protected void handleCompilationUnit(ICompilationUnit unit)
      throws CoreException {
    IResource resource = unit.getCorrespondingResource();
    IMarker[] markers = resource.findMarkers(MarkerFactory.ANALYZE_MARKER,
        true, IResource.DEPTH_ZERO);
    if (markers.length == 0) {
      return;
    }
    Set<Integer> markerLinesExpected = getIgnoreLines(unit);
    Set<Integer> makerLinesFound = new LinkedHashSet<Integer>();

    markerCount += markers.length;
    fileMarkerMap.put(resource, markers);
    System.out.println("resource=" + resource);
    //    System.out.println("markers=" + markers.length);
    for (IMarker marker : markers) {
      int lineNr = marker.getAttribute(IMarker.LINE_NUMBER, -1);
      makerLinesFound.add(Integer.valueOf(lineNr));
    }
    // -------------------------------------------------------------------------
    for (Integer markerLineExpected : markerLinesExpected) {
      if (!makerLinesFound.contains(markerLineExpected)) {
        badMarkerCount++;
        createMarker(resource, "Missing marker", markerLineExpected);
      }
    }

    for (Integer makerLineFound : makerLinesFound) {
      if (!markerLinesExpected.contains(makerLineFound)) {
        badMarkerCount++;
        createMarker(resource, "Additional marker", makerLineFound);
      }
    }
  }

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement)
      throws CoreException {
    if (javaElement.getResource() != null) {
      javaElement.getResource().deleteMarkers(ANALYZE_MARKER_CHECK_UCD_MARKERS,
          true, IResource.DEPTH_INFINITE);
    }
  }

  /**
   * Create a "report"
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Found ").append(markerCount).append(" markers. ");
    sb.append(badMarkerCount).append(" wrong markers");
    return sb.toString();
  }

  private static void createMarker(IResource resource, String message,
      Integer line) throws CoreException {
    IMarker marker = resource.createMarker(ANALYZE_MARKER_CHECK_UCD_MARKERS);
    marker.setAttribute(IMarker.MESSAGE, message);
    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
    marker.setAttribute(IMarker.LINE_NUMBER, line.intValue());
  }

  /**
   * Parse the java code
   */
  private Set<Integer> getIgnoreLines(ICompilationUnit unit)
      throws CoreException {
    IScanner scanner = ToolFactory.createScanner(true, false, false, true);
    char[] contents = unit.getBuffer().getCharacters();
    scanner.setSource(contents);
    Set<Integer> ignoreLines = new HashSet<Integer>();
    int nextToken;
    try {
      while ((nextToken = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
        Integer ignoreLine = LineManger.findTagInComment(scanner, "Marker YES",
            nextToken);
        if (ignoreLine != null) {
          ignoreLines.add(ignoreLine);
        }
      }
    }
    catch (InvalidInputException e) {
      IStatus status = new Status(IStatus.ERROR, UCDetectorPlugin.ID,
          IStatus.ERROR, e.getMessage(), e);
      throw new CoreException(status);
    }
    return ignoreLines;
  }

  @Override
  public String getJobName() {
    return "Check Ucd Marker Iterator";
  }
}
