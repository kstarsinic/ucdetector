package org.ucdetector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IProgressConstants;
import org.ucdetector.iterator.UCDetectorIterator;
import org.ucdetector.search.UCDProgressMonitor;
import org.ucdetector.util.JavaElementUtil;

/**
 * Feature request: Eclipse Save Action - ID: 2993738
 * <p>
 * @author Joerg Spieler
 * @since 28.10.2010
 */
// http://www.eclipse.org/articles/Article-Resource-deltas/resource-deltas.html
// Tracking resource changes: 
//     http://help.eclipse.org/helios/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/resAdv_events.htm
// Resource modification hooks:
//     http://help.eclipse.org/helios/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/resAdv_hooks.htm
@SuppressWarnings("nls")
public class RunOnSave implements IResourceChangeListener, IResourceDeltaVisitor {
  private final List<IType> changedTypes = new ArrayList<IType>();
  private Job activeJob = null;

  public void resourceChanged(IResourceChangeEvent event) {
    Log.info("IResourceChangeEvent: " + event);
    if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
      return;
    }
    changedTypes.clear();
    IResourceDelta rootDelta = event.getDelta();
    try {
      rootDelta.accept(this);
    }
    catch (CoreException ex) {
      Log.error("Visitor problems", ex);
    }
    Log.info("######### changedTypes: " + changedTypes.size());
    if (changedTypes.size() > 0) {
      runUCDetector();
    }
  }

  public boolean visit(IResourceDelta delta) {
    Log.info("IResourceDelta: " + delta);
    // Not added or removed deltas
    if (delta.getKind() != IResourceDelta.CHANGED) {
      return true;
    }
    //only interested in content changes
    if ((delta.getFlags() & IResourceDelta.CONTENT) == 0) {
      return true;
    }
    IResource resource = delta.getResource();
    IType type = JavaElementUtil.getTypeFor(resource);
    if (type != null) {
      Log.info("######### typeChanged: " + type.getElementName());
      changedTypes.add(type);
    }
    return true;
  }

  private void runUCDetector() {
    cancelActiveJob();
    final UCDetectorIterator iterator = new UCDetectorIterator();
    final Job job = new Job(iterator.getJobName()) {
      @Override
      public IStatus run(IProgressMonitor monitor) {
        UCDProgressMonitor ucdMonitor = new UCDProgressMonitor(monitor);
        iterator.setMonitor(ucdMonitor);
        try {
          iterator.iterateAll();
        }
        catch (CoreException e) {
          UCDetectorPlugin.logToEclipseLog(e.getStatus());
        }
        return Status.OK_STATUS;
      }
    };
    activeJob = job;
    setJobProperty(job);
    job.schedule();
  }

  private void cancelActiveJob() {
    if (activeJob != null) {
      Log.info("CANCEL: " + activeJob);
      activeJob.cancel();
    }
  }

  protected void setJobProperty(Job job) {
    job.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
    ImageDescriptor ucdIcon = UCDetectorPlugin.getImageDescriptor(UCDetectorPlugin.IMAGE_UCD);
    job.setProperty(IProgressConstants.ICON_PROPERTY, ucdIcon);
    job.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
  }

  void setActive(boolean runOnSave) {
    if (UCDetectorPlugin.isHeadlessMode()) {
      return;
    }
    Log.info("runOnSave: " + runOnSave);
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    if (runOnSave) {
      workspace.addResourceChangeListener(this);
    }
    else {
      workspace.removeResourceChangeListener(this);
      cancelActiveJob();
    }
  }
}