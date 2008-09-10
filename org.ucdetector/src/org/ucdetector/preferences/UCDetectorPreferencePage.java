/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 * Create the UCDetector preference page:<br>
 * Values are stored in:
 * <code>RUNTIME_WORSPACE_DIR\.metadata\.plugins\org.eclipse.core.runtime\.settings\org.ucdetector.prefs</code>
 * @see example in http://www.eclipsepluginsite.com/preference-pages.html
 */
public class UCDetectorPreferencePage extends FieldEditorPreferencePage // NO_UCD
    implements // NO_UCD
    IWorkbenchPreferencePage {

  /**
   * entryNames (first column) and values (second column) for the
   * ComboFieldEditor
   */
  private static final String[][] WARN_LEVELS = new String[][] {
      { WarnLevel.ERROR.toStringLocalized(), WarnLevel.ERROR.toString() },
      { WarnLevel.WARNING.toStringLocalized(), WarnLevel.WARNING.toString() },
      { WarnLevel.IGNORE.toStringLocalized(), WarnLevel.IGNORE.toString() } };

  public void init(IWorkbench workbench) {
  }

  public UCDetectorPreferencePage() {
    super(FieldEditorPreferencePage.GRID);
    this.setPreferenceStore(Prefs.getStore());
  }

  @Override
  public void createFieldEditors() {
    Composite parentGroups = createComposite(getFieldEditorParent(), 1, 1,
        GridData.FILL_HORIZONTAL);
    createFilterGroup(parentGroups);
    createDetectGroup(parentGroups);
    createKeywordGroup(parentGroups);
    createOtherGroup(parentGroups);
  }

  /**
   * Create a group of filter settings: Filter source folders,
   * packages, classes, methods, fields
   */
  private void createFilterGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups,
        Messages.PreferencePage_GroupFilter, 1, 1, GridData.FILL_HORIZONTAL);
    StringFieldEditor sourceFilter = createText(Prefs.FILTER_SOURCE_FOLDER,
        Messages.PreferencePage_IgnoreSourceFolderFilter, spacer,
        Messages.PreferencePage_IgnoreSourceFolderFilterToolTip);
    this.addField(sourceFilter);
    StringFieldEditor packageFilter = createText(Prefs.FILTER_PACKAGE,
        Messages.PreferencePage_IgnorePackageFilter, spacer,
        Messages.PreferencePage_IgnorePackageFilterToolTip);
    this.addField(packageFilter);
    StringFieldEditor classFilter = createText(Prefs.FILTER_CLASS,
        Messages.PreferencePage_IgnoreClassFilter, spacer,
        Messages.PreferencePage_IgnoreClassFilterToolTip);
    this.addField(classFilter);
    StringFieldEditor methodFilter = createText(Prefs.FILTER_METHOD,
        Messages.PreferencePage_IgnoreMethodFilter, spacer,
        Messages.PreferencePage_IgnoreMethodFilterToolTip);
    this.addField(methodFilter);
    StringFieldEditor fieldFilter = createText(Prefs.FILTER_FIELD,
        Messages.PreferencePage_IgnoreFieldFilter, spacer,
        Messages.PreferencePage_IgnoreFieldFilterToolTip);
    this.addField(fieldFilter);
    BooleanFieldEditor beanMethodFilter = new BooleanFieldEditor(
        Prefs.FILTER_BEAN_METHOD, "Ignore bean methods",
        BooleanFieldEditor.SEPARATE_LABEL, spacer);
    this.addField(beanMethodFilter);
  }

  /**
   * Create a group of detection settings: Search classes, methods, fields,
   * search class names in text files
   */
  private void createDetectGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups,
        Messages.PreferencePage_GroupDetect, 1, 1, GridData.FILL_HORIZONTAL);
    IntegerFieldEditor warnLimit = new IntegerFieldEditor(Prefs.WARN_LIMIT,
        Messages.PreferencePage_WarnLimit, spacer);
    warnLimit.getLabelControl(spacer).setToolTipText(
        Messages.PreferencePage_WarnLimitToolTip);
    this.addField(warnLimit);
    ComboFieldEditor analyzeClass = createCombo(Prefs.ANALYZE_CLASSES,
        Messages.PreferencePage_Classes, spacer);
    this.addField(analyzeClass);
    ComboFieldEditor analyzeMethod = createCombo(Prefs.ANALYZE_MEHTODS,
        Messages.PreferencePage_Methods, spacer);
    this.addField(analyzeMethod);
    ComboFieldEditor analyzeFields = createCombo(Prefs.ANALYZE_FIELDS,
        Messages.PreferencePage_Fields, spacer);
    this.addField(analyzeFields);
    SynchBooleanFieldEditor analyzeLiteralsCheck = new SynchBooleanFieldEditor(
        spacer);
    this.addField(analyzeLiteralsCheck);
    StringFieldEditor analyzeLiterals = createText(Prefs.ANALYZE_LITERALS,
        Messages.PreferencePage_Literals, spacer,
        Messages.PreferencePage_LiteralsToolTip);
    analyzeLiteralsCheck.setAnalyzeLiterals(analyzeLiterals);
    this.addField(analyzeLiterals);
  }

  /**
   * Create a group of keyword settings
   * (analyze visibility, try to make methods or fields final)
   */
  private void createKeywordGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups,
        Messages.PreferencePage_GroupKeyWord, 1, 1, GridData.FILL_HORIZONTAL);
    ComboFieldEditor analyzeVisibility = createCombo(Prefs.ANALYZE_VISIBILITY,
        Messages.PreferencePage_CheckVisibility, spacer);
    this.addField(analyzeVisibility);
    ComboFieldEditor analyzeFinalMethod = createCombo(
        Prefs.ANALYZE_FINAL_METHOD, Messages.PreferencePage_CheckFinalMethod,
        spacer);
    this.addField(analyzeFinalMethod);
    ComboFieldEditor analyzeFinalField = createCombo(Prefs.ANALYZE_FINAL_FIELD,
        Messages.PreferencePage_CheckFinalField, spacer);
    this.addField(analyzeFinalField);
  }

  /**
   * Create a group of few references settings
   * (handle 0, 1 ... references as warning)
   */
  private void createOtherGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups,
        Messages.PreferencePage_GroupOthers, 1, 1, GridData.FILL_HORIZONTAL);
    FileFieldEditor fileFieldEditor = new FileFieldEditor(Prefs.REPORT_FILE,
        Messages.PreferencePage_ReportFile, spacer) {
      /**
       * Permit all values entered!
       */
      @Override
      protected boolean checkState() {
        return true;
      }
    };
    fileFieldEditor
        .setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
    fileFieldEditor.setFileExtensions(new String[] { "*.html" }); //$NON-NLS-1$
    fileFieldEditor.getLabelControl(spacer).setToolTipText(
        Messages.PreferencePage_ReportFileToolTip);
    this.addField(fileFieldEditor);

    IntegerFieldEditor cycleDepth = new IntegerFieldEditor(Prefs.CYCLE_DEPTH,
        Messages.PreferencePage_MaxCycleSize, spacer) {
      /** 
       * Hack for layout problems. See also: adjustGridLayout()
       * */
      @Override
      public int getNumberOfControls() {
        return 3;
      }
    };
    cycleDepth.setValidRange(Prefs.CYCLE_DEPTH_MIN, Prefs.CYCLE_DEPTH_MAX);
    cycleDepth.setEmptyStringAllowed(false);
    cycleDepth.getLabelControl(spacer).setToolTipText(
        Messages.PreferencePage_MaxCycleSizeToolTip);
    this.addField(cycleDepth);
  }

  /** 
   * Hack for layout problems. See also: IntegerFieldEditor.getNumberOfControls() 
   * */
  @Override
  protected void adjustGridLayout() {
  }

  /**
   * This class synchronizes the "literal check box" to the "literal text field"
   */
  private static class SynchBooleanFieldEditor extends BooleanFieldEditor {
    private final Composite parent;
    private StringFieldEditor analyzeLiterals;
    private final Button check;

    private SynchBooleanFieldEditor(Composite parent) {
      super(Prefs.ANALYZE_LITERALS_CHECK,
          Messages.PreferencePage_LiteralsCheck, parent);
      this.parent = parent;
      check = getChangeControl(parent);
      check.setToolTipText(Messages.PreferencePage_LiteralsCheckToolTip);
    }

    /**
     * Necessary, because first "literal check box" must be created, then
     * analyzeLiterals
     */
    private void setAnalyzeLiterals(StringFieldEditor analyzeLiterals) {
      this.analyzeLiterals = analyzeLiterals;
    }

    /**
     * Hack to avoid ugly layout problems
     */
    @Override
    public int getNumberOfControls() {
      return 2;
    }

    @Override
    protected void fireStateChanged(String property, boolean oldValue,
        boolean newValue) {
      super.fireStateChanged(property, oldValue, newValue);
      synchronizeAnalyzeLiteralsCheck();
    }

    @Override
    protected void refreshValidState() {
      synchronizeAnalyzeLiteralsCheck();
    }

    private void synchronizeAnalyzeLiteralsCheck() {
      analyzeLiterals.setEnabled(getBooleanValue(), parent);
    }
  }

  // -------------------------------------------------------------------------
  // HELPER
  // -------------------------------------------------------------------------

  /**
   * create an ComboFieldEditor with label, tooltip and do layout
   */
  private static ComboFieldEditor createCombo(String name, String label,
      Composite parent) {
    ComboFieldEditor combo = new ComboFieldEditor(name, label, WARN_LEVELS,
        parent);
    combo.fillIntoGrid(parent, 2);
    combo.getLabelControl(parent).setToolTipText(
        Messages.PreferencePage_ComboToolTip);
    fillHorizontal(parent, combo);
    return combo;
  }

  /**
   * create an StringFieldEditor with label, tooltip and do layout
   */
  private static StringFieldEditor createText(String name, String label,
      Composite parent, String toolTip) {
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
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
        UCDetectorPlugin.HELP_ID);
    return super.createContents(parent);
  }

  // LaunchingPreferencePage
  private static Composite createGroup(Composite parent, String text,
      int columns, int hspan, int fill) {
    Group g = new Group(parent, SWT.NONE);
    g.setLayout(new GridLayout(columns, false));
    g.setText(text);
    GridData gd = new GridData(fill);
    gd.horizontalSpan = hspan;
    g.setLayoutData(gd);
    Composite spacer = createComposite(g, 1, 1, GridData.FILL_HORIZONTAL);
    return spacer;
  }

  // SWTFactory
  private static Composite createComposite(Composite parent, int columns,
      int hspan, int fill) {
    Composite g = new Composite(parent, SWT.NONE);
    g.setLayout(new GridLayout(columns, false));
    g.setFont(parent.getFont());
    GridData gd = new GridData(fill);
    gd.horizontalSpan = hspan;
    g.setLayoutData(gd);
    return g;
  }
}