/**
 * Copyright (c) 2008 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.ucdetector.Log;
import org.ucdetector.UCDetectorPlugin;

/**
 * // http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html
 * @see http://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html
 */
class DeleteQuickFix extends AbstractUCDQuickFix { 
  protected DeleteQuickFix(IMarker marker) throws CoreException {
    super(marker);
  }

  public String getLabel() {
    return "Delete code"; //$NON-NLS-1$
  }

  @Override
  public void runImpl(IMarker marker) throws Exception {
    if (UCDetectorPlugin.DEBUG) {
      Log.logDebug("  QuickFix delete: " + bodyDeclaration); //$NON-NLS-1$
    }
    rewrite.remove(bodyDeclaration, null);
    commit(marker);
    marker.delete();
  }
}
