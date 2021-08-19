/*********************************************************************
 * Copyright (c) 2021 Boeing
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
package org.eclipse.osee.ats.ide.util.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.eclipse.osee.ats.api.AtsApi;
import org.eclipse.osee.ats.api.agile.AgileEndpointApi;
import org.eclipse.osee.ats.api.agile.IAgileFeatureGroup;
import org.eclipse.osee.ats.api.agile.IAgileTeam;
import org.eclipse.osee.ats.api.agile.JaxAgileFeatureGroup;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.api.workflow.IAtsTeamWorkflow;
import org.eclipse.osee.ats.ide.agile.AgileFeatureGroupColumn;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.ats.ide.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.widgets.XHyperlinkLabelCmdValueSelection;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.FilteredCheckboxTreeDialog;

/**
 * @author Donald G. Dunne
 */
public class XAgileFeatureHyperlinkWidget extends XHyperlinkLabelCmdValueSelection {

   Collection<IAgileFeatureGroup> features = new HashSet<>();
   IAtsTeamWorkflow teamWf;
   public static final String WIDGET_ID = XAgileFeatureHyperlinkWidget.class.getSimpleName();
   AtsApi atsApi;
   IAtsTeamDefinition teamDef;

   public XAgileFeatureHyperlinkWidget() {
      super("Agile Feature", true, 50);
      atsApi = AtsApiService.get();
   }

   @Override
   public String getCurrentValue() {
      return Collections.toString(", ", features);
   }

   @Override
   public boolean handleSelection() {
      IAgileTeam agileTeam = null;
      if (teamWf != null) {
         agileTeam = atsApi.getAgileService().getAgileTeam(teamWf);
      } else if (teamDef != null) {
         agileTeam = atsApi.getAgileService().getAgileTeam(teamDef);
      } else {
         return false;
      }
      if (agileTeam == null) {
         AWorkbench.popup("No Agile Team configured for this ATS Team");
         return false;
      }
      AgileEndpointApi agileEp = atsApi.getServerEndpoints().getAgileEndpoint();

      List<AbstractWorkflowArtifact> awas = new ArrayList<>();
      if (teamWf != null) {
         awas.add((AbstractWorkflowArtifact) teamWf);
      }
      FilteredCheckboxTreeDialog<JaxAgileFeatureGroup> dialog =
         AgileFeatureGroupColumn.openSelectionDialog(agileEp, agileTeam.getId(), awas);

      if (dialog != null) {
         for (JaxAgileFeatureGroup grp : dialog.getChecked()) {
            for (IAgileFeatureGroup feature : atsApi.getAgileService().getAgileFeatureGroups(agileTeam)) {
               if (grp.getId().equals(feature.getId())) {
                  features.add(feature);
               }
            }
         }
      }
      return dialog != null;
   }

   @Override
   public boolean handleClear() {
      features.clear();
      return true;
   }

   public void setTeamWf(IAtsTeamWorkflow teamWf) {
      this.teamWf = teamWf;
   }

   public Collection<IAgileFeatureGroup> getFeatures() {
      return features;
   }

   public void setSelected(Collection<IAgileFeatureGroup> currFeatures) {
      features.clear();
      features.addAll(currFeatures);
   }

   public IAtsTeamDefinition getTeamDef() {
      return teamDef;
   }

   public void setTeamDef(IAtsTeamDefinition teamDef) {
      this.teamDef = teamDef;
   }

}
