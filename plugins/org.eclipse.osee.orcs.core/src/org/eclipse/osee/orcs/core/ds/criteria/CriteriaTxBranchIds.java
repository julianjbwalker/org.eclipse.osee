/*******************************************************************************
 * Copyright (c) 2013 Boeing.
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
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.orcs.core.ds.Criteria;
import org.eclipse.osee.orcs.core.ds.Options;

/**
 * @author Roberto E. Escobar
 */
public class CriteriaTxBranchIds extends Criteria {

   private final Collection<Integer> ids;

   public CriteriaTxBranchIds(Collection<Integer> ids) {
      super();
      this.ids = ids;
   }

   public Collection<Integer> getIds() {
      return ids;
   }

   @Override
   public void checkValid(Options options) throws OseeCoreException {
      super.checkValid(options);
      Conditions.checkExpressionFailOnTrue(getIds().isEmpty(), "Branch Ids cannot be empty");
   }

   @Override
   public String toString() {
      return "CriteriaTxBranchIds [ids=" + ids + "]";
   }

}
