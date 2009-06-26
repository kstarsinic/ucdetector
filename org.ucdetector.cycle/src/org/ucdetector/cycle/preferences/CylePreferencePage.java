/**
 * Copyright (c) 2008 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.ucdetector.cycle.CyclePlugin;
import org.ucdetector.cycle.Messages;
import org.ucdetector.preferences.UCDetectorBasePreferencePage;

/**
 * Create the UCDetector preference page:<br>
 * Values are stored in:
 * <code>RUNTIME_WORSPACE_DIR\.metadata\.plugins\org.eclipse.core.runtime\.settings\org.ucdetector.prefs</code>
 * @see "http://www.eclipsepluginsite.com/preference-pages.html"
 */
public class CylePreferencePage extends UCDetectorBasePreferencePage {

  public CylePreferencePage() {
    super(FieldEditorPreferencePage.GRID, CyclePlugin.getDefault()
        .getPreferenceStore());
  }

  @Override
  public void createFieldEditors() {
    Composite parentGroups = createComposite(getFieldEditorParent(), 1, 1,
        GridData.FILL_HORIZONTAL);
    //
    IntegerFieldEditor cycleDepth = new IntegerFieldEditor(
        CyclePrefs.CYCLE_DEPTH, Messages.PreferencePage_MaxCycleSize,
        parentGroups) {
      /** 
       * Hack for layout problems. 
       * */
      @Override
      public int getNumberOfControls() {
        return 3;
      }
    };
    cycleDepth.setValidRange(CyclePrefs.CYCLE_DEPTH_MIN,
        CyclePrefs.CYCLE_DEPTH_MAX);
    cycleDepth.setEmptyStringAllowed(false);
    cycleDepth.getLabelControl(parentGroups).setToolTipText(
        Messages.PreferencePage_MaxCycleSizeToolTip);
    this.addField(cycleDepth);

  }
}