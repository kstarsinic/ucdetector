/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 * Create the UCDetector preference page:<br>
 * Values are stored in property file:
 * <code>WORSPACE/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.ucdetector.prefs</code>
 * <p>
 * User specific modes are stored in: code>WORKSPACE/.metadata/.plugins/org.ucdetector/modes</code>
 * <p>
 * @see "http://www.eclipsepluginsite.com/preference-pages.html"
 */
public class UCDetectorPreferencePage extends UCDetectorBasePreferencePage {
  private static final String MODES_FILE_TYPE = ".properties"; //$NON-NLS-1$

  /** built-in preferences mode */
  enum Mode {
    Default, //
    classes_only, //
    full, //
    ;

    String toStringLocalized() {
      return Messages.getString("PrefMode_" + this.name(), this.name()); //$NON-NLS-1$
    }
  }

  private Button saveButton;
  private Button addButton;
  private Button removeButton;
  private Combo modesCombo;
  private final File modesDir;

  public UCDetectorPreferencePage() {
    super(FieldEditorPreferencePage.GRID, Prefs.getStore());
    File ucdDir = UCDetectorPlugin.getDefault().getStateLocation().toFile();
    modesDir = new File(ucdDir, "modes"); //$NON-NLS-1$
    modesDir.mkdirs();
  }

  @Override
  public void createFieldEditors() {
    Composite parentGroups = createComposite(getFieldEditorParent(), 1, 1, GridData.FILL_BOTH);
    setTitle("UCDetector " + UCDetectorPlugin.getAboutUCDVersion()); //$NON-NLS-1$
    createModeCombo(parentGroups);
    // -----------------------------------------------
    // org.eclipse.team.internal.ccvs.ui.CVSPreferencesPage.createGeneralTab()
    TabFolder tabFolder = new TabFolder(parentGroups, SWT.NONE);
    tabFolder.setLayoutData(createGridData(500, SWT.DEFAULT, SWT.FILL, SWT.CENTER, true, false));
    // FILTER -----------------------------------------------------------------
    Composite composite = createTab(tabFolder, Messages.PreferencePage_TabFilter);
    createFilterGroup(composite);
    // MAIN -----------------------------------------------------------------
    composite = createTab(tabFolder, Messages.PreferencePage_TabDetect);
    createDetectGroup(composite);
    createFileSearchGroup(composite);
    createOtherGroup(composite);
    // KEYWORD -----------------------------------------------------------------
    composite = createTab(tabFolder, Messages.PreferencePage_TabKeywords);
    createKeywordGroup(composite);
    createVisibilityGroupClasses(composite);
    // REPORT -----------------------------------------------------------------
    composite = createTab(tabFolder, Messages.PreferencePage_TabReport);
    createReportGroup(composite);
  }

