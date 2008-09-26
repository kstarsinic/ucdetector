/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import org.eclipse.jdt.core.IMember;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;

/**
 * Simple stop watch to measure performance
 */
public class StopWatch {
  private static final int MINIMUM_DURATION = Log.getDebugOption(
      "org.ucdetector/debug/search/duration", -1); //$NON-NLS-1$
  private final String message;
  private long start = System.currentTimeMillis();

  public StopWatch() {
    message = null;
  }

  //  public StopWatch(String message) {
  //    this.message = message;
  //  }

  public StopWatch(IMember member) {
    this.message = JavaElementUtil.getElementName(member);
  }

  /**
   * Prints a message to UCDetectorPlugin logging
   */
  public void end(String info) {
    long duration = System.currentTimeMillis() - start;
    start = System.currentTimeMillis();
    // traces for slow stuff
    if (UCDetectorPlugin.DEBUG && duration > MINIMUM_DURATION) {
      StringBuilder sb = new StringBuilder();
      if (info != null) {
        sb.append(info).append(" "); //$NON-NLS-1$
      }
      if (message != null) {
        sb.append(message);
      }
      sb.append(": ").append(duration / 1000d); //$NON-NLS-1$
      sb.append(" seconds"); //$NON-NLS-1$
      Log.logDebug(sb.toString());
    }
  }
}
