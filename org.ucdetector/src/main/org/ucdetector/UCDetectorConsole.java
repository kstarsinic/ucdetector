/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Show ucdetector logging in eclipse console
 * <p>
 * @author Joerg Spieler
 * @since 2011-03-11
 */
public class UCDetectorConsole extends MessageConsole {
  private static UCDetectorConsole console;
  private static PrintStream consoleStreamInfo;
  private static PrintStream consoleStreamWarn;

  public UCDetectorConsole(String name, ImageDescriptor imageDescriptor, boolean autoLifecycle) {
    super(name, imageDescriptor, autoLifecycle);
  }

  public static void log(boolean isWarn, String formattedMessage, Throwable ex) {
    PrintStream stream = isWarn ? consoleStreamWarn : consoleStreamInfo;
    if (stream != null) {
      if (formattedMessage != null) {
        stream.println(formattedMessage);
      }
      if (ex != null) {
        ex.printStackTrace(stream);
      }
    }
  }

  public static class UCDetectorConsoleFactory implements IConsoleFactory {

    public void openConsole() {
      IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
      boolean exists = false;
      if (console == null) {
        console = new UCDetectorConsole("UCDetector Console", null, true); //$NON-NLS-1$
        MessageConsoleStream infoStream = console.newMessageStream();
        infoStream.setColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        try {
          consoleStreamInfo = new PrintStream(infoStream, true, UCDetectorPlugin.UTF_8);
          MessageConsoleStream warnStream = infoStream;
          warnStream.setColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
          consoleStreamWarn = new PrintStream(warnStream, true, UCDetectorPlugin.UTF_8);
        }
        catch (UnsupportedEncodingException ex) {
          UCDetectorPlugin.logToEclipseLog("Can't set encoding", ex); //$NON-NLS-1$
        }
      }
      else {
        IConsole[] existingConsoles = consoleManager.getConsoles();
        for (IConsole existingConsole : existingConsoles) {
          if (console == existingConsole) {
            exists = true;
          }
        }
      }
      if (!exists) {
        consoleManager.addConsoles(new IConsole[] { console });
      }
      consoleManager.showConsoleView(console);
    }
  }

  public static class UCDetectorConsolePageParticipant implements IConsolePageParticipant {

    public void init(IPageBookViewPage page, IConsole unused) {
      // See: org.ucdetector.cycle.CycleView.removeSelectedMatches
      Action removeAction = new RemoveAction();
      removeAction.setText("Close UCDetector console"); //$NON-NLS-1$
      SearchPluginImages.setImageDescriptors(removeAction, SearchPluginImages.T_LCL,
          SearchPluginImages.IMG_LCL_SEARCH_REM);
      IActionBars bars = page.getSite().getActionBars();
      bars.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP, removeAction);
    }

    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
      return null;
    }

    public void dispose() {
      //
    }

    public void activated() {
      //
    }

    public void deactivated() {
      //
    }
  }

  private static class RemoveAction extends Action {

    @Override
    public void run() {
      IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
      if (console != null) {
        manager.removeConsoles(new IConsole[] { console });
        console = null;
      }
    }
  }
}
