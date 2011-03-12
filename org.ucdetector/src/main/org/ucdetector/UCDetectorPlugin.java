/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.ucdetector.Log.LogLevel;
import org.ucdetector.preferences.Prefs;

/**
 * Default Activator-class of this plug-ins
 * <p>
 * @author Joerg Spieler
 * @since 2008-02-29
 */
@SuppressWarnings("nls")
public class UCDetectorPlugin extends AbstractUIPlugin implements IPropertyChangeListener {
  private static final int MEGA_BYTE = 1024 * 1024;
  public static final String IMAGE_FINAL = "IMAGE_FINAL";
  public static final String IMAGE_UCD = "IMAGE_UCD";
  public static final String IMAGE_COMMENT = "IMAGE_COMMENT";
  public static final String IMAGE_TODO = "IMAGE_TODO";
  public static final String IMAGE_CYCLE = "IMAGE_CYCLE";
  /**
   * See MANIFEST.MF: Bundle-SymbolicName, and .project
   */
  public static final String ID = "org.ucdetector";
  // The shared instance.
  private static UCDetectorPlugin plugin;
  private static boolean isHeadlessMode = false;

  /**
   * Internal id for eclipse help
   * see /org.ucdetector/help/contexts.xml
   * http://www.eclipse.org/articles/article.php?file=Article-AddingHelpToRCP/index.html
   */
  public static final String HELP_ID = ID + ".ucd_context_id";
  public static final String HELP_ID_PREFERENCES = ID + ".ucd_context_id_preferences";
  //private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
  private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static final String SEPARATOR = "-----------------------------------------------------------------------------"; //$NON-NLS-1$

  // INSTANCE

  public UCDetectorPlugin() {
    plugin = this;
  }

  private void dumpInformation() {
    Log.info(SEPARATOR);
    Log.info("Starting UCDetector Plug-In version " + getAboutUCDVersion());
    Log.info(SEPARATOR);
    Log.info("Time            : " + getNow());
    Log.info("OS              : " + getAboutOS());
    Log.info("Java            : " + getAboutJavaVersion());
    Log.info("Eclipse version : " + getAboutEclipseVersion());
    Log.info("Eclipse home    : " + getAboutEclipseHome());
    Log.info("Workspace       : " + getAboutWorkspace());
    Log.info("Logfile         : " + getAboutLogfile());
    Log.info("Log level       : " + Log.getActiveLogLevel().toString());
    logMemoryInfo();
    Log.info(getPreferencesAsString());
    Log.info(SEPARATOR);
  }

  public static void logMemoryInfo() {
    long used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEGA_BYTE;
    long max = Runtime.getRuntime().maxMemory() / MEGA_BYTE;
    long percentUsed = (100 * used) / max;
    String message = String.format("Memory: %s MB max, %s MB used (%s %%)", //
        String.valueOf(max),//
        String.valueOf(used),//
        String.valueOf(percentUsed));
    Log.log(percentUsed > 80 ? LogLevel.WARN : LogLevel.INFO, message);
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    dumpInformation();
    getPreferenceStore().addPropertyChangeListener(this);
  }

  /** @return all available preferences and all and all preferences which are different from default preferences   */
  public static String getPreferencesAsString() {
    StringBuilder sb = new StringBuilder();
    Map<String, String> deltaPreferences = getDeltaPreferences();
    sb.append(String.format("%s UCDetector preferences are different from default preferences:", //
        "" + deltaPreferences.size()));
    Set<Entry<String, String>> entrySet = deltaPreferences.entrySet();
    for (Entry<String, String> entry : entrySet) {
      sb.append(String.format("%n       %s=%s", entry.getKey(), entry.getValue()));
    }
    return sb.toString();
  }

  /** @return All preferences which are different from default preferences, without internal preferences  */
  public static Map<String, String> getDeltaPreferences() {
    // Eclipse 3.7: InstanceScope.INSTANCE.getNode(ID);
    IEclipsePreferences node = new InstanceScope().getNode(ID);
    Map<String, String> allDeltas = getPreferencesImpl(node);
    Set<String> keySetClone = new HashSet<String>(allDeltas.keySet());
    for (String key : keySetClone) {
      if (key.startsWith(Prefs.INTERNAL)) {
        allDeltas.remove(key);
      }
    }
    return allDeltas;
  }

