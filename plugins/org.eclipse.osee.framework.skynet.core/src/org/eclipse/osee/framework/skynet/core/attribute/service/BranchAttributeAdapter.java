/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.skynet.core.attribute.service;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.jdk.core.type.Identity;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeAdapter;

public class BranchAttributeAdapter implements AttributeAdapter<IOseeBranch> {

   @Override
   public IOseeBranch adapt(Attribute<?> attribute, Identity<String> identity) throws OseeCoreException {
      String uuid = identity.getGuid();
      return Strings.isNumeric(uuid) ? BranchManager.getBranch(Long.valueOf(uuid)) : null;
   }

   @Override
   public Collection<IAttributeType> getSupportedTypes() {
      return Collections.singleton(CoreAttributeTypes.BranchReference);
   }

}