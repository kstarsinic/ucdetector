package org.ucdetector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.EditorManager;
import org.eclipse.ui.internal.WorkbenchPage;
import org.ucdetector.util.JavaElementUtil;

@SuppressWarnings("nls")
public class Startup implements IStartup {
  public void earlyStartup() {
    Log.info("Startup.earlyStartup()");
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        IWorkbenchWindow activeWorkbenchWindow = UCDetectorPlugin.getActiveWorkbenchWindow();
        try {
          IPartService partService = activeWorkbenchWindow.getPartService();
          IPartListener partListener = new PartListener();
          partService.addPartListener(partListener);
          //          Log.info("partListener added: " + partListener);
          ICommandService service = (ICommandService) activeWorkbenchWindow.getService(ICommandService.class);
          IExecutionListener commandListener = new ExecutionListener();
          service.addExecutionListener(commandListener);
          //          Log.info("commandListener added: " + commandListener);
        }
        catch (Exception ex) {
          Log.error("Can't add listeners to active workbench window", ex);
        }
      }
    });
  }

  class ExecutionListener implements IExecutionListener {

    public void preExecute(String commandId, ExecutionEvent event) {
      Log.info("ExecutionListener.preExecute: " + commandId + ": " + event);
      if (!isSave(commandId)) {
        return;
      }
      List<IType> typesBeforeSave = new ArrayList<IType>();
      try {
        Log.info("PRE SAVE: " + event);
        WorkbenchPage page = UCDetectorPlugin.getActivePage();
        EditorManager editorManager = page.getEditorManager();
        IEditorPart[] dirtyEditors = editorManager.getDirtyEditors();
        for (IEditorPart iEditorPart : dirtyEditors) {
          IEditorInput editorInput = iEditorPart.getEditorInput();
          //            Log.info("    editorInput: " + editorInput);
          if (editorInput instanceof IFileEditorInput) {
            IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
            IFile file = fileEditorInput.getFile();
            IType type = JavaElementUtil.getTypeFor(file);
            if (type != null) {
              typesBeforeSave.add(type);
              Log.info("    PRE SAVE type: " + type.getElementName());
            }
          }
        }
      }
      catch (Exception ex) {
        Log.error("Can't get java types from save action", ex);
      }
      Log.info("Types before save: %s", typesBeforeSave);
    }

    public void postExecuteSuccess(String commandId, Object returnValue) {
      Log.info("ExecutionListener.postExecuteSuccess: " + commandId + ": " + returnValue);
      if (isSave(commandId)) {
        Log.info("POST SAVE");
      }
    }

    private boolean isSave(String commandId) {
      return IWorkbenchCommandConstants.FILE_SAVE.equals(commandId)
          || IWorkbenchCommandConstants.FILE_SAVE_ALL.equals(commandId);
    }

    public void notHandled(String commandId, NotHandledException exception) {
      //
    }

    public void postExecuteFailure(String commandId, ExecutionException exception) {
      //
    }
  }

  class PartListener implements IPartListener {

    public void partActivated(IWorkbenchPart part) {
      Log.info("PartListener.partActivated: " + part);
    }

    public void partBroughtToTop(IWorkbenchPart part) {
      Log.info("PartListener.partBroughtToTop: " + part);

    }

    public void partClosed(IWorkbenchPart part) {
      Log.info("PartListener.partClosed: " + part);

    }

    public void partDeactivated(IWorkbenchPart part) {
      Log.info("PartListener.partDeactivated: " + part);

    }

    public void partOpened(IWorkbenchPart part) {
      Log.info("PartListener.partOpened: " + part);
    }
  }
}
//        Log.info("   getParameters: " + event.getParameters());
//        Log.info("   getApplicationContext: " + event.getApplicationContext());
//          EvaluationContext ec = (EvaluationContext) event.getApplicationContext();

// org.eclipse.ui.internal.SaveAllAction.run()
//          if (ec == null) {
//            return;
//          }
//          IEvaluationContext parentEc = ec.getParent();
//          FileEditorInput activeEditorInput = (FileEditorInput) parentEc.getVariable("activeEditorInput");
//          IFile file = activeEditorInput.getFile();
//          Log.info("File: " + file);
