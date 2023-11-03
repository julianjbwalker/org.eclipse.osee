/*********************************************************************
 * Copyright (c) 2023 Boeing
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

package org.eclipse.osee.testscript;

import java.util.Collection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.AttributeTypeToken;
import org.eclipse.osee.framework.jdk.core.annotation.Swagger;
import org.eclipse.osee.testscript.internal.ScriptSetToken;

/**
 * @author Stephen J. Molaro
 */
@Path("set")
@Swagger
public interface ScriptSetEndpoint {
   @GET()
   @Produces(MediaType.APPLICATION_JSON)
   Collection<ScriptSetToken> getAllScriptSets(@QueryParam("filter") String filter,
      @QueryParam("viewId") ArtifactId viewId, @QueryParam("pageNum") long pageNum, @QueryParam("count") long pageSize,
      @QueryParam("orderByAttributeType") AttributeTypeToken orderByAttributeType,
      @QueryParam("activeOnly") boolean activeOnly);

   @GET()
   @Path("count")
   @Produces(MediaType.APPLICATION_JSON)
   int getCount(@QueryParam("filter") String filter, @QueryParam("viewId") ArtifactId viewId);

   @GET()
   @Path("{id}")
   @Produces(MediaType.APPLICATION_JSON)
   ScriptSetToken getScriptProgram(@PathParam("id") ArtifactId scriptProgramId);

}