/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;
import org.ucdetector.util.UsedBy;

/**
 * Eclipse l10n of this plugin
 */
@SuppressWarnings("nls")
public final class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.ucdetector.messages";
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  /**
   * This method uses reflection to get string stored in static field!
   * @param name java name of the static field in this class
   * @param defaultString string to return, when field is not found
   * @return string found in messages.properties
   */
  public static String getString(String name, String defaultString) {
    try {
      Field field = Messages.class.getDeclaredField(name);
      return (String) field.get(null);
    }
    catch (Exception ex) {
      Log.logError("Can't get string for field: " + name, ex);
      return defaultString;
    }
  }

  private Messages() {
    // avoid instantiation
  }

  // ACTIONS -----------------------------------------------------------------
  public static String AbstractUCDetectorAction_AnalyzeFailedText; // NO_UCD
  public static String CountAction_ResultTitle; // NO_UCD
  public static String UCDetectorAction_ResultMessage; // NO_UCD
  public static String UCDetectorAction_ResultReport; // NO_UCD
  // ITERATORS ---------------------------------------------------------------
  public static String UCDetectorIterator_JobName; // NO_UCD
  public static String CleanMarkersIterator_JobName; // NO_UCD
  public static String UCDetectorIterator_MONITOR_INFO; // NO_UCD
  //  public static String UCDetectorPreferencePageKeywords_ChangeAllCombos; // NO_UCD
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

  // PreferencePage ----------------------------------------------------------
  public static String PreferencePage_ReduceVisibiltyWarning;//  "Reducing visibility of classes or methods may cause compile errors"
  public static String PreferencePage_CheckProtectedClasses; // NO_UCD
  public static String PreferencePage_CheckPrivateClasses; // NO_UCD
  //
  public static String PreferencePage_CheckProtectedMethods; // NO_UCD
  public static String PreferencePage_CheckPrivateMethods; // NO_UCD
  //
  public static String PreferencePage_CheckProtectedFields; // NO_UCD
  public static String PreferencePage_CheckPrivateFields; // NO_UCD
  public static String PreferencePage_ignoreSyntheticAccessEmulation; // NO_UCD
  public static String PreferencePage_ignoreSyntheticAccessEmulationTooltip; // NO_UCD
  //
  public static String PreferencePage_CheckProtectedConstants; // NO_UCD
  public static String PreferencePage_CheckPrivateConstants; // NO_UCD
  // ------------------------------------------------------------------

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
  public static String PreferencePage_IgnoreAnnotationsFilter; // NO_UCD
  public static String PreferencePage_IgnoreImplements; // NO_UCD
  public static String PreferencePage_IgnoreImplementsToolTip; // NO_UCD
  public static String PreferencePage_IgnoreAnnotationsFilterToolTip; // NO_UCD
  public static String PreferencePage_IgnoreContainString; // NO_UCD
  public static String PreferencePage_IgnoreContainStringToolTip; // NO_UCD
  public static String PreferencePage_IgnoreBeanMethods; // NO_UCD
  public static String PreferencePage_IgnoreBeanMethodsToolTip;// NO_UCD
  public static String PreferencePage_IgnoreDeprecated; // NO_UCD
  public static String PreferencePage_IgnoreDeprecatedToolTip; // NO_UCD
  public static String PreferencePage_IgnoreNoUcd; // NO_UCD
  public static String PreferencePage_IgnoreNoUcdToolTip; // NO_UCD
  //
  public static String PreferencePage_Classes; // NO_UCD
  public static String PreferencePage_Methods; // NO_UCD
  public static String PreferencePage_Fields; // NO_UCD
  public static String PreferencePage_DetectTestOnly; // NO_UCD
  public static String PreferencePage_DetectTestOnlyToolTip; // NO_UCD
  public static String PreferencePage_CheckFullClassName; // NO_UCD
  public static String PreferencePage_CheckFullClassNameToolTip; // NO_UCD
  public static String PreferencePage_LiteralsCheck; // NO_UCD
  public static String PreferencePage_LiteralsCheckToolTip; // NO_UCD
  public static String PreferencePage_Literals; // NO_UCD
  public static String PreferencePage_LiteralsToolTip; // NO_UCD
  public static String PreferencePage_WarnLimit; // NO_UCD
  public static String PreferencePage_WarnLimitToolTip; // NO_UCD
  //
  public static String PreferencePage_TabFilter; // NO_UCD
  public static String PreferencePage_TabDetect; // NO_UCD
  public static String PreferencePage_TabKeywords; // NO_UCD
  public static String PreferencePage_TabReport; // NO_UCD
  //
  public static String PreferencePage_ModeLabel; // NO_UCD
  public static String PreferencePage_ModeSave; // NO_UCD
  public static String PreferencePage_ModeNew; // NO_UCD
  public static String PreferencePage_ModeRemove; // NO_UCD
  //
  public static String PreferencePage_NewMode; // NO_UCD
  public static String PreferencePage_ModeName; // NO_UCD
  public static String PreferencePage_CantSetPreferences; // NO_UCD
  public static String PreferencePage_ModeFileCantSave; // NO_UCD
  //
  public static String PreferencePage_CreateHtmlReport; // NO_UCD
  public static String PreferencePage_CreateXmlReport; // NO_UCD
  public static String PreferencePage_CreateTextReport; // NO_UCD

  @UsedBy("reflection")
  public static String PrefMode_classes_only;// NO_UCD
  @UsedBy("reflection")
  public static String PrefMode_full;// NO_UCD
  @UsedBy("reflection")
  public static String PrefMode_Default;// NO_UCD

  //
  public static String PreferencePage_GroupDetect; // NO_UCD
  public static String PreferencePage_GroupFileSearch; // NO_UCD
  public static String PreferencePage_GroupOthers; // NO_UCD
  public static String PreferencePage_GroupFilter; // NO_UCD
  public static String PreferencePage_GroupFinal; // NO_UCD
  public static String PreferencePage_GroupVisibility; // NO_UCD
  // -------------------------------------------------------------------------
  public static String WarnLevel_Error; // NO_UCD
  public static String WarnLevel_Warning; // NO_UCD
  public static String WarnLevel_Ignore; // NO_UCD
  // -------------------------------------------------------------------------
  public static String PreferencePage_ReportFile; // NO_UCD
  public static String PreferencePage_ReportFileToolTip; // NO_UCD
  // SearchManager -----------------------------------------------------------
  public static String JavaElementUtil_Class; // NO_UCD
  public static String JavaElementUtil_Constructor; // NO_UCD
  public static String JavaElementUtil_Field; // NO_UCD
  public static String JavaElementUtil_Constant; // NO_UCD
  public static String JavaElementUtil_Method; // NO_UCD
  public static String JavaElementUtil_Initializer; // NO_UCD
  //
  public static String MarkerFactory_MarkerVisibility; // NO_UCD
  public static String MarkerFactory_VisibilityCompileErrorForClass; // NO_UCD
  public static String MarkerFactory_MarkerFinalMethod; // NO_UCD
  public static String MarkerFactory_MarkerFinalField; // NO_UCD
  public static String MarkerFactory_MarkerReference; // NO_UCD
  public static String MarkerFactory_MarkerReferenceFieldNeverRead; // NO_UCD
  public static String MarkerFactory_MarkerTestOnly; // NO_UCD
  //
  public static String SearchManager_Monitor; // NO_UCD
  public static String SearchManager_SearchReferences; // NO_UCD
  public static String SearchManager_SearchClassNameAsLiteral; // NO_UCD
  // XML -----------------------------------------------------------------------
  public static String XMLReport_WriteOk; // NO_UCD
  public static String XMLReport_WriteNoWarnings; // NO_UCD
  public static String XMLReport_WriteError; // NO_UCD
  // QuickFix ------------------------------------------------------------------
  public static String UseFinalQuickFix_label;// NO_UCD
  public static String DeleteQuickFix_label;// NO_UCD
  public static String LineCommentQuickFix_label;// NO_UCD
  public static String UseTag_NO_UCD_QuickFix_label; // NO_UCD
  public static String UseAnnotation_UCD_QuickFix_label; // NO_UCD
  public static String VisibilityQuickFix_label; // NO_UCD
  public static String TodoQuickFix_label; // NO_UCD

  public static String OutOfMemoryError_Hint; // NO_UCD

  // CYLCE  ------------------------------------------------------------------
  public static String CycleAction_cant_open_editor; // NO_UCD
  public static String CycleIterator_JobName; // NO_UCD
  public static String CycleIterator_MONITOR_INFO; // NO_UCD
  public static String PreferencePage_MaxCycleSize; // NO_UCD
  public static String PreferencePage_MaxCycleSizeToolTip; // NO_UCD
  public static String Cycle_Name; // NO_UCD
  public static String CycleRegion_Line; // NO_UCD
  public static String CycleType_matches; // NO_UCD
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
  public static String CycleSearchManager_Project_Info; // NO_UCD
  // -------------------------------------------------------------------------
  public static String CycleCalculator_Monitor; // NO_UCD
  public static String CycleCalculator_removeDoubleCycles; // NO_UCD
  public static String CycleView_run_ucd_for_results;// NO_UCD
  // REPORT --------------------------------------------------------------------
  public static String SearchResultRoot_Name; // NO_UCD
  public static String SearchResult_get_text; // NO_UCD
  // ModesPanel ---------------------------------------------------------------
  public static String ModesPanel_invalid_mode_name; // NO_UCD
}
