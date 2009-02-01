package org.ucdetector.preferences;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.ucdetector.Messages;

/**
 *
 */
public class UCDetectorPreferencePageVisibility extends
    UCDetectorBasePreferencePage {

  @Override
  public void createFieldEditors() {
    Composite parentGroups = createComposite(getFieldEditorParent(), 1, 1,
        GridData.FILL_HORIZONTAL);
    createKeywordGroup(parentGroups);
  }

  /**
   * Create a group of keyword settings
   * (analyze visibility, try to make methods or fields final)
   */
  private void createKeywordGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups,
        Messages.PreferencePage_GroupKeyWord, 1, 1, GridData.FILL_HORIZONTAL);
    ComboFieldEditor analyzeVisibility = createCombo(
        Prefs.ANALYZE_VISIBILITY_PROTECTED,
        Messages.PreferencePage_CheckVisibilityProtected, spacer);
    this.addField(analyzeVisibility);
    ComboFieldEditor analyzeVisibilityPrivate = createCombo(
        Prefs.ANALYZE_VISIBILITY_PRIVATE,
        Messages.PreferencePage_CheckVisibilityPrivate, spacer);
    this.addField(analyzeVisibilityPrivate);
  }
}
