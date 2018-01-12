/*******************************************************************************
 * Copyright (c) 2015 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.config;

import org.eclipse.osee.ats.client.integration.tests.ats.resource.AbstractRestTest;
import org.eclipse.osee.ats.demo.api.DemoInsertion;
import org.junit.Test;

/**
 * Unit Test for {@link InsertionResource}
 *
 * @author Donald G. Dunne
 */
public class InsertionResourceTest extends AbstractRestTest {

   private void testInsertionUrl(String url, int size, boolean hasDescription) {
      testUrl(url, size, "COMM", "ats.Description", hasDescription);
   }

   @Test
   public void testAtsInsertionsRestCall() {
      testInsertionUrl("/ats/insertion", 12, false);
   }

   @Test
   public void testAtsInsertionsDetailsRestCall() {
      testInsertionUrl("/ats/insertion/details", 12, true);
   }

   @Test
   public void testAtsInsertionRestCall() {
      testInsertionUrl("/ats/insertion/" + DemoInsertion.sawComm.getId(), 1, false);
   }

   @Test
   public void testAtsInsertionDetailsRestCall() {
      testInsertionUrl("/ats/insertion/" + DemoInsertion.sawComm.getId() + "/details", 1, true);
   }
}