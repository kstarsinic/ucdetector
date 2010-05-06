/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.iterator.TypeContainer;
import org.ucdetector.preferences.Prefs;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;
import org.ucdetector.util.StopWatch;

/**
 * Search for class, methods, fields using the eclipse search mechanism
 */
public class SearchManager {
  private static final boolean DEBUG = Log.isDebugOption("org.ucdetector/debug/search"); //$NON-NLS-1$
  /** Information for user, that we are searching for final stuff */
  private static final String SEARCH_FINAL_MESSAGE = "final"; //$NON-NLS-1$
  /** Get Information about code lines in source code files   */
  private final LineManger lineManger = new LineManger();
  /** Show progress to user */
  private final UCDProgressMonitor monitor;
  /** Number of classes, methods, fields to search */
  private final int searchTotal;
  /** Number UCDetector problems already found */
  private int markerCreated;
  /** Number of classes, methods, fields already searched */
  private int search = 0;
  /** shortcut to skip methods and fields of classes which have no references */
  private final List<IType> noRefTypes = new ArrayList<IType>();
  /** Skip search for enum constants, because they are used by value() or valueOf() */
  private final List<IType> usedByValueEnums = new ArrayList<IType>();
  /** contains all exceptions happened during search */
  private final List<IStatus> searchProblems = new ArrayList<IStatus>();
  /** Factory to create markers */
  private final MarkerFactory markerFactory;
  /** handle final stuff   */
  private final FinalHandler finalHandler;

  public SearchManager(UCDProgressMonitor monitor, int searchTotal, MarkerFactory markerFactory) {
    this.monitor = monitor;
    this.searchTotal = searchTotal;
    this.markerFactory = markerFactory;
    finalHandler = new FinalHandler(markerFactory);
  }

  /**
   * Start searching for classes, methods, fields
   * @param typeContainers classes to search
   */
  public final void search(Set<TypeContainer> typeContainers) {
    logStart(typeContainers);
    try {
      int pos = 0;
      for (TypeContainer container : typeContainers) {
        pos++;
        String message = String.format("Search %s of %s types. Markers %s. Exceptions %s. Class %s - %s", //$NON-NLS-1$
            fill(pos, 4),//
            fill(typeContainers.size(), 4),//
            fill(markerCreated, 4), //
            fill(searchProblems.size(), 2),//
            JavaElementUtil.getTypeName(container.getType()), //
            UCDetectorPlugin.getNow());
        if (Log.DEBUG) {
          Log.logDebug(message);
        }
        else if (pos == 1 || pos % 10 == 0 || pos == typeContainers.size()) {
          Log.logInfo(message);
        }
        if (container.getType() != null) {
          searchAndHandleException(container.getType());
        }
        for (IMethod method : container.getMethods()) {
          searchAndHandleException(method);
        }
        for (IField field : container.getFields()) {
          searchAndHandleException(field);
        }
      }
    }
    catch (OperationCanceledException e) {
      Log.logInfo("Search canceled: " + e.getMessage()); //$NON-NLS-1$
    }
    Log.logInfo("Search end: " + UCDetectorPlugin.getNow()); //$NON-NLS-1$
    if (searchProblems.size() > 0) {
      IStatus[] stati = searchProblems.toArray(new IStatus[searchProblems.size()]);
      MultiStatus status = new MultiStatus(UCDetectorPlugin.ID, IStatus.ERROR, stati, stati.length
          + " errors happened during UCDetection", null); //$NON-NLS-1$
      UCDetectorPlugin.logStatus(status);
    }
  }

  private void logStart(Set<TypeContainer> typeContainers) {
    int methodsToDetect = 0;
    int fieldsToDetect = 0;
    for (TypeContainer container : typeContainers) {
      methodsToDetect += container.getMethods().size();
      fieldsToDetect += container.getFields().size();
    }
    Log.logInfo("Detection start      : " + UCDetectorPlugin.getNow()); //$NON-NLS-1$
    Log.logInfo("    Classes to detect: " + typeContainers.size()); //$NON-NLS-1$
    Log.logInfo("    Methods to detect: " + methodsToDetect); //$NON-NLS-1$
    Log.logInfo("    Fields  to detect: " + fieldsToDetect); //$NON-NLS-1$
  }

