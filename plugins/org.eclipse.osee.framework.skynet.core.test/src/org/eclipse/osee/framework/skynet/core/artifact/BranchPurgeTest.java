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
package org.eclipse.osee.framework.skynet.core.artifact;

import static org.junit.Assert.assertFalse;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.SystemUser;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.operation.NullOperationLogger;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.database.operation.PurgeUnusedBackingDataAndTransactions;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.httpRequests.PurgeBranchHttpRequestOperation;
import org.eclipse.osee.framework.skynet.core.mocks.DbTestUtil;
import org.eclipse.osee.framework.skynet.core.util.FrameworkTestUtil;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.support.test.util.DemoSawBuilds;
import org.eclipse.osee.support.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;

/**
 * This test is intended to be run against a demo database. It tests the branch purge logic by counting the rows of the
 * version and txs tables, creating a branch, making changes and then purging the branch. If it works properly, all rows
 * should be equal.
 * 
 * @author Donald G. Dunne
 */
public class BranchPurgeTest {

   private final Map<String, Integer> preCreateCount = new HashMap<String, Integer>();
   private final Map<String, Integer> postCreateBranchCount = new HashMap<String, Integer>();
   private final Map<String, Integer> postPurgeCount = new HashMap<String, Integer>();
   List<String> tables = Arrays.asList("osee_attribute", "osee_artifact", "osee_relation_link", "osee_tx_details",
      "osee_txs");

   @Before
   public void setUp() throws Exception {
      // This test should only be run on test db
      assertFalse(TestUtil.isProductionDb());
      cleanup();
   }

   @org.junit.Test
   public void testPurgeBranch() throws Exception {
      Operations.executeWorkAndCheckStatus(new PurgeUnusedBackingDataAndTransactions(NullOperationLogger.getSingleton()));

      // Count rows in tables prior to purge
      DbTestUtil.getTableRowCounts(preCreateCount, tables);

      // create a new working branch
      Branch branch =
         BranchManager.createWorkingBranch(DemoSawBuilds.SAW_Bld_2, getClass().getSimpleName(),
            UserManager.getUser(SystemUser.OseeSystem));

      // create some software artifacts
      Collection<Artifact> softArts =
         FrameworkTestUtil.createSimpleArtifacts(CoreArtifactTypes.SoftwareRequirement, 10, getClass().getSimpleName(),
            branch);
      Artifacts.persistInTransaction("Test purge branch", softArts);

      // make more changes to artifacts
      for (Artifact softArt : softArts) {
         softArt.addAttribute(CoreAttributeTypes.StaticId, getClass().getSimpleName());
         softArt.persist(getClass().getSimpleName());
      }

      // Count rows and check that increased
      DbTestUtil.getTableRowCounts(postCreateBranchCount, tables);
      TestUtil.checkThatIncreased(preCreateCount, postCreateBranchCount);

      Operations.executeWorkAndCheckStatus(new PurgeBranchHttpRequestOperation(branch, false));

      TestUtil.sleep(4000);

      Operations.executeWorkAndCheckStatus(new PurgeUnusedBackingDataAndTransactions(NullOperationLogger.getSingleton()));
      // Count rows and check that same as when began
      DbTestUtil.getTableRowCounts(postPurgeCount, tables);
      // TODO looks like artifacts are not being removed when purge a branch
      TestUtil.checkThatEqual(preCreateCount, postPurgeCount);
   }

   @After
   public void testCleanupPost() throws Exception {
      cleanup();
   }

   private static void cleanup() throws Exception {
      FrameworkTestUtil.purgeWorkingBranches(Arrays.asList(BranchPurgeTest.class.getSimpleName()));
   }
}
