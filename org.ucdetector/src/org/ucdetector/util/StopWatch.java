/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.eclipse.jdt.core.IMember;
import org.ucdetector.Log;

/**
 * Simple stop watch to measure performance
 */
public class StopWatch {
  private static final int MINIMUM_DURATION = 1000;// Log.getDebugOption("org.ucdetector/debug/search/duration", 1000); 
  private final String message;
  private long start = System.currentTimeMillis();
  private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US)); //$NON-NLS-1$

  public StopWatch() {
    message = null;
  }

  public StopWatch(IMember member) {
    this.message = JavaElementUtil.getElementName(member);
  }

  /**
   * Prints a message to UCDetectorPlugin logging
   * @param info String to append to message
   */
  public void end(String info) {
    long duration = System.currentTimeMillis() - start;
    start = System.currentTimeMillis();
    // traces for slow stuff
    if (Log.isDebug() && duration > MINIMUM_DURATION) {
      StringBuilder sb = new StringBuilder();
      if (info != null) {
        sb.append(info).append(" "); //$NON-NLS-1$
      }
      if (message != null) {
        sb.append(message);
      }
      sb.append(": ").append(timeAsString(duration)); //$NON-NLS-1$
      Log.logDebug(sb.toString());
    }
  }

  public static String timeAsString(long millis) {
    if (millis <= 1000) {
      return millis + " millis";//$NON-NLS-1$
    }
    if (millis <= 60 * 1000) {
      return StopWatch.DOUBLE_FORMAT.format(millis / 1000.0) + " seconds";//$NON-NLS-1$
    }
    if (millis <= 60 * 60 * 1000) {
      return StopWatch.DOUBLE_FORMAT.format(millis / 60000.0) + " minutes";//$NON-NLS-1$
    }
    return StopWatch.DOUBLE_FORMAT.format(millis / 3600000.0) + " hours";//$NON-NLS-1$
  }
}