  private String fill(int i, int length) {
    String result = "" + i; //$NON-NLS-1$
    while (result.length() < length) {
      result = " " + result; //$NON-NLS-1$
    }
    return result;
  }

  /**
   * Search a member
   */
  private void searchAndHandleException(IMember member) {
    monitor.setActiveSearchElement(member);
    checkForCancel();
    search++;
    try {
      if (member instanceof IType) {
        searchSpecific((IType) member);
      }
      else if (member instanceof IMethod) {
        searchSpecific((IMethod) member);
      }
      else if (member instanceof IField) {
        searchSpecific((IField) member);
      }
    }
    //  Fix [ 2810802 ] UCDetector crashes with an Exception
    catch (Exception ex) {
      if (ex instanceof OperationCanceledException) {
        throw (OperationCanceledException) ex;
      }
      String message = String.format("Problems searching %s %s", // //$NON-NLS-1$
          JavaElementUtil.getMemberTypeString(member), JavaElementUtil.getElementName(member));
      Log.logError(message, ex);
      Status status = new Status(IStatus.ERROR, UCDetectorPlugin.ID, IStatus.ERROR, message, ex);
      markerFactory.reportDetectionProblem(status);
      searchProblems.add(status);
      if (searchProblems.size() > 100) {
        throw new OperationCanceledException("Stopped searching. To many Exceptions!"); //$NON-NLS-1$
      }
    }
  }

  /**
   * Search types
   */
  private void searchSpecific(IType type) throws CoreException {
    monitor.worked(1);
    String searchInfo = JavaElementUtil.getMemberTypeString(type);
    updateMonitorMessage(type, Messages.SearchManager_SearchReferences, searchInfo);
    StopWatch watch = new StopWatch(type);
    int found = searchImpl(type, searchInfo, false);
    watch.end("    Calculate reference marker"); //$NON-NLS-1$
    if (found == 0) {
      noRefTypes.add(type);
    }
    if (type.isEnum() && JavaElementUtil.isUsedBySpecialEnumMethods(type)) {
      usedByValueEnums.add(type);
    }
  }

  /**
   * Search method
   */
  private void searchSpecific(IMethod method) throws CoreException {
    monitor.worked(1);
    IType type = JavaElementUtil.getTypeFor(method, false);
    if (type.isAnonymous()) {
      return;// Ignore anonymous types
    }
    if (noRefTypes.contains(type)) {
      return; // Ignore types, which have no references
    }
    if (JavaElementUtil.isMethodOfJavaLangObject(method)) {
      return; // Ignore methods from java.lang.Object
    }

    if (JavaElementUtil.isSerializationMethod(method)) {
      return; // Ignore serialization methods
    }
    int line = lineManger.getLine(method);
    if (line == LineManger.LINE_NOT_FOUND) {
      return;
    }
    String searchInfo = JavaElementUtil.getMemberTypeString(method);
    updateMonitorMessage(method, "override/implements", searchInfo); //$NON-NLS-1$

    // it is very expensive to call this method!!!
    StopWatch stop = new StopWatch(method);
    boolean isOverriddenMethod = JavaElementUtil.isOverriddenMethod(method);
    stop.end("    Calculate if is overridden method"); //$NON-NLS-1$

    StopWatch watch = new StopWatch(method);
    if (!isOverriddenMethod) {
      updateMonitorMessage(method, SEARCH_FINAL_MESSAGE, searchInfo);
      boolean created = finalHandler.createFinalMarker(method, line);
      watch.end("    Calculate method final marker"); //$NON-NLS-1$
      if (created) {
        markerCreated++;
      }
    }
    updateMonitorMessage(method, Messages.SearchManager_SearchReferences, searchInfo);
    searchImpl(method, searchInfo, isOverriddenMethod);
    watch.end("    searchImpl"); //$NON-NLS-1$
  }

