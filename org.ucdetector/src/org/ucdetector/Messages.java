/*
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import org.eclipse.osgi.util.NLS;

/**
 * Eclipse l10n of this plugin
 */
public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.ucdetector.messages"; //$NON-NLS-1$
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }

  // ACTIONS -----------------------------------------------------------------
  public static String AbstractUCDetectorAction_AnalyzeFailedText; // NO_UCD
  public static String CountAction_ResultTitle; // NO_UCD
  public static String CycleAction_cant_open_editor; // NO_UCD

  // ITERATORS ---------------------------------------------------------------
  public static String UCDetectorIterator_JobName; // NO_UCD
  public static String CleanMarkersIterator_JobName; // NO_UCD
  // cycle
  public static String CycleIterator_JobName; // NO_UCD
  public static String CycleIterator_MONITOR_INFO; // NO_UCD

  public static String UCDetectorIterator_MONITOR_INFO; // NO_UCD
  public static String AbstractUCDetectorIterator_NothingToDetectTitle; // NO_UCD
  public static String AbstractUCDetectorIterator_NothingToDetect; // NO_UCD
  // count
  public static String CountIterator_JobName; // NO_UCD
  public static String CountIterator_NotPrivate; // NO_UCD
  public static String CountIterator_Classes; // NO_UCD
  public static String CountIterator_Fields; // NO_UCD
  public static String CountIterator_Methods; // NO_UCD
  public static String CountIterator_Packages; // NO_UCD
  public static String CountIterator_Projects; // NO_UCD
  public static String CountIterator_NothingFound; // NO_UCD

  // PreferencePage ----------------------------------------------------------
  public static String PreferencePage_CheckVisibilityProtected; // NO_UCD
  public static String PreferencePage_CheckVisibilityPrivate; // NO_UCD
  public static String PreferencePage_CheckFinalMethod; // NO_UCD
  public static String PreferencePage_CheckFinalField; // NO_UCD

  public static String PreferencePage_ComboToolTip; // NO_UCD

  public static String PreferencePage_IgnoreSourceFolderFilter; // NO_UCD
  public static String PreferencePage_IgnoreSourceFolderFilterToolTip; // NO_UCD
  public static String PreferencePage_IgnorePackageFilter; // NO_UCD
  public static String PreferencePage_IgnorePackageFilterToolTip; // NO_UCD
  public static String PreferencePage_IgnoreClassFilter; // NO_UCD
  public static String PreferencePage_IgnoreClassFilterToolTip; // NO_UCD
  public static String PreferencePage_IgnoreMethodFilter; // NO_UCD
  public static String PreferencePage_IgnoreMethodFilterToolTip; // NO_UCD
  public static String PreferencePage_IgnoreFieldFilter; // NO_UCD
  public static String PreferencePage_IgnoreFieldFilterToolTip; // NO_UCD
  public static String PreferencePage_IgnoreBeanMethods; // "Ignore bean methods",
  public static String PreferencePage_IgnoreBeanMethodsToolTip;

  public static String PreferencePage_Classes; // NO_UCD
  public static String PreferencePage_Methods; // NO_UCD
  public static String PreferencePage_Fields; // NO_UCD
  public static String PreferencePage_LiteralsCheck; // NO_UCD
  public static String PreferencePage_LiteralsCheckToolTip; // NO_UCD
  public static String PreferencePage_Literals; // NO_UCD
  public static String PreferencePage_LiteralsToolTip; // NO_UCD
  public static String PreferencePage_MaxCycleSize; // NO_UCD
  public static String PreferencePage_MaxCycleSizeToolTip; // NO_UCD
  public static String PreferencePage_WarnLimit; // NO_UCD
  public static String PreferencePage_WarnLimitToolTip; // NO_UCD

  public static String PreferencePage_GroupDetect; // NO_UCD
  public static String PreferencePage_GroupOthers; // NO_UCD
  public static String PreferencePage_GroupFilter; // NO_UCD
  public static String PreferencePage_GroupKeyWord; // NO_UCD
  // -------------------------------------------------------------------------
  public static String WarnLevel_Error; // NO_UCD
  public static String WarnLevel_Warning; // NO_UCD
  public static String WarnLevel_Ignore; // NO_UCD
  // -------------------------------------------------------------------------
  public static String PreferencePage_ReportFile; // NO_UCD
  public static String PreferencePage_ReportFileToolTip; // NO_UCD
  // SearchManager -----------------------------------------------------------
  public static String SearchManager_Class; // NO_UCD
  public static String SearchManager_Constructor; // NO_UCD
  public static String SearchManager_Field; // NO_UCD
  public static String SearchManager_Constant; // NO_UCD
  public static String SearchManager_Method; // NO_UCD
  public static String SearchManager_MarkerVisibility; // NO_UCD
  public static String SearchManager_MarkerFinalMethod; // NO_UCD
  public static String SearchManager_MarkerFinalField; // NO_UCD
  public static String SearchManager_MarkerReference; // NO_UCD
  public static String SearchManager_Monitor; // NO_UCD
  public static String SearchManager_SearchReferences; // NO_UCD
  public static String SearchManager_SearchClassNameAsLiteral; // NO_UCD
  // MODEL -------------------------------------------------------------------
  public static String SearchResultRoot_Name; // NO_UCD
  public static String SearchResult_get_text; // NO_UCD
  public static String Cycle_Name; // NO_UCD
  public static String CycleRegion_Line; // NO_UCD
  public static String CycleType_machtes; // NO_UCD
  public static String CycleType_match; // NO_UCD
  // -------------------------------------------------------------------------
  public static String CycleView_popup_copy_clipboard; // NO_UCD
  public static String CycleView_popup_open; // NO_UCD
  public static String CycleView_popup_refresh; // NO_UCD
  public static String CycleView_popup_remove; // NO_UCD
  public static String CycleView_popup_rotate; // NO_UCD
  // -------------------------------------------------------------------------
  public static String CycleSearchManager_Monitor; // NO_UCD
  public static String CycleSearchManager_MonitorProject; // NO_UCD
  public static String CycleSearchManager_OutOfMemoryError_Hint; // NO_UCD
  public static String CycleSearchManager_Project_Info; // NO_UCD
  // -------------------------------------------------------------------------
  public static String CycleCalculator_Monitor; // NO_UCD
  public static String CycleCalculator_removeDoubleCycles; // NO_UCD
  public static String CycleView_run_ucd_for_results;// NO_UCD
  // REPORT --------------------------------------------------------------------
  public static String XMLReportWriteOk; // NO_UCD
  public static String XMLReportWriteError; // NO_UCD
}
