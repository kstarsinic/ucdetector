/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.JavaElementUtil;

/**
 * Constant definitions for plug-in preferences, offer access to preferences
 */
public final class Prefs {
  protected static final String CHANGE_ALL_VISIBILIY_COMBO = "changeAllVisibiliyCombo"; //$NON-NLS-1$

  private Prefs() {
    // avoid instantiation
  }

  // FILTER --------------------------------------------------------------------
  static final String FILTER_SOURCE_FOLDER//
  = UCDetectorPlugin.ID + ".sourceFolderFilter"; //$NON-NLS-1$
  static final String FILTER_PACKAGE//
  = UCDetectorPlugin.ID + ".packageFilter"; //$NON-NLS-1$
  static final String FILTER_CLASS //
  = UCDetectorPlugin.ID + ".classFilter"; //$NON-NLS-1$
  static final String FILTER_METHOD //
  = UCDetectorPlugin.ID + ".methodFilter"; //$NON-NLS-1$
  static final String FILTER_FIELD //
  = UCDetectorPlugin.ID + ".fieldFilter"; //$NON-NLS-1$
  static final String FILTER_ANNOATIONS //
  = UCDetectorPlugin.ID + ".annotationsFilter"; //$NON-NLS-1$
  static final String FILTER_CONTAIN_STRING //
  = UCDetectorPlugin.ID + ".containString"; //$NON-NLS-1$
  static final String FILTER_BEAN_METHOD //
  = UCDetectorPlugin.ID + ".beanMethodFilter"; //$NON-NLS-1$
  static final String DETECT_TEST_ONLY //
  = UCDetectorPlugin.ID + ".detectTestOnly"; //$NON-NLS-1$
  // ANALYZE -------------------------------------------------------------------
  static final String ANALYZE_CLASSES //
  = UCDetectorPlugin.ID + ".classes"; //$NON-NLS-1$
  static final String ANALYZE_MEHTODS //
  = UCDetectorPlugin.ID + ".methods"; //$NON-NLS-1$
  static final String ANALYZE_FIELDS //
  = UCDetectorPlugin.ID + ".member"; //$NON-NLS-1$
  static final String ANALYZE_LITERALS_CHECK //
  = UCDetectorPlugin.ID + ".literalsCheck"; //$NON-NLS-1$
  static final String ANALYZE_CHECK_FULL_CLASS_NAME //
  = UCDetectorPlugin.ID + ".checkFullClassName"; //$NON-NLS-1$
  static final String ANALYZE_LITERALS //
  = UCDetectorPlugin.ID + ".literals"; //$NON-NLS-1$
  // WARN_LIMIT ----------------------------------------------------------------
  static final String WARN_LIMIT //
  = UCDetectorPlugin.ID + ".warnLimit"; //$NON-NLS-1$
  // KEYWORD -------------------------------------------------------------------
  /*
  Feature Requests ID: 2490344:
  */
  static final String ANALYZE_VISIBILITY_PROTECTED_CLASSES //
  = UCDetectorPlugin.ID + ".visibility.protected.classes"; //$NON-NLS-1$
  static final String ANALYZE_VISIBILITY_PRIVATE_CLASSES //
  = UCDetectorPlugin.ID + ".visibility.private.classes"; //$NON-NLS-1$
  //
  static final String ANALYZE_VISIBILITY_PROTECTED_METHODS //
  = UCDetectorPlugin.ID + ".visibility.protected.methods"; //$NON-NLS-1$
  static final String ANALYZE_VISIBILITY_PRIVATE_METHODS //
  = UCDetectorPlugin.ID + ".visibility.private.methods"; //$NON-NLS-1$
  //
  static final String ANALYZE_VISIBILITY_PROTECTED_FIELDS //
  = UCDetectorPlugin.ID + ".visibility.protected.fields"; //$NON-NLS-1$
  static final String ANALYZE_VISIBILITY_PRIVATE_FIELDS //
  = UCDetectorPlugin.ID + ".visibility.private.fields"; //$NON-NLS-1$
  static final String IGNORE_SYNTHETIC_ACCESS_EMULATION //
  = UCDetectorPlugin.ID + ".visibility.ignore.synthetic.access.emulation"; //$NON-NLS-1$
  //
  static final String ANALYZE_VISIBILITY_PROTECTED_CONSTANTS //
  = UCDetectorPlugin.ID + ".visibility.protected.constants"; //$NON-NLS-1$
  static final String ANALYZE_VISIBILITY_PRIVATE_CONSTANTS //
  = UCDetectorPlugin.ID + ".visibility.private.constants"; //$NON-NLS-1$
  //

