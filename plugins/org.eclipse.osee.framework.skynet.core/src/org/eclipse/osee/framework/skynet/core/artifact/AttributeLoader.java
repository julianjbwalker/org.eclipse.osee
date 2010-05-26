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
package org.eclipse.osee.framework.skynet.core.artifact;

import static org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoad.SHALLOW;
import static org.eclipse.osee.framework.skynet.core.artifact.ArtifactLoad.RELATION;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.client.ClientSessionManager;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.AttributeType;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.database.core.IOseeStatement;
import org.eclipse.osee.framework.database.core.OseeSql;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.attribute.BooleanAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.EnumeratedAttribute;

/**
 * @author Ryan Schmitt
 */
public class AttributeLoader {
   private static String getSql(boolean historical, boolean allowDeletedArtifacts, ArtifactLoad loadLevel) throws OseeCoreException {
      OseeSql sqlKey;
      if (historical) {
         sqlKey = OseeSql.LOAD_HISTORICAL_ATTRIBUTES;
      } else if (loadLevel == ArtifactLoad.ALL_CURRENT) {
         sqlKey = OseeSql.LOAD_ALL_CURRENT_ATTRIBUTES;
      } else if (allowDeletedArtifacts) {
         sqlKey = OseeSql.LOAD_CURRENT_ATTRIBUTES_WITH_DELETED;
      } else {
         sqlKey = OseeSql.LOAD_CURRENT_ATTRIBUTES;
      }

      String sql = ClientSessionManager.getSql(sqlKey);
      return sql;
   }

   private static final class AttrData {
      public int artifactId = -1;
      public int branchId = -1;
      public int attrId = -1;
      public int gammaId = -1;
      public int modType = -1;

      public AttrData() {
      }

      public AttrData(int _artifactId, int _branchId, int _attrId, int _gammaId, int _modType) {
         artifactId = _artifactId;
         branchId = _branchId;
         attrId = _attrId;
         gammaId = _gammaId;
         modType = _modType;
      }

      public static boolean isDifferentArtifact(AttrData previous, AttrData current) {
         return (current.branchId != previous.branchId || current.artifactId != previous.artifactId);
      }

      public static boolean multipleVersionsExist(AttrData current, AttrData previous) {
         return (current.attrId == previous.attrId && current.branchId == previous.branchId && current.artifactId == previous.artifactId);
      }
   }

   static void loadAttributeData(int queryId, Collection<Artifact> artifacts, boolean historical, boolean allowDeletedArtifacts, ArtifactLoad loadLevel) throws OseeCoreException {
      if (loadLevel == SHALLOW || loadLevel == RELATION) {
         return;
      }

      IOseeStatement chStmt = ConnectionHandler.getStatement();
      try {
         String sql = getSql(historical, allowDeletedArtifacts, loadLevel);
         chStmt.runPreparedQuery(artifacts.size() * 8, sql, queryId);

         Artifact artifact = null;
         AttrData previous = new AttrData();

         List<Integer> transactionNumbers = new ArrayList<Integer>();

         while (chStmt.next()) {
            AttrData current =
                  new AttrData(chStmt.getInt("art_id"), chStmt.getInt("branch_id"), chStmt.getInt("attr_id"),
                        chStmt.getInt("gamma_id"), chStmt.getInt("mod_type"));

            if (AttrData.isDifferentArtifact(previous, current)) {
               finishSetupOfPreviousArtifact(artifact, transactionNumbers);

               if (historical) {
                  artifact = ArtifactCache.getHistorical(current.artifactId, chStmt.getInt("stripe_transaction_id"));
               } else {
                  artifact = ArtifactCache.getActive(current.artifactId, current.branchId);
               }
               if (artifact == null) {
                  //TODO just masking a DB issue, we should probably really have an error here - throw new ArtifactDoesNotExist("Can not find aritfactId: " + artifactId + " on branch " + branchId);
                  OseeLog.log(ArtifactLoader.class, Level.WARNING, String.format(
                        "Orphaned attribute for artifact id[%d] branch[%d]", current.artifactId, current.branchId));
               } else if (artifact.isAttributesLoaded()) {
                  artifact = null;
               }
            }

            if (AttrData.multipleVersionsExist(current, previous)) {
               if (historical) {
                  // Okay to skip on historical loading... because the most recent transaction is used first due to sorting on the query
               } else {
                  OseeLog.log(
                        ArtifactLoader.class,
                        Level.WARNING,
                        String.format(
                              "multiple attribute version for attribute id [%d] artifact id[%d] branch[%d] previousGammaId[%s] currentGammaId[%s] previousModType[%s] currentModType[%s]",
                              current.attrId, current.artifactId, current.branchId, previous.gammaId, current.gammaId,
                              previous.modType, current.modType));
               }
            } else if (artifact != null) { //artifact will have been set to null if artifact.isAttributesLoaded() returned true
               AttributeType attributeType = AttributeTypeManager.getType(chStmt.getInt("attr_type_id"));
               boolean isBooleanAttribute =
                     AttributeTypeManager.isBaseTypeCompatible(BooleanAttribute.class, attributeType);
               boolean isEnumAttribute =
                     AttributeTypeManager.isBaseTypeCompatible(EnumeratedAttribute.class, attributeType);
               String value = chStmt.getString("value");
               if (isBooleanAttribute || isEnumAttribute) {
                  value = Strings.intern(value);
               }
               artifact.internalInitializeAttribute(attributeType, current.attrId, current.gammaId,
                     ModificationType.getMod(current.modType), false, value, chStmt.getString("uri"));
               transactionNumbers.add(chStmt.getInt("transaction_id"));
            }

            previous = current;
         }
         finishSetupOfPreviousArtifact(artifact, transactionNumbers);
      } finally {
         chStmt.close();
      }
   }

   static void finishSetupOfPreviousArtifact(Artifact artifact, List<Integer> transactionNumbers) throws OseeCoreException {
      if (artifact != null) { // exclude the first pass because there is no previous artifact
         setLastAttributePersistTransaction(artifact, transactionNumbers);
         transactionNumbers.clear();
         artifact.meetMinimumAttributeCounts(false);
         ArtifactCache.cachePostAttributeLoad(artifact);
      }
   }

   private static void setLastAttributePersistTransaction(Artifact artifact, List<Integer> transactionNumbers) {
      int maxTransactionId = Integer.MIN_VALUE;
      for (Integer transactionId : transactionNumbers) {
         maxTransactionId = Math.max(maxTransactionId, transactionId);
      }
      artifact.setTransactionId(maxTransactionId);
   }
}
