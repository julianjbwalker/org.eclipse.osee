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
package org.eclipse.osee.orcs.core.internal.attribute;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.orcs.core.ds.ArtifactData;
import org.eclipse.osee.orcs.core.ds.AttributeData;
import org.eclipse.osee.orcs.core.ds.AttributeDataFactory;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.ResourceNameResolver;
import org.eclipse.osee.orcs.data.AttributeTypes;

/**
 * @author Roberto E. Escobar
 */
public class AttributeFactory {

   private final AttributeClassResolver classResolver;
   private final AttributeDataFactory dataFactory;
   private final AttributeTypes cache;

   public AttributeFactory(AttributeClassResolver classResolver, AttributeDataFactory dataFactory, AttributeTypes cache) {
      this.classResolver = classResolver;
      this.dataFactory = dataFactory;
      this.cache = cache;
   }

   public <T> Attribute<T> createAttributeWithDefaults(AttributeContainer container, ArtifactData artifactData, IAttributeType attributeType) throws OseeCoreException {
      AttributeData data = dataFactory.create(artifactData, attributeType);
      return createAttribute(container, data, true, true);
   }

   public <T> Attribute<T> createAttribute(AttributeContainer container, AttributeData data) throws OseeCoreException {
      return createAttribute(container, data, false, false);
   }

   private <T> Attribute<T> createAttribute(AttributeContainer container, AttributeData data, boolean isDirty, boolean createWithDefaults) throws OseeCoreException {
      IAttributeType type = cache.getByUuid(data.getTypeUuid());
      Conditions.checkNotNull(type, "attributeType", "Cannot find attribute type with uuid[%s]", data.getTypeUuid());

      Attribute<T> attribute = classResolver.createAttribute(type);

      DataProxy proxy = data.getDataProxy();
      ResourceNameResolver resolver = createResolver(attribute);
      proxy.setResolver(resolver);

      Reference<AttributeContainer> artifactRef = new WeakReference<AttributeContainer>(container);

      attribute.internalInitialize(cache, artifactRef, data, isDirty, createWithDefaults);
      container.add(type, attribute);

      return attribute;
   }

   public <T> Attribute<T> copyAttribute(AttributeData source, IOseeBranch ontoBranch, AttributeContainer destinationContainer) throws OseeCoreException {
      AttributeData attributeData = dataFactory.copy(ontoBranch, source);
      Attribute<T> destinationAttribute = createAttribute(destinationContainer, attributeData, true, false);
      return destinationAttribute;
   }

   public <T> Attribute<T> cloneAttribute(AttributeData source, AttributeContainer destinationContainer) throws OseeCoreException {
      AttributeData attributeData = dataFactory.clone(source);
      Attribute<T> destinationAttribute = createAttribute(destinationContainer, attributeData, false, false);
      return destinationAttribute;
   }

   public <T> Attribute<T> introduceAttribute(AttributeData source, IOseeBranch ontoBranch, AttributeContainer destination) throws OseeCoreException {
      Attribute<T> introducedAttribute = null;
      // In order to reflect attributes they must exist in the data store
      if (source.getVersion().isInStorage()) {
         AttributeData attributeData = dataFactory.introduce(ontoBranch, source);
         introducedAttribute = createAttribute(destination, attributeData, true, false);
      }
      return introducedAttribute;
   }

   private ResourceNameResolver createResolver(Attribute<?> attribute) {
      return new AttributeResourceNameResolver(cache, attribute);
   }

   public int getMaxOccurrenceLimit(IAttributeType attributeType) throws OseeCoreException {
      return cache.getMaxOccurrences(attributeType);
   }

   public int getMinOccurrenceLimit(IAttributeType attributeType) throws OseeCoreException {
      return cache.getMinOccurrences(attributeType);
   }

}
