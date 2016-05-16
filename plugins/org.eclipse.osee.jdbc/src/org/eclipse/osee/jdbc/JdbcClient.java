/*******************************************************************************
 * Copyright (c) 2014 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.jdbc;

import java.sql.SQLException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Roberto E. Escobar
 */
public interface JdbcClient {

   JdbcDbType getDbType();

   JdbcClientConfig getConfig();

   JdbcStatement getStatement();

   JdbcStatement getStatement(int resultSetType, int resultSetConcurrency);

   int runQuery(Consumer<JdbcStatement> consumer, String query, Object... data);

   int runQuery(Consumer<JdbcStatement> consumer, int fetchSize, String query, Object... data);

   int runQueryWithLimit(Consumer<JdbcStatement> consumer, int limit, String query, Object... data);

   <R> R fetchObject(R defaultValue, Function<JdbcStatement, R> function, String query, Object... data);

   int runBatchUpdate(String query, Iterable<Object[]> dataList);

   OseePreparedStatement getBatchStatement(String query) throws SQLException;

   OseePreparedStatement getBatchStatement(String query, int batchIncrementSize) throws SQLException;

   int runPreparedUpdate(String query, Object... data);

   <T> T runPreparedQueryFetchObject(T defaultValue, String query, Object... data);

   /**
    * <pre>
    * Invoke an SQL stored function which returns a value.
    * Function uses the format function_name (?,?,?) if the function has parameters
    * or function_name if no parameters. Default value cannot be null and must match
    * the desired return type.
    * </pre>
    */
   <T> T runFunction(T defaultValue, String function, Object... data);

   Map<String, String> getStatistics();

   //////////  QUESTIONABLE? MAYBE ONLY FOR TX SUPPORT
   JdbcConnection getConnection();

   JdbcStatement getStatement(JdbcConnection connection, boolean autoClose);

   JdbcStatement getStatement(JdbcConnection connection);

   int runPreparedUpdate(JdbcConnection connection, String query, Object... data);

   int runBatchUpdate(JdbcConnection connection, String query, Iterable<Object[]> dataList);

   OseePreparedStatement getBatchStatement(JdbcConnection connection, String query);

   OseePreparedStatement getBatchStatement(JdbcConnection connection, String query, int batchIncrementSize);

   <T> T runPreparedQueryFetchObject(JdbcConnection connection, T defaultValue, String query, Object... data);

   //////////////////////////////////////////////////

   void runTransaction(JdbcTransaction transaction);

   void runTransaction(JdbcConnection connection, JdbcTransaction dbWork) throws JdbcException;

   void migrate(JdbcMigrationOptions options, Iterable<JdbcMigrationResource> schemaResources);

   long getNextSequence(String sequenceName, boolean aggressiveFetch);

   void invalidateSequences();
}