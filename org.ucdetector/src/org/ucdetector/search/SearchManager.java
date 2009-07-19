/**
 * Copyright (c) 2009 Joerg Spieler
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
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
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
  static final boolean DEBUG = Log.isDebugOption("org.ucdetector/debug/search"); //$NON-NLS-1$
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
  /** Pattern for text file search like *.xml;*.java  */
  private final String[] filePatternLiteralSearch;
  /** shortcut to skip methods and fields of classes which have no references */
  private final List<IType> noRefTypes = new ArrayList<IType>();
  /** contains all exceptions happened during search */
  private final List<IStatus> searchProblems = new ArrayList<IStatus>();
  /** Factory to create markers */
  private final MarkerFactory markerFactory;
  /** handle final stuff   */
  private final FinalHandler finalHandler;

  public SearchManager(UCDProgressMonitor monitor, int searchTotal,
      MarkerFactory markerFactory) {
    this.monitor = monitor;
    this.searchTotal = searchTotal;
    filePatternLiteralSearch = Prefs.getFilePatternLiteralSearch();
    this.markerFactory = markerFactory;
    finalHandler = new FinalHandler(markerFactory);
  }

  /**
   * Start searching for classes, methods, fields
   * @param typeContainers classes to search
   */
  public final void search(Set<TypeContainer> typeContainers) {
    Log.logInfo("Search start: " + UCDetectorPlugin.getNow()); //$NON-NLS-1$
    try {
      int pos = 0;
      for (TypeContainer container : typeContainers) {
        pos++;
        String message = "Search " + fill(pos, 4) // //$NON-NLS-1$
            + " of " + fill(typeContainers.size(), 4) //$NON-NLS-1$ 
            + " types. Markers " + fill(markerCreated, 4) //$NON-NLS-1$ 
            + ". Exceptions " + fill(searchProblems.size(), 2) //$NON-NLS-1$ 
            + ". Class " + JavaElementUtil.getTypeName(container.getType()) //$NON-NLS-1$ 
            + " - " + UCDetectorPlugin.getNow(); //$NON-NLS-1$
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
      IStatus[] stati = searchProblems.toArray(new IStatus[searchProblems
          .size()]);
      MultiStatus status = new MultiStatus(UCDetectorPlugin.ID, IStatus.ERROR,
          stati, stati.length + " errors happened during UCDetection", null); //$NON-NLS-1$
      UCDetectorPlugin.logStatus(status);
    }
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
      String message = "Problems searching " //$NON-NLS-1$
          + JavaElementUtil.getMemberTypeString(member)//
          + " " + JavaElementUtil.getElementName(member); //$NON-NLS-1$ 
      Log.logError(message, ex);
      Status status = new Status(IStatus.ERROR, UCDetectorPlugin.ID,
          IStatus.ERROR, message, ex);
      markerFactory.reportDetectionProblem(status);
      searchProblems.add(status);
      if (searchProblems.size() > 100) {
        throw new OperationCanceledException(
            "Stopped searching. To many Exceptions!"); //$NON-NLS-1$
      }
    }
  }

  /**
   * Search types
   */
  private void searchSpecific(IType type) throws CoreException {
    monitor.worked(1);
    String searchInfo = JavaElementUtil.getMemberTypeString(type);
    updateMonitorMessage(type, Messages.SearchManager_SearchReferences,
        searchInfo);
    StopWatch watch = new StopWatch(type);
    int found = searchImpl(type, searchInfo, false);
    watch.end("    Calculate reference marker"); //$NON-NLS-1$
    if (found == 0) {
      noRefTypes.add(type);
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
    updateMonitorMessage(method, Messages.SearchManager_SearchReferences,
        searchInfo);
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
    updateMonitorMessage(field, Messages.SearchManager_SearchReferences,
        searchInfo);
    int found = searchImpl(field, searchInfo, false);
    watch.end("    searchImpl"); //$NON-NLS-1$
    if (found > 0 && !hasReadAccess(field)) {
      String message = NLS.bind(
          Messages.MarkerFactory_MarkerReferenceFieldNeverRead,
          new Object[] { JavaElementUtil.getElementName(field) });
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
    SearchPattern pattern = SearchPattern.createPattern(field,
        IJavaSearchConstants.READ_ACCESSES);
    FieldReadRequestor requestor = new FieldReadRequestor();
    JavaElementUtil.runSearch(pattern, requestor, SearchEngine
        .createWorkspaceScope());
    return requestor.hasReadAccess;
  }

  /**
   * check, if a field has read access
   */
  static final class FieldReadRequestor extends SearchRequestor {
    boolean hasReadAccess = false;

    @Override
    public void acceptSearchMatch(SearchMatch match) {
      if (match.getElement() instanceof IJavaElement) {
        hasReadAccess = true;
        throw new OperationCanceledException(
            "Cancel Search: Field has read access");//$NON-NLS-1$
      }
    }

    @Override
    public String toString() {
      return "hasReadAccess=" + hasReadAccess; //$NON-NLS-1$
    }
  }

  /**
    * Search for references create marker
    */
  private int searchImpl(IMember member, String searchInfo,
      boolean isOverriddenMethod) throws CoreException {
    int line = lineManger.getLine(member);
    checkForCancel();
    if (line == LineManger.LINE_NOT_FOUND) {
      return 0;
    }
    VisibilityHandler visibilityHandler = new VisibilityHandler(markerFactory,
        member);
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
    Object[] bindings = new Object[] { searchInfo,
        JavaElementUtil.getElementName(member), Integer.valueOf(found) };
    String markerMessage = NLS.bind(Messages.MarkerFactory_MarkerReference,
        bindings);
    // BUG: Don't check for constructors called only 1 time - ID: 2743872
    if (member instanceof IMethod) {
      if (((IMethod) member).isConstructor() && found > 0) {
        return found;
      }
    }
    // Fix for BUG 2225016:  Don't create "0 references marker" for overridden methods
    if (found > Prefs.getWarnLimit() || isOverriddenMethod) {
      return found;
    }
    // Fix for BUG 2808853: Don't create "0 references marker" for classes with main methods
    if (member instanceof IType) {
      if (JavaElementUtil.hasMainMethod((IType) member)) {
        return found;
      }
    }
    created = markerFactory.createReferenceMarker(member, markerMessage, line,
        found);
    if (created) {
      markerCreated++;
    }
    return found;
  }

  /**
   * Search for java references
   */
  private UCDSearchRequestor searchJavaImpl(IMember member,
      VisibilityHandler visibilityHandler) throws CoreException {
    checkForCancel();
    IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
    SearchPattern pattern = SearchPattern.createPattern(member,
        IJavaSearchConstants.REFERENCES);
    UCDSearchRequestor requestor = new UCDSearchRequestor(member,
        visibilityHandler);
    boolean isSearchException = JavaElementUtil.runSearch(pattern, requestor,
        scope);
    // Let's be pessimistic and handle an Exception as "reference found"!
    if (isSearchException && requestor.found == 0) {
      requestor.found = 1;
    }
    return requestor;
  }

  /**
   * Search in text files
   */
  private int searchTextImpl(IMember member,
      VisibilityHandler visibilityHandler, int found) throws CoreException {
    checkForCancel();
    if (!Prefs.isUCDetectionInLiterals() || !(member instanceof IType)) {
      return 0;

    }
    // Only search if nothing is found and visibility is public
    if (found > 0 && visibilityHandler.isMaxVisibilityFoundPublic()) {
      return 0;
    }
    IType type = (IType) member;
    String searchInfo = JavaElementUtil.getMemberTypeString(member);
    updateMonitorMessage(type, Messages.SearchManager_SearchClassNameAsLiteral,
        searchInfo);
    FileTextSearchScope scope = FileTextSearchScope.newWorkspaceScope(
        filePatternLiteralSearch, /*exclude bin dir */false);
    String searchString;
    boolean searchFullClassName = Prefs.isUCDetectionInLiteralsFullClassName();
    if (searchFullClassName) {
      searchString = type.getFullyQualifiedName();
    }
    else {
      searchString = type.getElementName();
    }
    if (DEBUG) {
      StringBuilder mes = new StringBuilder();
      mes.append("Text search of ");//$NON-NLS-1$
      mes.append(searchFullClassName ? "full" : "simple");//$NON-NLS-1$ //$NON-NLS-2$
      mes.append(" classname '").append(searchString).append("'");//$NON-NLS-1$ //$NON-NLS-2$
      Log.logDebug(mes.toString());
    }
    if (searchString == null || searchString.length() == 0) {
      return 0;
    }
    Pattern searchPattern = Pattern.compile(Pattern.quote(searchString));
    UCDFileSearchRequestor requestor = new UCDFileSearchRequestor(searchString,
        visibilityHandler);
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
        Log.logDebug("Matches found searching class name '" + searchString //$NON-NLS-1$
            + " in text files"); //$NON-NLS-1$
      }
      noRefTypes.add(type);
    }
    return requestor.found;
  }

  /**
   * Message shown in the progress dialog like:<br>
   *        <code>Found 2! Done 7/58. Detecting class Classname.methodName()</code>
   */
  private void updateMonitorMessage(IJavaElement element, String details,
      String searchInfo) {
    checkForCancel();
    String javaElement = JavaElementUtil.getElementName(element);
    Object[] bindings = new Object[] { Integer.valueOf(markerCreated),
        Integer.valueOf(search), Integer.valueOf(searchTotal), searchInfo,
        javaElement, details };
    String message = NLS.bind(Messages.SearchManager_Monitor, bindings);
    monitor.subTask(message);
  }

  /**
   * text search in files
   */
  final class UCDFileSearchRequestor extends TextSearchRequestor {
    int found = 0;
    final VisibilityHandler visibilityHandler;
    final String searchString;

    @Override
    public String toString() {
      return "'" + searchString + "' found=" + found; //$NON-NLS-1$ //$NON-NLS-2$
    }

    UCDFileSearchRequestor(String searchString,
        VisibilityHandler visibilityHandler) {
      this.searchString = searchString;
      this.visibilityHandler = visibilityHandler;
    }

    /**
     * Search for className or packageName.className, check character 
     * before and after match, if it is a JavaIdentifier
     */
    @Override
    public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess)
        throws CoreException {
      char beforeChar = getCharBefore(matchAccess);
      char afterChar = getCharAfter(matchAccess);
      boolean isValidCharBefore = Character.isJavaIdentifierStart(beforeChar);
      boolean isValidCharAfter = Character.isJavaIdentifierPart(afterChar);
      boolean isClassNamMatchOk = !isValidCharBefore && !isValidCharAfter;
      if (DEBUG) {
        int offset = matchAccess.getMatchOffset();
        int length = matchAccess.getMatchLength();
        String match = matchAccess.getFileContent(offset, length);
        StringBuilder mes = new StringBuilder();
        mes.append("    TEXT MATCH {").append(beforeChar).append(match);//$NON-NLS-1$
        mes.append(afterChar).append("}"); //$NON-NLS-1$
        mes.append(", isMatchOk=").append(isClassNamMatchOk); //$NON-NLS-1$
        mes.append(", in=").append(matchAccess.getFile()); //$NON-NLS-1$
        Log.logDebug(mes.toString());
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
  static final class UCDSearchRequestor extends SearchRequestor {
    int found = 0;
    int foundTest = 0;
    private final IMember searchStart;
    private final VisibilityHandler visibilityHandler;

    @Override
    public String toString() {
      return searchStart.getElementName() + " found=" + found + ", foundTest=" //$NON-NLS-1$ //$NON-NLS-2$
          + foundTest;
    }

    UCDSearchRequestor(IMember searchStart, VisibilityHandler visibilityHandler) {
      this.searchStart = searchStart;
      this.visibilityHandler = visibilityHandler;
    }

    @Override
    public void acceptSearchMatch(SearchMatch match) {
      if (ignoreMatch(match)) {
        return;
      }
      this.found++;
      IJavaElement matchJavaElement = (IJavaElement) match.getElement();
      if (Prefs.isDetectTestOnly()
          && JavaElementUtil.isTestCode(matchJavaElement)) {
        foundTest++;
      }
      checkCancelSearch(matchJavaElement, found, foundTest);
      visibilityHandler.checkVisibility(matchJavaElement, found, foundTest);
    }

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
          Log.logError("Can't get flags of: " + importDecl.getElementName(), //$NON-NLS-1$
              ex);
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
      return false;
    }
  }

  /**
   * Check conditions from preferences and 
   * cancel search by throwing a {@link OperationCanceledException}
   * when necessary
   */
  static void checkCancelSearch(IJavaElement javaElement, int found,
      int foundTest) {
    if (Prefs.isDetectTestOnly() && (found == foundTest)) {
      // continue searching, because all matches are matches in test code
      return;
    }
    if (found > Prefs.getWarnLimit()
        && !Prefs.isCheckReduceVisibilityProtected(javaElement)
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