package org.ucdetector;

import org.eclipse.ui.IStartup;

public class Startup implements IStartup {
  public void earlyStartup() {
    System.out.println("RunOnSave.earlyStartup()"); //$NON-NLS-1$
  }
}