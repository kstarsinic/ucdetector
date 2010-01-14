/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.ucdetector.UCDetectorPlugin;

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
    registry.put(IMAGE_CYCLE, UCDetectorPlugin.getEclipseImage("org.eclipse.pde/icons/elcl16/refresh.gif")); //$NON-NLS-1$
  }
}
