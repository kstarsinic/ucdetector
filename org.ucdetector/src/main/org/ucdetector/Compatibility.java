/**
 * Copyright (c) 2011 Joerg Spieler All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector;

import org.osgi.framework.Version;

/**
 * Provide Compatibility to 
 * <p>
 * @author Joerg Spieler
 * @since 03.08.2011
 */
@SuppressWarnings("nls")
public class Compatibility {
  public static final Version ECLIPSE_USED = new Version(UCDetectorPlugin.getAboutEclipseVersion());
  //
  //  private static final Version ECLIPSE_3_5 = new Version("3.5.0");
  //  private static final Version ECLIPSE_3_6 = new Version("3.6.0");
  private static final Version ECLIPSE_3_7 = new Version("3.7.0");
  //  private static final Version ECLIPSE_3_8 = new Version("3.8.0");

  /** 
   * Since eclipse 3.7
   * See: org.eclipse.jdt.core.compiler.IProblem.MethodCanBeStatic
   **/
  public static final long IPROBLEM_METHOD_CAN_BE_STATIC = 603979897;

  public static boolean isEclipse37OrNewer() {
    return ECLIPSE_USED.compareTo(ECLIPSE_3_7) >= 0;
  }
}
