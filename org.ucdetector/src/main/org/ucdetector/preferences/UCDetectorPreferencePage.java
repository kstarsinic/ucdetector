/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import static org.ucdetector.Messages.PreferencePage_BrowseReportsDir;
import static org.ucdetector.Messages.PreferencePage_BrowseReportsDirToolTip;
import static org.ucdetector.Messages.PreferencePage_ChangeVisibilityCombos;
import static org.ucdetector.Messages.PreferencePage_CheckFinalField;
import static org.ucdetector.Messages.PreferencePage_CheckFinalMethod;
import static org.ucdetector.Messages.PreferencePage_CheckFullClassName;
import static org.ucdetector.Messages.PreferencePage_CheckFullClassNameToolTip;
import static org.ucdetector.Messages.PreferencePage_CheckPrivateClasses;
import static org.ucdetector.Messages.PreferencePage_CheckPrivateConstants;
import static org.ucdetector.Messages.PreferencePage_CheckPrivateFields;
import static org.ucdetector.Messages.PreferencePage_CheckPrivateMethods;
import static org.ucdetector.Messages.PreferencePage_CheckProtectedClasses;
import static org.ucdetector.Messages.PreferencePage_CheckProtectedConstants;
import static org.ucdetector.Messages.PreferencePage_CheckProtectedFields;
import static org.ucdetector.Messages.PreferencePage_CheckProtectedMethods;
import static org.ucdetector.Messages.PreferencePage_CheckSimleClassName;
import static org.ucdetector.Messages.PreferencePage_CheckSimpleClassNameToolTip;
import static org.ucdetector.Messages.PreferencePage_Classes;
import static org.ucdetector.Messages.PreferencePage_ComboToolTip;
import static org.ucdetector.Messages.PreferencePage_CreateXmlReport;
import static org.ucdetector.Messages.PreferencePage_DetectTestOnly;
import static org.ucdetector.Messages.PreferencePage_DetectTestOnlyToolTip;
import static org.ucdetector.Messages.PreferencePage_Fields;
import static org.ucdetector.Messages.PreferencePage_FilterClassWithMainMethod;
import static org.ucdetector.Messages.PreferencePage_FilterClassWithMainMethodToolTip;
import static org.ucdetector.Messages.PreferencePage_GroupCycles;
import static org.ucdetector.Messages.PreferencePage_GroupDetect;
import static org.ucdetector.Messages.PreferencePage_GroupFileSearch;
import static org.ucdetector.Messages.PreferencePage_GroupFinal;
import static org.ucdetector.Messages.PreferencePage_GroupLogging;
import static org.ucdetector.Messages.PreferencePage_GroupReports;
import static org.ucdetector.Messages.PreferencePage_GroupVisibility;
import static org.ucdetector.Messages.PreferencePage_IgnoreAnnotationsFilter;
import static org.ucdetector.Messages.PreferencePage_IgnoreAnnotationsFilterToolTip;
import static org.ucdetector.Messages.PreferencePage_IgnoreBeanMethods;
import static org.ucdetector.Messages.PreferencePage_IgnoreBeanMethodsToolTip;
import static org.ucdetector.Messages.PreferencePage_IgnoreClassFilter;
import static org.ucdetector.Messages.PreferencePage_IgnoreClassFilterToolTip;
import static org.ucdetector.Messages.PreferencePage_IgnoreClassesGroup;
import static org.ucdetector.Messages.PreferencePage_IgnoreContainString;
import static org.ucdetector.Messages.PreferencePage_IgnoreContainStringToolTip;
import static org.ucdetector.Messages.PreferencePage_IgnoreDeprecated;
import static org.ucdetector.Messages.PreferencePage_IgnoreDeprecatedToolTip;
import static org.ucdetector.Messages.PreferencePage_IgnoreDerived;
import static org.ucdetector.Messages.PreferencePage_IgnoreDerivedToolTip;
import static org.ucdetector.Messages.PreferencePage_IgnoreFieldFilter;
import static org.ucdetector.Messages.PreferencePage_IgnoreFieldFilterToolTip;
import static org.ucdetector.Messages.PreferencePage_IgnoreImplements;
import static org.ucdetector.Messages.PreferencePage_IgnoreImplementsToolTip;
import static org.ucdetector.Messages.PreferencePage_IgnoreMarkedCodeGroup;
import static org.ucdetector.Messages.PreferencePage_IgnoreMethodFilter;
import static org.ucdetector.Messages.PreferencePage_IgnoreMethodFilterToolTip;
import static org.ucdetector.Messages.PreferencePage_IgnoreNoUcd;
import static org.ucdetector.Messages.PreferencePage_IgnoreNoUcdToolTip;
import static org.ucdetector.Messages.PreferencePage_IgnoreOthersGroup;
import static org.ucdetector.Messages.PreferencePage_IgnorePackageFilter;
import static org.ucdetector.Messages.PreferencePage_IgnorePackageFilterToolTip;
import static org.ucdetector.Messages.PreferencePage_IgnoreResourcesGroup;
import static org.ucdetector.Messages.PreferencePage_IgnoreSourceFolderFilter;
import static org.ucdetector.Messages.PreferencePage_IgnoreSourceFolderFilterToolTip;
import static org.ucdetector.Messages.PreferencePage_Literals;
import static org.ucdetector.Messages.PreferencePage_LiteralsCheck;
import static org.ucdetector.Messages.PreferencePage_LiteralsCheckToolTip;
import static org.ucdetector.Messages.PreferencePage_LiteralsToolTip;
import static org.ucdetector.Messages.PreferencePage_LogLevel;
import static org.ucdetector.Messages.PreferencePage_LogLevelToolTip;
import static org.ucdetector.Messages.PreferencePage_LogToEclipse;
import static org.ucdetector.Messages.PreferencePage_LogToEclipseToolTip;
import static org.ucdetector.Messages.PreferencePage_MaxCycleSize;
import static org.ucdetector.Messages.PreferencePage_MaxCycleSizeToolTip;
import static org.ucdetector.Messages.PreferencePage_Methods;
import static org.ucdetector.Messages.PreferencePage_ReduceVisibiltyWarning;
import static org.ucdetector.Messages.PreferencePage_ReportDir;
import static org.ucdetector.Messages.PreferencePage_ReportDirToolTip;
import static org.ucdetector.Messages.PreferencePage_ReportFile;
import static org.ucdetector.Messages.PreferencePage_ReportFileToolTip;
import static org.ucdetector.Messages.PreferencePage_TabDetect;
import static org.ucdetector.Messages.PreferencePage_TabIgnore;
import static org.ucdetector.Messages.PreferencePage_TabKeywords;
import static org.ucdetector.Messages.PreferencePage_TabOther;
import static org.ucdetector.Messages.PreferencePage_TabReport;
import static org.ucdetector.Messages.PreferencePage_WarnLimit;
import static org.ucdetector.Messages.PreferencePage_WarnLimitToolTip;
import static org.ucdetector.Messages.PreferencePage_ignoreSyntheticAccessEmulation;
import static org.ucdetector.Messages.PreferencePage_ignoreSyntheticAccessEmulationTooltip;
import static org.ucdetector.preferences.Prefs.ANALYZE_CHECK_FULL_CLASS_NAME;
import static org.ucdetector.preferences.Prefs.ANALYZE_CHECK_SIMPLE_CLASS_NAME;
import static org.ucdetector.preferences.Prefs.ANALYZE_CLASSES;
import static org.ucdetector.preferences.Prefs.ANALYZE_FIELDS;
import static org.ucdetector.preferences.Prefs.ANALYZE_FINAL_FIELD;
import static org.ucdetector.preferences.Prefs.ANALYZE_FINAL_METHOD;
import static org.ucdetector.preferences.Prefs.ANALYZE_LITERALS;
import static org.ucdetector.preferences.Prefs.ANALYZE_LITERALS_CHECK;
import static org.ucdetector.preferences.Prefs.ANALYZE_MEHTODS;
import static org.ucdetector.preferences.Prefs.ANALYZE_VISIBILITY_PREFIX;
import static org.ucdetector.preferences.Prefs.ANALYZE_VISIBILITY_PRIVATE_CLASSES;
import static org.ucdetector.preferences.Prefs.ANALYZE_VISIBILITY_PRIVATE_CONSTANTS;
import static org.ucdetector.preferences.Prefs.ANALYZE_VISIBILITY_PRIVATE_FIELDS;
import static org.ucdetector.preferences.Prefs.ANALYZE_VISIBILITY_PRIVATE_METHODS;
import static org.ucdetector.preferences.Prefs.ANALYZE_VISIBILITY_PROTECTED_CLASSES;
import static org.ucdetector.preferences.Prefs.ANALYZE_VISIBILITY_PROTECTED_CONSTANTS;
import static org.ucdetector.preferences.Prefs.ANALYZE_VISIBILITY_PROTECTED_FIELDS;
import static org.ucdetector.preferences.Prefs.ANALYZE_VISIBILITY_PROTECTED_METHODS;
import static org.ucdetector.preferences.Prefs.CYCLE_DEPTH;
import static org.ucdetector.preferences.Prefs.CYCLE_DEPTH_MAX;
import static org.ucdetector.preferences.Prefs.CYCLE_DEPTH_MIN;
import static org.ucdetector.preferences.Prefs.DETECT_TEST_ONLY;
import static org.ucdetector.preferences.Prefs.FILTER_ANNOATIONS;
import static org.ucdetector.preferences.Prefs.FILTER_BEAN_METHOD;
import static org.ucdetector.preferences.Prefs.FILTER_CLASS;
import static org.ucdetector.preferences.Prefs.FILTER_CLASS_WITH_MAIN_METHOD;
import static org.ucdetector.preferences.Prefs.FILTER_CONTAIN_STRING;
import static org.ucdetector.preferences.Prefs.FILTER_FIELD;
import static org.ucdetector.preferences.Prefs.FILTER_IMPLEMENTS;
import static org.ucdetector.preferences.Prefs.FILTER_METHOD;
import static org.ucdetector.preferences.Prefs.FILTER_PACKAGE;
import static org.ucdetector.preferences.Prefs.FILTER_SOURCE_FOLDER;
import static org.ucdetector.preferences.Prefs.IGNORE_DEPRECATED;
import static org.ucdetector.preferences.Prefs.IGNORE_DERIVED;
import static org.ucdetector.preferences.Prefs.IGNORE_NO_UCD;
import static org.ucdetector.preferences.Prefs.IGNORE_SYNTHETIC_ACCESS_EMULATION;
import static org.ucdetector.preferences.Prefs.LOG_LEVEL;
import static org.ucdetector.preferences.Prefs.LOG_TO_ECLIPSE;
import static org.ucdetector.preferences.Prefs.MODE_NAME;
import static org.ucdetector.preferences.Prefs.REPORT_CREATE_XML;
import static org.ucdetector.preferences.Prefs.REPORT_DIR;
import static org.ucdetector.preferences.Prefs.REPORT_FILE;
import static org.ucdetector.preferences.Prefs.WARN_LIMIT;
import static org.ucdetector.preferences.Prefs.getReportStoreKey;
import static org.ucdetector.preferences.Prefs.getStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.ucdetector.Log;
import org.ucdetector.Log.LogLevel;
import org.ucdetector.UCDInfo;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.report.ReportExtension;
import org.ucdetector.report.ReportNameManager;