  /** @return All available preferences */
  public static Map<String, String> getAllPreferences() {
    // Eclipse 3.7: DefaultScope.INSTANCE.getNode(ID);
    IEclipsePreferences node = new DefaultScope().getNode(ID);
    return getPreferencesImpl(node);
  }

  private static Map<String, String> getPreferencesImpl(IEclipsePreferences ePrefs) {
    Map<String, String> result = new LinkedHashMap<String, String>();
    try {
      String[] propertyNames = ePrefs.keys();
      Arrays.sort(propertyNames);
      for (String propertyName : propertyNames) {
        result.put(propertyName, ePrefs.get(propertyName, null));
      }
    }
    catch (BackingStoreException ex) {
      result.put("EXCEPTION", ex.getMessage());
      Log.error("Can't get preferences for " + ePrefs, ex);
    }
    return result;
  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    Log.info("Stopping UCDetector Plug-In at " + getNow());
    super.stop(context);
    plugin = null;
  }

  /**
   * During search there can be <code>OutOfMemoryErrors</code>.
   * This method creates an messages for the user.
   * @param e {@link OutOfMemoryError} , which is wrapped in a {@link CoreException}
   * @throws CoreException which contains the {@link OutOfMemoryError}
   */
  public static void handleOutOfMemoryError(OutOfMemoryError e) throws CoreException {
    Status status = logToEclipseLog(Messages.OutOfMemoryError_Hint, e);
    throw new CoreException(status);
  }

  /**
  * @return the shared instance.
  */
  public static UCDetectorPlugin getDefault() {
    return plugin;
  }

  // ---------------------------------------------------------------------------
  // IMAGES
  // ---------------------------------------------------------------------------
  @Override
  protected void initializeImageRegistry(ImageRegistry registry) {
    super.initializeImageRegistry(registry);
    registry.put(IMAGE_UCD, createImage(ID, "icons/ucd.gif"));
    // Since eclipse 3.7 icon was renamed from refresh_nav.gif to refresh.gif in platform:/plugin/
    registry.put(IMAGE_CYCLE, createImage(ID, "icons/cycle.gif"));
    registry.put(IMAGE_FINAL, createImage(JavaUI.ID_PLUGIN, "icons/full/ovr16/final_co.gif"));
    registry.put(IMAGE_COMMENT, createImage(JavaUI.ID_PLUGIN, "icons/full/etool16/comment_edit.gif"));
    registry.put(IMAGE_TODO, createImage(IDEWorkbenchPlugin.IDE_WORKBENCH, "icons/full/elcl16/showtsk_tsk.gif"));
  }

  private static ImageDescriptor createImage(String bundleName, String icon) {
    IPath path = new Path(icon);
    // Other example: AbstractUIPlugin.imageDescriptorFromPlugin("plugin.name", "icons/xxx.gif");
    Bundle bundle = Platform.getBundle(bundleName);
    return JavaPluginImages.createImageDescriptor(bundle, path, true);
  }

  public static final Image getSharedImage(String id) {
    ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    return sharedImages.getImage(id);
  }

  public static Image getImage(String key) {
    return getDefault().getImageRegistry().get(key);
  }

  public static ImageDescriptor getImageDescriptor(String key) {
    return getDefault().getImageRegistry().getDescriptor(key);
  }

  public static String getNow() {
    return getDefault().dateFormat.format(new Date());
  }

  // -------------------------------------------------------------------------

  public static WorkbenchPage getActivePage() {
    IWorkbenchWindow window = getActiveWorkbenchWindow();
    return window == null ? null : (WorkbenchPage) window.getActivePage();
  }

  public static IWorkbenchWindow getActiveWorkbenchWindow() {
    return getDefault().getWorkbench().getWorkbenchWindows()[0];
  }

  public static Shell getShell() {
    return getActiveWorkbenchWindow().getShell();
  }

  protected static void setHeadlessMode(boolean isHeadlessMode) {
    UCDetectorPlugin.isHeadlessMode = isHeadlessMode;
  }

  public static boolean isHeadlessMode() {
    return UCDetectorPlugin.isHeadlessMode;
  }

