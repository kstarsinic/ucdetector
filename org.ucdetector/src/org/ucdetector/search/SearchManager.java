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
import org.eclipse.core.runtime.IProgressMonitor;
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
  private final IProgressMonitor monitor;
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
  private final FinalHandler finalHandler;

  public SearchManager(IProgressMonitor monitor, int searchTotal,
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
      updateMonitorMessage(type, Messages.SearchManager_SearchReferences);
      String searchInfo = Messages.SearchManager_Class;
      StopWatch watch = new StopWatch(type);
      int found = searchImpl(type, searchInfo, false);
      watch.end("searchImpl"); //$NON-NLS-1$
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
      updateMonitorMessage(method, ""); //$NON-NLS-1$
      IType type = JavaElementUtil.getTypeFor(method);
      // Ignore types, which have no references
      if (noRefTypes.contains(type)) {
        continue;
      }
      // Ignore interface methods
      if (type != null && type.isInterface()) {
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
      updateMonitorMessage(method, "override/implements"); //$NON-NLS-1$
      // ***********************************************************************
      // TODO 01.09.2008: Refactor. Confusing to understand / add new handler!
      //  - finalHandler.createFinalMarker
      //  - visibilityHandler.createMarker
      //  - markerFactory.createReferenceMarker
      // ***********************************************************************
      // Ignore methods overriding or implementing other methods
      boolean isOverride = JavaElementUtil.isOverrideOrImplements(method);
      if (isOverride) {
        continue;
      }

      // it is very expensive to call this method!!!
      boolean isOverriddenMethod = JavaElementUtil.isOverriddenMethod(method);

      int line = lineManger.getLine(method);
      StopWatch watch = new StopWatch(method);
      if (!isOverriddenMethod) {
        updateMonitorMessage(method, SEARCH_FINAL_MESSAGE);
        boolean created = finalHandler.createFinalMarker(method, line);
        watch.end("createFinalMarker"); //$NON-NLS-1$
        if (created) {
          foundTotal++;
        }
      }
      updateMonitorMessage(method, Messages.SearchManager_SearchReferences);
      String searchInfo = method.isConstructor() ? Messages.SearchManager_Constructor
          : Messages.SearchManager_Method;
      searchImpl(method, searchInfo, isOverriddenMethod);
      watch.end("searchImpl"); //$NON-NLS-1$
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
      updateMonitorMessage(field, SEARCH_FINAL_MESSAGE);
      StopWatch watch = new StopWatch(field);
      int line = lineManger.getLine(field);
      if (JavaElementUtil.isSerializationField(field)) {
        continue;
      }
      // We create final markers even for classes which have no references
      boolean created = finalHandler.createFinalMarker(field, line);
      watch.end("createFinalMarker"); //$NON-NLS-1$
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
      // ***********************************************************************
      // TODO 01.09.2008: Refactor. Confusing to understand / add new handler!
      //  - finalHandler.createFinalMarker
      //  - visibilityHandler.createMarker
      //  - markerFactory.createReferenceMarker
      // ***********************************************************************
      String searchInfo = JavaElementUtil.isConstant(field) ? Messages.SearchManager_Constant
          : Messages.SearchManager_Field;
      updateMonitorMessage(field, Messages.SearchManager_SearchReferences);
      searchImpl(field, searchInfo, false);
      watch.end("searchImpl"); //$NON-NLS-1$
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

    int found = searchJavaImpl(member, visibilityHandler);
    found += searchTextImpl(member, visibilityHandler, found);
    boolean created = false;
    // Fix for BUG 1925549:  Exclude overridden methods from visibility detection
    if (!isOverriddenMethod) {
      created = visibilityHandler.createMarker(member, line, found);
      if (created) {
        foundTotal++;
      }
    }
    Object[] bindings = new Object[] { searchInfo, member.getElementName(),
        new Integer(found) };
    String markerMessage = NLS.bind(Messages.SearchManager_MarkerReference,
        bindings);
    if (found <= Prefs.getWarnLimit()) {
      created = markerFactory
          .createReferenceMarker(member, markerMessage, line);
      if (created) {
        foundTotal++;
      }
    }
    return found;
  }

  /**
   * Search for java references
   */
  private int searchJavaImpl(IMember member, VisibilityHandler visibilityHandler)
      throws CoreException {
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
    return requestor.found;
  }

  /**
   * Search in text files
   * @param found 
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
    updateMonitorMessage(type, Messages.SearchManager_SearchClassNameAsLiteral);
    FileTextSearchScope scope = FileTextSearchScope.newWorkspaceScope(
        filePatternLiteralSearch, true);
    // String searchString = "\"" + type.getFullyQualifiedName() + "\"";
    String searchString = type.getFullyQualifiedName();
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
    return requestor.found;
  }

  /**
   * Message shown in the progress dialog like:<br>
   *        <code>Found 2, search 7/58: Classname.methodName()</code>
   */
  private void updateMonitorMessage(IJavaElement element, String details) {
    String info = JavaElementUtil.asString(element);
    Object[] bindings = new Object[] { Integer.valueOf(foundTotal),
        Integer.valueOf(search), Integer.valueOf(searchTotal), info, details };
    String message = NLS.bind(Messages.SearchManager_Monitor, bindings);
    monitor.subTask(message);
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
     * in java files we search for "org.ucdetector.Test" 
     * instead of org.ucdetector.Test
     */
    @Override
    public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess)
        throws CoreException {
      String fileName = matchAccess.getFile().getName();
      if (fileName.endsWith(".java")) { //$NON-NLS-1$
        if (matchIsQuoted(matchAccess)) {
          this.found++;
        }
      }
      else {
        this.found++;
      }

      if (found > Prefs.getWarnLimit()
          && !Prefs.isCheckIncreaseVisibilityProtected()
          && !Prefs.isCheckIncreaseVisibilityToPrivate()) {
        throw new OperationCanceledException(
            "Cancel Search: Warn limit reached");//$NON-NLS-1$
      }
      IJavaElement matchJavaElement = JavaCore.create(matchAccess.getFile());
      visibilityHandler.checkVisibility(matchJavaElement, found);
      return true;
    }

    /**
     * @return <code>true</code>, when match starts with " and ends with "
     */
    private boolean matchIsQuoted(TextSearchMatchAccess matchAccess) {
      int offset = matchAccess.getMatchOffset();
      int length = matchAccess.getMatchLength();
      boolean isLengthOk = (offset + length + 1) < matchAccess
          .getFileContentLength();
      if (offset > 0 && isLengthOk) {
        String content = matchAccess.getFileContent(offset - 1, length + 2);
        if (content.startsWith("\"") && content.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
          return true;
        }
      }
      return false;
    }
  }

  /**
   * search java references
   */
  private static final class UCDSearchRequestor extends SearchRequestor {
    private int found = 0;
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
      if (found > Prefs.getWarnLimit()
          && !Prefs.isCheckIncreaseVisibilityProtected()
          && !Prefs.isCheckIncreaseVisibilityToPrivate()) {
        throw new OperationCanceledException(
            "Cancel Search: Warn limit reached");//$NON-NLS-1$
      }
      IJavaElement matchJavaElement = (IJavaElement) match.getElement();
      visibilityHandler.checkVisibility(matchJavaElement, found);
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
      // Ignore javadoc matches.
      // See JavaDocExample
      if (match.isInsideDocComment()) {
        return true;
      }
      // Ignore matches in jars, or matches caused by compile problems
      // See ReferenceInJarExample
      if (match.getAccuracy() == SearchMatch.A_INACCURATE) {
        return true;
      }
      Object matchElement = match.getElement();
      // Ignore no IJavaElement matches
      if (!(matchElement instanceof IJavaElement)) {
        return true;
      }
      IJavaElement matchJavaElement = (IJavaElement) matchElement;
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
}
// 467
