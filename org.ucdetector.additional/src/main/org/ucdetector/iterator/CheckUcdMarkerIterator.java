package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringFileBuffers;
import org.eclipse.jface.text.IDocument;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;

/**
 * To 'test' UCDetector
 * <ul>
 * <li>Run UCDetector in directory /org.ucdetector.example/example</li>
 * <li>Run this class (pop up menu 'Find bad Markers') in directory /org.ucdetector.example/example</li>
 * <li>Markers will be created, if the expected markers are different from the created markers</li>
 * </ul>
 * The example code classes contain comments, to indicate which markers must be created.
 * For Example:
 * <pre>
 * public static int USE_FINAL = 1; // <b>Marker YES: use final,unused code</b>
 * </pre>
 */
public class CheckUcdMarkerIterator extends AbstractUCDetectorIterator {
  /** human readable String to use for marker tags  */
  private static final Map<String, String> markerMap;
  private static final String CHECK_UCD_MARKERS = MarkerFactory.UCD_MARKER + "CheckUcdMarkers";
  private int markersCount;
  private int badMarkerCount;

  static {
    Map<String, String> map = new HashMap<String, String>();
    map.put(MarkerFactory.UCD_MARKER_UNUSED, "unused code");
    map.put(MarkerFactory.UCD_MARKER_USED_FEW, "few used code");
    map.put(MarkerFactory.UCD_MARKER_USE_PRIVATE, "use private");
    map.put(MarkerFactory.UCD_MARKER_USE_PROTECTED, "use protected");
    map.put(MarkerFactory.UCD_MARKER_USE_DEFAULT, "use default");
    map.put(MarkerFactory.UCD_MARKER_USE_FINAL, "use final");
    map.put(MarkerFactory.UCD_MARKER_TEST_ONLY, "test only");
    markerMap = Collections.unmodifiableMap(map);
  }

  @Override
  protected void handleCompilationUnit(ICompilationUnit unit) throws CoreException {
    try {
      IDocument doc = RefactoringFileBuffers.acquire(unit).getDocument();
      IResource resource = unit.getCorrespondingResource();
      IMarker[] markers = resource.findMarkers(MarkerFactory.UCD_MARKER, true, IResource.DEPTH_ZERO);
      markersCount += markers.length;
      if (markers.length > 0 && Log.isDebug()) {
        Log.debug("%2s markers found for %s", "" + markers.length, JavaElementUtil.getElementName(unit));
      }
      Set<LineNrComments> lineNumbersMarkersExpected = getLineNumbersMarkersExpected(unit);
      Set<Integer> makerLinesFound = new LinkedHashSet<Integer>();
      // Check, if each marker has a marker comment
      for (IMarker marker : markers) {
        // 2010-01-04: Changed from IMarker.LINE_NUMBER to IMarker.CHAR_START
        // We must get a IDocument now to find the line number from IMarker.CHAR_START
        int offset = marker.getAttribute(IMarker.CHAR_START, -1);
        Integer markerLineFound = Integer.valueOf(doc.getLineOfOffset(offset) + 1);
        makerLinesFound.add(markerLineFound);
        // Marker line number not found in "MARKER YES" lines --------------------
        LineNrComments commentForLine = null;
        for (LineNrComments lineNrComments : lineNumbersMarkersExpected) {
          if (markerLineFound.equals(lineNrComments.lineNr)) {
            commentForLine = lineNrComments;
          }
        }
        if (commentForLine == null) {
          createMarker(marker, resource, "Additional marker", markerLineFound);
        }
        else {
          String problemMarker = markerMap.get(marker.getType());
          List<String> problemsExpected = commentForLine.commentList;
          if (!problemsExpected.contains(problemMarker)) {
            String mes = String.format("Wrong marker. Expected: '%s'. Found: '%s'", problemsExpected, problemMarker);
            createMarker(marker, resource, mes, markerLineFound);
          }
        }
      }
      // Check, if each marker comment has a marker
      for (LineNrComments lineNrComments : lineNumbersMarkersExpected) {
        if (!makerLinesFound.contains(lineNrComments.lineNr)) {
          createMarker(null, resource, "Missing marker", lineNrComments.lineNr);
        }
      }
    }
    catch (Exception e) {
      IStatus status = new Status(IStatus.ERROR, UCDetectorPlugin.ID, IStatus.ERROR, e.getMessage(), e);
      throw new CoreException(status);
    }
    finally {
      RefactoringFileBuffers.release(unit);
    }
  }

