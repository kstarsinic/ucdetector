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
 * <p>
 * @author Joerg Spieler
 * @since 2008-05-08
 */
public class StopWatch {
  private static final int MINIMUM_DURATION = 1000;// Log.getDebugOption("org.ucdetector/debug/search/duration", 1000); 
  private static final int MINIMUM_DURATION_WARN = MINIMUM_DURATION * 10;
  private final String message;
  private long start = System.currentTimeMillis();
  private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US)); //$NON-NLS-1$

  public StopWatch() {
    message = null;
  }

  public StopWatch(IMember member) {
    this.message = JavaElementUtil.getElementName(member);
  }

  public String end(String info) {
    return end(info, false);
  }

  /**
   * Prints a message to UCDetectorPlugin logging
   * @param info String to append to message
   * @param doLog do logging
   * @return log message
   */
  public String end(String info, boolean doLog) {
    long duration = System.currentTimeMillis() - start;
    // reset
    start = System.currentTimeMillis();
    String logMessage = createLogMessage(info, duration);
    if (doLog) {
      // Logging slow stuff
      if (Log.isDebug() && duration > MINIMUM_DURATION) {
        Log.debug(logMessage);
      }
      else if (duration > MINIMUM_DURATION_WARN) {
        Log.warn(logMessage);
      }
    }
    return logMessage;
  }

  private String createLogMessage(String info, long duration) {
    StringBuilder sb = new StringBuilder();
    if (info != null) {
      sb.append("Duration: ").append(info).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
    }
    if (message != null) {
      sb.append(message);
    }
    sb.append(": ").append(timeAsString(duration)); //$NON-NLS-1$
    return sb.toString();
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
