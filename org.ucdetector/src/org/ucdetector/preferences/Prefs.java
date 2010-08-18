/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

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
import org.ucdetector.Log.LogLevel;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.util.JavaElementUtil;

/**
 * Constant definitions for plug-in preferences, offer access to preferences
 */
@SuppressWarnings("nls")
public final class Prefs {
  private static final String ID = UCDetectorPlugin.ID;

  private Prefs() {
    // avoid instantiation
  }

  // FILTER --------------------------------------------------------------------
  static final String FILTER_SOURCE_FOLDER = ID + ".sourceFolderFilter";
  static final String FILTER_PACKAGE = ID + ".packageFilter";
  static final String FILTER_CLASS = ID + ".classFilter";
  static final String FILTER_METHOD = ID + ".methodFilter";
  static final String FILTER_FIELD = ID + ".fieldFilter";
  static final String FILTER_ANNOATIONS = ID + ".annotationsFilter";
  static final String FILTER_IMPLEMENTS = ID + ".superClassFilter";
  static final String FILTER_CONTAIN_STRING = ID + ".containString";
  static final String FILTER_BEAN_METHOD = ID + ".beanMethodFilter";
  static final String IGNORE_DEPRECATED = ID + ".ignoreDeprecated";
  static final String IGNORE_NO_UCD = ID + ".ignore.NO_UCD";
  static final String DETECT_TEST_ONLY = ID + ".detectTestOnly";
  // ANALYZE -------------------------------------------------------------------
  static final String ANALYZE_CLASSES = ID + ".classes";
  static final String ANALYZE_MEHTODS = ID + ".methods";
  static final String ANALYZE_FIELDS = ID + ".member";
  static final String ANALYZE_LITERALS_CHECK = ID + ".literalsCheck";
  static final String ANALYZE_CHECK_FULL_CLASS_NAME = ID + ".checkFullClassName";
  static final String ANALYZE_CHECK_SIMPLE_CLASS_NAME = ID + ".checkSimpleClassName";
  static final String ANALYZE_LITERALS = ID + ".literals";
  // WARN_LIMIT ----------------------------------------------------------------
  static final String WARN_LIMIT = ID + ".warnLimit";
  // KEYWORD -------------------------------------------------------------------

  //  protected static final String CHANGE_ALL_VISIBILIY_COMBO = ID + ".changeAllVisibiliyCombo";
  /*  Feature Requests ID: 2490344: */
  static final String ANALYZE_VISIBILITY_PREFIX = ID + ".visibility";
  static final String ANALYZE_VISIBILITY_PROTECTED_CLASSES = ANALYZE_VISIBILITY_PREFIX + ".protected.classes";
  static final String ANALYZE_VISIBILITY_PRIVATE_CLASSES = ANALYZE_VISIBILITY_PREFIX + ".private.classes";
  //
  static final String ANALYZE_VISIBILITY_PROTECTED_METHODS = ANALYZE_VISIBILITY_PREFIX + ".protected.methods";
  static final String ANALYZE_VISIBILITY_PRIVATE_METHODS = ANALYZE_VISIBILITY_PREFIX + ".private.methods";
  //
  static final String ANALYZE_VISIBILITY_PROTECTED_FIELDS = ANALYZE_VISIBILITY_PREFIX + ".protected.fields";
  static final String ANALYZE_VISIBILITY_PRIVATE_FIELDS = ANALYZE_VISIBILITY_PREFIX + ".private.fields";
  static final String IGNORE_SYNTHETIC_ACCESS_EMULATION = ID + ".ignore.synthetic.access.emulation";
  //
  static final String ANALYZE_VISIBILITY_PROTECTED_CONSTANTS = ANALYZE_VISIBILITY_PREFIX + ".protected.constants";
  static final String ANALYZE_VISIBILITY_PRIVATE_CONSTANTS = ANALYZE_VISIBILITY_PREFIX + ".private.constants";
  //
  static final String ANALYZE_FINAL_FIELD = ID + ".finalField";
  static final String ANALYZE_FINAL_METHOD = ID + ".finalMethod";
  //
  static final String REPORT_DIR = ID + ".report.dir";
  static final String REPORT_CREATE_HTML = ID + ".report.create.html";
  static final String REPORT_CREATE_XML = ID + ".report.create.xml";
  static final String REPORT_CREATE_TXT = ID + ".report.create.txt";
  //
  public static final String LOG_LEVEL = ID + ".log.level";
  public static final String LOG_TO_ECLIPSE = ID + ".log.toEclipse";
  //
  public static final String INTERNAL = ID + ".internal";
  static final String MODE_NAME = INTERNAL + ".mode.name";
  static final String PREFS_VERSION = INTERNAL + ".version";
  // 
  private static final String[] EMPTY_ARRAY = new String[0];
  // CYCLE -------------------------------------------------------------------
  static final String CYCLE_DEPTH = ID + ".cycleDepth";
  static final int CYCLE_DEPTH_MIN = 2;
  static final int CYCLE_DEPTH_DEFAULT = 4;
  static final int CYCLE_DEPTH_MAX = 8;

