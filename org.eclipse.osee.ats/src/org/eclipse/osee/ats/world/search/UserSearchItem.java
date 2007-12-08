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
package org.eclipse.osee.ats.world.search;

import java.sql.SQLException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.UserListDialog;
import org.eclipse.swt.widgets.Display;

/**
 * @author Donald G. Dunne
 */
public abstract class UserSearchItem extends WorldSearchItem {

   protected final User user;
   protected User selectedUser;

   public UserSearchItem(String name) {
      this(name, null);
   }

   public UserSearchItem(String name, User user) {
      super(name);
      this.user = user;
   }

   @Override
   public String getSelectedName() {
      return String.format("%s - %s", super.getSelectedName(), getUserSearchName());
   }

   public String getUserSearchName() {
      if (user != null)
         return user.getName();
      else if (selectedUser != null) return selectedUser.getName();
      return "";
   }

   @Override
   public void performSearch() throws SQLException, IllegalArgumentException {
      if (isCancelled()) return;
      if (user != null)
         searchIt(user);
      else
         searchIt();
   }

   protected void searchIt(User user) throws SQLException, IllegalArgumentException {
      if (isCancelled()) return;
   }

   private void searchIt() throws SQLException, IllegalArgumentException {
      if (isCancelled()) return;
      if (selectedUser != null) searchIt(selectedUser);
   }

   @Override
   public boolean performUI() {
      if (user != null) return true;
      UserListDialog ld = new UserListDialog(Display.getCurrent().getActiveShell());
      int result = ld.open();
      if (result == 0) {
         selectedUser = (User) ld.getSelection();
         return true;
      } else
         selectedUser = null;
      return false;
   }
}
