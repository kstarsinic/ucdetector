/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.JavaElementUtil;

/**
 * Get the source code line of classes, methods, fields
 */
public class LineManger {
  /**
   * Indicates, that a line could not be found in a source code file
   */
  public static final int LINE_NOT_FOUND = -1;
  /**
   * If this String is found in an comment, the source code line will be
   * ignored by UCDetector
   */
  private static final String NO_UCD_TAG = "NO_UCD"; //$NON-NLS-1$
  /**
   * parsed java files
   */
  private final Map<ICompilationUnit, ScannerTimestamp> scannerMap //
  = new HashMap<ICompilationUnit, ScannerTimestamp>();
  /**
   * lines containing "NO_UCD
   */
  private final Map<IScanner, Set<Integer>> ignoreLineMap //
  = new HashMap<IScanner, Set<Integer>>();

  /**
   * position in file of each end of line
   */
  private final Map<ICompilationUnit, int[]> lineEndsMap //
  = new HashMap<ICompilationUnit, int[]>();

  /**
   * parsed java files
   */
  private final Map<ICompilationUnit, char[]> contentsMap //
  = new HashMap<ICompilationUnit, char[]>();

  /**
   * @return the line number of a class, method, field in a file, or -1 if the
   *         line could not be found
   * @throws CoreException
   */
  public int getLine(IMember element) throws CoreException {
    ISourceRange sourceRange = element.getNameRange();
    int offset = sourceRange.getOffset();
    int lineNbr = getLine(element, offset);
    return lineNbr;
  }

  /**
   * @return The source code line from the offset (position in file)
   */
  public int getLine(IMember element, int offset) throws CoreException {
    IScanner scanner = createScanner(element);
    if (scanner == null) {
      return LINE_NOT_FOUND;
    }
    int lineNbr = scanner.getLineNumber(offset);
    Set<Integer> ignoreLines = ignoreLineMap.get(scanner);
    if (ignoreLines.contains(Integer.valueOf(lineNbr))) {
      return LINE_NOT_FOUND;
    }
    return lineNbr;
  }

  /**
   * Parse the java code
   */
  private IScanner createScanner(IJavaElement javaElement) throws CoreException {
    IOpenable openable = javaElement.getOpenable();
    if (!(openable instanceof ICompilationUnit)) {
      Log.logError("openable NOT instanceof ICompilationUnit '" + //$NON-NLS-1$
          JavaElementUtil.getElementName(javaElement)
          + "' " + javaElement.getClass().getName()); //$NON-NLS-1$
      return null;
    }
    ICompilationUnit compilationUnit = (ICompilationUnit) openable;
    // Update scanner, if file changed!
    long timeStamp = javaElement.getResource().getLocalTimeStamp();
    ScannerTimestamp scannerTimestamp = scannerMap.get(compilationUnit);
    if (scannerTimestamp != null) {
      if (timeStamp > scannerTimestamp.timeStamp) {
        scannerMap.remove(compilationUnit);
      }
      else {
        return scannerMap.get(compilationUnit).scanner;
      }
    }
    IScanner scanner = ToolFactory.createScanner(true, false, false, true);
    // old: char[] contents = org.eclipse.jdt.internal.core.CompilationUnit.getContents();
    char[] contents = compilationUnit.getBuffer().getCharacters();
    contentsMap.put(compilationUnit, contents);
    scanner.setSource(contents);
    Set<Integer> ignoreLines = new HashSet<Integer>();
    ignoreLineMap.put(scanner, ignoreLines);
    int nextToken;
    try {
      while ((nextToken = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
        Integer ignoreLine = findTagInComment(scanner, NO_UCD_TAG, nextToken);
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
    scannerMap.put(compilationUnit, new ScannerTimestamp(scanner, timeStamp));
    lineEndsMap.put(compilationUnit, scanner.getLineEnds());
    return scanner;
  }

  /**
   * @return Piece of code contained in offset
   */
  public String getPieceOfCode(IJavaElement element, int offset) {
    IType type = JavaElementUtil.getTypeFor(element);
    ICompilationUnit unit = type.getCompilationUnit();
    int[] lineEnds = lineEndsMap.get(unit);
    char[] chars = contentsMap.get(unit);
    if (lineEnds != null && chars != null) {
      for (int i = 0; i < lineEnds.length - 1; i++) {
        int start = lineEnds[i];
        int end = lineEnds[i + 1];
        if (start < offset && offset < end) {
          try {
            return String.valueOf(chars, start, end - start).trim();
          }
          catch (Exception e) {
            Log.logError("Can't get line", e); //$NON-NLS-1$
          }
        }
      }
    }
    return ""; //$NON-NLS-1$
  }

  /**
   * @return line number for a tag like "NO_UCD", or <code>null</code>
   * if there is no tag like "NO_UCD"
   */
  private static Integer findTagInComment(IScanner scanner, String tag,
      int nextToken) {
    if (nextToken == ITerminalSymbols.TokenNameCOMMENT_LINE) {
      char[] currentTokenSource = scanner.getCurrentTokenSource();
      String source = new String(currentTokenSource);
      if (source.contains(tag)) {
        int start = scanner.getCurrentTokenStartPosition();
        int line = scanner.getLineNumber(start);
        return Integer.valueOf(line);
      }
    }
    return null;
  }

  /**
   * This class holds a scanner and its timestamp.
   * Update scanner, if file has changed!
   */
  private static final class ScannerTimestamp {
    private final long timeStamp;
    private final IScanner scanner;

    ScannerTimestamp(IScanner scanner, long timeStamp) {
      this.scanner = scanner;
      this.timeStamp = timeStamp;
    }
  }
}
