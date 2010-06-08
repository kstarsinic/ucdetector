/**
 * Copyright (c) 2010 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import static org.ucdetector.preferences.UCDetectorPreferencePage.GROUP_START;
import static org.ucdetector.preferences.UCDetectorPreferencePage.TAB_START;

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
import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

class ModesPanel {
  //  private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$
  private static final String MODES_FILE_TYPE = ".properties"; //$NON-NLS-1$

  /** built-in preferences mode */
  enum Mode {
    Default, //
    classes_only, //
    full, //
    ;

    String toStringLocalized() {
      // Reflection!
      return Messages.getString("ModesPanel_mode_" + name(), name()) + " [built-in]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  //  private final Button saveButton;
  private final Button newButton;
  private final Button removeButton;
  private final Combo modesCombo;
  private final File modesDir;
  private final Composite parent;
  private final Composite modesPanelComposite;
  //  private final Label builtInInfoLabel;
  private final UCDetectorPreferencePage page;

  ModesPanel(UCDetectorPreferencePage page, Composite parentGroups) {
    this.page = page;
    this.parent = parentGroups;
    File ucdDir = UCDetectorPlugin.getDefault().getStateLocation().toFile();
    modesDir = new File(ucdDir, "modes"); //$NON-NLS-1$
    modesDir.mkdirs();
    modesPanelComposite = UCDetectorPreferencePage.createComposite(parent, 4, 1, GridData.FILL_HORIZONTAL);
    Label label = new Label(modesPanelComposite, SWT.LEFT);
    label.setText(Messages.ModesPanel_ModeLabel);
    modesCombo = new Combo(modesPanelComposite, SWT.READ_ONLY);
    newButton = new Button(modesPanelComposite, SWT.PUSH);
    newButton.setText(Messages.ModesPanel_ModeNew);
    removeButton = new Button(modesPanelComposite, SWT.PUSH);
    removeButton.setText(Messages.ModesPanel_ModeRemove);
    //    saveButton = new Button(modesPanelComposite, SWT.PUSH);
    //    saveButton.setText(Messages.ModesPanel_ModeSave);
    //
    //    builtInInfoLabel = new Label(modesPanelComposite, SWT.LEFT);
    //    builtInInfoLabel.setFont(new Font(parent.getDisplay(), "Arial", 10, SWT.BOLD));
    //    builtInInfoLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
    //    builtInInfoLabel.setText(NLS.bind(Messages.ModesPanel_ModePressNewHint, Messages.ModesPanel_ModeNew));

    createModeCombo();
  }

  private void createModeCombo() {
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
          String message = NLS.bind(Messages.ModesPanel_CantSetPreferences, modesFileName);
          UCDetectorPlugin.logErrorAndStatus(message, ex);
        }
      }
    });
    //
    SelectionListener selectionListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        Widget widget = event.widget;
        /*if (widget == saveButton) {
          saveMode(getCombo().getText());
        }
        else */
        if (widget == newButton) {
          addMode();
        }
        else if (widget == removeButton) {
          removeMode();
        }
      }
    };
    // updateModeButtons();
    //    saveButton.addSelectionListener(selectionListener);
    newButton.addSelectionListener(selectionListener);
    removeButton.addSelectionListener(selectionListener);
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
      Log.logDebug("Available modes are: %s", result); //$NON-NLS-1$
    }
    return result.toArray(new String[result.size()]);
  }

  /** Add a user specific mode, and save it to a file */
  private void addMode() {
    IInputValidator validator = new IInputValidator() {
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
    };
    int index = getCombo().getSelectionIndex();
    boolean isBuiltIn = index != -1 && index < Mode.values().length;
    String newName = "CopyOf_" + (isBuiltIn ? Mode.values()[index].name() : getCombo().getText()); //$NON-NLS-1$
    InputDialog input = new InputDialog(parent.getShell(), Messages.ModesPanel_NewMode, Messages.ModesPanel_ModeName,
        newName, validator);
    input.open();
    String newModeName = input.getValue();
    addMode(newModeName);
  }

  private void addMode(String newModeName) {
    page.performOk();
    if (newModeName != null && newModeName.trim().length() > 0) {
      saveMode(newModeName);
      Log.logDebug("Added new mode: %s", newModeName); //$NON-NLS-1$
      getCombo().setItems(getModes());
      getCombo().setText(newModeName);
      updateModeButtons();
    }
  }

  /** Uses the stored settings (to create a custom mode) */
  protected void createMyMode() {
    String version = Prefs.getStore().getString(Prefs.PREFS_VERSION);
    if (version == null || version.length() == 0) {
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
  void saveMode(String modeName) {
    //    page.performOk();
    Map<String, String> allPreferences = UCDetectorPlugin.getAllPreferences();
    allPreferences.putAll(UCDetectorPlugin.getDeltaPreferences());

    StringBuilder sb = new StringBuilder();
    sb.append(String.format("### ----------------------------------------------------------------------------%n"));
    sb.append(String.format("###               UCDetector preference file for mode: '%s'%n", modeName));
    sb.append(String.format("### ----------------------------------------------------------------------------%n"));
    sb.append(String.format("### Created by  : UCDetector %s%n", UCDetectorPlugin.getAboutUCDVersion()));
    sb.append(String.format("### Created date: %s%n", UCDetectorPlugin.getNow()));
    sb.append(String.format("### ----------------------------------------------------------------------------%n"));
    for (String extendedPreference : page.extendedPreferences) {
      if (extendedPreference.startsWith(TAB_START)) {
        sb.append(String.format("%n## -----------------------------------------------------------------------------%n"));
        sb.append(String.format("## Tab: %s%n", extendedPreference.substring(TAB_START.length())));
        sb.append(String.format("## -----------------------------------------------------------------------------%n"));
      }
      else if (extendedPreference.startsWith(GROUP_START)) {
        sb.append(String.format("%n# Group: %s%n", extendedPreference.substring(GROUP_START.length())));
      }
      else {
        sb.append(String.format("%s=%s%n", extendedPreference, allPreferences.get(extendedPreference)));
        allPreferences.remove(extendedPreference);
      }
    }
    //
    if (Log.DEBUG) {
      Log.logDebug(sb.toString());
    }
    // org.ucdetector.mode.index, old entries
    Log.logDebug("Unhandled preferences :" + allPreferences);
    File modesFile = getModesFile(modeName);
    try {
      FileWriter writer = new FileWriter(modesFile);
      writer.write(sb.toString());
      writer.close();
      Log.logDebug("Saved mode to: %s", modesFile.getAbsolutePath()); //$NON-NLS-1$
    }
    catch (IOException ex) {
      String message = NLS.bind(Messages.ModesPanel_ModeFileCantSave, modesFile.getAbsolutePath());
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
    Log.logDebug("Deleted mode file: %s", file.getAbsolutePath()); //$NON-NLS-1$
    getCombo().setItems(getModes());
    getCombo().setText(Mode.Default.toStringLocalized());
    page.performDefaults();
    updateModeButtons();
  }

  /** buttons 'save' and 'remove' are only enabled for custom modes */
  protected void updateModeButtons() {
    int index = getCombo().getSelectionIndex();
    boolean isCustom = (index < 0 || index >= Mode.values().length);
    //    saveButton.setEnabled(isCustom);
    removeButton.setEnabled(isCustom);

    for (Composite group : page.groups) {
      Control[] controls = group.getChildren();
      for (Control control : controls) {
        control.setEnabled(isCustom);
      }
    }
    //    builtInInfoLabel.setVisible(!isCustom);
    page.setMessage(isCustom ? null : Messages.ModesPanel_ModePressNewHint, IMessageProvider.WARNING);
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
