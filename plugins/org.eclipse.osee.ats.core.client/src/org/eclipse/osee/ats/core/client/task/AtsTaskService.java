/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.client.task;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.task.AbstractAtsTaskService;
import org.eclipse.osee.ats.api.task.AtsTaskEndpointApi;
import org.eclipse.osee.ats.api.task.JaxAtsTask;
import org.eclipse.osee.ats.api.task.JaxAtsTasks;
import org.eclipse.osee.ats.api.task.NewTaskData;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.core.client.IAtsClient;
import org.eclipse.osee.ats.core.client.internal.AtsClientService;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.model.event.DefaultBasicGuidArtifact;
import org.eclipse.osee.framework.core.model.event.DefaultBasicUuidRelation;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.ArtifactEvent;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.model.EventBasicGuidRelation;
import org.eclipse.osee.framework.skynet.core.event.model.EventModType;
import org.eclipse.osee.framework.skynet.core.relation.RelationEventType;
import org.eclipse.osee.framework.skynet.core.relation.RelationLink;

/**
 * @author Donald G. Dunne
 */
public class AtsTaskService extends AbstractAtsTaskService {

   private final IAtsClient atsClient;

   public AtsTaskService(IAtsClient atsClient) {
      this.atsClient = atsClient;
   }

   @Override
   public Collection<IAtsTask> createTasks(NewTaskData newTaskData) {
      AtsTaskEndpointApi taskEp = AtsClientService.getTaskEp();
      Response response = taskEp.create(newTaskData);

      Artifact teamWf = atsClient.getArtifact(newTaskData.getTeamWfUuid());

      JaxAtsTasks jaxTasks = response.readEntity(JaxAtsTasks.class);
      ArtifactEvent artifactEvent = new ArtifactEvent(AtsUtilCore.getAtsBranch());
      List<Long> artUuids = new LinkedList<>();
      for (JaxAtsTask task : jaxTasks.getTasks()) {
         String guid = ArtifactQuery.getGuidFromUuid(task.getUuid(), AtsUtilCore.getAtsBranch());
         artifactEvent.getArtifacts().add(new EventBasicGuidArtifact(EventModType.Added,
            AtsUtilCore.getAtsBranch().getUuid(), AtsArtifactTypes.Task.getGuid(), guid));
         artUuids.add(task.getUuid());

         RelationLink relation = getRelation(teamWf, task);
         Artifact taskArt = atsClient.getArtifact(task.getUuid());

         DefaultBasicUuidRelation guidRelation = new DefaultBasicUuidRelation(AtsUtilCore.getAtsBranch().getUuid(),
            AtsRelationTypes.TeamWfToTask_Task.getGuid(), relation.getId(), relation.getGammaId(),
            getBasicGuidArtifact(teamWf), getBasicGuidArtifact(taskArt));

         artifactEvent.getRelations().add(new EventBasicGuidRelation(RelationEventType.Added,
            newTaskData.getTeamWfUuid().intValue(), new Long(task.getUuid()).intValue(), guidRelation));
      }

      OseeEventManager.kickPersistEvent(getClass(), artifactEvent);

      List<IAtsTask> tasks = new LinkedList<>();
      for (Long uuid : artUuids) {
         tasks.add(AtsClientService.get().getWorkItemFactory().getTask(AtsClientService.get().getArtifact(uuid)));
      }
      return tasks;
   }

   public static DefaultBasicGuidArtifact getBasicGuidArtifact(Artifact artifact) {
      return new DefaultBasicGuidArtifact(artifact.getBranch().getUuid(), artifact.getArtTypeGuid(),
         artifact.getGuid());
   }

   private RelationLink getRelation(Artifact teamWf, JaxAtsTask task) {
      for (RelationLink relation : teamWf.getRelationsAll(DeletionFlag.EXCLUDE_DELETED)) {
         if (relation.getBArtifactId() == task.getUuid()) {
            return relation;
         }
      }
      return null;
   }

   @Override
   public Collection<IAtsTask> createTasks(IAtsTeamWorkflow teamWf, List<String> titles, List<IAtsUser> assignees, Date createdDate, IAtsUser createdBy, String relatedToState, String taskWorkDef, Map<String, List<String>> attributes, IAtsChangeSet changes) {
      throw new UnsupportedOperationException("Not Supported on Client");
   }

   @Override
   public Collection<IAtsTask> createTasks(NewTaskData newTaskData, IAtsChangeSet changes) {
      throw new UnsupportedOperationException("Not Supported on Client");
   }

}
