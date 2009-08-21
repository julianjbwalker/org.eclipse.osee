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
package org.eclipse.osee.framework.ui.skynet.panels;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.ui.skynet.ArtifactLabelProvider;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.ImageManager;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.dialogs.ArtifactSelectionDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactSelectPanel extends AbstractItemSelectPanel<Artifact> {

   public ArtifactSelectPanel() {
      super(new ArtifactLabelProvider(), new ArrayContentProvider());
   }

   @Override
   protected Dialog createSelectDialog(Shell shell, Artifact lastSelected) throws OseeCoreException {
      ArtifactSelectionDialog dialog = new ArtifactSelectionDialog(shell);
      dialog.setTitle("Select Destination Artifact");
      dialog.setMessage("Select a destination artifact. Imported items will be added as children of the selected artifact.");
      dialog.setImage(ImageManager.getImage(FrameworkImage.ARTIFACT_EXPLORER));
      dialog.setValidator(new SingleSelectionStatusValidator());
      Branch branch = lastSelected != null ? lastSelected.getBranch() : BranchManager.getCommonBranch();
      dialog.setInput(branch);
      if (lastSelected != null) {
         dialog.setInitialSelections(new Object[] {lastSelected});
      }
      return dialog;
   }

   @Override
   protected boolean updateFromDialogResult(Dialog dialog) {
      boolean wasUpdated = false;
      ArtifactSelectionDialog castedDialog = (ArtifactSelectionDialog) dialog;
      Artifact artifact = castedDialog.getFirstResult();
      if (artifact != null) {
         setSelected(artifact);
         wasUpdated = true;
      }
      return wasUpdated;
   }

   private final class SingleSelectionStatusValidator implements ISelectionStatusValidator {

      @Override
      public IStatus validate(Object[] selection) {
         IStatus status;
         if (selection == null || selection.length != 1) {
            status = new Status(IStatus.ERROR, SkynetGuiPlugin.PLUGIN_ID, IStatus.ERROR, "Must select 1 item", null);
         } else {
            status = new Status(IStatus.OK, SkynetGuiPlugin.PLUGIN_ID, 0, "", null);
         }
         return status;
      }
   }
}
