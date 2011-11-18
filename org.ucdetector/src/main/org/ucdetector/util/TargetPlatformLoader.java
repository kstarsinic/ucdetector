/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.util;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
import org.eclipse.pde.internal.core.target.provisional.ITargetHandle;
import org.eclipse.pde.internal.core.target.provisional.ITargetPlatformService;
import org.eclipse.pde.internal.core.target.provisional.LoadTargetDefinitionJob;
import org.ucdetector.Log;
import org.ucdetector.UCDHeadless;

/**
 * Load target platform if a target platform file is declared in ucdetector options file.<p>
 * If plugin org.eclipse.pde.core is missing:
 * <pre> 
 * java.lang.NoClassDefFoundError: org/eclipse/pde/internal/core/PDECore
 * </pre> 
 * Log a hint to use eclipse classic.
 * <p>
 * @author Joerg Spieler
 * @since 16.11.2011
 */
@SuppressWarnings("nls")
public class TargetPlatformLoader {

  public void loadTargetPlatform(IProgressMonitor monitor, File targetPlatformFile) throws CoreException {
    if (targetPlatformFile == null || !targetPlatformFile.exists()) {
      Log.info("Target platform file missing: Use eclipse as target platform");
      return;
    }
    StopWatch stopWatch = new StopWatch();
    Log.info("Use target platform declared in: " + targetPlatformFile.getAbsolutePath());
    Log.info("START: loadTargetPlatform");
    loadTargetPlatformImpl(monitor, targetPlatformFile);
    //    LoadTargetDefinitionJob.load(targetDefinition);
    Log.info(stopWatch.end("END: loadTargetPlatform", false));
    // Run it twice because of Exception, when running it with a complete workspace: See end of file
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=314814
    // Log.logInfo("Load target platform again, because of Exception - eclipse bug 314814");
    // loadTargetPlatform();
  }

  private static void loadTargetPlatformImpl(IProgressMonitor monitor, File targetPlatformFile) throws CoreException {
    try {
      ITargetPlatformService tps = (ITargetPlatformService) PDECore.getDefault().acquireService(
          ITargetPlatformService.class.getName());
      ITargetHandle targetHandle = tps.getTarget(targetPlatformFile.toURI());
      ITargetDefinition targetDefinition = targetHandle.getTargetDefinition();
      new LoadTargetDefinitionJob(targetDefinition).run(monitor);
    }
    catch (NoClassDefFoundError error) {
      Log.error("Can't load target platform file: " + targetPlatformFile.getAbsolutePath(), error);
      Log.error("Solutions:");
      Log.error(" - Use eclipse classic, or");
      Log.error(" - Remove key '" + UCDHeadless.HEADLESS_KEY_TARGET + "' from " + UCDHeadless.UCDETECTOR_OPTIONS);
      throw error;
    }
  }
}
