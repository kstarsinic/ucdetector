/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import static org.ucdetector.preferences.UCDetectorPreferencePage.GROUP_START;
import static org.ucdetector.preferences.UCDetectorPreferencePage.TAB_START;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

class ModesPanel {
  private static final String MODES_FILE_TYPE = ".properties"; //$NON-NLS-1$

  /** built-in preferences mode */
  enum Mode {
    Default, classes_only, full;

    String toStringLocalized() {
      // Reflection!
      return Messages.getString("ModesPanel_mode_" + name(), name()) + " [built-in]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  private final Button newButton;
  private final Button removeButton;
  private final Button renameButton;
  private final Combo modesCombo;
  private final File modesDir;
  private final Composite parent;
  private final Composite modesPanelComposite;
  private final UCDetectorPreferencePage page;

  ModesPanel(UCDetectorPreferencePage page, Composite parentGroups) {
    this.page = page;
    this.parent = parentGroups;
    File ucdDir = UCDetectorPlugin.getDefault().getStateLocation().toFile();
    modesDir = new File(ucdDir, "modes"); //$NON-NLS-1$
    modesDir.mkdirs();
    Log.logInfo("modesDir is '%s'", modesDir.getAbsolutePath()); //$NON-NLS-1$
    modesPanelComposite = UCDetectorPreferencePage.createComposite(parent, 5, 1, GridData.FILL_HORIZONTAL);
    Label label = new Label(modesPanelComposite, SWT.LEFT);
    label.setText(Messages.ModesPanel_ModeLabel);
    modesCombo = new Combo(modesPanelComposite, SWT.READ_ONLY);
    newButton = new Button(modesPanelComposite, SWT.PUSH);
    removeButton = new Button(modesPanelComposite, SWT.PUSH);
    renameButton = new Button(modesPanelComposite, SWT.PUSH);
    createButtonsDetails();
    createModeComboDetails();
  }

  private void createButtonsDetails() {
    newButton.setText(Messages.ModesPanel_ModeNew);
    removeButton.setText(Messages.ModesPanel_ModeRemove);
    renameButton.setText(Messages.ModesPanel_ModeRename);
    SelectionListener selectionListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        if (event.widget == newButton) {
          addMode();
        }
        else if (event.widget == removeButton) {
          removeMode();
        }
        else if (event.widget == renameButton) {
          remameMode();
        }
      }
    };
    newButton.addSelectionListener(selectionListener);
    removeButton.addSelectionListener(selectionListener);
    renameButton.addSelectionListener(selectionListener);
  }