  /**
   * Search field
   */
  private void searchSpecific(IField field) throws CoreException {
    monitor.worked(1);
    int line = lineManger.getLine(field);
    if (line == LineManger.LINE_NOT_FOUND) {
      return;
    }
    String searchInfo = JavaElementUtil.getMemberTypeString(field);
    updateMonitorMessage(field, SEARCH_FINAL_MESSAGE, searchInfo);
    StopWatch watch = new StopWatch(field);
    if (JavaElementUtil.isSerializationField(field)) {
      return;
    }
    // We create final markers even for classes which have no references
    boolean created = finalHandler.createFinalMarker(field, line);
    watch.end("    Calculate field final marker"); //$NON-NLS-1$
    if (created) {
      markerCreated++;
    }
    if (Flags.isPrivate(field.getFlags())) {
      return;
    }
    IType type = JavaElementUtil.getTypeFor(field, false);
    if (noRefTypes.contains(type)) {
      return;
    }
    if (type.isAnonymous()) {
      return; // Ignore anonymous classes
    }
    if (usedByValueEnums.contains(type)) {
      return;// See bug 2900561: enum detection, or don't create "unnecessary marker" for enum constants
    }
    updateMonitorMessage(field, Messages.SearchManager_SearchReferences, searchInfo);
    int found = searchImpl(field, searchInfo, false);
    watch.end("    searchImpl"); //$NON-NLS-1$
    if (found > 0 && !hasReadAccess(field)) {
      String message = NLS.bind(Messages.MarkerFactory_MarkerReferenceFieldNeverRead, new Object[] { JavaElementUtil
          .getElementName(field) });
      // found=0 needed here, to create reference marker!
      markerFactory.createReferenceMarker(field, message, line, 0);
    }
  }

  private void checkForCancel() {
    monitor.throwIfIsCanceled();
  }

  public int getMarkerCreated() {
    return markerCreated;
  }

  /**
   * @return <code>true</code>, when a field has read access
   */
  private static boolean hasReadAccess(IField field) throws CoreException {
    SearchPattern pattern = SearchPattern.createPattern(field, IJavaSearchConstants.READ_ACCESSES);
    CountSearchRequestor requestor = new CountSearchRequestor();
    JavaElementUtil.runSearch(pattern, requestor);
    return requestor.isFound();
  }

  /**
    * Search for references create marker
    */
  private int searchImpl(IMember member, String searchInfo, boolean isOverriddenMethod) throws CoreException {
    int line = lineManger.getLine(member);
    checkForCancel();
    if (line == LineManger.LINE_NOT_FOUND) {
      return 0;
    }
    VisibilityHandler visibilityHandler = new VisibilityHandler(markerFactory, member);
    UCDSearchRequestor foundResult = searchJavaImpl(member, visibilityHandler);
    int found = foundResult.found;
    // System.out.println("found: " + found + " - " + foundResult.foundTest);
    boolean created = false;
    if (found > 0 && (found == foundResult.foundTest)) {
      created = markerFactory.createReferenceMarkerTestOnly(member, line);
      if (created) {
        markerCreated++;
      }
    }
    found += searchTextImpl(member, visibilityHandler, found);
    // Fix for BUG 1925549:  Exclude overridden methods from visibility detection
    if (!isOverriddenMethod) {
      created = visibilityHandler.createMarker(member, line, found);
      if (created) {
        markerCreated++;
      }
    }
    Object[] bindings = new Object[] { searchInfo, JavaElementUtil.getElementName(member), Integer.valueOf(found) };
    String markerMessage = NLS.bind(Messages.MarkerFactory_MarkerReference, bindings);
    if (member instanceof IMethod) {
      IMethod method = (IMethod) member;
      // [ 2743872 ] Don't check for constructors called only 1 time
      if (method.isConstructor() && found > 0) {
        return found;
      }
      // [ 2826216 ] Unused interface methods are not detected
      // [ 2225016 ] Don't create "0 references marker" for overridden methods
      boolean isInterfaceMethod = JavaElementUtil.isInterfaceMethod(method);
      if (!isInterfaceMethod && isOverriddenMethod) {
        return found;
      }
    }
    if (found > Prefs.getWarnLimit()) {
      return found;
    }
    // Fix for BUG 2808853: Don't create "0 references marker" for classes with main methods
    if (member instanceof IType) {
      if (JavaElementUtil.hasMainMethod((IType) member)) {
        return found;
      }
    }
    created = markerFactory.createReferenceMarker(member, markerMessage, line, found);
    if (created) {
      markerCreated++;
    }
    return found;
  }

