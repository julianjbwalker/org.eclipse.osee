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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.osee.accessor.types.ArtifactAccessorResult;
import org.eclipse.osee.framework.core.data.ApplicabilityToken;
import org.eclipse.osee.framework.core.data.ArtifactReadable;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Luciano T. Vaglienti
 */
public class InterfaceStructureToken extends ArtifactAccessorResult {
   public static final InterfaceStructureToken SENTINEL = new InterfaceStructureToken();

   private String nameAbbrev;

   private String InterfaceStructureCategory;

   private String InterfaceMinSimultaneity;

   private String InterfaceMaxSimultaneity;

   private Integer InterfaceTaskFileType;

   private Integer numElements = 0;

   private Double sizeInBytes = 0.0;

   private String Description;

   private boolean autogenerated = false;

   private Collection<InterfaceStructureElementToken> elements = new LinkedList<>();

   private ApplicabilityToken applicability;

   public InterfaceStructureToken(ArtifactToken art) {
      this((ArtifactReadable) art);
   }

   public InterfaceStructureToken(ArtifactReadable art) {
      super(art);
      this.setId(art.getId());
      this.setName(art.getName());
      this.setNameAbbrev(art.getSoleAttributeValue(CoreAttributeTypes.NameAbbrev, ""));
      this.setDescription(art.getSoleAttributeValue(CoreAttributeTypes.Description, ""));
      this.setInterfaceMaxSimultaneity(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceMaxSimultaneity, ""));
      this.setInterfaceMinSimultaneity(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceMinSimultaneity, ""));
      this.setInterfaceStructureCategory(
         art.getSoleAttributeAsString(CoreAttributeTypes.InterfaceStructureCategory, ""));
      this.setInterfaceTaskFileType(art.getSoleAttributeValue(CoreAttributeTypes.InterfaceTaskFileType, 0));
      this.setElements(
         art.getRelated(CoreRelationTypes.InterfaceStructureContent_DataElement).getList().stream().filter(
            a -> !a.getExistingAttributeTypes().isEmpty()).map(a -> new InterfaceStructureElementToken(a)).collect(
               Collectors.toList()));
      this.setApplicability(
         !art.getApplicabilityToken().getId().equals(-1L) ? art.getApplicabilityToken() : ApplicabilityToken.SENTINEL);
   }

   /**
    * @param id
    * @param name
    */
   public InterfaceStructureToken(Long id, String name) {
      super(id, name);
   }

   /**
    *
    */
   public InterfaceStructureToken() {
      super();
   }

   public String getNameAbbrev() {
      return nameAbbrev;
   }

   public void setNameAbbrev(String nameAbbrev) {
      this.nameAbbrev = nameAbbrev;
   }

   /**
    * @return the interfaceStructureCategory
    */
   public String getInterfaceStructureCategory() {
      return InterfaceStructureCategory;
   }

   /**
    * @param interfaceStructureCategory the interfaceStructureCategory to set
    */
   public void setInterfaceStructureCategory(String interfaceStructureCategory) {
      InterfaceStructureCategory = interfaceStructureCategory;
   }

   /**
    * @return the interfaceMinSimultaneity
    */
   public String getInterfaceMinSimultaneity() {
      return InterfaceMinSimultaneity;
   }

   /**
    * @param interfaceMinSimultaneity the interfaceMinSimultaneity to set
    */
   public void setInterfaceMinSimultaneity(String interfaceMinSimultaneity) {
      InterfaceMinSimultaneity = interfaceMinSimultaneity;
   }

   /**
    * @return the interfaceMaxSimultaneity
    */
   public String getInterfaceMaxSimultaneity() {
      return InterfaceMaxSimultaneity;
   }

   /**
    * @param interfaceMaxSimultaneity the interfaceMaxSimultaneity to set
    */
   public void setInterfaceMaxSimultaneity(String interfaceMaxSimultaneity) {
      InterfaceMaxSimultaneity = interfaceMaxSimultaneity;
   }

   /**
    * @return the interfaceTaskFileType
    */
   public Integer getInterfaceTaskFileType() {
      return InterfaceTaskFileType;
   }

   /**
    * @param interfaceTaskFileType the interfaceTaskFileType to set
    */
   public void setInterfaceTaskFileType(Integer interfaceTaskFileType) {
      InterfaceTaskFileType = interfaceTaskFileType;
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
    * @return the elements
    */
   public List<InterfaceStructureElementToken> getElements() {
      return (List<InterfaceStructureElementToken>) elements;
   }

   /**
    * @param elements the elements to set
    */
   public void setElements(Collection<InterfaceStructureElementToken> elements) {
      this.elements = elements;
      setNumElements(elements.stream().filter(e -> e.isIncludedInCounts()).collect(Collectors.toList()).size());
      setSizeInBytes(elements.stream().filter(e -> e.isIncludedInCounts()).collect(
         Collectors.summingDouble(InterfaceStructureElementToken::getElementSizeInBytes)));
   }

   /**
    * @return the numElements
    */
   public Integer getNumElements() {
      return this.numElements;
   }

   public void setNumElements(Integer numElements) {
      this.numElements = numElements;
   }

   /**
    * @return the sizeInBytes
    */
   public Double getSizeInBytes() {
      return this.sizeInBytes;
   }

   public void setSizeInBytes(Double sizeInBytes) {
      this.sizeInBytes = sizeInBytes;
   }

   public boolean getIncorrectlySized() {
      return this.getSizeInBytes() % 8 != 0;
   }

   /**
    * @return the bytesPerSecondMaximum
    */
   public Double getBytesPerSecondMaximum() {
      return this.getSizeInBytes() * (Strings.isNumeric(this.getInterfaceMaxSimultaneity()) ? Double.parseDouble(
         this.getInterfaceMaxSimultaneity()) : 0.0);
   }

   /**
    * @return the bytesPerSecondMinimum
    */
   public Double getBytesPerSecondMinimum() {
      return this.getSizeInBytes() * (Strings.isNumeric(this.getInterfaceMinSimultaneity()) ? Double.parseDouble(
         this.getInterfaceMinSimultaneity()) : 0.0);
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
    * @return the autogenerated
    */
   public boolean isAutogenerated() {
      return autogenerated;
   }

   /**
    * @param autogenerated the autogenerated to set
    */
   public void setAutogenerated(boolean autogenerated) {
      this.autogenerated = autogenerated;
   }

}
