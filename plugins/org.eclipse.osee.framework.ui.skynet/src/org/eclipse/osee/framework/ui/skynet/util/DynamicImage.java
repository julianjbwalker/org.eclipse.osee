/*********************************************************************
 * Copyright (c) 2015 Boeing
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

package org.eclipse.osee.framework.ui.skynet.util;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author Donald G. Dunne
 */
@XmlRootElement
public class DynamicImage {

   private String imageUrl;
   private String artifactTypeName;
   private String artifactTypeUuid;

   public String getImageUrl() {
      return imageUrl;
   }

   public void setImageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
   }

   public String getArtifactTypeName() {
      return artifactTypeName;
   }

   public void setArtifactTypeName(String artifactTypeName) {
      this.artifactTypeName = artifactTypeName;
   }

   public String getArtifactTypeUuid() {
      return artifactTypeUuid;
   }

   public void setArtifactTypeUuid(String artifactTypeUuid) {
      this.artifactTypeUuid = artifactTypeUuid;
   }

}
