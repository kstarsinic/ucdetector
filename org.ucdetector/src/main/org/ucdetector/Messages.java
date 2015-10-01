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
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
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
      // Log.logError("Can't get string for field: " + name, ex);
      return defaultString;
    }
  }

  private Messages() {
    // avoid instantiation
  }

  // ACTIONS -----------------------------------------------------------------
  public static String AbstractUCDetectorAction_AnalyzeFailedText;
  public static String CountAction_ResultTitle;
  public static String UCDetectorAction_ResultMessage;
  public static String UCDetectorAction_ResultReport;
  // ITERATORS ---------------------------------------------------------------
  public static String UCDetectorIterator_JobName;
  public static String CleanMarkersIterator_JobName;
  public static String UCDetectorIterator_MONITOR_INFO;
  //  public static String UCDetectorPreferencePageKeywords_ChangeAllCombos;
  public static String AbstractUCDetectorIterator_NothingToDetectTitle;
  public static String AbstractUCDetectorIterator_NothingToDetect;
  // count
  public static String CountIterator_JobName;
  public static String CountIterator_NotPrivate;
  public static String CountIterator_Classes;
  public static String CountIterator_Fields;
  public static String CountIterator_Methods;
  public static String CountIterator_Packages;
  public static String CountIterator_Projects;

  // PreferencePage ----------------------------------------------------------
  public static String PreferencePage_ChangeVisibilityCombos;
  public static String PreferencePage_ReduceVisibiltyWarning;
  public static String PreferencePage_CheckProtectedClasses;
  public static String PreferencePage_CheckPrivateClasses;
  //
  public static String PreferencePage_CheckProtectedMethods;
  public static String PreferencePage_CheckPrivateMethods;
  //
  public static String PreferencePage_CheckProtectedFields;
  public static String PreferencePage_CheckPrivateFields;
  public static String PreferencePage_ignoreSyntheticAccessEmulation;
  public static String PreferencePage_ignoreSyntheticAccessEmulationTooltip;
  //
  public static String PreferencePage_CheckProtectedConstants;
  public static String PreferencePage_CheckPrivateConstants;
  // ------------------------------------------------------------------

  public static String PreferencePage_CheckFinalMethod;
  public static String PreferencePage_CheckFinalField;

  public static String PreferencePage_ComboToolTip;

  public static String PreferencePage_IgnoreResourcesGroup;
  public static String PreferencePage_IgnoreClassesGroup;
  public static String PreferencePage_IgnoreMarkedCodeGroup;
  public static String PreferencePage_IgnoreOthersGroup;

  public static String PreferencePage_IgnoreSourceFolderFilter;
  public static String PreferencePage_IgnoreSourceFolderFilterToolTip;
  public static String PreferencePage_IgnorePackageFilter;
  public static String PreferencePage_IgnorePackageFilterToolTip;
  public static String PreferencePage_IgnoreClassFilter;
  public static String PreferencePage_IgnoreClassFilterToolTip;
  public static String PreferencePage_IgnoreMethodFilter;
  public static String PreferencePage_IgnoreMethodFilterToolTip;
  public static String PreferencePage_IgnoreFieldFilter;
  public static String PreferencePage_IgnoreFieldFilterToolTip;
  public static String PreferencePage_IgnoreAnnotationsFilter;
  public static String PreferencePage_IgnoreImplements;
  public static String PreferencePage_IgnoreImplementsToolTip;
  public static String PreferencePage_IgnoreAnnotationsFilterToolTip;
  public static String PreferencePage_IgnoreContainString;
  public static String PreferencePage_IgnoreContainStringToolTip;
  public static String PreferencePage_FilterClassWithMainMethod;
  public static String PreferencePage_FilterClassWithMainMethodToolTip;
  public static String PreferencePage_IgnoreBeanMethods;
  public static String PreferencePage_IgnoreBeanMethodsToolTip;
  public static String PreferencePage_IgnoreDeprecated;
  public static String PreferencePage_IgnoreDeprecatedToolTip;
  public static String PreferencePage_IgnoreNoUcd;
  public static String PreferencePage_IgnoreNoUcdToolTip;
  public static String PreferencePage_IgnoreDerived;
  public static String PreferencePage_IgnoreDerivedToolTip;
  //
  public static String PreferencePage_Classes;
  public static String PreferencePage_Methods;
  public static String PreferencePage_Fields;
  public static String PreferencePage_DetectTestOnly;
  public static String PreferencePage_DetectTestOnlyToolTip;
  public static String PreferencePage_CheckFullClassName;
  public static String PreferencePage_CheckSimleClassName;
  public static String PreferencePage_CheckFullClassNameToolTip;
  public static String PreferencePage_CheckSimpleClassNameToolTip;
  public static String PreferencePage_LiteralsCheck;
  public static String PreferencePage_LiteralsCheckToolTip;
  public static String PreferencePage_Literals;
  public static String PreferencePage_LiteralsToolTip;
  public static String PreferencePage_WarnLimit;
  public static String PreferencePage_WarnLimitToolTip;
  //
  public static String PreferencePage_TabIgnore;
  public static String PreferencePage_TabDetect;
  public static String PreferencePage_TabKeywords;
  public static String PreferencePage_TabReport;
  public static String PreferencePage_TabOther;
  //
  public static String PreferencePage_CreateXmlReport;
  public static String PreferencePage_BrowseReportsDir;
  public static String PreferencePage_BrowseReportsDirToolTip;
  //
  public static String PreferencePage_LogLevel;
  public static String PreferencePage_LogLevelToolTip;
  //
  public static String PreferencePage_LogToEclipse;
  public static String PreferencePage_LogToEclipseToolTip;
  //
  public static String PreferencePage_GroupDetect;
  public static String PreferencePage_GroupFileSearch;
  public static String PreferencePage_GroupCycles;
  public static String PreferencePage_GroupReports;
  public static String PreferencePage_GroupLogging;

  public static String PreferencePage_GroupFinal;
  public static String PreferencePage_GroupVisibility;
  // -------------------------------------------------------------------------
  public static String WarnLevel_Error;
  public static String WarnLevel_Warning;
  public static String WarnLevel_Ignore;
  // -------------------------------------------------------------------------
  public static String PreferencePage_ReportDir;
  public static String PreferencePage_ReportDirToolTip;
  public static String PreferencePage_ReportFile;
  public static String PreferencePage_ReportFileToolTip;
  // SearchManager -----------------------------------------------------------
  //  public static String JavaElementUtil_Class;
  //  public static String JavaElementUtil_Constructor;
  //  public static String JavaElementUtil_Field;
  //  public static String JavaElementUtil_Constant;
  //  public static String JavaElementUtil_Method;
  //  public static String JavaElementUtil_Initializer;
  //
  public static String MarkerFactory_MarkerVisibility;
  public static String MarkerFactory_VisibilityCompileErrorForClass;
  public static String MarkerFactory_MarkerFinalMethod;
  public static String MarkerFactory_MarkerFinalField;
  public static String MarkerFactory_MarkerReference;
  public static String MarkerFactory_MarkerReferenceFieldNeverRead;
  public static String MarkerFactory_MarkerTestOnly;
  //
  public static String SearchManager_Monitor;
  public static String SearchManager_SearchReferences;
  public static String SearchManager_SearchClassNameAsLiteral;
  // XML -----------------------------------------------------------------------
  public static String XMLReport_WriteOk;
  public static String XMLReport_WriteNoWarnings;
  public static String XMLReport_WriteError;
  // QuickFix ------------------------------------------------------------------
  public static String UseFinalQuickFix_label;
  public static String DeleteCodeQuickFix_label;
  public static String DeleteFileQuickFix_label;
  public static String LineCommentQuickFix_label;
  public static String UseTag_NO_UCD_QuickFix_label;
  public static String UseAnnotation_UCD_QuickFix_label;
  public static String VisibilityQuickFix_label;
  public static String TodoQuickFix_label;

  public static String OutOfMemoryError_Hint; // NO_UCD

  // CYCLE  ------------------------------------------------------------------
  public static String CycleAction_cant_open_editor;
  public static String CycleIterator_JobName;
  public static String CycleIterator_MONITOR_INFO;
  public static String PreferencePage_MaxCycleSize;
  public static String PreferencePage_MaxCycleSizeToolTip;
  public static String Cycle_Name;
  public static String CycleRegion_Line;
  public static String CycleType_matches;
  public static String CycleType_match;
  // -------------------------------------------------------------------------
  public static String CycleView_popup_copy_clipboard;
  public static String CycleView_popup_open;
  public static String CycleView_popup_refresh;
  public static String CycleView_popup_remove;
  public static String CycleView_popup_rotate;
  // -------------------------------------------------------------------------
  public static String CycleSearchManager_Monitor;
  public static String CycleSearchManager_MonitorProject;
  public static String CycleSearchManager_Project_Info;
  // -------------------------------------------------------------------------
  public static String CycleCalculator_Monitor;
  public static String CycleCalculator_removeDoubleCycles;
  public static String CycleView_run_ucd_for_results;
  // REPORT --------------------------------------------------------------------
  public static String SearchResultRoot_Name;
  public static String SearchResult_get_text;
  // ModesPanel ---------------------------------------------------------------
  public static String ModesPanel_invalid_mode_name;
  //
  public static String ModesPanel_ModeLabel;
  public static String ModesPanel_ModeLabelToolTip;
  //
  public static String ModesPanel_ComboToolTipStart;
  // == new ==
  public static String ModesPanel_ModeNew;
  public static String ModesPanel_ModeNewToolTip;
  public static String ModesPanel_ModeNewTitle;
  // == remove ==
  public static String ModesPanel_ModeRemove;
  public static String ModesPanel_ModeRemoveToolTip;
  public static String ModesPanel_ModeRemoveQuestion;
  public static String ModesPanel_ModeRemoveTitle;
  // == rename ==
  public static String ModesPanel_ModeRename;
  public static String ModesPanel_ModeRenameToolTip;
  public static String ModesPanel_ModeRenameTitle;
  //
  public static String ModesPanel_ModePressNewHint;
  public static String ModesPanel_ModeAlreadyExists;
  //
  public static String ModesPanel_ModeName;
  public static String ModesPanel_CantSetPreferences; // NO_UCD
  public static String ModesPanel_ModeFileCantSave;
  // MODE STRINGS --------------------------------------------------------------
  @UsedBy("reflection")
  public static String ModesPanel_mode_Default;
  @UsedBy("reflection")
  public static String ModesPanel_mode_classes_only;
  @UsedBy("reflection")
  public static String ModesPanel_mode_unused_only;
  @UsedBy("reflection")
  public static String ModesPanel_mode_full;
  @UsedBy("reflection")
  public static String ModesPanel_mode_extreme;
}
