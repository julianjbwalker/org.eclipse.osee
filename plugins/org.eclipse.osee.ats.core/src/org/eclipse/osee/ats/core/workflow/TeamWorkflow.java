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
package org.eclipse.osee.ats.core.workflow;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.ArtifactToken;
import org.eclipse.osee.framework.jdk.core.util.Conditions;
import org.eclipse.osee.logger.Log;

/**
 * @author Donald G. Dunne
 */
public class TeamWorkflow extends WorkItem implements IAtsTeamWorkflow {

   IAtsTeamDefinition teamDef = null;

   public TeamWorkflow(Log logger, AtsApi atsApi, ArtifactToken artifact) {
      super(logger, atsApi, artifact, AtsArtifactTypes.TeamWorkflow);
   }

   @Override
   public Set<IAtsActionableItem> getActionableItems() {
      Set<IAtsActionableItem> ais = new HashSet<>();
      Collection<ArtifactId> artIds =
         atsApi.getAttributeResolver().getArtifactIdReferences(artifact, AtsAttributeTypes.ActionableItemReference);
      for (ArtifactId artId : artIds) {
         IAtsActionableItem ai = atsApi.getQueryService().getConfigItem(artId);
         Conditions.assertNotNull(ai, "ai can not be null for artId %s", artId);
         ais.add(ai);
      }
      return ais;
   }

   @Override
   public IAtsTeamDefinition getTeamDefinition() {
      if (teamDef == null) {
         ArtifactId teamDefId = atsApi.getAttributeResolver().getSoleArtifactIdReference(artifact,
            AtsAttributeTypes.TeamDefinitionReference, ArtifactId.SENTINEL);
         if (teamDefId.isValid()) {
            teamDef = atsApi.getTeamDefinitionService().getTeamDefinitionById(teamDefId);
         }
      }
      return teamDef;
   }
}
