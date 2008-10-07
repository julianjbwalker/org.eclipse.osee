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
package org.eclipse.osee.framework.db.connection.core;

import java.sql.Connection;
import java.util.HashMap;
import org.eclipse.osee.framework.db.connection.ConnectionHandler;
import org.eclipse.osee.framework.db.connection.OseeDbConnection;
import org.eclipse.osee.framework.db.connection.exception.OseeDataStoreException;
import org.eclipse.osee.framework.db.connection.pool.OseeConnection;

/**
 * @author Ryan D. Brooks
 */
public class SequenceManager {
   private static final String QUERY_SEQUENCE = "SELECT last_sequence FROM osee_sequence WHERE sequence_name = ?";
   private static final String INSERT_SEQUENCE =
         "INSERT INTO osee_sequence (last_sequence, sequence_name) VALUES (?,?)";
   private static final String UPDATE_SEQUENCE =
         "UPDATE osee_sequence SET last_sequence = ? WHERE sequence_name = ? AND last_sequence = ?";

   private HashMap<String, SequenceRange> sequences;

   public static final String ART_ID_SEQ = "SKYNET_ART_ID_SEQ";
   public static final String ART_TYPE_ID_SEQ = "SKYNET_ART_TYPE_ID_SEQ";
   public static final String ATTR_BASE_TYPE_ID_SEQ = "SKYNET_ATTR_BASE_TYPE_ID_SEQ";
   public static final String ATTR_PROVIDER_TYPE_ID_SEQ = "SKYNET_ATTR_PROVIDER_TYPE_ID_SEQ";
   public static final String ATTR_ID_SEQ = "SKYNET_ATTR_ID_SEQ";
   public static final String ATTR_TYPE_ID_SEQ = "SKYNET_ATTR_TYPE_ID_SEQ";
   public static final String FACTORY_ID_SEQ = "SKYNET_FACTORY_ID_SEQ";
   public static final String BRANCH_ID_SEQ = "SKYNET_BRANCH_ID_SEQ";
   public static final String REL_LINK_TYPE_ID_SEQ = "SKYNET_REL_LINK_TYPE_ID_SEQ";
   public static final String REL_LINK_ID_SEQ = "SKYNET_REL_LINK_ID_SEQ";
   public static final String GAMMA_ID_SEQ = "SKYNET_GAMMA_ID_SEQ";
   public static final String TRANSACTION_ID_SEQ = "SKYNET_TRANSACTION_ID_SEQ";
   public static final String IMPORT_ID_SEQ = "SKYNET_IMPORT_ID_SEQ";
   public static final String TTE_SESSION_SEQ = "TTE_SESSION_SEQ";

   public static final String[] sequenceNames =
         new String[] {ART_ID_SEQ, ART_TYPE_ID_SEQ, ATTR_BASE_TYPE_ID_SEQ, ATTR_PROVIDER_TYPE_ID_SEQ, ATTR_ID_SEQ,
               ATTR_TYPE_ID_SEQ, FACTORY_ID_SEQ, BRANCH_ID_SEQ, REL_LINK_TYPE_ID_SEQ, REL_LINK_ID_SEQ, GAMMA_ID_SEQ,
               TRANSACTION_ID_SEQ, IMPORT_ID_SEQ, TTE_SESSION_SEQ};

   private static final SequenceManager instance = new SequenceManager();

   private SequenceManager() {
      sequences = new HashMap<String, SequenceRange>(30);
   }

   public static SequenceManager getInstance() {
      return instance;
   }

   private SequenceRange getRange(String sequenceName) {
      SequenceRange range = sequences.get(sequenceName);
      if (range == null) {
         // do this to keep transaction id's sequential in the face of concurrent transaction by multiple users
         range = new SequenceRange(!sequenceName.equals(TRANSACTION_ID_SEQ));
         sequences.put(sequenceName, range);
      }
      return range;
   }

   private void prefetch(String sequenceName) throws OseeDataStoreException {
      SequenceRange range = getRange(sequenceName);

      long lastValue = -1;
      boolean gotSequence = false;
      OseeConnection connection = OseeDbConnection.getConnection();
      while (!gotSequence) {
         lastValue = getSequence(connection, sequenceName);
         gotSequence = updateSequenceValue(connection, sequenceName, lastValue + range.prefetchSize, lastValue);
      }
      connection.close();
      range.updateRange(lastValue);
   }