  public static void closeSave(Closeable closable) {
    if (closable != null) {
      try {
        closable.close();
      }
      catch (Exception e) {
        // ignore
      }
    }
  }

  // -------------------------------------------------------------------------
  public static String getAboutOS() {
    return System.getProperty("os.name") + " - " + System.getProperty("os.version");
  }

  public static String getAboutJavaVersion() {
    return System.getProperty("java.runtime.version");
  }

  public static String getAboutEclipseVersion() {
    return System.getProperty("osgi.framework.version");
  }

  // Ignore warning since target platform 3.7
  // @SuppressWarnings("cast")
  public static String getAboutUCDVersion() {
    return (String) getDefault().getBundle().getHeaders().get("Bundle-Version");
  }

  public static String getAboutEclipseHome() {
    String eclipseHome = System.getProperty("osgi.install.area");
    if (eclipseHome != null && eclipseHome.startsWith("file:")) {
      return eclipseHome.substring("file:".length());
    }
    return eclipseHome;
  }

  public static String getAboutLogfile() {
    return System.getProperty("osgi.logfile");
  }

  public static String getAboutWorkspace() {
    IPath location = ResourcesPlugin.getWorkspace().getRoot().getLocation();
    return location == null ? System.getProperty("osgi.instance.area") : location.toOSString();
  }

  public static String getHostName() {
    try {
      return java.net.InetAddress.getLocalHost().getHostName();
    }
    catch (Exception e) {
      return "?";
    }
  }

  public void propertyChange(PropertyChangeEvent event) {
    String property = event.getProperty();
    String newValue = event.getNewValue().toString();
    if (property.equals(Prefs.LOG_LEVEL)) {
      Log.setActiveLogLevel(LogLevel.valueOf(newValue));
    }
    if (property.equals(Prefs.LOG_TO_ECLIPSE)) {
      Log.setLogToEclipse(Boolean.parseBoolean(newValue));
    }
  }

  // STATUS -------------------------------------------------------------------
  /** @param status which is be logged to default log */
  public static void logToEclipseLog(IStatus status) {
    UCDetectorPlugin ucd = getDefault();
    if (ucd != null && ucd.getLog() != null) {
      ucd.getLog().log(status);
    }
    Log.status(status);
  }

  public static Status logToEclipseLog(String message, Throwable ex) {
    Status status = new Status(IStatus.ERROR, ID, IStatus.ERROR, message, ex);
    logToEclipseLog(status);
    return status;
  }

  /**
   * 
   * [ 3025571 ] Exception loading modes: Malformed  &#92;uxxxx encoding
   * <p>
   * java.util.Properties.load() fails, because of file names containing Strings (file names)
   *  which are similar to unicode signs
   * <p>
   * @param isFile <code>true</code>, when it is a file, <code>false</code>, when it is a resource in classpath
   * @param modeFileName name of mode file or mode resource
   * @return Map containing key value pairs loaded from modeFileName
   */
  public static Map<String, String> loadModeFile(boolean isFile, String modeFileName) {
    Map<String, String> result = new HashMap<String, String>();
    BufferedReader reader = null;
    try {
      InputStream in = isFile ? new FileInputStream(modeFileName) //
          : Prefs.class.getResourceAsStream("modes/" + modeFileName);
      reader = new BufferedReader(new InputStreamReader(in));
      String line = null;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        int indexEquals = line.indexOf('=');
        if (line.startsWith("#") || indexEquals == -1) { //$NON-NLS-1$
          continue;// comment or no '='
        }
        String key = line.substring(0, indexEquals).trim();
        String value = line.substring(indexEquals + 1).trim();
        result.put(key, value);
      }
    }
    catch (IOException ex) {
      String message = NLS.bind(Messages.ModesPanel_CantSetPreferences, modeFileName);
      logToEclipseLog(message, ex);
    }
    finally {
      closeSave(reader);
    }
    return result;
  }
  //  private static boolean isValidFileChar(char c) {
  //    if (c < 32 || c > 127) {
  //      return false;
  //    }
  //    if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
  //      return true;
  //    }
  //    return " !#$%&'()+,-.=?@[]^_".indexOf(c) > -1;
  //  }
}
