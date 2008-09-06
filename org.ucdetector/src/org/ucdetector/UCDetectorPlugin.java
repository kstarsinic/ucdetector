/*
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Default Activator-class of this plug-ins
 */
public class UCDetectorPlugin extends AbstractUIPlugin {
  private static final String LOG_LEVEL_DEBUG = "DEBUG"; //$NON-NLS-1$
  private static final String LOG_LEVEL_INFO = "INFO"; //$NON-NLS-1$
  private static final String LOG_LEVEL_WARN = "WARN"; //$NON-NLS-1$
  private static final String LOG_LEVEL_ERROR = "ERROR"; //$NON-NLS-1$
  /**
   * To activate debug traces add system property: "org.ucdetector.debug"
   */
  public static final boolean DEBUG = System
      .getProperty("org.ucdetector.debug") != null; //$NON-NLS-1$
  /**
   * http://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility
   */
  // public static final boolean DEBUG =
  // UCDetectorPlugin.getDefault().isDebugging() &&
  // "true".equalsIgnoreCase(Platform.getDebugOption("org.ucdetector/debug"));
  /**
   * See MANIFEST.MF: Bundle-SymbolicName, and .project
   */
  public static final String ID = "org.ucdetector"; //$NON-NLS-1$
  // The shared instance.
  private static UCDetectorPlugin plugin;
  /**
   * internal id for eclipse help
   */
  public static final String HELP_ID = "org.ucdetector.ucd_context_id";//$NON-NLS-1$

  public UCDetectorPlugin() {
    UCDetectorPlugin.plugin = this;
  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
    UCDetectorPlugin.plugin = null;
  }

  /**
   * During search there can be <code>OutOfMemoryErrors</code>.
   * This method creates an messages for the user.
   */
  public static void handleOutOfMemoryError(OutOfMemoryError e)
      throws CoreException {
    e.printStackTrace();
    IStatus status = new Status(IStatus.ERROR, ID, IStatus.ERROR,
        Messages.CycleSearchManager_OutOfMemoryError_Hint, e);
    log(status);
    throw new CoreException(status);
  }

  /**
  * @return the shared instance.
  */
  public static UCDetectorPlugin getDefault() {
    return UCDetectorPlugin.plugin;
  }

  /**
   * @param status which is be logged to default log
   */
  public static void log(IStatus status) {
    getDefault().getLog().log(status);
  }

  // -------------------------------------------------------------------------
  // LOGGING
  // -------------------------------------------------------------------------

  public static void logDebug(String message) {
    logImpl(LOG_LEVEL_DEBUG, message, null);
  }

  public static void logInfo(String message) {
    logImpl(LOG_LEVEL_INFO, message, null);
  }

  public static void logWarn(String message) {
    logImpl(LOG_LEVEL_WARN, message, null);
  }

  public static void logError(String message) {
    logError(message, null);
  }

  public static void logError(String message, Throwable ex) {
    logImpl(LOG_LEVEL_ERROR, message, ex);
  }

  /**
   * Very simple logging to System.out and System.err
   * VM Parameter "-Dorg.ucdetector.debug" must be set
   * to see info and debug traces
   */
  private static void logImpl(String level, String message, Throwable ex) {
    StringBuilder sb = new StringBuilder();
    sb.append(level).append(": ").append(message == null ? "" : message); //$NON-NLS-1$ //$NON-NLS-2$
    if (DEBUG
        && (LOG_LEVEL_DEBUG.equals(level) || LOG_LEVEL_INFO.equals(level))) {
      System.out.println(sb.toString());
    }
    else if (LOG_LEVEL_WARN.equals(level) || LOG_LEVEL_ERROR.equals(level)) {
      System.err.println(sb.toString());
    }
    if (ex != null) {
      ex.printStackTrace();
    }
  }

  public static IWorkbenchPage getActivePage() {
    return getActiveWorkbenchWindow().getActivePage();
  }

  private static IWorkbenchWindow getActiveWorkbenchWindow() {
    return getDefault().getWorkbench().getWorkbenchWindows()[0];
  }

  public static Shell getShell() {
    return getActiveWorkbenchWindow().getShell();
  }
}
