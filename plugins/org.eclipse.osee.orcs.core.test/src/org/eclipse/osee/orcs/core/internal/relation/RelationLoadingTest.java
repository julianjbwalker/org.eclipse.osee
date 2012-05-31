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
package org.eclipse.osee.orcs.core.internal.relation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.cache.RelationTypeCache;
import org.eclipse.osee.framework.core.model.mocks.MockOseeDataAccessor;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.orcs.core.ds.RelationData;
import org.eclipse.osee.orcs.core.internal.artifact.RelationContainer;
import org.eclipse.osee.orcs.data.HasLocalId;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrew M. Finkbeiner
 */
public class RelationLoadingTest {

   @Test
   public void testRelationCountMatches() throws OseeCoreException, IOException {
      RelationTypeCache cache = createAndPopulate();
      Map<Integer, RelationContainer> providersThatWillBeLoaded = getRelationProviderList(cache, 22);
      RelationRowMapper relationRowMapper = new RelationRowMapper(providersThatWillBeLoaded);

      loadRowData("data.csv", relationRowMapper);

      checkRelationCount(providersThatWillBeLoaded.get(1), RelationSide.SIDE_B, 9);
      checkRelationCount(providersThatWillBeLoaded.get(1), RelationSide.SIDE_A, 0);
      checkRelationCount(providersThatWillBeLoaded.get(2), RelationSide.SIDE_B, 0);
      checkRelationCount(providersThatWillBeLoaded.get(3), RelationSide.SIDE_B, 6);
      checkRelationCount(providersThatWillBeLoaded.get(3), RelationSide.SIDE_A, 1);
      checkRelationCount(providersThatWillBeLoaded.get(4), RelationSide.SIDE_B, 7);
   }

   //@formatter:off
   @Test
   public void testRelatedArtifactsMatch() throws OseeCoreException, IOException {
      RelationTypeCache cache = createAndPopulate();
      Map<Integer, RelationContainer> providersThatWillBeLoaded = getRelationProviderList(cache, 22);
      RelationRowMapper relationRowMapper = new RelationRowMapper(providersThatWillBeLoaded);

      loadRowData("data.csv", relationRowMapper);
      List<Integer> relatedArtifacts = new ArrayList<Integer>();
      checkRelatedArtifacts(relatedArtifacts, providersThatWillBeLoaded.get(1), RelationSide.SIDE_B, new int[]{2,3,4,5,6,7,8,9,10});
      checkRelatedArtifacts(relatedArtifacts, providersThatWillBeLoaded.get(1), RelationSide.SIDE_A, new int[]{});
      checkRelatedArtifacts(relatedArtifacts, providersThatWillBeLoaded.get(2), RelationSide.SIDE_B, new int[]{});
      checkRelatedArtifacts(relatedArtifacts, providersThatWillBeLoaded.get(3), RelationSide.SIDE_B, new int[]{11,12,13,14,15,16});
      checkRelatedArtifacts(relatedArtifacts, providersThatWillBeLoaded.get(3), RelationSide.SIDE_A, new int[]{1});
      checkRelatedArtifacts(relatedArtifacts, providersThatWillBeLoaded.get(4), RelationSide.SIDE_B, new int[]{17,18,19,20,21,22,2});
   }
   //@formatter:on

   public RelationTypeCache createAndPopulate() throws OseeCoreException {
      RelationTypeCache cache = new RelationTypeCache(new MockOseeDataAccessor<Long, RelationType>());
      cache.cache(new RelationType(1l, "test", "sideAName", "sideBName", CoreArtifactTypes.Artifact,
         CoreArtifactTypes.Artifact, RelationTypeMultiplicity.MANY_TO_MANY, ""));
      return cache;
   }

   private void checkRelationCount(RelationContainer relationContainer, RelationSide side, int size) {
      //      int count = relationContainer.getRelationCount(1, side);
      int count = relationContainer.getRelationCount(TokenFactory.createRelationTypeSide(side, 1, "blah"));
      Assert.assertEquals(
         String.format("We did not get the expected number of relations back [%d != %d]", size, count), size, count);
   }

   private void checkRelatedArtifacts(List<Integer> relatedArtifacts, RelationContainer relationContainer, RelationSide side, int[] expected) {
      relatedArtifacts.clear();
      relationContainer.getArtifactIds(relatedArtifacts, TokenFactory.createRelationTypeSide(side, 1, "blah"));
      Assert.assertTrue(String.format("Expected %d matches found %d", expected.length, relatedArtifacts.size()),
         expected.length == relatedArtifacts.size());
      for (int value : expected) {
         Assert.assertTrue(String.format("Expected relation to id[%d]", value), relatedArtifacts.contains(value));
      }
   }

   private Map<Integer, RelationContainer> getRelationProviderList(RelationTypeCache relationTypeCache, int size) {
      Map<Integer, RelationContainer> providersThatWillBeLoaded = new HashMap<Integer, RelationContainer>();
      for (int i = 1; i <= size; i++) {
         providersThatWillBeLoaded.put(i, createRelationContainer(relationTypeCache, i));
      }
      return providersThatWillBeLoaded;
   }

   private RelationContainer createRelationContainer(RelationTypeCache relationTypeCache, final int parentId) {
      return new RelationContainerImpl(new HasLocalId() {

         @Override
         public int getLocalId() {
            return parentId;
         }

      }, relationTypeCache);
   }

   private void loadRowData(String csvFile, RelationRowMapper relationRowMapper) throws IOException, OseeCoreException {
      URL url = RelationLoadingTest.class.getResource(csvFile);
      Assert.assertNotNull(url);

      List<RelationData> data = new ArrayList<RelationData>();
      RelationCsvReader csvReader = new RelationCsvReader(data);
      CsvReader reader = new CsvReader(url.openStream(), csvReader);
      reader.readFile();

      for (RelationData row : data) {
         relationRowMapper.onRow(row);
      }
   }

   public static class RelationCsvReader implements CsvRowHandler {

      private final List<RelationData> data;

      public RelationCsvReader(List<RelationData> data) {
         this.data = data;
      }

      @Override
      public void onRow(String... row) {
         //ArtIdA,ArtIdB,BranchId,GammaId,ModType,Rationale,RelationId,RelationTypeId
         if (row.length != 9) {
            Assert.assertTrue("Data file is not formatted correctly", false);
         }
         RelationData relationRow = new RelationData();
         relationRow.setParentId(Integer.parseInt(row[0]));
         relationRow.setArtIdA(Integer.parseInt(row[1]));
         relationRow.setArtIdB(Integer.parseInt(row[2]));
         relationRow.setBranchId(Integer.parseInt(row[3]));
         relationRow.setGammaId(Integer.parseInt(row[4]));
         relationRow.setModType(ModificationType.valueOf(row[5]));
         relationRow.setRationale(row[6]);
         relationRow.setRelationId(Integer.parseInt(row[7]));
         relationRow.setRelationTypeId(Integer.parseInt(row[8]));
         data.add(relationRow);
      }
   }
}
