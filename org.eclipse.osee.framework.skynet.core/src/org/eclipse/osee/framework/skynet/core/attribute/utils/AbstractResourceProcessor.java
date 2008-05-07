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
package org.eclipse.osee.framework.skynet.core.attribute.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.eclipse.osee.framework.skynet.core.attribute.providers.DataStore;
import org.eclipse.osee.framework.skynet.core.linking.ResourceProcessor;
import org.eclipse.osee.framework.skynet.core.linking.ResourceProcessor.AcquireResult;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractResourceProcessor {

   protected abstract URL getAcquireURL(DataStore dataStore) throws Exception;

   protected abstract URL getDeleteURL(DataStore dataStore) throws Exception;

   protected abstract URL getStorageURL(DataStore dataStore) throws Exception;

   public void saveResource(DataStore dataStore) throws Exception {
      InputStream inputStream = null;
      try {
         URL url = getStorageURL(dataStore);
         inputStream = dataStore.getInputStream();
         URI uri =
               ResourceProcessor.save(url, inputStream, dataStore.getContentType(), dataStore.getEncoding());
         if (uri != null) {
            dataStore.setLocator(uri.toASCIIString());
         }
      } catch (Exception ex) {
         throw new Exception("Error saving resource", ex);
      } finally {
         if (inputStream != null) {
            inputStream.close();
         }
      }
   }

   public void acquire(DataStore dataStore) throws Exception {
      int code = -1;
      try {
         URL url = getAcquireURL(dataStore);
         AcquireResult result = ResourceProcessor.acquire(url);
         code = result.getCode();
         if (code == HttpURLConnection.HTTP_OK) {
            dataStore.setContent(result.getData(), "", result.getContentType(), result.getEncoding());
         }
      } catch (Exception ex) {
         throw new Exception(String.format("Error acquiring resource: [%s] - status code: [%s]",
               dataStore.getLocator(), code), ex);
      }
   }

   public void purge(DataStore dataStore) throws Exception {
      int code = -1;
      try {
         URL url = getDeleteURL(dataStore);
         String response = ResourceProcessor.delete(url);
         if (response != null && response.equals("Deleted: " + dataStore.getLocator())) {
            dataStore.clear();
         }
      } catch (Exception ex) {
         throw new Exception(String.format("Error deleting resource: [%s] - status code: [%s]", dataStore.getLocator(),
               code), ex);
      }
   }
}
