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
package org.eclipse.osee.framework.ui.skynet.widgets;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.AttributeDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.validation.IOseeValidator;
import org.eclipse.osee.framework.skynet.core.validation.OseeValidator;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

public class XComboBooleanDam extends XCombo implements IAttributeWidget {

   private Artifact artifact;
   private IAttributeType attributeType;

   public XComboBooleanDam(String displayLabel) {
      super(displayLabel);
   }

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   @Override
   public IAttributeType getAttributeType() {
      return attributeType;
   }

   @Override
   public void setAttributeType(Artifact artifact, IAttributeType attributeType) throws OseeCoreException {
      this.artifact = artifact;
      this.attributeType = attributeType;
      Boolean result = artifact.getSoleAttributeValue(this.attributeType, null);
      if (result == null) {
         super.set("");
      } else {
         super.set(result ? "yes" : "no");
      }
   }

   @Override
   public void saveToArtifact() {
      try {
         if (!Strings.isValid(data)) {
            artifact.deleteSoleAttribute(attributeType);
         } else {
            String enteredValue = get();
            artifact.setSoleAttributeValue(attributeType, (enteredValue != null && enteredValue.equals("yes")));
         }
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public Result isDirty() throws OseeCoreException {
      try {
         String enteredValue = get();
         boolean storedValue = artifact.getSoleAttributeValue(attributeType);
         if (enteredValue.equals("yes") != storedValue) {
            return new Result(true, attributeType + " is dirty");
         }
      } catch (AttributeDoesNotExist ex) {
         if (!get().equals("")) {
            return new Result(true, attributeType + " is dirty");
         }
      }
      return Result.FalseResult;
   }

   @Override
   public void revert() throws OseeCoreException {
      setAttributeType(artifact, attributeType);
   }

   @Override
   public IStatus isValid() {
      IStatus status = super.isValid();
      if (status.isOK()) {
         status = OseeValidator.getInstance().validate(IOseeValidator.SHORT, artifact, attributeType, get());
      }
      return status;
   }
}
