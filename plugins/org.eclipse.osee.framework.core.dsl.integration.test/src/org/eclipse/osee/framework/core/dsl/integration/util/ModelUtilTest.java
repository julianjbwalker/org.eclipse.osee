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
package org.eclipse.osee.framework.core.dsl.integration.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.eclipse.osee.framework.core.dsl.integration.mocks.DslAsserts;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AccessContext;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AccessPermissionEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ArtifactTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AttributeTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.CompareOp;
import org.eclipse.osee.framework.core.dsl.oseeDsl.HierarchyRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.MatchField;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ObjectRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDsl;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationMultiplicityEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.RelationTypeRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactMatcher;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XAttributeType;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationSideEnum;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XRelationType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test Case for {@link ModelUtil}
 * 
 * @author Roberto E. Escobar
 */
public class ModelUtilTest {

   private static final String TYPE_TEST_INPUT = "testTypeModel.osee";
   private static final String ACCESS_TEST_INPUT = "testAccessModel.osee";

   @Test
   public void testModelUtilLoadType() throws OseeCoreException, IOException {
      String rawXTextData = Lib.fileToString(getClass(), TYPE_TEST_INPUT);

      OseeDsl model1 = ModelUtil.loadModel("osee:/text.osee", rawXTextData);

      Assert.assertEquals(5, model1.getArtifactTypes().size());
      Iterator<XArtifactType> type1 = model1.getArtifactTypes().iterator();
      // @formatter:off
      DslAsserts.assertEquals(type1.next(), "Artifact", "AAMFDh6S7gRLupAMwywA", new String[0], "Name", "Annotation");
      DslAsserts.assertEquals(type1.next(), "Requirement", "BOm4NmAq+HC1O2hkMagA", new String[] {"Artifact"}, "WordML");
      DslAsserts.assertEquals(type1.next(), "Software Requirement", "BOm+AIG2snNbAM5FFcwA", new String[] {"Requirement"});
      DslAsserts.assertEquals(type1.next(), "System Requirement", "BOnAaYTBOG68_Tw5Y_AA", new String[] {"Requirement"});
      DslAsserts.assertEquals(type1.next(), "SubSystem Requirement", "BOnBhJ1XAFGKcrku3LgA", new String[] {"Requirement"});
      // @formatter:on

      Assert.assertEquals(3, model1.getAttributeTypes().size());
      Iterator<XAttributeType> type2 = model1.getAttributeTypes().iterator();
      DslAsserts.assertEquals(type2.next(), "Name", "AAMFEcF1AzV7PKuHmxwA", "StringAttribute",
         "DefaultAttributeDataProvider", "1", "1", "DefaultAttributeTaggerProvider", //
         "Descriptive Name", "unnamed", null);
      DslAsserts.assertEquals(type2.next(), "Annotation", "AAMFEcWy0xc4e3tcemQA", "CompressedContentAttribute",
         "UriAttributeDataProvider", "0", "unlimited", "DefaultAttributeTaggerProvider", //
         "the version \'1.0\' is this \"1.2.0\"", null, null);
      DslAsserts.assertEquals(type2.next(), "WordML", "AAMFEcfcGS2V3SqQN2wA", "WordAttribute",
         "UriAttributeDataProvider", "0", "1", "XmlAttributeTaggerProvider",
         "value must comply with WordML xml schema",
         "<w:p xmlns:w=\"http://schemas.microsoft.com/office/word/2003/wordml\"><w:r><w:t></w:t></w:r></w:p>", "xml");

      Assert.assertEquals(1, model1.getRelationTypes().size());
      Iterator<XRelationType> type3 = model1.getRelationTypes().iterator();
      DslAsserts.assertEquals(type3.next(), "Requirement Relation", "BOkzi3U9VTe2fcUz_9gA", "requirement-sideA",
         "Requirement", "BOm4NmAq+HC1O2hkMagA", "subsystem-sideB", "SubSystem Requirement", "BOnBhJ1XAFGKcrku3LgA",
         "Lexicographical_Ascending", RelationMultiplicityEnum.ONE_TO_MANY);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ModelUtil.saveModel(model1, "osee:/text.osee", outputStream, false);
      outputStream.flush();
      String value = outputStream.toString("UTF-8");
      Assert.assertEquals(rawXTextData, value);

      OseeDsl model2 = ModelUtil.loadModel("osee:/text2.osee", value);
      DslAsserts.assertEquals(model1, model2);
   }

