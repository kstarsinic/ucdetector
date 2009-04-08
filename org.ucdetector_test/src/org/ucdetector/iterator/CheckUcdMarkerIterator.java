package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.MarkerFactory;

/**
 *
 */
public class CheckUcdMarkerIterator extends AbstractUCDetectorIterator {
  /** human readable String to use for marker tags  */
  private static final Map<String, String> markerMap //
  = new HashMap<String, String>();
  private static final String ANALYZE_MARKER_CHECK_UCD_MARKERS //
  = "org.ucdetector.analyzeMarkerCheckUcdMarkers"; //$NON-NLS-1$
  //
  private int markerCount;
  private int badMarkerCount;

  static {
    markerMap.put(MarkerFactory.UCD_MARKER_UNUSED, "unused code");
    markerMap.put(MarkerFactory.UCD_MARKER_USED_FEW, "few used code");
    markerMap.put(MarkerFactory.UCD_MARKER_USE_PRIVATE, "use private");
    markerMap.put(MarkerFactory.UCD_MARKER_USE_PROTECTED, "use protected");
    markerMap.put(MarkerFactory.UCD_MARKER_USE_DEFAULT, "use default");
    markerMap.put(MarkerFactory.UCD_MARKER_USE_FINAL, "use final");
    markerMap.put(MarkerFactory.UCD_MARKER_TEST_ONLY, "test only");
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void handleCompilationUnit(ICompilationUnit unit)
      throws CoreException {
    IResource resource = unit.getCorrespondingResource();
    IMarker[] markers = resource.findMarkers(MarkerFactory.UCD_MARKER, true,
        IResource.DEPTH_ZERO);

    Set<LineNrComments> lineNrsMARKER_YES = getLineNrMARKER_YES(unit);
    markerCount += markers.length;
    Set<Integer> makerLinesFound = new LinkedHashSet<Integer>();
    // Check, if each marker has a marker comment
    for (IMarker marker : markers) {
      Integer markerLineFound = Integer.valueOf(marker.getAttribute(
          IMarker.LINE_NUMBER, -1));
      makerLinesFound.add(markerLineFound);
      // Marker line number not found in "MARKER YES" lines --------------------
      LineNrComments commentForLine = null;
      for (LineNrComments lineNrMARKER_YES : lineNrsMARKER_YES) {
        if (markerLineFound.equals(lineNrMARKER_YES.lineNr)) {
          commentForLine = lineNrMARKER_YES;
        }
      }
      if (commentForLine == null) {
        createMarker(resource, "Additional marker", markerLineFound);
      }
      // -----------------------------------------------------------------------
      else {
        // "MARKER YES" lines do not contain marker ----------------------------
        Log.logDebug("Marker:" + new HashMap(marker.getAttributes()));
        String problemMarker = markerMap.get(marker.getType());
        List<String> problemsExpected = commentForLine.commentList;
        if (!problemsExpected.contains(problemMarker)) {
          createMarker(resource, "Wrong marker. Expected: '" + problemsExpected
              + "'. Found: '" + problemMarker + "'", markerLineFound);
        }
      }
      // -----------------------------------------------------------------------
    }
    // Check, if each marker comment has a marker
    for (LineNrComments lineNrMARKER_YES : lineNrsMARKER_YES) {
      if (!makerLinesFound.contains(lineNrMARKER_YES.lineNr)) {
        createMarker(resource, "Missing marker", lineNrMARKER_YES.lineNr);
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

  public int getBadMarkerCount() {
    return badMarkerCount;
  }

  private void createMarker(IResource resource, String message, Integer line)
      throws CoreException {
    IMarker marker = resource.createMarker(ANALYZE_MARKER_CHECK_UCD_MARKERS);
    marker.setAttribute(IMarker.MESSAGE, message);
    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
    marker.setAttribute(IMarker.LINE_NUMBER, line.intValue());
    badMarkerCount++;
  }

  /**
   * Parse the java code
   */
  private Set<LineNrComments> getLineNrMARKER_YES(ICompilationUnit unit)
      throws CoreException {
    IScanner scanner = ToolFactory.createScanner(true, false, false, true);
    scanner.setSource(unit.getBuffer().getCharacters());
    Set<LineNrComments> ignoreLines = new HashSet<LineNrComments>();
    int nextToken;
    try {
      while ((nextToken = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
        LineNrComments lineNrComment = findLineNrComments(scanner,
            "Marker YES: ", nextToken);
        if (lineNrComment != null) {
          ignoreLines.add(lineNrComment);
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

  /**
   * @return line number for a tag like "NO_UCD", or <code>null</code>
   * if there is no tag like "NO_UCD"
   */
  private static LineNrComments findLineNrComments(IScanner scanner,
      String tag, int nextToken) {
    if (nextToken == ITerminalSymbols.TokenNameCOMMENT_LINE) {
      char[] currentTokenSource = scanner.getCurrentTokenSource();
      String source = new String(currentTokenSource);
      int beginnIndex = source.indexOf(tag);
      if (beginnIndex != -1) {
        int start = scanner.getCurrentTokenStartPosition();
        int line = scanner.getLineNumber(start);
        String commentEnd = source.substring(beginnIndex + tag.length()).trim();
        return new LineNrComments(Integer.valueOf(line), commentEnd);
      }
    }
    return null;
  }

  private static final class LineNrComments {
    private final List<String> commentList = new ArrayList<String>();
    private final Integer lineNr;

    public LineNrComments(Integer lineNr, String comment) {
      this.lineNr = lineNr;
      String[] comments = comment.split(",");
      for (String com : comments) {
        commentList.add(com.trim());
      }
    }
  }

  @Override
  public String getJobName() {
    return "Check Ucd Marker Iterator";
  }
}