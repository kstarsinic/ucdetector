/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
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
import org.ucdetector.preferences.Prefs;
import org.ucdetector.util.JavaElementUtil;
import org.ucdetector.util.MarkerFactory;
import org.ucdetector.util.StopWatch;

/**
 * Search for class, methods, fields using the eclipse search mechanism
 */
public class SearchManager {
  private static final boolean DEBUG = Log
      .isDebugOption("org.ucdetector/debug/search"); //$NON-NLS-1$
  /**
   * Information for user, that we are searching for final stuff
   */
  private static final String SEARCH_FINAL_MESSAGE = "final"; //$NON-NLS-1$
  /**
   * Get Information about code lines in source code files
   */
  private final LineManger lineManger = new LineManger();
  /**
   * Show progress to user
   */
  private final UCDProgressMonitor monitor;
  /**
   * Number of classes, methods, fields to search
   */
  private final int searchTotal;
  /**
   * Number UCDetector problems already found
   */
  private int foundTotal;
  /**
   * Number of classes, methods, fields already searched
   */
  private int search = 0;
  /**
   * Pattern for text file search like *.xml;*.java
   */
  private final String[] filePatternLiteralSearch;
  /**
   * shortcut to skip methods and fields of classes which have no references
   */
  private final List<IType> noRefTypes = new ArrayList<IType>();
  /**
   * Factory to create markers
   */
  private final MarkerFactory markerFactory;
  /**
   * handle final stuff
   */
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
   */
  public final void search(List<IType> types, List<IMethod> methods,
      List<IField> fields, Object[] selected) throws CoreException {
    if (DEBUG) {
      Log.logDebug(types.size() + " types to search"); //$NON-NLS-1$
      Log.logDebug(methods.size() + " methods to search"); //$NON-NLS-1$
      Log.logDebug(fields.size() + " fields to search"); //$NON-NLS-1$
    }
    // first searchTypes to fill noRefTypes!
    searchTypes(types);
    searchMethods(methods);
    searchFields(fields);
  }

