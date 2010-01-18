/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.navigate;

import java.util.logging.Level;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.ats.actions.MyFavoritesAction;
import org.eclipse.osee.ats.actions.MyWorldAction;
import org.eclipse.osee.ats.actions.NewAction;
import org.eclipse.osee.ats.actions.NewGoal;
import org.eclipse.osee.ats.actions.OpenChangeReportByIdAction;
import org.eclipse.osee.ats.actions.OpenWorkflowByIdAction;
import org.eclipse.osee.ats.actions.OpenWorldByIdAction;
import org.eclipse.osee.ats.config.AtsBulkLoad;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.IActionable;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.ui.plugin.OseeUiActions;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.skynet.OseeContributionItem;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.action.CollapseAllAction;
import org.eclipse.osee.framework.ui.skynet.action.ExpandAllAction;
import org.eclipse.osee.framework.ui.skynet.notify.OseeNotificationManager;
import org.eclipse.osee.framework.ui.skynet.util.DbConnectionExceptionComposite;
import org.eclipse.osee.framework.ui.skynet.widgets.XCheckBox;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Donald G. Dunne
 */
public class NavigateView extends ViewPart implements IActionable {

   public static final String VIEW_ID = "org.eclipse.osee.ats.navigate.NavigateView";
   public static final String HELP_CONTEXT_ID = "atsNavigator";
   private AtsNavigateComposite xNavComp;
   public Text searchArea;
   public XCheckBox completeCancelledCheck;
   private boolean includeCompleteCancelled = false;

   public NavigateView() {
   }

   @Override
   public void setFocus() {
   }