  static final String ANALYZE_FINAL_FIELD //
  = UCDetectorPlugin.ID + ".finalField"; //$NON-NLS-1$
  static final String ANALYZE_FINAL_METHOD //
  = UCDetectorPlugin.ID + ".finalMethod"; //$NON-NLS-1$
  // REPORT --------------------------------------------------------------------
  static final String REPORT_FILE //
  = UCDetectorPlugin.ID + ".reportFile"; //$NON-NLS-1$
  // ---------------------------------------------------------------------------
  private static final String[] EMPTY_ARRAY = new String[0];
  private static IPreferenceStore store_FOR_TEST;
  /**
   * Separator used in text fields, which contain lists. Value is ","
   */
  //private static final String LIST_SEPARATOR = "\\s*,\\s*";
  private static final String LIST_SEPARATOR = ","; //$NON-NLS-1$

  // FILTER GROUP --------------------------------------------------------------
  /**
   * @param root package fragment root (source folder), which should be checked for filtering
   * @return <code>true</code>, when the IPackageFragmentRoot
   * matches the source folder filter
   */
  public static boolean filterPackageFragmentRoot(IPackageFragmentRoot root) {
    String sourceFolder = JavaElementUtil.getSourceFolderProjectRelativePath(root);
    return sourceFolder == null || matchFilter(FILTER_SOURCE_FOLDER, sourceFolder);
  }

  /**
   * @param packageFragment package, which should be checked for filtering
   * @return <code>true</code>, when the IPackageFragment
   * matches the package filter
   */
  public static boolean filterPackage(IPackageFragment packageFragment) {
    return matchFilter(FILTER_PACKAGE, packageFragment.getElementName());
  }

  /**
   * @param type type (class), which should be checked for filtering
   * @return <code>true</code>, when the type
   * matches the type filter
   */
  public static boolean filterType(IType type) {
    return matchFilter(FILTER_CLASS, type.getElementName());
  }

  /**
   * @return <code>true</code>, when bean methods should be filtered
   */
  public static boolean isFilterBeanMethod() {
    return getStore().getBoolean(FILTER_BEAN_METHOD);
  }

  /**
   * @return <code>true</code>, when we don't care about warning 'Access to enclosing type'
   * see: compiler warnings: Code style, 'access to a non-accessible member of an enclosing type'
   */
  public static boolean isIgnoreSyntheticAccessEmulationWarning() {
    return getStore().getBoolean(IGNORE_SYNTHETIC_ACCESS_EMULATION);
  }

  /**
   * @return <code>true</code>, when code is references only by test code
   */
  public static boolean isDetectTestOnly() {
    return getStore().getBoolean(DETECT_TEST_ONLY);
  }

  /**
   * @param method  which should be checked for reduce visibility
   * @return <code>true</code>, when the method
   * matches the method filter
   */
  public static boolean filterMethod(IMethod method) {
    return matchFilter(FILTER_METHOD, method.getElementName());
  }

  /**
   * @param field field which should be checked for filtering
   * @return <code>true</code>, when the field
   * matches the field filter
   */
  public static boolean filterField(IField field) {
    return matchFilter(FILTER_FIELD, field.getElementName());
  }

  /**
   * @param annotation which should be checked for filtering
   * @return <code>true</code>, when the annotation matches the field filter
   */
  // [ 2832790 ] Custom annotation filter
  public static boolean filterAnnotation(String annotation) {
    return matchFilter(FILTER_ANNOATIONS, annotation);
  }

