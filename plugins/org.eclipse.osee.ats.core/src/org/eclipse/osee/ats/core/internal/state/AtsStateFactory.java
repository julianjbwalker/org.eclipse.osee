/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.ats.core.internal.state;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.AtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.log.IAtsLogFactory;
import org.eclipse.osee.ats.api.workflow.state.IAtsStateFactory;
import org.eclipse.osee.ats.api.workflow.state.IAtsStateManager;
import org.eclipse.osee.ats.api.workflow.state.IAtsWorkStateFactory;
import org.eclipse.osee.framework.core.data.TransactionId;
import org.eclipse.osee.framework.jdk.core.type.Id;

/**
 * @author Donald G. Dunne
 */
public class AtsStateFactory implements IAtsStateFactory {

   private final IAtsWorkStateFactory workStateFactory;
   private final IAtsLogFactory logFactory;
   private final AtsApi atsApi;
   Map<Id, IAtsStateManager> idToStateManager = new HashMap<>();
   Map<Id, TransactionId> idToTransactionId = new HashMap<>();

   public AtsStateFactory(AtsApi atsApi, IAtsWorkStateFactory workStateFactory, IAtsLogFactory logFactory) {
      this.atsApi = atsApi;
      this.workStateFactory = workStateFactory;
      this.logFactory = logFactory;
   }

   @Override
   public IAtsStateManager getStateManager(IAtsWorkItem workItem) {
      IAtsStateManager stateMgr = idToStateManager.get(workItem);
      TransactionId transId = idToTransactionId.get(workItem);
      TransactionId workItemTransaction = atsApi.getStoreService().getTransactionId(workItem);
      if (stateMgr == null || workItemTransaction.isValid() && workItemTransaction.notEqual(transId)) {
         stateMgr = new StateManager(workItem, logFactory, atsApi);
         idToStateManager.put(workItem, stateMgr);
         idToTransactionId.put(workItem, workItemTransaction);
         StateManagerStore.load(workItem, stateMgr, atsApi.getAttributeResolver(), workStateFactory);
      }
      return stateMgr;
   }

   @Override
   public void writeToStore(AtsUser asUser, IAtsWorkItem workItem, IAtsChangeSet changes) {
      StateManagerStore.writeToStore(asUser, workItem, (StateManager) workItem.getStateMgr(),
         atsApi.getAttributeResolver(), changes, workStateFactory);
   }

   @Override
   public void load(IAtsWorkItem workItem, IAtsStateManager stateMgr) {
      StateManagerStore.load(workItem, stateMgr, atsApi.getAttributeResolver(), workStateFactory);
   }

   @Override
   public void clearStateManager(Id id) {
      idToStateManager.put(id, null);
   }

   @Override
   public void setStateMgr(IAtsWorkItem workItem, IAtsStateManager stateMgr) {
      idToStateManager.put(workItem, stateMgr);
      idToTransactionId.put(workItem, atsApi.getStoreService().getTransactionId(workItem));
   }

}
