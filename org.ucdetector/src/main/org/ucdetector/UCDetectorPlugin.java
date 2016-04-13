/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
public class UCDetectorPlugin extends AbstractUIPlugin {
  public static final String UTF_8 = "UTF-8";
  // @formatter:off
  public static final String IMAGE_FINAL   = "IMAGE_FINAL";
  public static final String IMAGE_UCD     = "IMAGE_UCD";
  public static final String IMAGE_COMMENT = "IMAGE_COMMENT";
  public static final String IMAGE_TODO    = "IMAGE_TODO";
  public static final String IMAGE_CYCLE   = "IMAGE_CYCLE";
  // @formatter:on
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
  private static final String SEPARATOR = "-----------------------------------------------------------------------------"; //$NON-NLS-1$

  public UCDetectorPlugin() {
    plugin = this;
  }

  private static void dumpInformation() {
    Log.info(SEPARATOR);
    Log.info("Starting UCDetector Plug-In version " + UCDInfo.getUCDVersion());
    Log.info(SEPARATOR);
    Log.info("Time            : " + UCDInfo.getNow(false));
    Log.info("OS              : " + UCDInfo.getOS());
    Log.info("Java            : " + UCDInfo.getJavaVersion());
    Log.info("Eclipse version : " + UCDInfo.getEclipseVersion());
    Log.info("Eclipse home    : " + UCDInfo.getEclipseHome());
    Log.info("Eclipse product : " + UCDInfo.getEclipseProduct());
    Log.info("Workspace       : " + UCDInfo.getWorkspace());
    Log.info("Logfile         : " + UCDInfo.getLogfile());
    Log.info("Log level       : " + Log.getActiveLogLevel().toString());
    Log.info("Modes Dir       : " + getModesDir().getAbsolutePath()); //$NON-NLS-1$
    Log.info(getPreferencesAsString());
    UCDInfo.logMemoryInfo();
    Log.info(SEPARATOR);
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    dumpInformation();
    addPropertyChangeListener();
  }

  private void addPropertyChangeListener() {
    getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        String newValue = event.getNewValue().toString();
        if (property.equals(Prefs.LOG_LEVEL)) {
          LogLevel newLogLevel = LogLevel.valueOf(newValue);
          Log.setActiveLogLevel(newLogLevel);
          if (newLogLevel.ordinal() > LogLevel.INFO.ordinal()) {
            System.out.println("UCDetector Log level: " + newLogLevel); // we need to log to System.out
          }
          else {
            Log.info("UCDetector Log level: " + newLogLevel);
          }
        }
        else if (property.equals(Prefs.LOG_TO_ECLIPSE)) {
          Log.setLogToEclipse(Boolean.parseBoolean(newValue));
        }
      }
    });
  }

  /** @return all available preferences and all and all preferences which are different from default preferences   */
  public static String getPreferencesAsString() {
    StringBuilder sb = new StringBuilder();
    Map<String, String> deltaPreferences = getDeltaPreferences();
    sb.append(String.format("%s UCDetector preferences are different from default preferences:",
        "" + deltaPreferences.size()));
    for (Entry<String, String> entry : deltaPreferences.entrySet()) {
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
    Log.info("Stopping UCDetector Plug-In at " + UCDInfo.getNow(true));
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

  public static void setHeadlessMode(boolean isHeadlessMode) {
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
        Log.warn("Can't close %s: %s", closable, e); //$NON-NLS-1$
      }
    }
  }

  public static String getCanonicalPath(File file) {
    if (file == null) {
      return null;
    }
    try {
      return file.getCanonicalPath();
    }
    catch (IOException e) {
      Log.warn("%s for getCanonicalPath(%s)", e, file);
      return file.getAbsolutePath();
    }
  }

  public static String readAll(Reader reader) throws IOException {
    final char[] buf = new char[2048];
    StringBuffer sb = new StringBuffer();
    int size = 0;
    while (size != -1) {
      size = reader.read(buf);
      if (size > 0) {
        sb.append(new String(buf, 0, size));
      }
    }
    return sb.toString();
  }

  public static String exceptionToString(Throwable ex) {
    if (ex == null) {
      return null;
    }
    StringWriter writer = new StringWriter();
    ex.printStackTrace(new PrintWriter(writer));
    // Needed??? writer.toString().replace("\r\n", "\n");
    return writer.toString();
  }

  /** @return WORKSPACE/.metadata/.plugins/org.ucdetector/modes */
  // Unused:   WORKSPACE/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.ucdetector.prefs
  public static File getModesDir() {
    File ucdDir = getDefault().getStateLocation().toFile();
    return new File(ucdDir, "modes"); //$NON-NLS-1$
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

  /** Try newest Parser first. */
  private static final int[] AST_PARSER_LEVELS = { //
      8, // AST.JLS8
      4, // AST.JLS4, fixes: #70 Error in numeric literal with underscores:  http://sourceforge.net/p/ucdetector/bugs/70/
      AST.JLS3, //
  };

  //
  /** @return latest parser */
  public static ASTParser newASTParser() {
    for (int level : AST_PARSER_LEVELS) {
      try {
        // Use int here instead of AST.JLS8, to avoid compile error
        return ASTParser.newParser(level);
      }
      catch (Exception e) {
        // Ignore, return older parser
      }
    }
    throw new RuntimeException("Cant find ASTParser for levels: " + Arrays.toString(AST_PARSER_LEVELS));
  }

  ///**
  //* @param map map to create a string
  //* @param format string used to format key value pairs, see {@link String#format(String, Object...)}
  //* @return a map as a string
  //*/
  //public static String toString(Map<? extends Object, ? extends Object> map, String format) {
  // if (map == null) {
  //   return String.valueOf(map);
  // }
  // String formatUsed = (format == null ? "\t%s = %s%n" : format);
  // StringBuilder result = new StringBuilder();
  // for (Object key : map.keySet()) {
  //   result.append(String.format(formatUsed, key, map.get(key)));
  // }
  // return result.toString();
  //}
  //
  ///**
  //* @param collection collection to create a string
  //* @param separator string to separate items
  //* @return a collection as a string
  //* */
  //public static String toString(Collection<? extends Object> collection, String separator) {
  // if (collection == null) {
  //   return String.valueOf(collection);
  // }
  // StringBuilder result = new StringBuilder();
  // String separatorUsed = (separator == null ? "\n" : separator);
  // for (Object object : collection) {
  //   result.append(result.length() == 0 ? "" : separatorUsed).append(object);
  // }
  // return result.toString();
  //}
}