   private boolean updateSequenceValue(Connection connection, String sequenceName, long value, long lastValue) throws OseeDataStoreException {
      return ConnectionHandler.runPreparedUpdate(connection, UPDATE_SEQUENCE, value, sequenceName, lastValue) == 1;
   }

   private boolean insertSequenceValue(String sequenceName, long value) throws OseeDataStoreException {
      return ConnectionHandler.runPreparedUpdate(INSERT_SEQUENCE, value, sequenceName) == 1;
   }

   private long getSequence(Connection connection, String sequenceName) throws OseeDataStoreException {
      long seq = ConnectionHandler.runPreparedQueryFetchLong(connection, -1, QUERY_SEQUENCE, sequenceName);
      if (seq == -1) {
         throw new OseeDataStoreException("Sequence name [" + sequenceName + "] was not found");
      }
      return seq;
   }

   public static synchronized long getNextSequence(String sequenceName) throws OseeDataStoreException {
      SequenceRange range = instance.getRange(sequenceName);
      if (range.lastAvailable == 0) {
         instance.prefetch(sequenceName);
      }

      range.currentValue++;
      if (range.currentValue == range.lastAvailable) {
         range.lastAvailable = 0;
      }
      return range.currentValue;
   }

   public void initializeSequence(String sequenceName) throws OseeDataStoreException {
      SequenceRange range = getRange(sequenceName);
      range.lastAvailable = 0;
      insertSequenceValue(sequenceName, 0);
   }

   public static int getNextSessionId() throws OseeDataStoreException {
      return (int) getNextSequence(TTE_SESSION_SEQ);
   }

   public static int getNextTransactionId() throws OseeDataStoreException {
      return (int) getNextSequence(TRANSACTION_ID_SEQ);
   }

   public static int getNextArtifactId() throws OseeDataStoreException {
      return (int) getNextSequence(ART_ID_SEQ);
   }

   public static int getNextGammaId() throws OseeDataStoreException {
      return (int) getNextSequence(GAMMA_ID_SEQ);
   }

   public static int getNextArtifactTypeId() throws OseeDataStoreException {
      return (int) getNextSequence(ART_TYPE_ID_SEQ);
   }

   public static int getNextAttributeBaseTypeId() throws OseeDataStoreException {
      return (int) getNextSequence(ATTR_BASE_TYPE_ID_SEQ);
   }

   public static int getNextAttributeProviderTypeId() throws OseeDataStoreException {
      return (int) getNextSequence(ATTR_PROVIDER_TYPE_ID_SEQ);
   }

   public static int getNextAttributeId() throws OseeDataStoreException {
      return (int) getNextSequence(ATTR_ID_SEQ);
   }

   public static int getNextAttributeTypeId() throws OseeDataStoreException {
      return (int) getNextSequence(ATTR_TYPE_ID_SEQ);
   }

   public static int getNextFactoryId() throws OseeDataStoreException {
      return (int) getNextSequence(FACTORY_ID_SEQ);
   }

   public static int getNextBranchId() throws OseeDataStoreException {
      return (int) getNextSequence(BRANCH_ID_SEQ);
   }

   public static int getNextRelationTypeId() throws OseeDataStoreException {
      return (int) getNextSequence(REL_LINK_TYPE_ID_SEQ);
   }

   public static int getNextRelationId() throws OseeDataStoreException {
      return (int) getNextSequence(REL_LINK_ID_SEQ);
   }

   public static int getNextImportId() throws OseeDataStoreException {
      return (int) getNextSequence(IMPORT_ID_SEQ);
   }

   private class SequenceRange {
      private long currentValue;
      private long lastAvailable;
      private int prefetchSize;
      private final boolean aggressiveFetch;

      /**
       * @param aggressiveFetch
       */
      public SequenceRange(boolean aggressiveFetch) {
         super();
         this.prefetchSize = 1;
         this.aggressiveFetch = aggressiveFetch;
      }

      public void updateRange(long lastValue) {
         currentValue = lastValue;
         lastAvailable = lastValue + prefetchSize;

         if (aggressiveFetch) {
            prefetchSize *= 2; // next time grab twice as many
         }
      }
   }
}