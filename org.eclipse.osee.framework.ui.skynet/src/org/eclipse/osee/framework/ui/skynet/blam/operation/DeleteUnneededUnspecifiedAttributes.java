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
package org.eclipse.osee.framework.ui.skynet.blam.operation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.attribute.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeType;
import org.eclipse.osee.framework.skynet.core.attribute.EnumeratedAttribute;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;

/**
 * @author Ryan D. Brooks
 */
public class DeleteUnneededUnspecifiedAttributes extends AbstractBlam {

   @Override
   public String getName() {
      return "Delete Unneeded Unspecified Attributes";
   }

   @Override
   public void runOperation(VariableMap variableMap, IProgressMonitor monitor) throws Exception {
      Branch branch = variableMap.getBranch("Branch");
      AttributeType attributeType = variableMap.getAttributeType("Attribute Type");
      List<Artifact> artifacts =
            ArtifactQuery.getArtifactsFromAttribute(attributeType, EnumeratedAttribute.UNSPECIFIED_VALUE, branch);
      SkynetTransaction transaction = new SkynetTransaction(branch);

      for (Artifact artifact : artifacts) {
         Collection<Attribute<String>> attributes = artifact.getAttributes(attributeType.getName());
         for (Attribute<String> attribute1 : attributes) {
            if (!attribute1.getValue().equals(EnumeratedAttribute.UNSPECIFIED_VALUE)) {
               for (Attribute<String> attribute : attributes) {
                  if (attribute.getValue().equals(EnumeratedAttribute.UNSPECIFIED_VALUE)) {
                     attribute.delete();
                  }
               }
               artifact.persistAttributes(transaction);
               break;
            }
         }
      }
      transaction.execute();
   }

   @Override
   public String getXWidgetsXml() {
      return "<xWidgets><XWidget xwidgetType=\"XAttributeTypeListViewer\" displayName=\"Attribute Type\" /><XWidget xwidgetType=\"XBranchSelectWidget\" displayName=\"Branch\" /></xWidgets>";
   }

   public Collection<String> getCategories() {
      return Arrays.asList("Admin");
   }
}