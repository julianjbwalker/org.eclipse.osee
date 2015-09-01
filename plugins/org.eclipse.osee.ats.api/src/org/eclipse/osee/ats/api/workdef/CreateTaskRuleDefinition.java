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
package org.eclipse.osee.ats.api.workdef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.ats.api.IAtsServices;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.util.IAtsChangeSet;
import org.eclipse.osee.ats.api.workflow.IAtsTask;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;

/**
 * @author Mark Joy
 */
public class CreateTaskRuleDefinition extends RuleDefinition implements IAtsCreateTaskRuleDefinition {

   private String taskWorkDef;
   private String relatedState;

   @Override
   public String getTaskWorkDef() {
      return taskWorkDef;
   }

   public void setTaskWorkDef(String taskWorkDef) {
      this.taskWorkDef = taskWorkDef;
   }

   @Override
   public String getRelatedState() {
      return relatedState;
   }

   public void setRelatedState(String relatedState) {
      this.relatedState = relatedState;
   }

   public void setRuleEvents(List<RuleEventType> ruleEvents) {
      this.ruleEvents = ruleEvents;
   }

   @Override
   public void execute(IAtsWorkItem workItem, IAtsServices atsServices, IAtsChangeSet changes, RunRuleResults ruleResults) {
      if (workItem.isTeamWorkflow()) {
         boolean createTask = true;
         for (IAtsTask task : atsServices.getWorkItemService().getTasks((IAtsTeamWorkflow) workItem)) {
            if (task.getName().equals(this.name)) {
               createTask = false;
               break;
            }
         }
         if (createTask) {
            // create the task
            Map<String, List<String>> attributes = new HashMap<>();
            List<String> desc = new ArrayList<>();
            desc.add(this.description);
            attributes.put("ats.Description", desc);
            List<String> titles = new ArrayList<>();
            titles.add(this.getTitle());

            Collection<IAtsTask> createdTasks = atsServices.getTaskService().createTasks((IAtsTeamWorkflow) workItem,
               titles, this.assignees, new Date(), atsServices.getUserService().getCurrentUser(), this.relatedState,
               this.taskWorkDef, attributes, "No Comment");
            ruleResults.addChange(workItem.getUuid(), RuleResultsEnum.CREATE_TASK);
            for (IAtsTask task : createdTasks) {
               ruleResults.addChange(task.getUuid(), RuleResultsEnum.NEW_TASK);
            }
         }
      }
   }

}
