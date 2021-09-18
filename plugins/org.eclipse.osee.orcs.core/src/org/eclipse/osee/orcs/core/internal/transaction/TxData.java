/*********************************************************************
 * Copyright (c) 2013 Boeing
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

package org.eclipse.osee.orcs.core.internal.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.GammaId;
import org.eclipse.osee.framework.core.data.HasBranchId;
import org.eclipse.osee.framework.core.data.UserId;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.jdbc.SqlTable;
import org.eclipse.osee.orcs.OrcsSession;
import org.eclipse.osee.orcs.core.ds.TupleData;
import org.eclipse.osee.orcs.core.internal.artifact.Artifact;
import org.eclipse.osee.orcs.core.internal.graph.GraphData;
import org.eclipse.osee.orcs.core.internal.relation.Relation;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.HasSession;

/**
 * @author Roberto E. Escobar
 * @author Megumi Telles
 */
public class TxData implements HasSession, HasBranchId {

   public static enum TxState {
      NEW_TX,
      COMMIT_STARTED,
      COMMITTED,
      COMMIT_FAILED;
   }

   private final OrcsSession session;
   private final GraphData graph;
   private final List<TupleData> tuples = new ArrayList<>();
   private final HashCollection<SqlTable, GammaId> tuplesToDelete = new HashCollection<>();
   private final HashMap<Long, Artifact> writeables = new HashMap<>();
   private final HashMap<Long, ArtifactReadable> readables = new HashMap<>();
   private final Set<Relation> relations = new HashSet<>();

   private UserId author;
   private String comment;

   private volatile boolean isCommitInProgress;
   private volatile TxState txState;

   public TxData(OrcsSession session, GraphData graph) {
      this.session = session;
      this.graph = graph;
      this.txState = TxState.NEW_TX;
   }

   public void clear() {
      isCommitInProgress = false;
      writeables.clear();
      readables.clear();
   }

   @Override
   public OrcsSession getSession() {
      return session;
   }

   @Override
   public BranchId getBranch() {
      return graph.getBranch();
   }

   public GraphData getGraph() {
      return graph;
   }

   public UserId getAuthor() {
      return author;
   }

   public String getComment() {
      return comment;
   }

   public TxState getTxState() {
      return txState;
   }

   public boolean isCommitInProgress() {
      return isCommitInProgress;
   }

   public void setAuthor(UserId author) {
      this.author = author;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public void setTxState(TxState txState) {
      this.txState = txState;
   }

   public void setCommitInProgress(boolean isCommitInProgress) {
      this.isCommitInProgress = isCommitInProgress;
   }

   public Artifact add(Artifact artifact) {
      return writeables.put(artifact.getId(), artifact);
   }

   public void add(ArtifactReadable artifact) {
      readables.put(artifact.getId(), artifact);
   }

   public void add(TupleData tupleData) {
      tuples.add(tupleData);
   }

   public List<TupleData> getTuplesToAdd() {
      return tuples;
   }

   public void deleteTuple(SqlTable tupleTable, GammaId gammaId) {
      tuplesToDelete.put(tupleTable, gammaId);
   }

   public HashCollection<SqlTable, GammaId> getTuplesToDelete() {
      return tuplesToDelete;
   }

   public Iterable<Artifact> getAllWriteables() {
      return writeables.values();
   }

   public Artifact getWriteable(ArtifactId artifactId) {
      return writeables.get(artifactId.getId());
   }

   public ArtifactReadable getReadable(ArtifactId artifactId) {
      return readables.get(artifactId.getId());
   }

   @Override
   public String toString() {
      return "TxData [session=" + session + ", graph=" + graph + ", author=" + author + ", comment=" + comment + ", isCommitInProgress=" + isCommitInProgress + ", txState=" + txState + "]";
   }

   public Set<Relation> getRelations() {
      return relations;
   }

   public void addRelation(Relation relation) {
      relations.add(relation);
   }
}