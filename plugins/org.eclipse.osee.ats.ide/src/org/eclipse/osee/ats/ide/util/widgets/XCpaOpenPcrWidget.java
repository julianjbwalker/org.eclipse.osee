/*********************************************************************
 * Copyright (c) 2014 Boeing
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

import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.core.cpa.CpaFactory;
import org.eclipse.osee.ats.ide.internal.AtsApiService;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.ArtifactWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.XHyperlinkLabel;

/**
 * @author Donald G. Dunne
 */
public class XCpaOpenPcrWidget extends XHyperlinkLabel implements ArtifactWidget {

   public static final String WIDGET_ID = XCpaOpenPcrWidget.class.getSimpleName();
   private Artifact artifact;
   private final AttributeTypeToken pcrIdAttr;

   public XCpaOpenPcrWidget(AttributeTypeToken pcrIdAttr) {
      super("open", "", false);
      this.pcrIdAttr = pcrIdAttr;
   }

   protected String getCpaBasepath() {
      return AtsApiService.get().getConfigValue(CpaFactory.CPA_BASEPATH_KEY);
   }

   @Override
   public String getUrl() {
      String url = null;
      String orgPcrId = artifact.getSoleAttributeValueAsString(pcrIdAttr, null);
      String pcrTool = artifact.getSoleAttributeValue(AtsAttributeTypes.PcrToolId, null);
      if (Strings.isValid(orgPcrId) && Strings.isValid(pcrTool)) {
         url = String.format("%s/ats/cpa/decision/%s?pcrSystem=%s", getCpaBasepath(), orgPcrId, pcrTool);
      }
      return url;
   }

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   @Override
   public void saveToArtifact() {
      // do nothing
   }

   @Override
   public void revert() {
      // do nothing
   }

   @Override
   public Result isDirty() {
      return Result.FalseResult;
   }

   @Override
   public void setArtifact(Artifact artifact) {
      this.artifact = artifact;
   }

}
