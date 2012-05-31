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
package org.eclipse.osee.orcs.core.internal.search.callable;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.data.ResultSetList;
import org.eclipse.osee.framework.core.enums.LoadLevel;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.LoadOptions;
import org.eclipse.osee.orcs.core.ds.QueryContext;
import org.eclipse.osee.orcs.core.ds.QueryData;
import org.eclipse.osee.orcs.core.ds.QueryEngine;
import org.eclipse.osee.orcs.core.ds.QueryPostProcessor;
import org.eclipse.osee.orcs.core.internal.OrcsObjectLoader;
import org.eclipse.osee.orcs.core.internal.SessionContext;
import org.eclipse.osee.orcs.core.internal.search.QueryCollector;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.AttributeReadable;
import org.eclipse.osee.orcs.search.Match;

/**
 * @author Roberto E. Escobar
 */
public class SearchCallable extends AbstractSearchCallable<ResultSet<ArtifactReadable>> {

   private QueryContext queryContext;

   public SearchCallable(Log logger, QueryEngine queryEngine, QueryCollector collector, OrcsObjectLoader objectLoader, SessionContext sessionContext, LoadLevel loadLevel, QueryData queryData) {
      super(logger, queryEngine, collector, objectLoader, sessionContext, loadLevel, queryData);
   }

   @Override
   protected ResultSet<ArtifactReadable> innerCall() throws Exception {
      QueryContext queryContext = queryEngine.create(sessionContext.getSessionId(), queryData);
      LoadOptions loadOptions =
         new LoadOptions(queryData.getOptions().isHistorical(), queryData.getOptions().areDeletedIncluded(), loadLevel);
      checkForCancelled();
      List<ArtifactReadable> artifacts = objectLoader.load(this, queryContext, loadOptions, sessionContext);

      List<ArtifactReadable> results;
      if (!queryContext.getPostProcessors().isEmpty()) {
         results = new ArrayList<ArtifactReadable>();
         for (QueryPostProcessor processor : queryContext.getPostProcessors()) {
            processor.setItemsToProcess(artifacts);
            checkForCancelled();
            List<Match<ArtifactReadable, AttributeReadable<?>>> matches = processor.call();
            for (Match<ArtifactReadable, AttributeReadable<?>> match : matches) {
               checkForCancelled();
               results.add(match.getItem());
            }
         }
      } else {
         results = artifacts;
      }
      return new ResultSetList<ArtifactReadable>(results);
   }

   @Override
   public void setCancel(boolean isCancelled) {
      super.setCancel(isCancelled);
      if (queryContext != null && !queryContext.getPostProcessors().isEmpty()) {
         for (QueryPostProcessor processor : queryContext.getPostProcessors()) {
            processor.setCancel(true);
         }
      }
   }

   @Override
   protected int getCount(ResultSet<ArtifactReadable> results) throws Exception {
      return results.getList().size();
   }
}
