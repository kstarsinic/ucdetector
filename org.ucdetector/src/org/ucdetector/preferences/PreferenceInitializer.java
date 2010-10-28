/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import static org.ucdetector.preferences.WarnLevel.IGNORE;
import static org.ucdetector.preferences.WarnLevel.WARNING;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.ucdetector.Log;
import org.ucdetector.Log.LogLevel;
import org.ucdetector.UCDetectorPlugin;

/**
 * Class used to initialize default preference values.
 * See: {@link  org.ucdetector.preferences.Prefs#LIST_SEPARATOR}
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
 */
@SuppressWarnings("nls")
public class PreferenceInitializer extends AbstractPreferenceInitializer {
  private static final String WARN = WARNING.name();
  private static final String SOURCE_FOLDER_FILTER = "*test*,generated,";
  private static final String PACKAGE_FILTER = "*test*,";
  private static final String CLASS_FILTER = "Test*,*Test";
  private static final String METHOD_FILTER = "test*,*Test";
  private static final String FIELD_FILTER = "test*,*Test";
  private static final String ANNOATIONS_FILTER = "";
  // feature 2996537: Eclipse Plug-in/Extension detection: added MANIFEST.MF
  private static final String FILE_PATTERN_LITERAL_SEARCH = "*.xml,MANIFEST.MF,";
  private static final String REPORT_DEFAULT_DIR = "ucdetector_reports";
  public static final String FILE_NAME_REPLACE_NUMBER = "${number}";

  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = UCDetectorPlugin.getDefault().getPreferenceStore();
    // FILTER ------------------------------------------------------------------
    store.setDefault(Prefs.FILTER_SOURCE_FOLDER, SOURCE_FOLDER_FILTER);
    store.setDefault(Prefs.FILTER_PACKAGE, PACKAGE_FILTER);
    store.setDefault(Prefs.FILTER_CLASS, CLASS_FILTER);
    store.setDefault(Prefs.FILTER_METHOD, METHOD_FILTER);
    store.setDefault(Prefs.FILTER_FIELD, FIELD_FILTER);
    store.setDefault(Prefs.FILTER_ANNOATIONS, ANNOATIONS_FILTER);
    store.setDefault(Prefs.FILTER_IMPLEMENTS, "");
    store.setDefault(Prefs.FILTER_CONTAIN_STRING, "");
    store.setDefault(Prefs.FILTER_BEAN_METHOD, true);
    store.setDefault(Prefs.IGNORE_DEPRECATED, false);
    store.setDefault(Prefs.IGNORE_NO_UCD, true);
    // WHAT TO DETECT ----------------------------------------------------------
    store.setDefault(Prefs.WARN_LIMIT, 0);
    store.setDefault(Prefs.ANALYZE_CLASSES, WARN);
    store.setDefault(Prefs.ANALYZE_MEHTODS, WARN);
    store.setDefault(Prefs.ANALYZE_FIELDS, WARN);
    store.setDefault(Prefs.DETECT_TEST_ONLY, true);
    // CLASS NAMES IN FILES ----------------------------------------------------
    store.setDefault(Prefs.ANALYZE_LITERALS_CHECK, true);
    store.setDefault(Prefs.ANALYZE_CHECK_FULL_CLASS_NAME, true);
    store.setDefault(Prefs.ANALYZE_CHECK_SIMPLE_CLASS_NAME, false);
    store.setDefault(Prefs.ANALYZE_LITERALS, FILE_PATTERN_LITERAL_SEARCH);
    // CYCLE -------------------------------------------------------------------
    store.setDefault(Prefs.CYCLE_DEPTH, Prefs.CYCLE_DEPTH_DEFAULT);
    store.setDefault(Prefs.REPORT_DIR, getReportDirDefault());
    store.setDefault(Prefs.REPORT_FILE, "UCDetetorReport_${number}");
    store.setDefault(Prefs.REPORT_CREATE_HTML, true);
    store.setDefault(Prefs.REPORT_CREATE_XML, false);
    store.setDefault(Prefs.REPORT_CREATE_TXT, false);
    store.setDefault(Prefs.LOG_LEVEL, LogLevel.INFO.toString());
    store.setDefault(Prefs.LOG_TO_ECLIPSE, false);
    store.setDefault(Prefs.RUN_ON_SAVE, true);
    // KEYWORDS ----------------------------------------------------------------
    store.setDefault(Prefs.ANALYZE_FINAL_METHOD, IGNORE.name());
    store.setDefault(Prefs.ANALYZE_FINAL_FIELD, IGNORE.name());
    // VISIBILITY --------------------------------------------------------------
    //    store.setDefault(Prefs.CHANGE_ALL_VISIBILIY_COMBO, WarnLevel.WARNING.ordinal());
    // methods
    store.setDefault(Prefs.ANALYZE_VISIBILITY_PROTECTED_CLASSES, WARN);
    store.setDefault(Prefs.ANALYZE_VISIBILITY_PRIVATE_CLASSES, WARN);
    // classes
    store.setDefault(Prefs.ANALYZE_VISIBILITY_PROTECTED_METHODS, WARN);
    store.setDefault(Prefs.ANALYZE_VISIBILITY_PRIVATE_METHODS, WARN);
    // fields
    store.setDefault(Prefs.ANALYZE_VISIBILITY_PROTECTED_FIELDS, WARN);
    store.setDefault(Prefs.ANALYZE_VISIBILITY_PRIVATE_FIELDS, WARN);
    store.setDefault(Prefs.IGNORE_SYNTHETIC_ACCESS_EMULATION, true);
    // constants
    store.setDefault(Prefs.ANALYZE_VISIBILITY_PROTECTED_CONSTANTS, WARN);
    store.setDefault(Prefs.ANALYZE_VISIBILITY_PRIVATE_CONSTANTS, WARN);
    //
    store.setDefault(Prefs.MODE_NAME, ModesPanel.Mode.Default.toStringLocalized());
  }

  // REPORT --------------------------------------------------------------------
  /**
   * @return report directory
   */
  public static String getReportDir() {
    String dir = Prefs.getString(Prefs.REPORT_DIR);
    if (dir.length() == 0) {
      dir = getReportDirDefault();
    }
    File reportDir = new File(dir);
    reportDir.mkdirs();
    return reportDir.getAbsolutePath();
  }

  static String getReportDirDefault() {
    String reportDir;
    try {
      File workspaceDir = Platform.getLocation().toFile();
      reportDir = new File(workspaceDir, REPORT_DEFAULT_DIR).getCanonicalPath();
    }
    catch (Exception e) {
      reportDir = REPORT_DEFAULT_DIR;
      Log.error("Can't get report file name", e);
    }
    // Needed to avoid message: Path does not exists
    new File(reportDir).mkdirs();
    return reportDir;
  }
}
