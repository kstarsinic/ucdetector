package org.ucdetector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
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
    if (UCDetectorPlugin.isHeadlessMode()) {
      return;
    }
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        IWorkbenchWindow activeWorkbenchWindow = UCDetectorPlugin.getActiveWorkbenchWindow();
        try {
          IPartService partService = activeWorkbenchWindow.getPartService();
          IPartListener partListener = new PartListener();
          partService.addPartListener(partListener);
          // Log.info("partListener added: " + partListener);
          ICommandService service = (ICommandService) activeWorkbenchWindow.getService(ICommandService.class);
          IExecutionListener commandListener = new ExecutionListener();
          service.addExecutionListener(commandListener);
          // Log.info("commandListener added: " + commandListener);
        }
        catch (Exception ex) {
          Log.error("Can't add listeners to active workbench window", ex);
        }
      }
    });
  }

  // ==========================================================================
  // PartListener
  // ==========================================================================
  private static final class PartListener implements IPartListener {

    public void partActivated(IWorkbenchPart part) {
      Log.info("PartListener.partActivated: " + part);
      if (part instanceof IEditorPart) {
        IEditorPart iEditorPart = (IEditorPart) part;
        IType type = getTypeForPart(iEditorPart);
        if (type != null) {
          Log.info("      type is: " + type.getElementName());
        }
      }
    }

    public void partBroughtToTop(IWorkbenchPart part) {
      // Log.info("PartListener.partBroughtToTop: " + part);
    }

    public void partClosed(IWorkbenchPart part) {
      // Log.info("PartListener.partClosed: " + part);
    }

    public void partDeactivated(IWorkbenchPart part) {
      // Log.info("PartListener.partDeactivated: " + part);
    }

    public void partOpened(IWorkbenchPart part) {
      // Log.info("PartListener.partOpened: " + part);
    }
  }

  // ==========================================================================
  // ExecutionListener
  // ==========================================================================
  private static final class ExecutionListener implements IExecutionListener {
    private final List<IType> typesBeforeSave = new ArrayList<IType>();

    public void preExecute(String commandId, ExecutionEvent event) {
      typesBeforeSave.clear();
      Log.info("ExecutionListener.preExecute: " + commandId + ": " + event);
      if (!isSaveCommand(commandId)) {
        return;
      }
      try {
        Log.info("PRE SAVE: " + event);
        WorkbenchPage page = UCDetectorPlugin.getActivePage();
        EditorManager editorManager = page.getEditorManager();
        IEditorPart[] dirtyEditors = editorManager.getDirtyEditors();
        for (IEditorPart iEditorPart : dirtyEditors) {
          IType type = getTypeForPart(iEditorPart);
          if (type != null) {
            typesBeforeSave.add(type);
            Log.info("    PRE SAVE type: " + type.getElementName());
          }
        }
      }
      catch (Exception ex) {
        Log.error("Can't get java types from save action", ex);
      }
      Log.info("Types before save: %s", JavaElementUtil.getElementNames(typesBeforeSave));
      parseTypes(typesBeforeSave);
    }

    public void postExecuteSuccess(String commandId, Object returnValue) {
      Log.info("ExecutionListener.postExecuteSuccess: " + commandId + ": " + returnValue);
      if (isSaveCommand(commandId)) {
        Log.info("POST SAVE ");
        parseTypes(typesBeforeSave);
      }
    }

    private boolean isSaveCommand(String commandId) {
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

  private static void parseTypes(List<IType> typesBeforeSave) {
    for (IType type : typesBeforeSave) {
      ICompilationUnit compilationUnit = type.getCompilationUnit();
      ASTParser parser = ASTParser.newParser(AST.JLS3);
      parser.setSource(compilationUnit); // compilationUnit needed for resolve bindings!
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setResolveBindings(true);
      ASTNode ast = parser.createAST(null);
      GetUsedCodeVisitor visitor = new GetUsedCodeVisitor();
      ast.accept(visitor);
      Log.info("invokedFields : %s", JavaElementUtil.getElementNames(visitor.invokedFields));
      Log.info("invokedMethods: %s", JavaElementUtil.getElementNames(visitor.invokedMethods));
    }
  }

  private static IType getTypeForPart(IEditorPart iEditorPart) {
    IEditorInput editorInput = iEditorPart.getEditorInput();
    if (editorInput instanceof IFileEditorInput) {
      IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
      IFile file = fileEditorInput.getFile();
      return JavaElementUtil.getTypeFor(file);
    }
    return null;
  }

  // ==========================================================================
  // ASTVisitor
  // ==========================================================================
  private static final class GetUsedCodeVisitor extends ASTVisitor {
    List<IMethod> invokedMethods = new ArrayList<IMethod>();
    List<IField> invokedFields = new ArrayList<IField>();

    @Override
    public boolean visit(MethodInvocation node) {
      // Log.info("MethodInvocation: " + node.toString());
      IMethodBinding methodBinding = node.resolveMethodBinding();
      // Log.info("methodBinding: " + methodBinding);
      IMethod methodFound = (IMethod) methodBinding.getJavaElement();
      if (!methodFound.isBinary()) {
        invokedMethods.add(methodFound);
        Log.info("methodFound: " + methodFound.getElementName());
      }
      return true;
    }

    @Override
    public boolean visit(FieldAccess node) {
      // Log.info("FieldAccess: " + node.toString());
      IVariableBinding fieldBinding = node.resolveFieldBinding();
      // Log.info("methodBinding: " + methodBinding);
      IField field = (IField) fieldBinding.getJavaElement();
      if (field.isBinary()) {
        invokedFields.add(field);
        Log.info("Field found: " + field.getElementName());
      }
      return true;
    }
  }
}