/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.widgets.dialog;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.plugin.util.ArrayTreeContentProvider;
import org.eclipse.osee.framework.ui.skynet.branch.BranchLabelProvider;
import org.eclipse.osee.framework.ui.skynet.branch.BranchNameSorter;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class FilteredCheckboxBranchDialog extends FilteredCheckboxTreeDialog {

   private Collection<? extends IOseeBranch> selectable;

   public FilteredCheckboxBranchDialog(String title, String message, Collection<? extends IOseeBranch> selectable) {
      this(title, message, selectable, new ArrayTreeContentProvider(), new BranchLabelProvider());
   }

   public FilteredCheckboxBranchDialog(String title, String message, Collection<? extends IOseeBranch> selectable, ILabelProvider labelProvider) {
      this(title, message, selectable, new ArrayTreeContentProvider(), labelProvider);
   }

   public FilteredCheckboxBranchDialog(String title, String message, Collection<? extends IOseeBranch> selectable, ITreeContentProvider contentProvider, ILabelProvider labelProvider) {
      super(title, message, contentProvider, labelProvider, new BranchNameSorter());
      this.selectable = selectable;
   }

   public FilteredCheckboxBranchDialog(String title, Collection<? extends IOseeBranch> selectable) {
      this(title, title, selectable, new BranchLabelProvider());
   }

   @SuppressWarnings("unchecked")
   @Override
   public Collection<IOseeBranch> getChecked() {
      if (super.getTreeViewer() == null) {
         return Collections.emptyList();
      }
      Set<IOseeBranch> checked = new HashSet<IOseeBranch>();
      for (Object obj : getResult()) {
         checked.add((IOseeBranch) obj);
      }
      return checked;
   }

   @Override
   protected Control createDialogArea(Composite container) {
      Control comp = super.createDialogArea(container);
      try {
         getTreeViewer().getViewer().setInput(selectable);
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return comp;
   }

   @Override
   protected Result isComplete() {
      return super.isComplete();
   }

   public Collection<? extends IOseeBranch> getSelectable() {
      return selectable;
   }

   public void setSelectable(Collection<IOseeBranch> selectable) {
      this.selectable = selectable;
   }

}
