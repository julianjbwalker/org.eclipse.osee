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
package org.eclipse.osee.framework.search.engine.internal;

import java.util.List;
import java.util.Set;
import org.eclipse.osee.framework.search.engine.ISearchEngine;
import org.eclipse.osee.framework.search.engine.Options;
import org.eclipse.osee.framework.search.engine.data.AttributeSearch;
import org.eclipse.osee.framework.search.engine.data.IAttributeLocator;
import org.eclipse.osee.framework.search.engine.utility.AttributeDataStore;
import org.eclipse.osee.framework.search.engine.utility.AttributeDataStore.AttributeData;

/**
 * @author Roberto E. Escobar
 */
public class SearchEngine implements ISearchEngine {

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.search.engine.ISearchEngine#search(java.lang.String, org.eclipse.osee.framework.search.engine.Options)
    */
   @Override
   public String search(String searchString, Options options) throws Exception {
      AttributeSearch attributeSearch = new AttributeSearch(searchString, options);
      Set<IAttributeLocator> attributeLocators = attributeSearch.findMatches();
      List<AttributeData> attributeDatas = AttributeDataStore.getAttribute(attributeLocators);
      for (AttributeData attributeData : attributeDatas) {
         String value = attributeData.getValue();
         if (value.contains(searchString)) {
            System.out.println("Matches: " + attributeData.getArtId());
         }

         // GET ACTUAL ATTRIBUTE CONTENT
         // Perform Second Pass Search -- this needs to be extremely fast;
      }
      return "12345,2";
   }
}
