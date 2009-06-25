/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.ucdetector.Messages;

/**
 * Create the UCDetector preference page:<br>
 * Values are stored in:
 * <code>RUNTIME_WORSPACE_DIR\.metadata\.plugins\org.eclipse.core.runtime\.settings\org.ucdetector.prefs</code>
 * @see "http://www.eclipsepluginsite.com/preference-pages.html"
 */
public class UCDetectorPreferencePage extends UCDetectorBasePreferencePage {

  @Override
  public void createFieldEditors() {
    Composite parentGroups = createComposite(getFieldEditorParent(), 1, 1,
        GridData.FILL_HORIZONTAL);
    createFilterGroup(parentGroups);
    createDetectGroup(parentGroups);
    createFileSearchGroup(parentGroups);
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
        Prefs.FILTER_BEAN_METHOD, Messages.PreferencePage_IgnoreBeanMethods,
        BooleanFieldEditor.SEPARATE_LABEL, spacer);
    beanMethodFilter.getLabelControl(spacer).setToolTipText(
        Messages.PreferencePage_IgnoreBeanMethodsToolTip);
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
    // Detect code only used by tests
    BooleanFieldEditor ignoreTestOnlyFilter = new BooleanFieldEditor(
        Prefs.DETECT_TEST_ONLY, Messages.PreferencePage_DetectTestOnly,
        BooleanFieldEditor.SEPARATE_LABEL, spacer);
    ignoreTestOnlyFilter.getLabelControl(spacer).setToolTipText(
        Messages.PreferencePage_DetectTestOnlyToolTip);
    this.addField(ignoreTestOnlyFilter);
  }

  /**
   * Create a group of detection settings: Search classes, methods, fields,
   * search class names in text files
   */
  private void createFileSearchGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups,
        Messages.PreferencePage_GroupFileSearch, 1, 1, GridData.FILL_HORIZONTAL);
    SynchBooleanFieldEditor analyzeLiteralsCheck = new SynchBooleanFieldEditor(
        spacer);
    this.addField(analyzeLiteralsCheck);
    BooleanFieldEditor checkFullClassName = new BooleanFieldEditor(
        Prefs.ANALYZE_CHECK_FULL_CLASS_NAME,
        Messages.PreferencePage_CheckFullClassName,
        BooleanFieldEditor.SEPARATE_LABEL, spacer);
    Label label = checkFullClassName.getLabelControl(spacer);
    label.setToolTipText(Messages.PreferencePage_CheckFullClassNameToolTip);
    this.addField(checkFullClassName);
    StringFieldEditor analyzeLiterals = createText(Prefs.ANALYZE_LITERALS,
        Messages.PreferencePage_Literals, spacer,
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
  }

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

    private SynchBooleanFieldEditor(Composite parent) {
      super(Prefs.ANALYZE_LITERALS_CHECK,
          Messages.PreferencePage_LiteralsCheck, parent);
      this.parent = parent;
      Button check = getChangeControl(parent);
      check.setToolTipText(Messages.PreferencePage_LiteralsCheckToolTip);
    }

    /**
     * Necessary, because first "literal check box" must be created, then
     * analyzeLiterals
     */
    private void setAnalyzeLiterals(StringFieldEditor analyzeLiterals) {
      this.analyzeLiterals = analyzeLiterals;
    }

    private void setCheckFullClassName(BooleanFieldEditor checkFullClassName) {
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
      checkFullClassName.setEnabled(getBooleanValue(), parent);
    }
  }
}