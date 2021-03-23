/*********************************************************************
 * Copyright (c) 2016 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.ui.skynet.widgets;

import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.OseeStateException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class XCheckBoxThreeStateDam extends XCheckBoxThreeState implements AttributeWidget {

   private Artifact artifact;
   private AttributeTypeToken attributeType;

   public XCheckBoxThreeStateDam(String displayLabel) {
      super(displayLabel);
   }

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   @Override
   public void saveToArtifact() {
      CheckState state = getCheckState();
      if (state == CheckState.UnSet) {
         artifact.deleteAttributes(attributeType);
      } else if (state == CheckState.Checked) {
         artifact.setSoleAttributeValue(attributeType, true);
      } else if (state == CheckState.UnChecked) {
         artifact.setSoleAttributeValue(attributeType, true);
      } else {
         throw new OseeStateException("UnExpected CheckState " + state.name());
      }
   }

   @Override
   public void revert() {
      setAttributeType(artifact, attributeType);
   }

   @Override
   public Result isDirty() {
      if (isEditable()) {
         CheckState storedCheckState = getStoredCheckState();
         CheckState checkState = getCheckState();
         if (storedCheckState != checkState) {
            new Result(true, getAttributeType().toString());
         }
      }
      return Result.FalseResult;
   }

   @Override
   public void refresh() {
      checkState = getStoredCheckState();
      updateCheckWidget();
   }

   @Override
   public void setAttributeType(Artifact artifact, AttributeTypeToken attributeType) {
      this.artifact = artifact;
      this.attributeType = attributeType;
      refresh();
   }

   private CheckState getStoredCheckState() {
      Boolean set = artifact.getSoleAttributeValue(this.attributeType, null);
      if (set == null) {
         return CheckState.UnSet;
      } else if (set) {
         return CheckState.Checked;
      } else {
         return CheckState.UnChecked;
      }
   }

   @Override
   public AttributeTypeToken getAttributeType() {
      return attributeType;
   }

}
