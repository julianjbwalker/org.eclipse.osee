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
package org.eclipse.osee.ats.api.agile;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Donald G. Dunne
 */
@XmlRootElement
public class NewAgileTeam {

   private String name;
   private String guid;
   private long uuid;
   private List<Long> atsTeamUuids = new ArrayList<Long>();
   private long backlogUuid = 0;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getGuid() {
      return guid;
   }

   public void setGuid(String guid) {
      this.guid = guid;
   }

   public long getUuid() {
      return uuid;
   }

   /**
    * Provided for deserialization. Setting will not apply to new artifact.
    */
   public void setUuid(long uuid) {
      this.uuid = uuid;
   }

   public List<Long> getAtsTeamUuids() {
      return atsTeamUuids;
   }

   public void setAtsTeamUuids(List<Long> atsTeamUuids) {
      this.atsTeamUuids = atsTeamUuids;
   }

   public long getBacklogUuid() {
      return backlogUuid;
   }

   public void setBacklogUuid(long backlogUuid) {
      this.backlogUuid = backlogUuid;
   }

}