  private void createModeComboDetails() {
    String[] modes = getModes();
    getCombo().setItems(modes);
    // Default first
    getCombo().setText(Mode.Default.toStringLocalized());
    getCombo().setText(Prefs.getModeName());
    getCombo().setToolTipText(modesDir.getAbsolutePath());

    getCombo().addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent evt) {
        updateModeButtons();
        int index = getCombo().getSelectionIndex();
        if (index == -1) {
          // ignore
        }
        else if (index == Mode.Default.ordinal()) {
          // default
          page.performDefaults();
        }
        else if (index < Mode.values().length) {
          // built-in
          Mode builtInMode = Mode.values()[index];
          String modesFileName = builtInMode + MODES_FILE_TYPE;
          setPreferences(false, modesFileName);
        }
        else {
          // custom
          setCustomPreferences();
        }
      }
    });
  }

  private String setCustomPreferences() {
    String customMode = getCombo().getText();
    String modesFileName = getModesFile(customMode).getAbsolutePath();
    setPreferences(true, modesFileName);
    return modesFileName;
  }

  /** Get built in modes and user specific modes */
  private String[] getModes() {
    Set<String> result = new LinkedHashSet<String>();
    for (Mode mode : Mode.values()) {
      result.add(mode.toStringLocalized());
    }
    List<String> modesFiles = new ArrayList<String>(Arrays.asList(modesDir.list()));
    Collections.sort(modesFiles, String.CASE_INSENSITIVE_ORDER);
    for (String modesFile : modesFiles) {
      if (modesFile.endsWith(MODES_FILE_TYPE)) {
        result.add(modesFile.substring(0, modesFile.length() - MODES_FILE_TYPE.length()));
      }
    }
    if (Log.isDebug()) {
      Log.logDebug("Available modes are: %s", result); //$NON-NLS-1$
    }
    return result.toArray(new String[result.size()]);
  }

  /** Add a user specific mode, and save it to a file */
  private void addMode() {
    String newName = "CopyOf_" + getActiveModeName(); //$NON-NLS-1$
    InputDialog input = new InputDialog(parent.getShell(), Messages.ModesPanel_NewMode, Messages.ModesPanel_ModeName,
        newName, new ValidFileNameValidator());
    input.open();
    addMode(input.getValue());
  }

  /** Get name of mode from combo without 'built-in' */
  private String getActiveModeName() {
    int index = getCombo().getSelectionIndex();
    boolean isBuiltIn = index != -1 && index < Mode.values().length;
    return (isBuiltIn ? Mode.values()[index].name() : getCombo().getText());
  }

  private void addMode(String newModeName) {
    page.performOk();
    if (newModeName != null && newModeName.trim().length() > 0) {
      // set default, when report directory is missing
      Prefs.getStore().setValue(Prefs.REPORT_DIR, PreferenceInitializer.getReportDir());
      saveMode(newModeName);
      Log.logInfo("Added new mode: %s", newModeName); //$NON-NLS-1$
      getCombo().setItems(getModes());
      getCombo().setText(newModeName);
      updateModeButtons();
      setCustomPreferences();
    }
  }

  /** Uses the stored settings (to create a custom mode) */
  protected void createMyMode() {
    String version = Prefs.getStore().getString(Prefs.PREFS_VERSION);
    if (version.length() == 0) {
      Log.logInfo("Adding mode: MyMode"); //$NON-NLS-1$
      addMode("MyMode"); //$NON-NLS-1$
      Prefs.getStore().setValue(Prefs.PREFS_VERSION, UCDetectorPlugin.getAboutUCDVersion());
    }
  }

  void saveMode() {
    if (getCombo().getSelectionIndex() >= Mode.values().length) {
      saveMode(getCombo().getText()); // custom
    }
  }

  /** Save it to a file in WORKSPACE/.metadata/.plugins/org.ucdetector/modes  */
  @SuppressWarnings("nls")
  private void saveMode(String modeName) {
    Map<String, String> allPreferences = UCDetectorPlugin.getAllPreferences();
    allPreferences.putAll(UCDetectorPlugin.getDeltaPreferences());
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("### -------------------------------------------------------------------------%n"));
    sb.append(String.format("###               UCDetector preference file for mode: '%s'%n", modeName));
    sb.append(String.format("### -------------------------------------------------------------------------%n"));
    sb.append(String.format("### Created by  : UCDetector %s%n", UCDetectorPlugin.getAboutUCDVersion()));
    sb.append(String.format("### Created date: %s%n", UCDetectorPlugin.getNow()));
    sb.append(String.format("### This class can't be loaded by java.util.Properties.load()%n"));
    sb.append(String.format("### -------------------------------------------------------------------------%n"));
    for (String extendedPreference : page.extendedPreferences) {
      if (extendedPreference.startsWith(TAB_START)) {
        sb.append(String.format("%n## --------------------------------------------------------------------------%n"));
        sb.append(String.format("## Tab: %s%n", extendedPreference.substring(TAB_START.length())));
        sb.append(String.format("## --------------------------------------------------------------------------%n"));
      }
      else if (extendedPreference.startsWith(GROUP_START)) {
        sb.append(String.format("%n# Group: %s%n", extendedPreference.substring(GROUP_START.length())));
      }
      else {
        sb.append(String.format("%s=%s%n", extendedPreference, allPreferences.get(extendedPreference)));
        allPreferences.remove(extendedPreference);
      }
    }
    String fileText = sb.toString();
    if (Log.isDebug()) {
      Log.logDebug(fileText);
      Log.logDebug("Unhandled preferences :" + allPreferences);
    }
    File modesFile = getModesFile(modeName);
    FileWriter writer = null;
    try {
      writer = new FileWriter(modesFile);
      writer.write(fileText);
      Log.logDebug("Saved mode to: %s", modesFile.getAbsolutePath()); //$NON-NLS-1$
    }
    catch (IOException ex) {
      String message = NLS.bind(Messages.ModesPanel_ModeFileCantSave, modesFile.getAbsolutePath());
      UCDetectorPlugin.logErrorAndStatus(message, ex);
    }
    finally {
      UCDetectorPlugin.closeSave(writer);
    }
  }

  /**
   * [ 3025571 ] Exception loading modes: Malformed  &#92;uxxxx encoding
   * <p>
   * java.util.Properties.load() fails, because of file names containing Strings (file names)
   *  which are similar to unicode signs
   */
  public static Map<String, String> loadMode(boolean isFile, String modesFileName) {
    Map<String, String> result = new HashMap<String, String>();
    BufferedReader reader = null;
    try {
      InputStream in = isFile ? new FileInputStream(modesFileName) : ModesPanel.class
          .getResourceAsStream(modesFileName);
      reader = new BufferedReader(new InputStreamReader(in));
      String line = null;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        int index = line.indexOf('=');
        if (line.startsWith("#") || index == -1) { //$NON-NLS-1$
          continue;// comment
        }
        String key = line.substring(0, index);
        String value = (line.length() == index ? "" : line.substring(index + 1)); //$NON-NLS-1$
        result.put(key, value);
      }
    }
    catch (IOException ex) {
      String message = NLS.bind(Messages.ModesPanel_CantSetPreferences, modesFileName);
      UCDetectorPlugin.logErrorAndStatus(message, ex);
    }
    finally {
      UCDetectorPlugin.closeSave(reader);
    }
    return result;
  }

  private File getModesFile(String modeName) {
    return new File(modesDir, modeName + MODES_FILE_TYPE);
  }

  private void removeMode() {
    String modeToRemove = getCombo().getText();
    boolean doRemove = MessageDialog.openQuestion(parent.getShell(), Messages.ModesPanel_ModeRemove,
        Messages.ModesPanel_ModeRemoveQuestion + modeToRemove);
    if (!doRemove) {
      return;
    }
    File file = getModesFile(modeToRemove);
    boolean deleteOk = file.delete();
    Log.logSuccess(deleteOk, String.format("Delete mode '%s' - file is %s", modeToRemove, file.getAbsolutePath()));//$NON-NLS-1$
    getCombo().setItems(getModes());
    getCombo().setText(Mode.Default.toStringLocalized());
    page.performDefaults();
    updateModeButtons();
  }

  private void remameMode() {
    String oldName = getActiveModeName();
    InputDialog input = new InputDialog(parent.getShell(), Messages.ModesPanel_ModeRename,
        Messages.ModesPanel_ModeName, oldName, new ValidFileNameValidator());
    input.open();
    String newName = input.getValue();
    File oldModesFile = getModesFile(oldName);
    File newModesFile = getModesFile(newName);
    boolean renameOK = oldModesFile.renameTo(newModesFile);
    Log.logSuccess(renameOK,
        String.format("Rename mode '%s' to '%s' - new file: %s", oldName, newName, newModesFile.getAbsolutePath()));//$NON-NLS-1$
    getCombo().setItems(getModes());
    getCombo().setText(newName);
  }

  /** buttons 'save' and 'remove' are only enabled for custom modes */
  protected void updateModeButtons() {
    int index = getCombo().getSelectionIndex();
    boolean isCustom = (index < 0 || index >= Mode.values().length);
    removeButton.setEnabled(isCustom);
    renameButton.setEnabled(isCustom);

    for (Composite group : page.groups) {
      Control[] controls = group.getChildren();
      for (Control control : controls) {
        control.setEnabled(isCustom);
      }
    }
    page.setMessage(isCustom ? null : Messages.ModesPanel_ModePressNewHint, IMessageProvider.WARNING);
  }

  /**
   * @param preferencesFile Set preferences from selected file
   */
  private void setPreferences(boolean isFile, String modesFilename) {
    PreferenceStore tempReplaceStore = new PreferenceStore();
    // Put default values
    Map<String, String> allPreferences = UCDetectorPlugin.getAllPreferences();
    addAll(tempReplaceStore, allPreferences);
    Map<String, String> savedMode = loadMode(isFile, modesFilename);
    addAll(tempReplaceStore, savedMode);
    for (FieldEditor field : page.fields) {
      IPreferenceStore originalStore = field.getPreferenceStore();
      field.setPreferenceStore(tempReplaceStore);
      field.load();
      field.setPreferenceStore(originalStore);
    }
  }

  private void addAll(PreferenceStore tempReplaceStore, Map<String, String> allPreferences) {
    Set<Entry<String, String>> entrySet = allPreferences.entrySet();
    for (Entry<String, String> entry : entrySet) {
      tempReplaceStore.putValue(entry.getKey(), entry.getValue());
    }
  }

  Combo getCombo() {
    return modesCombo;
  }

  private final class ValidFileNameValidator implements IInputValidator {
    public String isValid(String fileName) {
      String[] modes = getModes();
      for (String mode : modes) {
        if (mode.equals(fileName)) {
          return Messages.ModesPanel_ModeAlreadyExists;
        }
      }
      boolean isValidFileName = !fileName.matches(".*[\\\\/:*?|<>\"].*"); //$NON-NLS-1$
      return isValidFileName ? null : NLS.bind(Messages.ModesPanel_invalid_mode_name, fileName);
    }
  }
}
