package org.ucdetector.preferences;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 *
 */
public class UCDetectorPreferencePageVisibility extends
    FieldEditorPreferencePage // NO_UCD
    implements IWorkbenchPreferencePage {

  public UCDetectorPreferencePageVisibility() {
    super(FieldEditorPreferencePage.GRID);
    this.setPreferenceStore(Prefs.getStore());
  }

  @Override
  protected Control createContents(Composite parent) {
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
        UCDetectorPlugin.HELP_ID);
    return super.createContents(parent);
  }

  @Override
  public void createFieldEditors() {
    Composite parentGroups = UCDetectorPreferencePage.createComposite(
        getFieldEditorParent(), 1, 1, GridData.FILL_HORIZONTAL);
    //    createFilterGroup(parentGroups);
    //    createDetectGroup(parentGroups);
    //    createFileSearchGroup(parentGroups);
    createKeywordGroup(parentGroups);
    //    createOtherGroup(parentGroups);
  }

  /**
   * Create a group of keyword settings
   * (analyze visibility, try to make methods or fields final)
   */
  private void createKeywordGroup(Composite parentGroups) {
    Composite spacer = UCDetectorPreferencePage.createGroup(parentGroups,
        Messages.PreferencePage_GroupKeyWord, 1, 1, GridData.FILL_HORIZONTAL);
    ComboFieldEditor analyzeVisibility = UCDetectorPreferencePage.createCombo(
        Prefs.ANALYZE_VISIBILITY_PROTECTED,
        Messages.PreferencePage_CheckVisibilityProtected, spacer);
    this.addField(analyzeVisibility);
    ComboFieldEditor analyzeVisibilityPrivate = UCDetectorPreferencePage
        .createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE,
            Messages.PreferencePage_CheckVisibilityPrivate, spacer);
    this.addField(analyzeVisibilityPrivate);
    //    ComboFieldEditor analyzeFinalMethod = createCombo(
    //        Prefs.ANALYZE_FINAL_METHOD, Messages.PreferencePage_CheckFinalMethod,
    //        spacer);
    //    this.addField(analyzeFinalMethod);
    //    ComboFieldEditor analyzeFinalField = createCombo(Prefs.ANALYZE_FINAL_FIELD,
    //        Messages.PreferencePage_CheckFinalField, spacer);
    //    this.addField(analyzeFinalField);
  }

  public void init(IWorkbench workbench) {
  }

  @Override
  public boolean performOk() {
    Log.logInfo(UCDetectorPlugin.getPreferencesAsString());
    return super.performOk();
  }

}
