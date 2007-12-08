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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.ats.ActionDebug;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.actions.NewAction;
import org.eclipse.osee.framework.ui.plugin.util.db.ConnectionHandler;
import org.eclipse.osee.framework.ui.skynet.SkynetContributionItem;
import org.eclipse.osee.framework.ui.skynet.ats.IActionable;
import org.eclipse.osee.framework.ui.skynet.ats.OseeAts;
import org.eclipse.osee.framework.ui.skynet.widgets.xnavigate.XNavigateItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

/**
 * Insert the type's description here.
 * 
 * @see ViewPart
 * @author Donald G. Dunne
 */
public class NavigateView extends ViewPart implements IActionable {

   public static final String VIEW_ID = "org.eclipse.osee.ats.navigate.NavigateView";
   public static final String HELP_CONTEXT_ID = "atsNavigator";
   private ActionDebug debug = new ActionDebug(false, "NavigateView");
   private AtsNavigateComposite xNavComp;

   /**
    * The constructor.
    */
   public NavigateView() {
   }

   public void setFocus() {
   }

   /*
    * @see IWorkbenchPart#createPartControl(Composite)
    */
   public void createPartControl(Composite parent) {
      debug.report("createPartControl");

      try {
         ConnectionHandler.getConnection();
      } catch (Exception ex) {
         (new Label(parent, SWT.NONE)).setText("  DB Connection Unavailable");
         return;
      }

      SkynetContributionItem.addTo(this, true);

      xNavComp = new AtsNavigateComposite(new AtsNavigateViewItems(), parent, SWT.NONE);

      AtsPlugin.getInstance().setHelp(xNavComp, HELP_CONTEXT_ID);
      createActions();
      xNavComp.refresh();
      xNavComp.getFilteredTree().getFilterControl().setFocus();
   }

   protected void createActions() {
      debug.report("createActions");

      Action collapseAction = new Action("Collapse All") {

         public void run() {
            xNavComp.getFilteredTree().getViewer().collapseAll();
         }
      };
      collapseAction.setImageDescriptor(AtsPlugin.getInstance().getImageDescriptor("collapseAll.gif"));
      collapseAction.setToolTipText("Collapse All");

      Action refreshAction = new Action("Refresh") {

         public void run() {
            xNavComp.refresh();
         }
      };
      refreshAction.setImageDescriptor(AtsPlugin.getInstance().getImageDescriptor("refresh.gif"));
      refreshAction.setToolTipText("Refresh");

      OseeAts.addBugToViewToolbar(this, this, AtsPlugin.getInstance(), VIEW_ID, "ATS Navigator");

      IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
      toolbarManager.add(collapseAction);
      toolbarManager.add(refreshAction);
      toolbarManager.add(new NewAction());
   }

   public String getActionDescription() {
      IStructuredSelection sel = (IStructuredSelection) xNavComp.getFilteredTree().getViewer().getSelection();
      if (sel.iterator().hasNext()) return String.format("Currently Selected - %s",
            ((XNavigateItem) sel.iterator().next()).getName());
      return "";
   }

}