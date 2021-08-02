/*********************************************************************
* Copyright (c) 2021 Boeing
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://no-click.mil/?https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Boeing - initial API and implementation
**********************************************************************/

package org.eclipse.osee.orcs.rest.internal;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.eclipse.osee.framework.core.data.ArtifactId;
import org.eclipse.osee.framework.core.data.BranchId;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.data.UserId;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.rest.model.RelationEndpoint;
import org.eclipse.osee.orcs.transaction.TransactionBuilder;

/**
 * @author Hugo Trejo, Torin Grenda, David Miller
 */
public class RelationEndpointImpl implements RelationEndpoint {

   private final OrcsApi orcsApi;
   private final BranchId branch;
   private final UserId account;
   private final UriInfo uriInfo;

   public RelationEndpointImpl(OrcsApi orcsApi, BranchId branch, UserId accountId, UriInfo uriInfo) {
      this.orcsApi = orcsApi;
      this.account = accountId;
      this.uriInfo = uriInfo;
      this.branch = branch;
   }

   @Override
   public Response createRelationByType(ArtifactId sideA, ArtifactId sideB, RelationTypeToken relationType) {
      TransactionBuilder tx = orcsApi.getTransactionFactory().createTransaction(branch, account,
         String.format("RelationEndpoint REST api creating relation %s between %s and %s", relationType.getName(),
            sideA.getIdString(), sideB.getIdString()));
      tx.relate(sideA, relationType, sideB);
      tx.commit();

      return Response.ok().build();
   }
}