   @Override
   public void dispose() {
      try {
         OseeNotificationManager.sendNotifications();
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE, ex);
      }
      super.dispose();
   }

   @Override
   public void createPartControl(Composite parent) {
      if (!DbConnectionExceptionComposite.dbConnectionIsOk(parent)) {
         return;
      }

      OseeContributionItem.addTo(this, false);

      xNavComp = new AtsNavigateComposite(new AtsNavigateViewItems(), parent, SWT.NONE);

      AtsPlugin.getInstance().setHelp(xNavComp, HELP_CONTEXT_ID, "org.eclipse.osee.ats.help.ui");
      createToolBar();
      getViewSite().getActionBars().updateActionBars();

      // add search text box      
      createSearchInputPart(xNavComp);

      if (savedFilterStr != null) {
         xNavComp.getFilteredTree().getFilterControl().setText(savedFilterStr);
      }
      xNavComp.refresh();
      xNavComp.getFilteredTree().getFilterControl().setFocus();

      Label label = new Label(xNavComp, SWT.None);
      String str = getWhoAmI();
      if (AtsUtil.isAtsAdmin()) {
         str += " - Admin";
      }
      if (!str.equals("")) {
         if (AtsUtil.isAtsAdmin()) {
            label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
         } else {
            label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
         }
      }
      label.setText(str);
      label.setToolTipText(str);
      GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_CENTER);
      gridData.heightHint = 15;
      label.setLayoutData(gridData);

      AtsBulkLoad.run(false);
   }

   public void createSearchInputPart(Composite parent) {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(ALayout.getZeroMarginLayout(4, false));
      comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      Button searchButton = new Button(comp, SWT.PUSH);
      searchButton.setText("Search:");
      searchButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            try {
               xNavComp.handleDoubleClick(new SearchNavigateItem(null, new AtsNavigateQuickSearch("ATS Quick Search",
                     searchArea.getText(), isIncludeCompleteCancelled())));
            } catch (OseeCoreException ex) {
               OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });

      GridData gridData = new GridData(SWT.RIGHT, SWT.NONE, false, false);
      gridData.heightHint = 15;
      this.searchArea = new Text(comp, SWT.SINGLE | SWT.BORDER);
      GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
      this.searchArea.setFont(parent.getFont());
      this.searchArea.setLayoutData(gd);
      this.searchArea.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent event) {
            if (event.character == '\r') {
               try {
                  xNavComp.handleDoubleClick(new SearchNavigateItem(null, new AtsNavigateQuickSearch(
                        "ATS Quick Search", searchArea.getText(), isIncludeCompleteCancelled())));
               } catch (OseeCoreException ex) {
                  OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
               }
            }
         }
      });
      this.searchArea.setToolTipText("ATS Quick Search - Type in a search string.");
      this.completeCancelledCheck = new XCheckBox("IC");
      this.completeCancelledCheck.createWidgets(comp, 2);
      this.completeCancelledCheck.setToolTip("Include completed/cancelled ATS Artifacts");
      completeCancelledCheck.addSelectionListener(new SelectionListener() {
         public void widgetDefaultSelected(SelectionEvent e) {
         }

         public void widgetSelected(SelectionEvent e) {
            includeCompleteCancelled = completeCancelledCheck.isSelected();
         };
      });
   }

   public boolean isIncludeCompleteCancelled() {
      return includeCompleteCancelled;
   }

   private String getWhoAmI() {
      try {
         String userName = UserManager.getUser().getName();
         return String.format("%s - %s:%s", userName, ClientSessionManager.getDataStoreName(),
               ClientSessionManager.getDataStoreLoginName());
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         return "Exception: " + ex.getLocalizedMessage();
      }
   }

   protected void createToolBar() {
      IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
      toolbarManager.add(new MyWorldAction());
      toolbarManager.add(new MyFavoritesAction());
      toolbarManager.add(new CollapseAllAction(xNavComp.getFilteredTree().getViewer()));
      toolbarManager.add(new ExpandAllAction(xNavComp.getFilteredTree().getViewer()));
      toolbarManager.add(new OpenChangeReportByIdAction());
      toolbarManager.add(new OpenWorldByIdAction());
      toolbarManager.add(new OpenWorkflowByIdAction());
      if (AtsUtil.isGoalEnabled()) {
         toolbarManager.add(new NewGoal());
      }
      toolbarManager.add(new NewAction());

      OseeUiActions.addBugToViewToolbar(this, this, AtsPlugin.getInstance(), VIEW_ID, "ATS Navigator");
   }

   /**
    * Provided for tests to be able to simulate a double-click
    */
   public void handleDoubleClick(XNavigateItem item, TableLoadOption... tableLoadOptions) throws OseeCoreException {
      OseeLog.log(AtsPlugin.class, Level.INFO,
            "===> Simulating NavigateView Double-Click for \"" + item.getName() + "\"...");
      xNavComp.handleDoubleClick(item, tableLoadOptions);
   }

   public static NavigateView getNavigateView() {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      try {
         return (NavigateView) page.showView(NavigateView.VIEW_ID);
      } catch (PartInitException e1) {
         MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Launch Error",
               "Couldn't Launch OSEE ATS NavigateView " + e1.getMessage());
      }
      return null;
   }

   public String getActionDescription() {
      IStructuredSelection sel = (IStructuredSelection) xNavComp.getFilteredTree().getViewer().getSelection();
      if (sel.iterator().hasNext()) {
         return String.format("Currently Selected - %s", ((XNavigateItem) sel.iterator().next()).getName());
      }
      return "";
   }

   private static final String INPUT = "filter";
   private static final String FILTER_STR = "filterStr";

   @Override
   public void saveState(IMemento memento) {
      super.saveState(memento);
      memento = memento.createChild(INPUT);

      if (xNavComp != null && xNavComp.getFilteredTree().getFilterControl() != null && !xNavComp.getFilteredTree().isDisposed()) {
         String filterStr = xNavComp.getFilteredTree().getFilterControl().getText();
         memento.putString(FILTER_STR, filterStr);
      }
   }
   private String savedFilterStr = null;

   @Override
   public void init(IViewSite site, IMemento memento) throws PartInitException {
      super.init(site, memento);
      try {
         if (memento != null) {
            memento = memento.getChild(INPUT);
            if (memento != null) {
               savedFilterStr = memento.getString(FILTER_STR);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.WARNING, "NavigateView error on init", ex);
      }
   }
}