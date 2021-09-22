/*********************************************************************
 * Copyright (c) 2021 Boeing
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
package org.eclipse.osee.mim.types;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.framework.core.data.ApplicabilityToken;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.orcs.data.ArtifactReadable;

/**
 * @author Luciano T. Vaglienti
 */
public class InterfaceEnumerationSet extends PLGenericDBObject {

   private String Description;
   private ApplicabilityToken applicability;
   private List<InterfaceEnumeration> enumerations = new LinkedList<InterfaceEnumeration>();

   public InterfaceEnumerationSet(ArtifactToken art) {
      this((ArtifactReadable) art);
   }

   public InterfaceEnumerationSet(ArtifactReadable art) {
      super(art);
      this.setDescription(art.getSoleAttributeValue(CoreAttributeTypes.Description, ""));
   }

   public InterfaceEnumerationSet(Long id, String name) {
      super(id, name);
   }

   public InterfaceEnumerationSet() {
   }

   /**
    * @return the description
    */
   public String getDescription() {
      return Description;
   }

   /**
    * @param description the description to set
    */
   public void setDescription(String description) {
      Description = description;
   }

   /**
    * @return the applicability
    */
   public ApplicabilityToken getApplicability() {
      return applicability;
   }

   /**
    * @param applicability the applicability to set
    */
   public void setApplicability(ApplicabilityToken applicability) {
      this.applicability = applicability;
   }

   /**
    * @return the enumerations
    */
   public List<InterfaceEnumeration> getEnumerations() {
      return enumerations;
   }

   /**
    * @param enumerations the enumerations to set
    */
   public void setEnumerations(List<InterfaceEnumeration> enumerations) {
      this.enumerations = enumerations;
   }

}
