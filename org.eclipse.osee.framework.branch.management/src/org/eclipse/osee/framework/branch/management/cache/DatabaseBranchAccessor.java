/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.branch.management.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.framework.branch.management.internal.InternalBranchActivator;
import org.eclipse.osee.framework.core.cache.BranchCache;
import org.eclipse.osee.framework.core.cache.IOseeCache;
import org.eclipse.osee.framework.core.cache.TransactionCache;
import org.eclipse.osee.framework.core.enums.BranchArchivedState;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.TransactionDetailsType;
import org.eclipse.osee.framework.core.exception.BranchDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.BranchFactory;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.services.IOseeCachingServiceProvider;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryServiceProvider;
import org.eclipse.osee.framework.database.IOseeDatabaseServiceProvider;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Roberto E. Escobar
 */
public class DatabaseBranchAccessor extends AbstractDatabaseAccessor<Branch> {
   public static final int NULL_PARENT_BRANCH_ID = -1;
   private static final String SELECT_BRANCHES =
         "SELECT ob.*, txd.transaction_id FROM osee_branch ob, osee_tx_details txd WHERE ob.branch_id = txd.branch_id and txd.tx_type = " + TransactionDetailsType.Baselined.getId();
   //   private static final String INSERT_BRANCH =
   //         "INSERT INTO osee_branch (branch_id, branch_guid, branch_name, parent_branch_id, parent_transaction_id, archived, associated_art_id, branch_type, branch_state) VALUES (?,?,?,?,?,?,?,?,?)";
   //   private static final String DELETE_BRANCH = "DELETE from osee_branch where branch_id = ?";

   private static final String SELECT_MERGE_BRANCHES = "SELECT * FROM osee_merge";

   private static final String SELECT_BRANCH_ALIASES =
         "select * from osee_branch_definitions order by mapped_branch_id";

   private final IOseeCachingServiceProvider cachingService;

   public DatabaseBranchAccessor(IOseeDatabaseServiceProvider databaseProvider, IOseeModelFactoryServiceProvider factoryProvider, IOseeCachingServiceProvider cachingService) {
      super(databaseProvider, factoryProvider);
      this.cachingService = cachingService;
   }

   //   private Object[] toInsertValues(Branch type) throws OseeCoreException {
   //      Branch parentBranch = type.getParentBranch();
   //      int parentBranchId = parentBranch != null ? parentBranch.getId() : NULL_PARENT_BRANCH_ID;
   //      return new Object[] {type.getId(), type.getGuid(), type.getName(),
   //            parentBranchId, type.getBaseTransaction().getTransactionNumber(), type.getArchiveState().getValue(),
   //            type.getAssociatedArtifactId(), type.getBranchType().getValue(), type.getBranchState().getValue()};
   //   }

   @Override
   public void load(IOseeCache<Branch> cache) throws OseeCoreException {
      long startTime = System.currentTimeMillis();
      Map<Branch, Integer> childToParent = new HashMap<Branch, Integer>();
      Map<Branch, Integer> branchToSourceTx = new HashMap<Branch, Integer>();
      Map<Branch, Integer> branchToBaseTx = new HashMap<Branch, Integer>();
      Map<Branch, Integer> associatedArtifact = new HashMap<Branch, Integer>();

      BranchCache brCache = (BranchCache) cache;
      loadBranches(brCache, childToParent, branchToBaseTx, branchToSourceTx, associatedArtifact);
      loadBranchHierarchy(brCache, childToParent);
      loadMergeBranches(brCache);
      loadBranchAliases(brCache);
      loadAssociatedArtifacts(brCache, associatedArtifact);
      loadBranchRelatedTransactions(brCache, branchToBaseTx, branchToSourceTx);

      for (Branch branch : cache.getAll()) {
         branch.clearDirty();
      }
      OseeLog.log(InternalBranchActivator.class, Level.INFO, String.format("Branch Cache loaded [%s]",
            Lib.getElapseString(startTime)));
   }

   private void loadAssociatedArtifacts(BranchCache cache, Map<Branch, Integer> associatedArtifact) throws OseeCoreException {
      //      if (cache.getDefaultAssociatedArtifact() == null) {
      //         cache.setDefaultAssociatedArtifact(new ShallowArtifact(cache, -1));
      //      }
      //      for (Entry<Branch, Integer> entry : associatedArtifact.entrySet()) {
      //         Branch branch = entry.getKey();
      //         branch.setAssociatedArtifact(new ShallowArtifact(cache, entry.getValue()));
      //      }
   }

   private void loadBranchRelatedTransactions(BranchCache cache, Map<Branch, Integer> branchToBaseTx, Map<Branch, Integer> branchToSourceTx) throws OseeCoreException {
      //      Set<Integer> transactions = Collections.setUnion(branchToBaseTx.values(), branchToSourceTx.values());
      TransactionCache txCache = cachingService.getOseeCachingService().getTransactionCache();
      txCache.ensurePopulated();

      for (Entry<Branch, Integer> entry : branchToBaseTx.entrySet()) {
         Branch branch = entry.getKey();
         if (branch.getBaseTransaction() == null) {
            TransactionRecord baseTransaction = txCache.getById(entry.getValue());
            cache.cacheBaseTransaction(branch, baseTransaction);
         }
      }

      for (Entry<Branch, Integer> entry : branchToBaseTx.entrySet()) {
         Branch branch = entry.getKey();
         if (branch.getSourceTransaction() == null) {
            TransactionRecord sourceTransaction = txCache.getById(entry.getValue());
            cache.cacheSourceTransaction(branch, sourceTransaction);
         }
      }
   }

