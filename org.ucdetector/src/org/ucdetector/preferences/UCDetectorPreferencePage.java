/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 * Create the UCDetector preference page:<br>
 * Values are stored in:
 * <code>RUNTIME_WORSPACE_DIR\.metadata\.plugins\org.eclipse.core.runtime\.settings\org.ucdetector.prefs</code>
 * @see "http://www.eclipsepluginsite.com/preference-pages.html"
 */
public class UCDetectorPreferencePage extends UCDetectorBasePreferencePage {
  enum Mode {
    classes_only, //
    Default, //
    full, //
    ;

    String toStringLocalized() {
      return Messages.getString("PrefMode_" + this.name(), this.name()); //$NON-NLS-1$
    }
  }

  private static final String[] MODES;

  static {
    Mode[] modes = Mode.values();
    MODES = new String[modes.length];
    for (int i = 0; i < modes.length; i++) {
      MODES[i] = modes[i].toStringLocalized();
    }
  }

  public UCDetectorPreferencePage() {
    super(FieldEditorPreferencePage.GRID, Prefs.getStore());
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
    createFilterTab(tabFolder);
    createMainTab(tabFolder);
    createKeywordTab(tabFolder);
  }

  private void createModeCombo(Composite parentGroups) {
    Composite spacer = createComposite(parentGroups, 2, 1, GridData.FILL_HORIZONTAL);
    Label label = new Label(spacer, SWT.LEFT);
    label.setText("Select detection mode");
    final Combo changeMode = new Combo(spacer, SWT.READ_ONLY);
    changeMode.setItems(MODES);
    changeMode.setText(Mode.Default.name());
    changeMode.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent evt) {
        int index = changeMode.getSelectionIndex();
        if (index != -1) {
          Mode mode = Mode.values()[index];
          setPreferences(mode + ".properties"); //$NON-NLS-1$
        }
      }
    });
  }

  private void createMainTab(TabFolder tabFolder) {
    Composite compositeMain = createComposite(tabFolder, 1, 1, GridData.FILL_HORIZONTAL);
    TabItem tabMain = new TabItem(tabFolder, SWT.NONE);
    tabMain.setText("Detect");
    tabMain.setControl(compositeMain);
    createDetectGroup(compositeMain);
    createFileSearchGroup(compositeMain);
    createOtherGroup(compositeMain);
  }

  private void createFilterTab(TabFolder tabFolder) {
    Composite compositeFilter = createComposite(tabFolder, 1, 1, GridData.FILL_HORIZONTAL);
    TabItem tabFilter = new TabItem(tabFolder, SWT.NONE);
    tabFilter.setText("Filter");
    tabFilter.setControl(compositeFilter);
    createFilterGroup(compositeFilter);
  }

  private void createKeywordTab(TabFolder tabFolder) {
    Composite compositeVisibility = createComposite(tabFolder, 1, 1, GridData.FILL_HORIZONTAL);
    TabItem tabVisibility = new TabItem(tabFolder, SWT.NONE);
    tabVisibility.setText("Keywords");
    tabVisibility.setControl(compositeVisibility);
    createKeywordGroup(compositeVisibility);
    createVisibilityGroupClasses(compositeVisibility);
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
    FileFieldEditor fileFieldEditor = new FileFieldEditor(Prefs.REPORT_FILE, Messages.PreferencePage_ReportFile, spacer) {
      /**
       * Permit all values entered!
       */
      @Override
      protected boolean checkState() {
        return true;
      }
    };
    fileFieldEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
    fileFieldEditor.setFileExtensions(new String[] { "*.html" }); //$NON-NLS-1$
    fileFieldEditor.getLabelControl(spacer).setToolTipText(Messages.PreferencePage_ReportFileToolTip);
    this.addField(fileFieldEditor);
    // Cycle
    IntegerFieldEditor cycleDepth = new IntegerFieldEditor(Prefs.CYCLE_DEPTH, Messages.PreferencePage_MaxCycleSize,
        spacer, 1) {
      /** 
       * Hack for layout problems. 
       * */
      @Override
      public int getNumberOfControls() {
        return 3;
      }
    };
    cycleDepth.setValidRange(Prefs.CYCLE_DEPTH_MIN, Prefs.CYCLE_DEPTH_MAX);
    cycleDepth.setEmptyStringAllowed(false);
    cycleDepth.getLabelControl(spacer).setToolTipText(Messages.PreferencePage_MaxCycleSizeToolTip);
    this.addField(cycleDepth);
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
    //
    //    addLineHack(spacer);
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_CLASSES,
        Messages.PreferencePage_CheckProtectedClasses, spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_CLASSES, Messages.PreferencePage_CheckPrivateClasses,
        spacer));
    addLineHack(spacer);

    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_METHODS,
        Messages.PreferencePage_CheckProtectedMethods, spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_METHODS, Messages.PreferencePage_CheckPrivateMethods,
        spacer));
    addLineHack(spacer);

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

    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_CONSTANTS,
        Messages.PreferencePage_CheckProtectedConstants, spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_CONSTANTS,
        Messages.PreferencePage_CheckPrivateConstants, spacer));
  }

  private static void addLineHack(Composite spacer) {
    //    Composite composite = createComposite(spacer, 2, 1, GridData.FILL_BOTH);
    //    Label label = new Label(spacer, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.WRAP);
    //    label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
    //    label = new Label(spacer, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.WRAP);
    //    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    //    label.setLayoutData(gd);
    Label label = new Label(spacer, SWT.WRAP);
    label.setText("------");
    label = new Label(spacer, SWT.WRAP);
  } // --------------------------------------------------------------------------

  /**
   * Hack for layout problems. See also: IntegerFieldEditor.getNumberOfControls()
   * */
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