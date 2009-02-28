package org.ucdetector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.CoreException;
import org.ucdetector.iterator.UCDetectorIterator;
import org.ucdetector.search.UCDProgressMonitor;


public class UCDetectorTask extends Task {

  public void execute() throws BuildException {
    System.out.println("Helo World ant ");

    UCDetectorIterator iterator = new UCDetectorIterator();
    try {
      UCDProgressMonitor ucdMonitor = new UCDProgressMonitor();
      iterator.setMonitor(ucdMonitor);
      iterator.iterateAll();
    }
    catch (CoreException e) {
      throw new BuildException(e);
    }
  }

}
