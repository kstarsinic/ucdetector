/*
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Default Activator-class of this plug-ins
 */
public class UCDetectorPlugin extends AbstractUIPlugin {
  public static final String IMAGE_FINAL = "IMAGE_FINAL"; //$NON-NLS-1$
  public static final String IMAGE_CYCLE = "IMAGE_CYCLE"; //$NON-NLS-1$
  /** org.eclipse.jdt.ui\etool16\comment_edit.gif */
  public static final String IMAGE_COMMENT = "IMAGE_COMMENT"; //$NON-NLS-1$
  /**
   * To activate debug traces add line
   * <pre>org.ucdetector/debug=true</pre>
   * to file ECLIPSE_INSTALL_DIR\.options
   * 
   * @see http://wiki.eclipse.org/FAQ_How_do_I_use_the_platform_debug_tracing_facility%3F
   */
  public static final boolean DEBUG = Log.isDebugOption("org.ucdetector/debug"); //$NON-NLS-1$
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

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    dumpInformation();
    registerImages(getDefault().getImageRegistry());
  }

  private void dumpInformation() {
    StringBuilder sb = new StringBuilder();
    sb.append("-----------------------------------------------"); //$NON-NLS-1$
    Object version = getBundle().getHeaders().get("Bundle-Version"); //$NON-NLS-1$
    sb.append("\r\nStarting UCDetector version ").append(version); //$NON-NLS-1$
    sb.append(" at ").append( //$NON-NLS-1$
        DateFormat.getDateTimeInstance().format(new Date()));
    sb.append("\r\njava=").append(System.getProperty("java.version")); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append(",eclipse=").append(System.getProperty("osgi.framework.version")); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append("\r\nlogfile=").append(System.getProperty("osgi.logfile")); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append("\r\n---- Plugin Preferences -------------------"); //$NON-NLS-1$
    sb.append(getPreferencesAsString());
    sb.append("\r\n-----------------------------------------------"); //$NON-NLS-1$
    Log.logInfo(sb.toString());
  }

  private static String getPreferencesAsString() {
    StringBuilder sb = new StringBuilder();
    String[] propertyNames = plugin.getPluginPreferences().propertyNames();
    for (String propertyName : propertyNames) {
      sb.append("\r\n   ").append(propertyName).append("="); //$NON-NLS-1$ //$NON-NLS-2$
      sb.append(plugin.getPluginPreferences().getString(propertyName));
    }
    return sb.toString();
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
    Status status = Log.logErrorAndStatus(
        Messages.CycleSearchManager_OutOfMemoryError_Hint, e);
    throw new CoreException(status);
  }

  /**
  * @return the shared instance.
  */
  public static UCDetectorPlugin getDefault() {
    return UCDetectorPlugin.plugin;
  }

  // ---------------------------------------------------------------------------
  // IMAGES
  // ---------------------------------------------------------------------------
  private void registerImages(ImageRegistry registry) {
    registry.put(IMAGE_CYCLE, getUcdImage("cycle.gif")); //$NON-NLS-1$
    registry.put(IMAGE_COMMENT, getUcdImage("comment_edit.gif")); //$NON-NLS-1$
    registry.put(IMAGE_FINAL, JavaPluginImages.DESC_OVR_FINAL);
  }

  private ImageDescriptor getUcdImage(String icon) {
    IPath path = new Path("icons").append("/" + icon); //$NON-NLS-1$ //$NON-NLS-2$
    return JavaPluginImages.createImageDescriptor(getDefault().getBundle(),
        path, true);
  }

  public static final Image getSharedImage(String id) {
    ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    return sharedImages.getImage(id);
  }

  public static Image getImage(String key) {
    return getDefault().getImageRegistry().get(key);
  }

  // -------------------------------------------------------------------------

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