  /**
   * Search for java references
   */
  private UCDSearchRequestor searchJavaImpl(IMember member, VisibilityHandler visibilityHandler) throws CoreException {
    checkForCancel();
    SearchPattern pattern = SearchPattern.createPattern(member, IJavaSearchConstants.REFERENCES);
    UCDSearchRequestor requestor = new UCDSearchRequestor(member, visibilityHandler, lineManger);
    boolean isSearchException = JavaElementUtil.runSearch(pattern, requestor);
    // Let's be pessimistic and handle an Exception as "reference found"!
    if (isSearchException && requestor.found == 0) {
      requestor.found = 1;
    }
    return requestor;
  }

  /**
   * Search in text files
   */
  private int searchTextImpl(IMember member, VisibilityHandler visibilityHandler, int found) throws CoreException {
    checkForCancel();
    if (!Prefs.isUCDetectionInLiterals() || !(member instanceof IType)) {
      return 0;
    }
    // Only search if nothing is found and visibility is public
    if (found > 0 && visibilityHandler.isMaxVisibilityFoundPublic()) {
      return 0;
    }
    IType type = (IType) member;
    // increase performance: only search public classes and primary classes
    if (!JavaElementUtil.isPrimary(type) || !Flags.isPublic(type.getFlags())) {
      return 0;
    }
    String searchInfo = JavaElementUtil.getMemberTypeString(member);

    updateMonitorMessage(type, Messages.SearchManager_SearchClassNameAsLiteral, searchInfo);
    FileTextSearchScope scope = FileTextSearchScope.newWorkspaceScope(Prefs.getFilePatternLiteralSearch(), /*exclude bin dir */
    false);
    String searchString;
    boolean searchFullClassName = Prefs.isUCDetectionInLiteralsFullClassName();
    if (searchFullClassName) {
      searchString = type.getFullyQualifiedName();
    }
    else {
      searchString = type.getElementName();
    }

    if (DEBUG) {
      Log.logDebug("Text search of %s classname '%s'", searchFullClassName ? "full" : "simple", searchString);// //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    if (searchString == null || searchString.length() == 0) {
      return 0;
    }
    Pattern searchPattern = Pattern.compile(Pattern.quote(searchString));
    UCDFileSearchRequestor requestor = new UCDFileSearchRequestor(searchString, visibilityHandler);
    try {
      // If we use monitor here, progressbar is very confusing!
      if (UCDetectorPlugin.isHeadlessMode()) {
        // special search without UI stuff, which fails in headless mode
        new UCDTextSearchVisitor(requestor, searchPattern).search(scope, null);
      }
      else {
        TextSearchEngine.create().search(scope, requestor, searchPattern, null);
      }
    }
    catch (OperationCanceledException e) {
      // ignore
    }
    catch (OutOfMemoryError e) {
      UCDetectorPlugin.handleOutOfMemoryError(e);
    }
    // bug fix [ 2373808 ]: Classes found by text search should have no markers
    if (requestor.found > 0) {
      if (Log.DEBUG) {
        Log.logDebug("Matches found searching class name '%s' in text files", searchString); //$NON-NLS-1$
      }
      noRefTypes.add(type);
    }
    return requestor.found;
  }

  /**
   * Message shown in the progress dialog like:<br>
   *        <code>Found 2! Done 7/58. Detecting class Classname.methodName()</code>
   */
  private void updateMonitorMessage(IJavaElement element, String details, String searchInfo) {
    checkForCancel();
    String javaElement = JavaElementUtil.getElementName(element);
    Object[] bindings = new Object[] { Integer.valueOf(markerCreated), Integer.valueOf(search),
        Integer.valueOf(searchTotal), searchInfo, javaElement, details };
    String message = NLS.bind(Messages.SearchManager_Monitor, bindings);
    monitor.subTask(message);
  }

