package org.ucdetector.action;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.iterator.AbstractUCDetectorIterator;
import org.ucdetector.iterator.CheckUcdMarkerIterator;

/**
 *
 */
public class CheckUcdMarkerAction extends AbstractUCDetectorAction {// NO_UCD
  private AbstractUCDetectorIterator iterator;

  @Override
  protected AbstractUCDetectorIterator createIterator() {
    iterator = new CheckUcdMarkerIterator();
    return iterator;
  }

  @Override
  protected IStatus postIteration() {
    // show message for count dialog, create status
    final IStatus status = new Status(IStatus.INFO, UCDetectorPlugin.ID,
        IStatus.INFO, iterator.toString(), null);
    //    UCDetectorPlugin.log(status);
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        MessageDialog.openInformation(UCDetectorPlugin.getShell(), "Title",
            iterator.toString());
      }
    });
    return status;
  }
}
