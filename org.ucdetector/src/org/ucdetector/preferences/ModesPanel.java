/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
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
import org.eclipse.swt.widgets.Widget;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

class ModesPanel {
  private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$
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
  private Button newButton;
  private Button removeButton;
  private Combo modesCombo;
  private final File modesDir;
  private final Composite parent;
  private final UCDetectorPreferencePage page;

  ModesPanel(UCDetectorPreferencePage page, Composite parentGroups) {
    this.page = page;
    this.parent = parentGroups;
    File ucdDir = UCDetectorPlugin.getDefault().getStateLocation().toFile();
    modesDir = new File(ucdDir, "modes"); //$NON-NLS-1$
    modesDir.mkdirs();
  }

  Combo createModeCombo() {
    Composite spacer = UCDetectorPreferencePage.createComposite(parent, 5, 1, GridData.FILL_HORIZONTAL);
    Label label = new Label(spacer, SWT.LEFT);
    label.setText(Messages.PreferencePage_ModeLabel);
    modesCombo = new Combo(spacer, SWT.READ_ONLY);

    newButton = new Button(spacer, SWT.PUSH);
    newButton.setText(Messages.PreferencePage_ModeNew);
    removeButton = new Button(spacer, SWT.PUSH);
    removeButton.setText(Messages.PreferencePage_ModeRemove);
    saveButton = new Button(spacer, SWT.PUSH);
    saveButton.setText(Messages.PreferencePage_ModeSave);
    //
    String[] modes = getModes();
    getCombo().setItems(modes);
    int savedIndex = page.getPreferenceStore().getInt(Prefs.MODE_INDEX);
    boolean isValidIndex = (savedIndex >= 0 && savedIndex < modes.length);
    getCombo().setText(isValidIndex ? modes[savedIndex] : Mode.Default.toStringLocalized());

    getCombo().addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent evt) {
        updateModeButtons();
        String modesFileName = null;
        try {
          int index = getCombo().getSelectionIndex();
          if (index == -1) {
            // ignore
          }
          // default
          else if (index == Mode.Default.ordinal()) {
            page.performDefaults();
          }
          // built-in
          else if (index < Mode.values().length) {
            Mode mode = Mode.values()[index];
            modesFileName = mode + MODES_FILE_TYPE;
            setPreferences(getClass().getResourceAsStream(modesFileName));
          }
          // custom
          else {
            String newMode = getCombo().getText();
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
          saveMode(getCombo().getText());
        }
        else if (widget == newButton) {
          addMode();
        }
        else if (widget == removeButton) {
          removeMode();
        }
      }
    };
    updateModeButtons();
    saveButton.addSelectionListener(selectionListener);
    newButton.addSelectionListener(selectionListener);
    removeButton.addSelectionListener(selectionListener);
    return getCombo();
  }

  /** Get built in modes and user specific modes */
  private String[] getModes() {
    Set<String> result = new LinkedHashSet<String>();
    for (Mode mode : Mode.values()) {
      result.add(mode.toStringLocalized());
    }
    for (String modesFile : modesDir.list()) {
      if (modesFile.endsWith(MODES_FILE_TYPE)) {
        result.add(modesFile.substring(0, modesFile.length() - MODES_FILE_TYPE.length()));
      }
    }
    if (Log.DEBUG) {
      Log.logDebug("Available modes are: " + result); //$NON-NLS-1$
    }
    return result.toArray(new String[result.size()]);
  }

  /** Add a user specific mode, and save it to a file */
  private void addMode() {
    IInputValidator validator = new IInputValidator() {
      public String isValid(String fileName) {
        boolean isValid = !fileName.matches(".*[\\\\/:*?|<>\"].*"); //$NON-NLS-1$
        return isValid ? null : NLS.bind(Messages.ModesPanel_invalid_mode_name, fileName);
      }
    };
    InputDialog input = new InputDialog(parent.getShell(), Messages.PreferencePage_NewMode,
        Messages.PreferencePage_ModeName, null, validator);
    input.open();
    String newModeName = input.getValue();
    if (newModeName != null && newModeName.trim().length() > 0) {
      saveMode(newModeName);
      Log.logDebug("Added new mode: " + newModeName); //$NON-NLS-1$
      getCombo().setItems(getModes());
      getCombo().setText(newModeName);
      updateModeButtons();
    }
  }

  /** Save it to a file in WORKSPACE/.metadata/.plugins/org.ucdetector/modes  */
  private void saveMode(String modeName) {
    page.performOk();
    Map<String, String> allPreferences = UCDetectorPlugin.getAllPreferences();
    allPreferences.putAll(UCDetectorPlugin.getDeltaPreferences());

    StringBuilder sb = new StringBuilder();
    sb.append("# ").append(UCDetectorPlugin.SEPARATOR).append(NL);//$NON-NLS-1$
    sb.append("# UCDetector mode preference file").append(NL);//$NON-NLS-1$
    sb.append("# ").append(UCDetectorPlugin.SEPARATOR).append(NL);//$NON-NLS-1$
    sb.append("# Mode        : " + modeName).append(NL);//$NON-NLS-1$
    sb.append("# Created by  : " + getClass().getName()).append(NL);//$NON-NLS-1$
    sb.append("# Created date: " + UCDetectorPlugin.getNow()).append(NL);//$NON-NLS-1$
    sb.append("# ").append(UCDetectorPlugin.SEPARATOR).append(NL);//$NON-NLS-1$
    for (String extendedPreference : page.extendedPreferences) {
      if (extendedPreference.startsWith(UCDetectorPreferencePage.TAB_START)) {
        sb.append(NL);
        sb.append(UCDetectorPreferencePage.TAB_START).append(UCDetectorPlugin.SEPARATOR).append(NL);
        sb.append(extendedPreference).append(NL);
        sb.append(UCDetectorPreferencePage.TAB_START).append(UCDetectorPlugin.SEPARATOR);
      }
      else if (extendedPreference.startsWith(UCDetectorPreferencePage.GROUP_START)) {
        sb.append(extendedPreference);
      }
      else {
        sb.append(extendedPreference).append('=').append(allPreferences.get(extendedPreference));
        allPreferences.remove(extendedPreference);
      }
      sb.append(NL);
    }
    sb.append(NL);
    //
    if (Log.DEBUG) {
      Log.logDebug(sb.toString());
    }
    // org.ucdetector.mode.index, old entries
    Log.logDebug("Unhandled preferences :" + allPreferences); //$NON-NLS-1$
    File modesFile = getModesFile(modeName);
    try {
      FileWriter writer = new FileWriter(modesFile);
      writer.write(sb.toString());
      writer.close();
      //      properties.store(new FileOutputStream(modesFile), "Created by " + getClass().getName()); //$NON-NLS-1$
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
    String modeToRemove = getCombo().getText();
    File file = getModesFile(modeToRemove);
    file.delete();
    Log.logDebug("Deleted mode file: " + file.getAbsolutePath()); //$NON-NLS-1$
    getCombo().setItems(getModes());
    getCombo().setText(Mode.Default.toStringLocalized());
    page.performDefaults();
    updateModeButtons();
  }

  /** save and remove buttons are only enabled for custom modes */
  private void updateModeButtons() {
    int index = getCombo().getSelectionIndex();
    boolean enabled = (index < 0 || index >= Mode.values().length);
    saveButton.setEnabled(enabled);
    removeButton.setEnabled(enabled);
  }

  /**
   * @param preferencesFile Set preferences from selected file
   */
  private void setPreferences(InputStream in) throws IOException {
    PreferenceStore tempReplaceStore = new PreferenceStore();
    // Put default values
    Set<Entry<String, String>> entrySet = UCDetectorPlugin.getAllPreferences().entrySet();
    for (Entry<String, String> entry : entrySet) {
      tempReplaceStore.putValue(entry.getKey(), entry.getValue());
    }
    tempReplaceStore.load(in);
    for (FieldEditor field : page.fields) {
      IPreferenceStore originalStore = field.getPreferenceStore();
      field.setPreferenceStore(tempReplaceStore);
      field.load();
      field.setPreferenceStore(originalStore);
    }
    // checkState();
  }

  Combo getCombo() {
    return modesCombo;
  }
}
