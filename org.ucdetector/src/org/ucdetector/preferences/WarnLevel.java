/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.preferences;

import org.ucdetector.Messages;

/**
 * Enum for the warn level in the preference page
 */
public enum WarnLevel {
  /**
   * Show detected code as "Error"
   */
  ERROR(),

  /**
   * Show detected code as "Warning"
   */
  WARNING(),

  /**
   * Don't try to detect code, don't show detected code problems
   */
  IGNORE();

  private WarnLevel() {
  }

  String toStringLocalized() {
    switch (this) {
      case ERROR:
        return Messages.WarnLevel_Error;
      case WARNING:
        return Messages.WarnLevel_Warning;
      default:
        return Messages.WarnLevel_Ignore;
    }
  }
}