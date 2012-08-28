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
package org.eclipse.osee.ats.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.core.client.util.AtsUsersClient;
import org.eclipse.osee.ats.core.config.AtsConfigCache;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.IDefaultInitialBranchesProvider;

/**
 * @author Robert A. Fisher
 */
public class TeamBasedDefaultBranchProvider implements IDefaultInitialBranchesProvider {

   @Override
   public Collection<Branch> getDefaultInitialBranches() throws OseeCoreException {
      IAtsUser user = AtsUsersClient.getUser();
      try {
         Collection<IAtsTeamDefinition> teams = new ArrayList<IAtsTeamDefinition>();
         for (Artifact art : AtsUsersClient.getOseeUser(user).getRelatedArtifacts(AtsRelationTypes.TeamMember_Team)) {
            teams.add(AtsConfigCache.instance.getSoleByGuid(art.getGuid(), IAtsTeamDefinition.class));
         }
         Collection<Branch> branches = new LinkedList<Branch>();

         Branch branch;
         for (IAtsTeamDefinition team : teams) {
            branch = BranchManager.getBranchByGuid(team.getTeamBranchGuid());
            if (branch != null) {
               branches.add(branch);
            }
         }

         return branches;
      } catch (Exception ex) {
         OseeLog.log(TeamBasedDefaultBranchProvider.class, Level.WARNING, ex);
      }

      return Collections.emptyList();
   }
}