   private void loadBranches(BranchCache cache, Map<Branch, Integer> childToParent, Map<Branch, Integer> branchToBaseTx, Map<Branch, Integer> branchToSourceTx, Map<Branch, Integer> associatedArtifact) throws OseeCoreException {
      BranchFactory factory = getFactoryService().getBranchFactory();
      IOseeStatement chStmt = getDatabaseService().getStatement();
      try {
         chStmt.runPreparedQuery(2000, SELECT_BRANCHES);
         while (chStmt.next()) {
            try {
               int branchId = chStmt.getInt("branch_id");

               String branchName = chStmt.getString("branch_name");
               BranchState branchState = BranchState.getBranchState(chStmt.getInt("branch_state"));
               BranchType branchType = BranchType.getBranchType(chStmt.getInt("branch_type"));
               boolean isArchived = BranchArchivedState.valueOf(chStmt.getInt("archived")).isArchived();
               Branch branch = cache.getById(branchId);
               if (branch == null) {

                  branch =
                        factory.create(chStmt.getString("branch_guid"), branchName, branchType, branchState, isArchived);
                  branch.setId(branchId);
                  branch.setModificationType(ModificationType.MODIFIED);
                  branch.clearDirty();
                  cache.cache(branch);
               } else {
                  branch.setName(branchName);
                  branch.setArchived(isArchived);
                  branch.setBranchType(branchType);
                  branch.setBranchState(branchState);
                  branch.setModificationType(ModificationType.MODIFIED);
               }
               Integer parentBranchId = chStmt.getInt("parent_branch_id");
               if (parentBranchId != NULL_PARENT_BRANCH_ID) {
                  childToParent.put(branch, parentBranchId);
               }
               branchToSourceTx.put(branch, chStmt.getInt("parent_transaction_id"));
               branchToBaseTx.put(branch, chStmt.getInt("transaction_id"));
               associatedArtifact.put(branch, chStmt.getInt("associated_art_id"));
            } catch (OseeCoreException ex) {
               OseeLog.log(InternalBranchActivator.class, Level.SEVERE, ex);
            }
         }
      } finally {
         chStmt.close();
      }
   }

   private void loadBranchHierarchy(BranchCache branchCache, Map<Branch, Integer> childToParent) throws OseeCoreException {
      for (Entry<Branch, Integer> entry : childToParent.entrySet()) {
         Branch childBranch = entry.getKey();
         Branch parentBranch = branchCache.getById(entry.getValue());
         if (parentBranch == null) {
            throw new BranchDoesNotExist(String.format("Parent Branch id:[%s] does not exist for child branch [%s]",
                  entry.getValue(), entry.getKey()));
         }
         branchCache.setBranchParent(parentBranch, childBranch);

      }
   }

   private void loadMergeBranches(BranchCache branchCache) throws OseeCoreException {
      IOseeStatement chStmt = getDatabaseService().getStatement();
      try {
         chStmt.runPreparedQuery(1000, SELECT_MERGE_BRANCHES);
         while (chStmt.next()) {
            Branch sourceBranch = branchCache.getById(chStmt.getInt("source_branch_id"));
            Branch destBranch = branchCache.getById(chStmt.getInt("dest_branch_id"));
            Branch mergeBranch = branchCache.getById(chStmt.getInt("merge_branch_id"));
            branchCache.cacheMergeBranch(mergeBranch, sourceBranch, destBranch);
         }
      } finally {
         chStmt.close();
      }

   }

   private void loadBranchAliases(BranchCache branchCache) throws OseeCoreException {
      HashCollection<Integer, String> aliasMap = new HashCollection<Integer, String>();
      IOseeStatement chStmt = getDatabaseService().getStatement();
      try {
         chStmt.runPreparedQuery(SELECT_BRANCH_ALIASES);
         while (chStmt.next()) {
            int branchId = chStmt.getInt("mapped_branch_id");
            String alias = chStmt.getString("static_branch_name").toLowerCase();
            aliasMap.put(branchId, alias);
         }
      } finally {
         chStmt.close();
      }
      for (Integer branchId : aliasMap.keySet()) {
         Branch branch = branchCache.getById(branchId);
         Collection<String> aliases = aliasMap.getValues(branchId);
         if (aliases != null) {
            branch.setAliases(aliases.toArray(new String[aliases.size()]));
         }
      }
   }

   @Override
   public void store(Collection<Branch> branches) throws OseeCoreException {
      Operations.executeWork(new BranchStoreOperation(getDatabaseServiceProvider(), branches),
            new NullProgressMonitor(), -1);
   }

}
