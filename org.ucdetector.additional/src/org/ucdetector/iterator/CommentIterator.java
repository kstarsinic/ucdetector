/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.List;

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

/**
 * Detect comments in java files:
 * <ul>
 *   <li>lineComments: <code>// line comment</code></li>
 *   <li>blockComments: <code>/* block comment</code></li>
 *   <li>blockComments: <code>/** javadoc comment</code></li>
 * </ul>
 */
@SuppressWarnings("nls")
public class CommentIterator extends AdditionalIterator {
  private final List<String> lineComments = new ArrayList<String>();
  private final List<String> blockComments = new ArrayList<String>();
  private final List<String> javadocComments = new ArrayList<String>();
  private final List<String> todoComments = new ArrayList<String>();

  @Override
  protected void handleCompilationUnit(ICompilationUnit unit) throws CoreException {
    IScanner scanner = ToolFactory.createScanner(true, false, false, true);
    // old: char[] contents = org.eclipse.jdt.internal.core.CompilationUnit.getContents();
    char[] contents = unit.getBuffer().getCharacters();
    scanner.setSource(contents);
    int nextToken;
    try {
      while ((nextToken = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
        String comment = null;
        if (nextToken == ITerminalSymbols.TokenNameCOMMENT_LINE) {
          comment = String.valueOf(scanner.getCurrentTokenSource());
          lineComments.add(comment);
        }
        else if (nextToken == ITerminalSymbols.TokenNameCOMMENT_BLOCK) {
          comment = String.valueOf(scanner.getCurrentTokenSource());
          blockComments.add(comment);
        }
        else if (nextToken == ITerminalSymbols.TokenNameCOMMENT_JAVADOC) {
          comment = String.valueOf(scanner.getCurrentTokenSource());
          javadocComments.add(comment);
        }
        if (comment != null && comment.indexOf("TODO") != -1) {
          todoComments.add(comment);
        }
      }
    }
    catch (InvalidInputException e) {
      IStatus status = new Status(IStatus.ERROR, UCDetectorPlugin.ID, IStatus.ERROR, e.getMessage(), e);
      throw new CoreException(status);
    }
  }

  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    System.out.println("------------------ todo comments");
    for (String todoComment : todoComments) {
      System.out.println(todoComment);
    }
    //    System.out.print(todoComments);
    //    System.out.println("------------------ lineComments");
    //    System.out.print(lineComments);
    //    System.out.println("------------------ blockComments");
    //    for (String blockComment : blockComments) {
    //      System.out.println(blockComment);
    //    }
    //    System.out.println("------------------ javadocComments");
    //    for (String javadocComment : javadocComments) {
    //       System.out.println(javadocComment);
    //    }
  }

  /**
   * do a simple "report"
   */
  @Override
  public String getMessage() {
    return "line comments:\t" + lineComments.size() //
        + ",\nblock comments:\t" + blockComments.size() //
        + "\njavadoc comments:\t" + javadocComments.size()//
        + "\ntodo comments:\t" + todoComments.size()//
    ;
  }

  @Override
  public String getJobName() {
    return "Detect java comments job";
  }
}
