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
package org.eclipse.osee.framework.core.model.test.access;

import junit.framework.Assert;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.core.model.access.AccessData;
import org.eclipse.osee.framework.core.model.access.AccessDataQuery;
import org.eclipse.osee.framework.core.model.access.AccessDetail;
import org.eclipse.osee.framework.core.model.access.PermissionStatus;
import org.eclipse.osee.framework.core.model.test.mocks.MockArtifact;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.junit.Test;

/**
 * Test Case for {@link AccessDataQuery}
 *
 * @author Jeff C. Phillips
 */
public class AccessDataQueryTest {

   @Test
   public void testBranch() throws OseeCoreException {
      IOseeBranch branchToCheck = CoreBranches.SYSTEM_ROOT;

      TestObject testObject = getTestData();
      AccessData data = testObject.getAccessData();

      AccessDataQuery query = new AccessDataQuery(data);
      PermissionStatus status = new PermissionStatus();

      query.branchMatches(PermissionEnum.WRITE, branchToCheck, status);
      Assert.assertTrue(status.matches());

      query.branchMatches(PermissionEnum.FULLACCESS, branchToCheck, status);
      Assert.assertFalse(status.matches());
   }

   @Test
   public void testArtifactType() throws OseeCoreException {
      TestObject testObject = getTestData();
      AccessData data = testObject.getAccessData();
      IBasicArtifact<?> artifactToCheck = testObject.getArtifact();

      AccessDataQuery query = new AccessDataQuery(data);
      PermissionStatus status = new PermissionStatus();

      query.artifactTypeMatches(PermissionEnum.WRITE, artifactToCheck, status);
      Assert.assertTrue(status.matches());

      query.artifactTypeMatches(PermissionEnum.FULLACCESS, artifactToCheck, status);
      Assert.assertFalse(status.matches());
   }

   @Test
   public void testArtifact() throws OseeCoreException {
      TestObject testObject = getTestData();
      AccessData data = testObject.getAccessData();
      IBasicArtifact<?> artifactToCheck = testObject.getArtifact();

      AccessDataQuery query = new AccessDataQuery(data);
      PermissionStatus status = new PermissionStatus();

      query.artifactMatches(PermissionEnum.WRITE, artifactToCheck, status);
      Assert.assertTrue(status.matches());
   }

   @Test
   public void testAttributeType() throws OseeCoreException {
      TestObject testObject = getTestData();
      AccessData data = testObject.getAccessData();
      IBasicArtifact<?> artifactToCheck = testObject.getArtifact();

      IAttributeType attributeType = CoreAttributeTypes.PARAGRAPH_NUMBER;
      IAttributeType wordAttributeType = CoreAttributeTypes.WORD_TEMPLATE_CONTENT;

      data.add(artifactToCheck, new AccessDetail<IAttributeType>(attributeType, PermissionEnum.WRITE));
      data.add(artifactToCheck, new AccessDetail<IAttributeType>(wordAttributeType, PermissionEnum.READ));

      AccessDataQuery query = new AccessDataQuery(data);
      PermissionStatus status = new PermissionStatus();

      query.artifactMatches(PermissionEnum.WRITE, artifactToCheck, status);
      Assert.assertTrue(status.matches());

      query.attributeTypeMatches(PermissionEnum.READ, artifactToCheck, wordAttributeType, status);
      Assert.assertTrue(status.matches());

      query.attributeTypeMatches(PermissionEnum.WRITE, artifactToCheck, wordAttributeType, status);
      Assert.assertFalse(status.matches());

      query.artifactTypeMatches(PermissionEnum.WRITE, artifactToCheck, status);
      Assert.assertTrue(status.matches());

      query.artifactTypeMatches(PermissionEnum.FULLACCESS, artifactToCheck, status);
      Assert.assertFalse(status.matches());
   }

   private TestObject getTestData() throws OseeCoreException {
      IOseeBranch branchToCheck = CoreBranches.SYSTEM_ROOT;
      IArtifactType artifactType = CoreArtifactTypes.AbstractSoftwareRequirement;
      IBasicArtifact<?> artifactToCheck = new MockArtifact(GUID.create(), "Hello", branchToCheck, artifactType, 12);
      AccessData data = new AccessData();

      data.add(branchToCheck, new AccessDetail<IOseeBranch>(branchToCheck, PermissionEnum.WRITE));
      data.add(artifactToCheck, new AccessDetail<IBasicArtifact<?>>(artifactToCheck, PermissionEnum.WRITE));
      data.add(artifactToCheck, new AccessDetail<IArtifactType>(artifactType, PermissionEnum.WRITE));

      return new TestObject(artifactToCheck, data);
   }

   private class TestObject {
      final IBasicArtifact<?> artifact;
      final AccessData accessData;

      public TestObject(IBasicArtifact<?> artifact, AccessData accessData) {
         super();
         this.artifact = artifact;
         this.accessData = accessData;
      }

      public IBasicArtifact<?> getArtifact() {
         return artifact;
      }

      public AccessData getAccessData() {
         return accessData;
      }
   }
}
