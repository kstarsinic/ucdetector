/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.cycle;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.ucdetector.Log;
import org.ucdetector.Messages;
import org.ucdetector.UCDetectorPlugin;
import org.ucdetector.cycle.model.Cycle;
import org.ucdetector.cycle.model.CycleRegion;
import org.ucdetector.cycle.model.CycleType;
import org.ucdetector.cycle.model.ICycleBaseElement;
import org.ucdetector.cycle.model.SearchResult;
import org.ucdetector.cycle.model.SearchResultRoot;

/**
 * 
 */
public class CycleView extends ViewPart { // 
  public static final String ID = "org.ucdetector.cycle.view.CycleView"; //$NON-NLS-1$
  private static CycleView INSTANCE = null;
  private TreeViewer viewer;
  private Tree tree;
  private Clipboard clipboard;

  private Action refreshAction;
  private Action rotateAction;
  private Action removeAction;
  private IAction copyAction;
  private Action openAction;

  private Label label;

  /**
   * The constructor.
   */
  public CycleView() {
    INSTANCE = this;
  }

  /**
   * This is a callback that will allow us to create the viewer and initialize
   * it.
   */
  @Override
  public void createPartControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gridLayout = new GridLayout(1, false);
    composite.setLayout(gridLayout);

    label = new Label(composite, SWT.NONE);
    label.setText(Messages.CycleView_run_ucd_for_results);

    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    viewer = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    tree = viewer.getTree();
    tree.setLayoutData(new GridData(GridData.FILL_BOTH));
    // drillDownAdapter = new DrillDownAdapter(viewer);
    viewer.setContentProvider(new ViewContentProvider());
    viewer.setLabelProvider(new ViewLabelProvider());
    viewer.setInput(getViewSite());

