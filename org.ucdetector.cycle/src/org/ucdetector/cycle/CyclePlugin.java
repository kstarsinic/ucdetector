/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Default Activator-class of this plug-ins
 */
public class CyclePlugin extends AbstractUIPlugin {
  public static final String IMAGE_CYCLE = "IMAGE_CYCLE"; //$NON-NLS-1$
  public static final String ID = "org.ucdetector.cycle"; //$NON-NLS-1$
  private static CyclePlugin plugin;

  public CyclePlugin() {
    CyclePlugin.plugin = this;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
    CyclePlugin.plugin = null;
  }

  /**
  * @return the shared instance.
  */
  public static CyclePlugin getDefault() {
    return CyclePlugin.plugin;
  }

  // ---------------------------------------------------------------------------
  // IMAGES
  // ---------------------------------------------------------------------------
  public static Image getImage(String key) {
    return getDefault().getImageRegistry().get(key);
  }

  @Override
  protected void initializeImageRegistry(ImageRegistry registry) {
    super.initializeImageRegistry(registry);
    registry.put(IMAGE_CYCLE, getUcdImage("cycle.gif")); //$NON-NLS-1$
  }

  private ImageDescriptor getUcdImage(String icon) {
    IPath path = new Path("icons").append("/" + icon); //$NON-NLS-1$ //$NON-NLS-2$
    return JavaPluginImages.createImageDescriptor(getDefault().getBundle(),
        path, true);
  }
}
