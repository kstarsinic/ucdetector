/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.preferences.ModesPanel.Mode;

/**
 * Create the UCDetector preference page:<br>
 * Values are stored in property file:
 * <code>WORSPACE/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.ucdetector.prefs</code>
 * <p>
 * User specific modes are stored in: code>WORKSPACE/.metadata/.plugins/org.ucdetector/modes</code>
 * <p>
 * @see "http://www.eclipsepluginsite.com/preference-pages.html"
 */
public class UCDetectorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
  protected final List<FieldEditor> fields = new ArrayList<FieldEditor>();
  /**
   * entryNames (first column) and values (second column) for the
   * ComboFieldEditor
   */
  static final String[][] WARN_LEVELS = new String[][] {
      { WarnLevel.ERROR.toStringLocalized(), WarnLevel.ERROR.toString() },
      { WarnLevel.WARNING.toStringLocalized(), WarnLevel.WARNING.toString() },
      { WarnLevel.IGNORE.toStringLocalized(), WarnLevel.IGNORE.toString() } };
  private ModesPanel modesPanel;

  public UCDetectorPreferencePage() {
    super(FieldEditorPreferencePage.GRID);
    this.setPreferenceStore(Prefs.getStore());
  }

  public void init(IWorkbench workbench) {
    //
  }

  @Override
  public void createFieldEditors() {
    Composite parentGroups = createComposite(getFieldEditorParent(), 1, 1, GridData.FILL_BOTH);
    setTitle("UCDetector " + UCDetectorPlugin.getAboutUCDVersion()); //$NON-NLS-1$
    modesPanel = new ModesPanel(this, parentGroups);
    modesPanel.createModeCombo();
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

  @Override
  public boolean performOk() {
    boolean result = super.performOk();
    getPreferenceStore().setValue(Prefs.MODE_INDEX, modesPanel.getCombo().getSelectionIndex());
    Log.logInfo("New preferences: " + UCDetectorPlugin.getPreferencesAsString()); //$NON-NLS-1$
    return result;
  }

  @Override
  protected void performDefaults() {
    super.performDefaults();
    modesPanel.getCombo().setText(Mode.Default.toStringLocalized());
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

  private Composite createTab(TabFolder tabFolder, String tabText) {
    Composite composite = createComposite(tabFolder, 1, 1, GridData.FILL_HORIZONTAL);
    TabItem tabMain = new TabItem(tabFolder, SWT.NONE);
    tabMain.setText(tabText);
    tabMain.setControl(composite);
    return composite;
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

  void dumpPreferencesPerPage() {
    List<String> orderedPreferences = new ArrayList<String>();
    for (FieldEditor field : fields) {
      orderedPreferences.add(field.getPreferenceName());
    }
    Map<String, String> allPreferences = UCDetectorPlugin.getAllPreferences();
    for (String pref : orderedPreferences) {
      System.out.println(pref + "=" + allPreferences.get(pref)); //$NON-NLS-1$
    }
  }

  @Override
  protected void addField(FieldEditor editor) {
    fields.add(editor);
    super.addField(editor);
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

  /**
   * create an ComboFieldEditor with label, tooltip and do layout
   */
  static ComboFieldEditor createCombo(String name, String label, Composite parent) {
    ComboFieldEditor combo = new ComboFieldEditor(name, label, WARN_LEVELS, parent);
    combo.fillIntoGrid(parent, 2);
    combo.getLabelControl(parent).setToolTipText(Messages.PreferencePage_ComboToolTip);
    fillHorizontal(parent, combo);
    return combo;
  }

  /**
   * create an StringFieldEditor with label, tooltip and do layout
   */
  static StringFieldEditor createText(String name, String label, Composite parent, String toolTip) {
    StringFieldEditor text = new StringFieldEditor(name, label, parent);
    text.fillIntoGrid(parent, 2);
    text.getLabelControl(parent).setToolTipText(toolTip);
    return text;
  }

  private static void fillHorizontal(Composite parent, FieldEditor fieldEditor) {
    Label labelControl = fieldEditor.getLabelControl(parent);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    labelControl.setLayoutData(gd);
    // org.eclipse.swt.graphics. Color color
    // = org.eclipse.swt.widgets.Display.getDefault().getSystemColor(SWT.
    // COLOR_CYAN);
    // labelControl.setBackground(color);
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
  static Composite createGroup(Composite parent, String text, int columns, int hspan, int fill) {
    Group g = new Group(parent, SWT.NONE);
    g.setLayout(new GridLayout(3, false));
    g.setText(text);
    GridData gd = new GridData(fill);
    gd.horizontalSpan = hspan;
    g.setLayoutData(gd);
    Composite spacer = createComposite(g, columns, 1, fill);
    return spacer;
  }

  // SWTFactory
  public static Composite createComposite(Composite parent, int columns, int hspan, int fill) {
    Composite g = new Composite(parent, SWT.NONE);
    g.setLayout(new GridLayout(columns, false));
    g.setFont(parent.getFont());
    GridData gd = new GridData(fill);
    gd.horizontalSpan = hspan;
    g.setLayoutData(gd);
    return g;
  }

  protected static GridData createGridData(int width, int height, int hAlign, int vAlign, boolean hGrab, boolean vGrab) {
    final GridData gd = new GridData(hAlign, vAlign, hGrab, vGrab);
    gd.widthHint = width;
    gd.heightHint = height;
    return gd;
  }
}