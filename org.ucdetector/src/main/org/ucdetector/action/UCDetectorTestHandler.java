///**
// * Copyright (c) 2017 Joerg Spieler All rights reserved. This program and the
// * accompanying materials are made available under the terms of the Eclipse
// * Public License v1.0 which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// */
//package org.ucdetector.action;
//
//import org.eclipse.core.commands.ExecutionEvent;
//import org.eclipse.core.commands.ExecutionException;
//import org.eclipse.core.commands.IHandler;
//import org.eclipse.core.commands.IHandlerListener;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Shell;
//import org.ucdetector.UCDetectorPlugin;
//

//  <extension point="org.eclipse.ui.commands">
//    <command
//        defaultHandler="org.ucdetector.action.UCDetectorTestHandler"
//        id="org.ucdetector.definitionId.ucd"
//        name="%command.name.0">
//    </command>
//  </extension>

//public class UCDetectorTestHandler implements IHandler {
//
//  @Override
//  public void addHandlerListener(IHandlerListener handlerListener) {
//    System.out.println("UCDetectorTestHandler.addHandlerListener: " + handlerListener);
//  }
//
//  @Override
//  public void dispose() {
//    System.out.println("UCDetectorTestHandler.dispose");
//  }
//
//  @Override
//  public Object execute(final ExecutionEvent event) throws ExecutionException {
//    System.out.println("UCDetectorTestHandler.execute: " + event);
//    Display.getDefault().asyncExec(new Runnable() {
//      @Override
//      public void run() {
//        Shell shell = UCDetectorPlugin.getShell();
//        MessageDialog.openInformation(shell, "UCDetectorTestHandler", String.valueOf(event));
//      }
//    });
//    new UCDetectorAction().runWithEvent(null, null);
//    return null;
//  }
//
//  @Override
//  public boolean isEnabled() {
//    System.out.println("UCDetectorTestHandler.isEnabled");
//    return true;
//  }
//
//  @Override
//  public boolean isHandled() {
//    System.out.println("UCDetectorTestHandler.isHandled");
//    return true;
//  }
//
//  @Override
//  public void removeHandlerListener(IHandlerListener handlerListener) {
//    System.out.println("UCDetectorTestHandler.removeHandlerListener: " + handlerListener);
//  }
//}