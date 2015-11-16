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
package org.eclipse.osee.orcs.core.ds.criteria;

import java.util.Collection;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.Options;

/**
 * @author John Misinco
 */
public class CriteriaAttributeTypeNotExists extends Criteria {

   private final Collection<? extends IAttributeType> attributeTypes;

   public CriteriaAttributeTypeNotExists(Collection<? extends IAttributeType> attributeTypes) {
      super();
      this.attributeTypes = attributeTypes;
   }

   public Collection<? extends IAttributeType> getTypes() {
      return attributeTypes;
   }

   @Override
   public void checkValid(Options options) throws OseeCoreException {
      Conditions.checkNotNull(getTypes(), "attribute types");
   }

   @Override
   public String toString() {
      return "CriteriaAttributeTypeNotExists [attributeType=" + attributeTypes + "]";
   }
}