   @Test
   public void testModelUtilLoadAccess() throws OseeCoreException, IOException {
      String rawXTextData = Lib.fileToString(getClass(), ACCESS_TEST_INPUT);

      OseeDsl model1 = ModelUtil.loadModel("osee:/text.osee", rawXTextData);
      Assert.assertEquals(2, model1.getArtifactTypes().size());
      Iterator<XArtifactType> type1 = model1.getArtifactTypes().iterator();
      DslAsserts.assertEquals(type1.next(), "Artifact", "BZOUrxO35x+LBZkEYzAA", new String[0]);
      DslAsserts.assertEquals(type1.next(), "Software Requirement", "BZO4PuM+Oz58hpAcTGwA", new String[] {"Artifact"});

      Assert.assertEquals(1, model1.getAttributeTypes().size());
      Iterator<XAttributeType> type2 = model1.getAttributeTypes().iterator();
      DslAsserts.assertEquals(type2.next(), "Qualification Method", "BZPB25t9fRVnLuII6+wA", "StringAttribute",
         "DefaultAttributeDataProvider", "0", "1", null, null, "test", null);

      Assert.assertEquals(1, model1.getRelationTypes().size());
      Iterator<XRelationType> type3 = model1.getRelationTypes().iterator();
      DslAsserts.assertEquals(type3.next(), "Requirement Relation", "BOkzi3U9VTe2fcUz_9gA", "requirement-sideA",
         "Software Requirement", "BZO4PuM+Oz58hpAcTGwA", "artifact-sideB", "Artifact", "BZOUrxO35x+LBZkEYzAA",
         "Lexicographical_Ascending", RelationMultiplicityEnum.ONE_TO_MANY);

      Assert.assertEquals(3, model1.getArtifactMatchRefs().size());
      Iterator<XArtifactMatcher> type4 = model1.getArtifactMatchRefs().iterator();
      // @formatter:off
      XArtifactMatcher matcher = type4.next();
      DslAsserts.assertEquals(matcher, "Software Items");
      DslAsserts.assertEquals(matcher.getConditions().get(0), MatchField.ARTIFACT_GUID, CompareOp.EQ, "AAMFEcWy0xc4e3tcem99");
      matcher = type4.next();
      DslAsserts.assertEquals(matcher, "Systems");
      DslAsserts.assertEquals(matcher.getConditions().get(0), MatchField.BRANCH_NAME, CompareOp.LIKE, "\\w+");
      matcher = type4.next();
      DslAsserts.assertEquals(matcher, "SubSystems");
      DslAsserts.assertEquals(matcher.getConditions().get(0), MatchField.ARTIFACT_NAME, CompareOp.EQ, "xx");
      // @formatter:on

      Assert.assertEquals(2, model1.getAccessDeclarations().size());
      Iterator<AccessContext> type5 = model1.getAccessDeclarations().iterator();
      AccessContext context1 = type5.next();
      DslAsserts.assertEquals(context1, "System Context", "AAMFEcWy0xc4e3tcem11", new String[0]);
      List<ObjectRestriction> restrictions1 = context1.getAccessRules();
      Assert.assertEquals(1, restrictions1.size());
      DslAsserts.assertEquals(((ArtifactTypeRestriction) restrictions1.iterator().next()), AccessPermissionEnum.DENY,
         "Artifact");

      List<HierarchyRestriction> hierarchy1 = context1.getHierarchyRestrictions();
      Assert.assertEquals(3, hierarchy1.size());

      Iterator<HierarchyRestriction> heirar = hierarchy1.iterator();
      DslAsserts.assertEquals(heirar.next(), "Software Items");
      DslAsserts.assertEquals(heirar.next(), "Systems");
      DslAsserts.assertEquals(heirar.next(), "SubSystems");

      AccessContext context2 = type5.next();
      DslAsserts.assertEquals(context2, "subsystem.requirement.writer", "AAMFEcWy0xc4e3tcem22",
         new String[] {"System Context"});
      List<ObjectRestriction> restrictions2 = context2.getAccessRules();
      Assert.assertEquals(4, restrictions2.size());
      Iterator<ObjectRestriction> restIt = restrictions2.iterator();
      // @formatter:off
      DslAsserts.assertEquals(((AttributeTypeRestriction) restIt.next()), AccessPermissionEnum.DENY, "Qualification Method", "Software Requirement");
      DslAsserts.assertEquals(((AttributeTypeRestriction) restIt.next()), AccessPermissionEnum.ALLOW, "Qualification Method", "Software Requirement");
      DslAsserts.assertEquals(((AttributeTypeRestriction) restIt.next()), AccessPermissionEnum.ALLOW, "Qualification Method", "Software Requirement");
      DslAsserts.assertEquals(((RelationTypeRestriction) restIt.next()), AccessPermissionEnum.DENY, "Requirement Relation", XRelationSideEnum.SIDE_A);

      // @formatter:on

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ModelUtil.saveModel(model1, "osee:/text.osee", outputStream, false);
      outputStream.flush();
      String value = outputStream.toString("UTF-8");
      Assert.assertEquals(rawXTextData, value);

      OseeDsl model2 = ModelUtil.loadModel("osee:/text2.osee", value);
      DslAsserts.assertEquals(model1, model2);
   }
}
