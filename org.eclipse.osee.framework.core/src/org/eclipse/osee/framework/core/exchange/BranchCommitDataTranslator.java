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
package org.eclipse.osee.framework.core.exchange;

import org.eclipse.osee.framework.core.IDataTranslationService;
import org.eclipse.osee.framework.core.data.Branch;
import org.eclipse.osee.framework.core.data.BranchCommitData;
import org.eclipse.osee.framework.core.data.IBasicArtifact;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.PropertyStore;

/**
 * @author Roberto E. Escobar
 */
public final class BranchCommitDataTranslator implements IDataTranslator<BranchCommitData> {

   private enum Entry {
      userArtifact,
      sourceBranch,
      destinationBranch,
      isArchiveAllowed;
   }

   private IDataTranslationService service;

   public BranchCommitDataTranslator() {
      service = null;
   }

   public void setService(IDataTranslationService service) {
      this.service = service;
   }

   public IDataTranslationService getService() {
      return service;
   }

   public BranchCommitData convert(PropertyStore propertyStore) throws OseeCoreException {
      PropertyStore sourceBranchStore = propertyStore.getPropertyStore(Entry.sourceBranch.name());
      PropertyStore destinationBranchStore = propertyStore.getPropertyStore(Entry.destinationBranch.name());
      PropertyStore userArtifactStore = propertyStore.getPropertyStore(Entry.userArtifact.name());

      IDataTranslationService service = getService();
      IBasicArtifact<?> userArtifact = service.convert(userArtifactStore, IBasicArtifact.class);
      Branch sourceBranch = service.convert(sourceBranchStore, Branch.class);
      Branch destinationBranch = service.convert(destinationBranchStore, Branch.class);

      boolean isArchiveAllowed = propertyStore.getBoolean(Entry.isArchiveAllowed.name());
      BranchCommitData data = new BranchCommitData(userArtifact, sourceBranch, destinationBranch, isArchiveAllowed);
      return data;
   }

   public PropertyStore convert(BranchCommitData data) throws OseeCoreException {
      IDataTranslationService service = getService();

      PropertyStore store = new PropertyStore();
      store.put(Entry.isArchiveAllowed.name(), data.isArchiveAllowed());
      store.put(Entry.userArtifact.name(), service.convert(data.getUser(), IBasicArtifact.class));
      store.put(Entry.sourceBranch.name(), service.convert(data.getSourceBranch(), Branch.class));
      store.put(Entry.destinationBranch.name(), service.convert(data.getDestinationBranch(), Branch.class));

      return store;
   }

}