  private void createModeCombo(Composite parentGroups) {
    Composite spacer = createComposite(parentGroups, 5, 1, GridData.FILL_HORIZONTAL);
    Label label = new Label(spacer, SWT.LEFT);
    label.setText(Messages.PreferencePage_ModeLabel);
    modesCombo = new Combo(spacer, SWT.READ_ONLY);

    saveButton = new Button(spacer, SWT.PUSH);
    saveButton.setText(Messages.PreferencePage_ModeSave);
    addButton = new Button(spacer, SWT.PUSH);
    addButton.setText(Messages.PreferencePage_ModeAdd);
    removeButton = new Button(spacer, SWT.PUSH);
    removeButton.setText(Messages.PreferencePage_ModeRemove);
    //
    String[] modes = getModes();
    modesCombo.setItems(modes);
    int savedIndex = getPreferenceStore().getInt(Prefs.MODE_INDEX);
    boolean isValidIndex = (savedIndex >= 0 && savedIndex < modes.length);
    modesCombo.setText(isValidIndex ? modes[savedIndex] : Mode.Default.toStringLocalized());

    modesCombo.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent evt) {
        updateModeButtons();
        String modesFileName = null;
        try {
          int index = modesCombo.getSelectionIndex();
          if (index == -1) {
            // ignore
          }
          // default
          else if (index == Mode.Default.ordinal()) {
            performDefaults();
          }
          // built-in
          else if (index < Mode.values().length) {
            Mode mode = Mode.values()[index];
            modesFileName = mode + MODES_FILE_TYPE;
            setPreferences(getClass().getResourceAsStream(modesFileName));
          }
          // custom
          else {
            String newMode = modesCombo.getText();
            modesFileName = getModesFile(newMode).getAbsolutePath();
            setPreferences(new FileInputStream(modesFileName));
          }
        }
        catch (IOException ex) {
          String message = NLS.bind(Messages.PreferencePage_CantSetPreferences, modesFileName);
          UCDetectorPlugin.logErrorAndStatus(message, ex);
        }
      }
    });
    //
    SelectionListener selectionListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        Widget widget = event.widget;
        if (widget == saveButton) {
          saveMode(modesCombo.getText());
        }
        else if (widget == addButton) {
          addMode();
        }
        else if (widget == removeButton) {
          removeMode();
        }
      }
    };
    updateModeButtons();
    saveButton.addSelectionListener(selectionListener);
    addButton.addSelectionListener(selectionListener);
    removeButton.addSelectionListener(selectionListener);
  }

  /** Get built in modes and user specific modes */
  private String[] getModes() {
    Set<String> result = new LinkedHashSet<String>();
    for (Mode mode : Mode.values()) {
      result.add(mode.toStringLocalized());
    }
    for (String modesFile : modesDir.list()) {
      result.add(modesFile.substring(0, modesFile.length() - MODES_FILE_TYPE.length()));
    }
    Log.logDebug("Available modes are: " + result); //$NON-NLS-1$
    return result.toArray(new String[result.size()]);
  }

  /** Add a user specific mode, and save it to a file */
  private void addMode() {
    InputDialog input = new InputDialog(getShell(), Messages.PreferencePage_NewMode, Messages.PreferencePage_ModeName,
        null, null);
    input.open();
    String newModeName = input.getValue();
    if (newModeName != null && newModeName.trim().length() > 0) {
      saveMode(newModeName);
      Log.logDebug("Added new mode: " + newModeName); //$NON-NLS-1$
      modesCombo.setItems(getModes());
      modesCombo.setText(newModeName);
      updateModeButtons();
    }
  }

  /** Save it to a file in WORKSPACE/.metadata/.plugins/org.ucdetector/modes  */
  private void saveMode(String modeName) {
    super.performApply();
    Map<String, String> allPreferences = UCDetectorPlugin.getAllPreferences();
    Map<String, String> delta = UCDetectorPlugin.getDeltaPreferences();
    Properties properties = new Properties();
    properties.putAll(allPreferences);
    properties.putAll(delta);
    File modesFile = getModesFile(modeName);
    try {
      properties.store(new FileOutputStream(modesFile), "Created by " + getClass().getName()); //$NON-NLS-1$
      Log.logDebug("Saved mode to: " + modesFile.getAbsolutePath()); //$NON-NLS-1$
    }
    catch (IOException ex) {
      String message = NLS.bind(Messages.PreferencePage_ModeFileCantSave, modesFile.getAbsolutePath());
      UCDetectorPlugin.logErrorAndStatus(message, ex);
    }
  }

  private File getModesFile(String modeName) {
    return new File(modesDir, modeName + MODES_FILE_TYPE);
  }

  private void removeMode() {
    String modeToRemove = modesCombo.getText();
    File file = getModesFile(modeToRemove);
    file.delete();
    Log.logDebug("Deleted mode file: " + file.getAbsolutePath()); //$NON-NLS-1$
    modesCombo.setItems(getModes());
    modesCombo.setText(Mode.Default.toStringLocalized());
    performDefaults();
    updateModeButtons();
  }

  /** save and remove buttons are only enabled for custom modes */
  private void updateModeButtons() {
    int index = modesCombo.getSelectionIndex();
    boolean enabled = (index < 0 || index >= Mode.values().length);
    saveButton.setEnabled(enabled);
    removeButton.setEnabled(enabled);
  }

  private Composite createTab(TabFolder tabFolder, String tabText) {
    Composite composite = createComposite(tabFolder, 1, 1, GridData.FILL_HORIZONTAL);
    TabItem tabMain = new TabItem(tabFolder, SWT.NONE);
    tabMain.setText(tabText);
    tabMain.setControl(composite);
    return composite;
  }

  @Override
  public boolean performOk() {
    boolean result = super.performOk();
    getPreferenceStore().setValue(Prefs.MODE_INDEX, modesCombo.getSelectionIndex());
    Log.logInfo("New preferences: " + UCDetectorPlugin.getPreferencesAsString()); //$NON-NLS-1$
    return result;
  }

  @Override
  protected void performDefaults() {
    super.performDefaults();
    modesCombo.setText(Mode.Default.toStringLocalized());
    //    dumpPreferencesPerPage();
  }

  /**
   * Create a group of filter settings: Filter source folders,
   * packages, classes, methods, fields
   */
  private void createFilterGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, Messages.PreferencePage_GroupFilter, 1, 1, GridData.FILL_HORIZONTAL);
    StringFieldEditor sourceFilter = createText(Prefs.FILTER_SOURCE_FOLDER,
        Messages.PreferencePage_IgnoreSourceFolderFilter, spacer,
        Messages.PreferencePage_IgnoreSourceFolderFilterToolTip);
    this.addField(sourceFilter);
    StringFieldEditor packageFilter = createText(Prefs.FILTER_PACKAGE, Messages.PreferencePage_IgnorePackageFilter,
        spacer, Messages.PreferencePage_IgnorePackageFilterToolTip);
    this.addField(packageFilter);
    StringFieldEditor classFilter = createText(Prefs.FILTER_CLASS, Messages.PreferencePage_IgnoreClassFilter, spacer,
        Messages.PreferencePage_IgnoreClassFilterToolTip);
    this.addField(classFilter);
    StringFieldEditor methodFilter = createText(Prefs.FILTER_METHOD, Messages.PreferencePage_IgnoreMethodFilter,
        spacer, Messages.PreferencePage_IgnoreMethodFilterToolTip);
    this.addField(methodFilter);
    StringFieldEditor fieldFilter = createText(Prefs.FILTER_FIELD, Messages.PreferencePage_IgnoreFieldFilter, spacer,
        Messages.PreferencePage_IgnoreFieldFilterToolTip);
    this.addField(fieldFilter);
    StringFieldEditor annotationsFilter = createText(Prefs.FILTER_ANNOATIONS,
        Messages.PreferencePage_IgnoreAnnotationsFilter, spacer, Messages.PreferencePage_IgnoreAnnotationsFilterToolTip);
    this.addField(annotationsFilter);
    StringFieldEditor implementsFilter = createText(Prefs.FILTER_IMPLEMENTS, Messages.PreferencePage_IgnoreImplements,
        spacer, Messages.PreferencePage_IgnoreImplementsToolTip);
    this.addField(implementsFilter);
    StringFieldEditor containStringFilter = createText(Prefs.FILTER_CONTAIN_STRING,
        Messages.PreferencePage_IgnoreContainString, spacer, Messages.PreferencePage_IgnoreContainStringToolTip);
    this.addField(containStringFilter);
    BooleanFieldEditor beanMethodFilter = new BooleanFieldEditor(Prefs.FILTER_BEAN_METHOD,
        Messages.PreferencePage_IgnoreBeanMethods, BooleanFieldEditor.SEPARATE_LABEL, spacer);
    beanMethodFilter.getLabelControl(spacer).setToolTipText(Messages.PreferencePage_IgnoreBeanMethodsToolTip);
    this.addField(beanMethodFilter);
    BooleanFieldEditor deprecatedFilter = new BooleanFieldEditor(Prefs.IGNORE_DEPRECATED,
        Messages.PreferencePage_IgnoreDeprecated, BooleanFieldEditor.SEPARATE_LABEL, spacer);
    deprecatedFilter.getLabelControl(spacer).setToolTipText(Messages.PreferencePage_IgnoreDeprecatedToolTip);
    this.addField(deprecatedFilter);
  }

  /**
   * Create a group of detection settings: Search classes, methods, fields,
   * search class names in text files
   */
  private void createDetectGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, Messages.PreferencePage_GroupDetect, 1, 1, GridData.FILL_HORIZONTAL);
    IntegerFieldEditor warnLimit = new IntegerFieldEditor(Prefs.WARN_LIMIT, Messages.PreferencePage_WarnLimit, spacer);
    warnLimit.getLabelControl(spacer).setToolTipText(Messages.PreferencePage_WarnLimitToolTip);
    this.addField(warnLimit);
    ComboFieldEditor analyzeClass = createCombo(Prefs.ANALYZE_CLASSES, Messages.PreferencePage_Classes, spacer);
    this.addField(analyzeClass);
    ComboFieldEditor analyzeMethod = createCombo(Prefs.ANALYZE_MEHTODS, Messages.PreferencePage_Methods, spacer);
    this.addField(analyzeMethod);
    ComboFieldEditor analyzeFields = createCombo(Prefs.ANALYZE_FIELDS, Messages.PreferencePage_Fields, spacer);
    this.addField(analyzeFields);
    // Detect code only used by tests
    BooleanFieldEditor ignoreTestOnlyFilter = new BooleanFieldEditor(Prefs.DETECT_TEST_ONLY,
        Messages.PreferencePage_DetectTestOnly, BooleanFieldEditor.SEPARATE_LABEL, spacer);
    ignoreTestOnlyFilter.getLabelControl(spacer).setToolTipText(Messages.PreferencePage_DetectTestOnlyToolTip);
    this.addField(ignoreTestOnlyFilter);
  }

  /**
   * Create a group of detection settings: Search classes, methods, fields,
   * search class names in text files
   */
  private void createFileSearchGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, Messages.PreferencePage_GroupFileSearch, 1, 1,
        GridData.FILL_HORIZONTAL);
    SynchBooleanFieldEditor analyzeLiteralsCheck = new SynchBooleanFieldEditor(spacer);
    this.addField(analyzeLiteralsCheck);
    BooleanFieldEditor checkFullClassName = new BooleanFieldEditor(Prefs.ANALYZE_CHECK_FULL_CLASS_NAME,
        Messages.PreferencePage_CheckFullClassName, BooleanFieldEditor.SEPARATE_LABEL, spacer);
    Label label = checkFullClassName.getLabelControl(spacer);
    label.setToolTipText(Messages.PreferencePage_CheckFullClassNameToolTip);
    this.addField(checkFullClassName);
    StringFieldEditor analyzeLiterals = createText(Prefs.ANALYZE_LITERALS, Messages.PreferencePage_Literals, spacer,
        Messages.PreferencePage_LiteralsToolTip);
    analyzeLiteralsCheck.setAnalyzeLiterals(analyzeLiterals);
    analyzeLiteralsCheck.setCheckFullClassName(checkFullClassName);
    this.addField(analyzeLiterals);
  }

  /**
   * Create a group of other settings
   */
  // Don't use fileFieldEditor in other group: Layout problems!
  private void createOtherGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, Messages.PreferencePage_GroupOthers, 1, 1, GridData.FILL_HORIZONTAL);
    IntegerFieldEditor cycleDepth = new IntegerFieldEditor(Prefs.CYCLE_DEPTH, Messages.PreferencePage_MaxCycleSize,
        spacer, 1);
    cycleDepth.setValidRange(Prefs.CYCLE_DEPTH_MIN, Prefs.CYCLE_DEPTH_MAX);
    cycleDepth.setEmptyStringAllowed(false);
    cycleDepth.getLabelControl(spacer).setToolTipText(Messages.PreferencePage_MaxCycleSizeToolTip);
    this.addField(cycleDepth);
  }

  private void createReportGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, Messages.PreferencePage_GroupOthers, 1, 1, GridData.FILL_HORIZONTAL);
    appendBoolean(Prefs.REPORT_CREATE_HTML, Messages.PreferencePage_CreateHtmlReport, spacer);
    appendBoolean(Prefs.REPORT_CREATE_XML, Messages.PreferencePage_CreateXmlReport, spacer);
    appendBoolean(Prefs.REPORT_CREATE_TXT, Messages.PreferencePage_CreateTextReport, spacer);
    DirectoryFieldEditor path = new DirectoryFieldEditor(Prefs.REPORT_DIR, Messages.PreferencePage_ReportFile, spacer);
    path.getLabelControl(spacer).setToolTipText(Messages.PreferencePage_ReportFileToolTip);
    this.addField(path);
  }

  private void appendBoolean(String name, String text, Composite parent) {
    BooleanFieldEditor bool = new BooleanFieldEditor(name, text, BooleanFieldEditor.SEPARATE_LABEL, parent);
    bool.fillIntoGrid(parent, 3);
    this.addField(bool);
  }

  // --------------------------------------------------------------------------
  // VISIBILITY
  // --------------------------------------------------------------------------
  /**
   * Create a group of keyword settings
   * (analyze visibility, try to make methods or fields final)
   */
  private void createKeywordGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, Messages.PreferencePage_GroupFinal, 1, 1, GridData.FILL_HORIZONTAL);
    // FINAL -------------------------------------------------------------------
    ComboFieldEditor analyzeFinalMethod = createCombo(Prefs.ANALYZE_FINAL_METHOD,
        Messages.PreferencePage_CheckFinalMethod, spacer);
    this.addField(analyzeFinalMethod);
    ComboFieldEditor analyzeFinalField = createCombo(Prefs.ANALYZE_FINAL_FIELD,
        Messages.PreferencePage_CheckFinalField, spacer);
    this.addField(analyzeFinalField);
  }

  /**
   * Create a group of keyword settings
   * (analyze visibility, try to make methods or fields final)
   */
  private void createVisibilityGroupClasses(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups, Messages.PreferencePage_GroupVisibility, 1, 1,
        GridData.FILL_HORIZONTAL);
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_CLASSES,
        Messages.PreferencePage_CheckProtectedClasses, spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_CLASSES, Messages.PreferencePage_CheckPrivateClasses,
        spacer));
    addLineHack(spacer);
    //
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_METHODS,
        Messages.PreferencePage_CheckProtectedMethods, spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_METHODS, Messages.PreferencePage_CheckPrivateMethods,
        spacer));
    addLineHack(spacer);
    //
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_FIELDS, Messages.PreferencePage_CheckProtectedFields,
        spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_FIELDS, Messages.PreferencePage_CheckPrivateFields,
        spacer));
    // [ 2804064 ] Access to enclosing type - make 2743908 configurable
    BooleanFieldEditor ignoreSyntheticAccessEmulation = new BooleanFieldEditor(Prefs.IGNORE_SYNTHETIC_ACCESS_EMULATION,
        "    " //$NON-NLS-1$
            + Messages.PreferencePage_ignoreSyntheticAccessEmulation, BooleanFieldEditor.SEPARATE_LABEL, spacer);
    ignoreSyntheticAccessEmulation.getLabelControl(spacer).setToolTipText(
        Messages.PreferencePage_ignoreSyntheticAccessEmulationTooltip);
    this.addField(ignoreSyntheticAccessEmulation);
    addLineHack(spacer);
    //
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_CONSTANTS,
        Messages.PreferencePage_CheckProtectedConstants, spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_CONSTANTS,
        Messages.PreferencePage_CheckPrivateConstants, spacer));
  }

  private static void addLineHack(Composite spacer) {
    //    Label label = new Label(spacer, SWT.WRAP);
    //    label.setText("------");
    Label label = new Label(spacer, SWT.NONE);
    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    gd.horizontalSpan = 3;
    label.setLayoutData(gd);
  } // --------------------------------------------------------------------------

  /** Hack for layout problems. See also: IntegerFieldEditor.getNumberOfControls() */
  @Override
  protected void adjustGridLayout() {
    //
  }

  /**
   * This class synchronizes the "literal check box" to the "literal text field"
   */
  private static class SynchBooleanFieldEditor extends BooleanFieldEditor {
    private final Composite parent;
    private StringFieldEditor analyzeLiterals;
    private BooleanFieldEditor checkFullClassName;

    SynchBooleanFieldEditor(Composite parent) {
      super(Prefs.ANALYZE_LITERALS_CHECK, Messages.PreferencePage_LiteralsCheck, parent);
      this.parent = parent;
      Button check = getChangeControl(parent);
      check.setToolTipText(Messages.PreferencePage_LiteralsCheckToolTip);
    }

    /**
     * Necessary, because first "literal check box" must be created, then
     * analyzeLiterals
     */
    void setAnalyzeLiterals(StringFieldEditor analyzeLiterals) {
      this.analyzeLiterals = analyzeLiterals;
    }

    void setCheckFullClassName(BooleanFieldEditor checkFullClassName) {
      this.checkFullClassName = checkFullClassName;
    }

    /**
     * Hack to avoid ugly layout problems
     */
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
      analyzeLiterals.setEnabled(getBooleanValue(), parent);
      checkFullClassName.setEnabled(getBooleanValue(), parent);
    }
  }
}