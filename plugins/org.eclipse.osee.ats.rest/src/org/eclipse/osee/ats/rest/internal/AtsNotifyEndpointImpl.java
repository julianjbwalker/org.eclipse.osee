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
package org.eclipse.osee.ats.rest.internal;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;
import org.eclipse.osee.ats.api.notify.AtsNotificationCollector;
import org.eclipse.osee.ats.api.notify.AtsNotifyEndpointApi;
import org.eclipse.osee.ats.impl.IAtsServer;
import org.eclipse.osee.framework.jdk.core.util.AHTML;

/**
 * @author Donald G. Dunne
 */
public class AtsNotifyEndpointImpl implements AtsNotifyEndpointApi {

   private final IAtsServer atsServer;

   public AtsNotifyEndpointImpl(IAtsServer atsServer) {
      this.atsServer = atsServer;
   }

   @GET
   public String get() {
      return AHTML.simplePage("ATS Notify Resource");
   }

   @Override
   public Response sendNotifications(AtsNotificationCollector notifications) {
      atsServer.sendNotifications(notifications);
      return Response.ok().build();
   }

}
