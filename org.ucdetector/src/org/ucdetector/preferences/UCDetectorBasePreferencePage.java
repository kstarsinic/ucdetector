/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;

/**
 *
 */
public abstract class UCDetectorBasePreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {
  private final List<FieldEditor> fields = new ArrayList<FieldEditor>();
  /**
   * entryNames (first column) and values (second column) for the
   * ComboFieldEditor
   */
  private static final String[][] WARN_LEVELS = new String[][] {
      { WarnLevel.ERROR.toStringLocalized(), WarnLevel.ERROR.toString() },
      { WarnLevel.WARNING.toStringLocalized(), WarnLevel.WARNING.toString() },
      { WarnLevel.IGNORE.toStringLocalized(), WarnLevel.IGNORE.toString() } };

  public UCDetectorBasePreferencePage(int style, IPreferenceStore store) {
    super(style);
    this.setPreferenceStore(store);
  }

  public void init(IWorkbench workbench) {
    //
  }

  @Override
  public boolean performOk() {
    boolean result = super.performOk();
    Log.logInfo("New preferences: " + UCDetectorPlugin.getPreferencesAsString()); //$NON-NLS-1$
    return result;
  }

  @Override
  protected void performApply() {
    super.performApply();
    //    dumpPreferencesPerPage();
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
  protected void performDefaults() {
    super.performDefaults();
    //    setPreferences("class_only.properties");
  }

  /**
   * @param preferencesFile Set preferences from selected file
   */
  void setPreferences(String preferencesFile) {
    PreferenceStore tempReplaceStore = new PreferenceStore();
    InputStream in = getClass().getResourceAsStream(preferencesFile);
    // Put default values
    Set<Entry<String, String>> entrySet = UCDetectorPlugin.getAllPreferences().entrySet();
    for (Entry<String, String> entry : entrySet) {
      tempReplaceStore.putValue(entry.getKey(), entry.getValue());
    }
    try {
      tempReplaceStore.load(in);
      for (FieldEditor field : fields) {
        IPreferenceStore originalStore = field.getPreferenceStore();
        field.setPreferenceStore(tempReplaceStore);
        field.load();
        field.setPreferenceStore(originalStore);
      }
      checkState();
    }
    catch (IOException e) {
      Log.logError("Can't load preferences", e); //$NON-NLS-1$
    }
  }

  @Override
  protected void addField(FieldEditor editor) {
    fields.add(editor);
    super.addField(editor);
  }

  // -------------------------------------------------------------------------
  // HELPER
  // -------------------------------------------------------------------------

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
    g.setLayout(new GridLayout(columns, false));
    g.setText(text);
    GridData gd = new GridData(fill);
    gd.horizontalSpan = hspan;
    g.setLayoutData(gd);
    Composite spacer = createComposite(g, 1, 1, GridData.FILL_HORIZONTAL);
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
}
