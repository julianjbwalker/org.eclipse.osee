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
package org.eclipse.osee.ats.client.integration.tests.ats.core.client.action;

import java.util.List;
import org.junit.Assert;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsWorkDefinition;
import org.eclipse.osee.ats.artifact.GoalManager;
import org.eclipse.osee.ats.client.integration.tests.AtsClientService;
import org.eclipse.osee.ats.client.integration.tests.ats.core.client.AtsTestUtil;
import org.eclipse.osee.ats.core.client.action.ActionManager;
import org.eclipse.osee.ats.core.client.artifact.GoalArtifact;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.artifact.search.QueryOptions;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test for {@link ActionManager}
 * 
 * @author Mark Joy
 */
public class AutoAddActionToGoalTest {

   @BeforeClass
   @AfterClass
   public static void cleanup() throws Exception {
      AtsTestUtil.cleanup();

      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranchToken(), "AutoAddActionToGoalTest - cleanup");
      for (Artifact art : ArtifactQuery.getArtifactListFromName("AutoAddActionToGoalTest", AtsUtil.getAtsBranchToken(),
         DeletionFlag.EXCLUDE_DELETED, QueryOptions.CONTAINS_MATCH_OPTIONS)) {
         art.deleteAndPersist(transaction);
      }
      transaction.execute();
   }

   // Test that no action is added to the Goal
   @org.junit.Test
   public void testNoActionAddedToGoal() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("AutoAddActionToGoalTest - AutoAddActionToGoalTest - NoAdd");

      GoalArtifact goalArt = GoalManager.createGoal("AutoAddActionToGoalTest - NoActionAddedGoal");
      List<Artifact> memArt = goalArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member);
      Assert.assertEquals("Goal should have no memebers", 0, memArt.size());
      ArtifactCache.deCache(goalArt);
   }

   // Create a goal with a relation to an AI and add an Action.  The 
   // Team Workflow should be added to the Goal
   @org.junit.Test
   public void testAutoAddActionToGoal_AI() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("AutoAddActionToGoalTest - AddActionWithAI");

      IAtsWorkDefinition workDef = AtsTestUtil.getWorkDef();
      GoalArtifact goalArt = GoalManager.createGoal("AutoAddActionToGoalTest - AddActionToGoalFromAI");
      IAtsActionableItem actionItem2 = AtsTestUtil.getTestAi2();

      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranchToken(), getClass().getSimpleName());
      AtsClientService.get().storeConfigObject(actionItem2, transaction);
      IAtsTeamDefinition teamDef = AtsTestUtil.getTestTeamDef();
      teamDef.getVersions().clear();
      AtsClientService.get().storeConfigObject(teamDef, transaction);
      transaction.execute();

      Artifact testAI2 = AtsClientService.get().getArtifact(AtsTestUtil.getTestAi2());

      goalArt.addRelation(AtsRelationTypes.AutoAddActionToGoal_ConfigObject, testAI2);

      AtsClientService.get().getWorkDefinitionAdmin().addWorkDefinition(workDef);

      TeamWorkFlowArtifact teamWf2 = AtsTestUtil.getTeamWf2();

      List<Artifact> memArt = goalArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member);
      Assert.assertEquals("Goal should have one memeber from AI", 1, memArt.size());
      Assert.assertTrue("Team Workflow with AI not part of Goal",
         goalArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member).contains(teamWf2));

      AtsTestUtil.cleanup();
      testAI2.deleteAndPersist();
      ArtifactCache.deCache(goalArt);
   }

   // Create a goal with a relation to a Team Definition and add an Action.  The 
   // Team Workflow should be added to the Goal
   @org.junit.Test
   public void testAutoAddActionToGoal_TeamWF() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("AutoAddActionToGoalTest - AddActionWithTeamDef");

      GoalArtifact goalArt = GoalManager.createGoal("AutoAddActionToGoalTest - AddActionToGoalFromTeamDef");

      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranchToken(), getClass().getSimpleName());
      IAtsTeamDefinition teamDef = AtsTestUtil.getTestTeamDef();
      teamDef.getVersions().clear();
      AtsClientService.get().storeConfigObject(teamDef, transaction);
      transaction.execute();

      Artifact teamDefArtifact = AtsClientService.get().getArtifact(AtsTestUtil.getTestTeamDef());

      goalArt.addRelation(AtsRelationTypes.AutoAddActionToGoal_ConfigObject, teamDefArtifact);

      TeamWorkFlowArtifact teamWf2 = AtsTestUtil.getTeamWf2();
      List<Artifact> memArt = goalArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member);
      Assert.assertEquals("Goal should have one memeber from Team Definition", 1, memArt.size());
      Assert.assertTrue("Team Workflow with TeamDef not part of Goal",
         goalArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member).contains(teamWf2));

      AtsTestUtil.cleanup();
      ArtifactCache.deCache(goalArt);
   }

   // Create a Goal with a relation to an AI and Team Definition.  The action should
   // only be added to the goal once.
   @org.junit.Test
   public void testAutoAddActionToGoal_AIandTeamWF() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("AutoAddActionToGoalTest - AddActionWithAIandTeamDef");

      IAtsWorkDefinition workDef = AtsTestUtil.getWorkDef();
      GoalArtifact goalArt = GoalManager.createGoal("AutoAddActionToGoalTest - AddActionToGoalFromAIorTeamDef");
      IAtsActionableItem actionItem = AtsTestUtil.getTestAi2();

      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranchToken(), getClass().getSimpleName());
      AtsClientService.get().storeConfigObject(actionItem, transaction);
      IAtsTeamDefinition teamDef = AtsTestUtil.getTestTeamDef();
      teamDef.getVersions().clear();
      AtsClientService.get().storeConfigObject(teamDef, transaction);
      transaction.execute();

      Artifact testAI2 = AtsClientService.get().getArtifact(AtsTestUtil.getTestAi2());
      Artifact teamDefArtifact = AtsClientService.get().getArtifact(AtsTestUtil.getTestTeamDef());

      goalArt.addRelation(AtsRelationTypes.AutoAddActionToGoal_ConfigObject, testAI2);
      goalArt.addRelation(AtsRelationTypes.AutoAddActionToGoal_ConfigObject, teamDefArtifact);

      AtsClientService.get().getWorkDefinitionAdmin().addWorkDefinition(workDef);

      TeamWorkFlowArtifact teamWf2 = AtsTestUtil.getTeamWf2();
      List<Artifact> memArt = goalArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member);
      Assert.assertEquals("Goal should only have one memeber", 1, memArt.size());
      Assert.assertTrue("Team Workflow with AI and TeamDef not part of Goal",
         goalArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member).contains(teamWf2));

      AtsTestUtil.cleanup();
      testAI2.deleteAndPersist();
      ArtifactCache.deCache(goalArt);
   }

   // Create two Goals.  Goal one has relation to two different AIs.  Goal two has a relation to
   // one of those AIs.  Create two Actions and test Goal one has two members and Goal two has
   // only one member.
   @org.junit.Test
   public void testAutoAddActionToGoal_TwoAIsTwoGoals() throws OseeCoreException {
      AtsTestUtil.cleanupAndReset("DecisionReviewManagerTest - UnAssigned");

      IAtsWorkDefinition workDef = AtsTestUtil.getWorkDef();
      GoalArtifact goalArt = GoalManager.createGoal("AutoAddActionToGoalTest - AddTwoActions");
      GoalArtifact goalArt2 = GoalManager.createGoal("AutoAddActionToGoalTest - SecondGoal");
      IAtsActionableItem actionItem2 = AtsTestUtil.getTestAi2();
      IAtsActionableItem actionItem3 = AtsTestUtil.getTestAi3();

      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranchToken(), getClass().getSimpleName());
      AtsClientService.get().storeConfigObject(actionItem2, transaction);
      AtsClientService.get().storeConfigObject(actionItem3, transaction);
      IAtsTeamDefinition teamDef = AtsTestUtil.getTestTeamDef();
      teamDef.getVersions().clear();
      AtsClientService.get().storeConfigObject(teamDef, transaction);
      transaction.execute();

      Artifact testAI2 = AtsClientService.get().getArtifact(AtsTestUtil.getTestAi2());
      Artifact testAI3 = AtsClientService.get().getArtifact(AtsTestUtil.getTestAi3());

      goalArt.addRelation(AtsRelationTypes.AutoAddActionToGoal_ConfigObject, testAI2);
      goalArt.addRelation(AtsRelationTypes.AutoAddActionToGoal_ConfigObject, testAI3);
      goalArt2.addRelation(AtsRelationTypes.AutoAddActionToGoal_ConfigObject, testAI2);

      AtsClientService.get().getWorkDefinitionAdmin().addWorkDefinition(workDef);

      TeamWorkFlowArtifact teamWf2 = AtsTestUtil.getTeamWf2();
      TeamWorkFlowArtifact teamWf3 = AtsTestUtil.getTeamWf3();
      List<Artifact> memArt = goalArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member);
      Assert.assertEquals("Goal should have two memebers", 2, memArt.size());
      Assert.assertTrue("Team Workflow with two memebers of Goal - TeamWf",
         goalArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member).contains(teamWf2));
      Assert.assertTrue("Team Workflow with two memebers of Goal - TeamWf2",
         goalArt.getRelatedArtifacts(AtsRelationTypes.Goal_Member).contains(teamWf3));
      List<Artifact> memArt2 = goalArt2.getRelatedArtifacts(AtsRelationTypes.Goal_Member);
      Assert.assertEquals("Goal2 should only have one memeber", 1, memArt2.size());
      Assert.assertTrue("Team Workflow with AI part of Goal2",
         goalArt2.getRelatedArtifacts(AtsRelationTypes.Goal_Member).contains(teamWf2));

      AtsTestUtil.cleanup();
      testAI2.deleteAndPersist();
      testAI3.deleteAndPersist();
      ArtifactCache.deCache(goalArt);
      ArtifactCache.deCache(goalArt2);
   }

}