/**
 * Create the UCDetector preference page:<br>
 * Values are stored in property file:
 * <code>WORSPACE/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.ucdetector.prefs</code>
 * <p>
 * User specific modes are stored in: code>WORKSPACE/.metadata/.plugins/org.ucdetector/modes</code>
 * <p>
 * @see "http://www.eclipsepluginsite.com/preference-pages.html"
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
 */
public class UCDetectorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
  private static final String SEPARATOR = ":"; //$NON-NLS-1$
  protected final List<FieldEditor> fields = new ArrayList<>();
  /** Hack to enable/disable children of groups (=fields)  */
  protected final List<Composite> groups = new ArrayList<>();
  /** Contains group names, tab names, preference names */
  protected final List<String> extendedPreferences = new ArrayList<>();
  // @formatter:off
  protected static final String GROUP_START = "# "; //$NON-NLS-1$
  protected static final String TAB_START   = "## "; //$NON-NLS-1$
  /**
   * entryNames (first column) and values (second column) for the
   * ComboFieldEditor
   */
  private static final String[][] WARN_LEVELS = new String[][] {
    { WarnLevel.ERROR  .toStringLocalized(), WarnLevel.ERROR  .toString()},
    { WarnLevel.WARNING.toStringLocalized(), WarnLevel.WARNING.toString()},
    { WarnLevel.IGNORE .toStringLocalized(), WarnLevel.IGNORE .toString()},
  };
  private static final String[][] LOG_LEVELS = new String[][] {
    { LogLevel.DEBUG.toString(), LogLevel.DEBUG.toString()},
    { LogLevel.INFO .toString(), LogLevel.INFO .toString()},
    { LogLevel.WARN .toString(), LogLevel.WARN .toString()},
    { LogLevel.ERROR.toString(), LogLevel.ERROR.toString()},
    { LogLevel.OFF  .toString(), LogLevel.OFF  .toString()},
  };
  // @formatter:on
  private ModesPanel modesPanel;

  public UCDetectorPreferencePage() {
    super(FieldEditorPreferencePage.GRID);
    setPreferenceStore(getStore());
  }

  @Override
  public void init(IWorkbench workbench) {
    //
  }

  @Override
  public void createFieldEditors() {
    Composite parentGroups = createComposite(getFieldEditorParent(), 1, 1, GridData.FILL_BOTH);
    setTitle("UCDetector " + UCDInfo.getUCDVersion()); //$NON-NLS-1$
    modesPanel = new ModesPanel(this, parentGroups);
    tabFolder = new TabFolder(parentGroups, SWT.NONE);
    tabFolder.setLayoutData(createGridData(500, SWT.DEFAULT, SWT.FILL, SWT.CENTER, true, false));
    createTabIgnore();
    createTabDetect();
    createTabKeyworts();
    createTabReport();
    createTabOther();
    modesPanel.updateModeButtons();
    modesPanel.createMyMode();
  }

  private void createTabIgnore() {
    Composite tab = createTab(PreferencePage_TabIgnore);
    createIgnoreResourcesGroup(tab);
    createIgnoreClassesGroup(tab);
    createIgnoreMarkedCode(tab);
    createIgnoreOthers(tab);
  }

  private void createTabDetect() {
    Composite composite = createTab(PreferencePage_TabDetect);
    createDetectGroup(composite);
    createFileSearchGroup(composite);
    createCycleGroup(composite);
  }

  private void createTabKeyworts() {
    Composite composite = createTab(PreferencePage_TabKeywords);
    createFinalGroup(composite);
    createVisibilityGroup(composite);
  }

  private void createTabReport() {
    Composite composite = createTab(PreferencePage_TabReport);
    createReportGroup(composite);
  }

  private void createTabOther() {
    Composite composite = createTab(PreferencePage_TabOther);
    createOtherGroup(composite);
  }

  private void createIgnoreResourcesGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, PreferencePage_IgnoreResourcesGroup);
    appendText(FILTER_SOURCE_FOLDER, PreferencePage_IgnoreSourceFolderFilter,
        PreferencePage_IgnoreSourceFolderFilterToolTip, spacer);
    appendText(FILTER_PACKAGE, PreferencePage_IgnorePackageFilter, PreferencePage_IgnorePackageFilterToolTip, spacer);
  }

  private void createIgnoreClassesGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, PreferencePage_IgnoreClassesGroup);
    appendText(FILTER_CLASS, PreferencePage_IgnoreClassFilter, PreferencePage_IgnoreClassFilterToolTip, spacer);
    appendText(FILTER_IMPLEMENTS, PreferencePage_IgnoreImplements, PreferencePage_IgnoreImplementsToolTip, spacer);
    appendText(FILTER_CONTAIN_STRING, PreferencePage_IgnoreContainString, PreferencePage_IgnoreContainStringToolTip,
        spacer);
    appendBool(FILTER_CLASS_WITH_MAIN_METHOD, PreferencePage_FilterClassWithMainMethod,
        PreferencePage_FilterClassWithMainMethodToolTip, spacer, 2);
    appendBool(IGNORE_DERIVED, PreferencePage_IgnoreDerived, PreferencePage_IgnoreDerivedToolTip, spacer, 2);
  }

  private void createIgnoreMarkedCode(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, PreferencePage_IgnoreMarkedCodeGroup);
    appendText(FILTER_ANNOATIONS, PreferencePage_IgnoreAnnotationsFilter, PreferencePage_IgnoreAnnotationsFilterToolTip,
        spacer);
    appendBool(IGNORE_DEPRECATED, PreferencePage_IgnoreDeprecated, PreferencePage_IgnoreDeprecatedToolTip, spacer, 2);
    appendBool(IGNORE_NO_UCD, PreferencePage_IgnoreNoUcd, //
        PreferencePage_IgnoreNoUcdToolTip, spacer, 2);
  }

  private void createIgnoreOthers(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, PreferencePage_IgnoreOthersGroup);
    appendText(FILTER_FIELD, PreferencePage_IgnoreFieldFilter, PreferencePage_IgnoreFieldFilterToolTip, spacer);
    appendText(FILTER_METHOD, PreferencePage_IgnoreMethodFilter, PreferencePage_IgnoreMethodFilterToolTip, spacer);
    appendBool(FILTER_BEAN_METHOD, PreferencePage_IgnoreBeanMethods, PreferencePage_IgnoreBeanMethodsToolTip, spacer,
        2);
  }

  /**
   * Create a group of detection settings: Search classes, methods, fields,
   * search class names in text files
   */
  private void createDetectGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, PreferencePage_GroupDetect);
    IntegerFieldEditor warnLimit = new IntegerFieldEditor(WARN_LIMIT, PreferencePage_WarnLimit + SEPARATOR, spacer);
    warnLimit.getLabelControl(spacer).setToolTipText(PreferencePage_WarnLimitToolTip);
    addField(warnLimit);
    appendCombo(ANALYZE_CLASSES, PreferencePage_Classes, spacer);
    appendCombo(ANALYZE_MEHTODS, PreferencePage_Methods, spacer);
    appendCombo(ANALYZE_FIELDS, PreferencePage_Fields, spacer);
    appendBool(DETECT_TEST_ONLY, PreferencePage_DetectTestOnly, PreferencePage_DetectTestOnlyToolTip, spacer, 2);
  }

  /**
   * Create a group of detection settings: Search classes, methods, fields,
   * search class names in text files
   */
  private void createFileSearchGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, PreferencePage_GroupFileSearch);
    SynchBooleanFieldEditor analyzeLiteralsCheck = new SynchBooleanFieldEditor(spacer);
    addField(analyzeLiteralsCheck);

    BooleanFieldEditor checkFullClassName = new BooleanFieldEditor(ANALYZE_CHECK_FULL_CLASS_NAME,
        PreferencePage_CheckFullClassName + SEPARATOR, BooleanFieldEditor.SEPARATE_LABEL, spacer);
    Label label = checkFullClassName.getLabelControl(spacer);
    label.setToolTipText(PreferencePage_CheckFullClassNameToolTip);
    addField(checkFullClassName);

    BooleanFieldEditor checkSimpleClassName = new BooleanFieldEditor(ANALYZE_CHECK_SIMPLE_CLASS_NAME,
        PreferencePage_CheckSimleClassName + SEPARATOR, BooleanFieldEditor.SEPARATE_LABEL, spacer);
    label = checkSimpleClassName.getLabelControl(spacer);
    label.setToolTipText(PreferencePage_CheckSimpleClassNameToolTip);
    addField(checkSimpleClassName);

    StringFieldEditor analyzeLiterals = appendText(ANALYZE_LITERALS, PreferencePage_Literals,
        PreferencePage_LiteralsToolTip, spacer);
    analyzeLiteralsCheck.setAnalyzeLiterals(analyzeLiterals);
    analyzeLiteralsCheck.setCheckFullClassName(checkFullClassName, checkSimpleClassName);
  }

  private void createCycleGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, PreferencePage_GroupCycles);
    IntegerFieldEditor cycleDepth = new IntegerFieldEditor(CYCLE_DEPTH, PreferencePage_MaxCycleSize + SEPARATOR, spacer,
        1);
    cycleDepth.setValidRange(CYCLE_DEPTH_MIN, CYCLE_DEPTH_MAX);
    cycleDepth.setEmptyStringAllowed(false);
    cycleDepth.getLabelControl(spacer).setToolTipText(PreferencePage_MaxCycleSizeToolTip);
    addField(cycleDepth);
  }

  private void createFinalGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, PreferencePage_GroupFinal);
    appendCombo(ANALYZE_FINAL_METHOD, PreferencePage_CheckFinalMethod, spacer);
    appendCombo(ANALYZE_FINAL_FIELD, PreferencePage_CheckFinalField, spacer);
  }

  private void createVisibilityGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, PreferencePage_GroupVisibility);

    addChangeAllVisibiliyCombo(spacer);
    addLineHack(spacer, null);

    Label visibilityWarnLabel = new Label(spacer, SWT.LEFT); // setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
    visibilityWarnLabel.setFont(new Font(spacer.getDisplay(), "Arial", 10, SWT.BOLD)); //$NON-NLS-1$
    visibilityWarnLabel.setText(PreferencePage_ReduceVisibiltyWarning);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    visibilityWarnLabel.setLayoutData(gd);
    // @formatter:off
    appendCombo(ANALYZE_VISIBILITY_PROTECTED_CLASSES , PreferencePage_CheckProtectedClasses, spacer);
    appendCombo(ANALYZE_VISIBILITY_PRIVATE_CLASSES   , PreferencePage_CheckPrivateClasses, spacer);
    addLineHack(spacer, null);
    appendCombo(ANALYZE_VISIBILITY_PROTECTED_METHODS , PreferencePage_CheckProtectedMethods, spacer);
    appendCombo(ANALYZE_VISIBILITY_PRIVATE_METHODS   , PreferencePage_CheckPrivateMethods, spacer);
    addLineHack(spacer, null);
    appendCombo(ANALYZE_VISIBILITY_PROTECTED_FIELDS  , PreferencePage_CheckProtectedFields, spacer);
    appendCombo(ANALYZE_VISIBILITY_PRIVATE_FIELDS    , PreferencePage_CheckPrivateFields, spacer);
    // [ 2804064 ] Access to enclosing type - make 2743908 configurable
    appendBool(IGNORE_SYNTHETIC_ACCESS_EMULATION      , PreferencePage_ignoreSyntheticAccessEmulation,
                                                        PreferencePage_ignoreSyntheticAccessEmulationTooltip, spacer, 2);
    addLineHack(spacer, null);
    appendCombo(ANALYZE_VISIBILITY_PROTECTED_CONSTANTS, PreferencePage_CheckProtectedConstants, spacer);
    appendCombo(ANALYZE_VISIBILITY_PRIVATE_CONSTANTS  , PreferencePage_CheckPrivateConstants, spacer);
    // @formatter:on
  }

  private void createReportGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, PreferencePage_GroupReports);
    appendBool(REPORT_CREATE_XML, PreferencePage_CreateXmlReport, null, spacer, 3);
    for (ReportExtension extension : ReportExtension.getAllExtensions()) {
      String text = String.format("Create %s report", extension.getDescription()); //$NON-NLS-1$
      appendBool(getReportStoreKey(extension), text, null, spacer, 3);
    }

    appendText(REPORT_FILE, PreferencePage_ReportFile, PreferencePage_ReportFileToolTip, spacer, 3);

    DirectoryFieldEditor path = new DirectoryFieldEditor(REPORT_DIR, PreferencePage_ReportDir, spacer);
    path.getLabelControl(spacer).setToolTipText(PreferencePage_ReportDirToolTip);
    addField(path);

    addLineHack(spacer, null);
    Button ok = new Button(spacer, SWT.PUSH);
    ok.setText(PreferencePage_BrowseReportsDir);
    ok.setToolTipText(PreferencePage_BrowseReportsDirToolTip);
    ok.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        Program p = Program.findProgram("html"); //$NON-NLS-1$
        p = (p == null ? Program.findProgram("htm") : p); //$NON-NLS-1$
        if (p != null) {
          // java 6: Desktop.getInstance().open(file);
          p.execute(ReportNameManager.getReportDir(true));
        }
      }
    });
  }

  private void createOtherGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, PreferencePage_GroupLogging);
    ComboFieldEditor combo = new ComboFieldEditor(LOG_LEVEL, PreferencePage_LogLevel + SEPARATOR, LOG_LEVELS, spacer);
    addField(combo);
    combo.getLabelControl(spacer).setToolTipText(PreferencePage_LogLevelToolTip);
    appendBool(LOG_TO_ECLIPSE, PreferencePage_LogToEclipse, PreferencePage_LogToEclipseToolTip, spacer, 2);

    Composite spacerFiles = createGroup(parentGroups, "Files and directories:"); //$NON-NLS-1$
    //    addLineHack(spacer, null);
    //    addLineHack(spacer, "Files and directories:"); //$NON-NLS-1$
    // @formatter:off
    File modesDir = UCDetectorPlugin.getModesDir();
    appendLabelAndText(spacerFiles, "Reports"     , ReportNameManager.getReportDir(true));//$NON-NLS-1$
    appendLabelAndText(spacerFiles, "Modes"       , modesDir.getAbsolutePath()          );//$NON-NLS-1$
    appendLabelAndText(spacerFiles, "Eclipse home", UCDInfo.getEclipseHome()            );//$NON-NLS-1$
    appendLabelAndText(spacerFiles, "Log file"    , UCDInfo.getLogfile()                );//$NON-NLS-1$
    appendLabelAndText(spacerFiles, "Workspace"   , UCDInfo.getWorkspace()              );//$NON-NLS-1$
    // @formatter:on
  }

  private static void appendLabelAndText(Composite spacer, String labelText, String textText) {
    Label label = new Label(spacer, SWT.LEFT);
    label.setText("    " + labelText + SEPARATOR); //$NON-NLS-1$
    Text text = new Text(spacer, /*SWT.MULTI | */SWT.BORDER);
    text.setText(textText);
    text.setEditable(false);
    text.setBackground(label.getBackground());
    //    addLineHack(spacer);
  }

  private Combo changeVisibiliyCombo;
  private TabFolder tabFolder;

  // [2810803] Change visibility options more comfortable
  private void addChangeAllVisibiliyCombo(Composite parent) {
    Label label = new Label(parent, SWT.LEFT);
    label.setText(PreferencePage_ChangeVisibilityCombos);
    changeVisibiliyCombo = new Combo(parent, SWT.READ_ONLY);
    changeVisibiliyCombo.setItems(new String[] { // @formatter:off
          WarnLevel.ERROR  .toStringLocalized(),
          WarnLevel.WARNING.toStringLocalized(),
          WarnLevel.IGNORE .toStringLocalized()
        }
    );// @formatter:on
    changeVisibiliyCombo.setText(WarnLevel.WARNING.toStringLocalized());
    changeVisibiliyCombo.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent evt) {
        int selectionIndex = changeVisibiliyCombo.getSelectionIndex();
        if (selectionIndex != -1) {
          for (FieldEditor field : fields) {
            if (field.getPreferenceName().startsWith(ANALYZE_VISIBILITY_PREFIX)) {
              // "ERROR" instead of "Error"
              String comboValue = WarnLevel.values()[selectionIndex].name();
              field.getPreferenceStore().setValue(field.getPreferenceName(), comboValue);
              field.load();
            }
          }
        }
      }
    });
  }

  private Composite createTab(String tabText) {
    addTab(tabText);
    Composite composite = createComposite(tabFolder, 1, 1, GridData.FILL_HORIZONTAL);
    TabItem tabMain = new TabItem(tabFolder, SWT.NONE);
    tabMain.setText(tabText);
    tabMain.setControl(composite);
    return composite;
  }

  private static void addLineHack(Composite spacer, String text) {
    if (text != null) {
      // Create a horizontal separator
      GridData gdSeparator = new GridData(GridData.FILL_HORIZONTAL);
      gdSeparator.horizontalSpan = 3;
      Label separator = new Label(spacer, SWT.HORIZONTAL | SWT.SEPARATOR);
      separator.setLayoutData(gdSeparator);
    }
    Label label = new Label(spacer, SWT.NONE);
    if (text != null) {
      label.setText(text);
    }
    GridData gdLabel = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    gdLabel.horizontalSpan = 3;
    label.setLayoutData(gdLabel);
  }

  /** Hack for layout problems. See also: IntegerFieldEditor.getNumberOfControls() */
  @Override
  protected void adjustGridLayout() {
    //
  }

  private void addTab(String tab) {
    extendedPreferences.add(TAB_START + tab);
  }

  private void addGroup(String group) {
    extendedPreferences.add(GROUP_START + group.replace("&", "")); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  protected void addField(FieldEditor editor) {
    fields.add(editor);
    extendedPreferences.add(editor.getPreferenceName());
    super.addField(editor);
  }

  /**
   * This class synchronizes the "literal check box" to the "literal text field"
   */
  private static class SynchBooleanFieldEditor extends BooleanFieldEditor {
    private final Composite parent;
    private StringFieldEditor analyzeLiterals;
    private BooleanFieldEditor[] checkXClassNames;
    private final Button check;

    SynchBooleanFieldEditor(Composite parent) {
      super(ANALYZE_LITERALS_CHECK, PreferencePage_LiteralsCheck, parent);
      this.parent = parent;
      check = getChangeControl(parent);
      check.setToolTipText(PreferencePage_LiteralsCheckToolTip);
    }

    /** Necessary, because first "literal check box" must be created, then analyzeLiterals  */
    void setAnalyzeLiterals(StringFieldEditor analyzeLiterals) {
      this.analyzeLiterals = analyzeLiterals;
    }

    void setCheckFullClassName(BooleanFieldEditor... checkXClassNames) {
      this.checkXClassNames = checkXClassNames;
    }

    /** Hack to avoid ugly layout problems */
    @Override
    public int getNumberOfControls() {
      return 2;
    }

    @Override
    protected void fireStateChanged(String property, boolean oldValue, boolean newValue) {
      super.fireStateChanged(property, oldValue, newValue);
      synchronizeAnalyzeLiteralsCheck();
    }

    @Override
    protected void refreshValidState() {
      synchronizeAnalyzeLiteralsCheck();
    }

    private void synchronizeAnalyzeLiteralsCheck() {
      if (check.isEnabled()) { // needed because modes panel may set enabled checkBox
        analyzeLiterals.setEnabled(getBooleanValue(), parent);
        for (BooleanFieldEditor checkXClassName : checkXClassNames) {
          checkXClassName.setEnabled(getBooleanValue(), parent);
        }
      }
    }
  }

  private void appendCombo(String name, String label, Composite parent) {
    ComboFieldEditor combo = new ComboFieldEditor(name, label + SEPARATOR, WARN_LEVELS, parent);
    combo.fillIntoGrid(parent, 2);
    combo.getLabelControl(parent).setToolTipText(PreferencePage_ComboToolTip);
    Label labelControl = combo.getLabelControl(parent);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    labelControl.setLayoutData(gd);
    addField(combo);
  }

  private StringFieldEditor appendText(String name, String label, String toolTip, Composite parent) {
    return appendText(name, label, toolTip, parent, 2);
  }

  private StringFieldEditor appendText(String name, String label, String toolTip, Composite parent, int columns) {
    StringFieldEditor text = new StringFieldEditor(name, label + SEPARATOR, parent);
    text.fillIntoGrid(parent, columns);
    text.getLabelControl(parent).setToolTipText(toolTip);
    addField(text);
    return text;
  }

  private void appendBool(String name, String text, String tooltip, Composite parent, int columns) {
    BooleanFieldEditor bool = new BooleanFieldEditor(name, text + SEPARATOR, BooleanFieldEditor.SEPARATE_LABEL, parent);
    bool.fillIntoGrid(parent, columns);
    bool.getLabelControl(parent).setToolTipText(tooltip);
    addField(bool);
  }

  // -------------------------------------------------------------------------
  // SWT
  // -------------------------------------------------------------------------
  @Override
  // org.eclipse.help.ui.internal.preferences.HelpContentPreferencePage
  protected Control createContents(Composite parent) {
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, UCDetectorPlugin.HELP_ID_PREFERENCES);
    return super.createContents(parent);
  }

  // LaunchingPreferencePage
  private Composite createGroup(Composite parent, String text) {
    addGroup(text);
    int columns = 1, hspan = 1, fill = GridData.FILL_HORIZONTAL;
    Group g = new Group(parent, SWT.NONE);
    g.setLayout(new GridLayout(3, false));
    g.setText(text);
    GridData gd = new GridData(fill);
    gd.horizontalSpan = hspan;
    g.setLayoutData(gd);
    Composite group = createComposite(g, columns, 1, fill);
    groups.add(group);
    return group;
  }

  // SWTFactory
  protected static Composite createComposite(Composite parent, int columns, int hspan, int fill) {
    Composite g = new Composite(parent, SWT.NONE);
    g.setLayout(new GridLayout(columns, false));
    g.setFont(parent.getFont());
    GridData gd = new GridData(fill);
    gd.horizontalSpan = hspan;
    g.setLayoutData(gd);
    return g;
  }

  private static GridData createGridData(int width, int height, int hAlign, int vAlign, boolean hGrab, boolean vGrab) {
    final GridData gd = new GridData(hAlign, vAlign, hGrab, vGrab);
    gd.widthHint = width;
    gd.heightHint = height;
    return gd;
  }

  // -------------------------------------------------------------------------
  // EVENTS
  // -------------------------------------------------------------------------

  @Override
  public boolean performOk() {
    boolean result = super.performOk();
    modesPanel.saveMode();
    getPreferenceStore().setValue(MODE_NAME, modesPanel.getCombo().getText());
    if (Log.isDebug()) {
      Log.debug("New preferences: " + UCDetectorPlugin.getPreferencesAsString()); //$NON-NLS-1$
    }
    return result;
  }

  @Override
  protected void performDefaults() {
    super.performDefaults();
    modesPanel.getCombo().setText(ModesPanel.Mode.Default.toStringLocalized());
    modesPanel.updateModeButtons();
    super.performOk();
  }
}