  /** Separator used in text fields, which contain lists. Value is "," */
  private static final String LIST_SEPARATOR = ","; // "\\s*,\\s*";

  // FILTER GROUP --------------------------------------------------------------
  /**
   * @param root package fragment root (source folder), which should be checked for filtering
   * @return <code>true</code>, when the IPackageFragmentRoot
   * matches the source folder filter
   */
  public static boolean isFilterPackageFragmentRoot(IPackageFragmentRoot root) {
    String sourceFolder = JavaElementUtil.getSourceFolderProjectRelativePath(root);
    return sourceFolder == null || isMatchFilter(FILTER_SOURCE_FOLDER, sourceFolder);
  }

  /**
   * @param packageFragment package, which should be checked for filtering
   * @return <code>true</code>, when the IPackageFragment
   * matches the package filter
   */
  public static boolean isFilterPackage(IPackageFragment packageFragment) {
    return isMatchFilter(FILTER_PACKAGE, packageFragment.getElementName());
  }

  /**
   * @param type type (class), which should be checked for filtering
   * @return <code>true</code>, when the type
   * matches the type filter
   */
  public static boolean isFilterType(IType type) {
    return isMatchFilter(FILTER_CLASS, type.getElementName());
  }

  /**
   * @return <code>true</code>, when bean methods should be filtered
   */
  public static boolean isFilterBeanMethod() {
    return getStore().getBoolean(FILTER_BEAN_METHOD);
  }

  /**
   * @return <code>true</code>, when deprecated code should be filtered
   */
  public static boolean isFilterDeprecated() {
    return getStore().getBoolean(IGNORE_DEPRECATED);
  }

