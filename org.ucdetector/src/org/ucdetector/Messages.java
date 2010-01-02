/**
 * Copyright (c) 2010 Joerg Spieler
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
public final class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.ucdetector.messages"; //$NON-NLS-1$
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
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
  public static String UCDetectorPreferencePageKeywords_ChangeAllCombos; // NO_UCD
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
  public static String PreferencePage_IgnoreAnnotationsFilterToolTip; // NO_UCD
  public static String PreferencePage_IgnoreContainString; // NO_UCD
  public static String PreferencePage_IgnoreContainStringToolTip; // NO_UCD
  public static String PreferencePage_IgnoreBeanMethods; // NO_UCD
  public static String PreferencePage_IgnoreBeanMethodsToolTip;// NO_UCD
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

}