  /**
   * text search in files
   */
  private final static class UCDFileSearchRequestor extends TextSearchRequestor {
    int found = 0;
    final VisibilityHandler visibilityHandler;
    final String searchString;

    @Override
    public String toString() {
      return String.format("'%s' found=%s", searchString, Integer.valueOf(found)); //$NON-NLS-1$ 
    }

    UCDFileSearchRequestor(String searchString, VisibilityHandler visibilityHandler) {
      this.searchString = searchString;
      this.visibilityHandler = visibilityHandler;
    }

    /**
     * Search for className or packageName.className, check character
     * before and after match, if it is a JavaIdentifier
     */
    @SuppressWarnings("boxing")
    @Override
    public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {
      char beforeChar = getCharBefore(matchAccess);
      char afterChar = getCharAfter(matchAccess);
      boolean isValidCharBefore = Character.isJavaIdentifierStart(beforeChar);
      boolean isValidCharAfter = Character.isJavaIdentifierPart(afterChar);
      boolean isClassNamMatchOk = !isValidCharBefore && !isValidCharAfter;
      if (DEBUG) {
        int offset = matchAccess.getMatchOffset();
        int length = matchAccess.getMatchLength();
        String match = matchAccess.getFileContent(offset, length);
        Log.logDebug("    TEXT MATCH {%s%s%s}. isMatchOk: %s. in: %s", //$NON-NLS-1$
            beforeChar, match, afterChar, isClassNamMatchOk, matchAccess.getFile());
      }
      if (isClassNamMatchOk) {
        this.found++;
      }
      checkCancelSearch(null, found, -1);
      IJavaElement matchJavaElement = JavaCore.create(matchAccess.getFile());
      visibilityHandler.checkVisibility(matchJavaElement, found, -1);
      return true;
    }

    private char getCharBefore(TextSearchMatchAccess match) {
      int offset = match.getMatchOffset();
      return (offset == 0) ? '\n' : match.getFileContentChar(offset - 1);
    }

    private char getCharAfter(TextSearchMatchAccess match) {
      int offset = match.getMatchOffset();
      int length = match.getMatchLength();
      boolean fileEnd = (offset + length) >= match.getFileContentLength();
      return fileEnd ? '\n' : match.getFileContentChar(offset + length);
    }
  }

  /**
   * search java references
   */
  private static final class UCDSearchRequestor extends SearchRequestor {
    int found = 0;
    int foundTest = 0;
    private final IMember searchStart;
    private final VisibilityHandler visibilityHandler;
    @SuppressWarnings("unused")
    private final LineManger lineManager;

    @Override
    public String toString() {
      return String.format("%s found=%s, foundTest=%s", searchStart.getElementName(), Integer.valueOf(found), //$NON-NLS-1$
          Integer.valueOf(foundTest));
    }

    UCDSearchRequestor(IMember searchStart, VisibilityHandler visibilityHandler, LineManger lineManager) {
      this.searchStart = searchStart;
      this.visibilityHandler = visibilityHandler;
      this.lineManager = lineManager;
    }

    @Override
    public void acceptSearchMatch(SearchMatch match) {
      if (ignoreMatch(match)) {
        return;
      }
      this.found++;
      IJavaElement matchJavaElement = (IJavaElement) match.getElement();
      //      checkUnusedBoolean(match, matchJavaElement);
      if (Prefs.isDetectTestOnly() && JavaElementUtil.isTestCode(matchJavaElement)) {
        foundTest++;
      }
      checkCancelSearch(matchJavaElement, found, foundTest);
      visibilityHandler.checkVisibility(matchJavaElement, found, foundTest);
    }