  /**
   * @return <code>true</code>, when comment lines containing "NO_UCD" code should be filtered
   */
  public static boolean isFilter_NO_UCD() {
    return getStore().getBoolean(IGNORE_NO_UCD);
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
  public static boolean isFilterMethod(IMethod method) {
    return isMatchFilter(FILTER_METHOD, method.getElementName());
  }

  /**
   * @param field field which should be checked for filtering
   * @return <code>true</code>, when the field
   * matches the field filter
   */
  public static boolean isFilterField(IField field) {
    return isMatchFilter(FILTER_FIELD, field.getElementName());
  }

  /**
   * @param annotation which should be checked for filtering
   * @return <code>true</code>, when the annotation matches the field filter
   */
  // [ 2832790 ] Custom annotation filter
  public static boolean isFilterAnnotation(String annotation) {
    return isMatchFilter(FILTER_ANNOATIONS, annotation);
  }

  /**
   * @param className which should be checked for filtering
   * @return <code>true</code>, when the className matches the field filter
   */
  // [ 2929828 ] Filter to exclude classes extending/implementing
  public static boolean isFilterImplements(String className) {
    return isMatchFilter(FILTER_IMPLEMENTS, className);
  }

  public static boolean isFilterImplements() {
    return getStore().getString(FILTER_IMPLEMENTS).trim().length() > 0;
  }

  /**
   * @return <code>true</code>, when this filter is active
   */
  public static boolean isFilterClassContainingString() {
    String[] strings = getStrings(FILTER_CONTAIN_STRING, true);
    for (String string : strings) {
      if (string.length() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param classAsString Class text content as a String
   * @return <code>true</code>, the class contains one of the strings
   */
  public static boolean isFilterClassContainingString(String classAsString) {
    String[] stringsToFindInFile = getStrings(FILTER_CONTAIN_STRING, false);
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
    return WarnLevel.IGNORE != getUCDetectionInClasses();
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
    return WarnLevel.IGNORE != getUCDetectionInMethods();
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
    return WarnLevel.IGNORE != getUCDetectionInFields();
  }

  // LITERALS ----------------------------
  /**
   * @return File pattern to search like: "*.xml,MANIFEST.MF,"
   */
  public static String[] getFilePatternLiteralSearch() {
    if (isUCDetectionInLiterals()) {
      return getStrings(ANALYZE_LITERALS, true);
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

  /**
   * @return <code>true</code> if we should detect SIMPLE class names in literals as well
   */
  public static boolean isUCDetectionInLiteralsSimpleClassName() {
    return getStore().getBoolean(ANALYZE_CHECK_SIMPLE_CLASS_NAME) && getString(ANALYZE_LITERALS).length() > 0;
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
    return WarnLevel.IGNORE != getCheckReduceVisibilityProtected(member);
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
    return WarnLevel.IGNORE != getCheckReduceVisibilityToPrivate(member);
  }

  private static boolean isConstant(IMember member) {
    try {
      return Flags.isStatic(member.getFlags()) && Flags.isFinal(member.getFlags());
    }
    catch (JavaModelException e) {
      Log.logError("Can't get isConstant for: " + member, e);
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
    return WarnLevel.IGNORE != getCheckUseFinalField();
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
    return WarnLevel.IGNORE != getCheckUseFinalMethod();
  }

  public static boolean isCreateReportHTML() {
    return getStore().getBoolean(REPORT_CREATE_HTML);
  }

  public static boolean isCreateReportXML() {
    return getStore().getBoolean(REPORT_CREATE_XML);
  }

  public static boolean isCreateReportTXT() {
    return getStore().getBoolean(REPORT_CREATE_TXT);
  }

  public static LogLevel getLogLevel() {
    return LogLevel.valueOf(getString(LOG_LEVEL));
  }

  public static boolean isLogToEclipse() {
    return getStore().getBoolean(LOG_TO_ECLIPSE);
  }

  public static String getModeName() {
    return getStore().getString(MODE_NAME);
  }

  /**
   * @return <code>true</code> if we want to create a report file
   */
  public static boolean isWriteReportFile() {
    return isCreateReportHTML() || isCreateReportXML() || isCreateReportTXT();
  }

  // ---------------------------------------------------------------------------
  // HELPER
  // ---------------------------------------------------------------------------

  protected static IPreferenceStore getStore() {
    return UCDetectorPlugin.getDefault().getPreferenceStore();
  }

  /**
   * @return never <code>null</code>. Returns "" instead!
   */
  protected static String getString(String name) {
    return getStore().getString(name);
  }

  /**
   * @return array of strings splitted by {@link #LIST_SEPARATOR}, strings maybe trimmed
   */
  private static String[] getStrings(String name, boolean trim) {
    String[] strings = getStore().getString(name).split(LIST_SEPARATOR);
    if (trim) {
      // Bugs 2996965: Property File name pattern to search
      for (int i = 0; i < strings.length; i++) {
        strings[i] = strings[i].trim();
      }
    }
    return strings;
  }

  /**
   * @param name name of preference
   * @param value , which should changed in preference store
   */
  public static void setValue(String name, String value) {
    getStore().setValue(name, value);
  }

  /**
   * @return <code>true</code>, when the name matches one of the Strings
   * found in the preference filteName.<br>
   */
  private static boolean isMatchFilter(String filterName, String name) {
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
    String[] strings = getStrings(filterName, true);
    String[] filters = new String[strings.length];
    for (int i = 0; i < strings.length; i++) {
      filters[i] = simpleRegexToJavaRegex(strings[i]);
    }
    return filters;
  }

  private static String simpleRegexToJavaRegex(String simpleRegex) {
    String regex = simpleRegex.replaceAll("\\*", ".*");
    return regex.replace("?", ".{1}");
  }

  /**
   * @return Maximum depth of searching for class cycles
   */
  public static int getCycleDepth() {
    int cycleDepth = getStore().getInt(CYCLE_DEPTH);
    return cycleDepth < CYCLE_DEPTH_MIN ? CYCLE_DEPTH_MIN : cycleDepth > CYCLE_DEPTH_MAX ? CYCLE_DEPTH_MAX : cycleDepth;
  }
}