  @Override
  public void handleStartSelectedElement(IJavaElement javaElement) throws CoreException {
    if (javaElement.getResource() != null) {
      javaElement.getResource().deleteMarkers(CHECK_UCD_MARKERS, true, IResource.DEPTH_INFINITE);
    }
  }

  @Override
  public void handleEndGlobal(IJavaElement[] javaElements) throws CoreException {
    Log.info("End CheckUcdMarkerIterator: " + toString());
  }

  /**
   * Create a "report"
   */
  @Override
  public String toString() {
    return String.format("Found %s markers. %s  wrong markers", //
        Integer.valueOf(markersCount), Integer.valueOf(badMarkerCount));
  }

  public int getBadMarkerCount() {
    return badMarkerCount;
  }

  private void createMarker(IMarker markerFound, IResource resource, String message, Integer line) throws CoreException {
    IMarker marker = resource.createMarker(CHECK_UCD_MARKERS);
    marker.setAttribute(IMarker.MESSAGE, message);
    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
    marker.setAttribute(IMarker.LINE_NUMBER, line.intValue());
    marker.setAttribute(IMarker.LOCATION, resource.getFullPath().toString());
    if (markerFound != null) {
      // Copy attributes
      marker.setAttribute(MarkerFactory.JAVA_NAME, markerFound.getAttribute(MarkerFactory.JAVA_NAME));
      marker.setAttribute(MarkerFactory.JAVA_TYPE, markerFound.getAttribute(MarkerFactory.JAVA_TYPE));
      //      marker.setAttribute(IMarker.CHAR_START, markerFound.getAttribute(IMarker.CHAR_START));
      //      marker.setAttribute(IMarker.CHAR_END, markerFound.getAttribute(IMarker.CHAR_END));
    }
    Log.warn("############ BAD MARKER #################", MarkerFactory.dumpMarker(marker));
    for (Entry<String, Object> entry : MarkerFactory.markerAsMap(marker).entrySet()) {
      Log.warn("# %10s = %s", entry.getKey(), entry.getValue());
    }
    Log.warn("##########################################", MarkerFactory.dumpMarker(marker));
    badMarkerCount++;
  }

  /**
   * Parse the java code
   */
  private static Set<LineNrComments> getLineNumbersMarkersExpected(ICompilationUnit unit) throws CoreException,
      InvalidInputException {
    IScanner scanner = ToolFactory.createScanner(true, false, false, true);
    scanner.setSource(unit.getBuffer().getCharacters());
    Set<LineNrComments> ignoreLines = new HashSet<LineNrComments>();
    int nextToken;
    while ((nextToken = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
      LineNrComments lineNrComment = findLineNrComments(scanner, "Marker YES: ", nextToken);
      if (lineNrComment != null) {
        ignoreLines.add(lineNrComment);
      }
    }
    return ignoreLines;
  }

  /**
   * @return line number for a tag like "NO_UCD", or <code>null</code>
   * if there is no tag like "NO_UCD"
   */
  private static LineNrComments findLineNrComments(IScanner scanner, String tag, int nextToken) {
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

  /**
   * line number and the comments for this line number.
   * <pre>
   *   // Marker YES: use final,unused code
   * </pre>
   * Will be converted to: 20 (line number) and 'use final,unused code'
   */
  private static final class LineNrComments {
    private final List<String> commentList = new ArrayList<String>();
    private final Integer lineNr;

    private LineNrComments(Integer lineNr, String comment) {
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