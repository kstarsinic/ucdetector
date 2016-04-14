/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.util.ASTMemberVisitor;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.UsedBy;

/**
 * Get the source code line of classes, methods, fields
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
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
  private static final String NO_UCD_COMMENT = "NO_UCD"; //$NON-NLS-1$
  /**
   * If this String is found like @SuppressWarnings("ucd"),
   * the source code line will be ignored by UCDetector
   */
  private static final String UCD_ANNOTATION_VALUE = "ucd"; //$NON-NLS-1$
  /**
   * parsed java files
   */
  private final Map<ICompilationUnit, ScannerTimestamp> scannerMap = new HashMap<ICompilationUnit, ScannerTimestamp>();
  /**
   * lines containing "NO_UCD
   */
  private final Map<IScanner, Set<Integer>> ignoreLineMap = new HashMap<IScanner, Set<Integer>>();

  /**
   * position in file of each end of line
   */
  private final Map<ICompilationUnit, int[]> lineEndsMap = new HashMap<ICompilationUnit, int[]>();

  /**
   * parsed java files
   */
  private final Map<ICompilationUnit, char[]> contentsMap = new HashMap<ICompilationUnit, char[]>();

  /** Contains author from javadoc */
  private final static Map<IType, String> authorMap = new HashMap<IType, String>();

  public LineManger() {
    authorMap.clear();
  }

  /**
   * @param element class, method or field to get the line in source code
   * @return the line number of a class, method, field in a file, or -1 if the
   *         line could not be found
   * @throws CoreException when there are problem in scanning java files
   */
  public int getLine(IMember element) throws CoreException {
    ISourceRange sourceRange = element.getNameRange();
    int offset = sourceRange.getOffset();
    return getLine(element, offset);
  }

  public int getLineEnd(IMember element) {
    try {
      ISourceRange sourceRange = element.getSourceRange();
      int offsetEnd = sourceRange.getOffset() + sourceRange.getLength();
      IScanner scanner = createScanner(element);
      if (scanner != null) {
        return scanner.getLineNumber(offsetEnd);
      }
    }
    catch (CoreException e) {
      Log.warn("Can't get LineEnd: %s", e); //$NON-NLS-1$
    }
    return LINE_NOT_FOUND;
  }

  public int getLineStart(IMember element) {
    try {
      ISourceRange javaDocRange = element.getJavadocRange();
      int offset = javaDocRange != null ? javaDocRange.getOffset() : element.getSourceRange().getOffset();
      IScanner scanner = createScanner(element);
      if (scanner != null) {
        return scanner.getLineNumber(offset);
      }
    }
    catch (CoreException e) {
      Log.warn("Can't get LineEnd: %s", e); //$NON-NLS-1$
    }
    return LINE_NOT_FOUND;
  }

  public static String getAuthor(IJavaElement javaElement) {
    return authorMap.get(JavaElementUtil.getTypeFor(javaElement, true));
  }

  /**
   * @param element to create a scanner
   * @param offset char position in the file
   * @return The source code line from the offset (position in file)
   * @throws CoreException when there are problem in scanning java files
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
   * Get the lines for which the @SuppressWarnings annotations are<p>
   * See feature request: Want annotations, not comments, to indicate non-dead code - ID: 2658675
   */
  private static FindIgnoreLinesVisitor findUcdSuppressWarningLines(IScanner scanner, ICompilationUnit compilationUnit) {
    ASTParser parser = UCDetectorPlugin.newASTParser();
    parser.setSource(compilationUnit); // compilationUnit needed for resolve bindings!
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setResolveBindings(true);
    ASTNode createAST = parser.createAST(null);
    FindIgnoreLinesVisitor visitor = new FindIgnoreLinesVisitor(scanner);
    createAST.accept(visitor);
    // System.out.println("ignoreLines=" + visitor.ignoreLines);
    return visitor;
  }

  private static class FindIgnoreLinesVisitor extends ASTMemberVisitor {
    final Set<Integer> ignoreLines = new LinkedHashSet<Integer>();
    private final IScanner scanner;
    private String firstAuthor;

    protected FindIgnoreLinesVisitor(IScanner scanner) {
      this.scanner = scanner;
    }

    /**
     * We start at a bodyDeclaration (=declaration of a class, method, field...)
     * like 'private in i = 0'<br>
     * Then we try to find @SuppressWarnings<br>
     * then we look for the value of the annotation like @SuppressWarnings("ucd")
     */
    @Override
    protected boolean visitImpl(BodyDeclaration declaration, SimpleName name) {
      // System.out.println("declaration=" + declaration);
      for (Object modifier : declaration.modifiers()) {
        // System.out.println("modifier=" + modifier + Log.getClassName(modifier));
        if (modifier instanceof Annotation) {
          Annotation annotation = (Annotation) modifier;
          if (isIgnoreAnnotation(annotation)) {
            ignoreLines.add(Integer.valueOf(scanner.getLineNumber(name.getStartPosition())));
          }
        }
      }
      // [ 2923567 ] Do not report markers for deprecated class members
      // All children for a class are ignored automatically. See SearchManager.noRefTypes
      if (Prefs.isFilterDeprecated()) {
        Javadoc javadoc = declaration.getJavadoc();
        if (javadoc != null && javadoc.tags() != null) {
          @SuppressWarnings("unchecked")
          List<TagElement> tags = javadoc.tags();
          for (TagElement tag : tags) {
            if (TagElement.TAG_DEPRECATED.equals(tag.getTagName())) {
              ignoreLines.add(Integer.valueOf(scanner.getLineNumber(name.getStartPosition())));
            }
          }
        }
      }
      if (declaration instanceof AbstractTypeDeclaration) {
        Javadoc javadoc = declaration.getJavadoc();
        if (javadoc != null && javadoc.tags() != null) {
          @SuppressWarnings("unchecked")
          List<TagElement> tags = javadoc.tags();
          for (TagElement tag : tags) {
            if (TagElement.TAG_AUTHOR.equals(tag.getTagName())) {
              @SuppressWarnings("unchecked")
              List<TextElement> fragments = tag.fragments();
              if (!fragments.isEmpty() && firstAuthor == null) {
                firstAuthor = fragments.get(0).getText();
                break;
              }
            }
          }
        }
      }
      return true;
    }

    private static boolean isIgnoreAnnotation(Annotation annotation) {
      // See example in: SuppressWarningsProposal
      // The name we see in code. eg: Test, but maybe also be org.junit.Test
      String visibleName = annotation.getTypeName().getFullyQualifiedName();
      if (Prefs.isFilter_NO_UCD() && isSuppressWarningsUCDetector(annotation, visibleName)) {
        return true;
      }
      if (isUsedByAnnotation(visibleName)) {
        return true;
      }
      if (Prefs.isFilterDeprecated() && isDeprecatedAnnotation(visibleName)) {
        return true;
      }
      if (Prefs.isFilterAnnotation(visibleName)) {
        return true;
      }
      // Match org.ucdetector.example.FilterMeAnnotation AND FilterMeAnnotation
      // using bindings, we get simple name AND full name
      ITypeBinding typeBinding = annotation.resolveTypeBinding();
      if (typeBinding != null) {
        String name = typeBinding.getName();
        String fullName = typeBinding.getQualifiedName();
        if (Prefs.isFilterAnnotation(fullName) || Prefs.isFilterAnnotation(name)) {
          return true;
        }
      }
      return false;
    }

    private static boolean isSuppressWarningsUCDetector(Annotation annotation, String name) {
      if (SuppressWarnings.class.getName().equals(name) || SuppressWarnings.class.getSimpleName().equals(name)) {
        if (annotation instanceof SingleMemberAnnotation) {
          Expression value = ((SingleMemberAnnotation) annotation).getValue();
          if (value instanceof ArrayInitializer) {
            ArrayInitializer arrayInitializer = (ArrayInitializer) value;
            List<?> expressions = arrayInitializer.expressions();
            for (Object oExpression : expressions) {
              if (isUcdTag((StringLiteral) oExpression)) {
                return true;
              }
            }
          }
          else if (value instanceof StringLiteral) {
            if (isUcdTag((StringLiteral) value)) {
              return true;
            }
          }
        }
      }
      return false;
    }

    private static boolean isUsedByAnnotation(String name) {
      return UsedBy.class.getSimpleName().equals(name) //
          || UsedBy.class.getName().equals(name);
    }

    // [ 2923567 ] Do not report markers for deprecated class members
    private static boolean isDeprecatedAnnotation(String name) {
      return Deprecated.class.getSimpleName().equals(name) //
          || Deprecated.class.getName().equals(name);
    }

    private static boolean isUcdTag(StringLiteral literal) {
      //      System.out.println("\tliteralValue=" + literal.getLiteralValue());
      return literal.getLiteralValue().equalsIgnoreCase(UCD_ANNOTATION_VALUE);
    }
  }

  /**
   * Parse the java code
   */
  private IScanner createScanner(IJavaElement javaElement) throws CoreException {
    IOpenable openable = javaElement.getOpenable();
    if (!(openable instanceof ICompilationUnit)) {
      Log.warn("openable NOT instanceof ICompilationUnit '%s' %s", //$NON-NLS-1$
          JavaElementUtil.getElementName(javaElement), JavaElementUtil.getClassName(javaElement));
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
    IScanner scanner = UCDetectorPlugin.createScanner(javaElement);
    // old: char[] contents = org.eclipse.jdt.internal.core.CompilationUnit.getContents();
    char[] contents = compilationUnit.getBuffer().getCharacters();
    contentsMap.put(compilationUnit, contents);
    scanner.setSource(contents);
    Set<Integer> ignoreLines = new HashSet<Integer>();
    ignoreLineMap.put(scanner, ignoreLines);
    int nextToken;
    try {
      while ((nextToken = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
        // We must run getNextToken() until end, to call scanner.getLineEnds() later 
        if (Prefs.isFilter_NO_UCD()) {
          addIgnoreLineForToken(ignoreLines, scanner, NO_UCD_COMMENT, nextToken, ITerminalSymbols.TokenNameCOMMENT_LINE);
        }
      }
    }
    catch (InvalidInputException e) {
      IStatus status = new Status(IStatus.ERROR, UCDetectorPlugin.ID, IStatus.ERROR, e.getMessage(), e);
      throw new CoreException(status);
    }
    scannerMap.put(compilationUnit, new ScannerTimestamp(scanner, timeStamp));
    lineEndsMap.put(compilationUnit, scanner.getLineEnds());
    FindIgnoreLinesVisitor visitor = findUcdSuppressWarningLines(scanner, compilationUnit);
    ignoreLines.addAll(visitor.ignoreLines);
    if (visitor.firstAuthor != null) {
      IType type = JavaElementUtil.getTypeFor(javaElement, true);
      //      System.out.println(type.getElementName() + "->" + visitor.firstAuthor);
      authorMap.put(type, visitor.firstAuthor.trim());
    }
    return scanner;
  }

  /**
   * @param element to create a scanner
   * @param offset char position in the file
   * @return Piece of code contained in offset
   */
  public String getPieceOfCode(IJavaElement element, int offset) {
    IType type = JavaElementUtil.getTypeFor(element, false);
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
            Log.error("Can't get piece of code for element: " //$NON-NLS-1$
                + JavaElementUtil.getElementName(element) + ", offset: " + offset, e); //$NON-NLS-1$
          }
        }
      }
    }
    return ""; //$NON-NLS-1$
  }

  //   /**
  //    * @param element to create a scanner
  //    * @param offset char position in the file
  //    * @param length of the code
  //    * @return Piece of code contained in offset
  //    * @throws CoreException if there are problems creating a scanner
  //    */
  //   public String getPieceOfCode(IJavaElement element, int offset, int length)
  //       throws CoreException {
  //     createScanner(element);
  //     IType type = JavaElementUtil.getTypeFor(element, false);
  //     ICompilationUnit unit = type.getCompilationUnit();
  //     char[] chars = contentsMap.get(unit);
  //     if (chars != null && (offset + length) < chars.length) {
  //       return String.valueOf(chars, offset, length);
  //     }
  //     return null;
  //   }

  /**
   * Add line number for a tag like "NO_UCD"
   */
  private static void addIgnoreLineForToken(Set<Integer> ignoreLines, IScanner scanner, String tag, int nextToken,
      int tokenType) {
    if (nextToken == tokenType) {
      char[] currentTokenSource = scanner.getCurrentTokenSource();
      String source = new String(currentTokenSource);
      if (source.contains(tag)) {
        int start = scanner.getCurrentTokenStartPosition();
        int line = scanner.getLineNumber(start);
        ignoreLines.add(Integer.valueOf(line));
      }
    }
    /*
    if (nextToken == ITerminalSymbols.TokenNameAT) {
      scanner.getCurrentTokenSource(); // @
      scanner.getNextToken(); // SurpressWarnings
      scanner.getNextToken(); // (
      scanner.getNextToken(); // "unchecked"
      scanner.getNextToken(); // )
    }
    */
  }

  /**
   * This class holds a scanner and its timestamp.
   * Update scanner, if file has changed!
   */
  private static final class ScannerTimestamp {
    final long timeStamp;
    final IScanner scanner;

    ScannerTimestamp(IScanner scanner, long timeStamp) {
      this.scanner = scanner;
      this.timeStamp = timeStamp;
    }
  }
}
