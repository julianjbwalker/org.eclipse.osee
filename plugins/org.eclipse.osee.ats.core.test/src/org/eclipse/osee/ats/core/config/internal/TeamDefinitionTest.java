/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.config.internal;

import junit.framework.Assert;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.junit.Test;

/**
 * Test case for {@link TeamDefinition}
 * 
 * @author Donald G. Dunne
 */
public class TeamDefinitionTest {

   @Test
   public void testGetTeamDefinitionHoldingVersions() throws OseeCoreException {
      TeamDefinition teamDef1 = new TeamDefinition("Team Def 1", "td1guid", "ADSF");

      TeamDefinition teamDef1_child1 = new TeamDefinition("Team Def 1_1", "td1_1guid", "ADSF");
      teamDef1_child1.setParentTeamDef(teamDef1);

      Assert.assertNull(teamDef1.getTeamDefinitionHoldingVersions());
      Assert.assertNull(teamDef1_child1.getTeamDefinitionHoldingVersions());

      teamDef1.getVersions().add(new Version("ver1", "ver1guid", "ASDF"));

      Assert.assertEquals(teamDef1, teamDef1.getTeamDefinitionHoldingVersions());
      Assert.assertEquals(teamDef1, teamDef1_child1.getTeamDefinitionHoldingVersions());
   }

   @Test
   public void testIsTeamUsesVersions() throws OseeCoreException {
      TeamDefinition teamDef1 = new TeamDefinition("Team Def 1", "td1guid", "ADSF");

      TeamDefinition teamDef1_child1 = new TeamDefinition("Team Def 1_1", "td1_1guid", "ADSF");
      teamDef1_child1.setParentTeamDef(teamDef1);

      Assert.assertFalse(teamDef1.isTeamUsesVersions());
      Assert.assertFalse(teamDef1_child1.isTeamUsesVersions());

      teamDef1.getVersions().add(new Version("ver1", "ver1guid", "ASDF"));

      Assert.assertTrue(teamDef1.isTeamUsesVersions());
      Assert.assertTrue(teamDef1_child1.isTeamUsesVersions());
   }

}