  /**
   * Search types
   */
  private void searchTypes(List<IType> types) throws CoreException {
    for (IType type : types) {
      if (monitor.isCanceled()) {
        return;
      }
      search++;
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
  }

  /**
   * Search methods
   */
  private void searchMethods(List<IMethod> methods) throws CoreException {
    for (IMethod method : methods) {
      if (monitor.isCanceled()) {
        return;
      }
      search++;
      monitor.worked(1);
      String searchInfo = JavaElementUtil.getMemberTypeString(method);
      IType type = JavaElementUtil.getTypeFor(method);
      // Ignore types, which have no references
      if (noRefTypes.contains(type)) {
        continue;
      }
      // Ignore methods overriding java.lang.Object methods
      if (JavaElementUtil.isMethodOfJavaLangObject(method)) {
        continue;
      }
      // Ignore serialization methods
      if (JavaElementUtil.isSerializationMethod(method)) {
        continue;
      }
      updateMonitorMessage(method, "override/implements", searchInfo); //$NON-NLS-1$
      // Ignore methods overriding or implementing other methods
      boolean isOverride = JavaElementUtil.isOverrideOrImplements(method);
      if (isOverride) {
        continue;
      }

      // it is very expensive to call this method!!!
      StopWatch stop = new StopWatch(method);
      boolean isOverriddenMethod = JavaElementUtil.isOverriddenMethod(method);
      stop.end("    Calculate if is overridden method"); //$NON-NLS-1$

      int line = lineManger.getLine(method);
      StopWatch watch = new StopWatch(method);
      if (!isOverriddenMethod) {
        updateMonitorMessage(method, SEARCH_FINAL_MESSAGE, searchInfo);
        boolean created = finalHandler.createFinalMarker(method, line);
        watch.end("    Calculate method final marker"); //$NON-NLS-1$
        if (created) {
          foundTotal++;
        }
      }
      updateMonitorMessage(method, Messages.SearchManager_SearchReferences,
          searchInfo);
      searchImpl(method, searchInfo, isOverriddenMethod);
      watch.end("    searchImpl"); //$NON-NLS-1$
    }
  }

  /**
   * Search fields
   */
  private void searchFields(List<IField> fields) throws CoreException {
    for (IField field : fields) {
      if (monitor.isCanceled()) {
        return;
      }
      search++;
      monitor.worked(1);
      String searchInfo = JavaElementUtil.getMemberTypeString(field);
      updateMonitorMessage(field, SEARCH_FINAL_MESSAGE, searchInfo);
      StopWatch watch = new StopWatch(field);
      int line = lineManger.getLine(field);
      if (JavaElementUtil.isSerializationField(field)) {
        continue;
      }
      // We create final markers even for classes which have no references
      boolean created = finalHandler.createFinalMarker(field, line);
      watch.end("    Calculate field final marker"); //$NON-NLS-1$
      if (created) {
        foundTotal++;
      }
      if (Flags.isPrivate(field.getFlags())) {
        continue;
      }
      IType type = JavaElementUtil.getTypeFor(field);
      if (noRefTypes.contains(type)) {
        continue;
      }
      updateMonitorMessage(field, Messages.SearchManager_SearchReferences,
          searchInfo);
      int found = searchImpl(field, searchInfo, false);
      watch.end("    searchImpl"); //$NON-NLS-1$
      if (found > 0 && !hasReadAccess(field)) {
        String message = NLS.bind(
            Messages.SearchManager_MarkerReferenceFieldNeverRead,
            new Object[] { JavaElementUtil.getElementName(field) });
        markerFactory.createReferenceMarker(field, message, line, 0);
      }
    }
  }

  /**
   * @return <code>true</code>, when a fiedl has read access
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
  private static final class FieldReadRequestor extends SearchRequestor {
    private boolean hasReadAccess = false;

    @Override
    public void acceptSearchMatch(SearchMatch match) {
      if (match.getElement() instanceof IJavaElement) {
        hasReadAccess = true;
        throw new OperationCanceledException(
            "Cancel Search: Field has read access");//$NON-NLS-1$
      }
    }
  }

  /**
    * Search for references create marker
    */
  private int searchImpl(IMember member, String searchInfo,
      boolean isOverriddenMethod) throws CoreException {
    int line = lineManger.getLine(member);
    if (monitor.isCanceled() || line == LineManger.LINE_NOT_FOUND) {
      return 0;
    }
    VisibilityHandler visibilityHandler = new VisibilityHandler(markerFactory,
        member);

    UCDSearchRequestor foundResult = searchJavaImpl(member, visibilityHandler);
    int found = foundResult.found;
    // System.out.println("found: " + found + " - " + foundResult.foundTest);
    // 
    boolean created = false;
    if (found > 0 && (found == foundResult.foundTest)) {
      created = markerFactory.createReferenceMarkerTestOnly(member, line);
      if (created) {
        foundTotal++;
      }
    }
    found += searchTextImpl(member, visibilityHandler, found);
    // Fix for BUG 1925549:  Exclude overridden methods from visibility detection
    if (!isOverriddenMethod) {
      created = visibilityHandler.createMarker(member, line, found);
      if (created) {
        foundTotal++;
      }
    }
    Object[] bindings = new Object[] { searchInfo,
        JavaElementUtil.getElementName(member), Integer.valueOf(found) };
    String markerMessage = NLS.bind(Messages.SearchManager_MarkerReference,
        bindings);
    // Fix for BUG 2225016:  Dont create "0 references marker" for overridden methods
    if (found <= Prefs.getWarnLimit() && !isOverriddenMethod) {
      created = markerFactory.createReferenceMarker(member, markerMessage,
          line, found);
      if (created) {
        foundTotal++;
      }
    }
    return found;
  }

  /**
   * Search for java references
   */
  private UCDSearchRequestor searchJavaImpl(IMember member,
      VisibilityHandler visibilityHandler) throws CoreException {
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
    UCDFileSearchRequestor requestor = new UCDFileSearchRequestor(member,
        visibilityHandler);
    TextSearchEngine searchEngine = TextSearchEngine.create();
    try {
      // If we use monitor here, progressbar is very confusing!
      searchEngine.search(scope, requestor, searchPattern, null);
    }
    catch (OperationCanceledException e) {
      // ignore
    }
    catch (OutOfMemoryError e) {
      UCDetectorPlugin.handleOutOfMemoryError(e);
    }
    // bug fix [ 2373808 ]:
    // Classes found by text search should have no markers
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
    String javaElement = JavaElementUtil.getElementName(element);
    Object[] bindings = new Object[] { Integer.valueOf(foundTotal),
        Integer.valueOf(search), Integer.valueOf(searchTotal), searchInfo,
        javaElement, details };
    String message = NLS.bind(Messages.SearchManager_Monitor, bindings);
    monitor.subTask(message);
    if (DEBUG) {
      Log.logDebug(message);
    }
  }

  /**
   * text search in files
   */
  private static final class UCDFileSearchRequestor extends TextSearchRequestor {
    private int found = 0;
    private final VisibilityHandler visibilityHandler;

    private UCDFileSearchRequestor(IMember searchStart,
        VisibilityHandler visibilityHandler) {
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
      checkCancelSearch(found, -1);
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
    private int found = 0;
    private int foundTest = 0;
    private final IMember searchStart;
    private final VisibilityHandler visibilityHandler;

    private UCDSearchRequestor(IMember searchStart,
        VisibilityHandler visibilityHandler) {
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
      checkCancelSearch(found, foundTest);
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
        return true;
      }
      // Ignore type matches referred by itself.
      // See UnusedClassUsedByItself
      if (searchStart instanceof IType) {
        IType searchStartType = (IType) searchStart;
        IType typeFor = JavaElementUtil.getTypeFor(matchJavaElement);
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
  private static void checkCancelSearch(int found, int foundTest) {
    if (Prefs.isDetectTestOnly() && (found == foundTest)) {
      // continue searching, because all machtes are matches in test code
      return;
    }
    if (found > Prefs.getWarnLimit()
        && !Prefs.isCheckIncreaseVisibilityProtected()
        && !Prefs.isCheckIncreaseVisibilityToPrivate()) {
      throw new OperationCanceledException("Cancel Search: Warn limit reached");//$NON-NLS-1$
    }
  }

  /**
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
// 467
