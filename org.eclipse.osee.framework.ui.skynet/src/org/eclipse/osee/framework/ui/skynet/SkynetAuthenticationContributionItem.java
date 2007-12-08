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
package org.eclipse.osee.framework.ui.skynet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.ui.plugin.event.AuthenticationEvent;
import org.eclipse.osee.framework.ui.plugin.event.Event;
import org.eclipse.osee.framework.ui.plugin.security.AuthenticationDialog;
import org.eclipse.osee.framework.ui.plugin.security.OseeAuthentication;
import org.eclipse.osee.framework.ui.plugin.security.UserCredentials.UserCredentialEnum;
import org.eclipse.osee.framework.ui.plugin.util.OverlayImage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

/**
 * @author Roberto E. Escobar
 */
public class SkynetAuthenticationContributionItem extends SkynetContributionItem {

   private static final String ID = "skynet.authentication";

   private static final SkynetEventManager eventManager = SkynetEventManager.getInstance();
   private static final OseeAuthentication oseeAuthentication = OseeAuthentication.getInstance();
   private static final SkynetAuthentication skynetAuthentication = SkynetAuthentication.getInstance();

   private static final Image ENABLED_IMAGE = SkynetGuiPlugin.getInstance().getImage("user.gif");
   private static final Image DISABLED_IMAGE = new OverlayImage(ENABLED_IMAGE,
         SkynetGuiPlugin.getInstance().getImageDescriptor("red_slash.gif")).createImage();

   private static String ENABLED_TOOLTIP = "Authenticated as: ";
   private static String DISABLED_TOOLTIP = "Not Authenticated.\n" + "Double-Click to Log On.";

   public SkynetAuthenticationContributionItem() {
      super(ID, ENABLED_IMAGE, DISABLED_IMAGE, ENABLED_TOOLTIP, DISABLED_TOOLTIP, eventManager);
      init();
   }

   private void init() {
      setActionHandler(new Action() {

         public void run() {
            if (oseeAuthentication.isAuthenticated()) {
               boolean result = MessageDialog.openQuestion(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                     "Log Off...", "Are you sure you want to log off and exit OSEE?");
               if (result) {
                  oseeAuthentication.logOff();
                  PlatformUI.getWorkbench().close();
               }
            } else {
               if (oseeAuthentication.isLoginAllowed()) {
                  AuthenticationDialog.openDialog();
               } else {
                  oseeAuthentication.authenticate("", "", "", false);
               }
            }
            notifyListeners();
         }
      });

      eventManager.register(AuthenticationEvent.class, this);
      notifyListeners();
   }

   private void notifyListeners() {
      eventManager.kick(new AuthenticationEvent(this));
   }

   public static void addTo(IStatusLineManager manager) {
      for (IContributionItem item : manager.getItems())
         if (item instanceof SkynetAuthenticationContributionItem) return;
      manager.add(new SkynetAuthenticationContributionItem());
   }

   public void onEvent(Event event) {
      if (event instanceof AuthenticationEvent) {

         if (oseeAuthentication.isAuthenticated()) {
            User skynetName = skynetAuthentication.getAuthenticatedUser();

            this.setEnabledToolTip(String.format(ENABLED_TOOLTIP + "%s (%s)\nDouble-Click to Log Off.",
                  (skynetName != null ? skynetName.getName() : skynetName),
                  oseeAuthentication.getCredentials().getField(UserCredentialEnum.Id)));
         }
         updateStatus(oseeAuthentication.isAuthenticated());
      }
   }

   public boolean runOnEventInDisplayThread() {
      return true;
   }

   @Override
   public void dispose() {
      eventManager.unRegister(AuthenticationEvent.class, this);
   }
}