    // Create the help context id for the viewer's control
    PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), UCDetectorPlugin.HELP_ID);
    makeActions();
    hookContextMenu();
    hookDoubleClickAction();
    clipboard = new Clipboard(tree.getDisplay());
  }

  private void fillContextMenu(IMenuManager manager) {
    // final List selectedElements = getSelectedElements();
    Object first = getFirstSelectedElement();
    if (first != null) {
      if (!(first instanceof SearchResult)) {
        manager.add(openAction);
      }
      if (first instanceof Cycle) {
        manager.add(rotateAction);
      }
      manager.add(copyAction);

      if (first instanceof Cycle || first instanceof SearchResult) {
        manager.add(removeAction);
      }

      manager.add(refreshAction);
      manager.add(new Separator());
      // Other plug-ins can contribute there actions here
    }
    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
  }

  private void hookContextMenu() {
    MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        CycleView.this.fillContextMenu(manager);
      }
    });
    Menu menu = menuMgr.createContextMenu(viewer.getControl());
    viewer.getControl().setMenu(menu);
    getSite().registerContextMenu(menuMgr, viewer);
  }

  private void hookDoubleClickAction() {
    viewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        openAction.run();
      }
    });
  }

  private void makeActions() {
    // ---------------------------------------------------------------------
    // REFRESH
    // ---------------------------------------------------------------------
    refreshAction = new Action() {
      @Override
      public void run() {
        refresh();
      }
    };
    refreshAction.setText(Messages.CycleView_popup_refresh);
    ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
    refreshAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

    // ---------------------------------------------------------------------
    // ROTATE
    // ---------------------------------------------------------------------
    rotateAction = new Action() {
      @Override
      public void run() {
        Cycle cycle = (Cycle) getFirstSelectedElement();
        if (cycle != null) {
          cycle.rotate();
          refresh();
        }
      }
    };
    rotateAction.setText(Messages.CycleView_popup_rotate);

    // ---------------------------------------------------------------------
    // REMOVE
    // ---------------------------------------------------------------------
    removeAction = new Action() {
      @Override
      public void run() {
        TreePath[] paths = getTreeSelection().getPaths();
        for (TreePath treePath : paths) {
          Object last = treePath.getLastSegment();
          if (last instanceof SearchResult || last instanceof Cycle) {
            ((ICycleBaseElement) last).getParent().getChildren().remove(last);
          }
        }
        refresh();
      }
    };
    removeAction.setText(Messages.CycleView_popup_remove);

    // ---------------------------------------------------------------------
    // OPEN
    // ---------------------------------------------------------------------
    openAction = new Action() {
      @Override
      public void run() {
        Object obj = getFirstSelectedElement();
        if (obj == null) {
          return; // 2008.11.26: npe found on smoke test
        }
        if (obj instanceof Cycle) {
          Cycle cycle = (Cycle) obj;
          List<CycleType> types = cycle.getChildren();
          for (CycleType cycleType : types) {
            openInEditor(cycleType.getJavaElement(), -1, -1);
          }
          return;
        }
        IJavaElement element = ((ICycleBaseElement) obj).getJavaElement();
        int offset = -1;
        int length = -1;
        if (obj instanceof CycleRegion) {
          CycleRegion cycleRegion = (CycleRegion) obj;
          offset = cycleRegion.getOffset();
          length = cycleRegion.getLength();
          element = cycleRegion.getParent().getJavaElement();
        }
        // -------------------------------------------------------------
        openInEditor(element, offset, length);
      }

      private void openInEditor(IJavaElement element, int offset, int length) {
        if (element == null) {
          return;
        }
        try {
          // part = JavaUI.openInEditor(element, activateOnOpen);
          IEditorPart part = JavaUI.openInEditor(element, true, false);
          if (part == null) {
            return;
          }
          if (offset != -1 && length != -1) {
            EditorUtility.revealInEditor(part, offset, length);
          }
          else {
            JavaUI.revealInEditor(part, element);
          }
        }
        catch (Exception e) {
          Log.logError("Can't open editor", e); //$NON-NLS-1$
        }
      }
    };
    openAction.setText(Messages.CycleView_popup_open);

    // ---------------------------------------------------------------------
    // COPY
    // ---------------------------------------------------------------------
    copyAction = new Action() {
      @Override
      public void run() {
        List<?> elements = getSelectedElements();
        StringBuilder sb = new StringBuilder();
        for (Object obj : elements) {
          sb.append(obj).append('\n').append('\n');
        }
        Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
        clipboard.setContents(new Object[] { sb.toString() }, types);
      }
    };
    copyAction.setEnabled(true);
    copyAction.setText(Messages.CycleView_popup_copy_clipboard);
    copyAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));

  }

  public void refresh() {
    viewer.refresh();
    boolean hasInput = tree != null && tree.getItemCount() > 0;
    label.setText(hasInput ? "" : Messages.CycleView_run_ucd_for_results); //$NON-NLS-1$
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public void setFocus() {
    viewer.getControl().setFocus();
  }

  private List<?> getSelectedElements() {
    ISelection selection = this.viewer.getSelection();
    return ((IStructuredSelection) selection).toList();
  }

  private Object getFirstSelectedElement() {
    ISelection selection = this.viewer.getSelection();
    return ((IStructuredSelection) selection).getFirstElement();
  }

  private ITreeSelection getTreeSelection() {
    ITreeSelection treeSelection = (ITreeSelection) CycleView.this.viewer.getSelection();
    return treeSelection;
  }

  public static CycleView getInstance() {
    return INSTANCE;
  }

  // -------------------------------------------------------------------------
  // LabelProvider
  // -------------------------------------------------------------------------
  private static class ViewLabelProvider extends LabelProvider {
    // // org.eclipse.jdt.ui\icons\full\elcl16\refresh_nav.gif
    @Override
    public Image getImage(Object obj) {
      return ((ICycleBaseElement) obj).getImage();
    }

    @Override
    public String getText(Object obj) {
      return ((ICycleBaseElement) obj).getText();
    }
  }

  // -------------------------------------------------------------------------
  // ContentProvider
  // -------------------------------------------------------------------------
  private static class ViewContentProvider implements ITreeContentProvider {

    public void dispose() {
      //
    }

    public Object[] getElements(Object parent) {
      return SearchResultRoot.getInstance().getChildren().toArray();
    }

    public Object[] getChildren(Object parent) {
      return ((ICycleBaseElement) parent).getChildren().toArray();
    }

    public Object getParent(Object child) {
      return ((ICycleBaseElement) child).getParent();
    }

    public boolean hasChildren(Object parent) {
      return ((ICycleBaseElement) parent).hasChildren();
    }

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
      //
    }
  }
}