    //TODO: request 2893808: Check for unnecessary (boolean) parameters
    //    private void checkUnusedBoolean(SearchMatch match, IJavaElement matchElement) {
    //      if (match instanceof MethodReferenceMatch) {
    //        MethodReferenceMatch method = (MethodReferenceMatch) match;
    //        System.out.println("method: " + method);
    //        int offset = method.getOffset();
    //        int length = method.getLength();
    //        try {
    //          String code = lineManager
    //              .getPieceOfCode(matchElement, offset, length);
    //          System.out.println("code: " + code);
    //          ASTParser parser = ASTParser.newParser(AST.JLS3);
    //          parser.setSource(code.toCharArray());
    //          parser.setKind(ASTParser.K_STATEMENTS);
    //          //          parser.setResolveBindings(true);
    //          //          ASTNode createAST = parser.createAST(null);
    //          //          FindUcdSuppressWarningsVisitor visitor = new FindUcdSuppressWarningsVisitor(
    //          //              scanner);
    //          //          createAST.accept(visitor);
    //        }
    //        catch (CoreException e) {
    //          e.printStackTrace();
    //        }
    //      }
    //    }

    /**
     * @return <code>true</code> if the found match should be ignored
     * Ignore matches:
     * <ul>
     * <li>matches in jars</li>
     * <li>matches caused by compile problems</li>
     * <li>no IJavaElement matches like javadoc</li>
     * <li>Ignore import, because it maybe an unnecessary import!</li>
     * <li>Ignore type matches referred by itself</li>
     * </ul>
     */
    private boolean ignoreMatch(SearchMatch match) {
      IJavaElement matchJavaElement = defaultIgnoreMatch(match);
      if (matchJavaElement == null) {
        return true;
      }
      // Ignore import, because it maybe an unnecessary import!
      // See OnlyImportDeclarationReferenceExample
      if (matchJavaElement instanceof IImportDeclaration) {
        IImportDeclaration importDecl = (IImportDeclaration) matchJavaElement;
        try {
          // Bug fix: Static imports are not recognized - ID: 2783734
          boolean isStatic = Flags.isStatic(importDecl.getFlags());
          return !isStatic;
        }
        catch (JavaModelException ex) {
          Log.logError("Can't get flags of: " + importDecl.getElementName(), ex); //$NON-NLS-1$
          return false;
        }
      }
      // Ignore type matches referred by itself.
      // See UnusedClassUsedByItself
      if (searchStart instanceof IType) {
        IType searchStartType = (IType) searchStart;
        IType typeFor = JavaElementUtil.getTypeFor(matchJavaElement, false);
        if (typeFor.equals(searchStartType)) {
          return true;
        }
      }
      // Bug 2864967: Ignore references for recursive methods
      if (matchJavaElement.equals(searchStart)) {
        return true;
      }
      return false;
    }
  }

  /**
   * Check conditions from preferences and
   * cancel search by throwing a {@link OperationCanceledException}
   * when necessary
   */
  private static void checkCancelSearch(IJavaElement javaElement, int found, int foundTest) {
    if (Prefs.isDetectTestOnly() && (found == foundTest)) {
      // continue searching, because all matches are matches in test code
      return;
    }
    if (found > Prefs.getWarnLimit() && !Prefs.isCheckReduceVisibilityProtected(javaElement)
        && !Prefs.isCheckReduceVisibilityToPrivate(javaElement)) {
      throw new OperationCanceledException("Cancel Search: Warn limit reached");//$NON-NLS-1$
    }
  }

  /**
   * @param match search match, which should be checked
   * @return <code>true</code> if the found match should be ignored
   * Ignore matches:
   * <ul>
   * <li>matches in jars</li>
   * <li>matches caused by compile problems</li>
   * <li>no IJavaElement matches like javadoc</li>
   * </ul>
   */
  public static IJavaElement defaultIgnoreMatch(SearchMatch match) {
    // Ignore javadoc matches.
    // See JavaDocExample
    if (match.isInsideDocComment()) {
      return null;
    }
    // Ignore matches in jars, or matches caused by compile problems
    // See ReferenceInJarExample
    if (match.getAccuracy() == SearchMatch.A_INACCURATE) {
      return null;
    }
    Object matchElement = match.getElement();
    // Ignore no IJavaElement matches
    if (!(matchElement instanceof IJavaElement)) {
      return null;
    }
    return (IJavaElement) matchElement;
  }
}