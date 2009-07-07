/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.ucdetector.Messages;

/**
 * See also feature request:
 * [ 2490344 ] filters options for visibility (fields, constants, methods)
 * <p>
 * https://sourceforge.net/tracker2/?func=detail&aid=2490344&group_id=219599&atid=1046868
 */
public class UCDetectorPreferencePageKeywords extends
    UCDetectorBasePreferencePage {

  public UCDetectorPreferencePageKeywords() {
    super(FieldEditorPreferencePage.GRID, Prefs.getStore());
  }

  @Override
  public void createFieldEditors() {
    Composite parentGroups = createComposite(getFieldEditorParent(), 1, 1,
        GridData.FILL_HORIZONTAL);
    createKeywordGroup(parentGroups);
    createVisibilityGroupClasses(parentGroups);
  }

  /**
   * Create a group of keyword settings
   * (analyze visibility, try to make methods or fields final)
   */
  private void createKeywordGroup(Composite parentGroups) {
    Composite spacer = createGroup(parentGroups,
        Messages.PreferencePage_GroupFinal, 1, 1, GridData.FILL_HORIZONTAL);
    // FINAL -------------------------------------------------------------------
    ComboFieldEditor analyzeFinalMethod = createCombo(
        Prefs.ANALYZE_FINAL_METHOD, Messages.PreferencePage_CheckFinalMethod,
        spacer);
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
    Composite spacer = createGroup(parentGroups,
        Messages.PreferencePage_GroupVisibility, 1, 1, GridData.FILL_HORIZONTAL);
    addLineHack(spacer);
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_CLASSES,
        Messages.PreferencePage_CheckProtectedClasses, spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_CLASSES,
        Messages.PreferencePage_CheckPrivateClasses, spacer));
    addLineHack(spacer);

    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_METHODS,
        Messages.PreferencePage_CheckProtectedMethods, spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_METHODS,
        Messages.PreferencePage_CheckPrivateMethods, spacer));
    addLineHack(spacer);

    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_FIELDS,
        Messages.PreferencePage_CheckProtectedFields, spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_FIELDS,
        Messages.PreferencePage_CheckPrivateFields, spacer));
    addLineHack(spacer);

    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_CONSTANTS,
        Messages.PreferencePage_CheckProtectedConstants, spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_CONSTANTS,
        Messages.PreferencePage_CheckPrivateConstants, spacer));

  }

  private void addLineHack(Composite spacer) {
    //    Composite composite = createComposite(spacer, 1, 1, GridData.FILL_HORIZONTAL);
    //    Label label = new Label(spacer, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.WRAP);
    //    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    //    label.setLayoutData(gd);
    new Label(spacer, SWT.WRAP);
    new Label(spacer, SWT.WRAP);
  }
}