  /**
   * @return <code>true</code>, when this filter is active
   */
  public static boolean isFilterClassContainingString() {
    String[] strings = getStrings(FILTER_CONTAIN_STRING);
    for (String string : strings) {
      if (string.trim().length() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param classAsString Class text content as a String
   * @return <code>true</code>, the class contains one of the strings
   */
  public static boolean filterClassContainingString(String classAsString) {
    String[] stringsToFindInFile = getStrings(FILTER_CONTAIN_STRING);
    for (String stringToFindInFile : stringsToFindInFile) {
      if (stringToFindInFile.trim().length() > 0) {
        if (classAsString != null && classAsString.contains(stringToFindInFile)) {
          return true;
        }
      }
    }
    return false;
  }

  // DETECT GROUP --------------------------------------------------------------
  // CLASSES --------------------------
  /**
   * @return WarnLevel for unnecessary code in classes
   */
  public static WarnLevel getUCDetectionInClasses() {
    return WarnLevel.valueOf(getString(ANALYZE_CLASSES));
  }

  /**
   * Don't use this method as filter!
   * See bug [ 2103655 ] Detect cycles does not show anything)
   * @return <code>true</code> if we should detect unnecessary code in classes
   */
  public static boolean isUCDetectionInClasses() {
    return !WarnLevel.IGNORE.equals(getUCDetectionInClasses());
  }

  // METHODS --------------------------
  /**
   * @return WarnLevel for unnecessary code in methods
   */
  public static WarnLevel getUCDetectionInMethods() {
    return WarnLevel.valueOf(getString(ANALYZE_MEHTODS));
  }

  /**
   * Don't use this method as filter!
   * See bug [ 2103655 ] Detect cycles does not show anything)
   * @return <code>true</code> if we should detect unnecessary code in methods
   */
  public static boolean isUCDetectionInMethods() {
    return !WarnLevel.IGNORE.equals(getUCDetectionInMethods());
  }

  // FIELDS ----------------------------
  /**
   * @return WarnLevel for unnecessary code in fields
   */
  public static WarnLevel getUCDetectionInFields() {
    return WarnLevel.valueOf(getString(ANALYZE_FIELDS));
  }

  /**
   * Don't use this method as filter!
   * See bug [ 2103655 ] Detect cycles does not show anything)
   * @return <code>true</code> if we should detect unnecessary code in fields
   */
  public static boolean isUCDetectionInFields() {
    return !WarnLevel.IGNORE.equals(getUCDetectionInFields());
  }

  // LITERALS ----------------------------
  /**
   * @return File pattern to search like: "*.java,*.xml"
   */
  public static String[] getFilePatternLiteralSearch() {
    if (isUCDetectionInLiterals()) {
      return getStrings(ANALYZE_LITERALS);
    }
    return EMPTY_ARRAY;
  }

  /**
   * @return <code>true</code> if we should detect class names in literals as well
   */
  public static boolean isUCDetectionInLiterals() {
    return getStore().getBoolean(ANALYZE_LITERALS_CHECK) && getString(ANALYZE_LITERALS).length() > 0;
  }

  /**
   * @return <code>true</code> if we should detect FULL class names in literals as well
   */
  public static boolean isUCDetectionInLiteralsFullClassName() {
    return getStore().getBoolean(ANALYZE_CHECK_FULL_CLASS_NAME) && getString(ANALYZE_LITERALS).length() > 0;
  }

  // WARN GROUP ----------------------------------------------------------------
  /**
   * @return number of references, which are necessary to create a marker
   */
  public static int getWarnLimit() {
    int warnLimit = getStore().getInt(WARN_LIMIT);
    return warnLimit < 0 ? 0 : warnLimit;
  }

  // KEYWORD GROUP -------------------------------------------------------------
  // VISIBILITY PROTECTED -----------------------
  /**
   * @param member which should be checked for reduce visibility
   * @return WarnLevel if we can use protected
   */
  public static WarnLevel getCheckReduceVisibilityProtected(IJavaElement member) {
    if (member instanceof IType) {
      return WarnLevel.valueOf(getString(ANALYZE_VISIBILITY_PROTECTED_CLASSES));
    }
    if (member instanceof IMethod) {
      return WarnLevel.valueOf(getString(ANALYZE_VISIBILITY_PROTECTED_METHODS));
    }
    if (member instanceof IField) {
      IField field = (IField) member;
      if (isConstant(field)) {
        return WarnLevel.valueOf(getString(ANALYZE_VISIBILITY_PROTECTED_CONSTANTS));

      }
      return WarnLevel.valueOf(getString(ANALYZE_VISIBILITY_PROTECTED_FIELDS));
    }
    // Text search: member == null, initializer
    return WarnLevel.WARNING;
  }

  /**
   * @param member  which should be checked for reduce visibility
   * @return <code>true</code> if we can use protected
   */
  public static boolean isCheckReduceVisibilityProtected(IJavaElement member) {
    return !WarnLevel.IGNORE.equals(getCheckReduceVisibilityProtected(member));
  }

  // VISIBILITY PRIVATE -----------------------
  /**
   * @param member  which should be checked for reduce visibility
   * @return WarnLevel if we can use private
   */
  public static WarnLevel getCheckReduceVisibilityToPrivate(IJavaElement member) {
    if (member instanceof IType) {
      return WarnLevel.valueOf(getString(ANALYZE_VISIBILITY_PRIVATE_CLASSES));
    }
    if (member instanceof IMethod) {
      return WarnLevel.valueOf(getString(ANALYZE_VISIBILITY_PRIVATE_METHODS));
    }
    if (member instanceof IField) {
      IField field = (IField) member;
      if (isConstant(field)) {
        return WarnLevel.valueOf(getString(ANALYZE_VISIBILITY_PRIVATE_CONSTANTS));

      }
      return WarnLevel.valueOf(getString(ANALYZE_VISIBILITY_PRIVATE_FIELDS));
    }
    // Text search: member == null, initializer
    return WarnLevel.WARNING;
  }

  /**
   * @param member  which should be checked for reduce visibility
   * @return <code>true</code> if we can use private
   */
  public static boolean isCheckReduceVisibilityToPrivate(IJavaElement member) {
    return !WarnLevel.IGNORE.equals(getCheckReduceVisibilityToPrivate(member));
  }

  private static boolean isConstant(IMember member) {
    try {
      return Flags.isStatic(member.getFlags()) && Flags.isFinal(member.getFlags());
    }
    catch (JavaModelException e) {
      Log.logError("Can't get isConstant: " + member, e); //$NON-NLS-1$
      return false;
    }
  }

  // FINAL FIELD -----------------------
  /**
   * @return WarnLevel if we can use final for fields
   */
  public static WarnLevel getCheckUseFinalField() {
    return WarnLevel.valueOf(getString(ANALYZE_FINAL_FIELD));
  }

  /**
   * @return <code>true</code> if we can use final for fields
   */
  public static boolean isCheckUseFinalField() {
    return !WarnLevel.IGNORE.equals(getCheckUseFinalField());
  }

  // FINAL METHOD -----------------------
  /**
   * @return WarnLevel if we can use final for methods
   */
  public static WarnLevel getCheckUseFinalMethod() {
    return WarnLevel.valueOf(getString(ANALYZE_FINAL_METHOD));
  }

  /**
   * @return <code>true</code> if we can use final for methods
   */
  public static boolean isCheckUseFinalMethod() {
    return !WarnLevel.IGNORE.equals(getCheckUseFinalMethod());
  }

  // REPORT --------------------------------------------------------------------
  /**
   * @return html report file
   */
  public static String getReportFile() {
    return getString(REPORT_FILE);
  }

  public static String getReportFolder() {
    String fileName = getReportFile();
    return fileName.length() == 0 ? null : new File(fileName).getParent();
  }

  /**
   * @return <code>true</code> if we want to create a html report file
   */
  public static boolean isWriteReportFile() {
    return getReportFile().length() > 0;
  }

  // ---------------------------------------------------------------------------
  // HELPER
  // ---------------------------------------------------------------------------
  @SuppressWarnings("ucd")
  protected static void setStore_FOR_TEST(IPreferenceStore store_FOR_TEST) {
    Prefs.store_FOR_TEST = store_FOR_TEST;
  }

  protected static IPreferenceStore getStore() {
    if (store_FOR_TEST != null) {
      return store_FOR_TEST;
    }
    IPreferenceStore store = UCDetectorPlugin.getDefault().getPreferenceStore();
    // PrefConverter.convert09to10(store);
    return store;
  }

  /**
   * @return never <code>null</code>. Returns "" instead!
   */
  private static String getString(String name) {
    return getStore().getString(name);
  }

  /**
   * @return array of strings splitted by {@link #LIST_SEPARATOR}
   */
  private static String[] getStrings(String name) {
    String[] split = getStore().getString(name).split(LIST_SEPARATOR);
    return split;
  }

  /**
   * @param name prefix "org.ucdetector." is added
   * @param value , which should changed in preference store
   */
  public static void setUcdValue(String name, String value) {
    getStore().setValue("org.ucdetector." + name, value); //$NON-NLS-1$
  }

  /**
   * @return <code>true</code>, when the name matches one of the Strings
   * found in the preference filteName.<br>
   */
  private static boolean matchFilter(String filterName, String name) {
    String[] filters = parseFilters(filterName);
    for (String regex : filters) {
      // IPackageFragmentRoot can be "", filter can be ""
      try {
        if (regex.length() > 0 && Pattern.matches(regex, name)) {
          return true;
        }
      }
      catch (PatternSyntaxException e) {
        Log.logWarn(e.getMessage());
      }
    }
    return false;
  }

  /**
   * @return an array, created splitting the filter text by LIST_SEPARATOR
   */
  private static String[] parseFilters(String filterName) {
    String[] strings = getStrings(filterName);
    String[] filters = new String[strings.length];
    for (int i = 0; i < strings.length; i++) {
      filters[i] = simpleRegexToJavaRegex(strings[i]);
    }
    return filters;
  }

  private static String simpleRegexToJavaRegex(String simpleRegex) {
    String regex = simpleRegex.replaceAll("\\*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
    return regex.replace("?", ".{1}"); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
