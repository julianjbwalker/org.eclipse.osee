/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests;

import org.eclipse.osee.ats.api.agile.AgileEndpointApi;
import org.eclipse.osee.ats.api.country.CountryEndpointApi;
import org.eclipse.osee.ats.api.insertion.InsertionActivityEndpointApi;
import org.eclipse.osee.ats.api.insertion.InsertionEndpointApi;
import org.eclipse.osee.ats.api.program.ProgramEndpointApi;
import org.eclipse.osee.ats.api.task.AtsTaskEndpointApi;
import org.eclipse.osee.ats.core.client.IAtsClient;
import org.eclipse.osee.framework.core.client.OseeClientProperties;
import org.eclipse.osee.jaxrs.client.JaxRsClient;
import org.eclipse.osee.jaxrs.client.JaxRsWebTarget;
import org.eclipse.osee.orcs.rest.model.OrcsWriterEndpoint;

/**
 * @author Donald G. Dunne
 */
public class AtsClientService {

   private static IAtsClient atsClient;
   private static AgileEndpointApi agile;
   private static JaxRsWebTarget target;
   private static OrcsWriterEndpoint orcsWriter;
   private static CountryEndpointApi countryEp;
   private static ProgramEndpointApi programEp;
   private static InsertionEndpointApi insertionEp;
   private static InsertionActivityEndpointApi insertionActivityEp;
   private static AtsTaskEndpointApi taskEp;

   public void setAtsClient(IAtsClient atsClient) {
      AtsClientService.atsClient = atsClient;
   }

   public static IAtsClient get() {
      return atsClient;
   }

   private static JaxRsWebTarget getTarget() {
      if (target == null) {
         String appServer = OseeClientProperties.getOseeApplicationServer();
         String atsUri = String.format("%s/ats", appServer);
         JaxRsClient jaxRsClient = JaxRsClient.newBuilder().build();
         target = jaxRsClient.target(atsUri);
      }
      return target;
   }

   public static AgileEndpointApi getAgile() {
      if (agile == null) {
         agile = getTarget().newProxy(AgileEndpointApi.class);
      }
      return agile;
   }

   public static OrcsWriterEndpoint getOrcsWriter() {
      if (orcsWriter == null) {
         String appServer = OseeClientProperties.getOseeApplicationServer();
         String orcsUri = String.format("%s/orcs", appServer);
         JaxRsClient jaxRsClient = JaxRsClient.newBuilder().build();
         orcsWriter = jaxRsClient.target(orcsUri).newProxy(OrcsWriterEndpoint.class);
      }
      return orcsWriter;
   }

   public static CountryEndpointApi getCountryEp() {
      if (countryEp == null) {
         countryEp = getTarget().newProxy(CountryEndpointApi.class);
      }
      return countryEp;
   }

   public static ProgramEndpointApi getProgramEp() {
      if (programEp == null) {
         programEp = getTarget().newProxy(ProgramEndpointApi.class);
      }
      return programEp;
   }

   public static InsertionEndpointApi getInsertionEp() {
      if (insertionEp == null) {
         insertionEp = getTarget().newProxy(InsertionEndpointApi.class);
      }
      return insertionEp;
   }

   public static InsertionActivityEndpointApi getInsertionActivityEp() {
      if (insertionActivityEp == null) {
         insertionActivityEp = getTarget().newProxy(InsertionActivityEndpointApi.class);
      }
      return insertionActivityEp;
   }

   public static AtsTaskEndpointApi getTaskEp() {
      if (taskEp == null) {
         taskEp = getTarget().newProxy(AtsTaskEndpointApi.class);
      }
      return taskEp;
   }

}
