/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.headless;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.LoadTargetDefinitionJob;
import org.eclipse.pde.internal.core.PDECore;
import org.ucdetector.Log;

//==  before 3.5 ==
//There is no target code

//== 3.5 to 3.7  ==
//import org.eclipse.pde.internal.core.target.provisional.ITargetDefinition;
//import org.eclipse.pde.internal.core.target.provisional.ITargetHandle;
//import org.eclipse.pde.internal.core.target.provisional.ITargetPlatformService;
//import org.eclipse.pde.internal.core.target.provisional.LoadTargetDefinitionJob;

//== Since 3.8 and 4.2 ==
//import org.eclipse.pde.core.target.ITargetDefinition;
//import org.eclipse.pde.core.target.ITargetHandle;
//import org.eclipse.pde.core.target.ITargetPlatformService;
//import org.eclipse.pde.core.target.LoadTargetDefinitionJob;

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

  public static void loadTargetPlatformImpl(IProgressMonitor monitor, File targetPlatformFile) throws CoreException {
    try {
      // See: org.eclipse.pde.internal.core.PluginModelManager.initializeTable()
      ITargetPlatformService service = (ITargetPlatformService) PDECore.getDefault()//
          .acquireService(ITargetPlatformService.class.getName());
      if (service == null) {
        return;
      }
      ITargetHandle handle = service.getTarget(targetPlatformFile.toURI());
      ITargetDefinition targetDefinition = handle.getTargetDefinition();
      new LoadTargetDefinitionJob(targetDefinition).run(monitor);
    }
    catch (NoClassDefFoundError error) {
      Log.error("Can't load target platform file: " + targetPlatformFile.getAbsolutePath(), error);
      Log.error("Solutions:");
      Log.error(" - Use eclipse classic, version >= 3.5 and <= 3.7, or");
      Log.error(" - Remove key '" + UCDHeadless.HEADLESS_KEY_TARGET + "' from " + UCDHeadless.UCDETECTOR_OPTIONS);
      throw error;
    }
  }
}
