/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import java.lang.reflect.Method;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TypedListener;
import org.ucdetector.Messages;

/**
 * See also feature request:
 * [ 2490344 ] filters options for visibility (fields, constants, methods)
 * <p>
 * https://sourceforge.net/tracker2/?func=detail&aid=2490344&group_id=219599&atid=1046868
 */
public class UCDetectorPreferencePageKeywords extends
    UCDetectorBasePreferencePage {
  private Method getComboListeners;
  private Combo changeVisibiliyCombos;

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
    //
    addChangeAllVisibiliyCombo(spacer);
    //
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

    // [ 2804064 ] Access to enclosing type - make 2743908 configurable
    BooleanFieldEditor ignoreSyntheticAccessEmulation = new BooleanFieldEditor(
        Prefs.IGNORE_SYNTHETIC_ACCESS_EMULATION, "    " //$NON-NLS-1$
            + Messages.PreferencePage_ignoreSyntheticAccessEmulation,
        BooleanFieldEditor.SEPARATE_LABEL, spacer);
    ignoreSyntheticAccessEmulation.getLabelControl(spacer).setToolTipText(
        Messages.PreferencePage_ignoreSyntheticAccessEmulationTooltip);
    this.addField(ignoreSyntheticAccessEmulation);
    addLineHack(spacer);

    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PROTECTED_CONSTANTS,
        Messages.PreferencePage_CheckProtectedConstants, spacer));
    this.addField(createCombo(Prefs.ANALYZE_VISIBILITY_PRIVATE_CONSTANTS,
        Messages.PreferencePage_CheckPrivateConstants, spacer));
  }

  // [2810803] Change visibility options more comfortable
  private void addChangeAllVisibiliyCombo(Composite parent) {
    try {
      Class<?>[] parameterTypes = new Class[] { int.class };
      getComboListeners = Combo.class.getMethod("getListeners", parameterTypes); //$NON-NLS-1$
    }
    catch (Exception e) {
      getComboListeners = null;
      // eclipse 3.3!!!
      return;
    }
    Label label = new Label(parent, SWT.LEFT);
    label.setText(Messages.UCDetectorPreferencePageKeywords_ChangeAllCombos);
    changeVisibiliyCombos = new Combo(parent, SWT.READ_ONLY);
    changeVisibiliyCombos.setItems(new String[] {
        WarnLevel.ERROR.toStringLocalized(),
        WarnLevel.WARNING.toStringLocalized(),
        WarnLevel.IGNORE.toStringLocalized() });
    changeVisibiliyCombos.setText(WarnLevel.WARNING.toStringLocalized());
    changeVisibiliyCombos.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent evt) {
        Control[] children = changeVisibiliyCombos.getParent().getChildren();
        for (Control control : children) {
          if (control instanceof Combo && control != changeVisibiliyCombos) {
            Combo visibilityCombo = (Combo) control;
            visibilityCombo.setText(changeVisibiliyCombos.getText());
            createSelectionEventHack(visibilityCombo);
          }
        }
      }

      /**
       *  It is not enough to call Combo.setText().
       *  We must create a selection event for ComboFieldEditor#fCombo
       *  */
      private void createSelectionEventHack(Combo visibilityCombo) {
        Listener[] listeners = getListeners(visibilityCombo);
        if (listeners.length == 0) {
          return;
        }
        TypedListener listener = (TypedListener) listeners[0];
        ((SelectionListener) listener.getEventListener()).widgetSelected(null);
      }

      /**
       * Compatibility eclipse 3.3:
       * The method <code>combo.getListeners(SWT.Selection)</code> only exists in 3.4
       */
      private Listener[] getListeners(Combo visibilityCombo) {
        try {
          Integer selection = Integer.valueOf(SWT.Selection);
          return (Listener[]) getComboListeners.invoke(visibilityCombo,
              new Object[] { selection });
        }
        catch (Exception e) {
          // ignore
        }
        return new Listener[0];
      }
    });
  }

  @Override
  public boolean performOk() {
    boolean performOk = super.performOk();
    if (getComboListeners != null) {
      getPreferenceStore().setValue(Prefs.CHANGE_ALL_VISIBILIY_COMBO,
          changeVisibiliyCombos.getSelectionIndex());
    }
    return performOk;
  }

  @Override
  protected void initialize() {
    super.initialize();
    if (getComboListeners != null) {
      int index = getPreferenceStore().getInt(Prefs.CHANGE_ALL_VISIBILIY_COMBO);
      changeVisibiliyCombos.select(index);
    }
  }

  @Override
  protected void performDefaults() {
    super.performDefaults();
    if (getComboListeners != null) {
      changeVisibiliyCombos.select(WarnLevel.WARNING.ordinal());
    }
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
