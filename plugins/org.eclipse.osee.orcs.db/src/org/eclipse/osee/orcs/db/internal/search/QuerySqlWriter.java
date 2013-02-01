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
package org.eclipse.osee.orcs.db.internal.search;

import java.util.List;
import org.eclipse.osee.framework.core.enums.TxChange;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.database.IOseeDatabaseService;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.DataPostProcessor;
import org.eclipse.osee.orcs.core.ds.QueryOptions;
import org.eclipse.osee.orcs.db.internal.SqlProvider;
import org.eclipse.osee.orcs.db.internal.sql.AbstractSqlWriter;
import org.eclipse.osee.orcs.db.internal.sql.SqlContext;
import org.eclipse.osee.orcs.db.internal.sql.SqlHandler;
import org.eclipse.osee.orcs.db.internal.sql.TableEnum;

public class QuerySqlWriter extends AbstractSqlWriter<QueryOptions> {

   private final int branchId;
   private final QueryType queryType;

   public QuerySqlWriter(Log logger, IOseeDatabaseService dbService, SqlProvider sqlProvider, SqlContext<QueryOptions, ? extends DataPostProcessor<?>> context, QueryType queryType, int branchId) {
      super(logger, dbService, sqlProvider, context);
      this.queryType = queryType;
      this.branchId = branchId;
   }

   private void writeSelectHelper() throws OseeCoreException {
      String txAlias = getAliasManager().getFirstAlias(TableEnum.TXS_TABLE);
      String artAlias = getAliasManager().getFirstAlias(TableEnum.ARTIFACT_TABLE);

      write("SELECT ");
      if (getOptions().isHistorical()) {
         write("max(%s.transaction_id) as transaction_id, %s.art_id, %s.branch_id", txAlias, artAlias, txAlias);
      } else {
         write("%s.art_id, %s.branch_id", artAlias, txAlias);
      }
   }

   @Override
   public void writeSelect(List<SqlHandler<?, QueryOptions>> handlers) throws OseeCoreException {
      if (queryType == QueryType.COUNT_ARTIFACTS) {
         if (getOptions().isHistorical()) {
            write("SELECT count(xTable.art_id) FROM (\n ");
            writeSelectHelper();
         } else {
            String artAlias = getAliasManager().getFirstAlias(TableEnum.ARTIFACT_TABLE);
            write("SELECT%s count(%s.art_id)", getSqlHint(), artAlias);
         }
      } else {
         writeSelectHelper();
      }
   }

   @Override
   public void writeGroupAndOrder() throws OseeCoreException {
      if (getOptions().isHistorical()) {
         String txAlias = getAliasManager().getFirstAlias(TableEnum.TXS_TABLE);
         String artAlias = getAliasManager().getFirstAlias(TableEnum.ARTIFACT_TABLE);

         write("\n GROUP BY %s.art_id, %s.branch_id", artAlias, txAlias);
      }
      if (queryType != QueryType.COUNT_ARTIFACTS) {
         String txAlias = getAliasManager().getFirstAlias(TableEnum.TXS_TABLE);
         String artAlias = getAliasManager().getFirstAlias(TableEnum.ARTIFACT_TABLE);

         write("\n ORDER BY %s.art_id, %s.branch_id", artAlias, txAlias);
      } else {
         if (getOptions().isHistorical()) {
            write("\n) xTable");
         }
      }
   }

   @Override
   public void writeTxBranchFilter(String txsAlias) throws OseeCoreException {
      writeTxFilter(txsAlias);
      if (branchId > 0) {
         write(" AND ");
         write(txsAlias);
         write(".branch_id = ?");
         addParameter(branchId);
      }
   }

   private void writeTxFilter(String txsAlias) throws OseeCoreException {
      if (getOptions().isHistorical()) {
         write(txsAlias);
         write(".transaction_id <= ?");
         addParameter(getOptions().getFromTransaction());
         if (!getOptions().areDeletedIncluded()) {
            writeAndLn();
            write(txsAlias);
            write(".tx_current");
            write(" IN (");
            write(String.valueOf(TxChange.CURRENT.getValue()));
            write(", ");
            write(String.valueOf(TxChange.NOT_CURRENT.getValue()));
            write(")");
         }
      } else {
         write(txsAlias);
         write(".tx_current");
         if (getOptions().areDeletedIncluded()) {
            write(" IN (");
            write(String.valueOf(TxChange.CURRENT.getValue()));
            write(", ");
            write(String.valueOf(TxChange.DELETED.getValue()));
            write(", ");
            write(String.valueOf(TxChange.ARTIFACT_DELETED.getValue()));
            write(")");
         } else {
            write(" = ");
            write(String.valueOf(TxChange.CURRENT.getValue()));
         }
      }
   }
}
